package de.rrze.guery.base

import java.util.Map;

import de.rrze.guery.GueryInstanceHolder
import de.rrze.guery.converters.Javascript
import de.rrze.guery.operator.ClosureOperationManager
import de.rrze.guery.operator.IOperationManager
import de.rrze.guery.operator.Operator

class QueryBase {

	String id
	String description
	IOperationManager operationManager = new ClosureOperationManager()
	Map<String,Boolean>	readonlyBehaviour = [:]
	
	
	
	protected Boolean 				_sortable
	protected Map<String,String>	_lang = [:]
	protected Map<String,Filter> 	_filters = [:]
	protected Map<String,Operator> 	_operators = [:]
	protected List<String>			_conditions = null
	protected String				_defaultCondition = null
	
	def QueryBase() {}
	
	Map<String,Operator> getOperators() {
		_operators
	}
	
	Map<String,Filter> getFilters() {
		_filters
	}
	
	Map<String,String> getLang() {
		_lang
	}
	
	Boolean getSortable() {
		_sortable
	}
	
	List<String> getConditions() {
		_conditions
	}
	
	String getDefaultCondition() {
		_defaultCondition
	}
	
	QueryBase addOperator(Operator o) {
		// add language label for operator
		if (o.label) {
			this.lang.put("operator_${o.type}".toString(), o.label)
		}
		
		// add operator
		if(!this._operators.containsKey(o.type)) this._operators.put(o.type, o)
			else throw new RuntimeException("An operator of type ${o.type} already exists and cannot be re-added!")
		
		this
	}

	@Deprecated	
	QueryBase add(Operator o) {
		addOperator(o)
	} 
	
	def getOperator(String type) {
		this.getOperators().get(type)
	}
	
	QueryBase addFilter(Filter f) {
		
		// find and automatically add missing operators
		if (f.operators) {
			def addOpKeys = f.operators*.type - this.operators?.keySet()?:[]
			def addOps = f.operators.findAll { it.type in addOpKeys }
			addOps.each { this.addOperator(it) }
		}
		
		// add filter
		if(!this._filters.containsKey(f.id))  this._filters.put(f.id, f)
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
		putIfNotEmpty(ret,"conditions")
		putIfNotEmpty(ret,"defaultCondition")
		putIfNotEmpty(ret,"lang")
		putIfNotEmpty(ret,"readonlyBehaviour")
		
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
			if (fieldName == 'defaultCondition') map.put('default_condition', value)
			if (fieldName == 'readonlyBehaviour') map.put('readonly_behavior', value)
			else map.put(fieldName, value)
		}
		map
	}
	
	
	
}
