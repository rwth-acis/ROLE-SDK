package eu.role_project.service.resource;

import java.util.Map;

import se.kth.csc.kmr.conserve.Concept;

public interface ServiceConfiguration {

	String getRequestUrl(OAuthEndpointSetup setup, Map<String, String> attribs,
			Concept context, Concept user);

	String getRequestMethod(OAuthEndpointSetup setup,
			Map<String, String> attribs, Concept context, Concept user);

	String getAuthorizationUrl(OAuthEndpointSetup setup,
			Map<String, String> attribs, Concept context, Concept user);

	String getAccessUrl(OAuthEndpointSetup setup, Map<String, String> attribs,
			Concept context, Concept user);

	String getAccessMethod(OAuthEndpointSetup setup,
			Map<String, String> attribs, Concept context, Concept user);

	String getOpenID(OAuthEndpointSetup setup, Concept user);

}
