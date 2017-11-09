package de.rrze.guery

class GueryTagLib {

	
	static namespace = "guery"
	
	def pluginManager
	
	def builder = { attrs, body ->
		def gueryAttrs = [:]
		gueryAttrs.putAll(attrs)
		
		
		def gueryParams = attrs.params?:[:]
		
		if (attrs.plugins) {
			if (!gueryParams.plugins) gueryParams.plugins = [:]
			
			if (attrs.plugins instanceof String) {
				gueryParams.plugins.putAll(attrs.plugins.split(',').collectEntries { e-> [(e):null] })
			}
			else if (attrs.plugins instanceof Map) {
				gueryParams.plugins.putAll(attrs.plugins)
			}
			else if (attrs.plugins instanceof Collection) {
				gueryParams.plugins.putAll(attrs.plugins.collectEntries { e-> [(e):null] })
			}
			else {
				log.warn("Unable to parse data from 'plugins' parameter: ${attrs.plugins}")
			}
		}
		
		if (attrs.instance) {
			def instance = attrs.instance
			gueryAttrs.builderConfig = instance.baseToJsString(gueryParams)
			gueryAttrs.builderElementId = "guery_builder_${instance.id}"
		}
		else {
			gueryAttrs.builderConfig = attrs.builderConfig?:pageScope.builderConfig
			gueryAttrs.builderElementId = attrs.id
		}

		if (attrs.policy != null) {
			gueryAttrs.builderRules = attrs.policy.toJSON()
		}
		else {
			gueryAttrs.builderRules = attrs.builderRules?:pageScope.builderRules
		}

		gueryAttrs.builderResultName = attrs.builderResultName?:"${gueryAttrs.builderElementId}_result"
		gueryAttrs.builderResultId = attrs.builderResultId?:"${gueryAttrs.builderElementId}_result"
						
		out << '<div class="guery-container">'
		
		out << """
	<script>
		var ${gueryAttrs.builderElementId}_validation_success = true;

		function update_${gueryAttrs.builderElementId}() {
			${gueryAttrs.builderElementId}_validation_success = true;
			var rules = \$('#${gueryAttrs.builderElementId}').queryBuilder('getRules');
            var jsonResult = rules;
//			var jsonResult = JSON.stringify(rules, undefined, 2);
			\$('#${gueryAttrs.builderResultId}').val(jsonResult);
		}

		\$(function(){
			
			\$('#${gueryAttrs.builderElementId}').on('validationError.queryBuilder', function(e, rule, error, value) {
				${gueryAttrs.builderElementId}_validation_success = false;
			});

			var form = \$('#${gueryAttrs.builderElementId}').closest('form');
			form.attr('onsubmit', 'update_${gueryAttrs.builderElementId}(); if (${gueryAttrs.builderElementId}_validation_success) {' + form.attr('onsubmit') + ';} else return false;')
		});
	</script>
"""
		out << g.hiddenField(id:gueryAttrs.builderResultId, name:gueryAttrs.builderResultName, value:'')

		// This is the jQuery Query Builder main element
		def builderElement = [tag:'div', attrs:[id:gueryAttrs.builderElementId], content:'']

		// process child elements in body
		def bodyHtml
		if (body) {
			bodyHtml = body(builderElementId:gueryAttrs.builderElementId, builderElement: builderElement)
		}
		
		// output main element and body result
		def builderHtml = "<${builderElement.tag}"
		builderElement.attrs.each {
			builderHtml += " ${it.key}=\"${it.value}\""
		}
		builderHtml +=">${builderElement.content}</${builderElement.tag}>"
		out << builderHtml
		out << bodyHtml
		
		// init builder configuration from queryBase
		// and load builderRules - if any are given
		out << """
	<script>
		\$(function(){
			console.log('Initializing query builder ${gueryAttrs.builderElementId} ...');
			\$('#${gueryAttrs.builderElementId}').queryBuilder(${gueryAttrs.builderConfig});
"""
		if (gueryAttrs.builderRules != null) {
			out << "		\$('#${gueryAttrs.builderElementId}').queryBuilder('setRules',jQuery.parseJSON('${gueryAttrs.builderRules}'));"
		}
		
		out << """
			\$('#${gueryAttrs.builderElementId}').trigger('afterInit.${gueryAttrs.builderElementId}');
		}); 
	</script>"""
		
		
		// the end
		out << "</div>"
	}
	
	/**
	 *  This is meant to be used in the body of a builder element 
	 */
	def rules = { attrs, body ->
		def builderElementId = pageScope.builderElementId
		def builderElement = pageScope.builderElement
		if (!builderElementId || !builderElement) {
			throw new RuntimeException("Element <guery:rules /> must be located inside a <guery:builder /> element!")
		}

		def data = attrs.jsonData
		def dataId = attrs.identifier
		
		builderElement.attrs += ['data-id':dataId]
		
		out << """
	<script>
			console.log('Installing afterInit.${builderElementId} trigger...');
			\$('#${builderElementId}').on('afterInit.${builderElementId}', function(e) {
				console.log('Loading rules... ${builderElementId}');
				\$('#${builderElementId}').queryBuilder('setRules',jQuery.parseJSON('${data}'));
				console.log('Loading complete: ${builderElementId}');
			});
	</script>
"""
	}
	
	@Deprecated
	def builderFormRemote = { attrs, body ->
		def gueryAttrs = [:]
		gueryAttrs.putAll(attrs)
		
		
		gueryAttrs.builderConfig = attrs.builderConfig?:pageScope.builderConfig
		gueryAttrs.builderRules = attrs.builderRules?:pageScope.builderRules
		gueryAttrs.builderElementId = attrs.builderElementId?:"${gueryAttrs.id?:gueryAttrs.name}_queryBuilder"
		gueryAttrs.builderResultName = attrs.builderResultName?:"${gueryAttrs.builderElementId}_result"
		gueryAttrs.builderResultId = attrs.builderResultId?:"${gueryAttrs.builderElementId}_result"
		
		
		gueryAttrs.before = (gueryAttrs.before?gueryAttrs.before.replaceAll(";\$", "") + ';':"") + "update_${gueryAttrs.builderElementId}()"
		
		def gueryBody = {
			def ret = ""
			
			ret += g.hiddenField(id:gueryAttrs.builderResultId, name:gueryAttrs.builderResultName, value:'')
			
			ret += body(builderElementId:gueryAttrs.builderElementId)
			
		    ret += """
<script>
	\$('#${gueryAttrs.builderElementId}').queryBuilder(${gueryAttrs.builderConfig});
""" 
			
			if (gueryAttrs.builderRules) {
				ret += "\$('#${gueryAttrs.builderElementId}').queryBuilder('setRules',jQuery.parseJSON('${gueryAttrs.builderRules}'));"
		    }
			
			ret += """
</script>
"""
			return ret
		}
		
		
		out << '<div class="guery-container">'
		
		out << """
<script>
	function update_${gueryAttrs.builderElementId}() {
		var jsonResult = JSON.stringify(\$('#${gueryAttrs.builderElementId}').queryBuilder('getRules'), undefined, 2);
		\$('#${gueryAttrs.builderResultId}').val(jsonResult);
	}
</script>
"""
		def filterAttrs = ['builderConfig', 'builderRules', 'builderElementId']
		out << g.formRemote(gueryAttrs.findAll { !(it.key in filterAttrs) }, gueryBody)
		
		out << '</div>'
		
	}
	
}
