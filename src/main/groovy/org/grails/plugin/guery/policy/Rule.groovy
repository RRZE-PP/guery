package org.grails.plugin.guery.policy

import org.grails.plugin.guery.Level
import org.grails.plugin.guery.base.Filter
import org.grails.plugin.guery.operator.Operator
import org.grails.plugin.guery.base.QueryBase
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class Rule implements IEvaluateable {

	QueryBase qb
	
	Filter filter
	Operator operator
	Object 		val
	
	Set<String> tags = []
	Boolean		readonly = false

    def stats = [
            last : null,
            count : 0,
            avgTime: 0,
            maxTime: 0,
            minTime: 0,
    ]

    Level statsLevel = Level.ALL
    Level auditLevel = Level.OFF

	def Rule(QueryBase qb, Map qm) {
		this.qb = qb
		parseRuleMap(qm)
	}
	
	def Rule(QueryBase qb, String operatorType, Object val) {
		this.qb = qb
		this.operator = this.qb.getOperator(operatorType)
		this.filter = this.operator.filter
		this.val = this.operator.mapper(val)
	}

	def Rule(Operator operator, Object val) {
		this.qb = operator.qb
		this.operator = operator
		this.filter = this.operator.filter
		this.val = this.operator.mapper(val)
	}
	
	def parseRuleMap(Map qm) {
		this.filter = qb.filters?.get(qm.id)
		if (!this.filter) throw new RuntimeException("Could not resolve filter id in given query base '${qb.id}': ${qm.id}")
		
		this.operator = qb.operators?.get(qm.operator)
		if (!this.operator) throw new RuntimeException("Could not resolve operator in given query base '${qb.id}': ${qm.operator}")
		
		if (qm.tags) this.tags = qm.tags
		if (qm.readonly) this.readonly = Boolean.parseBoolean(qm.readonly.toString())
		
		this.val = this.operator.mapper(qm.value)
	}
	
	Map toRuleMap() {
		def map = [:]
		if (filter.id) map.id = filter.id
		if (filter.field) map.field = filter.field
		if (filter.type) map.type = filter.type
		if (filter.input) map.input = filter.input
		if (operator.type) map.operator = operator.type
		if (val) map.value = val
		if (readonly != null) map.readonly = readonly
				
		if (tags) map.tags = tags
	
		map
	}
	
	def toJSON(Boolean convert=true) {
		def map = [data:[:]]
		if (filter.id) map.id = filter.id
		if (filter.field) map.field = filter.field
		if (filter.type) map.type = filter.type
		if (filter.input) map.input = filter.input
		if (operator.type) map.operator = operator.type
		if (val) map.value = val
		if (readonly != null) map.readonly = readonly
				
		if (tags) map.data.tags = tags?.join(';')
	
		return convert?(map as JSON):map
	}


    protected void updateStats(timeMs) {
        this.stats.last = new Date()

        if (timeMs > stats.maxTime) stats.maxTime = timeMs
        if (timeMs < stats.minTime) stats.minTime = timeMs

        // travelling mean (see https://math.stackexchange.com/a/106720)
        stats.count++
        stats.avgTime = stats.avgTime + ((timeMs - stats.avgTime) / stats.count)
    }

    protected updateAudit(data, dest) {
        def wrapper = [:]
        wrapper.type = 'Rule'
        wrapper.time = new Date()
        wrapper.duration = data.duration
        if (this.stats.last) wrapper.stats = this.stats.clone()
        wrapper.ref = this
        wrapper.children = data?.results?.collect{ it.audit }

        dest.audit = wrapper
    }

	private _evaluate(Map req, Map res) {
       this.operator.apply(val, req, res)
	}

    Object evaluate(Map req, Map res) {
        def startTime = System.currentTimeMillis()

        def opRet = _evaluate(val, req, res)

        // the response does not reflect the calculated opRet, yet!
        def ret = [result:opRet, response:res]

        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (req?.opts?.statsLevel != null && req.opts.statsLevel.value >= Level.RULE.value) updateStats(duration)
        if (req?.opts?.auditLevel != null && req.opts.auditLevel.value >= Level.RULE.value) updateAudit([duration:duration, results:[opRet]], ret)

        return ret
    }

	Object evaluateAnd(Map req, Map res) {
        def startTime = System.currentTimeMillis()

		def opRet = _evaluate(req, res)
        def opRes = opRet.result
		
		if (!opRes.is(res)) { // not same object
			
			if (opRes) { // groovy truth
				if (opRes.is(true)) {
					// nothing to do here
				}
				else {
					if (!(opRes in Collection)) {
						opRes = [opRes] as Set
					}
					
					def acc = res.status.get(filter.field) as Set
					if (!acc) res.status.put(filter.field, opRes) // init on first use
					else {
						def missing = acc.findAll { accit -> !(opRes.find { accit.is(it) }) }
						acc.removeAll(missing) // intersect
						res.status.put(filter.field, acc)

						// rule had results but intersection had none --> decision is false from now on						
						if (acc.size() == 0) res.decision = false
					}
				}
			}
			else {
				res.decision = false
				res.status.put(filter.field, [])
			}
			
		}
		else {
			// The entire response object has been returned
			// --> will presume all actions have been taken care of
			// TODO This feature needs some more work!!
		}


        def ret = [response:res]

        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (req?.opts?.statsLevel != null && req.opts.statsLevel.value >= Level.RULE.value) updateStats(duration)
        if (req?.opts?.auditLevel != null && req.opts.auditLevel.value >= Level.RULE.value) updateAudit([duration:duration, results:[opRet]], ret)

        return ret
	}
	
	Object evaluateOr(Map req, Map res) {
        def startTime = System.currentTimeMillis()

        def opRet = _evaluate(req, res)
        def opRes = opRet.result

		if (!opRes.is(res)) { // not same object
			
			if (opRes) { // groovy truth
				res.decision = true
				if (opRes.is(true)) {
					// nothing to do here
				}
				else {
					if (!(opRes in Collection)) {
						opRes = [opRes] as Set
					}
					
					def acc = res.status.get(filter.field) as Set
					if (!acc) res.status.put(filter.field, opRes) // init on first use
					else {
						acc.addAll(opRes) // join
						res.status.put(filter.field, acc)
					}
				}
			}
			
		}
		else {
			// The entire response object has been returned
			// --> will presume all actions have been taken care of
			// TODO This feature needs some more work!!
		}

        def ret = [response:res]

        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (req?.opts?.statsLevel != null && req.opts.statsLevel.value >= Level.RULE.value) updateStats(duration)
        if (req?.opts?.auditLevel != null && req.opts.auditLevel.value >= Level.RULE.value) updateAudit([duration:duration, results:[opRet]], ret)

        return ret
	}
	
	String getType() {
		operator.type
	}
	
	String getFilterId() {
		filter.id
	}
	
	def tag(String tag) {
		this.tags.add(tag)
		this
	}
	
	def addTag(String tag) {
		this.tags.add(tag)
		this
	}
	
	def removeTag(String tag) {
		this.tags.remove(tag)
		this
	}

	def readonly(Boolean sw = null) {
		if (sw != null) this.readonly = sw
		else this.readonly = true
		this
	}

}
