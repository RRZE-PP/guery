package org.grails.plugin.guery.policy

import org.grails.plugin.guery.Level
import org.grails.plugin.guery.base.QueryBase
import groovy.util.logging.Log4j

@Log4j
class Policy {

	volatile String id
	volatile String description 
	
	final QueryBase qb
	final RuleSet rs

    def stats = [
            last : null,
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

    protected void updateStats(duration) {
        this.stats.last = new Date()

        if (duration > stats.maxTime) stats.maxTime = duration
        if (duration < stats.minTime) stats.minTime = duration

        // travelling mean (see https://math.stackexchange.com/a/106720)
        stats.count++
        stats.avgTime = stats.avgTime + ((duration - stats.avgTime) / stats.count)
    }

    protected updateAudit(data, dest) {
        def wrapper = [:]
        wrapper.type = 'Policy'
        wrapper.time = new Date()
        wrapper.duration = data.duration
        if (this.stats.last) wrapper.stats = this.stats.clone()
        wrapper.ref = this
        wrapper.children = data?.results?.collect{ it.audit }

        dest.audit = wrapper
    }

	Object evaluate(Map req) {
        def startTime = System.currentTimeMillis()

		def immutableRequest = req.asImmutable()
        def childRet = rs.evaluate(immutableRequest)
        def childrenRet = [childRet]

        // ret = [id:xxx, decision:xxx, status:xxx, obligations:xxx, audit:xxx]
        def response = childRet.response
        def ret = [
                id          : this.id,
                decision    : response.decision,
                status      : response.status,
                obligations : response.obligations,
        ]


        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (req?.opts?.statsLevel != null && req.opts.statsLevel.value >= Level.POLICY.value) updateStats(duration)
        if (req?.opts?.auditLevel != null && req.opts.auditLevel.value >= Level.POLICY.value) updateAudit([duration:duration, results:childrenRet], ret)

		return ret
	}
}
