package eu.role_project.service.dui;
import java.io.IOException;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.common.servlet.InjectedFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DuiHttpFilter extends InjectedFilter {
	private static final Logger log = LoggerFactory.getLogger(DuiHttpFilter.class);
	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		log.info("filtering http, keli watching here--------------------------------------");
	    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
	        throw new ServletException("Auth filter can only handle HTTP");
	      }
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		
		chain.doFilter(request, response);
	}

}
