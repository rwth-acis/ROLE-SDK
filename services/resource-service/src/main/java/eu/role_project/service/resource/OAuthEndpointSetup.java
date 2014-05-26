package eu.role_project.service.resource;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import se.kth.csc.kmr.conserve.Concept;

public class OAuthEndpointSetup {

	private static final Logger log = LoggerFactory
			.getLogger(OAuthEndpointSetup.class);

	private final Concept context;

	private final Concept user;

	private final Map<String, String> params;

	Map<String, ServiceConfiguration> configs;

	public OAuthEndpointSetup(Map<String, ServiceConfiguration> configs,
			Concept context, Concept user, Map<String, String> params) {
		this.configs = configs;
		this.context = context;
		this.user = user;
		this.params = params;
	}

	public String getRequestUrl(NodeIterator service, String url) {
		Map<String, String> attribs = getAttributes(service);
		ServiceConfiguration config = getConfig(service, attribs);
		return config != null ? config.getRequestUrl(this, attribs, context,
				user) : url;
	}

	public String getRequestMethod(NodeIterator service, String url) {
		Map<String, String> attribs = getAttributes(service);
		ServiceConfiguration config = getConfig(service, attribs);
		return config != null ? config.getRequestMethod(this, attribs, context,
				user) : url;
	}

	public String getAuthorizationUrl(NodeIterator service, String url) {
		Map<String, String> attribs = getAttributes(service);
		ServiceConfiguration config = getConfig(service, attribs);
		return config != null ? config.getAuthorizationUrl(this, attribs,
				context, user) : url;
	}

	public String getAccessUrl(NodeIterator service, String url) {
		Map<String, String> attribs = getAttributes(service);
		ServiceConfiguration config = getConfig(service, attribs);
		return config != null ? config.getAccessUrl(this, attribs, context,
				user) : url;
	}

	public String getAccessMethod(NodeIterator service, String url) {
		Map<String, String> attribs = getAttributes(service);
		ServiceConfiguration config = getConfig(service, attribs);
		return config != null ? config.getAccessMethod(this, attribs, context,
				user) : url;
	}

	private ServiceConfiguration getConfig(NodeIterator service,
			Map<String, String> attribs) {
		// Match service predicate in XML to service mapping in params
		return getConfig(attribs.get("service"));
	}

	public ServiceConfiguration getConfig(String servicePredicate) {
		// Match service predicate to service mapping in params
		String serviceInstance = params.get(servicePredicate);
		ServiceConfiguration config = configs.get(serviceInstance);
		if (config == null) {
			log.info("Service not found: " + serviceInstance);
		} else {
			log.info("Service found: " + serviceInstance);
		}
		return config;
	}

	private Map<String, String> getAttributes(NodeIterator service) {
		// Get attributes from the XML
		Map<String, String> attribs = new HashMap<String, String>();
		NamedNodeMap attribNodes = service.nextNode().getAttributes();
		for (int i = 0; i < attribNodes.getLength(); i++) {
			Node node = attribNodes.item(i);
			if ("http://www.role-project.eu/xml/openapp/opensocialext/"
					.equals(node.getNamespaceURI())) {
				attribs.put(node.getLocalName(), node.getTextContent());
			}
		}
		return attribs;
	}

}