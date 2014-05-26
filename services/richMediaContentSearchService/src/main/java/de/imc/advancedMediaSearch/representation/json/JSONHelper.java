/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.representation.json;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class JSONHelper {

	/**
	 * wraps the specified function call around the json objects data
	 * 
	 * @param o
	 * @param callbackFunction
	 * @return
	 */
	private static String wrapJsonToJsonp(JSONObject o, String callbackFunction) {
		String ret = callbackFunction + "( " + o.toString() + " )";
		return ret;
	}

	/**
	 * wraps the specified function call around the json strings data
	 * 
	 * @param o
	 * @param callbackFunction
	 * @return
	 */
	private static String wrapJsonToJsonp(String o, String callbackFunction) {
		String ret = callbackFunction + "( " + o + " )";
		return ret;
	}

	/**
	 * creates a json representation from the given json object
	 * 
	 * @param o
	 *            the json object to represent
	 * @return a json representation of the given Json object
	 */
	public static Representation createJsonRepresentation(JSONObject o) {
		StringRepresentation rep = new StringRepresentation(o.toString(),
				MediaType.APPLICATION_JSON);
		return rep;
	}

	/**
	 * creates a Json-P representation from the given json object using the
	 * specified callback function
	 * 
	 * @param o
	 *            a json object
	 * @param callback
	 *            the callback function to use
	 * @return a JsonP representation
	 */
	public static Representation createJsonPRepresentation(JSONObject o,
			String callback) {
		StringRepresentation rep = new StringRepresentation(wrapJsonToJsonp(o,
				callback), MediaType.TEXT_JAVASCRIPT);
		return rep;
	}

	/**
	 * creates a Json-P representation from the given json string using the
	 * specified callback function
	 * 
	 * @param o
	 *            a json string
	 * @param callback
	 *            the callback function to use
	 * @return a JsonP representation
	 */
	public static Representation createJsonPRepresentation(String o,
			String callback) {
		StringRepresentation rep = new StringRepresentation(wrapJsonToJsonp(o,
				callback), MediaType.TEXT_JAVASCRIPT);
		return rep;
	}

	public static Representation createJSONErrorRepresentation(String errorId,
			String internalMessage, List<String> userMessages) {
		JSONObject o = createJSONErrorObject(errorId, internalMessage,
				userMessages);
		return createJsonRepresentation(o);

	}

	/**
	 * @param string
	 * @param string2
	 * @param object
	 * @return
	 */
	public static Representation createJSONPErrorRepresentation(String errorId,
			String internalMessage, List<String> userMessages, String callback) {
		JSONObject o = createJSONErrorObject(errorId, internalMessage,
				userMessages);
		return createJsonPRepresentation(o, callback);
	}

	private static JSONObject createJSONErrorObject(String errorId,
			String internalMessage, List<String> userMessages) {
		JSONObject o = new JSONObject();

		try {
			if (internalMessage != null) {
				o.put("internal-message", internalMessage);
			}
			if (userMessages != null) {
				o.put("user-messages", userMessages);
			}
			if (errorId != null) {
				o.put("error-id", errorId);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

}
