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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultThumbnail;
import de.imc.advancedMediaSearch.result.ResultUser;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class GuardianTarget extends Target {

	public static final String ID = "guardian.co.uk";

	private static final String DEFAULTAPIURL = "http://content.guardianapis.com/";

	private String apiurl = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.baseurls.guardian", DEFAULTAPIURL);

	private static Logger logger = Logger.getLogger(GuardianTarget.class);

	public GuardianTarget() {
		super();
		initializeMetaData();
	}

	public void initializeMetaData() {
		name = "The Guardian";
		url = "http://www.guardian.co.uk";
		mediaTypeIconUrl = "";
		description = "The official article database of the 'The Guardian' newspaper.";
		String[] mTypes = { MediaType.TEXT.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue("de.imc.advancedMediaSearch.iconurls.guardian",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/guardian.ico");
	}

	private JSONObject executeQuery(String url, QueryArguments args)
			throws MalformedURLException, IOException {
		RESTHttpClient client = new ApacheHttpClient();
		return client.executeGetURLJSONResponse(new URL(url));
	}

	private ResultSet parseResults(JSONObject obj) {
		ResultSet results = new ResultSet();
		results.addSourceRepository(ID);

		if (obj == null) {
			return results;
		} else if (!obj.has("response")) {
			return results;
		}

		try {
			JSONObject resp = obj.getJSONObject("response");

			if (!resp.has("results")) {
				return results;
			}

			JSONArray resultsArray = resp.getJSONArray("results");

			for (int i = 0; i < resultsArray.length(); i++) {
				JSONObject curResult = resultsArray.getJSONObject(i);
				ResultEntity e = new ResultEntity();
				e.setSource(ID);
				e.addLanguage("en");
				e.setMediaType(MediaType.TEXT);
				e.setFormat("html");
				
				// parse curResult
				if (curResult.has("sectionName")) {
					e.addCategory(curResult.getString("sectionName"), ID);
				}
				if (curResult.has("webPublicationDate")) {
					//TODO: parse Date
				}
				if (curResult.has("webTitle")) {
					e.setTitle(curResult.getString("webTitle"));
				}
				if (curResult.has("webUrl")) {
					e.setUrl(curResult.getString("webUrl"));
				}

				//parse tags
				if(curResult.has("tags")) {
					JSONArray tags = curResult.getJSONArray("tags");
					for(int k=0; k<tags.length(); k++) {
						JSONObject curTag = tags.getJSONObject(k);
						if(curTag.has("webTitle")) {
							e.addTag(new ResultTag(curTag.getString("webTitle"), ID));
						}
					}
				}
				
				
				// parse fields object
				if (curResult.has("fields")) {
					JSONObject fields = curResult.getJSONObject("fields");
					
					if (fields.has("headline")) {
						e.setTitle(fields.getString("headline"));
					}
					if (fields.has("trailText")) {
						e.setDescription(fields.getString("trailText"));
					}
					if (fields.has("shortUrl")) {
						e.setUrl(fields.getString("shortUrl"));
					}
					if (fields.has("thumbnail")) {
						String tnurl = fields.getString("thumbnail");
						e.setThumbnail(new ResultThumbnail(0, 0, new URL(tnurl)));
					}
					if (fields.has("byline")) {
						e.addAuthor(new ResultUser(fields.getString("byline"),
								null, null, null, ID));
					}
					if (fields.has("lastModified")) {
						// TODO: parse Date
					}

				}

				if (e.getTitle() != null) {
					results.add(e);
				}
			}
			results = filterResult(results);
			return results;
		} catch (Exception e) {
			logger.debug(e.getMessage());
			return results;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByTags(java.lang.String,
	 * de.imc.advancedMediaSearch.target.QueryArguments)
	 */
	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {
		String url = apiurl + "tags?format=json&q="
				+ encodeQueryString(tagQuery);
		url += "&page-size=" + getMaxQueryResults();

		try {
			return parseResults(executeQuery(url, args));
		} catch (MalformedURLException e) {
			logger.debug(e.getMessage());
			ResultSet s = new ResultSet();
			s.addSourceRepository(ID);
			return s;
		} catch (IOException e) {
			logger.debug(e.getMessage());
			ResultSet s = new ResultSet();
			s.addSourceRepository(ID);
			return s;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByFullTextQuery(java.lang
	 * .String, de.imc.advancedMediaSearch.target.QueryArguments)
	 */
	@Override
	public ResultSet searchByFullTextQuery(String searchTermQuery,
			QueryArguments args) {
		String url = apiurl
				+ "search?format=json&show-tags=all&show-fields=all&q="
				+ encodeQueryString(searchTermQuery);
		url += "&page-size=" + getMaxQueryResults();
		
		//TODO: relevance parameter + api key usage

		try {
			return parseResults(executeQuery(url, args));
		} catch (MalformedURLException e) {
			logger.debug(e.getMessage());
			ResultSet s = new ResultSet();
			s.addSourceRepository(ID);
			return s;
		} catch (IOException e) {
			logger.debug(e.getMessage());
			ResultSet s = new ResultSet();
			s.addSourceRepository(ID);
			return s;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByAuthor(java.lang.String,
	 * de.imc.advancedMediaSearch.target.QueryArguments)
	 */
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
		return ID;
	}

}
