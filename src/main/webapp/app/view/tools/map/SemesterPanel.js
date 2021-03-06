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
Ext.define('Ssp.view.tools.map.SemesterPanel', {
    extend: 'Ext.Panel',
    alias: 'widget.semesterpanel',
    mixins: ['Deft.mixin.Injectable', 'Deft.mixin.Controllable'],
	inject:{
		appEventsController: 'appEventsController',
    	currentMapPlan: 'currentMapPlan'
	},
    controller: 'Ssp.controller.tool.map.SemesterPanelViewController',
    columnLines: false,
    layout: {
                type: 'fit'
            },
	height: 200,
	width: 225,
	pastTerm: false,
    initComponent: function(){
        var me = this;
        Ext.apply(me, {
            tools: [
			{
                xtype: 'button',
                itemId: 'isImportantTermButton',
                width: 10,
                height: 20,
                cls: 'importantIconSmall',
                text:'',
                hidden: true,
                tooltip: 'This is an important term!'
                
            },	{
                    xtype: 'tbspacer',
                    flex: .8
                },{
                xtype: 'button',
                itemId: 'pastTermButton',
                width: 20,
                height: 20,
                cls: 'helpIconSmall',
                text:'',
                hidden: !me.pastTerm,
                tooltip: 'This term is in the past and cannot be edited.'
                
            },{
                xtype: 'button',
                itemId: 'termNotesButton',
                width: 20,
                height: 20,
                cls: 'editPencilIcon',
                text:'',
                tooltip: 'Term Notes'
                
            },{
                xtype: 'button',
                itemId: 'deleteButton',
                width: 20,
                height: 20,                
                text:'',
                cls: 'deleteIcon',
                tooltip: 'Select a course and press this button to remove it from the term.'
            }],
			items : [ /*{
				store: me.store,
				scroll: true,
				xtype : 'semestergrid',
			}*/],
           
            dockedItems: [{
                dock: 'bottom',
                xtype: 'toolbar',
                height: '25',
				itemId: "semesterBottomDock",
                items: [
                {
                    xtype: 'tbspacer',
                    flex: .5
                },{
                    text: 'Term Cr. Hrs:',
                    xtype: 'label'
                }, {
                    text: '0',
                    name: 'termCrHrs',
                    itemId: 'termCrHrs',
                    xtype: 'label',
					width: 20
                }
                ,
                 {
                    xtype: 'tbspacer',
                    flex: .5
                }]
            }]
        
        });
        
        return me.callParent(arguments);
    }
    
});