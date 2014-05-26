/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class JavaHttpClient extends RESTHttpClient implements
		JsonResponseHttpClient {

	private static Logger logger = Logger.getLogger(JavaHttpClient.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.http.RESTHttpClient#executeGETURL(java.net
	 * .URL)
	 */
	@Override
	public Document executeGETURL(URL url) throws IOException {
		return executeGETURL(url, null);
	}

	private Document parse(String xmlString)
			throws ParserConfigurationException, SAXException, IOException {
		// define inputs
//		StringReader stringReader = new StringReader(xmlString);
//		InputSource inputSource = new InputSource(stringReader);
		// parse inputs
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
		
		//System.out.println( xmlToString(document.getFirstChild()));
		
		return document;
	}

	public String executeGetStringResponse(URL url, String encoding) throws IOException {
		//TODO: encoding
		if (url != null) {
			logger.debug("opening connection to url: " + url.toString());
			URLConnection con = url.openConnection();
			BufferedReader rd = null;
			
			
			
			if(encoding!=null) {
				rd = new BufferedReader(new InputStreamReader(con.getInputStream(), encoding));
			} else {
				rd = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
			}
			StringBuffer sb = new StringBuffer();

			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			con = null;
			String xmlString = sb.toString();

			String utf8String = new String(xmlString.getBytes(), "UTF-8");
			
			return utf8String;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.http.JsonResponseHttpClient#executeJSONGETURL
	 * (java.net.URL)
	 */
	public JSONObject executeGetJSONResponse(URL url) throws IOException {
		if (url != null) {
			String responseString = executeGetStringResponse(url,null);
			JSONObject obj = null;
			try {
				obj = new JSONObject(responseString);
			} catch (JSONException e) {
				logger.error("The retrieved response could not be parsed to JSONObject: "
						+ e.getMessage());
			}
			return obj;
		} else {
			return null;
		}
	}
	
	/**
	 * queries the given url using the POST method
	 * returns null on errors,
	 * returns the server response as string
	 * @param url the url to post to
	 * @return the server response as a string
	 */
	public String executePostStringResponse(URL url) {
		if(url!=null) {
			URLConnection con = null;
			try {
				con = url.openConnection();
				con.setDoInput(true);
				con.setDoOutput(true);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		} else {
			return null;
		}
		//TODO: implement
		return null;
	}
	
	public JSONObject executePostJSONResponse(URL url) {
		//TODO: implement
		return null;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.http.RESTHttpClient#executeGETURL(java.net.URL, java.lang.String)
	 */
	@Override
	public Document executeGETURL(URL url, String encoding) throws IOException {
		Document xmlDocument = null;
		if (url != null) {
			try {
				String xmlString = executeGetStringResponse(url,encoding);
				// logger.debug("retrieved xmlContent: " + xmlString);
				try {
					logger.debug("XMLString Openscout: " + xmlString);
					xmlDocument = parse(xmlString);
				} catch (ParserConfigurationException e) {
					logger.error("An exception occured while parsing the xml: "
							+ e.getMessage());
					e.printStackTrace();
				} catch (SAXException e) {
					logger.error("An exception occured while parsing the xml: "
							+ e.getMessage());
					e.printStackTrace();
				}
			} catch (Exception e) {
				logger.error("An exception occured while executing a http get command: "
						+ e.getMessage());
			}

			if (xmlDocument != null) {
				logger.debug("Retrieved xml encoding is: "
						+ xmlDocument.getXmlEncoding());
			}
			return xmlDocument;

		} else {
			return null;
		}
	}
	
	public static String xmlToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.http.RESTHttpClient#executeGetURLJSONResponse(java.net.URL)
	 */
	@Override
	public JSONObject executeGetURLJSONResponse(URL url) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
