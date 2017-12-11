package org.grails.plugin.guery.operator

import org.grails.plugin.guery.Level
import org.grails.plugin.guery.base.Filter
import org.grails.plugin.guery.base.QueryBase
import org.grails.plugin.guery.converters.JavascriptCode
import groovy.util.logging.Log4j

@Log4j
class Operator {

	QueryBase qb
	Filter filter
	
	String				type
	Boolean				accept_values
	Collection<String>	apply_to
	
	Boolean				multiple
	String 				label
	
	JavascriptCode mongo

    def stats = [
            last: null,
            count : 0,
            avgTime: 0,
            maxTime: 0,
            minTime: 0,
    ]

    Level statsLevel = Level.ALL
    Level auditLevel = Level.OFF

    static Closure defaultMapper = { val ->
        if (multiple && val in String) {
            // auto-convert CSV values
            log.debug("Auto-converting values from CSV-String to Collection for Operator '${type}'.")
            def newVal = []
            newVal.addAll(val.split(','))
            return newVal
        }
        else {
            return val
        }
    }

    Closure mapper

	Operator() { setMapper(Operator.defaultMapper) }

	def setMongo(Boolean flag) {
		if (flag) mongo = new JavascriptCode('function(v){ return v[0]; }')
		else mongo = null
	}
	def setMongo(String code) {
		if (code.startsWith("function")) this.mongo = new JavascriptCode(code)
		else this.mongo = new JavascriptCode("function(k,v){ return ${code}; }")
	}
	def setMongo(JavascriptCode code) {
		if (code.toString().startsWith("function")) this.mongo = code
		else this.mongo = new JavascriptCode("function(k,v){ return ${code.toString()}; }")
	}

    def setMapper(Closure m) {
        Closure mClone = m.clone()
        mClone.resolveStrategy = Closure.DELEGATE_FIRST
        mClone.delegate = this
        mapper = mClone
    }

    protected void updateStats(timeMs) {
        this.stats.last = new Date()

        if (timeMs > stats.maxTime) stats.maxTime = timeMs
        if (timeMs < stats.minTime) stats.minTime = timeMs

        // travelling mean (see https://math.stackexchange.com/a/106720)
        stats.count++
        stats.avgTime = stats.avgTime + ((timeMs - stats.avgTime) / stats.count)
    }

    protected updateAudit(data, dest) {
        def wrapper = [:]
        wrapper.type = 'Operator'
        wrapper.time = new Date()
        wrapper.duration = data.duration
        if (this.stats.last) wrapper.stats = this.stats.clone()
        wrapper.ref = this

        dest.audit = wrapper
    }

	Object apply(Object val, Map req, Map res) {
        def startTime = System.currentTimeMillis()

		def opResult
		
		if (accept_values) {
			opResult = qb.operationManager.apply(type,val,req,res)
		}
		else {
			opResult = qb.operationManager.apply(type,req,res)
		}
		
		if (log.isTraceEnabled()) log.trace("Operator ${type} ===> ${opResult}")

        def ret = [result: opResult, response:res]

        def stopTime = System.currentTimeMillis()
        def duration = stopTime-startTime
        if (req?.opts?.statsLevel != null && req.opts.statsLevel.value >= Level.ALL.value) updateStats(duration)
        if (req?.opts?.auditLevel != null && req.opts.auditLevel.value >= Level.ALL.value) updateAudit([duration:duration, results:[opResult]], ret)

        return ret
	}
	
	
	Map flatten(Map params = [:]) {
		def ret = [:]
		
		putIfNonNull(ret,"type")
		putIfNonNull(ret,"apply_to")
		
		// FIXME Hack to support v2.1.0
//		putIfNonNull(ret,"accept_values")
		if (accept_values) {
			ret.nb_inputs = 1
			if (this.multiple) {
				ret.multiple = true
			}
			else {
				ret.multiple = false
			}
		}
		else {
			ret.nb_inputs = 0
			ret.multiple = false
		}
		
		ret
	}

	
	private Map putIfNonNull(Map map, String fieldName) {
		def value = this."${fieldName}"
		if (value != null) {
			map.put(fieldName, value)
		}
		map
	}
}
