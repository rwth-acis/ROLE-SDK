package eu.role_project.service.shindig;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.servlet.RequestScoped;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Guard;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.core.AbstractResponder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

@RequestScoped
public class RaveResponder extends AbstractResponder implements Guard {

	private static Logger log = LoggerFactory.getLogger(RaveResponder.class);

	public static final UUID FakeID = UUID
			.fromString("baf3928b-061b-4d30-a0dc-dc07a44fa37b");

	private StringBuilder sb = new StringBuilder();

	@Inject
	private SecurityInfo securityInfo;

	@Override
	public boolean canPut(Request request) {
		return true;
	}

	@Override
	public boolean canPost(Request request) {
		return true;
	}

	@Override
	public boolean canGet(Request request) {
		return true;
	}

	@Override
	public boolean canDelete(Request request) {
		return true;
	}

	@Override
	public Object doGet(Request request) {
		return Response.ok().type(MediaType.TEXT_PLAIN_TYPE)
				.entity("rave-get:" + sb.toString()).build();
	}

	@Override
	public Object doPut(Request request, byte[] data) {
		if (securityInfo.getAgent() == null) {
			// Ignore request
			return Response.ok().build();
		}
		Concept context = null;
		for (Resolution resolution : request.getResolutionPath()) {
			if (!resolution.getType().equals(Resolution.StandardType.ROOT)
					&& !resolution.getType().equals(
							Resolution.StandardType.CONTEXT)) {
				break;
			}
			context = resolution.getContext();
		}
		if (context == null
				|| !(context.getPredicate().equals(ROLETerms.space) || context
						.getPredicate().equals(ROLETerms.member))) {
			return Response.status(Status.FORBIDDEN).build();
		}

		String query = sb.toString();

		Pattern putPreferencePattern = Pattern
				.compile("/api/rest/regionWidgets/(\\d+)/preferences/([^/]+)");
		Matcher putPreferenceMatcher = putPreferencePattern.matcher(query);

		Pattern putPreferencesPattern = Pattern
				.compile("/api/rest/regionWidgets/(\\d+)/preferences");
		Matcher putPreferencesMatcher = putPreferencesPattern.matcher(query);

		if (putPreferenceMatcher.matches()) {
			long queryModuleId = Long.valueOf(putPreferenceMatcher.group(1));
			log.info("Put preference for widget " + queryModuleId);
			for (Concept tool : store().in(context).sub(ROLETerms.tool).list()) {
				long moduleId = Math.abs(tool.getUuid()
						.getMostSignificantBits()
						^ tool.getUuid().getLeastSignificantBits());
				if (moduleId == queryModuleId) {
					Concept toolAnnotations;
					if ((context.getPredicate().equals(ROLETerms.space) && isOwner(context))
							|| (context.getPredicate().equals(ROLETerms.member) && securityInfo
									.getAgent().equals(context.getUuid()))) {
						toolAnnotations = tool;
					} else {
						toolAnnotations = store().in(securityInfo.getAgent())
								.sub(ConserveTerms.annotates)
								.acquire(store().in(tool).uri().toString());
					}
					Concept preferencesAnnotation = store().in(toolAnnotations)
							.sub(ROLETerms.preferences).acquire("preferences");
					Content preferencesContent = store()
							.in(preferencesAnnotation)
							.as(ConserveTerms.representation).get();

					JSONObject preferences = null;
					if (preferencesContent != null) {
						try {
							preferences = new JSONObject(store().as(
									preferencesContent).string());
						} catch (JSONException e) {
						}
					}
					if (preferences == null) {
						preferences = new JSONObject();
					}

					JSONObject newPreference;
					try {
						newPreference = new JSONObject(new String(data,
								Charset.forName("UTF-8")));
					} catch (JSONException e) {
						return Response.status(Status.BAD_REQUEST)
								.entity("Invalid JSON in body").build();
					}

					try {
						preferences.put(newPreference.getString("name"),
								newPreference.getString("value"));
					} catch (JSONException e) {
						throw new RuntimeException(e);
					}

					store().in(preferencesAnnotation)
							.as(ConserveTerms.representation)
							.type(MediaType.APPLICATION_JSON)
							.string(preferences.toString());

					return Response.ok().build();
				}
			}
			return Response.status(Status.BAD_REQUEST)
					.entity("Widget not found").build();

		} else if (putPreferencesMatcher.matches()) {
			long queryModuleId = Long.valueOf(putPreferencesMatcher.group(1));
			log.info("Put preferences for widget " + queryModuleId);
			for (Concept tool : store().in(context).sub(ROLETerms.tool).list()) {
				long moduleId = Math.abs(tool.getUuid()
						.getMostSignificantBits()
						^ tool.getUuid().getLeastSignificantBits());
				if (moduleId == queryModuleId) {
					Concept toolAnnotations;
					if ((context.getPredicate().equals(ROLETerms.space) && isOwner(context))
							|| (context.getPredicate().equals(ROLETerms.member) && securityInfo
									.getAgent().equals(context.getUuid()))) {
						toolAnnotations = tool;
					} else {
						toolAnnotations = store().in(securityInfo.getAgent())
								.sub(ConserveTerms.annotates)
								.acquire(store().in(tool).uri().toString());
					}
					Concept preferencesAnnotation = store().in(toolAnnotations)
							.sub(ROLETerms.preferences).acquire("preferences");
					Content preferencesContent = store()
							.in(preferencesAnnotation)
							.as(ConserveTerms.representation).get();

					JSONObject preferences = null;
					if (preferencesContent != null) {
						try {
							preferences = new JSONObject(store().as(
									preferencesContent).string());
						} catch (JSONException e) {
						}
					}
					if (preferences == null) {
						preferences = new JSONObject();
					}

					JSONObject newPreferences;
					try {
						newPreferences = new JSONObject(new String(data,
								Charset.forName("UTF-8")));
					} catch (JSONException e) {
						return Response.status(Status.BAD_REQUEST)
								.entity("Invalid JSON in body").build();
					}

					try {
						JSONArray preferencesArray = newPreferences
								.getJSONArray("preferences");
						for (int i = 0; i < preferencesArray.length(); i++) {
							JSONObject newPreference = preferencesArray
									.getJSONObject(i);
							preferences.put(newPreference.getString("name"),
									newPreference.getString("value"));
						}
					} catch (JSONException e) {
						throw new RuntimeException(e);
					}

					store().in(preferencesAnnotation)
							.as(ConserveTerms.representation)
							.type(MediaType.APPLICATION_JSON)
							.string(preferences.toString());

					return Response.ok().build();
				}
			}
			return Response.status(Status.BAD_REQUEST)
					.entity("Widget not found").build();
		}

		return Response.ok().type(MediaType.TEXT_PLAIN_TYPE)
				.entity("rave-post:" + sb.toString()).build();
	}

