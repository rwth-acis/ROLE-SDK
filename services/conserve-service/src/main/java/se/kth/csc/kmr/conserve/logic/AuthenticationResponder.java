package se.kth.csc.kmr.conserve.logic;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractResponder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public class AuthenticationResponder extends AbstractResponder {

	static class AuthenticationTemplateResponder extends TemplateResponder {

		public AuthenticationTemplateResponder() {
			super("/ui/html/authentication.html");
		}

	}

	static class AuthenticationRDFResponder extends SystemResponder {

		@Override
		public void addTriples(Graph graph, Request request, Concept context,
				UriBuilder uriBuilder) {
			Concept c = context;
			List<Control> realms = null;
			for (;;) {
				realms = store().in(c).get(ConserveTerms.realm);
				if (realms != null && realms.size() > 0) {
					break;
				}
				if (c.getUuid().equals(c.getContext())) {
					break;
				}
				c = store.getConcept(c.getContext());
			}

			if (realms == null || realms.size() == 0) {
				return;
			}

			Concept realm = store.getConcept(realms.get(0).getObject());
			UriBuilder realmUri = store().in(realm).uriBuilder();

			if (!c.getUuid().equals(realm.getUuid())) {

				ValueFactory valueFactory = graph.getValueFactory();
				graph.add(
						valueFactory.createURI(uriBuilder.build().toString()),
						valueFactory.createURI("http://purl.org/openapp/",
								"realm"), valueFactory.createURI(realmUri
								.build().toString()));

			}

			super.addTriples(graph, request, realm, realmUri);
		}
	}

	@Inject
	private AuthenticationTemplateResponder templateResponder;

	@Inject
	private AuthenticationRDFResponder rdfResponder;

	@Override
	public Object doGet(Request request) {
		String accept = ((RequestImpl) request).getHttpServletRequest()
				.getHeader("Accept");
		if ("application/json".equals(accept)) {
			return rdfResponder.doGet(request);
		}
		return templateResponder.doGet(request);
	}

}
