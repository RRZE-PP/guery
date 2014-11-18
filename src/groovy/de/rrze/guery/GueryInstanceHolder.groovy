package de.rrze.guery

import org.apache.log4j.Logger

class GueryInstanceHolder {

	
	static log = Logger.getLogger(GueryInstanceHolder.class)
	
	static Map<String,GueryInstance> registry = [:]
	
	
	static GueryInstance putInstance(GueryInstance gueryInstance) {
		registry.put(gueryInstance.id, gueryInstance)
	}
	
	static put(GueryInstance gueryInstance) {
		registry.put(gueryInstance.id, gueryInstance)
	}
	
	static GueryInstance getInstance(String instanceId) {
		registry.get(instanceId)
	}
	
	static get(String instanceId) {
		registry.get(instanceId)
	}

	static GueryInstance getOrCreateInstance(String instanceId) {
		def instance = GueryInstanceHolder.getInstance(instanceId)
		if (!instance) {
			log.info("No instance with id '${instanceId}' -- creating new one.")
			instance = new GueryInstance(instanceId)
			GueryInstanceHolder.putInstance(instance)
		}
		else {
			log.debug("Found exisiting instance with id '${instanceId}'.")
		}
		
		instance
	}

	
	static getInstances() {
		registry.values()
	}
	
	static void reset() {
		registry.clear()
	}
}
