package de.rrze.guery.base

import java.util.List;

import de.rrze.guery.operator.IOperationManager;
import de.rrze.guery.operator.Operator;

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
		c.resolveStrategy = Closure.TO_SELF
		c.metaClass.methodMissing = { name, arguments ->
//			log.warn(name)
//			log.warn(arguments)
//			def ucFirstName = name[0].toUpperCase() + name[1..-1]
			
			def opSettings = [
				//type			: f.id + ucFirstName,
				type			: f.id + '_' + name,
				label			: name,
					
				accept_values	: true, // FIXME not always true
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
		
		log.info("Adding filter ${f.id} ...")
		qb.addFilter(f)
	}
	
	
	def lang(Map value) {
		qb._lang += value
	}
	
	def sortable(Boolean value) {
		qb._sortable = value
	}
	
	def conditions(List<String> value) {
		qb._conditions = value
		if (!qb._defaultCondition) defaultCondition(value.get(0))
	}

	def defaultCondition(String value) {
		qb._defaultCondition = value
	}
			
	def readonlyBehaviour(Map<String,Boolean> value) {
		qb.readonlyBehaviour = value
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
	

//	def methodMissing(String name, arguments) {
//		
////		if (name in ['sortable']) {
////			// QueryBase options
////			qb."${name}" = arguments[0]
////		}
//		
//		
////		if (name in ['to', 'from']) {
////			def airport = arguments[0].split(',')
////			def airPortname = airport[0].trim()
////			def city = airport[1].trim()
////			reservation.flight."$name" = new Airport(name: airPortname, city: city)
////		}
//	}

	def propertyMissing(String name, Object value) {
		if (name == 'sortable') sortable(value)
		else if (name == 'conditions') conditions(value)
		else if (name == 'defaultCondition') defaultCondition(value)
		else if (name == 'id') id(value)
		else if (name == 'description') description(value)
		else if (name == 'lang') lang(value)
		else if (name == 'readonlyBehaviour') readonlyBehaviour(value)
		else throw new MissingPropertyException(name, this.class)
	}
	
	
	
	
	
	private runClosure(Closure runClosure) {
		// Create clone of closure for threading access.
		Closure runClone = runClosure.clone()

		// Set delegate of closure to this builder.
		runClone.delegate = this

		// And only use this builder as the closure delegate.
		runClone.resolveStrategy = Closure.DELEGATE_ONLY

		// Run closure code.
		runClone()
	}
}
