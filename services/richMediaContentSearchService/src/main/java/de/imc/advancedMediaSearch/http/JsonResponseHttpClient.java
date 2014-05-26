/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.http;

import java.io.IOException;
import java.net.URL;

import org.json.JSONObject;

/**
 * @author julian.weber@im-c.de
 *
 */
public interface JsonResponseHttpClient {
	
	public JSONObject executeGetJSONResponse(URL url) throws IOException;
}
