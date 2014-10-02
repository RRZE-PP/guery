package de.rrze.guery

import grails.converters.JSON
import groovy.lang.Closure;
import de.rrze.guery.base.QueryBase
import de.rrze.guery.base.QueryBaseBuilder
import de.rrze.guery.policy.Policy

class GueryInstance {

	String id
	String description
	
	QueryBase qb
	Map<String,Policy> policies = [:] 
	
	
	def GueryInstance() {}
	
	
	
	
	/*
	 * QUERY BASE
	 */
	QueryBase makeBase(Closure c) {
		qb = new QueryBaseBuilder().make(c)
		qb
	}
	
	JSON baseToJs() {
		qb.toJs()
	}

	String baseToJsString(Boolean prettyPrint) {
		qb.toJsString(prettyPrint)
	}

	
	QueryBase getQueryBase() {
		qb
	}
	
	/*
	 * POLICIES
	 */
	Policy parsePolicyFromJson(String queryBuilderResult, Closure mods = null) {
		def p = new Policy(qb, queryBuilderResult)
		if (mods) { mods(p) }
		p
	}
	
	Policy makePolicyFromJson(String id, String queryBuilderResult, Closure mods = null) {
		def p = parsePolicyFromJson(queryBuilderResult, mods)
		p.id = id
		addPolicy(p)
		p
	}
	
	GueryInstance addPolicy(Policy p) {
		if (!p.id) throw new RuntimeException("Cannot add policy without id!")
		policies.put(p.id, p)
		this
	}
	
	Policy getPolicy(String id) {
		policies.get(id)
	}
	
	
}
