//= require guery

$.fn.queryBuilder.define('readonly', function(options) {
	/* "this" is the QueryBuilder instance */
  	var that = this;

	this.on('afterAddRule', function(e, rule) {
        console.log(rule);
        jQuery.extend(rule.__.flags, options.rules);
	});

    this.on('afterAddGroup', function(e, group) {
        console.log(group);
        jQuery.extend(group.__.flags, options.groups);
    });
	

}, {
	/* optional default plugin configuration */

	rules: {
        filter_readonly: true,
        operator_readonly: true,
        value_readonly: true,
        no_delete: true
    },
    groups: {
        condition_readonly: true,
        no_add_group: true,
        no_add_rule: true,
        no_delete: true,
        no_drop: true,
        no_sortable: true,
    }

});