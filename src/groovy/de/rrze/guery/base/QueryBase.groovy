package de.rrze.guery.base

import java.util.Map;

import de.rrze.guery.converters.Javascript
import de.rrze.guery.converters.JavascriptCode
import de.rrze.guery.operator.ClosureOperationManager
import de.rrze.guery.operator.IOperationManager
import de.rrze.guery.operator.Operator

class QueryBase {

	String id
	String description
	
	protected final IOperationManager operationManager = new ClosureOperationManager()
	protected final Map sharedData = [:]
	
	protected Boolean 				_sortable
	protected Map<String,String>	_lang = [operators:[:], conditions:[:], errors:[:]]
	protected Map<String,Filter> 	_filters = [:]
	protected Map<String,Operator> 	_operators = [:]
	protected List<String>			_conditions = null
	protected String				_defaultCondition = null
	protected Set<String>			_plugins
	protected Boolean				_allowEmpty
	
	
	
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
	
	Set<String> getPlugins() {
		_plugins
	}
	
	Boolean getAllowEmpty() {
		_allowEmpty
	}
	
	List<String> getConditions() {
		_conditions
	}
	
	String getDefaultCondition() {
		_defaultCondition
	}
	
	
	def getExposedData(String id) {
		return sharedData.get(id)
	} 
	
	
	QueryBase addOperator(Operator o) {
		if(!this._operators.containsKey(o.type)) {
			
			// add operator
			this._operators.put(o.type, o)
			
			// add language label for operator
			if (o.label) {
				this._lang.operators.put("${o.type}".toString(), o.label)
			}
			
		}
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
		putIfNotEmpty(ret,"plugins")
		putIfNotEmpty(ret,"allowEmpty")
		
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
		
		// handle mongoOperators
		def flatMongoOperators = [:]
		this.operators.each { k, v ->
			if (v.mongo) flatMongoOperators.put(k, v.mongo)
		}
		if (flatMongoOperators) ret.put('mongoOperators', flatMongoOperators)
		
		ret
	}
		
	public Javascript toJs() {
		def flatConfig = this.flatten()
		def js = new Javascript(flatConfig)
		js
	}
	
	public String toJsString(Boolean prettyPrint) {
		if (prettyPrint) {
			toJs().toString(prettyPrint)
		}
		else  {
			toJs().toString()
		}
	}
	
	private Map putIfNotEmpty(Map map, String fieldName) {
		def value = this."${fieldName}"
		
		if (value) {
			if (fieldName == 'defaultCondition') map.put('default_condition', value)
			else if (fieldName == 'readonlyBehaviour') map.put('readonly_behavior', value)
			else if (fieldName == 'allowEmpty') map.put('allow_empty', value)
			else map.put(fieldName, value)
		}
		map
	}
	
	
	
}
