package org.jasig.ssp.service.impl; // NOPMD

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;
import org.jasig.ssp.dao.MessageDao;
import org.jasig.ssp.model.Message;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.SubjectAndBody;
import org.jasig.ssp.service.MessageService;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonService;
import org.jasig.ssp.service.SecurityService;
import org.jasig.ssp.service.reference.ConfigService;
import org.jasig.ssp.util.collections.Pair;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortDirection;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.util.transaction.WithTransaction;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Message service implementation for sending e-mails (messages) to various
 * parties.
 */
@Service
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

	private static final Integer QUEUE_BATCH_SIZE = 25;

	private static final long INTER_QUEUE_BATCH_SLEEP = 200;

	@Autowired
	private transient JavaMailSender javaMailSender;

	@Autowired
	private transient MessageDao messageDao;

	@Autowired
	private transient PersonService personService;

	@Autowired
	private transient SecurityService securityService;

	@Autowired
	private transient ConfigService configService;

	@Autowired
	private transient WithTransaction withTransaction;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MessageServiceImpl.class);

	@Value("#{contextProperties.applicationMode}")
	private transient String applicationMode;



	/**
	 * Gets the global BCC e-mail address from the application configuration
	 * information.
	 * 
	 * @return the global BCC e-mail address from the application configuration
	 *         information
	 */
	public String getBcc() {
		final String bcc = configService.getByNameEmpty("bcc_email_address");
		if (!bcc.isEmpty() && !bcc.equalsIgnoreCase("noone@test.com")) {
			return bcc;
		}
		return null;
	}

	@Override
	/**
	 * Always returns true in TEST applicationMode
	 */
	public boolean shouldSendMail() {
		if ("TEST".equals(applicationMode)) {
			return true;
		}

		final String shouldSendMail = configService.getByNameNull("send_mail");
		if (shouldSendMail != null) {
			return Boolean.valueOf(shouldSendMail);
		}

		return false;
	}

	/**
	 * Create a new message.
	 * 
	 * @param subjAndBody
	 *            SubjectAndBody subjAndBody
	 * @return A new message for the specified SubjectAndBody
	 * @throws ObjectNotFoundException
	 *             If the current user or administrator could not be loaded.
	 */
	private Message createMessage(
			final SubjectAndBody subjAndBody)
			throws ObjectNotFoundException {

		final Message message = new Message(subjAndBody);

		Person person = null; // NOPMD by jon.adams on 5/17/12 9:42 AM
		if (securityService.isAuthenticated()) {
			person = securityService.currentUser().getPerson();
		} else {
			// E-mails sent by anonymous users are sent by the administrator
			person = personService.get(Person.SYSTEM_ADMINISTRATOR_ID);
		}

		message.setSender(person);
		message.setCreatedBy(person);
		return message;
	}

	@Override
	@Transactional(readOnly = false)
	public Message createMessage(@NotNull final Person to,
			final String emailCC, final SubjectAndBody subjAndBody)
			throws ObjectNotFoundException, SendFailedException,
			ValidationException {

		if (to == null) {
			throw new ValidationException("Recipient missing.");
		}

		if (to.getPrimaryEmailAddress() == null) {
			throw new ValidationException(
					"Recipient primary e-mail address is missing.");
		}

		final Message message = createMessage(subjAndBody);

		message.setRecipient(to);
		message.setCarbonCopy(emailCC);

		return messageDao.save(message);
	}

	@Override
	public Message createMessage(@NotNull final String to,
			final String emailCC,
			@NotNull final SubjectAndBody subjAndBody)
			throws ObjectNotFoundException {

		final Message message = createMessage(subjAndBody);

		message.setRecipientEmailAddress(to);
		message.setCarbonCopy(emailCC);

		return messageDao.save(message);
	}

	@Override
	@Scheduled(fixedDelay = 150000)
	// run 2.5 minutes after the end of the last invocation
	public void sendQueuedMessages() {
		LOGGER.info("BEGIN : sendQueuedMessages()");

		int startRow = 0;
		final AtomicReference<SortingAndPaging> sap =
				new AtomicReference<SortingAndPaging>();
		sap.set(new SortingAndPaging(ObjectStatus.ACTIVE, startRow, QUEUE_BATCH_SIZE,
				null, null, null));
		// process each batch in its own transaction... don't want to hold
		// a single transaction open while processing what is effectively
		// an unbounded number of messages.
		while (true) {
			Pair<PagingWrapper<Message>, Collection<Throwable>> rslt =
					withTransaction.withTransactionAndUncheckedExceptions(
					new Callable<Pair<PagingWrapper<Message>, Collection<Throwable>>>() {
				@Override
				public Pair<PagingWrapper<Message>, Collection<Throwable>> call()
						throws Exception {
					return sendQueuedMessageBatch(sap.get());
				}
			});
			PagingWrapper<Message> msgsHandled = rslt.getFirst();
			if ( msgsHandled.getRows() == null ||
					msgsHandled.getRows().size() < QUEUE_BATCH_SIZE ) {
				break;
			}
			// Are potentially more msgs to handle and we know at least one
			// msg in the previous batch errored out. go ahead and grab another
			// full batch. Grabbing a full batch avoids slowdown when enough
			// errors accumulate to dramatically reduce the number of
			// *potentially* valid messages in the previous batch.
			Collection<Throwable> errors = rslt.getSecond();
			if ( errors != null && !(errors.isEmpty())) {
				startRow += msgsHandled.getRows().size();
				sap.set(new SortingAndPaging(ObjectStatus.ACTIVE, startRow,
						QUEUE_BATCH_SIZE,
						null, null, null));
				// lets not get into an excessively tight email loop
				maybePauseBetweenQueueBatches();
			} else {
				break;
			}
		}

		LOGGER.info("END : sendQueuedMessages()");
	}

	private Pair<PagingWrapper<Message>, Collection<Throwable>>
	sendQueuedMessageBatch(SortingAndPaging sap) {
		LinkedList<Throwable> errors = Lists.newLinkedList();
		final PagingWrapper<Message> messages = messageDao.queued(sap);
		for (final Message message : messages ) {
			try {
				sendMessage(message);
			} catch (final ObjectNotFoundException e) {
				LOGGER.error("Could not load current user or administrator.", e);
				errors.add(e);
			} catch (final SendFailedException e) {
				LOGGER.error("Could not send queued message.", e);
				errors.add(e);
			}
		}
		return new Pair<PagingWrapper<Message>, Collection<Throwable>>(messages, errors);
	}

	private void maybePauseBetweenQueueBatches() {
		if ( INTER_QUEUE_BATCH_SLEEP > 0 ) {
			try {
				Thread.sleep(INTER_QUEUE_BATCH_SLEEP);
			} catch ( InterruptedException e ) {
				// reassert
				Thread.currentThread().interrupt();
				throw new RuntimeException("Abandoning message queue"
						+ " processing because job thread was interrupted.", e);
			}
		}
	}

	/**
	 * Validate e-mail address via {@link EmailValidator}.
	 * 
	 * @param email
	 *            E-mail address to validate
	 * @return True if the e-mail is valid
	 */
	protected boolean validateEmail(final String email) {
		final EmailValidator emailValidator = EmailValidator.getInstance();
		return emailValidator.isValid(email);
	}

	@Override
	public boolean sendMessage(@NotNull final Message message)
			throws ObjectNotFoundException, SendFailedException {

		LOGGER.info("BEGIN : sendMessage()");
		LOGGER.info("Sending message: {}", message.toString());

		try {
			final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
					mimeMessage);

			mimeMessageHelper.setFrom(personService.get(
					Person.SYSTEM_ADMINISTRATOR_ID).getEmailAddressWithName());
			// We just happen to know that getEmailAddressWithName() uses the
			// primary address. This could probably be handled better. W/o
			// the blank string check, though, javax.mail will blow up
			// w/ a AddressException
			String replyTo = message.getSender().getPrimaryEmailAddress();
			if ( StringUtils.isNotBlank(replyTo) ) {
				mimeMessageHelper.setReplyTo(message.getSender()
						.getEmailAddressWithName());
			}

			if (message.getRecipient() != null) { // NOPMD by jon.adams
				mimeMessageHelper.setTo(message.getRecipient()
						.getEmailAddressWithName());
			} else if (message.getRecipientEmailAddress() != null) { // NOPMD
				mimeMessageHelper.setTo(message.getRecipientEmailAddress());
			} else {
				return false;
			}

			if (!validateEmail(message.getRecipientEmailAddress())) {
				throw new SendFailedException("Recipient Email Address '"
						+ message.getRecipientEmailAddress() + "' is invalid");
			}

			if (!StringUtils.isEmpty(message.getCarbonCopy())) { // NOPMD
				try {
					mimeMessageHelper.setCc(message.getCarbonCopy());
				} catch ( MessagingException e ) {
					LOGGER.warn("Invalid carbon copy address: '{}'. Will"
							+ " attempt to send message anyway.",
							message.getCarbonCopy(), e);
				}
			} else if (!StringUtils.isEmpty(getBcc())) {
				final String bcc = getBcc();
				try {
					mimeMessageHelper.setBcc(bcc);
				} catch ( MessagingException e ) {
					LOGGER.warn("Invalid BCC address: '{}'. Will"
							+ " attempt to send message anyway.", bcc, e);
				}
			}

			mimeMessageHelper.setSubject(message.getSubject());
			mimeMessageHelper.setText(message.getBody());
			mimeMessage.setContent(message.getBody(), "text/html");

			send(mimeMessage);

			message.setSentDate(new Date());
			messageDao.save(message);
		} catch (final MessagingException e) {
			LOGGER.error("ERROR : sendMessage() : {}", e);
			throw new SendFailedException(
					"The message parameters were invalid.", e);
		}

		LOGGER.info("END : sendMessage()");
		return true;
	}

	private void send(final MimeMessage mimeMessage) throws SendFailedException {
		if (shouldSendMail()) {
			LOGGER.debug("_ : JavaMailSender.send()");
			try {
				javaMailSender.send(mimeMessage);
			} catch (final MailSendException e) {
				try {
					LOGGER.warn("Send failed, going to wait and try again");
					Thread.sleep(20 * 1000L);
					javaMailSender.send(mimeMessage);
				} catch (final InterruptedException e1) {
					LOGGER.error("Thread error", e1);
				} catch (final MailSendException e2) {
					throw new SendFailedException("Unable to send message.", e2);
				}
			}
		} else {
			LOGGER.warn("_ : JavaMailSender was not called; message was marked sent but was not actually sent.  To enable mail, update the configuration of the app.");
		}
	}
}