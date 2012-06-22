Ext.define('Ssp.view.tools.profile.SpecialServiceGroups', {
	extend: 'Ext.grid.Panel',
	alias : 'widget.profilespecialservicegroups',
    mixins: [ 'Deft.mixin.Injectable'],
    inject: {
    	store: 'profileSpecialServiceGroupsStore'
    },
	width: '100%',
	height: '100%',
	initComponent: function() {	
		Ext.apply(this, 
				{
		            store: this.store,
					autoScroll: true,
    		        columns: [
    		                { header: 'Special Service Group',  
    		                  dataIndex: 'name',
    		                  flex: 1,
    		                }],
				});
		
		return this.callParent(arguments);
	}
});