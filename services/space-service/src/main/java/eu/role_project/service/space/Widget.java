package eu.role_project.service.space;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.logic.RepresentationResponder;
import se.kth.csc.kmr.conserve.util.TemplateLayout;
import se.kth.csc.kmr.conserve.util.TemplateManager;

public class Widget extends RepresentationResponder {

	@Inject
	@Named("space")
	private TemplateManager templates;

	@Override
	public Object doGet(Request request) {
		Concept space = request.getContext();
		Content metadata = store().in(space).as(ConserveTerms.metadata)
				.require();
		String spaceTitle = "Space";
		Graph metaGraph = store().as(metadata).graph();
		URI dcTitle = metaGraph.getValueFactory().createURI(
				"http://purl.org/dc/terms/title");
		for (Statement s : metaGraph) {
			if (dcTitle.equals(s.getPredicate())) {
				spaceTitle = s.getObject().stringValue();
			}
		}

		TemplateLayout layout = templates
				.layout("widget")
				.param("space.title", spaceTitle)
				.param("base.uri",
						((RequestImpl) request).getUriInfo().getBaseUri()
								.toString())
				.param("context.uri",
						store().in(request.getContext()).uri().toString());
		return Response.ok().entity(layout.render())
				.type(MediaType.APPLICATION_XML_TYPE)
				.header("Cache-Control", "no-store").build();
	}

}