package de.rrze.guery

class GueryTagLib {

	
	static namespace = "guery"
	
	def pluginManager
	
	def builder = { attrs, body ->
		def gueryAttrs = [:]
		gueryAttrs.putAll(attrs)
		
		
		gueryAttrs.builderConfig = attrs.builderConfig?:pageScope.builderConfig
		gueryAttrs.builderRules = attrs.builderRules?:pageScope.builderRules
		gueryAttrs.builderElementId = attrs.id //attrs.builderElementId?:"${gueryAttrs.id?:gueryAttrs.name}_queryBuilder"
		gueryAttrs.builderResultName = attrs.builderResultName?:"${gueryAttrs.builderElementId}_result"
		gueryAttrs.builderResultId = attrs.builderResultId?:"${gueryAttrs.builderElementId}_result"
		
		out << '<div class="guery-container">'
		
		out << """
	<script>
		function update_${gueryAttrs.builderElementId}() {
			var jsonResult = JSON.stringify(\$('#${gueryAttrs.builderElementId}').queryBuilder('getRules'), undefined, 2);
			\$('#${gueryAttrs.builderResultId}').val(jsonResult);
		}

		\$(function(){
			var form = \$('#${gueryAttrs.builderElementId}').closest('form');
			form.attr('onsubmit', 'update_${gueryAttrs.builderElementId}();' + form.attr('onsubmit'))
		});
	</script>
"""
		out << g.hiddenField(id:gueryAttrs.builderResultId, name:gueryAttrs.builderResultName, value:'')
		
		if (body) {
			out << body(builderElementId:gueryAttrs.builderElementId)
		}
		else {
			out << "	<div id=\"${gueryAttrs.builderElementId}\"></div>"
		}
		
		out << """
	<script>
		\$('#${gueryAttrs.builderElementId}').queryBuilder(${gueryAttrs.builderConfig});
"""
		if (gueryAttrs.builderRules) {
			out << "	\$('#${gueryAttrs.builderElementId}').queryBuilder('setRules',jQuery.parseJSON('${gueryAttrs.builderRules}'));"
		}
		
		out << """
	</script>
</div>
"""

	}
	
	
	def builderFormRemote = { attrs, body ->
		
//		if(pluginManager.allPlugins.find { it.name == "resources" }) {
//			r.require(modules:"jq_queryBuilder")
//		}
		
		
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
