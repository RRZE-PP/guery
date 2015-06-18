package de.rrze.guery

import grails.converters.JSON

import java.util.concurrent.locks.ReentrantReadWriteLock

import de.rrze.guery.base.DelegatingQueryBase
import de.rrze.guery.base.Filter
import de.rrze.guery.base.QueryBase
import de.rrze.guery.base.QueryBaseBuilder
import de.rrze.guery.policy.Policy

class GueryInstance {

	volatile String id
	volatile String description

	volatile QueryBase qb
	
	final Map<String,Policy> policyMap = [:] 
	final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true)
		
	final GueryInstance parent
	
	def GueryInstance(String instanceId) {
		id = instanceId
	}
	
	def GueryInstance(String instanceId, GueryInstance parentGi) {
		this(instanceId)
		if (parentGi) setParent(parentGi)
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
//		if (!qb.onValidationError) qb.onValidationError = new JavascriptCode("onValidationError_guery_builder_${id}")
		this
	}
	
	def getOperator(String type) {
		qb.getOperator(type)
	}
	
	JSON baseToJs() {
		qb.toJs()
	}

	String baseToJsString(Boolean prettyPrint) {
		qb.toJsString(prettyPrint)
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
		def immutableRequest = req.asImmutable()
		def results = []
		getPolicies().each { policy ->
			def result =  policy.evaluate(immutableRequest) // result = [descision:xxx, status:xxx, obligations:xxx]
			result.put('id', policy.id)
			results << result
		}
		return results
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
