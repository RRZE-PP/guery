

modules = {
//	jq_queryBuilder {
//		dependsOn 'jquery'
//		dependsOn 'jquery-ui'
//		
//		resource url:'js/jQuery-QueryBuilder-1.2.1/dist/query-builder.js', disposition:'head'
//		resource url:'js/jQuery-QueryBuilder-1.2.1/dist/query-builder.css', disposition:'head'
//		resource url:'js/jQuery-QueryBuilder-fixes.css', disposition:'head'
//	}

//    jq_selectize {
//        dependsOn 'jquery'
//
//        resource url: 'js/selectize-0.11.2/standalone/selectize.min.js', disposition: 'head'
//        resource url: 'js/selectize-0.11.2/selectize.default.css', disposition: 'head'
//        resource url: 'js/jQuery-Selectize-fixes.css', disposition: 'head'
//    }
	
	jq_queryBuilder {
		dependsOn 'jquery-querybuilder-1.2.1'
	}
	
	jq_selectize {
		dependsOn 'jquery'

		resource url: 'js/selectize/0.11.2/selectize.min.js', disposition: 'head'
		resource url: 'css/selectize/0.11.2/selectize.default.css', disposition: 'head'
		resource url: 'css/selectize/0.11.2/jQuery-Selectize-fixes.css', disposition: 'head'
	}
	
	'jquery-querybuilder-1.2.1' {
		
		dependsOn 'jquery'
		dependsOn 'jquery-ui'
		
		resource url: 'js/jQuery-QueryBuilder/1.2.1/query-builder.min.js', disposition: 'head'
		
		resource url: 'css/jQuery-QueryBuilder/1.2.1/query-builder.min.css', disposition: 'head'
		resource url: 'css/jQuery-QueryBuilder/1.2.1/jQuery-QueryBuilder-fixes.css', disposition: 'head'
		
	}
	
	'jquery-querybuilder-2.0.1' {
		
		dependsOn 'jquery'
		dependsOn 'jquery-ui'
		
		resource url: 'js/bootstrap/3.3.4/bootstrap.js', disposition: 'head'
		
		resource url: 'js/jQuery-QueryBuilder/2.0.1/jQuery.extendext.js', disposition: 'head'
		resource url: 'js/jQuery-QueryBuilder/2.0.1/query-builder.min.js', disposition: 'head'
		
		resource url: 'css/jQuery-QueryBuilder/2.0.1/query-builder.default.css', disposition: 'head'
		
		resource url: 'css/bootstrap/3.3.4/bootstrap.css', disposition: 'head'
	
	}
}