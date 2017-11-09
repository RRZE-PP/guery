package org.grails.plugin.guery.operator

interface IOperationManager {

	abstract Object apply(String id, Object val, Map req, Map res)

	abstract Object apply(String id, Map req, Map res)
	
	abstract Object get(String id)
	
	abstract Object put(String id, Object op)
	
}
