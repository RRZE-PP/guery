<!DOCTYPE html>
<html>
	<head>
		<title>Rules</title>
		<meta name="layout" content="main">
		
		<asset:javascript src="jquery.js"/>
		
		<asset:javascript src="guery.js"/>
		<asset:stylesheet src="guery.css"/>
		
	</head>
	<body>
	
		<style>
		
			caption {
				text-align: left;
			}
		</style>
	
		<g:form action="save">
			<guery:builder id="test" instance="${gueryInstance}" />
			<g:submitButton name="Submit" />
			
			<a href="#" onClick="updateMongoQuery()">MongoDB</a>
			<g:link action="init">Init</g:link>
		</g:form>
	
		<br/><br/><br/>
		
		<script>
			function updateMongoQuery() {
				var mongoQuery = JSON.stringify($('#guery_builder_extended').queryBuilder('getMongo'));
				console.log(mongoQuery);
				$('#mongoQuery').html(mongoQuery);
			}
			
			//$('#guery_builder_extended').on('getMongoDBFieldID', function(name, value) {
			//	console.log(name);
			//	console.log(value);
			//});
			
			
		</script>
		<div id="mongoQuery"></div>
	
	</body>
</html>
