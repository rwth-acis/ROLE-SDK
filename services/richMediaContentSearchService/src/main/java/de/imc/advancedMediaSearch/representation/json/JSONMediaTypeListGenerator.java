/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.representation.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.imc.advancedMediaSearch.result.MediaType;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class JSONMediaTypeListGenerator {

	private JSONObject generateJSONMediaType(MediaType t) {
		String stringvalue = capitalizeFirstLetter(t.toString());

		JSONObject obj = new JSONObject();
		try {
			obj.put("id", t.toString());
			obj.put("name", stringvalue);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public JSONObject generateJSONMediaTypeList(String callurl) {
		List<JSONObject> mediaTypes = new ArrayList<JSONObject>();
		for (MediaType t : MediaType.values()) {
			mediaTypes.add(generateJSONMediaType(t));
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("search-url", callurl);
			obj.put("items", mediaTypes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;

	}

	private String capitalizeFirstLetter(String input) {
		if (input == null || input.length() < 1) {
			return "";
		}

		input = input.toLowerCase();
		
		String firstchar = input.substring(0, 1).toUpperCase();
		String restchars = input.substring(1, input.length());
		
		String ret = firstchar + restchars;
		return ret;
	}
}
