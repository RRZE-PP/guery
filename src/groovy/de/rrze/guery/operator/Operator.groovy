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
	
	
	def apply(val, obj) {
		qb.operationManager.apply(type,val,obj)
	}
	
	
	def flatten() {
		def ret = [:]
		
		putIfNonNull(ret,"type")
		putIfNonNull(ret,"accept_values")
		putIfNonNull(ret,"apply_to")
		
		
		ret
	}
	
	
	private putIfNonNull(Map map, String fieldName) {
		def value = this."${fieldName}"
		if (value != null) {
			map.put(fieldName, value)
		}
		map
	}
}
