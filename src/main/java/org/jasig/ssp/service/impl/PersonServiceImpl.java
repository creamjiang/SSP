package org.jasig.ssp.service.impl;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Sets;
import org.hibernate.exception.ConstraintViolationException;
import org.jasig.ssp.dao.ObjectExistsException;
import org.jasig.ssp.dao.PersonDao;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.external.ExternalPerson;
import org.jasig.ssp.security.PersonAttributesResult;
import org.jasig.ssp.security.exception.UnableToCreateAccountException;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonAttributesService;
import org.jasig.ssp.service.PersonService;
import org.jasig.ssp.service.external.ExternalPersonService;
import org.jasig.ssp.service.external.RegistrationStatusByTermService;
import org.jasig.ssp.service.tool.IntakeService;
import org.jasig.ssp.transferobject.reports.AddressLabelSearchTO;
import org.jasig.ssp.util.collections.Pair;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.util.transaction.WithTransaction;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import org.springframework.util.StringUtils;

import javax.portlet.PortletRequest;

/**
 * Person service implementation
 * 
 * @author jon.adams
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService {

	public static final boolean ALL_AUTHENTICATED_USERS_CAN_CREATE_ACCOUNT = true;

	public static final String PERMISSION_TO_CREATE_ACCOUNT = "ROLE_CAN_CREATE";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PersonServiceImpl.class);

	private static final Logger TIMING_LOGGER = LoggerFactory
			.getLogger("timing." + PersonServiceImpl.class.getName());

	@Autowired
	private transient PersonDao dao;

	@Autowired
	private transient PersonAttributesService personAttributesService;

	@Autowired
	private transient RegistrationStatusByTermService registrationStatusByTermService;

	@Autowired
	private transient ExternalPersonService externalPersonService;

	@Autowired
	private transient WithTransaction withTransaction;

	private static interface PersonAttributesLookup {
		public PersonAttributesResult lookupPersonAttributes(String username)
				throws ObjectNotFoundException;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Person createUserAccount(final String username,
			final Collection<GrantedAuthority> authorities) {

		return createUserAccount(username, authorities, new PersonAttributesLookup() {
			@Override
			public PersonAttributesResult lookupPersonAttributes(String username)
			throws ObjectNotFoundException {
				return personAttributesService.getAttributes(username);
			}
		});

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Person createUserAccountForCurrentPortletUser(String username,
			final PortletRequest portletRequest)
			throws UnableToCreateAccountException {

		if ( !(StringUtils.hasText(username)) ) {
			@SuppressWarnings("unchecked") Map<String,String> userInfo =
					(Map<String,String>) portletRequest.getAttribute(PortletRequest.USER_INFO);
			username = userInfo
					.get(PortletRequest.P3PUserInfos.USER_LOGIN_ID.toString());
		}

		if ( !(StringUtils.hasText(username)) ) {
			throw new UnableToCreateAccountException(
					"Cannot find a username to assign to new account.");
		}

		Collection<GrantedAuthority> authorities = Sets.newHashSet();
		return createUserAccount(username, authorities, new PersonAttributesLookup() {
			@Override
			public PersonAttributesResult lookupPersonAttributes(String username)
			throws ObjectNotFoundException {
				return personAttributesService.getAttributes(username, portletRequest);
			}
		});

	}

	private Person createUserAccount(String username,
			Collection<GrantedAuthority> authorities,
			PersonAttributesLookup personAttributesLookup) {

		Person person = null;

		if (hasAccountCreationPermission(authorities)) {
			person = new Person();
			person.setEnabled(true);
			person.setUsername(username);

			try {
				// Get the Person Attributes to create the person
				final PersonAttributesResult attr =
						personAttributesLookup.lookupPersonAttributes(username);
				person.setSchoolId(attr.getSchoolId());
				person.setFirstName(attr.getFirstName());
				person.setLastName(attr.getLastName());
				person.setPrimaryEmailAddress(attr.getPrimaryEmailAddress());

				// try to create the person
				try {
					person = create(person);
					LOGGER.info("Successfully Created Account for {}",
							username);
				} catch (final ObjectExistsException oee) {
					// Don't have to give up in the same way do in the
					// ConstraintViolationException catch below b/c we happen to
					// know an insert hasn't been attempted yet under the
					// current create() impl.
					person = personFromUsername(username);
				}

			} catch (final ObjectNotFoundException onfe) {
				// personAttributesService may throw this exception, if so,
				// we can't create the user.
				throw new UnableToCreateAccountException(// NOPMD
						"Unable to pull required attributes", onfe);

			} catch (final ConstraintViolationException sqlException) {
				// if we received a constraintViolationException of
				// unique_person_username, then the user might have been
				// added since we started. (If using SQLServer this will only
				// work if you're running with ExtendedSQLServer*Dialect. Else
				// getConstraintName() will always be null.)
				if (sqlException.getConstraintName().equalsIgnoreCase(
						"unique_person_username")) {
					LOGGER.info("Tried to add a user that was already present");

					// SSP-397. Have to throw something to rollback the
					// transaction, else Spring/Hib will attempt a commit when
					// this method returns, which Postgres will refuse with a
					// "current transaction is aborted" message and the caller
					// will get an opaque HibernateJdbcException. With an
					// ObjectExistsException the client has at least some
					// clue as to a reasonable recovery path.
					throw new ObjectExistsException("Account with user name "
							+ username + " already exists.");
				}

				// Also SSP-397
				throw sqlException;

			} catch (final Exception genException) {
				// This exception seems to get swallowed... trying to reveal
				// it.
				throw new UnableToCreateAccountException( // NOPMD
						"Unable to Create Account for login.", genException);
			}

		} else {
			throw new UnableToCreateAccountException( // NOPMD
					// already know the account was not found
					"Insufficient Permissions to create Account");
		}

		return person;
	}

	private boolean hasAccountCreationPermission(
			final Collection<GrantedAuthority> authorities) {
		boolean permission = ALL_AUTHENTICATED_USERS_CAN_CREATE_ACCOUNT;

		// if already true, skip permission check
		if (permission) {
			return true;
		}

		for (final GrantedAuthority auth : authorities) {
			if (auth.getAuthority().equals(PERMISSION_TO_CREATE_ACCOUNT)) {
				permission = true;
				break;
			}
		}

		return permission;
	}

	@Override
	public PagingWrapper<Person> getAll(final SortingAndPaging sAndP) {
		final PagingWrapper<Person> people = dao.getAll(sAndP);
		additionalAttribsForStudents(people);
		return people;
	}

	/**
	 * Retrieves the specified Person.
	 * 
	 * @param id
	 *            Required identifier for the Person to retrieve. Can not be
	 *            null.
	 * @exception ObjectNotFoundException
	 *                If the supplied identifier does not exist in the database
	 *                or is not {@link ObjectStatus#ACTIVE}.
	 * @return The specified Person instance.
	 */
	@Override
	public Person get(final UUID id) throws ObjectNotFoundException {
		final Person person = dao.get(id);
		return additionalAttribsForStudent(person);
	}

	@Override
	public Person load(final UUID id) {
		return dao.load(id);
	}

	@Override
	public Person getBySchoolId(final String schoolId)
			throws ObjectNotFoundException {
		try {
			return additionalAttribsForStudent(dao.getBySchoolId(schoolId));
		} catch (final ObjectNotFoundException e) {
			final ExternalPerson externalPerson = externalPersonService
					.getBySchoolId(schoolId);
			if (externalPerson == null) {
				throw new ObjectNotFoundException( // NOPMD
						"Unable to find person by schoolId: " + schoolId,
						"Person");
			}

			final Person person = new Person();
			externalPersonService.updatePersonFromExternalPerson(person,
					externalPerson);
			return additionalAttribsForStudent(person);
		}
	}

	@Override
	public Person personFromUsername(final String username)
			throws ObjectNotFoundException {

		final Person obj = dao.fromUsername(username);
		additionalAttribsForStudent(obj);

		if (null == obj) {
			throw new ObjectNotFoundException(
					"Could not find person with username: " + username,
					"Person");
		}
		return obj;
	}

	/**
	 * Creates a new Person instance based on the supplied model.
	 * 
	 * @param obj
	 *            Model instance
	 * @throws ObjectExistsException
	 *             Thrown if any of the specified data has a unique key
	 *             violation.
	 */
	@Override
	public Person create(final Person obj) {

		LOGGER.debug("Creating User {}", obj);

		// Best to check for schoolId collisions 1st b/c proposed usernames
		// are sometimes just calculated values from the UI. Conflicts with
		// those calculated values are perfectly legitimate, but the UI will
		// then attempt to load *that* person record to help the user work
		// through the conflict. But what the user actually entered was a
		// schoolId, not a username, and the username conflict might have
		// been on a person record that doesn't have the schoolId they originally
		// requested. So if the UI isn't careful, it will end up potentially
		// reloading the screen with the wrong person record or, worse,
		// eventually overwriting the wrong person record.
		if (obj.getSchoolId() != null) {
			Person existing = null;
			try {
				existing = dao.getBySchoolId(obj.getSchoolId());
			} catch ( ObjectNotFoundException e ) {}
			if (null != existing) {
				throw new ObjectExistsException(Person.class.getName(),
						new Pair<String,String>("schoolId", obj.getSchoolId()).toMap());
			}
		}

		if (obj.getUsername() != null) {
			final Person existing = dao.fromUsername(obj.getUsername());
			if (null != existing) {
				throw new ObjectExistsException(Person.class.getName(),
						new Pair<String,String>("username", obj.getUsername()).toMap());
			}
		}

		Person person = null;
		try {
			person = dao.create(obj);
		} catch ( ConstraintViolationException e ) {
			final String constraintName = e.getConstraintName();
			if ( "uq_person_school_id".equals(constraintName) ) {
				throw new ObjectExistsException(Person.class.getName(),
						new Pair<String,String>("schoolId", obj.getSchoolId()).toMap(),
						e);
			}
			if ( "unique_person_username".equals(constraintName) ) {
				throw new ObjectExistsException(Person.class.getName(),
						new Pair<String,String>("username", obj.getUsername()).toMap());
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (person == null) {
				LOGGER.debug("Failed to create user");
			} else {
				LOGGER.debug("User successfully created");
			}
		}

		return additionalAttribsForStudent(person);
	}

	/**
	 * @param obj
	 *            Model instance to save
	 * @see IntakeService
	 */
	@Override
	public Person save(final Person obj) throws ObjectNotFoundException {
		final Person person = dao.save(obj);
		return additionalAttribsForStudent(person);
	}

	/**
	 * Mark a Person as deleted.
	 * 
	 * Does not remove them from persistent storage, but marks their status flag
	 * to {@link ObjectStatus#INACTIVE}.
	 */
	@Override
	public void delete(final UUID id) throws ObjectNotFoundException {
		final Person current = get(id);

		if (null != current) {
			current.setObjectStatus(ObjectStatus.INACTIVE);
			save(current);
		}
	}

	protected void setDao(final PersonDao dao) {
		this.dao = dao;
	}

	protected void setRegistrationStatusByTermService(
			final RegistrationStatusByTermService registrationStatusByTermService) {
		this.registrationStatusByTermService = registrationStatusByTermService;
	}

	@Override
	public List<Person> peopleFromListOfIds(final List<UUID> personIds,
			final SortingAndPaging sAndP) {
		try {
			final List<Person> people = dao.getPeopleInList(personIds, sAndP);
			additionalAttribsForStudents(people);
			return people;
		} catch (final ValidationException exc) {
			return Lists.newArrayList();
		}
	}

	/**
	 * Used for Specific Report "Address Labels"
	 */
	@Override
	public List<Person> peopleFromCriteria(
			final AddressLabelSearchTO addressLabelSearchTO,
			final SortingAndPaging sAndP) throws ObjectNotFoundException {

		final List<Person> people = dao.getPeopleByCriteria(
				addressLabelSearchTO,
				sAndP);
		additionalAttribsForStudents(people);
		return people;
	}

	@Override
	public List<Person> peopleFromSpecialServiceGroups(
			final List<UUID> specialServiceGroupIDs,
			final SortingAndPaging sAndP) throws ObjectNotFoundException {

		final List<Person> people = dao.getPeopleBySpecialServices(
				specialServiceGroupIDs,
				sAndP);

		additionalAttribsForStudents(people);
		return people;
	}

	@Override
	public PagingWrapper<Person> getAllCoaches(final SortingAndPaging sAndP) {
		long methodStart = new Date().getTime();
		final Collection<Person> coaches = Lists.newArrayList();

		long pasLookupStart = new Date().getTime();
		final Collection<String> coachUsernames = personAttributesService
				.getCoaches();
		long pasLookupEnd = new Date().getTime();
		TIMING_LOGGER.info("Read {} coaches from PersonAttributesService in {} ms",
				coachUsernames.size(), pasLookupEnd - pasLookupStart);

		long mergeLoopStart = new Date().getTime();
		final AtomicLong timeInExternalReads = new AtomicLong();
		final AtomicLong timeInExternalWrites = new AtomicLong();
		for (final String coachUsername : coachUsernames) {

			long singlePersonStart = new Date().getTime();

			final AtomicReference<Person> coach = new AtomicReference<Person>();

			try {
				withTransaction.withNewTransactionAndUncheckedExceptions(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						long localPersonLookupStart = new Date().getTime();
						try {
							coach.set(personFromUsername(coachUsername));
						} catch (final ObjectNotFoundException e) {
							LOGGER.debug("Coach {} not found", coachUsername);
						}
						long localPersonLookupEnd = new Date().getTime();
						TIMING_LOGGER.info("Read local coach by username {} in {} ms",
								coachUsername, localPersonLookupEnd - localPersonLookupStart);

						// Does coach exist in local SSP.person table?

						if (coach.get() == null) {

							// Attempt to find coach in external data
							try {
								long externalPersonLookupStart = new Date().getTime();

								final ExternalPerson externalPerson = externalPersonService
										.getByUsername(coachUsername);

								long externalPersonLookupEnd = new Date().getTime();
								long externalPersonLookupElapsed = externalPersonLookupEnd -
										externalPersonLookupStart;
								timeInExternalReads.set(timeInExternalReads.get()
										+ externalPersonLookupElapsed);
								TIMING_LOGGER.info("Read external coach by username {} in {} ms",
										coachUsername, externalPersonLookupElapsed);

								long externalPersonSyncStart = new Date().getTime();

								coach.set(new Person()); // NOPMD
								externalPersonService.updatePersonFromExternalPerson(
										coach.get(), externalPerson);

								long externalPersonSyncEnd = new Date().getTime();
								long externalPersonSyncElapsed = externalPersonSyncEnd -
										externalPersonSyncStart;
								timeInExternalWrites.set(timeInExternalWrites.get()
										+ externalPersonSyncElapsed);
								TIMING_LOGGER.info("Synced external coach by username {} in {} ms",
										coachUsername, externalPersonSyncElapsed);

							} catch (final ObjectNotFoundException e) {
								LOGGER.debug("Coach {} not found in external data",
										coachUsername);
							}
						}
						return coach.get();
					}
				});
			} catch ( ConstraintViolationException e ) {
				if ( "uq_person_school_id".equals(e.getConstraintName()) ) {
					LOGGER.warn("Skipping coach with non-unique schoolId '{}' (username '{}')",
							new Object[] { coach.get().getSchoolId(), coachUsername, e });
					coach.set(null);
				} else if ( "unique_person_username".equals(e.getConstraintName()) ) {
					LOGGER.warn("Skipping coach with non-unique username '{}' (schoolId '{}')",
							new Object[] { coachUsername, coach.get().getSchoolId(), e });
					coach.set(null);
				} else {
					throw e;
				}
			}


			if (coach.get() != null) {
				coaches.add(coach.get());
			}
			long singlePersonEnd = new Date().getTime();
			TIMING_LOGGER.info("SSP coach merge for username {} completed in {} ms",
					coachUsername, singlePersonEnd - singlePersonStart);
		}
		Long mergeLoopEnd = new Date().getTime();
		TIMING_LOGGER.info("All SSP merges for {} coaches completed in {} ms. Reading: {} ms. Writing: {} ms",
				new Object[] { coachUsernames.size(), mergeLoopEnd - mergeLoopStart,
						timeInExternalReads.get(), timeInExternalWrites.get() });

		PagingWrapper pw = new PagingWrapper<Person>(coaches);
		long methodEnd = new Date().getTime();
		TIMING_LOGGER.info("Read and merged PersonAttributesService {} coaches in {} ms",
				coaches.size(), methodEnd - methodStart);
		return pw;
	}

	private Iterable<Person> additionalAttribsForStudents(
			final Iterable<Person> people) {
		if (people == null) {
			return null;
		}

		for (final Person person : people) {
			additionalAttribsForStudent(person);
		}

		return people;
	}

	private Person additionalAttribsForStudent(final Person person) {
		registrationStatusByTermService
				.applyRegistrationStatusForCurrentTerm(person);
		return person;
	}

	@Override
	public void setPersonAttributesService(
			final PersonAttributesService personAttributesService) {
		this.personAttributesService = personAttributesService;
	}
}