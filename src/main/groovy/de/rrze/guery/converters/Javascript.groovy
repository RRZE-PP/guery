package de.rrze.guery.converters

import grails.converters.JSON
import grails.util.GrailsWebUtil
import groovy.util.logging.Log4j
import org.grails.web.converters.exceptions.ConverterException

import javax.servlet.http.HttpServletResponse

@Log4j
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
		value(o);
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
