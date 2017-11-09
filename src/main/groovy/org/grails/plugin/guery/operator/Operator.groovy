package org.grails.plugin.guery.operator

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

	Object apply(Object val, Map req, Map res) {
		def result
		
		if (accept_values) {
			result = qb.operationManager.apply(type,val,req,res)
		}
		else {
			result = qb.operationManager.apply(type,req,res)
		}
		
		if (log.isTraceEnabled()) log.trace("Operator ${type} ===> ${result}")
		return result
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
