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
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultUser;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class TimesTarget extends Target {

	public static final String ID = "nytimes.com";
	public static final String BASEURL = "http://api.nytimes.com/svc/search/v1/article";
	public static final String APIKEY = "f2fe03601b7d67761bc8735c4b617aae:0:63972651";

	private String baseurl = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.baseurls.nytimes", BASEURL);

	private String apikey = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.apikeys.nytimes", APIKEY);

	private static Logger logger = Logger.getLogger(TimesTarget.class);

	public TimesTarget() {
		super();
		initializeMetaData();
	}

	public void initializeMetaData() {
		name = "NYTimes";
		url = "http://www.nytimes.com";
		mediaTypeIconUrl = "";
		description = "The New York Times official article database.";
		String[] mTypes = { MediaType.TEXT.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue("de.imc.advancedMediaSearch.iconurls.nytimes",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/nytimes.ico");
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
		return searchByFullTextQuery(tagQuery, args);
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
		return parseResponse(executeQuery(searchTermQuery, args),
				searchTermQuery);
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

	/**
	 * executes the query and returns the retrieved JSONObject
	 * 
	 * @param query
	 * @param args
	 * @return the JSON Response of the query
	 */
	private JSONObject executeQuery(String query, QueryArguments args) {

		// initialize client
		RESTHttpClient httpclient = new ApacheHttpClient();

		String urlString = baseurl + "?format=json&query="
				+ encodeQueryString(query) + "&api-key=" + apikey;

		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			logger.error("An error occured while constructing the query url: "
					+ e.getMessage());
		}

		try {
			return httpclient.executeGetURLJSONResponse(url);
		} catch (IOException e) {
			logger.error("An error occured while executing the http request: "
					+ e.getMessage());
			return null;
		}
	}

	private ResultSet parseResponse(JSONObject json, String searchQuery) {
		ResultSet results = new ResultSet();
		results.addSourceRepository(ID);
		results.setSearchQuery(searchQuery);

		if (json != null) {
			if (json.has("results")) {

				JSONArray resultsArray = null;
				try {
					resultsArray = json.getJSONArray("results");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (resultsArray != null) {
					for (int i = 0; i < resultsArray.length(); i++) {
						try {
							JSONObject curItem = resultsArray.getJSONObject(i);
							ResultEntity en = new ResultEntity();
							en.addLanguage("en");
							en.setMediaType(MediaType.TEXT);
							en.setSource(TimesTarget.ID);
							
							if (curItem.has("title")) {
								en.setTitle(curItem.getString("title"));
							}
							if (curItem.has("body")) {
								en.setDescription(curItem.getString("body"));
							}
							if (curItem.has("byline")) {
								en.addAuthor(new ResultUser("", "", curItem
										.getString("byline").substring(3), "",
										TimesTarget.ID));
							}
							if (curItem.has("date")) {
								en.setPublished(createDateFromString(curItem.getString("date")));
							}
							if (curItem.has("url")) {
								en.setUrl(curItem.getString("url"));
							}
							
							results.add(en);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							logger.debug(e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		}
		results = filterResult(results);
		return results;
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

	private Date createDateFromString(String d) {
		String yearStr = d.substring(0, 4);
		String monthStr = d.substring(4, 6);
		String dayStr = d.substring(6);

		Date date = new Date(0);
		int day = Integer.parseInt(dayStr);
		int month = Integer.parseInt(monthStr);
		int year = Integer.parseInt(yearStr);

		date.setDate(day);
		date.setMonth(month);
		date.setYear(year);
		return date;
	}

}
