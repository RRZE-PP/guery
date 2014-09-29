package de.rrze.guery.operator

import java.util.concurrent.Callable

class ClosureOperationManager implements IOperationManager {

	Map<String, Closure> operations = [:]
	
	
	@Override
	public Object apply(String id, Object val, Object obj) {
		Closure c = get(id) 
		def argc = c.maximumNumberOfParameters
		
		if (argc == 1) c.call(val)
		else if (argc == 2) c.call(val, obj)
		else c.call(val, obj, id)
		
	}

	@Override
	public Closure get(String id) {
		operations.get(id)
	}

	@Override
	public Object put(String id, Callable op) {
		if (!(op in Closure)) throw new RuntimeException("ClosureOperationManager can only handle Closure type operations: ${op.class}")
		operations.put(id, op)
	}

}
