package de.rrze.guery.policy

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
	
	
	Map explain(Object obj) {
		
	}
	
	
	Boolean evaluate(Object obj) {
		
		if (!condition && !rules && !subsets) {
			log.warn("Emtpy ruleset evaluates to 'false' by default!")
			return false
		}

		if (this.condition == 'AND') {
			
			for (Rule r : rules) { if (!r.evaluate(obj)) return false }
			for (RuleSet rs : subsets) { if (!rs.evaluate(obj)) return false }
			
			return true
		}
		else if (this.condition == 'OR') {
		
			for (Rule r : rules) { if (r.evaluate(obj)) return true }
			for (RuleSet rs : subsets) { if (rs.evaluate(obj)) return true }
			
			return false
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
