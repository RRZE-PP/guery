package de.rrze.guery.operator

import java.util.concurrent.Callable

class ClosureOperationManager implements IOperationManager {

	Map<String, Closure> operations = [:]
	
	
	@Override
	public Object apply(String id, Object val, Map req, Map res) {
		Closure c = get(id) 
		def argc = c.maximumNumberOfParameters
		
		if (argc == 1) c.call(val)
		else if (argc == 2) c.call(val, req)
		else if (argc == 3) c.call(val, req, res)
		else c.call(val, req, res, id)
		
	}

	public Object apply(String id, Map req, Map res) {
		Closure c = get(id)
		def argc = c.maximumNumberOfParameters
		
		if (argc == 1) c.call(req)
		else if (argc == 2) c.call(req, res)
		else c.call(req, res, id)
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
