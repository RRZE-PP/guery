package org.grails.plugin.guery.policy

import org.grails.plugin.guery.Level
import org.grails.plugin.guery.base.QueryBase
import groovy.util.logging.Log4j
import org.grails.plugin.guery.operator.Operator

@Log4j
class Policy {

	volatile String id
    volatile String name
	volatile String description 
	
	final QueryBase qb
	final RuleSet rs

    def stats = [
            last : null,
            count : 0,
            avgTime: 0,
            maxTime: 0,
            minTime: Long.MAX_VALUE,
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

    /*
     * RULESET
     */
    def getRuleSet() {
        this.rs
    }

	Map toRuleMap() {
		rs.toRuleMap()
	}
	
	String toJSON() {
		rs.toJSON()
	}

    Boolean isEmpty() {
        this.rs.isEmpty()
    }

    /**
     * Mark everything as readonly
     *
     * @param sw Can be set to false to remove a previous mark as readonly
     * @return
     */
    def readonly(Boolean sw = null) {
        this.rs.readonly(sw)
        this
    }

    /**
     * Marks all rulesets as readonly. Rules are not touched.
     * @param sw Can be set to false to remove a previous mark as readonly
     * @return
     */
    def readonlyStructure(Boolean sw = null) {
        this.rs.readonlyStructure(sw)
        this
    }

    /**
     * Marks all rules that use a filter matching one of the specified filterIds as readonly.
     * @param filterIds Filters to look for
     * @param sw Can be set to false to remove a previous mark as readonly
     * @return
     */
    def readonlyRulesByFilterId(Collection <String> filterIds, Boolean sw = null) {
        this.rs.readonlyRulesByFilterId(filterNames, sw)
        this
    }

    Collection<Rule> findAllByFilterId(String filterId) {
        findAllByFilterIds([filterId])
    }

    Collection<Rule> findAllByFilterIds(Collection<String> filterIds) {
        def result = []
        this.rs.findAllByFilterIds(filterIds, result)
        result
    }

    /*
     * QUERY BASE
     */
	def getQueryBase() {
		this.qb
	}

    Map<String, Operator> getOperators() {
        this.qb.getOperators()
    }

    Collection<String> getOperatorIds() {
        this.qb.getOperatorIds()
    }

	
	
//	Map toMongoFilter() {
// TODO implement this for backend mongo queries
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
        wrapper.ref = this
        wrapper.time = new Date()
        wrapper.duration = data.duration
        if (this.stats.last) wrapper.stats = this.stats.clone()
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

        if (Level.POLICY.matches(req?.opts?.statsLevel)) updateStats(duration)
        if (Level.POLICY.matches(req?.opts?.auditLevel)) updateAudit([duration:duration, results:childrenRet], ret)

		return ret
	}
}
