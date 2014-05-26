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

import de.imc.advancedMediaSearch.target.Target;

/**
 * @author julian.weber@im-c.de
 *
 */
public class RepositoryJSONGenerator {
	
	/**
	 * generates a JSONObject from a targets meta information
	 * @param t the target to transform
	 * @return JSONObject representating the given targets meta data
	 */
	public JSONObject generateJSONTargetObject(Target t) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", t.getId());
			obj.put("name", t.getName());
			obj.put("url", t.getUrl());
			obj.put("icon", t.getIconUrl());
			obj.put("media-type-icon", t.getMediaTypeIconUrl());
			obj.put("description", t.getDescription());
			obj.put("media-types", t.getMediaTypes());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * generates a JSONObject representing a list of given targets
	 * @param targets the targets to represent 
	 * @param getUrl the http call url to attach to the information
	 * @return a JSONObject representing the given list of targets 
	 */
	public JSONObject generateJSONTargetListObject(List<Target> targets, String getUrl) {
		JSONObject list = new JSONObject();
		List<JSONObject> targetJsons = new ArrayList<JSONObject>();
		
		for(Target t : targets) {
			targetJsons.add(generateJSONTargetObject(t));
		}
		
		int numberOfTargets = targets.size();
		try {
			list.put("search-url", getUrl);
			list.put("total-results", numberOfTargets);
			list.put("items", targetJsons);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}
}
