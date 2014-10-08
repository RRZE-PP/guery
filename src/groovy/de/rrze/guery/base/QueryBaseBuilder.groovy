package de.rrze.guery.base

import de.rrze.guery.operator.IOperationManager;
import de.rrze.guery.operator.Operator;

class QueryBaseBuilder {

	QueryBase qb
	
	def QueryBaseBuilder() {}
	
	QueryBase makeDelegate(QueryBase parentQb, Closure c) {
		qb = new DelegatingQueryBase(parentQb)
		runClosure(c)
		qb
	}
	
	QueryBase make(Closure c) {
		qb = new QueryBase()
		runClosure(c)
		qb
	}

	
	def filter(Map m, Closure c) {
		def f = new Filter(m)
		c.resolveStrategy = Closure.TO_SELF
		c.metaClass.methodMissing = { name, arguments ->
			def ucFirstName = name[0].toUpperCase() + name[1..-1]
			
			def opSettings = [
				type			: f.id + ucFirstName,
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
			qb.operationManager.put(op.type, operationClosure)
				
		}
		c()
		
		log.info("Adding filter ${f.id} ...")
		qb.addFilter(f)
	}
	
	def sortable(Boolean value) {
		qb._sortable = value
	}
		
	def operationManager(IOperationManager opm) {
		qb.operationManager = opm
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

//	def propertyMissing(String name) {
//		if (name == 'retourFlight') {
//			reservation.retourFlight = true
//		}
//	}
	
	
	
	
	
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
