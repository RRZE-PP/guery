package de.rrze.guery.converters

import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONWriter

class JavascriptWriter extends JSONWriter {
	
	
	def JavascriptWriter(Writer out) {
		super(out)
//		println "JavascriptWriter init ..."
	}
	 
	public JavascriptWriter value(Object o) {
//		println o.getClass().getName() + "     " + o.toString() + "    mode:${mode}"
		
		if (o instanceof JavascriptCode) {
			append(o.toString());
		}
		else if (o instanceof JSONElement) {
			append(o.toString());
		}
		else {
			super.value(o)
		}
		return this
    }
	
//	public JavascriptWriter key(String s) {
//		println "key: ${s}, mode: ${mode}"
//		def r = super.key(s)
//		println "mode: ${mode}"
//		r
//	}
//	
//	
//	protected JSONWriter append(String s) {
//		println "append: ${s}, mode: ${mode}"
//		def r = super.append(s)
//		println "mode: ${mode}"
//		r
//	}
}