package org.grails.plugin.guery.converters

import groovy.util.logging.Log4j
import org.grails.web.json.JSONElement

@Log4j
class JavascriptCode implements JSONElement, CharSequence {

	String codeString
	
	def JavascriptCode(code) {
		codeString = code
	}
	
	String toString() {
		codeString
	}
	
	@Override
	public int length() {
		return codeString.length()
	}

	@Override
	public char charAt(int index) {
		return codeString.charAt(index)
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return codeString.subSequence(start,end);
	}
	
	Writer writeTo(Writer w) {
		w.write(codeString)
	}
}
