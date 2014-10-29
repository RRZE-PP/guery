<!DOCTYPE html>
<html>
	<head>
		<title>Rules</title>
		<meta name="layout" content="main">
		
		<r:require modules="jq_queryBuilder" />
		
	</head>
	<body>
	
		<style>
		
			caption {
				text-align: left;
			}
		</style>
	
	
		<!-- FORM 1 -->
		<guery:builderFormRemote id="form1" name="save" url="${[action:'save', params:[ajax:(params.ajax?true:false)]]}"
			builderResultName="queryBuilderResult">
		
			<div class="dialog editDialog">
				<table>
					<caption><g:message code="queryRules.table.caption" default="Query rule" /></caption>
					<tr><td>
						<div id="${builderElementId}"></div>
					</td></tr>
				</table>
			</div>
		
			<fieldset class="buttons">
				<g:submitButton name="save" class="save" value="${message(code: 'default.button.save.label', default: 'Save')}" />
				<g:link action="init">Re-Init</g:link>
			</fieldset>
			
		</guery:builderFormRemote>
		
		
		<br/>
		<br/>
		
		<!-- FORM 2 -->
		<guery:builderFormRemote id="form2" name="save" url="${[action:'save', params:[ajax:(params.ajax?true:false)]]}"
			builderResultName="queryBuilderResult">
		
			<div class="dialog editDialog">
				<table>
					<caption><g:message code="queryRules.table.caption" default="Query rule" /></caption>
					<tr><td>
						<div id="${builderElementId}"></div>
					</td></tr>
				</table>
			</div>
		
			<fieldset class="buttons">
				<g:submitButton name="save" class="save" value="${message(code: 'default.button.save.label', default: 'Save')}" />
				<g:link action="init">Re-Init</g:link>
			</fieldset>
			
		</guery:builderFormRemote>
	
	</body>
</html>
