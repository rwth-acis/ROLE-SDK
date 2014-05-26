/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.target;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.restlet.resource.SearchResource;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class GlobeTarget extends Target {

	private static final String baseapiurl = "http://ariadne.cs.kuleuven.be/GlobeFinderF1/servlet/search?engine=InMemory&json=";

	private String baseurl = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.baseurls.globe", baseapiurl);

	public static final String ID = "globe-info.net";
	private static Logger logger = Logger.getLogger(GlobeTarget.class);

	public GlobeTarget() {
		super();
		initializeMetaData();
	}

	public GlobeTarget(int maxDuration, int maxQueryResults) {
		super(maxDuration, maxQueryResults);
		initializeMetaData();
	}

	public void initializeMetaData() {
		name = "GLOBE";
		url = "http://www.globe-info.net";
		mediaTypeIconUrl = ""; // TODO: insert url
		description = "The Global Learning Objects Brokered Exchange (GLOBE) alliance contains organizations from around the world that together make shared online learning resources available to educators and students around the world.";
		String[] mTypes = { MediaType.VIDEO.toString(),
				MediaType.AUDIO.toString(), MediaType.PRESENTATION.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue("de.imc.advancedMediaSearch.iconurls.globe",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/globe.ico");
	}

	/**
	 * returns the same results as searchByFullTextQuery for GLOBE target
	 */
	// TODO: implement tag Search
	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {
		return searchByFullTextQuery(tagQuery, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByFullTextQuery(java.lang
	 * .String)
	 */
	@Override
	public ResultSet searchByFullTextQuery(String searchTermQuery,
			QueryArguments args) {
		JSONObject query = generateQueryObject(searchTermQuery, null,
				getMaxQueryResults(), args);
		String queryJsonString = query.toString();
		queryJsonString = encodeQueryString(queryJsonString);
		URL url = null;
		try {
			url = new URL(baseurl + queryJsonString);
		} catch (MalformedURLException e) {
			logger.error("An error occured while constructing the query url: "
					+ e.getMessage());
			return parseResponse(null);
		}

		ApacheHttpClient httpClient = new ApacheHttpClient();
		
		JSONObject responseObject;
		try {
			responseObject = httpClient.executeGetURLJSONResponse(url);
		} catch (IOException e) {
			logger.error("An error occured while executing the request: " + e.getMessage());
			return parseResponse(null);
		}
		return parseResponse(responseObject);
	}

	/**
	 * returns the same results as searchByFullTextQuery for GLOBE target
	 */
	// TODO: implement Author Search
	@Override
	public ResultSet searchByAuthor(String authorQuery, QueryArguments args) {
		return searchByFullTextQuery(authorQuery, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.imc.advancedMediaSearch.target.Target#getId()
	 */
	@Override
	public String getId() {
		return GlobeTarget.ID;
	}

	private JSONObject generateQueryObject(String query,
			List<String> languages, int maxResults, QueryArguments args) {
		JSONObject o = new JSONObject();

		// {
		// "clause": [
		// {"language":"VSQL","expression":"university"},
		// {"language":"anyOf","expression":"language:de,es,en,fr"},
		// {"language":"anyOf","expression":"lrt:simulation"}
		// ],
		// "uiLanguage":"en",
		// "preferredLanguages":[],
		// "facets":["language","format","context","lrt"],
		// "resultInfo":"display",
		// "resultListOffset":0,
		// "resultListSize":12,
		// "idListOffset":0,
		// "idListSize":12,
		// "resultFormat":"json",
		// "resultSortkey":""
		// }

		try {
			List<JSONObject> clauses = new ArrayList<JSONObject>();

			o.put("resultFormat", "json");
			o.put("idListSize", maxResults);
			o.put("idListOffset", 0);
			o.put("resultListSize", maxResults);
			o.put("resultListOffset", 0);
			o.put("resultInfo", "display");

			if (args != null
					&& args.getLanguage() != null
					&& !args.getLanguage().equals(
							SearchResource.DEFAULT_LANGUAGE)) {
				String[] lang = { args.getLanguage() };
				o.put("PreferredLanguage", lang);
				o.put("uiLanguage", args.getLanguage());
				String langstring = "language:" + args.getLanguage();
				clauses.add(generateClause("anyOf", langstring));

			} else {
				o.put("uiLanguage", "en");
			}

			String[] facets = { "language", "format", "context", "lrt" };
			o.put("facets", facets);

			clauses.add(generateClause("VSQL", query));

			o.put("clause", clauses);
		} catch (JSONException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
		return o;
	}

	private JSONObject generateClause(String language, String expression) {
		JSONObject o = new JSONObject();
		try {
			o.put("expression", expression);
			o.put("language", language);
		} catch (JSONException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * parses the response jsonobject to a ResultSet
	 * 
	 * @param response
	 * @return
	 */
	private ResultSet parseResponse(JSONObject response) {
		ResultSet s = new ResultSet();
		s.addSourceRepository(getUrl());

		// return empty ResultSet on errors
		if (response == null) {
			return s;
		} else {
			try {
				JSONObject resultObj = response.getJSONObject("result");
				JSONArray metaDataArray = resultObj.getJSONArray("metadata");
				// iterate through items
				for (int i = 0; i < metaDataArray.length(); i++) {
					JSONObject aItem = metaDataArray.getJSONObject(i);
					// parse items
					// title
					String title = null;
					if (aItem.has("title")) {
						title = aItem.getString("title");
						title = StringEscapeUtils.unescapeHtml(title);
					}
					// description
					String description = null;
					if (aItem.has("description")) {
						description = aItem.getString("description");
						description = StringEscapeUtils
								.unescapeHtml(description);
					}
					// keywords
					// parse keywords
					List<ResultTag> tags = new ArrayList<ResultTag>();
					if (aItem.has("keywords")) {
						String keywords = aItem.getString("keywords");
						keywords = keywords.replace(" ", "");
						String[] words = keywords.split(";");
						for (String w : words) {
							if (w != null && !w.equals("")) {
								w = StringEscapeUtils.unescapeHtml(w);
								w = w.replace("&#044", "");
								ResultTag t = new ResultTag(w, GlobeTarget.ID);
								tags.add(t);
							}
						}
					}

					// location
					String source = null;
					if (aItem.has("location")) {
						source = aItem.getString("location");
					}

					// create and add resultEntity from information
					ResultEntity en = new ResultEntity();
					en.setSource(GlobeTarget.ID);
					en.setTitle(title);
					en.setDescription(description);
					en.setTags(tags);
					en.setUrl(source);
					s.add(en);
				}

			} catch (JSONException e) {
				logger.error("An error occured while parsing the JSON object: "
						+ e.getMessage());
			}

			s = filterResult(s);
			return s;
		}
	}

}
