package org.grails.plugin.guery.policy

import org.grails.plugin.guery.base.QueryBase
import groovy.util.logging.Log4j

@Log4j
class Policy {

	volatile String id
	volatile String description 
	
	final QueryBase qb
	final RuleSet rs

    def stats = [
            count : 0,
            avgTime: 0,
            maxTime: 0,
            minTime: 0,
    ]

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

    protected void updateStats(timeMs) {
        if (timeMs > stats.maxTime) stats.maxTime = timeMs
        if (timeMs < stats.minTime) stats.minTime = timeMs

        // travelling mean (see https://math.stackexchange.com/a/106720)
        stats.avgTime = stats.avgTime + ((timeMs - stats.avgTime) / stats.count)
        stats.count++
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
        def startTime = System.currentTimeMillis()

		def immutableRequest = req.asImmutable()
		def result = rs.evaluate(immutableRequest)
		result.put('id', this.id)

        def stopTime = System.currentTimeMillis()
        updateStats(stopTime-startTime)

		return result
	}
}
