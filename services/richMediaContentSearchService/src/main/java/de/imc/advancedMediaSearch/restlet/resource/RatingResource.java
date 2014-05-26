



package de.imc.advancedMediaSearch.restlet.resource;

import java.util.Enumeration;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import de.imc.advancedMediaSearch.properties.PropertyManager;
import de.imc.advancedMediaSearch.target.Aggregator;
import de.imc.advancedMediaSearch.target.AggregatorFactory;
import de.imc.advancedMediaSearch.target.ITunesTarget;


/** */
public class RatingResource extends BaseServerResource {

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		
		PropertyManager mg = new PropertyManager("advancedmediasearch.properties");
		Enumeration<Object> e = mg.getProperties().elements();
		while(e.hasMoreElements()) {
			System.out.println(e.nextElement().toString());
		}
		
		AggregatorFactory af = new AggregatorFactory();
		Aggregator a = null;
		a = af.createAggregator();

		a.addTarget(new ITunesTarget(30, 50));
		
		a.setQueryString("bike");
		a.setQueryType(QueryType.fullTextQuery);
		a.setMaxQueryResults(400);
		
		
		
		a.run();
		
		a.getId();
		
		System.out.println(a.toString());
		
	}
	
	@Get
	public Representation represent() {
		return new StringRepresentation("test!");
	}
	
}
