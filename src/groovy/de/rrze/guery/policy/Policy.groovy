package de.rrze.guery.policy

import de.rrze.guery.base.QueryBase;

class Policy {

	String id
	String description 
	
	QueryBase qb
	RuleSet rs
	
	def Policy(QueryBase queryBase) {
		qb = queryBase
	}
	
	def Policy(QueryBase queryBase, String queryBuilderResult) {
		qb = queryBase
		rs = new RuleSet(qb, queryBuilderResult)
	}
	
	/**
	 * Checks, if the ruleset contained in this policy is satisfied
	 * 
	 * @param obj
	 * @return
	 * 		the provided Closure's return value or false if the Closure is not applied
	 */
	Object evaluate(Map req, Closure c) {
		def res = rs.evaluate(req)
		if (res.decision == true) {
			return c(res)
		}
		else {
			return false
		}
	}
	
	Object evaluate(Map req) {
		rs.evaluate(req)
	}
}
