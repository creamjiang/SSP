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
Ext.define('Ssp.controller.tool.actionplan.DisplayStrengthsViewController', {
    extend: 'Deft.mvc.ViewController',
    mixins: [ 'Deft.mixin.Injectable' ],
    inject: {
    	authenticatedPerson: 'authenticatedPerson',
    	formUtils: 'formRendererUtils',
    	model: 'currentPerson',
        personLite: 'personLite',
    	service: 'personService'
    },
    
    control: {  	
    	saveButton: {
    		selector: '#saveButton',
    		listeners: {
    			click: 'onSaveClick'
    		}
    	},
    	
    	strengthsField: {
    		selector: '#strengths'
    	},
    	
    	saveSuccessMessage: '#saveSuccessMessage'
	},
	
	init: function() {
		var me=this;
        me.getSaveButton().setDisabled( !me.authenticatedPerson.hasAccess('ACTION_PLAN_STRENGTHS_FIELD') );
		me.getStrengthsField().setDisabled( !me.authenticatedPerson.hasAccess('ACTION_PLAN_STRENGTHS_FIELD') );
       

        // display loader
        me.getView().setLoading(true);
        if ( !(me.model) || !(me.model.get('id')) || !(me.personLite.get('id') === me.model.get('id')) ) {

            me.service.get(me.personLite.get('id'), {
                success: me.loadPersonSuccess,
                failure: me.loadPersonFailure,
                scope: me
            });
        } else {
            me.bindModelToView();
        }


		return me.callParent(arguments);
    },

    loadPersonSuccess: function(response, scope) {
        var me = scope;
        me.model.populateFromGenericObject(response);
        me.bindModelToView();
    },

    bindModelToView: function() {
        var me = this;
        me.getView().getForm().loadRecord( me.model );
        me.getView().setLoading(false);
    },

    loadPersonFailure: function(response, scope) {
        var me = scope;
        me.getView().setLoading(false);
    },
    
    onSaveClick: function(button) {
		var me=this;
		var form = me.getView().getForm();
		var jsonData;
		if (form.isValid())
		{
			form.updateRecord();
			jsonData = me.model.data;
			jsonData = me.model.setPropsNullForSave( me.model.data );
			me.getView().setLoading(true);
			me.service.save( jsonData , {
				success: me.savePersonSuccess,
				failure: me.savePersonFailure,
				scope: me
			});	
		}else{
			Ext.Msg.alert('Unable to save strengths. Please correct the errors in the form.');
		}	
    },

    savePersonSuccess: function( r, scope){
    	var me=scope;
    	me.getView().setLoading( false );
		me.model.commit();
		me.formUtils.displaySaveSuccessMessage( me.getSaveSuccessMessage() );
    },

    savePersonFailure: function( response, scope ){
    	var me=scope;
    	me.getView().setLoading( false );
    }
});