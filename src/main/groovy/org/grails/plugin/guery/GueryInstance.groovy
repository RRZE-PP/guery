package org.grails.plugin.guery

import org.grails.plugin.guery.base.Filter
import org.grails.plugin.guery.base.QueryBase
import org.grails.plugin.guery.base.QueryBaseBuilder
import org.grails.plugin.guery.policy.Policy
import grails.converters.JSON
import groovy.util.logging.Log4j

import java.util.concurrent.locks.ReentrantReadWriteLock

@Log4j
class GueryInstance {

	volatile String id
	volatile String description

	volatile QueryBase qb
	
	final Map<String,Policy> policyMap = [:] 
	final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true)
		
	final GueryInstance parent

    Closure requestPreprocessor = null

    def stats = [
            count : 0,
            avgTime: 0,
            maxTime: 0,
            minTime: 0,
    ]

	def GueryInstance(String instanceId) {
		this.id = instanceId
	}
	
	def GueryInstance(String instanceId, QueryBase qb) {
		this(instanceId)
		if (qb) this.qb = qb
	}
	
	def GueryInstance(String instanceId, GueryInstance parentGi) {
		this(instanceId)
		if (parentGi) {
            this.parent = parentGi
            this.requestPreprocessor = parentGi.requestPreprocessor
        }
	}
	
	def GueryInstance(String instanceId, QueryBase qb, GueryInstance parentGi) {
		this(instanceId, parentGi)
		if (qb) this.qb = qb
	}

    /*
     * GUERY INSTANCE
     */

    protected void updateStats(timeMs) {
        if (timeMs > stats.maxTime) stats.maxTime = timeMs
        if (timeMs < stats.minTime) stats.minTime = timeMs

        // travelling mean (see https://math.stackexchange.com/a/106720)
        stats.avgTime = stats.avgTime + ((timeMs - stats.avgTime) / stats.count)
        stats.count++
    }

    GueryInstance set(Closure c) {
        this.with(c)
        this
    }


	/*
	 * QUERY BASE
	 */
	@Deprecated
	GueryInstance makeBase(Closure c) {
		buildBase(c)
	}
	
	GueryInstance buildBase(Closure c) {
		if (parent) {
			qb = new QueryBaseBuilder().makeDelegate(parent.qb, c)
		}
		else {
			qb = new QueryBaseBuilder().make(c)
		}
		
		if (!qb.id) qb.id = "${id}_queryBase"
		if (!qb.description) qb.description = "QueryBase for ${id}"
        // TODO implement interface for catching validation errors
//		if (!qb.onValidationError) qb.onValidationError = new JavascriptCode("onValidationError_guery_builder_${id}")
		this
	}
	
	
	def getOperator(String type) {
		qb.getOperator(type)
	}
	
	def getExposedData(String id) {
		qb.getExposedData(id)
	}
	
	JSON baseToJs() {
		qb.toJs()
	}

	String baseToJsString(Boolean prettyPrint = false) {
		baseToJsString([:],prettyPrint)
	}
	String baseToJsString(Map params, Boolean prettyPrint = false) {
		qb.toJsString(params, prettyPrint)
	}

	
	QueryBase getQueryBase() {
		qb
	}
	
	Boolean hasQueryBase() {
		(qb != null)
	}
	
	Boolean hasFilters() {
		getQueryBase()?.getFilters() as Boolean
	}
	
	Map<String,Filter> getFilters() {
		getQueryBase()?.getFilters()
	}
	
	Boolean hasOperators() {
		getQueryBase()?.getOperators() as Boolean
	}
	
	def reset() {
		resetQueryBase()
		resetPolicies()
	}
	
	def resetQueryBase() {
		qb = null
	}
	
	def resetPolicies() {
		rwl.writeLock().lock()
		try {
			policyMap.clear()
		}
		finally {
			rwl.writeLock().unlock()
		}
	}
	
	/*
	 * PROCESSING
	 */

    Map preprocessRequest(Map req) {
        def immutableRequest = req.asImmutable()
        if (requestPreprocessor) {
            log.trace("Calling requestPreprocessor Closure ...")
            def preprocessedRequest = requestPreprocessor(this,immutableRequest)
            if (!preprocessedRequest in Map) throw new RuntimeException("Request preprocessor Closure did not return an instance of Map!")
            immutableRequest = ((Map)preprocessedRequest).asImmutable()
        }
        immutableRequest
    }

	Object evaluate(Map req, Closure c) {
		def results = evaluate(req)
		c(results)
	}
	
	Object evaluateEach(Map req, Closure c) {
		def results = evaluate(req)
		results.each { result -> c(result)}
	}

	Object evaluateEachMatch(Map req, Closure c) {
		def results = evaluate(req)
		results.findAll { it.decision == true }.each { result -> c(result)}
	}

	
	Object evaluate(Map req) {
        def startTime = System.currentTimeMillis()

		def immutableRequest = preprocessRequest(req)
		def results = []
		getPolicies().each { policy ->
			def result =  policy.evaluate(immutableRequest) // result = [descision:xxx, status:xxx, obligations:xxx]
			results << result
		}

        def stopTime = System.currentTimeMillis()
        updateStats(stopTime-startTime)

		return results
	}


    Object evaluateOne(Policy policy, Map req) {
        def immutableRequest = preprocessRequest(req)
        policy.evaluate(immutableRequest) // result = [descision:xxx, status:xxx, obligations:xxx]
    }

    Object evaluateOne(String policyId, Map req) {
        def policy = getPolicy(policyId)
        if (!policy) throw new RuntimeException("Could not find policy '${id}'/'${policyId}'")
        evaluateOne(policy, req)
    }
	
	
	/*
	 * POLICIES
	 */
	Policy parsePolicyFromJson(String queryBuilderResult, Closure mods = null) {
		def p = new Policy(qb, queryBuilderResult)
		if (mods) { mods(p) }
		p
	}
	
	Policy addPolicyFromJson(String id, String queryBuilderResult, Closure mods = null) {
		def p = parsePolicyFromJson(queryBuilderResult, mods)
		p.id = id
		addPolicy(p)
		p
	}
	
	Policy putPolicyFromJson(String id, String queryBuilderResult, Closure mods = null) {
		def p = parsePolicyFromJson(queryBuilderResult, mods)
		p.id = id
		putPolicy(p)
		p
	}
	
	Policy parsePolicyFromMap(Map ruleMap, Closure mods = null) {
		def p = new Policy(qb, ruleMap)
		if (mods) { mods(p) }
		p
	}
	
	Policy addPolicyFromMap(String id, Map ruleMap, Closure mods = null) {
		def p = parsePolicyFromMap(ruleMap, mods)
		p.id = id
		addPolicy(p)
		p
	}
	
	Policy putPolicyFromMap(String id, Map ruleMap, Closure mods = null) {
		def p = parsePolicyFromMap(ruleMap, mods)
		p.id = id
		putPolicy(p)
		p
	}
	
	GueryInstance addPolicy(Policy p) {
		if (!p.id) throw new RuntimeException("Cannot add policy without id!")
		rwl.writeLock().lock()
		try {
			if (policyMap.get(p.id)) throw new RuntimeException("Policy with id '${p.id}' already exists! Use putPolicy() method to add a new or update an existing policy id.")
			policyMap.put(p.id, p)
		}
		catch(e) {
			throw e
		}
		finally {
			rwl.writeLock().unlock()
		}
		
		this
	}
	
	GueryInstance putPolicy(Policy p) {
		if (!p.id) throw new RuntimeException("Cannot add policy without id!")
		rwl.writeLock().lock()
		try {
			policyMap.put(p.id, p)
		}
		catch(e) {
			throw e
		}
		finally {
			rwl.writeLock().unlock()
		}

		this
	}

	GueryInstance removePolicy(String policyId) {
		if (!policyId) throw new RuntimeException("Cannot remove policy without id!")
		
		// ignore gracefully
		//if (!policies.containsKey(policyId)) throw new RuntimeException("No policy with id '${policyId}' exists!")
		
		rwl.writeLock().lock()
		try {
			policyMap.remove(policyId)
		}
		catch(e) {
			throw e
		}
		finally {
			rwl.writeLock().unlock()
		}

		this
	}
	
	GueryInstance removePolicy(Policy p) {
		if (!p.id) throw new RuntimeException("Cannot remove policy without id!")
		removePolicy(p.id)
	}
	
	Policy getPolicy(String id) {
		rwl.readLock().lock()
		try {
			return policyMap.get(id)
		}
		catch(e) {
			throw e
		}
		finally {
			rwl.readLock().unlock()
		}
	}
	
	List<Policy> getPolicies() {
		def p = []
		rwl.readLock().lock()
		try {
			p.addAll(this.policyMap.values())
		}
		catch(e) {
			throw e
		}
		finally {
			rwl.readLock().unlock()
		}
		
		p
	}
	
}
