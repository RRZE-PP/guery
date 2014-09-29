package de.rrze.guery.policy

import java.util.Map;

import de.rrze.guery.base.Filter;
import de.rrze.guery.base.QueryBase;
import de.rrze.guery.operator.Operator;

class Rule {

	QueryBase 	qb
	
	Filter		filter
	Operator 	operator
	Object 		value
	
	
	def Rule(QueryBase qb, Map qm) {
		this.qb = qb
		parseQueryMap(qm)
	}
	
	def parseQueryMap(Map qm) {
		this.filter = qb.filters?.get(qm.id)
		if (!this.filter) throw new RuntimeException("Could not resolve filter id in given query base: ${qm.id}")
		
		this.operator = qb.operators?.get(qm.operator)
		if (!this.operator) throw new RuntimeException("Could not resolve operator in given query base: ${qm.operator}")
		
		this.value = qm.value
	}
	
	Boolean evaluate(Object obj) {
		this.operator.apply(value, obj)
	}
	
}
