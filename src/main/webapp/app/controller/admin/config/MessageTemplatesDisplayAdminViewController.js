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
Ext.define('Ssp.controller.admin.config.MessageTemplatesDisplayAdminViewController', {
    extend: 'Deft.mvc.ViewController',
    mixins: [ 'Deft.mixin.Injectable' ],
    inject: {
    	apiProperties: 'apiProperties',
    	store: 'messageTemplatesStore',
    	formUtils: 'formRendererUtils',
		model: 'currentMessageTemplate',
		adminSelectedIndex: 'adminSelectedIndex'
    },
    config: {
    	containerToLoadInto: 'adminforms',
    	formToDisplay: 'messagetemplatedetails'
    },
    control: {
    	'editButton': {
			click: 'onEditClick'
		}
    },       
	init: function() {
		var me=this;
		
		me.formUtils.reconfigureGridPanel( me.getView(), me.store);
		var ptb = me.getView().down('pagingtoolbar');
        var asidx = me.adminSelectedIndex.get('value');
        var pageidx = parseInt(asidx / me.apiProperties.getPagingSize());
        var startidx = pageidx * me.apiProperties.getPagingSize();
        
        me.store.load({
            params: {
                start: startidx,
                page: (pageidx + 1)
            },
            callback: function(){
                if (asidx >= 0) {
                    var rowidx = (asidx - startidx);
                    
                    me.getView().getSelectionModel().select(rowidx);
                    
                    var sn = me.getView().getView().getSelectedNodes()[0];
                    
                    Ext.get(sn).highlight(Ssp.util.Constants.SSP_EDITED_ROW_HIGHLIGHT_COLOR,
                        Ssp.util.Constants.SSP_EDITED_ROW_HIGHLIGHT_OPTIONS);
                    me.adminSelectedIndex.set('value', -1);
                   
                    if (ptb) {
                       
                        ptb.child('#inputItem').setValue((pageidx + 1));
                        
                    }
					me.getView().getStore().currentPage = (pageidx + 1); 
                }
            }
        });
		
		return me.callParent(arguments);
    }, 
    
	onEditClick: function(button) {
		var grid, record, idx;
		grid = button.up('grid');
		record = grid.getView().getSelectionModel().getSelection()[0];
		
		this.adminSelectedIndex.set('value', -1);
        if (record) 
        {	
			this.model.data=record.data;
        	this.displayEditor();
        }else{
     	   Ext.Msg.alert('SSP Error', 'Please select an item to edit.'); 
        }
	},
	
	displayEditor: function(){
		var comp = this.formUtils.loadDisplay(this.getContainerToLoadInto(), this.getFormToDisplay(), true, {});
	}
});