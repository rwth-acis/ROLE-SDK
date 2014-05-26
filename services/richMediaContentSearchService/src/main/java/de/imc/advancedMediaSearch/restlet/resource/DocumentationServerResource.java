/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.restlet.resource;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import de.imc.advancedMediaSearch.properties.AMSPropertyManager;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class DocumentationServerResource extends ServerResource {
	private static Logger logger = Logger
			.getLogger(DocumentationServerResource.class);
	private String docUrl = AMSPropertyManager
			.getInstance()
			.getStringValue(
					"de.imc.advancedMediaSearch.documentationurl",
					"http://sourceforge.net/apps/mediawiki/role-project/index.php?title=Web_Service_API:Media_Search");

	@Get
	public Representation represent() {
		return createHtmlRepresentation();
	}
	
	private Representation createHtmlRepresentation() {
		String html = "<html><head><title>Rich Content Media Search Service Documentation</title></head><body>";
		html += "<p align='center'>";
		html += "<h1>Rich Media Content Search Service Documentation</h1>";
		html += "<ul>";
		html += "<li><a href='" + docUrl + "'>Documentation Wiki</a></li>" ;
		html += "</ul>";
		html += "</p>";
		html += "</body></html>";
		
		StringRepresentation rep = new StringRepresentation(html,MediaType.TEXT_HTML);
		return rep;
	}

}
