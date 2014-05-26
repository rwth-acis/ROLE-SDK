/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.restlet.resource;


import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * represents the entry point to retrieve detail information about a
 * ResultEntity
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class EntityDetailResource extends BaseServerResource {
	
	@Override
	protected void doInit() throws ResourceException {
		//TODO: implement
	};
	
	@Get
	public Representation represent() {
		//TODO: implement
		return new StringRepresentation("Not implemented yet!");
	}
}
