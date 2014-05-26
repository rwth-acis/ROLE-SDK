package de.imc.gadgetProxy;

import de.imc.gadgetProxy.utils.Scraper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class GadgetProxy
 * 
 * 
 * @author Daniel Dahrendorf (daniel.dahrendorf@im-c.de)
 * @version 0.1, 21.12.2009
 *
 */
public class GadgetProxy extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    /**
     * Default constructor
     * 
     * @see HttpServlet#HttpServlet()
     */
    public GadgetProxy() {
        super();
    }

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGetOrPost(request,response);
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGetOrPost(request,response);
	}
	

	/**
	 * The handler of a get or post request
	 * 
	 * @param request 
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getParameter("url");
		
		try{
			//init scraper
			Scraper scraper = new Scraper(url,"http://"+request.getServerName()+":"+request.getServerPort()+request.getRequestURI());
			
			//scrape the page from the given url with the constructed the proxy prefix
			String result = 
				scraper.scrape("http://"+request.getServerName()+":"+request.getServerPort()+request.getRequestURI()+"?url=");
			
			//set encoding and type and return the scraped page 
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html");
			response.getWriter().write(result);
			
		}catch(Exception e){
			//TODO: log
			
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html");
			
			//TODO: create a nice not found answer
			response.getWriter().write("Can't find URL: "+url);
		}
		
	}

}
