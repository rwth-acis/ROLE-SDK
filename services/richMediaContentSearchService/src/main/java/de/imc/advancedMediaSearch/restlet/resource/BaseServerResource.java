/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.restlet.resource;

import org.restlet.resource.ServerResource;

/**
 * The Base Class for all Server Resources
 * @author julian.weber@im-c.de
 *
 */
public class BaseServerResource extends ServerResource {
	public static final String DEFAULT_FORMAT = RepresentationFormat.json.toString();
	
	
}	
