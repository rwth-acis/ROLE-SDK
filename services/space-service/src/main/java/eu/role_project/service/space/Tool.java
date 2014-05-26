package eu.role_project.service.space;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.GadgetHTMLProxy;
import eu.role_project.service.resource.OAuthEndpointSetup;
import eu.role_project.service.resource.ROLETerms;
import eu.role_project.service.resource.ServiceConfiguration;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class Tool extends ResourceResponder {

	private static Logger log = LoggerFactory.getLogger(Tool.class);

	public static final UUID user_root = UUID
			.fromString("4eed8e2e-4594-11e0-998e-705ab6abc097");

	@Inject
	private Map<String, ServiceConfiguration> configs;

	@Inject
	private SecurityInfo securityInfo;

	@Override
	public Object doGet(Request request) {
		boolean isOpenSocialGadget = null != store.getControls(request
				.getContext().getUuid(), ConserveTerms.type, UUID
				.fromString("9564bb4d-4dfe-59bc-b048-f6eb2bfe240f"));

		Concept toolContext = request.getContext();
		List<Control> reference = store.getControls(toolContext.getUuid(),
				ConserveTerms.reference);
		if (reference == null || reference.size() == 0 || !isOpenSocialGadget) {
			return super.doGet(request);
		}
		String refUri = reference.get(0).getUri();

		final String baseUri = ((RequestImpl) request).getUriInfo()
				.getBaseUri().toString();

		String gadgetBase = refUri;
		if (gadgetBase.contains("?")) {
			gadgetBase = gadgetBase.substring(0, gadgetBase.indexOf("?"));
		}
		if (gadgetBase.contains("/")) {
			gadgetBase = gadgetBase.substring(0,
					gadgetBase.lastIndexOf("/") + 1);
		}
		final String gadgetBaseUri = gadgetBase;

		final Concept context = store().get(toolContext.getContext());
		// final URI contextUri = Relation.getContextUri(context,
		// (RequestImpl) request).build();

		UUID agent = securityInfo.getAgent();
		final Concept user = agent != null ? store().in(user_root)
				.sub(ROLETerms.member).get(agent) : null;
		// final URI userUri = user != null ? Relation.getContextUri(user,
		// (RequestImpl) request).build() : null;

		String queryString = ((RequestImpl) request).getHttpServletRequest()
				.getQueryString();
		final Map<String, String> params = new HashMap<String, String>();
		if (queryString != null) {
			params.putAll(RequestImpl.parseQueryParams(queryString));
		}

		final OAuthEndpointSetup endpointSetup = new OAuthEndpointSetup(
				configs, context, user, params);
		// injector.injectMembers(endpointSetup);

		final StreamSource xmlSource = new StreamSource(refUri);
		final StreamSource xslSource = new StreamSource(getClass()
				.getClassLoader().getResourceAsStream("xsl/gadget.xsl"));
		StreamingOutput resultOutput = new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				StreamResult result = new StreamResult(output);
				TransformerFactory transFact = TransformerFactory.newInstance();
				Transformer trans;
				try {
					trans = transFact.newTransformer(xslSource);
					trans.setParameter("endpointSetup", endpointSetup);
					trans.setParameter("htmlProxy", new GadgetHTMLProxy());
					trans.setParameter("baseUri", baseUri);
					trans.setParameter("gadgetBaseUri", gadgetBaseUri);
					trans.transform(xmlSource, result);
				} catch (TransformerConfigurationException e) {
					log.error("Gadget XML transform error", e);
				} catch (TransformerException e) {
					log.error("Gadget XML transform error", e);
				}
			}
		};

		return Response.ok().type(MediaType.APPLICATION_XML_TYPE)
				.entity(resultOutput).header("Cache-Control", "no-store")
				.build();
	}
}