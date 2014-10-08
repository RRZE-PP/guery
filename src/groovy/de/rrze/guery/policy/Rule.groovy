package de.rrze.guery.policy

import java.util.Map;

import de.rrze.guery.base.Filter;
import de.rrze.guery.base.QueryBase;
import de.rrze.guery.operator.Operator;

class Rule implements IEvaluateable {

	QueryBase 	qb
	
	Filter		filter
	Operator 	operator
	Object 		val
	
	
	def Rule(QueryBase qb, Map qm) {
		this.qb = qb
		parseQueryMap(qm)
	}
	
	def parseQueryMap(Map qm) {
		this.filter = qb.filters?.get(qm.id)
		if (!this.filter) throw new RuntimeException("Could not resolve filter id in given query base: ${qm.id}")
		
		this.operator = qb.operators?.get(qm.operator)
		if (!this.operator) throw new RuntimeException("Could not resolve operator in given query base: ${qm.operator}")
		
		this.val = qm.value
	}
	
	Object evaluate(Map req, Map res) {
		this.operator.apply(val, req, res)
	}
	
	Object evaluateAnd(Map req, Map res) {
		def evalResult = evaluate(req, res)
		
		if (!evalResult.is(res)) { // not same object
			if (evalResult) {
				if (evalResult.is(true)) {
					// nothing to do here
				}
				else {
					if (!(evalResult in Collection)) {
						evalResult = [evalResult] as Set
					}
					
					def acc = res.status.get(filter.id) as Set
					if (!acc) res.status.put(filter.id, evalResult) // init on first use
					else {
						def missing = acc.findAll { accit -> !(evalResult.find { accit.is(it) }) }
						acc.removeAll(missing)
						res.status.put(filter.id, acc)
					}
				}
			}
			else {
				res.decision = false
				res.status.put(filter.id, [])
			}
		}
		else {
			// The entire response object has been returned
			// --> will presume all actions have been taken care of
		}
		
		evalResult
	}
	
	Object evaluateOr(Map req, Map res) {
		def evalResult = evaluate(req, res)
		
		if (!evalResult.is(res)) { // not same object
			if (evalResult) {
				if (evalResult.is(true)) {
					// nothing to do here
				}
				else {
					if (!(evalResult in Collection)) {
						evalResult = [evalResult] as Set
					}
					
					def acc = res.status.get(filter.id) as Set
					if (!acc) res.status.put(filter.id, evalResult) // init on first use
					else {
						acc.addAll(evalResult)
						res.status.put(filter.id, acc)
					}
				}
				
				res.decision = true
			}
		}
		else {
			// The entire response object has been returned
			// --> will presume all actions have been taken care of
		}
		
		evalResult
	}
	
	String getType() {
		operator.type
	}
	
	String getFilterId() {
		filter.id
	}
	
}
