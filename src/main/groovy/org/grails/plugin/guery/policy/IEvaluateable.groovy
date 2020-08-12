package org.grails.plugin.guery.policy;

import java.util.Map;

public interface IEvaluateable {
	abstract Object evaluate(Map req, Map res)
	abstract Object evaluateAnd(Map req, Map res)
	abstract Object evaluateOr(Map req, Map res)

    abstract Object readonly(Boolean sw)
    abstract Object readonly()

    abstract Object readonlyRulesByFilterId(Collection <String> filterIds, Boolean sw)
    abstract Object readonlyRulesByFilterId(Collection <String> filterIds)

    abstract Object readonlyStructure(Boolean sw)
    abstract Object readonlyStructure()

    Collection<Rule> findAllByFilterIds(Collection<String> filterIds, Collection<Rule> result)
}

