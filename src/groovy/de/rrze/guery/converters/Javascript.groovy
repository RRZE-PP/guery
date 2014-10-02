package de.rrze.guery.converters

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.web.converters.AbstractConverter;
import org.codehaus.groovy.grails.web.converters.Converter;
import org.codehaus.groovy.grails.web.converters.Converter.CircularReferenceBehaviour;
import org.codehaus.groovy.grails.web.converters.configuration.ConverterConfiguration;
import org.codehaus.groovy.grails.web.converters.configuration.ConvertersConfigurationHolder;
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException;
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONException;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.codehaus.groovy.grails.web.json.JSONTokener;
import org.codehaus.groovy.grails.web.json.JSONWriter;
import org.codehaus.groovy.grails.web.json.PathCapturingJSONWriterWrapper;
import org.codehaus.groovy.grails.web.json.PrettyPrintJSONWriter;

import grails.converters.JSON
import grails.util.GrailsWebUtil;
import groovy.lang.Closure;

class Javascript extends JSON {

	
	protected Object target;
	
	def Javascript() {
		super()
	}
	
	
	def Javascript(Object target) {
		this()
        setTarget(target)
	}
	
	private void prepareRender(Writer out) {
		writer = new JavascriptWriter(out)
		referenceStack = new Stack<Object>();
	}
	
	public void render(Writer out) throws ConverterException {
		prepareRender(out);
		try {
			value(target);
		}
		finally {
			finalizeRender(out);
		}
	}
	
	public void convertAnother(Object o) throws ConverterException {
//		println "convertAnother: ${o}"
		value(o);
	}
	
	
	String toString(Boolean prettyPrint = false) {
		//this.prettyPrint = prettyPrint  // TODO
		super.toString()
	}

	@Override
	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public void render(HttpServletResponse response) throws ConverterException {
		response.setContentType(GrailsWebUtil.getContentType("text/javascript", GrailsWebUtil.DEFAULT_ENCODING));
        try {
            render(response.getWriter());
        }
        catch (IOException e) {
            throw new ConverterException(e);
        }
		
	}

}
