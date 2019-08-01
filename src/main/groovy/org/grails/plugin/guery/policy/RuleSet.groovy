package org.grails.plugin.guery.policy

import groovy.util.logging.Log4j
import grails.converters.JSON
import org.grails.plugin.guery.Level
import org.grails.plugin.guery.util.IntersectUtil
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import org.grails.plugin.guery.base.QueryBase

@Log4j
class RuleSet implements IEvaluateable {

	QueryBase qb
	
	String condition
	List <IEvaluateable> evals = []
	
	Set<String> tags = []
	Boolean		readonly = false

    def stats = [
            last : null,
            count : 0,
            avgTime: 0,
            maxTime: 0,
            minTime: Long.MAX_VALUE,
    ]

	def RuleSet(QueryBase qb, String queryBuilderResult) {
		this.qb = qb
		def ruleMap = jsonRecParse(queryBuilderResult)
		parseRuleMap(ruleMap)
	}
	
	def RuleSet(QueryBase qb, Map queryMap) {
		this.qb = qb
		parseRuleMap(queryMap)
	}
	
	def RuleSet(String condition, Collection<IEvaluateable> evals) {
		this.condition = condition
		this.evals.addAll(evals)
	}


	def parseRuleMap(Map queryMap) {
		this.condition = queryMap.condition
		if (queryMap.tags) this.tags = queryMap.tags
		if (queryMap.readonly) this.readonly = queryMap.readonly
		queryMap.rules.each { Map entry ->
			if (entry.containsKey("condition")) {
				this.evals << new RuleSet(qb, entry) 
			}
			else {
				this.evals << new Rule(qb, entry)
			}
			
		} 
		
		this
	}
	
	IEvaluateable find(Closure c) {
		evals.find(c)
	}
	
	List<IEvaluateable> findAll(Closure c) {
		evals.findAll(c)
	}

	Map toRuleMap() {
		if (!this.condition) return [:]
		
		def ruleMap = [
			condition : this.condition,
            data: [:],
		]
		if (tags) ruleMap.tags = tags
		if (readonly != null) ruleMap.readonly = readonly

		
		ruleMap.rules = []		
		this.evals.each { ruleMap.rules <<  it.toRuleMap() }
		
		ruleMap
	}
	
