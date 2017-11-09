package org.grails.plugin.guery.base

import org.grails.plugin.guery.operator.Operator
import groovy.util.logging.Log4j

@Log4j
class FilterInterceptor implements GroovyInterceptable {

    Filter f
    QueryBase qb

    FilterInterceptor(Filter f, QueryBase qb) {
        this.f = f
        this.qb = qb
    }

    @Override
    public invokeMethod(String name, Object arguments) {
        def opSettings = [
                type			: f.id + '_' + name,
                label			: name,

                accept_values	: true,
                apply_to		: [f.type],
        ]

        if (arguments[0] in Map) {
            // overrides
            opSettings.putAll(arguments[0])
        }

        def op = new Operator(opSettings)
        op.qb = qb
        f.add(op)

        def operationClosure = arguments[-1]
        if (!(operationClosure in Closure)) {
            throw new RuntimeException("Last filter operator argument must be of type Closure!")
        }
        operationClosure.resolveStrategy = Closure.DELEGATE_FIRST
        qb.operationManager.put(op.type, operationClosure)
    }
}