	@Override
	public Object doPost(Request request, byte[] data) {
		if (securityInfo.getAgent() == null) {
			// Ignore request
			return Response.ok().build();
		}
		Concept context = null;
		for (Resolution resolution : request.getResolutionPath()) {
			if (!resolution.getType().equals(Resolution.StandardType.ROOT)
					&& !resolution.getType().equals(
							Resolution.StandardType.CONTEXT)) {
				break;
			}
			context = resolution.getContext();
		}
		if (context == null
				|| !(context.getPredicate().equals(ROLETerms.space) || context
						.getPredicate().equals(ROLETerms.member))) {
			return Response.status(Status.FORBIDDEN).build();
		}

		String query = sb.toString();

		Pattern deleteWidgetPattern = Pattern
				.compile("/api/rpc/page/regionWidget/(\\d+)/delete");
		Matcher deleteWidgetMatcher = deleteWidgetPattern.matcher(query);

		Pattern addPagePattern = Pattern.compile("/api/rpc/page/add");
		Matcher addPageMatcher = addPagePattern.matcher(query);

		if (deleteWidgetMatcher.matches()) {
			long queryModuleId = Long.valueOf(deleteWidgetMatcher.group(1));
			log.info("Delete widget " + queryModuleId);
			for (Concept tool : store().in(context).sub(ROLETerms.tool).list()) {
				long moduleId = Math.abs(tool.getUuid()
						.getMostSignificantBits()
						^ tool.getUuid().getLeastSignificantBits());
				if (moduleId == queryModuleId) {
					store.deleteConcept(tool);
					return Response.ok().build();
				}
			}
			return Response.status(Status.BAD_REQUEST)
					.entity("Widget not found").build();

		} else if (addPageMatcher.matches()) {
			String form = new String(data, Charset.forName("UTF-8"));
			Map<String, String> params = Maps.newHashMap();
			for (String param : form.split("&")) {
				String[] pair = param.split("=", 2);
				if (pair.length == 2) {
					params.put(decodeURIComponent(pair[0]),
							decodeURIComponent(pair[1]));
				}
			}

			String pageName = params.get("pageName");
			String pageLayoutCode = params.get("pageLayoutCode");
			Concept page = store().in(context).sub(ROLETerms.activity)
					.acquire(pageName);

			JSONObject response = new JSONObject();
			JSONObject result = new JSONObject();
			try {
				result.put("id", pageName);
				response.put("result", result);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}

			return Response.ok().type(MediaType.APPLICATION_JSON_TYPE)
					.entity(response.toString()).build();
		}

		return Response.ok().type(MediaType.TEXT_PLAIN_TYPE)
				.entity("rave-post:" + sb.toString()).build();
	}

	private boolean isOwner(Concept space) {
		UUID agent = securityInfo.getAgent();
		if (agent == null) {
			return false;
		}
		// String agentUri =
		// store().in(store.getConcept(agent)).uri().toString();
		for (Concept sub : store.getConcepts(space.getUuid())) {
			if (ConserveTerms.owner.equals(sub.getPredicate())) {
				log.info("Found owner");
				List<Control> references = store.getControls(sub.getUuid(),
						ConserveTerms.reference);
				if (references == null || references.size() == 0) {
					continue;
				}
				if (agent.equals(references.get(0).getObject())) {
					return true;
				}
			}
		}
		return false;
	}

	private static String decodeURIComponent(String comp) {
		try {
			return URLDecoder.decode(comp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object doDelete(Request request) {
		return Response.ok().type(MediaType.TEXT_PLAIN_TYPE)
				.entity("rave-delete:" + sb.toString()).build();
	}

	@Override
	public Resolution resolve(Request request) {
		sb.append("/");
		sb.append(((RequestImpl) request).getId().getPath());
		return new Resolution(Resolution.StandardType.CONTEXT,
				store.createConcept(FakeID, ConserveTerms.hasPart, FakeID));
	}

}