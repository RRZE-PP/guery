package de.rrze.guery.operator

interface IOperationManager {

	abstract Object apply(String id, Object val, Object obj)
	
	abstract Object get(String id)
	
	abstract Object put(String id, Object op)
	
}
