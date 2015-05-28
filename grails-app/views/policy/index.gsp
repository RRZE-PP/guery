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
			
		</g:form>
	
		<br/><br/><br/>
		
		<script>
			function updateMongoQuery() {
				var mongoQuery = JSON.stringify($('#guery_builder_extended').queryBuilder('getMongo'));
				console.log(mongoQuery);
				$('#mongoQuery').html(mongoQuery);
			}
		</script>
		<div id="mongoQuery"></div>
	
	</body>
</html>
