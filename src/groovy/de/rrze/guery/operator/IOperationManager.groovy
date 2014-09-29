package de.rrze.guery.operator

import java.util.concurrent.Callable

interface IOperationManager {

	abstract Object apply(String id, Object val, Object obj)
	
	abstract Callable get(String id)
	
	abstract Object put(String id, Callable op)
	
}
