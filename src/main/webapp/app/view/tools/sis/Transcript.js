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
Ext.define('Ssp.view.tools.sis.Transcript', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.sistranscript',
    mixins: [ 'Deft.mixin.Injectable',
              'Deft.mixin.Controllable'],
    controller: 'Ssp.controller.tool.sis.TranscriptViewController',
    width: '100%',
    height: '100%',
    autoScroll: true,
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            columns: [
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'string',
                    text: 'Course'
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'string',
                    text: 'Section'
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'string',
                    text: 'Credit'
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'string',
                    text: 'Title'
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'string',
                    text: 'Grade'
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'string',
                    text: 'Term'
                }
            ],
            viewConfig: {

            }
        });

        me.callParent(arguments);
    }
});