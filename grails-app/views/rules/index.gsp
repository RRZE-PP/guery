<!DOCTYPE html>
<html>
	<head>
		<title>Rules</title>
		<meta name="layout" content="main">
		
		<r:require modules="jquery, jquery-ui, jq_queryBuilder" />
		
	</head>
	<body>
	
		<style>
		
			caption {
				text-align: left;
			}
		</style>
	
		<script>
			function updateQueryBuilderResult() {
				var jsonResult = JSON.stringify($('#builder').queryBuilder('getRules'), undefined, 2);
				console.log(jsonResult);
				$('#queryBuilderResult').val(jsonResult);
			}
	
		
		</script>
	
	
		<g:formRemote name="save" 
			before="updateQueryBuilderResult()"
			url="${[action:'save', params:[ajax:(params.ajax?true:false)]]}">
	
			<g:hiddenField name="queryBuilderResult" value="" />
	
			<div class="dialog editDialog">
		
				<%-- QUERY BUILDER --%>
				<table>
					<caption><g:message code="queryRules.table.caption" default="Query rule" /></caption>
					<tr><td>
						<div id="builder"></div>
					</td></tr>
				</table>
				<script>
					$('#builder').queryBuilder(${config});
					<%
						if (rules) {
							out << '$(\'#builder\').queryBuilder(\'setRules\',' + rules + ');'
						}
					%>
				</script>
			
			</div>
		
		
			<fieldset class="buttons">
				<g:submitButton name="save" class="save" value="${message(code: 'default.button.save.label', default: 'Save')}" />
				<g:link action="init">Re-Init</g:link>
			</fieldset>
			
		</g:formRemote>
	
	</body>
</html>
