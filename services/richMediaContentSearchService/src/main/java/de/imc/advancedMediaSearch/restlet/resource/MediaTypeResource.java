/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.restlet.resource;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.restlet.data.Language;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import de.imc.advancedMediaSearch.representation.json.JSONHelper;
import de.imc.advancedMediaSearch.representation.json.JSONMediaTypeListGenerator;

/**
 * Resource for representing a list of all available mime types
 * @author julian.weber@im-c.de
 *
 */
public class MediaTypeResource extends BaseServerResource {
	
	private static Logger logger = Logger.getLogger(MediaTypeResource.class);
	
	private String format;

	private String callback;
	
	private String callUrl;
	
	@Override
	public void doInit() throws ResourceException {
		
		//retrieving call url
		callUrl = getReference().getIdentifier();
		
		// retrieving parameters
		format = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("format");

		if (format != null) {
			logger.debug("Retrieved format parameter is: " + format);
		} else {
			format = DEFAULT_FORMAT;
			logger.debug("No format parameter given. Setting to: " + format);
		}

		callback = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("callback");

		if (callback != null) {
			logger.debug("Retrieved callback parameter is: " + callback);
		}
	}
	
	@Get
	public Representation represent() {
		Representation rep = null;
		
		// check format variable
		if (format.equals(RepresentationFormat.atom.toString())) {
			// TODO: implement atom representation here
			rep = new StringRepresentation(
					"Atom feed format not implemented yet!", Language.DEFAULT);
		}
		// fallback to default json
		else {
			JSONMediaTypeListGenerator gen = new JSONMediaTypeListGenerator();
			JSONObject obj = gen.generateJSONMediaTypeList(callUrl);
			
			//wrap into callback function (jsonp)
			if(callback!=null) {
				rep = JSONHelper.createJsonPRepresentation(obj, callback);
			} else {
				rep = JSONHelper.createJsonRepresentation(obj);
			}
		}
		
		return rep;
	}
	
	
}
