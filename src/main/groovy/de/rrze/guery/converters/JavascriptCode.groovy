package de.rrze.guery.converters

import org.grails.web.json.JSONElement

class JavascriptCode implements JSONElement, CharSequence {

	String codeString
	
	def JavascriptCode(code) {
		codeString = code
	}
	
	String toString() {
		codeString
	}
	
//	String toString(int indentFactor) {
//		codeString
//	}

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
