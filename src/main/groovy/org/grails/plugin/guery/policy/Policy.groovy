package org.grails.plugin.guery.policy

import org.grails.plugin.guery.base.QueryBase
import groovy.util.logging.Log4j

@Log4j
class Policy {

	volatile String id
	volatile String description 
	
	final QueryBase qb
	final RuleSet rs
	
	def Policy(QueryBase queryBase) {
		qb = queryBase
	}
	
	def Policy(QueryBase queryBase, String queryBuilderResult) {
		qb = queryBase
		rs = new RuleSet(qb, queryBuilderResult)
	}
	
	def Policy(QueryBase queryBase, Map ruleMap) {
		qb = queryBase
		rs = new RuleSet(qb, ruleMap)
	}

	def Policy(QueryBase queryBase, RuleSet ruleSet) {
		qb = queryBase
		ruleSet.qb = this.qb
		rs = ruleSet
	}
		
	Map toRuleMap() {
		rs.toRuleMap()
	}
	
	String toJSON() {
		rs.toJSON()
	}
	
	def getRuleSet() {
		this.rs
	}
	
	def getQueryBase() {
		this.qb
	}
	
	Boolean isEmpty() {
		this.rs.isEmpty()
	}
	
	
	
//	Map toMongoFilter() {
//			
//	}
	
	
	/**
	 * Checks, if the ruleset contained in this policy is satisfied
	 * 
	 * @param obj
	 * @return
	 * 		the provided Closure's return value or false if the Closure is not applied
	 */
	Object evaluate(Map req, Closure c) {
		def res = evaluate(req)
		if (res.decision == true) {
			return c(res)
		}
		else {
			return false
		}
	}
	
	Object evaluate(Map req) {
		def immutableRequest = req.asImmutable()
		def result = rs.evaluate(immutableRequest)
		result.put('id', this.id)
		return result
	}
}
