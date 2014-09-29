

modules = {
	jq_queryBuilder {
		dependsOn 'jquery'
		dependsOn 'jquery-ui'
		
		resource url:'js/jQuery-QueryBuilder-1.2.1/dist/query-builder.js', disposition:'head'
		resource url:'js/jQuery-QueryBuilder-1.2.1/dist/query-builder.min.css', disposition:'head'
		resource url:'js/jQuery-QueryBuilder-fixes.css', disposition:'head'
		
	}
}