	def toJSON(Boolean convert=true) {
		if (!this.condition) return [:]
		
		def ruleMap = [
			condition : this.condition,
			data: [:],
		]
		if (tags) ruleMap.data.tags = tags?.join(';')
		if (readonly != null) ruleMap.readonly = readonly

		
		ruleMap.rules = []
		this.evals.each { ruleMap.rules <<  it.toJSON(false) }
		
		return convert?(ruleMap as JSON):ruleMap
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

    private _evaluate(Map req, Map res) {
        def childrenRet = []

        if (!condition && !evals) {
            if (log.isWarnEnabled()) log.warn("Emtpy ruleset evaluates to 'false' by default!")
            res.decision = false
        }
        else if (this.condition == 'AND') {

            for (IEvaluateable e : evals) {
                childrenRet << e.evaluateAnd(req, res)

                // AND condition
                //if (res.decision == false) return res // no positive decision, && behaviour --> break here
            }
        }
        else if (this.condition == 'OR' || this.condition == 'EXECUTE') { // FIXME EXECUTE equals OR

            for (IEvaluateable e : evals) {
                childrenRet << e.evaluateOr(req, res)

                // OR condition
                //if (res.decision == true) return res // positive decision, || behaviour --> break here
            }
        }
        else {
            throw new RuntimeException("Unknown condition: ${this.condition}")
        }

        return childrenRet
    }

    Map evaluate(Map req) {
        def startTime = System.currentTimeMillis()

        def res = [
                decision : (this.condition == 'AND')?true:false,
                status : [:],
                obligations : [:],
        ]

        def childrenRet = _evaluate(req, res)
        def ret = [response:res]

        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (Level.RULESET.matches(req?.opts?.statsLevel)) updateStats(duration)
        if (Level.RULESET.matches(req?.opts?.auditLevel)) updateAudit([duration:duration, results:childrenRet], ret)

        return ret
    }

	Map evaluate(Map req, Map res) {
        def startTime = System.currentTimeMillis()

        def childrenRet = _evaluate(req,res)

        def ret = [response:res]
        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (Level.RULESET.matches(req?.opts?.statsLevel)) updateStats(duration)
        if (Level.RULESET.matches(req?.opts?.auditLevel)) updateAudit([duration:duration, results:childrenRet], ret)

        return ret
	}
	
	def evaluateAnd(Map req, Map res) {
        def startTime = System.currentTimeMillis()
        def childrenRet

		if (this.condition == 'AND') {
            childrenRet = _evaluate(req, res)
		}
		else {
			// Outer condition is 'AND'(intersect) - inner condition is 'OR'(join)
			def tmpResponse = [
				decision : false,
				status : [:],
				obligations : [:],
				]
            childrenRet = _evaluate(req, tmpResponse)
			
			// merge decision - AND
			if (tmpResponse.decision == false) {
				res.decision = false 
			}

			
			// merge status updates on positive decision (e.g. the subordinate Rule or RuleSet were applicable)
			if (tmpResponse.decision == true) {
				tmpResponse.status.each { filterId, evalResult ->
					if (!(evalResult in Collection)) evalResult = [evalResult] as Set

					def acc = res.status.get(filterId) as Set
					if (!acc) res.status.put(filterId, evalResult) // init on first use
					else {
						// intersect
                        def result

                        result = IntersectUtil.intersectWithIdentityMap(acc,evalResult)

						res.status.put(filterId, result)
					}
				}
			}
			
			// merge obligations on positive decision (e.g. the subordinate Rule or RuleSet were applicable)
			if (tmpResponse.decision == true) {
				tmpResponse.obligations.each { k, v ->
					if (!(v in Collection)) {
						v = [v] as Set
					}
					
					def acc = res.obligations.get(k) as Set
					if (!acc) res.obligations.put(k, v)
					else {
						acc.putAll(v)
						res.obligations.put(k, acc)
					}
				}
			}
		}

        def ret = [response:res]
        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (Level.RULESET.matches(req?.opts?.statsLevel)) updateStats(duration)
        if (Level.RULESET.matches(req?.opts?.auditLevel)) updateAudit([duration:duration, results:childrenRet], ret)

        return ret
	}
	
	def evaluateOr(Map req, Map res) {
        def startTime = System.currentTimeMillis()
        def childrenRet

		if (this.condition == 'OR') {
            childrenRet = _evaluate(req, res)
		}
		else {
			// Outer condition is 'OR'(join) - inner condition is 'AND'(intersect)
			def tmpResponse = [
				decision : true,
				status : [:],
				obligations : [:],
				]
            childrenRet = _evaluate(req, tmpResponse)
			
			// merge decision - OR
			if (tmpResponse.decision == true) {
				res.decision = true
			}
			
			// merge status updates on positive decision (e.g. the subordinate Rule or RuleSet were applicable)
			if (tmpResponse.decision == true) {
				tmpResponse.status.each { filterId, evalResult ->
					if (!(evalResult in Collection)) {
						evalResult = [evalResult] as Set
					}
					
					def acc = res.status.get(filterId) as Set
					if (!acc) res.status.put(filterId, evalResult) // init on first use
					else {
						// join
						acc.addAll(evalResult)
						res.status.put(filterId, acc)
					}
				}
			}
			
			// merge obligations on positive decision (e.g. the subordinate Rule or RuleSet were applicable)
			if (tmpResponse.decision == true) {
				tmpResponse.obligations.each { k, v ->
					if (!(v in Collection)) {
						v = [v] as Set
					}
					
					def acc = res.obligations.get(k) as Set
					if (!acc) res.obligations.put(k, v)
					else {
						acc.putAll(v)
						res.obligations.put(k, acc)
					}
				}
			}
		}

        def ret = [response:res]
        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (Level.RULESET.matches(req?.opts?.statsLevel)) updateStats(duration)
        if (Level.RULESET.matches(req?.opts?.auditLevel)) updateAudit([duration:duration, results:childrenRet], ret)

        return ret
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
	
	
	Boolean isEmpty() {
		toRuleMap().isEmpty()
	}
	
	
	/* LIST INTERFACE */
	def getAt(arg) {
		this.evals.getAt(arg)
	}
	
	
	/* JSON PARSING HELPERS */
	static jsonRecParse(content) {
		def jsonObj = JSON.parse(content)
		return jsonRecConvert(jsonObj)
	}
	static jsonRecConvert(JSONObject obj) {
		obj.collectEntries {
			[it.key, jsonRecConvert(it.value)]
		}
	}
	static jsonRecConvert(JSONArray obj) {
		//println obj
		obj.collect { jsonRecConvert(it) }
	}

	static jsonRecConvert(Object obj) {
		obj
	}
}
