package de.rrze.guery

import groovy.util.logging.Log4j

import java.util.concurrent.locks.ReentrantReadWriteLock

@Log4j
class GueryInstanceHolder {

	static final Map<String,GueryInstance> registry = [:]
	static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock()
	
	static GueryInstance putInstance(GueryInstance gueryInstance) {
		put(gueryInstance)
	}
	
	static put(GueryInstance gueryInstance) {
		rwl.writeLock().lock()
		try {
			if (log.isDebugEnabled()) log.debug("Putting GueryInstance with id '${gueryInstance.id}' ...")
			return registry.put(gueryInstance.id, gueryInstance)
		}
		catch(e) { throw e }
		finally { rwl.writeLock().unlock() }
		
	}

	static removeInstance(GueryInstance gueryInstance) {
		remove(gueryInstance)
	}
	
	
	static remove(GueryInstance gueryInstance) {
		rwl.writeLock().lock()
		try {
			if (log.isDebugEnabled()) log.debug("Removing GueryInstance with id '${gueryInstance.id}' ...")
			return registry.remove(gueryInstance.id)
		}
		catch(e) { throw e }
		finally { rwl.readLock().unlock() }
	}

		
	static GueryInstance getInstance(String instanceId) {
		get(instanceId)
	}
	
	static get(String instanceId) {
		rwl.readLock().lock()
		try {
			return registry.get(instanceId)		
		}
		catch(e) { throw e }
		finally { rwl.readLock().unlock() }
	}

	static GueryInstance getOrCreateInstance(String instanceId) {
		def instance = GueryInstanceHolder.getInstance(instanceId)
		if (!instance) {
			if (log.isDebugEnabled()) log.debug("No instance with id '${instanceId}' -- creating new one.")
			instance = new GueryInstance(instanceId)
			putInstance(instance)
		}
		instance
	}
	
	static GueryInstance replaceOrCreateInstance(String instanceId, GueryInstance parentGi = null) {
		def instance = GueryInstanceHolder.getInstance(instanceId)
		if (!instance) {
			if (log.isDebugEnabled()) log.debug("No instance with id '${instanceId}' -- creating new one.")
			
		}
		else {
			if (log.isDebugEnabled()) log.debug("Found existing instance with id '${instanceId}' -- replacing existing instance.")
		}
		instance = new GueryInstance(instanceId, parentGi)
		putInstance(instance)
		
		instance
	}

	
	static getInstances() {
		rwl.readLock().lock()
		try {
			return registry.values()
		}
		catch(e) { throw e }
		finally { rwl.readLock().unlock() }
	}
	
	static void reset() {
		rwl.writeLock().lock()
		try {
			if (log.isDebugEnabled()) log.debug("Removing all guery instances from registry ...")
			registry.clear()
		}
		catch(e) { throw e }
		finally { rwl.writeLock().unlock() }
	}
}
