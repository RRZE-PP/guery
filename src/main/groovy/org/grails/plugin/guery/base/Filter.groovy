package org.grails.plugin.guery.base

import org.grails.plugin.guery.operator.Operator
import groovy.util.logging.Log4j

/**
 * Generic filter representation 
 * 
 * @author unrza249
 *
 */
@Log4j
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
	Collection<Operator> operators = []
	Object					values // FIXME implement some type safety later
    String                  plugin
    Map<String,String>      plugin_config

	// generic data for access by operator implementations
	Object					data
	
	def Filter() {}
	
	def add(Operator o) {
		o.filter = this
		operators << o
	}

	def getField() {
		if (!this.field) return this.id
		else return this.field
	}
	
	def flatten(Map params = [:]) {
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

		if (values) {
			def _values = values
			if (values in Closure) {
				_values = values(params)
			}
			
			if (_values instanceof LinkedHashMap || _values instanceof SortedMap) {
				// ordered
				ret['values'] = _values.collect { [ (it.key) : (it.value) ] }
			}
			else {
				// unordered
				ret['values'] = _values
			}
		}
		
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
