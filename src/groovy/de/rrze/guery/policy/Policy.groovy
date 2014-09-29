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
	 * 		true if the ruleset is satisfied, false otherwise
	 */
	Boolean evaluate(Object obj, Closure c = {}) {
		if (rs.evaluate(obj)) {
			c()
			return true
		}
		else {
			return false
		}
	}
	
}
