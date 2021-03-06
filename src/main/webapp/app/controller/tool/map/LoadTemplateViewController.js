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
Ext.define('Ssp.controller.tool.map.LoadTemplateViewController', {
    extend: 'Deft.mvc.ViewController',
    mixins: [ 'Deft.mixin.Injectable' ],
    inject:{
		appEventsController: 'appEventsController',
		formUtils: 'formRendererUtils',
    	currentMapPlan: 'currentMapPlan',
    	store: 'planTemplatesSummaryStore',
        personLite: 'personLite',
    	apiProperties: 'apiProperties',
    	mapPlanService:'mapPlanService',
		programsStore: 'programsFacetedStore',
        departmentsStore: 'departmentsStore',
        divisionsStore: 'divisionsStore'
    },
    
	control: {
    	'openButton': {
			click: 'onOpenClick'
		},	
		'cancelButton': {
			click: 'onCloseClick'
		},
	   'program': {
    	   selector: '#program',
    	   listeners: {
            select: 'onProgramSelect'
           }
        },
          'department':{
           selector: '#department',
           hidden: false,
           listeners: {
            select: 'onDepartmentSelect'
           }
        },
         'division':{
           selector: '#division',
           listeners: {
            select: 'onDivisionSelect'
           }
        },

        'programCancel':{
            selector: '#programCancel',
            hidden: true,
              listeners: {
               click: 'onProgramCancelClick'
              }
           },
           
        'departmentCancel':{
            selector: '#departmentCancel',
            hidden: false,
            listeners: {
             click: 'onDepartmentCancelClick'
            }
         },
        
        
        'divisionCancel':{
           selector: '#divisionCancel',
           listeners: {
            click: 'onDivisionCancelClick'
           }
        },
		'objectStatusFilter':{
            selector: '#objectStatusFilter',
            hidden: false,
            listeners: {
             select: 'onObjectStatusFilterSelect'
            }
         },

		'typeFilter':{
            selector: '#typeFilter',
            hidden: false,
            listeners: {
             select: 'onTypeFilterSelect'
            }
         },
         view: {
            show: 'onShow'
         }
	},

	init: function() {
		var me=this;
	    me.resetForm();
	    me.store.addListener("load", me.onStoreLoaded, me);
	    //wait for onShow() to actually load the store
		return me.callParent(arguments);
    },

    loadTemplates: function() {
        var me = this;
        me.getView().setLoading(true);
        me.store.load(); // callback registered in init()
    },
    
    onStoreLoaded: function(){
    	var me = this;
    	me.getView().setLoading(false);
    },

    onShow: function() {
        // do this on show rather than init b/c this component isn't destroyed
        // when dismissed, but we need to make sure you see all the latest
        // Template changes whenever we do display this component. The easiest
        // way to do that is to just hit the store/server again every time the
        // view fires its show event
        var me = this;
        me.loadTemplates();
    },

    resetForm: function() {
        var me = this;
         me.getView().query("form")[0].getForm().reset();
    },
    
    onOpenClick: function(button) {
    	var me = this;
		var grid, record;
    	me.getView().setLoading(true);
		var callbacks = new Object();
		callbacks.success = me.onLoadCompleteSuccess;
		callbacks.failure = me.onLoadCompleteFailure;
		callbacks.scope = me;
		
		record = me.getView().query('gridpanel')[0].getView().getSelectionModel().getSelection()[0];
        if (record) 
        {	
        	 me.mapPlanService.getTemplate(record.get('id'), callbacks);
        }else{
     	   Ext.Msg.alert('SSP Error', 'Please select an item to edit.'); 
        }    	
    },
	
	onLoadCompleteSuccess: function(serviceResponses){
        var me = this;
		if(!serviceResponses || !serviceResponses.responseText || serviceResponses.responseText.trim().length == 0) {

       	} else {
       		me.scope.currentMapPlan.loadFromServer(Ext.decode(serviceResponses.responseText));
       		me.scope.currentMapPlan.set('isTemplate',true);
			me.scope.appEventsController.getApplication().fireEvent('onLoadTemplatePlan');
			me.scope.appEventsController.getApplication().fireEvent("onCurrentMapPlanChangeUpdateMapView");
	    	me.scope.getView().setLoading(false);
			me.scope.getView().hide();
		}
	},

	initNewPlan: function( btnId ){
		var me=this;
		if (btnId=="yes")
		{
			me.appEventsController.getApplication().fireEvent('onCreateNewTemplatePlan');
		}
		else
		{
			me.appEventsController.getApplication().fireEvent('onShowMain');
		}
		me.getView().hide();
	},
	onLoadCompleteFailure: function(serviceResponses){
		var me = this;
		view.setLoading(false);
	},	    
	onCloseClick: function(){
		var me = this;
		me.getView().hide();
	},
	
	 onProgramSelect: function(){
	        var me=this;
	        me.handleSelect(me);
	        var params = {};
	        me.setParam(params, me.getProgram(), "programCode");
    },  
    
    onProgramCancelClick: function(button){
        var me=this;
        me.getProgram().setValue("");
        me.handleSelect(me);
    },
    
    onDepartmentSelect: function(){
        var me=this;
		me.handleSelect(me);
    }, 
    
    onDepartmentCancelClick: function(button){
        var me=this;
        me.getDepartment().setValue("");
        me.handleSelect(me);
    },
    
    onDivisionSelect: function(){
        var me=this;
		me.handleSelect(me);
    },   
    
    onDivisionCancelClick: function(button){
        var me=this;
        me.getDivision().setValue("");
        me.handleSelect(me);
    },
    
    handleSelect: function(me){
    	var params = {};
    	me.setParam(params, me.getProgram(), 'programCode');
    	me.setParam(params, me.getDepartment(), 'departmentCode');
    	me.setParam(params, me.getDivision(), 'divisionCode');
		params["objectStatus"] = "ALL"; //Object status and object type filtered client side.
    	me.store.on('load', me.onLoadComplete, this, {single: true});
    	me.store.load({params: params});
    },
    
    onLoadComplete: function(){
		var me = this;
    	me.onObjectStatusFilterSelect();
    },
    
    setParam: function(params, field, fieldName){
    	if(field.getValue() && field.getValue().length > 0)
    		params[fieldName] = field.getValue();
    },

	onTypeFilterSelect:function(){
		var me = this;
		me.onObjectStatusFilterSelect();
	},
	
	onObjectStatusFilterSelect:function(){
			var me = this;
			me.values = {}
			me.values.objectStatus = me.getObjectStatusFilter().getValue();
			me.values.typeValue = me.getTypeFilter().getValue();
			me.store.clearFilter(false);
			me.store.filterBy(me.typeStatusFilter, me);
	},
	
	
	
	typeStatusFilter: function(record){
		var me = this;
		if(me.values.objectStatus != null && me.values.objectStatus != undefined && me.values.objectStatus != 'ALL')
		{
			if(record.get("objectStatus") != me.values.objectStatus)
				return false;
		}
		if(me.values.typeValue != null && me.values.typeValue != undefined && me.values.typeValue != 'ALL')
		{
			if(record.get("isPrivate") == true && me.values.typeValue == "PUBLIC")
				return false;
			if(record.get("isPrivate") == false && me.values.typeValue == "PRIVATE")
				return false;
		}
		return me.filterForPublicTemplates(record);
	},
	
	filterForPublicTemplates: function(record){
		var me = this;
		var typeValue = me.getTypeFilter().getValue();
		var filterValue = true;
		if(typeValue == 'PUBLIC' && (me.getProgram().getValue() == null || me.getProgram().getValue() < 1))
		{
			filterValue = me.hasNoProgram(record);
			if(filterValue == false)
				return filterValue;
		}
		if(typeValue == 'PUBLIC' && (me.getDepartment().getValue() == null || me.getDepartment().getValue() < 1))
		{
			filterValue = me.hasNoDepartment(record);
			if(filterValue == false)
				return filterValue;
		}
		return filterValue;
	},
    
	hasNoProgram: function(record){
		if(record.get("noProgramCode") == null || record.get("noProgramCode") < 1)
			return true;
		return false;
	},
	
	hasNoDepartment: function(record){
		if(record.get("departmentCode") == null || record.get("departmentCode") < 1)
			return true;
		return false;
	},
	
	destroy:function(){
	    var me=this;
	    me.store.clearFilter(false);
	    me.store.removeListener("load", me.onStoreLoaded, me);
	    return me.callParent( arguments );
	}
		
});
