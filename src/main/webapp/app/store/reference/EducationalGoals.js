Ext.define('Ssp.store.reference.EducationalGoals', {
    extend: 'Ssp.store.reference.AbstractReferences',
    model: 'Ssp.model.reference.EducationalGoal',
    storeId: 'educationalGoalsReferenceStore',
    constructor: function(){
    	this.callParent(arguments);
    	var url = this.getProxy().url;
    	Ext.apply(this.getProxy(),{url: url+'educationGoal/'});
    }
});