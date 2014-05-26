/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class ApacheHttpClient extends RESTHttpClient {
	
	private static Logger logger = Logger.getLogger(ApacheHttpClient.class);
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.http.RESTHttpClient#executeGETURL(java.net
	 * .URL)
	 */
	@Override
	public Document executeGETURL(URL url) throws IOException {
		return executeGETURL(url, "UTF-8");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.http.RESTHttpClient#executeGETURL(java.net
	 * .URL, java.lang.String)
	 */
	@Override
	public Document executeGETURL(URL url, String encoding) throws IOException {
		
		logger.debug("Start querying adress: " + url.toString());
		HttpGet get = new HttpGet(url.toString());
		HttpClient client = new DefaultHttpClient();

		HttpResponse response = client.execute(get);
		Document doc = newDocumentFromInputStream(response.getEntity()
				.getContent());
		logger.debug("Finished querying adress: " + url.toString());
		return doc;
	}

	private Document newDocumentFromInputStream(InputStream in) {
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document ret = null;

		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		try {
			ret = builder.parse(new InputSource(in));
		} catch (SAXException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		try {
			in.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.http.RESTHttpClient#executeGetURLJSONResponse
	 * (java.net.URL)
	 */
	@Override
	public JSONObject executeGetURLJSONResponse(URL url) throws IOException {
		
		logger.debug("Start querying adress: " + url.toString());
		HttpGet get = new HttpGet(url.toString());
		HttpClient client = new DefaultHttpClient();
		
		HttpResponse response = client.execute(get);
		String jsonString = convertStreamToString(response.getEntity()
				.getContent());
		
		logger.debug("Finished querying adress: " + url.toString());
		JSONObject obj;
		try {
			obj = new JSONObject(jsonString);
		} catch (JSONException e) {
			throw new IOException("Couldn't convert the retrieved data to json object!");
		}
		return obj;
	}

	public String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}
