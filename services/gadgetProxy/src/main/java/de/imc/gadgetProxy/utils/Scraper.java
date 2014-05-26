package de.imc.gadgetProxy.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class which reads a html page, insert a piece of JavaScript 
 * (for cross-domain communication), changes all relative links
 * to absolute links and returns the page back in String 
 * representation
 * 
 * The most of the code is based from a tutorial from blogs.sun.com
 * 
 *   http://blogs.sun.com/jtb/entry/cross_domain_iframe_resizing
 * 
 * @author Daniel Dahrendorf (daniel.dahrendorf@im-c.de)
 * @version 0.1, 21.12.2009
 *
 */
public class Scraper {

	/**
	 * This attribute stores the url which should be scraped
	 */
	private String url;
	
	
	/**
	 * This attribute stores the servlet url
	 */
	private String servletUrl;

	/**
	 * Constructor of the class
	 * 
	 * @param url
	 */
	public Scraper(String url,String servletUrl) {
		this.url = url;
		this.servletUrl = servletUrl;
		
		System.out.println("x"+this.servletUrl);
		
		if(this.servletUrl.endsWith("GadgetProxy")){
		    this.servletUrl = 
		        this.servletUrl.substring(0, this.servletUrl.length()-"GadgetProxy".length());
		}
		
		System.out.println("s"+this.servletUrl);
	}

	/**
	 * Returns a String of the page from the url defined in the constructor 
	 * 
	 * @param proxyURLPrefix the prefix which will be removed from the url
	 * @return
	 * @throws IOException
	 */
	public String scrape(String proxyURLPrefix) throws IOException {
		URL u = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(u
				.openStream(), "UTF-8"));

		String inputLine;
		StringBuffer b = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			b.append(inputLine);
		}

		in.close();

		String base = getBase(url);
		String result = b.toString();

		if (base != null) {
			result = setBase(result, base);
			result = replaceLinks(result, base, proxyURLPrefix);
			result = setJavaScript(result);
		} else {
			result = b.toString();

		}

		return result;
	}

	/**
	 * Replaces all relatives links with absolute links
	 * 
	 * @param content A html String
	 * @param base A base url
	 * @param proxyURLPrefix The prefix of the proxy
	 * @return
	 */
	private static String replaceLinks(String content, String base,
			String proxyURLPrefix) {

		// create link pattern
		Pattern pattern = Pattern.compile("<a(.*?)href=\"(.*?)\"(.*?)>",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		StringBuffer stringBuffer = new StringBuffer();

		while (matcher.find()) {

			// TODO: find a better regex for protocols
			if (!matcher.group(2).matches("(.*?):(.*?)")) {
				// we found no protocol so we can assume that the link will be
				// relative
				// and we append the proxyURLPrefix and the base to the link
				matcher.appendReplacement(stringBuffer, "<a" + matcher.group(1)
						+ "href=\"" + proxyURLPrefix + base + "/"
						+ matcher.group(2) + "\"" + matcher.group(3) + ">");
			} else if (matcher.group(2).matches("http(.?):(.*?)")) {
				// we found a absolute link just add the proxyURLPrefix
				matcher.appendReplacement(stringBuffer, "<a" + matcher.group(1)
						+ "href=\"" + proxyURLPrefix + matcher.group(2) + "\""
						+ matcher.group(3) + ">");
			}

		}

		return stringBuffer.toString();
	}

	/**
	 * Return the base of an url
	 * 
	 * @param url
	 * @return
	 */
	private static String getBase(String url) {
		try {
			URL u = new URL(url);
			StringBuffer b = new StringBuffer();
			b.append(u.getProtocol());
			b.append("://");
			b.append(u.getHost());
			if (u.getPort() != -1) {
				b.append(":");
				b.append(u.getPort());
			}

			return b.toString();
		} catch (MalformedURLException mfue) {
			return null;
		}
	}

	/**
	 * Sets a base tag
	 * 
	 * @param content A html String
	 * @param base The base which should be set
	 * @return
	 */
	private static String setBase(String content, String base) {
		// remove base tag if it exists
		Pattern basePattern = Pattern.compile("<base.*?>.*?</base>|<base.*?>",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher baseMatcher = basePattern.matcher(content);
		if (baseMatcher.find()) {
			// base is already set
			content = baseMatcher.replaceFirst("");
		}

		// add new base tag
		Pattern headPattern = Pattern.compile("<head>(.*?)</head>",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher headMatcher = headPattern.matcher(content);

		if (headMatcher.find()) {
			StringBuffer newHead = new StringBuffer();
			newHead.append("<head>\n");
			newHead.append("<base href=\"");
			newHead.append(base);
			newHead.append("\" />\n");
			newHead.append(headMatcher.group(1));
			newHead.append("\n");
			newHead.append("</head>\n");

			content = headMatcher.replaceFirst(Matcher.quoteReplacement(newHead
					.toString()));
		}

		return content;
	}

	/**
	 * Inserts the link to the PMRPC javascript code
	 * 
	 * @param content
	 * @return
	 */
	private String setJavaScript(String content) {
		// define javascript
		String javascript = 
		                //"<script src=\""+servletUrl+"js/pmrpc.js\" type=\"text/javascript\"></script>"
		                //"<script src='http://www.json.org/json2.js' type='text/javascript'></script>"
            		        //"<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js\" type=\"text/javascript\"></script>"
		                //Test if jQuery is loaded else load it
		                
		                "<script type=\"text/javascript\"> \n"+
                		"    if (typeof jQuery == 'undefined') { \n"+ 
                		"        var head = document.getElementsByTagName('head')[0]; \n"+
                		"        script = document.createElement('script'); \n"+
                		"        script.id = 'jQuery'; \n"+
                		"        script.type = 'text/javascript'; \n"+
                		"        script.src = 'http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js'; \n"+
                		"        head.appendChild(script); \n"+
                		"    } \n"+
                                ""+
                                "        var head = document.getElementsByTagName('head')[0]; \n"+
                                "        script = document.createElement('script'); \n"+
                                "        script.id = 'jQuery'; \n"+
                                "        script.type = 'text/javascript'; \n"+
                                "        script.src = '"+servletUrl+"js/gadgetProxy.js\'; \n"+
                                "        head.appendChild(script); \n"+
                                ""+
                                "        var head = document.getElementsByTagName('head')[0]; \n"+
                                "        script = document.createElement('script'); \n"+
                                "        script.id = 'jQuery'; \n"+
                                "        script.type = 'text/javascript'; \n"+
                                "        script.src = '"+servletUrl+"js/json2.js'; \n"+
                                "        head.appendChild(script); \n"+
		                "</script> \n"+
            		        //"<script src=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.0/jquery-ui.js\" type=\"text/javascript\"></script>"+
		                //"<script src=\""+servletUrl+"js/json2.js\" type=\"text/javascript\"></script> "+
		                "<script src='http://pmrpc.googlecode.com/files/pmrpc.js' type='text/javascript'></script> ";
				//"<script src=\""+servletUrl+"js/gadgetProxy.js\" type=\"text/javascript\"></script> ";

		// add javascript
		Pattern headPattern = Pattern.compile("<head>(.*?)</head>",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher headMatcher = headPattern.matcher(content);

		if (headMatcher.find()) {
			StringBuffer newHead = new StringBuffer();
			newHead.append("<head>\n");
			newHead.append(headMatcher.group(1));
			newHead.append("\n");
			newHead.append(javascript);
			newHead.append("</head>\n");

			content = headMatcher.replaceFirst(Matcher.quoteReplacement(newHead
					.toString()));

		} else {

			System.out.println("No head found???");
		}
		return content;
	}
}
