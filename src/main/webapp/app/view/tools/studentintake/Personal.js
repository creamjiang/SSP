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
Ext.define('Ssp.view.tools.studentintake.Personal', {
	extend: 'Ext.form.Panel',
	alias: 'widget.studentintakepersonal',
	id: 'StudentIntakePersonal',
    mixins: [ 'Deft.mixin.Injectable',
              'Deft.mixin.Controllable'],
    controller: 'Ssp.controller.tool.studentintake.PersonalViewController',
    inject: {
    	columnRendererUtils: 'columnRendererUtils',
        statesStore: 'statesStore',
        textStore:'textStore'
    },
	width: '100%',
    height: '100%',
	minHeight: 1000,
	minWidth: 600,
	style: 'padding: 0px 5px 5px 10px',
	initComponent: function() {
		var me=this;
		Ext.apply(me, 
				{
					autoScroll: true,
    		        border: 0,	
				    bodyPadding: 5,				    
					layout: 'anchor',
				    defaults: {
				        anchor: '100%'
				    },
				    fieldDefaults: {
				        msgTarget: 'side',
				        labelAlign: 'right',
				        labelWidth: 200
				    },
				    items: [{
				            xtype: 'fieldset',
				            border: 0,
				            title: '',
				            defaultType: 'textfield',
				            defaults: {
				                anchor: '95%'
				            },
				       items: [{
				    	xtype: 'displayfield',
				        fieldLabel: 'Intake Completion Date',
				        name: 'formattedStudentIntakeCompleteDate',
				        renderer: Ext.util.Format.dateRenderer('m/d/Y'),
				        listeners: {
				            render: function(field){
				                Ext.create('Ext.tip.ToolTip',{
				                    target: field.getEl(),
				                    html: 'This is the date on which intake data for this student was most recently received. It is shown in institution-local time. E.g. for a May 9, 11pm submission on the US west coast to an east coast school, this would display the "next" day, i.e. May 10.'
				                });
				            }
				        }
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.first-name'),
				        name: 'firstName',
				        itemId: 'firstName',
				        maxLength: 50,
				        allowBlank:false
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.middle-name'),
				        name: 'middleName',
				        itemId: 'middleName',
				        maxLength: 50,
				        allowBlank:true
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.last-name'),
				        name: 'lastName',
				        itemId: 'lastName',
				        maxLength: 50,
				        allowBlank:false
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.student-id'),
				        name: 'schoolId',
				        minLength: 0,
				        maxLength: 7,
				        itemId: 'studentId',
				        allowBlank:false
				    },{
				    	xtype: 'datefield',
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.birthday'),
				    	itemId: 'birthDate',
				    	altFormats: 'm/d/Y|m-d-Y',
				    	invalidText: '{0} is not a valid date - it must be in the format: 06/02/2012 or 06-02-2012',
				        name: 'birthDate',
				        allowBlank:false
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.home-phone'),
				        name: 'homePhone',
				        emptyText: 'xxx-xxx-xxxx',
				        maxLength: 25,
				        allowBlank:true,
				        itemId: 'homePhone' 
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.work-phone'),
				        name: 'workPhone',
				        emptyText: 'xxx-xxx-xxxx',
				        maxLength: 25,
				        allowBlank:true,
				        itemId: 'workPhone'
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.cell-phone'),
				        name: 'cellPhone',
				        emptyText: 'xxx-xxx-xxxx',
				        maxLength: 25,
				        allowBlank:true,
				        itemId: 'cellPhone'
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.school-email'),
				        name: 'primaryEmailAddress',
				        vtype:'email',
				        maxLength: 100,
				        allowBlank:true,
				        itemId: 'primaryEmailAddress'
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.alternate-email'),
				        name: 'secondaryEmailAddress',
				        vtype:'email',
				        maxLength: 100,
				        allowBlank:true,
				        itemId: 'secondaryEmailAddress'
				    },{
				    	xtype: 'displayfield',
				    	fieldLabel: 'CURRENT ADDRESS'
				    },{
				    	xtype: 'displayfield',
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.non-local'),
				    	name: 'nonLocalAddress',
				    	renderer: me.columnRendererUtils.renderFriendlyBoolean
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.address-1'),
				        name: 'addressLine1',
				        maxLength: 50,
				        allowBlank:true,
				        itemId: 'addressLine1'
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.address-2'),
				        name: 'addressLine2',
				        maxLength: 50,
				        allowBlank:true,
				        itemId: 'addressLine2'
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.city'),
				        name: 'city',
				        maxLength: 50,
				        allowBlank:true,
				        itemId: 'city'
				    },{
				        xtype: 'combobox',
				        name: 'state',
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.state'),
				        emptyText: 'Select a State',
				        store: me.statesStore,
				        valueField: 'code',
				        displayField: 'title',
				        mode: 'local',
				        typeAhead: true,
				        queryMode: 'local',
				        allowBlank: true,
				        forceSelection: true,
				        itemId: 'state',
						listeners: {
								'select': function() {
								me.statesStore.clearFilter();
							}
						}
					},{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.zip'),
				        name: 'zipCode',
				        maxLength: 10,
				        allowBlank:true,
				        itemId: 'zipCode'
				    },{
				    	xtype: 'displayfield',
				    	fieldLabel: 'ALTERNATE ADDRESS'
				    },{
				    	xtype:'checkbox',
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.alt-in-use'),
				    	name: 'alternateAddressInUse'
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.address-1'),
				        name: 'alternateAddressLine1',
				        maxLength: 50,
				        allowBlank:true,
				        itemId: 'alternateAddressLine1'
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.address-2'),
						name: 'alternateAddressLine2',
						maxLength: 50,
						allowBlank: true,
						itemId: 'alternateAddressLine2'
					},{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.city'),
				        name: 'alternateAddressCity',
				        maxLength: 50,
				        allowBlank:true,
				        itemId: 'alternateAddressCity'
				    },{
				        xtype: 'combobox',
				        name: 'alternateAddressState',
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.state'),
				        emptyText: 'Select a State',
				        store: me.statesStore,
				        valueField: 'code',
				        displayField: 'title',
				        mode: 'local',
				        typeAhead: true,
				        queryMode: 'local',
				        allowBlank: true,
				        forceSelection: true,
				        itemId: 'alternateAddressState',
						listeners: {
								'select': function() {
								me.statesStore.clearFilter();
							}
						}
					},{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.zip'),
				        name: 'alternateAddressZipCode',
				        maxLength: 10,
				        allowBlank:true,
				        itemId: 'alternateAddressZipCode'
				    },{
				        fieldLabel: me.textStore.getValueByCode('intake.tab1.label.country'),
				        name: 'alternateAddressCountry',
				        allowBlank:true,
				        itemId: 'alternateAddressCountry',
						maxLength: 50
				    }]
				    }]
				});
		
		return me.callParent(arguments);
	}
});
