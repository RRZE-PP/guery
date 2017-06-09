package de.rrze.guery.operator

class DelegatingClosureOperationManager extends ClosureOperationManager {

	ClosureOperationManager parent
	
	def DelegatingClosureOperationManager() {
		super()
	}
	
	def DelegatingClosureOperationManager(ClosureOperationManager parentCom) {
		super()
		parent = parentCom
	}
	
	@Override
	public Closure get(String id) {
		def op = _operations.get(id)
		if (parent && !op) {
			op = parent.get(id)
		}
		op
	}

	@Override
	public Object put(String id, Object op) {
		if (!(op in Closure)) throw new RuntimeException("ClosureOperationManager can only handle Closure type operations: ${op.class}")
		if (parent && parent.get(id)) {
			if (log.isWarnEnabled()) log.warn("Parent already defines operation with id '${id}' -- will be overridden!")
		}
		_operations.put(id, op) // will override possible parent definition
	}

}
