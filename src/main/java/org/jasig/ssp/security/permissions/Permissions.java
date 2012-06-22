package org.jasig.ssp.security.permissions;

/**
 * These are the individual permissions in SSP. 
 * 
 * @author awills
 */
public enum Permissions {
	
	/*
	 * Service permissions
	 */

	PERSON_APPOINTMENT_READ,
	PERSON_APPOINTMENT_WRITE,
	PERSON_APPOINTMENT_DELETE,
	PERSON_CASELOAD_READ,
	PERSON_CHALLENGE_READ,
	PERSON_CHALLENGE_WRITE,
	PERSON_CHALLENGE_DELETE,
	PERSON_DOCUMENT_READ,
	PERSON_DOCUMENT_WRITE,
	PERSON_DOCUMENT_DELETE,
	PERSON_EARLY_ALERT_READ,
	PERSON_EARLY_ALERT_WRITE,
	PERSON_EARLY_ALERT_DELETE,
	PERSON_EARLY_ALERT_RESPONSE_READ,
	PERSON_EARLY_ALERT_RESPONSE_WRITE,
	PERSON_EARLY_ALERT_RESPONSE_DELETE,
	PERSON_GOAL_READ,
	PERSON_GOAL_WRITE,
	PERSON_GOAL_DELETE,
	PERSON_JOURNAL_ENTRY_READ,
	PERSON_JOURNAL_ENTRY_WRITE,
	PERSON_JOURNAL_ENTRY_DELETE,
	PERSON_READ,
	PERSON_WRITE,
	PERSON_DELETE,
	PERSON_SEARCH_READ,
	PERSON_PROGRAM_STATUS_READ,
	PERSON_PROGRAM_STATUS_WRITE,
	PERSON_PROGRAM_STATUS_DELETE,
	PERSON_TASK_READ,
	PERSON_TASK_WRITE,
	PERSON_TASK_DELETE,

	REFERENCE_READ,
	REFERENCE_WRITE,

	STUDENT_INTAKE_READ,
	STUDENT_INTAKE_WRITE,

	/*
	 * Data permissions
	 */

	DATA_MY_RECORD_ONLY,
	DATA_EVERYONE,
	DATA_ACADEMIC_RESOURCE_CENTER,
	DATA_COUNSELING_SERVICES,
	DATA_DISABILITY,
	DATA_DISPLACED_WORKERS,
	DATA_EARLY_ALERT,
	DATA_ENGLISH_SECOND_LANGAGE,
	DATA_FAST_FORWARD,
	DATA_INDIVIDUALIZED_LEARNING_PLAN,
	DATA_MANAGER,
	DATA_STAFF
	
	/*
	 * Report permissions (TBD)
	 */

//	REPORT_REPORTNAME1_VIEW
//	REPORT_REPORTNAME2_VIEW
//	REPORT_REPORTNAME3_VIEW

}