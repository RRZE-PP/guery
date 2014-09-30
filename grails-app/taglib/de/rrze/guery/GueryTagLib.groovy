package de.rrze.guery

class GueryTagLib {

	
	static namespace = "guery"
	
	def pluginManager
	
	def builderFormRemote = { attrs, body ->
		
		if(pluginManager.allPlugins.find { it.name == "resources" }) {
			r.require(modules:"jq_queryBuilder")
		}
		
		
		def gueryAttrs = [:]
		gueryAttrs.putAll(attrs)
		
		
		gueryAttrs.builderConfig = attrs.builderConfig?:pageScope.builderConfig
		gueryAttrs.builderRules = attrs.builderRules?:pageScope.builderRules
		gueryAttrs.builderElementId = attrs.builderElementId?:"${gueryAttrs.id?:gueryAttrs.name}_queryBuilder"
		gueryAttrs.builderResultName = attrs.builderResultName?:"${gueryAttrs.builderElementId}_result"
		gueryAttrs.builderResultId = attrs.builderResultId?:"${gueryAttrs.builderElementId}_result"
		
		
		gueryAttrs.before = (gueryAttrs.before?gueryAttrs.before.replaceAll(";\$", "") + ';':"") + "update_${gueryAttrs.builderElementId}()"
		gueryAttrs."class" =  gueryAttrs."class"?gueryAttrs."class" + " ":"" + "guery-container"

		
		
		def gueryBody = {
			def ret = ""
			
			ret += g.hiddenField(id:gueryAttrs.builderResultId, name:gueryAttrs.builderResultName, value:'')
			
			ret += body(builderElementId:gueryAttrs.builderElementId)
			
		    ret += """
<script>
	\$('#${gueryAttrs.builderElementId}').queryBuilder(${gueryAttrs.builderConfig});
""" 
			
			if (gueryAttrs.rules) {
				ret += "\$('#${gueryAttrs.builderElementId}').queryBuilder('setRules','${gueryAttrs.builderRules}');"
		    }
			
			ret += """
</script>
"""
		}
		
		
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
	}
	
}
