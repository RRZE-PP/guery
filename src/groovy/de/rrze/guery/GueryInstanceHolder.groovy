package de.rrze.guery

class GueryInstanceHolder {

	static Map<String,GueryInstance> registry = [:]
	
	
	static GueryInstance putGueryInstance(GueryInstance gueryInstance) {
		registry.put(gueryInstance.id, gueryInstance)
	}
	
	static GueryInstance getGueryInstance(String instanceId) {
		registry.get(instanceId)
	}

}
