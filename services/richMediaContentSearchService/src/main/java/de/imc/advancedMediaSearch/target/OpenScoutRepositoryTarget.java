package de.imc.advancedMediaSearch.target;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.lom.LomHelper;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;

public class OpenScoutRepositoryTarget extends Target {

	private static Logger logger = Logger
			.getLogger(OpenScoutRepositoryTarget.class);

	private static String QUERY_LANGUAGE = "lucene";

	private static String RESULT_FORMAT = "lom";

	private static final String LOMPREFIX = "oai_lom:";

	private static final String DEFAULTTHUMBNAILBASEURL = "http://learn.openscout.net/thumbnails/";

	private static final String DEFAULTAPIURL = "http://monet.informatik.rwth-aachen.de/openscout-live/repositoryOpenScoutMetadata/api/sqitarget";

	private String apiurl = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.baseurls.openscout", DEFAULTAPIURL);

	public static String ThumbnailUrl = AMSPropertyManager.getInstance()
			.getStringValue(
					"de.imc.advancedMediaSearch.baseurls.openscout.thumbnail",
					DEFAULTTHUMBNAILBASEURL);

	public static final String ID = "openscout.net";

	public void initializeMetaData() {
		name = "OpenScout";
		url = "http://www.openscout.net";
		mediaTypeIconUrl = "";
		description = "OpenScout stands for Skill based scouting of open user-generated and community-improved content for management education and training. OpenScout aims at providing an education service in the internet that enables users to easily find, access, use and exchange open content for management education and training.";
		String[] mTypes = { MediaType.PRESENTATION.toString(),
				MediaType.TEXT.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue(
						"de.imc.advancedMediaSearch.iconurls.openscout",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/openscout.ico");
	}

	public OpenScoutRepositoryTarget() {
		super();

		// Initialize Meta Data
		initializeMetaData();
	}

	/**
	 * @param maxDuration
	 * @param maxQueryResults
	 */
	public OpenScoutRepositoryTarget(int maxDuration, int maxQueryResults) {
		super(maxDuration, maxQueryResults);

		// Initialize Meta Data
		initializeMetaData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByTags(java.lang.String)
	 */
	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {

		// create tag query
		String query = "lom.general.keyword.string:"
				+ encodeQueryString(tagQuery);
		query = addRestrictionsToQuery(query, args);

		logger.debug("created tag query: " + query);

		return executeQuery(query, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchBySearchTerm(java.lang
	 * .String)
	 */
	@Override
	public ResultSet searchByFullTextQuery(String searchTermQuery,
			QueryArguments args) {
		// create full term query
		String query = encodeQueryString(searchTermQuery);
		query = addRestrictionsToQuery(query, args);

		logger.debug("created fullterm query: " + query);

		return executeQuery(query, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByAuthor(java.lang.String)
	 */
	@Override
	public ResultSet searchByAuthor(String authorQuery, QueryArguments args) {

		// create author query
		String query = "lom.lifecycle.contribute.entity.author:"
				+ encodeQueryString(authorQuery);
		query = addRestrictionsToQuery(query, args);

		logger.debug("created author query: " + query);

		return executeQuery(query, args);
	}

	private String addRestrictionsToQuery(String query, QueryArguments args) {
		if (query == null || query.equals("")) {
			return "";
		}

		if (args == null) {
			return query;
		}

		//put paranthesis around query
		query = "(" + query + ")";
		
		if (args.isLanguageSet()) {
			// inserts paranthesis here
			query = query + "%20AND%20lom.general.language:"
					+ convertLanguageCode(args.getLanguage());
		}

		//just add type restriction, when restrictions are set in the queryargs and if they are not all types (<8)
		if (args.getMediaTypes() != null && args.getMediaTypes().size() > 0 && args.getMediaTypes().size() < 8) {
			query += "%20AND%20" + generateContentTypeQuery(args.getMediaTypes());
		}

		return query;
	}

	private String convertLanguageCode(String code) {
		if (code.equals("de")) {
			return "German";
		} else if (code.equals("nl")) {
			return "Netherlands";
		} else if (code.equals("cn")) {
			return "Chinese";
		} else if (code.equals("fr")) {
			return "French";
		} else if (code.equals("es") || code.equals("spanish")) {
			return "Spanish";
		} else if (code.equals("sv")) {
			return "Swedish";
		} else if (code.equals("fi")) {
			return "Finnish";
		} else if (code.equals("it")) {
			return "Italian";
		} else if (code.equals("po") || code.equals("or-br")
				|| code.equals("pt-bar")) {
			return "Portuguese";
		} else if (code.equals("chv")) {
			return "Cheyenne";
		} else if (code.equals("sl")) {
			return "Slovenian";
		} else {
			return "English";
		}
	}

	private String generateContentTypeQuery(List<MediaType> types) {
		String lomformatstr = "lom.technical.format:(";
		
		
		//remove list and application restrictions for openscout queries
		types.remove(MediaType.LIST);
		types.remove(MediaType.APPLICATION);
		
		//just one restriction for one type
		if(types.size()==1) {
			return lomformatstr + getOpenScoutMediaTypeForType(types.get(0)) + ")";
		}
		
		//multiple types
		else  {
			for(int i=0; i<types.size(); i++) {
				MediaType t = types.get(i);
				if(i==0) {
					lomformatstr += getOpenScoutMediaTypeForType(t);
				} else {
					lomformatstr += "%20OR%20" + getOpenScoutMediaTypeForType(t);
				}
			}
		}
		
		lomformatstr+=")";
		
		return lomformatstr;
	}

	private String getOpenScoutMediaTypeForType(MediaType t) {
		// application and list types are not mapped

		if (t == MediaType.AUDIO) {
			return "(audio%20OR%20podcast)";
		} else if (t == MediaType.IMAGE) {
			return "image";
		} else if (t == MediaType.PRESENTATION) {
			return "powerpoint";
		} else if (t == MediaType.TEXT) {
			return "(text%20OR%20dissertation%20OR%20html%20OR%20pdf%20OR%20word%20OR%20excel%20OR%20peer-reviewed%20OR%20interactive%20OR%20compressed%20OR%20interactive)";
		} else if (t == MediaType.VIDEO) {
			return "video";
		}
		// Unknown as default
		else {
			return "(unknown%20OR%20applefile)";
		}

	}

	/**
	 * @param query
	 * @return
	 */
	private ResultSet executeQuery(String query, QueryArguments args) {

		ResultSet result = new ResultSet();
		result.addSourceRepository(this.getUrl());

		Date startDate = new Date();

		String queryString = "query=" + query;

		String secondPart = queryString + "&start=1&size="
				+ this.getMaxQueryResults() + "&lang=" + QUERY_LANGUAGE
				+ "&format=" + RESULT_FORMAT;

		String targetURL = apiurl + "?" + secondPart;

		logger.debug("Try to query URL: " + targetURL);

		// RESTHttpClient httpclient = new RESTletHttpClient();
		// RESTHttpClient httpclient = new JakartaRESTHttpClient();
		RESTHttpClient httpclient = new ApacheHttpClient();

		try {
			result = parseDocument(httpclient.executeGETURL(new URL(targetURL)));
			if (result != null) {
				result.addSourceRepository(getUrl());
			}
		} catch (MalformedURLException e) {
			logger.error("Error: " + e.getMessage(), e);
		} catch (DOMException e) {
			logger.error("Error: " + e.getMessage(), e);
		} catch (ParseException e) {
			logger.error("Error: " + e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Error: " + e.getMessage(), e);
		}

		result = filterResult(result);

		Date endDate = new Date();
		searchTime = endDate.getTime() - startDate.getTime();

		logger.debug("OpenScoutTarget searchtime: " + searchTime);

		return result;
	}

	private ResultSet parseDocument(Document document)
			throws MalformedURLException, DOMException, ParseException {

		// return empty list on null argument
		if (document == null) {
			ResultSet s = new ResultSet();
			s.addSourceRepository(this.getUrl());
			return s;
		}

		logger.debug("Try to parse openscout result document!");

		// create a new resultset
		ResultSet result = new ResultSet();
		result.addSourceRepository(this.getUrl());

		// parse lom nodes
		NodeList entitynodes = document.getElementsByTagName("lom:lom");
		if (entitynodes != null) {
			for (int i = 0; i < entitynodes.getLength(); i++) {
				if (entitynodes.item(i) != null) {
					ResultEntity en = parseLomNode(entitynodes.item(i));
					if (en != null) {
						result.add(en);
					}
				}
			}
		}

		// parse oai_lom:lom nodes
		entitynodes = document.getElementsByTagName(LOMPREFIX + "lom");
		if (entitynodes != null) {
			for (int i = 0; i < entitynodes.getLength(); i++) {
				if (entitynodes.item(i) != null) {
					ResultEntity en = parseLomNode(entitynodes.item(i));
					if (en != null) {
						result.add(en);
					}
				}
			}
		}

		// parse lom nodes
		entitynodes = document.getElementsByTagName("lom");
		if (entitynodes != null) {
			for (int i = 0; i < entitynodes.getLength(); i++) {
				if (entitynodes.item(i) != null) {
					ResultEntity en = parseLomNode(entitynodes.item(i));
					if (en != null) {
						result.add(en);
					}
				}
			}
		}

		logger.debug("Found " + result.size() + " results for openscout.net!");

		return result;
	}

	private ResultEntity parseLomNode(Node n) {
		ResultEntity en = new ResultEntity();
		en.setSource(getId());

		NodeList categorynodes = n.getChildNodes();

		// iterate through all base lom categories
		for (int i = 0; i < categorynodes.getLength(); i++) {
			Node curCategory = categorynodes.item(i);
			// general
			if (curCategory.getNodeName().toLowerCase().endsWith("general")) {
				en = LomHelper.parseLomGeneralNode(curCategory, en, ID);
			}
			// technical
			else if (curCategory.getNodeName().toLowerCase()
					.endsWith("technical")) {
				en = LomHelper.parseLomTechnicalNode(curCategory, en);
			}
			// lifeCycle
			else if (curCategory.getNodeName().toLowerCase()
					.endsWith("lifecycle")) {
				en = LomHelper.parseLomLifecycleNode(curCategory, en, ID);
			}
			// metaMetadata
			else if (curCategory.getNodeName().toLowerCase()
					.endsWith("metametadata")) {
				en = LomHelper.parseLomMetaMetadataNode(curCategory, en, ID);
			}
		}
		return en;
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

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// * de.imc.advancedMediaSearch.target.Target#translateQueryString(java.lang
	// * .String)
	// */
	// public String translateQueryString(String queryString) {
	// //FIXME: Use no translation here -> pipe through
	//
	// queryString = queryString.trim();
	// queryString = capsulateQueryIntoParanthesis(queryString);
	// return queryString;
	// }
	//
	// private String capsulateQueryIntoParanthesis(String query) {
	// String[] splitted = query.split(" ");
	//
	// if (splitted.length < 1) {
	// return query;
	// }
	//
	// String ret = "(" + "\"" + splitted[0] + "\"";
	//
	// for (int i = 1; i < splitted.length; i++) {
	// String s = splitted[i];
	//
	// if (s.toLowerCase().equals("and") || s.toLowerCase().equals("or")) {
	// ret += " " + s + " ";
	// } else {
	// ret += " \"" + s + "\" ";
	// }
	// }
	// ret += ")";
	// return ret;
	// }
}
