package de.rrze.guery.base

import de.rrze.guery.converters.Javascript
import de.rrze.guery.operator.ClosureOperationManager
import de.rrze.guery.operator.IOperationManager
import de.rrze.guery.operator.Operator

class QueryBase {

	String id
	String description
	
	Boolean 				sortable
	Map<String,String>		lang = [:]
	
	
	Map<String,Filter> 		filters = [:]
	
	Map<String,Operator> 	operators = [:]
	IOperationManager		operationManager = new ClosureOperationManager()
	
	def QueryBase() {}
	
	
	QueryBase addOperator(Operator o) {
		// add language label for operator
		if (o.label) {
			this.lang.put("operator_${o.type}".toString(), o.label)
		}
		
		// add operator
		if(!this.operators.containsKey(o.type)) this.operators.put(o.type, o)
			else throw new RuntimeException("An operator of type ${o.type} already exists and cannot be re-added!")
		
		this
	}

	@Deprecated	
	QueryBase add(Operator o) {
		addOperator(o)
	} 
	
	
	QueryBase addFilter(Filter f) {
		
		// find and automatically add missing operators
		if (f.operators) {
			def addOpKeys = f.operators*.type - this.operators?.keySet()?:[]
			def addOps = f.operators.findAll { it.type in addOpKeys }
			addOps.each { this.add(it) }
		}
		
		// add filter
		if(!this.filters.containsKey(f.id))  this.filters.put(f.id, f)
			else throw new RuntimeException("A filter with id ${f.id} already exists and cannot be re-added!")
		
		this
	}
	
	@Deprecated
	QueryBase add(Filter f) {
		addFilter(f)
	}
	
	Map flatten() {
		def ret = [:]
		
		putIfNotEmpty(ret,"sortable")
		putIfNotEmpty(ret,"lang")
			
		// handle filters
		def flatFilters = []
		this.filters.values().each {
			flatFilters << it.flatten()
		}
		ret.put('filters', flatFilters)
		
		
		// handle operators
		def flatOperators = []
		this.operators.values().each {
			flatOperators << it.flatten()
		}
		if (flatOperators) ret.put('operators', flatOperators)
		
		ret
	}
		
	public Javascript toJs() {
		def flatConfig = this.flatten()
//		JSON.registerObjectMarshaller(new LinkedHashMapMarshaller(),0)
		def js = new Javascript(flatConfig)
		js
	}
	
	public String toJsString(Boolean prettyPrint) {
		toJs().toString(prettyPrint)
	}
	
	
	
	private Map putIfNotEmpty(Map map, String fieldName) {
		def value = this."${fieldName}"
		if (value) {
			map.put(fieldName, value)
		}
		map
	}
	
}
