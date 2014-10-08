package de.rrze.guery

class GueryInstanceHolder {

	static Map<String,GueryInstance> registry = [:]
	
	
	static GueryInstance putInstance(GueryInstance gueryInstance) {
		registry.put(gueryInstance.id, gueryInstance)
	}
	
	static GueryInstance getInstance(String instanceId) {
		registry.get(instanceId)
	}

	static GueryInstance getOrCreateInstance(String instanceId) {
		def instance = GueryInstanceHolder.getInstance(instanceId)
		if (!instance) instance = new GueryInstance(instanceId)
		instance
	}

}
