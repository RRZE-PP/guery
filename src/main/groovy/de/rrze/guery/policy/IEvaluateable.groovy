package de.rrze.guery.policy;

import java.util.Map;

public interface IEvaluateable {
	abstract Object evaluate(Map req, Map res)
	abstract Object evaluateAnd(Map req, Map res)
	abstract Object evaluateOr(Map req, Map res)
}
