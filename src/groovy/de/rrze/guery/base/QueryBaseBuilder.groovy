package de.rrze.guery.base

import de.rrze.guery.converters.JavascriptCode
import de.rrze.guery.operator.IOperationManager
import de.rrze.guery.operator.Operator

class QueryBaseBuilder {

	QueryBase qb
	
	def originalDelegate
	
	def QueryBaseBuilder() {}
	
	QueryBase makeDelegate(QueryBase parentQb, Closure c) {
		qb = new DelegatingQueryBase(parentQb)
		originalDelegate = c.delegate
		runClosure(c)
		qb
	}
	
	QueryBase make(Closure c) {
		qb = new QueryBase()
		originalDelegate = c.delegate
		runClosure(c)
		qb
	}

	
	def filter(Map m, Closure c) {
		def f = new Filter(m)
		log.trace(m)
		c.resolveStrategy = Closure.TO_SELF
		c.metaClass.methodMissing = { name, arguments ->
			def opSettings = [
				type			: f.id + '_' + name,
				label			: name,
					
				accept_values	: true,
				apply_to		: [f.type],
			] 
			
			if (arguments[0] in Map) {
				// overrides
				opSettings.putAll(arguments[0])
			}
			
			def op = new Operator(opSettings)
			op.qb = qb
			f.add(op)
			
			def operationClosure = arguments[-1]
			if (!(operationClosure in Closure)) {
				throw new RuntimeException("Last filter operator argument must be of type Closure!")
			}
			operationClosure.delegate = originalDelegate
			operationClosure.resolveStrategy = Closure.DELEGATE_ONLY
			qb.operationManager.put(op.type, operationClosure)
				
		}
		c()
		
		log.trace("Adding filter ${f.id} ...")
		qb.addFilter(f)
	}
	
	
	def lang(Map value) {
		qb._lang += value
	}
	
	def sortable(Boolean value) {
		if (value) plugin('sortable')
	}
	
	def filterDescription(Boolean value) {
		if (value)  plugin('filter-description')
	}
	
	def plugins(List<String> value) {
		qb._plugins = value as Set
	}
	
	def plugin(String value) {
		if (!qb._plugins) qb._plugins = [] as Set
		qb._plugins.add(value)
	}
	
	def allowEmpty(Boolean value) {
		qb._allowEmpty = value
	}
	
	def conditions(List<String> value) {
		qb._conditions = value
		if (!qb._defaultCondition) defaultCondition(value.get(0))
	}

	def defaultCondition(String value) {
		qb._defaultCondition = value
	}
			
	def operationManager(IOperationManager opm) {
		qb.operationManager = opm
	}

	def id(String value) {
		qb.id = value
	}

	def description(String value) {
		qb.description = value
	}
	
	def propertyMissing(String name, Object value) {
		if (name == 'sortable') sortable(value)
		else if (name == 'plugins') plugins(value)
		else if (name == 'conditions') conditions(value)
		else if (name == 'defaultCondition') defaultCondition(value)
		else if (name == 'id') id(value)
		else if (name == 'description') description(value)
		else if (name == 'lang') lang(value)
		else if (name == 'allowEmpty') allowEmpty(value)
		else throw new MissingPropertyException(name, this.class)
	}
	
	
	
	
	
	private runClosure(Closure runClosure) {
		Closure runClone = runClosure.clone()
		runClone.delegate = this
		runClone.resolveStrategy = Closure.DELEGATE_ONLY
		runClone()
	}
}
