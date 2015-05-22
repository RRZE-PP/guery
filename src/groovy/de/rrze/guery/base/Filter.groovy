package de.rrze.guery.base

import de.rrze.guery.converters.JavascriptCode
import de.rrze.guery.operator.Operator

/**
 * Generic filter representation 
 * 
 * @author unrza249
 *
 */
class Filter {

	String 					id //required
	String 					field
	String 					label
	String					description
	String 					type = 'string' // required
	String 					input
	Boolean 				multiple
	String 					placeholder
	Boolean 				vertical
	Collection<Operator>	operators = []
	Map<String,String>		values = [:]
    String                  plugin
    Map<String,String>      plugin_config = [:]
    JavascriptCode          onAfterCreateRuleInput
    JavascriptCode          onAfterSetValue

	
	def Filter() {}
	
	def add(Operator o) {
		o.filter = this
		operators << o
	}
	
	def setOnAfterCreateRuleInput(String s) {
		onAfterCreateRuleInput = new JavascriptCode(s)
	}

	def setOnAfterSetValue(String s) {
		onAfterSetValue = new JavascriptCode(s)
	}

	def getField() {
		if (!this.field) return this.id
		else return this.field
	}
	
	def flatten() {
		def ret = [:]
		
		putIfNotEmpty(ret,"id")
		putIfNotEmpty(ret,"field")
		putIfNotEmpty(ret,"label")
		putIfNotEmpty(ret,"description")
		putIfNotEmpty(ret,"type")
		putIfNotEmpty(ret,"input")
		putIfNotEmpty(ret,"multiple")
		putIfNotEmpty(ret,"placeholder")
		putIfNotEmpty(ret,"vertical")
        putIfNotEmpty(ret,"plugin")
        putIfNotEmpty(ret,"plugin_config")
        putIfNotEmpty(ret,"onAfterCreateRuleInput")

		if (values) {
			if (values instanceof LinkedHashMap || values instanceof SortedMap) {
				// ordered
				ret['values'] = values.collect { [ (it.key) : (it.value) ] }
//				ret['values'] = values.collect { it.value } // test for backwards compatibility
			}
			else {
				// unordered
				ret['values'] = values
			}
		}
		
		// FIXME find better plugin mechanism
        if (this.plugin?.contains("selectize") && !this.onAfterSetValue) {
            this.onAfterSetValue = "function(\$rule, value) {" +
                    "var selectize = \$rule.find('.rule-value-container input')[0].selectize;" +
                    "selectize.setValue(value);" +
                    "}"
        }
		putIfNotEmpty(ret,"onAfterSetValue")

		// handle operators
		def flatOperators = []
		this.operators.each {
			flatOperators << it.type
		}
		if(flatOperators) ret.put('operators', flatOperators)
		
		ret
	}
		
	
	private putIfNotEmpty(Map map, String fieldName) {
		def value = this."${fieldName}"
		if (value) {
			map.put(fieldName, value)
		}
		map
	}
}
