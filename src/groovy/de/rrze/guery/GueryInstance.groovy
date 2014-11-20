package de.rrze.guery

import grails.converters.JSON
import de.rrze.guery.base.QueryBase
import de.rrze.guery.base.QueryBaseBuilder
import de.rrze.guery.converters.JavascriptCode
import de.rrze.guery.policy.Policy

class GueryInstance {

	String id
	String description

	QueryBase qb
	Map<String,Policy> policies = [:] 
	
	GueryInstance parent
	
	def GueryInstance(String instanceId) {
		id = instanceId
	}
	
	def GueryInstance(String instanceId, GueryInstance parentGi) {
		this(instanceId)
		addParent(parentGi)
	}
	
	def addParent(GueryInstance parentGi) {
		parent = parentGi
	}
	
	
	/*
	 * QUERY BASE
	 */
	@Deprecated
	QueryBase makeBase(Closure c) {
		buildBase(c)
	}
	
	QueryBase buildBase(Closure c) {
		if (parent) {
			qb = new QueryBaseBuilder().makeDelegate(parent.qb, c)
		}
		else {
			qb = new QueryBaseBuilder().make(c)
		}
		
		if (!qb.id) qb.id = "${id}_queryBase"
		if (!qb.description) qb.description = "QueryBase for ${id}"
		if (!qb.onValidationError) qb.onValidationError = new JavascriptCode("onValidationError_guery_builder_${id}")
		qb
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
	
	
	def reset() {
		resetQueryBase()
		resetPolicies()
	}
	
	def resetQueryBase() {
		qb = null
	}
	
	def resetPolicies() {
		policies = [:]
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
		policies.values().each { policy ->
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
		if (policies.containsKey(p.id)) throw new RuntimeException("Policy with id '${p.id}' already exists! Use putPolicy() method to add a new or update an existing policy id.")
		policies.put(p.id, p)
		this
	}
	
	GueryInstance putPolicy(Policy p) {
		if (!p.id) throw new RuntimeException("Cannot add policy without id!")
		policies.put(p.id, p)
		this
	}

	
	Policy getPolicy(String id) {
		policies.get(id)
	}
	
	
}
