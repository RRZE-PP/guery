package de.rrze.guery.base

import de.rrze.guery.operator.IOperationManager
import groovy.util.logging.Log4j

@Log4j
class QueryBaseBuilder {


    QueryBase qb

    def QueryBaseBuilder() {}

    QueryBase makeDelegate(QueryBase parentQb, Closure c) {
        qb = new DelegatingQueryBase(parentQb)
        runClosure(c)
        qb
    }

    QueryBase make(Closure c) {
        qb = new QueryBase()
        runClosure(c)
        qb
    }

    QueryBase make(QueryBase qb) {
        qb
    }


    def filter(Map m, Closure c) {
        def f = new Filter(m)
        if (log.isTraceEnabled()) log.trace("Building filter: ${m}")

        c.delegate = new FilterInterceptor(f, qb)
        c(f)

        if (log.isTraceEnabled()) log.trace("Adding filter ${f.id} ...")
        qb.addFilter(f)
    }

    def expose(String id, value) {
        qb.sharedData.put(id, value)
    }

    def params(String id, value) {
        qb.instanceData.put(id, value)
    }

    def expose(Map value) {
        value.each { k,v -> expose(k,v) }
    }

    def params(Map value) {
        value.each { k,v -> params(k,v) }
    }

    def lang(Map value) {
        qb._lang += value
    }

    def sortable(Boolean value) {
        if (value) plugin('sortable')
    }

    def filterDescription(Boolean value) {
        if (value)  plugin('filter-description')
    }

    def plugins(List<String> value) {
        qb._plugins = value as Set
    }

    def plugin(String value) {
        if (!qb._plugins) qb._plugins = [] as Set
        qb._plugins.add(value)
    }

    def allowEmpty(Boolean value) {
        qb._allowEmpty = value
    }

    def conditions(List<String> value) {
        qb._conditions = value
        if (!qb._defaultCondition) defaultCondition(value.get(0))
    }

    def defaultCondition(String value) {
        qb._defaultCondition = value
    }

    def operationManager(IOperationManager opm) {
        qb.operationManager = opm
    }

    def id(String value) {
        qb.id = value
    }

    def description(String value) {
        qb.description = value
    }

    def propertyMissing(String name, Object value) {
        if (name == 'sortable') sortable(value)
        else if (name == 'plugins') plugins(value)
        else if (name == 'conditions') conditions(value)
        else if (name == 'defaultCondition') defaultCondition(value)
        else if (name == 'id') id(value)
        else if (name == 'description') description(value)
        else if (name == 'lang') lang(value)
        else if (name == 'allowEmpty') allowEmpty(value)
        else if (name == 'expose') expose(value)
        else if (name == 'params') params(value)
        else throw new MissingPropertyException(name, this.class)
    }





    private runClosure(Closure runClosure) {
        Closure runClone = runClosure.clone()
        runClone.delegate = this
        runClone.resolveStrategy = Closure.DELEGATE_FIRST
        runClone()
    }
}