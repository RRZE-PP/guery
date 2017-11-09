package org.grails.plugin.guery.converters

import groovy.util.logging.Log4j
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONWriter

@Log4j
class JavascriptWriter extends JSONWriter {

	def JavascriptWriter(Writer out) {
		super(out)
	}
	 
	public JavascriptWriter value(Object o) {
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
}
