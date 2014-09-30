package de.rrze.guery.operator

import java.util.Map;

import de.rrze.guery.base.QueryBase;

class Operator {

	QueryBase 	qb
	
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
		
		return result
	}
	
	
	Map flatten() {
		def ret = [:]
		
		putIfNonNull(ret,"type")
		putIfNonNull(ret,"accept_values")
		putIfNonNull(ret,"apply_to")
		
		
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
