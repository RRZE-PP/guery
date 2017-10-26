package de.rrze.guery.operator

import groovy.util.logging.Log4j
import org.slf4j.LoggerFactory

@Log4j
class ClosureOperationManager implements IOperationManager {

    static log = LoggerFactory.getLogger(ClosureOperationManager.class)

	protected Map<String, Closure> _operations = [:]
	
	def ClosureOperationManager() {}
	
	@Override
	public Object apply(String id, Object val, Map req, Map res) {
		Closure c = get(id)
		def argc = c.maximumNumberOfParameters
		
		if (argc == 1) c.call(req) //c.call([id:id, val:val, req:req, res:res])
		else if (argc == 2) c.call(val, req)
		else if (argc == 3) c.call(val, req, res)
		else c.call(val, req, res, id)
		
	}

	public Object apply(String id, Map req, Map res) {
		Closure c = get(id)
		def argc = c.maximumNumberOfParameters
		
		if (argc == 1) c.call(req) //c.call([id:id, val:null, req:req, res:res])
		else if (argc == 2) c.call(req, res)
		else c.call(req, res, id)
	}
	
	@Override
	public Closure get(String id) {
		def op = _operations.get(id)
		op
	}

	@Override
	public Object put(String id, Object op) {
		if (!(op in Closure)) throw new RuntimeException("ClosureOperationManager can only handle Closure type operations: ${op.class}")
		_operations.put(id, op) // will override possible parent definition
	}

}
