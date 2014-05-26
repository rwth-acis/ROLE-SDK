package de.imc.advancedMediaSearch.http;

import java.io.IOException;
import java.net.URL;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public abstract class RESTHttpClient {

    public abstract Document executeGETURL(URL url) throws IOException;
    
    public abstract Document executeGETURL(URL url, String encoding) throws IOException;
    
    public abstract JSONObject executeGetURLJSONResponse(URL url) throws IOException;
    
    public static String getStringFromDoc(org.w3c.dom.Document doc)    {
        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        return lsSerializer.writeToString(doc);   
    }

}
