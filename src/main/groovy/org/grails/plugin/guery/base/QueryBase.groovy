package org.grails.plugin.guery.base

import org.grails.plugin.guery.converters.Javascript
import org.grails.plugin.guery.operator.ClosureOperationManager
import org.grails.plugin.guery.operator.IOperationManager
import org.grails.plugin.guery.operator.Operator
import groovy.util.logging.Log4j

@Log4j
class QueryBase {

	String id
	String description
	
	protected final IOperationManager operationManager = new ClosureOperationManager()
	protected final Map sharedData = [:]
	protected final Map instanceData = [:]
	
	protected Boolean 				_sortable
	protected Map<String,String>	_lang = [operators:[:], conditions:[:], errors:[:]]
	protected Map<String,Filter>    _filters = [:]
	protected Map<String,Operator>  _operators = [:]
	protected List<String>			_conditions = null
	protected String				_defaultCondition = null
	protected Set<String>			_plugins // FIXME should be map with <String,String> <-- second string is JSON
	protected Boolean				_allowEmpty
	
	
	
	def QueryBase() {}
	
	def QueryBase(Map qbMap) {
		parseFromMap(qbMap)
	}
	
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
	
	
	QueryBase parseFromMap(Map qbMap) {
		id			= qbMap.name
		description = qbMap.description
		
		_sortable 			= qbMap.queryBase.sortable
		_plugins			= qbMap.queryBase.plugins
		_allowEmpty 		= qbMap.queryBase.allow_empty
		_conditions 		= qbMap.queryBase.conditions
		_defaultCondition 	= qbMap.queryBase.default_condition
		
		def parsedOperatorsMap = [:]
		qbMap.queryBase.operators.each {
			def newOperator = new Operator([
				qb				: this,
				type			: it.type,
				accept_values	: (it.nb_inputs > 0),
				apply_to		: it.apply_to,
			])

			if (qbMap.queryBase?.lang?.operators?."${it.type}")
				newOperator.setLabel(qbMap.queryBase?.lang?.operators?."${it.type}")
						
			if (qbMap.queryBase?.mongoOperators?."${it.type}")
				newOperator.setMongo(qbMap.queryBase?.mongoOperators?."${it.type}")
				
			parsedOperatorsMap.put(newOperator.type, newOperator)
		}
		log.info("Parsed ${parsedOperatorsMap.size()} operators.")
		
		def numFilters = 0
		qbMap.queryBase.filters.each {
			
			def operators = []
			it.operators.each {  opType ->
				def op = parsedOperatorsMap[(opType)]
				if (!op) throw new RuntimeException("Could not find referenced operator type '${opType}'!")
				else operators << op
			}
			
			this.addFilter(new Filter([
				id			: it.id,
				field		: it.field,
				label		: it.label,
				description : it.description,
				type		: it.type,
				input		: it.input,
				multiple	: it.multiple,
				placeholder : it.placeholder,
				vertical	: it.vertical,
				operators   : operators,
				values		: it.values,
				plugin		: it.plugin,
				plugin_config : it.plugin_config,
				])
			)
			numFilters++
			
		}
		log.info("Parsed ${numFilters} filters.")
		
		log.info("QueryBase now contains ${filters.size()} filters with ${operators.size()} operators.")
		
		this
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
	
	Map flatten(Map params = [:]) {
		def ret = [:]
		
		putIfNotEmpty(ret,"sortable")
		putIfNotEmpty(ret,"conditions")
		putIfNotEmpty(ret,"defaultCondition")
		putIfNotEmpty(ret,"lang")
		putIfNotEmpty(ret,"plugins")
		putIfNotEmpty(ret,"allowEmpty")
		
		// handle plugins
		if (params.plugins) {
			if (!ret.plugins) ret.plugins = [:]
			if (params.plugins instanceof String) {
				ret.plugins.putAll(params.plugins.split(',').collectEntries { e-> [(e):null] })
			}
			else if (params.plugins instanceof Map) {
				ret.plugins.putAll(params.plugins)
			}
			else if (params.plugins instanceof Collection) {
				ret.plugins.putAll(params.plugins.collectEntries { e-> [(e):null] })
			}
			else {
				log.warn("Unable to parse data from 'plugins' parameter: ${params.plugins}")
			}
		}
		
		// handle filters
		def flatFilters = []
		this.filters.values().each {
			flatFilters << it.flatten(params)
		}
		
		ret.put('filters', flatFilters)
		
		// handle operators
		def flatOperators = []
		this.operators.values().each {
			flatOperators << it.flatten(params)
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
		
	public Javascript toJs(Map params = [:]) {
		def flatConfig = this.flatten(params)
		def js = new Javascript(flatConfig)
		js
	}
	
	public String toJsString(Boolean prettyPrint = false) {
		toJsString([:], prettyPrint)
	}
	public String toJsString(Map params, Boolean prettyPrint = false) {
		if (prettyPrint) {
			toJs(params).toString(prettyPrint)
		}
		else  {
			toJs(params).toString()
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
