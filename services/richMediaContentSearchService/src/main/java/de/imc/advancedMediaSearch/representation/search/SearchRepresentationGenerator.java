



package de.imc.advancedMediaSearch.representation.search;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

import de.imc.advancedMediaSearch.result.ResultSet;


/** */
public abstract class SearchRepresentationGenerator {
	
	/** */
	public abstract Representation generateRepresentation(ResultSet resultSet, ServerResource resource, Variant variant );

}
