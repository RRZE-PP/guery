

modules = {
	jq_queryBuilder {
		dependsOn 'jquery'
		dependsOn 'jquery-ui'
		
		resource url:'js/jQuery-QueryBuilder-1.2.1/dist/query-builder.js', disposition:'head'
		resource url:'js/jQuery-QueryBuilder-1.2.1/dist/query-builder.css', disposition:'head'
		resource url:'js/jQuery-QueryBuilder-fixes.css', disposition:'head'
	}

    jq_selectize {
        dependsOn 'jquery'

        resource url: 'js/selectize-0.11.2/standalone/selectize.min.js', disposition: 'head'
        resource url: 'js/selectize-0.11.2/selectize.default.css', disposition: 'head'
        resource url: 'js/jQuery-Selectize-fixes.css', disposition: 'head'
    }
}