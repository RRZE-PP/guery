package de.rrze.guery.policy

import org.slf4j.LoggerFactory

import java.awt.event.ItemEvent;

import grails.converters.JSON

import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import de.rrze.guery.base.QueryBase

import static org.grails.web.json.JSONObject.*;

class RuleSet implements IEvaluateable {

    static log = LoggerFactory.getLogger(RuleSet.class)

	QueryBase qb
	
	String condition
	List <IEvaluateable> evals = []
	
	Set<String> tags = []
	Boolean		readonly = false
	
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
	
	Map evaluate(Map req) {
		def res = [
			decision : (this.condition == 'AND')?true:false,
			status : [:],
			obligations : [:],
		]
		evaluate(req, res)
	}
	
	Map evaluate(Map req, Map res) {
		if (!condition && !evals) {
			if (log.isWarnEnabled()) log.warn("Emtpy ruleset evaluates to 'false' by default!")
			res.decision = false
			return res
		}

		if (this.condition == 'AND') {
			
			for (IEvaluateable e : evals) { 
				e.evaluateAnd(req, res)
				 
				// AND condition
				//if (res.decision == false) return res // no positive decision, && behaviour --> break here
			}
			
			
			return res
		}
		else if (this.condition == 'OR' || this.condition == 'EXECUTE') { // FIXME EXECUTE
		
			for (IEvaluateable e : evals) { 
				e.evaluateOr(req, res)
				
				// OR condition
				//if (res.decision == true) return res // positive decision, || behaviour --> break here
			}
			
			
			return res
		}
		else {
			throw new RuntimeException("Unknown condition: ${this.condition}")
		}
	}
	
	def evaluateAnd(Map req, Map res) {
		if (this.condition == 'AND') {
			return evaluate(req, res)
		}
		else {
			// Outer condition is 'AND'(intersect) - inner condition is 'OR'(join)
			def tmpResponse = [
				decision : false,
				status : [:],
				obligations : [:],
				]
			evaluate(req, tmpResponse)
			
			// merge decision - AND
			if (tmpResponse.decision == false) {
				res.decision = false 
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
						// intersect
						def missing = acc.findAll { accit -> !(evalResult.find { accit.is(it) }) }
						acc.removeAll(missing)
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
			
			return res
		}
	}
	
	def evaluateOr(Map req, Map res) {
		if (this.condition == 'OR') {
			return evaluate(req, res)
		}
		else {
			// Outer condition is 'OR'(join) - inner condition is 'AND'(intersect)
			def tmpResponse = [
				decision : true,
				status : [:],
				obligations : [:],
				]
			evaluate(req, tmpResponse)
			
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
			
			return res
		}
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
