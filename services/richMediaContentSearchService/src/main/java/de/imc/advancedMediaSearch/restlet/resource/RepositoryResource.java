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

import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.representation.json.JSONHelper;
import de.imc.advancedMediaSearch.representation.json.RepositoryJSONGenerator;
import de.imc.advancedMediaSearch.target.Aggregator;
import de.imc.advancedMediaSearch.target.AggregatorFactory;

/**
 * The repository resource is used to provide information about the available
 * search repositories
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class RepositoryResource extends BaseServerResource {

	private static Logger logger = Logger.getLogger(RepositoryResource.class);

	private String format = AMSPropertyManager.getInstance().getStringValue("de.imc.advancedMediaSearch.format", DEFAULT_FORMAT);

	private String callback;
	
	private String callUrl;

	@Override
	protected void doInit() throws ResourceException {

		//retrieving call url
		callUrl = getReference().getIdentifier();
		
		// retrieving parameters
		String formatString = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("format");
		if(formatString!=null) {
			format = formatString;
		}
		logger.debug("format= " + format);
		
		callback = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("callback");

		logger.debug("callback= " + callback);

	}

	/**
	 * Returns a Representation of the available search repositories
	 * 
	 * @return a Representation of the retrieved search results
	 */
	@Get
	public Representation represent() {

		AggregatorFactory af = new AggregatorFactory();
		Aggregator allTargetsAggregator = af.createAggregator();

		Representation rep = null;
		
		// check format variable
		if (format.equals(RepresentationFormat.atom.toString())) {
			// TODO: implement atom representation here
			rep = new StringRepresentation(
					"Atom feed format not implemented yet!", Language.DEFAULT);
		}
		// fallback to default json
		else {
			RepositoryJSONGenerator gen = new RepositoryJSONGenerator();
			JSONObject jsonlist = gen.generateJSONTargetListObject(allTargetsAggregator.getTargets(), callUrl);
			allTargetsAggregator = null;
			
			//wrap into callback function (jsonp)
			if(callback!=null) {
				rep = JSONHelper.createJsonPRepresentation(jsonlist, callback);
			} else {
				rep = JSONHelper.createJsonRepresentation(jsonlist);
			}
		}
		return rep;
	}
	
}
