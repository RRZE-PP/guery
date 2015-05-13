package de.rrze.guery.operator

import de.rrze.guery.base.Filter
import de.rrze.guery.base.QueryBase

class Operator {

	QueryBase 			qb
	Filter				filter
	
	String				type
	Boolean				accept_values
	Collection<String>	apply_to
	
	String 				label
	
	def Operator() {}
	
	
	Object apply(Object val, Map req, Map res) {
		def result
		
		if (accept_values) {
			result = qb.operationManager.apply(type,val,req,res)
		}
		else {
			result = qb.operationManager.apply(type,req,res)
		}
		
		log.trace("Operator ${type} ===> ${result}")
		return result
	}
	
	
	Map flatten() {
		def ret = [:]
		
		putIfNonNull(ret,"type")
		putIfNonNull(ret,"apply_to")
		
		// FIXME Hack to support v2.1.0
//		putIfNonNull(ret,"accept_values")
		if (accept_values) ret.nb_inputs = 1
		else ret.nb_inputs = 0
		
		ret.multiple = false

		
		
		ret
	}
	
	
	private Map putIfNonNull(Map map, String fieldName) {
		def value = this."${fieldName}"
		if (value != null) {
			map.put(fieldName, value)
		}
		map
	}
}
