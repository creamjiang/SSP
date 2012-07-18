package org.jasig.ssp.dao; // NOPMD by jon.adams on 5/16/12 9:59 PM

import static org.jasig.ssp.util.assertions.SspAssert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.jasig.ssp.model.EarlyAlert;
import org.jasig.ssp.model.EarlyAlertResponse;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.reference.EarlyAlertOutreach;
import org.jasig.ssp.model.reference.EarlyAlertSuggestion;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonService;
import org.jasig.ssp.service.impl.SecurityServiceInTestEnvironment;
import org.jasig.ssp.service.reference.CampusService;
import org.jasig.ssp.service.reference.EarlyAlertOutcomeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("dao-testConfig.xml")
@TransactionConfiguration(defaultRollback = false)
@Transactional
public class EarlyAlertResponseDaoTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EarlyAlertResponseDaoTest.class);

	private static final UUID PERSON_ID = UUID
			.fromString("1010e4a0-1001-0110-1011-4ffc02fe81ff");

	private static final UUID EARLY_ALERT_SUGGESTION_ID = UUID
			.fromString("b2d11141-5056-a51a-80c1-c1250ba820f8");

	private static final String EARLY_ALERT_SUGGESTION_NAME = "See Instructor";

	private static final String EARLY_ALERT_COURSE_NAME = "Complicated Science 101";

	private static final UUID EARLY_ALERT_SUGGESTION_DELETED_ID = UUID
			.fromString("881DF3DD-1AA6-4CB8-8817-E95DAF49227A");

	private static final String EARLY_ALERT_RESPONSE_OUTCOME_OTHER = "Other";

	private static final UUID EARLY_ALERT_OUTREACH_ID = UUID
			.fromString("e7908476-e67d-4fb2-890b-2d4e6c9b0e42");

	private static final String EARLY_ALERT_OUTREACH_NAME = "Text";

	public final static UUID EARLY_ALERT_OUTCOME_STUDENTRESPONDED_ID = UUID
			.fromString("12a58804-45dc-40f2-b2f5-d7e4403acee1");

	@Autowired
	private transient EarlyAlertResponseDao dao;

	@Autowired
	private transient EarlyAlertDao earlyAlertDao;
	@Autowired
	private transient SecurityServiceInTestEnvironment securityService;

	@Autowired
	private transient CampusService campusService;

	@Autowired
	private transient EarlyAlertOutcomeService earlyAlertOutcomeService;

	@Autowired
	private transient PersonService personService;

	@Before
	public void setUp() {
		securityService.setCurrent(new Person(Person.SYSTEM_ADMINISTRATOR_ID));
	}

	/**
	 * Test the {@link EarlyAlertDao#save(EarlyAlert)} and
	 * {@link EarlyAlertDao#delete(EarlyAlert)} actions.
	 * 
	 * @throws ObjectNotFoundException
	 *             If any referenced objects could not be found. Since this is a
	 *             test, it should not be thrown.
	 */
	@Test
	public void testSaveNew() throws ObjectNotFoundException {
		final EarlyAlertResponse ear = createTestEarlyAlertResponse();
		earlyAlertDao.save(ear.getEarlyAlert());
		final EarlyAlertResponse saved = dao.save(ear);

		assertNotNull("Id should have been automatically generated.",
				saved.getId());
		final UUID savedId = saved.getId();

		LOGGER.debug(saved.toString());

		try {
			final EarlyAlertResponse obj = dao.get(savedId);

			assertNotNull("Saved message could not reloaded.", obj);
			assertEquals("Saved and reloaded IDs do not match.", savedId,
					obj.getId());

			final Collection<EarlyAlertResponse> all = dao.getAll(
					ObjectStatus.ACTIVE)
					.getRows();
			assertNotNull("GetAll list should not have been null.", all);
			assertFalse("GetAll list should not have been empty.",
					all.isEmpty());
			assertList(all);

			dao.delete(obj);
		} catch (final ObjectNotFoundException e) {
			fail("Saved message could not be found to reload.");
		}
	}

	@Test(expected = ObjectNotFoundException.class)
	public void testNull() throws ObjectNotFoundException {
		final UUID id = UUID.randomUUID();
		final EarlyAlertResponse obj = dao.get(id);

		assertNull("Random ID should not have loaded any object.", obj);
	}

	private void assertList(final Collection<EarlyAlertResponse> objects) {
		for (final EarlyAlertResponse object : objects) {
			assertNotNull("List should not have contained any null objects.",
					object.getId());
		}
	}

	@Test
	public void testHashCode() throws ObjectNotFoundException {
		final EarlyAlert earlyAlert = new EarlyAlert();
		earlyAlert.setId(UUID.randomUUID());
		final EarlyAlertResponse empty = new EarlyAlertResponse();
		empty.setEarlyAlert(earlyAlert);
		assertNotEquals("HashCodes should not have matched.",
				createTestEarlyAlertResponse().hashCode(), empty.hashCode());
		assertEquals("HashCodes should have matched.", empty.hashCode(),
				empty.hashCode());
		assertEquals("HashCodes should have matched.",
				new EarlyAlertResponse().hashCode(),
				new EarlyAlertResponse().hashCode());
	}

	/**
	 * Create a new sample message to use for testing.
	 * 
	 * @return a new sample message to use for testing
	 * @throws ObjectNotFoundException
	 */
	private EarlyAlertResponse createTestEarlyAlertResponse()
			throws ObjectNotFoundException {
		final EarlyAlert earlyAlert = new EarlyAlert();
		earlyAlert.setClosedDate(null);
		earlyAlert.setClosedDate(new Date());
		earlyAlert.setPerson(personService.get(PERSON_ID));
		earlyAlert.setObjectStatus(ObjectStatus.ACTIVE);
		earlyAlert.setClosedById(PERSON_ID);
		earlyAlert.setCourseName(EARLY_ALERT_COURSE_NAME);
		earlyAlert.setCampus(campusService.get(UUID
				.fromString("901E104B-4DC7-43F5-A38E-581015E204E1")));

		final Set<EarlyAlertSuggestion> earlyAlertSuggestionIds = Sets
				.newHashSet();
		earlyAlertSuggestionIds.add(new EarlyAlertSuggestion(
				EARLY_ALERT_SUGGESTION_ID, EARLY_ALERT_SUGGESTION_NAME,
				"description", (short) 0)); // NOPMD by jon.adams on 5/21/12
		final EarlyAlertSuggestion deletedSuggestion = new EarlyAlertSuggestion(
				EARLY_ALERT_SUGGESTION_DELETED_ID,
				"EARLY_ALERT_SUGGESTION_DELETED_NAME", "description",
				(short) 0); // NOPMD
		deletedSuggestion.setObjectStatus(ObjectStatus.INACTIVE);
		earlyAlertSuggestionIds.add(deletedSuggestion);
		earlyAlert.setEarlyAlertSuggestionIds(earlyAlertSuggestionIds);

		final EarlyAlertResponse obj = new EarlyAlertResponse();
		obj.setEarlyAlert(earlyAlert);
		obj.setObjectStatus(ObjectStatus.ACTIVE);
		obj.setEarlyAlertOutcome(earlyAlertOutcomeService
				.get(EARLY_ALERT_OUTCOME_STUDENTRESPONDED_ID));
		obj.setEarlyAlertOutcomeOtherDescription(EARLY_ALERT_RESPONSE_OUTCOME_OTHER);

		final Set<EarlyAlertOutreach> earlyAlertOutreachIds = Sets
				.newHashSet();
		earlyAlertOutreachIds.add(new EarlyAlertOutreach(
				EARLY_ALERT_OUTREACH_ID, EARLY_ALERT_OUTREACH_NAME,
				"description", (short) 0)); // NOPMD by jon.adams on 5/21/12

		obj.setEarlyAlertOutreachIds(earlyAlertOutreachIds);

		return obj;
	}
}