package de.imc.advancedMediaSearch.http;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JakartaRESTHttpClient extends RESTHttpClient {

    private static Logger logger = Logger.getLogger(JakartaRESTHttpClient.class);

    @Override
    public Document executeGETURL(URL url) throws IOException{
        HttpClient httpclient = new DefaultHttpClient();
        
        HttpGet httpget = new HttpGet(url.toString());
       
        try {
            HttpResponse response = httpclient.execute(httpget);
            
            HttpEntity entity = response.getEntity();
            
            return this.parse(EntityUtils.toString(entity));

            
        } catch (ClientProtocolException e) {
            logger.error("Error: " + e.getMessage(),e);
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage(),e);
        } catch (ParseException e) {
            logger.error("Error: " + e.getMessage(),e);
        } catch (ParserConfigurationException e) {
            logger.error("Error: " + e.getMessage(),e);
        } catch (SAXException e) {
            logger.error("Error: " + e.getMessage(),e);
        }
        
        return null;

    }
    
    private Document parse(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        // define inputs
        StringReader stringReader = new StringReader(xmlString);
        InputSource inputSource = new InputSource(stringReader);
        // parse inputs
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputSource);
        return document;
}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.http.RESTHttpClient#executeGETURL(java.net.URL, java.lang.String)
	 */
	@Override
	public Document executeGETURL(URL url, String encoding) throws IOException {
		return executeGETURL(url);
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
