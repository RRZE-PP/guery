package de.rrze.guery.policy

import java.awt.event.ItemEvent;

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import de.rrze.guery.base.QueryBase;

class RuleSet {

	QueryBase qb
	
	String condition
	Collection <Rule> rules = []
	Collection <RuleSet> subsets = []
	
	def RuleSet(QueryBase qb, String queryBuilderResult) {
		this.qb = qb
		def queryMap = jsonRecParse(queryBuilderResult)
//		log.info(queryMap.getClass().name + " -- " + queryMap)
		parseQueryMap(queryMap)
	}
	
	def RuleSet(QueryBase qb, Map queryMap) {
		this.qb = qb
		parseQueryMap(queryMap)
	}
	
	def parseQueryMap(Map queryMap) {
		this.condition = queryMap.condition
		queryMap.rules.each { Map entry ->
			if (entry.containsKey("condition")) {
				this.subsets << new RuleSet(qb, entry) 
			}
			else {
				this.rules << new Rule(qb, entry)
			}
			
		} 
		
		this
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
		if (!condition && !rules && !subsets) {
			log.warn("Emtpy ruleset evaluates to 'false' by default!")
			res.decision = false
			return res
		}

		if (this.condition == 'AND') {
			
			for (Rule r : rules) { 
				 def evalResult = r.evaluate(req, res)
				 if (!evalResult.is(res)) { // not same object
					 if (evalResult) {
						 if (evalResult.is(true)) {
						 	// nothing to do here
						 }
						 else {
							 def acc = res.status.get(r.filterId) as Set
							 if (!acc) res.status.put(r.filterId, evalResult) // init on first use
							 else {
								 def missing = acc.findAll { !(it.is(evalResult)) }
								 acc.removeAll(missing)
								 res.status.put(r.filterId, acc)
							 }
						 }
					 }
					 else { // no positive response --> break here
						 //res.status.put(r.filterId, null)
						 res.decision = false
					 }
				 }
				 else {
					 // The entire response object has been returned
					 // --> will presume all actions have been taken care of
				 }
				 
				println "============== " + res.status
				 
				// AND condition
				if (res.decision == false) return res
			}
			
			// TODO
			//for (RuleSet rs : subsets) { if (!rs.evaluate(req, res)) return res } 
			
			return res
		}
		else if (this.condition == 'OR') {
		
			for (Rule r : rules) { 
				def evalResult = r.evaluate(req, res)
				if (!evalResult.is(res)) { // not same object
					if (evalResult) {
						if (evalResult.is(true)) {
							// nothing to do here
						}
						else {
							def acc = res.status.get(r.filterId) as Set
							if (!acc) res.status.put(r.filterId, evalResult) // init on first use
							else {
								acc.addAll(evalResult)
								res.status.put(r.filterId, acc)
							}
						}
					}
					else { // no positive response --> break here
						//res.status.put(r.filterId, null)
						res.decision = false
						return res
					}
				}
				else {
					// The entire response object has been returned 
					// --> will presume all actions have been taken care of
				}
				
				// OR condition
				if (res.decision == true) return res
			}
			
			// TODO
			//for (RuleSet rs : subsets) { if (rs.evaluate(req, res)) return res }
			
			return res
		}
		else {
			throw new RuntimeException("Unknown condition: ${this.condition}")
		}
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
		obj.collect { it }
	}
	static jsonRecConvert(Object obj) {
		return obj
	}
}
