/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
Ext.define('Ssp.controller.tool.profile.ProfilePersonViewController', {
    extend: 'Deft.mvc.ViewController',
    mixins: ['Deft.mixin.Injectable'],
    inject: {
        apiProperties: 'apiProperties',
        appEventsController: 'appEventsController',
        person: 'currentPerson',
        personLite: 'personLite',
        personService: 'personService',
        transcriptService: 'transcriptService',
		personProgramStatusService: 'personProgramStatusService',
        profileReferralSourcesStore: 'profileReferralSourcesStore',
        profileServiceReasonsStore: 'profileServiceReasonsStore',
		profileSpecialServiceGroupsStore: 'profileSpecialServiceGroupsStore',
		programStatusChangeReasonsStore: 'programStatusChangeReasonsStore',
        sspConfig: 'sspConfig',
		formUtils: 'formRendererUtils'
    },
    
    control: {
        nameField: '#studentName',
        photoUrlField: '#studentPhoto',
		primaryEmailAddressField: '#primaryEmailAddress',
		
        
        studentIdField: '#studentId',
        birthDateField: '#birthDate',
        studentTypeField: '#studentType',
        programStatusField: '#programStatus',
        f1StatusField: '#f1Status',

        gpaField: '#cumGPA',
        
        academicStandingField: '#academicStanding',
        currentRestrictionsField: '#currentRestrictions',
        creditCompletionRateField: '#creditCompletionRate',
        currentYearFinancialAidAwardField: '#currentYearFinancialAidAward',
        academicProgramsField: '#academicPrograms',
        sapStatusField: '#sapStatus',
        registeredTermsField: '#registeredTerms',
        paymentStatusField: '#paymentStatus',
     

        earlyAlertField: '#earlyAlert',
        actionPlanField: '#actionPlan',
        
        studentIntakeAssignedField: '#studentIntakeAssigned',
        studentIntakeCompletedField: '#studentIntakeCompleted',
        
		'serviceReasonEdit': {
            click: 'onServiceReasonEditButtonClick'
        },
        
        'serviceGroupEdit': {
            click: 'onServiceGroupEditButtonClick'
        }
    
    },
    init: function(){
        var me = this;
        var id = me.personLite.get('id');
        me.resetForm();
       
        if (id != "") {
            // display loader
            me.getView().setLoading(true);

            var serviceResponses = {
                failures: {},
                successes: {},
                responseCnt: 0,
                expectedResponseCnt: 3
            }

            me.personService.get(id, {
                success: me.newServiceSuccessHandler('person', me.getPersonSuccess, serviceResponses),
                failure: me.newServiceFailureHandler('person', me.getPersonFailure, serviceResponses),
                scope: me
            });
            me.transcriptService.getSummary(id, {
                success: me.newServiceSuccessHandler('transcript', me.getTranscriptSuccess, serviceResponses),
                failure: me.newServiceFailureHandler('transcript', me.getTranscriptFailure, serviceResponses),
                scope: me
            });
			me.personProgramStatusService.getCurrentProgramStatus(id, {
                success: me.newServiceSuccessHandler('programstatus', me.getCurrentProgramStatusSuccess, serviceResponses),
                failure: me.newServiceFailureHandler('programstatus', me.getCurrentProgramStatusFailure, serviceResponses),
                scope: me
            });
			
			
        }
		
		if (!me.programStatusChangeReasonsStore.getTotalCount()) {
			me.programStatusChangeReasonsStore.load({
				params: {
					start: 0,
					limit: 50
				}
			});
			
		}
        return me.callParent(arguments);
    },

    resetForm: function() {
        var me = this;
        me.getView().getForm().reset();

        // Set defined configured label for the studentId field
        var studentIdAlias = me.sspConfig.get('studentIdAlias');
        me.getStudentIdField().setFieldLabel(studentIdAlias);

    },

    newServiceSuccessHandler: function(name, callback, serviceResponses) {
        var me = this;
        return me.newServiceHandler(name, callback, serviceResponses, function(name, serviceResponses, response) {
            serviceResponses.successes[name] = response;
        });
    },

    newServiceFailureHandler: function(name, callback, serviceResponses) {
        var me = this;
        return me.newServiceHandler(name, callback, serviceResponses, function(name, serviceResponses, response) {
            serviceResponses.failures[name] = response;
        });
    },

    newServiceHandler: function(name, callback, serviceResponses, serviceResponsesCallback) {
        return function(r, scope) {
            var me = scope;
            serviceResponses.responseCnt++;
            if ( serviceResponsesCallback ) {
                serviceResponsesCallback.apply(me, [name, serviceResponses, r]);
            }
            if ( callback ) {
                callback.apply(me, [ serviceResponses ]);
            }
            me.afterServiceHandler(serviceResponses);
        };
    },

    getPersonSuccess: function(serviceResponses) {
        var me = this;
        var personResponse = serviceResponses.successes.person;
        me.person.populateFromGenericObject(personResponse);

        // load and render person data
        me.profileReferralSourcesStore.removeAll();
        me.profileServiceReasonsStore.removeAll();
		
        var nameField = me.getNameField();	
		var primaryEmailAddressField = me.getPrimaryEmailAddressField();
        var photoUrlField = me.getPhotoUrlField();
        var birthDateField = me.getBirthDateField();
        var studentTypeField = me.getStudentTypeField();
        var programStatusField = me.getProgramStatusField();
        var earlyAlertField = me.getEarlyAlertField();
        var actionPlanField = me.getActionPlanField();
		var studentIdField = me.getStudentIdField();
		
        var studentIntakeAssignedField = me.getStudentIntakeAssignedField();
		var studentIntakeCompletedField = me.getStudentIntakeCompletedField();

        var fullName = me.person.getFullName();
		var firstLastName = me.person.getFirstLastName();
        var coachName = me.person.getCoachFullName();
		

        // load referral sources
        if (personResponse.referralSources != null) {
            me.profileReferralSourcesStore.loadData(me.person.get('referralSources'));
        }

        // load service reasonssd
        if (personResponse.serviceReasons != null) {
            me.profileServiceReasonsStore.loadData(me.person.get('serviceReasons'));
        }

		// load service reasonssd
        if (personResponse.serviceReasons != null) {
            me.profileSpecialServiceGroupsStore.loadData(me.person.get('specialServiceGroups'));
        }

        // load general student record
        me.getView().loadRecord(me.person);

        // load additional values
		nameField.setFieldLabel('');
        nameField.setValue('<span style="color:#15428B">Name:  </span>' + firstLastName);
		studentIdField.setFieldLabel('');
        studentIdField.setValue('<span style="color:#15428B">' + me.sspConfig.get('studentIdAlias') + ':  </span>' + me.person.get('schoolId'));
		primaryEmailAddressField.setFieldLabel('');
        primaryEmailAddressField.setValue('<span style="color:#15428B">Email:  </span>' + me.handleNull(me.person.get('primaryEmailAddress')));
		birthDateField.setFieldLabel('');
        birthDateField.setValue('<span style="color:#15428B">DOB:  </span>' + me.handleNull(me.person.getFormattedBirthDate()));
		studentTypeField.setFieldLabel('');
        studentTypeField.setValue('<span style="color:#15428B">Student Type:  </span>' + me.handleNull(me.person.getStudentTypeName()));
        photoUrlField.setSrc(me.person.getPhotoUrl());
		programStatusField.setFieldLabel('');
        programStatusField.setValue('<span style="color:#15428B">SSP Status:  </span>' + me.handleNull(me.person.getProgramStatusName()));
		earlyAlertField.setFieldLabel('');
        earlyAlertField.setValue('<span style="color:#15428B">Early Alerts:  </span>' + me.handleNull(me.person.getEarlyAlertRatio()));
		actionPlanField.setFieldLabel('');
        actionPlanField.setValue('<span style="color:#15428B">Action Plan:  </span>' + me.handleNull(me.person.getActionPlanSummary()));
        me.getRegisteredTermsField().setValue(me.handleNull(me.person.get('registeredTerms')));
        me.getPaymentStatusField().setValue(me.handleNull(me.person.get('paymentStatus')));
		
        studentIntakeAssignedField.setValue(me.handleNull(me.person.get('studentIntakeRequestDate')));
        studentIntakeCompletedField.setValue(me.handleNull(me.person.get('studentIntakeCompleteDate')));


        var studentRecordComp = Ext.ComponentQuery.query('.studentrecord')[0];
        var studentCoachButton = Ext.ComponentQuery.query('#emailCoachButton')[0];
        studentRecordComp.setTitle('Student: ' + fullName + '          ' + '  -   ID#: ' + me.person.get('schoolId'));
        studentCoachButton.setText('<u>Coach: ' + coachName + '</u>');
		
        me.appEventsController.assignEvent({
            eventName: 'emailCoach',
            callBackFunc: me.onEmailCoach,
            scope: me
        });
    },

    getPersonFailure: function() {
        // nothing to do
    },
    
	handleNull: function(value, defaultValue){
		if(defaultValue == null || defaultValue == undefined)
			defaultValue = "";
		if(value == null || value == undefined || value == 'null')
			return defaultValue;
		return value;
	},

    getTranscriptSuccess: function(serviceResponses) {
        var me = this;
        var transcriptResponse = serviceResponses.successes.transcript;

        var transcript = new Ssp.model.Transcript(transcriptResponse);
        var gpa = transcript.get('gpa');
        if ( gpa ) {
			var gpaFormatted = Ext.util.Format.number(gpa.gradePointAverage, '0.00');
			if(gpa.gpaTrendIndicator && gpa.gpaTrendIndicator.length > 0)
				gpaFormatted += "  " + gpa.gpaTrendIndicator;
			me.getGpaField().setFieldLabel('');
            me.getGpaField().setValue('<span style="color:#15428B">GPA:  </span>' + gpaFormatted);
			me.getAcademicStandingField().setFieldLabel('');
            me.getAcademicStandingField().setValue('<span style="color:#15428B">Standing:  </span>' + me.handleNull(gpa.academicStanding));
			me.getCreditCompletionRateField().setFieldLabel('');
            me.getCreditCompletionRateField().setValue('<span style="color:#15428B">Comp Rate:  </span>' + me.handleNull(gpa.creditCompletionRate) + '%');
            me.getCurrentRestrictionsField().setFieldLabel('');
			me.getCurrentRestrictionsField().setValue('<span style="color:#15428B">Restrictions:  </span>' + me.handleNull(gpa.currentRestrictions))

        }
        var programs = transcript.get('programs');
        if ( programs ) {
            var programNames = [];
            Ext.Array.each(programs, function(program) {
                programNames.push(program.programName);
            });
			me.getAcademicProgramsField().setFieldLabel('');
            me.getAcademicProgramsField().setValue('<span style="color:#15428B">Academic Program:  </span>' + programNames.join(', '));
        }
        

        var financialAid = transcript.get('financialAid');
        if ( financialAid ) {
            me.getCurrentYearFinancialAidAwardField().setFieldLabel('');
        	me.getCurrentYearFinancialAidAwardField().setValue('<span style="color:#15428B">FA Award:  </span>' + me.handleNull(financialAid.currentYearFinancialAidAward));
        	me.getSapStatusField().setFieldLabel('');
			me.getSapStatusField().setValue('<span style="color:#15428B">SAP:  </span>' + me.handleNull(financialAid.sapStatus));
        }
    },

    getTranscriptFailure: function() {
        // nothing to do
    },
	
	getCurrentProgramStatusSuccess: function(serviceResponses) {
        var me = this;
		var programStatusReason;
		var studentStatus;
		
		var programStatusResponse = serviceResponses.successes.programstatus;
		studentStatus = programStatusResponse['programStatusChangeReasonId'];
		
		
		var programStatusReasonField = Ext.ComponentQuery.query('#programStatusReason')[0];
		if (studentStatus) {
                    programStatusReason = me.programStatusChangeReasonsStore.findRecord('id', studentStatus);
					
                    if (programStatusReason) {
							programStatusReasonField.show();
							programStatusReasonField.setFieldLabel('');
							programStatusReasonField.setValue('<span style="color:#15428B">Reason:  </span>' + programStatusReason.get('name'));
					}
		}
		else
		{
			programStatusReasonField.hide();
		}
		
	},
	
	getCurrentProgramStatusFailure: function() {
		
        // nothing to do
    },

    afterServiceHandler: function(serviceResponses) {
        var me = this;
        if ( serviceResponses.responseCnt >= serviceResponses.expectedResponseCnt ) {
            me.getView().setLoading(false);
        }
    },

	destroy: function() {
        var me=this;
        return me.callParent( arguments );
    },

    onEmailCoach: function(){
        var me = this;
        if (me.person.getCoachPrimaryEmailAddress()) {
            window.location = 'mailto:' + me.person.getCoachPrimaryEmailAddress();
        }
    },
	
	onServiceReasonEditButtonClick: function(button){
        var me=this;
        
        var comp = this.formUtils.loadDisplay('mainview', 'caseloadassignment', true, {flex:1}); 
        
    },
    
    onServiceGroupEditButtonClick: function(button){
        var me=this;
        
        var comp = this.formUtils.loadDisplay('mainview', 'caseloadassignment', true, {flex:1}); 
        
    }
	
});
