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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.imc.advancedMediaSearch.helpers.ConversionHelper;
import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.result.LearningList;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultThumbnail;

/**
 * This class represents the connection to the richMediaContentList Service of
 * the ROLE Project
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class LearningListTarget extends Target {

	private static Logger logger = Logger.getLogger(LearningListTarget.class);

	private static final String DEFAULT_BASEURL = "http://role-demo.de:8080/mediaListService/";

	private String baseurl = AMSPropertyManager.getInstance()
			.getStringValue("de.imc.advancedMediaSearch.baseurls.learninglist",
					DEFAULT_BASEURL);
	private String listurl = baseurl + "lists/";

	public static final String ID = "mediaListService";

	public void initializeMetaData() {
		name = "MediaListService";
		url = "http://role-demo.de:8080/mediaListService";
		mediaTypeIconUrl = ""; // TODO: insert url
		description = "The Media List Service provides learning lists on various topics.";
		String[] mTypes = { MediaType.LIST.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue(
						"de.imc.advancedMediaSearch.iconurls.learninglist",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/list.ico");
	}

	public LearningListTarget() {
		super();
		initializeMetaData();
	}

	public LearningListTarget(int maxDuration, int maxQueryResults) {
		super(maxDuration, maxQueryResults);
		initializeMetaData();
	}

	// TODO: Implement LearningListTarget functions
	/**
	 * IMPORTANT: add new Target to Aggregator initialization (query argument
	 * analysis, ...)
	 */

	private ResultSet executeQuery(String url) {
		RESTHttpClient client = new ApacheHttpClient();

		Date startDate = new Date();

		try {
			// execute http get
			Document doc = client.executeGETURL(new URL(url));
			// generate Results
			ResultSet s = parseXMLResponse(doc);

			// return results
			return s;
		} catch (MalformedURLException e) {
			logger.error("An exception occured while executing the query: "
					+ e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("An exception occured while executing the query: "
					+ e.getMessage());
			e.printStackTrace();
		}
		// return empty list on errors
		ResultSet set = new ResultSet();
		set.addSourceRepository(this.getUrl());
		set = filterResult(set);

		// calculate search time
		Date stopDate = new Date();
		long searchtime = stopDate.getTime() - startDate.getTime();
		logger.debug("mediaListService searchtime: " + searchtime);

		return set;

	}

	/**
	 * @param doc
	 * @return
	 */
	private ResultSet parseXMLResponse(Document doc) {
		if (doc == null) {
			// return empty list on null arguments
			ResultSet set = new ResultSet();
			set.addSourceRepository(this.getUrl());
			return set;
		}

		// initialize resultset
		ResultSet results = new ResultSet();
		results.addSourceRepository(this.getUrl());

		NodeList listsList = doc.getElementsByTagName("list");
		// iterate through list items
		for (int i = 0; i < listsList.getLength(); i++) {
			Node node = listsList.item(i);
			LearningList newList = new LearningList();
			newList.setSource(this.getUrl());

			// get item nodes child nodes
			NodeList nodesChilds = node.getChildNodes();

			// iterate through child nodes
			for (int j = 0; j < nodesChilds.getLength(); j++) {
				Node subNode = nodesChilds.item(j);
				String subNodeName = subNode.getNodeName().toLowerCase();

				// title
				if (subNodeName.equals("list_title")) {
					if (subNode != null) {
						if (subNode.getFirstChild() != null) {
							newList.setTitle(subNode.getFirstChild()
									.getNodeValue());
						}
					}
				}

				// id
				if (subNodeName.equals("list_id")) {
					if (subNode != null) {
						if (subNode.getFirstChild() != null) {
							String retrievedId = subNode.getFirstChild()
									.getNodeValue();
							newList.setId(ConversionHelper.convertToInt(
									retrievedId, 0, false));
							newList.setUrl(listurl
									+ ConversionHelper.convertToInt(
											retrievedId, 0, false));
						}
					}
				}

				// description
				if (subNodeName.equals("list_description")) {
					if (subNode != null) {
						if (subNode.getFirstChild() != null) {
							newList.setDescription(subNode.getFirstChild()
									.getNodeValue());
						}
					}
				}

			}

			// iterate through all items of the list here -> add them to the
			// learning list
			if (newList != null && newList.getId() != 0) {
				String itemString = "item" + newList.getId();

				// select all item(Nr of list) nodes
				NodeList itemNodes = doc.getElementsByTagName(itemString);

				for (int l = 0; l < itemNodes.getLength(); l++) {
					Node actualItemNode = itemNodes.item(l);
					NodeList itemSubNodes = actualItemNode.getChildNodes();

					ResultEntity myItem = new ResultEntity();

					for (int k = 0; k < itemSubNodes.getLength(); k++) {
						Node actualItemSubNode = itemSubNodes.item(k);
						String subNodeName = actualItemSubNode.getNodeName()
								.toLowerCase();

						if (subNodeName.equals("item_title")) {
							if (actualItemSubNode.getFirstChild() != null) {
								myItem.setTitle(actualItemSubNode
										.getFirstChild().getNodeValue());
								insertTagsBasedOnTitle(myItem);
							}
						}

						if (subNodeName.equals("item_description")) {
							if (actualItemSubNode.getFirstChild() != null) {
								myItem.setDescription(actualItemSubNode
										.getFirstChild().getNodeValue());
							}
						}

						if (subNodeName.equals("url_content")) {
							if (actualItemSubNode.getFirstChild() != null) {
								String urlString = actualItemSubNode
										.getFirstChild().getNodeValue();
								// prevent false linkage here
								if (!(urlString.startsWith("http")
										|| urlString.startsWith("https")
										|| urlString.startsWith("ftp") || urlString
										.startsWith("ftps"))) {
									urlString = "http://" + urlString;
								}
								myItem.setUrl(urlString);
							}
						}

						if (subNodeName.equals("image_url")) {
							if (actualItemSubNode.getFirstChild() != null) {
								try {
									ResultThumbnail tn = new ResultThumbnail(0,
											0, new URL(actualItemSubNode
													.getFirstChild()
													.getNodeValue()));
									myItem.setThumbnail(tn);
								} catch (Exception e) {

								}
							}
						}

					}
					newList.addEntity(myItem);

					// add tags from item to new list
					for (ResultTag t : myItem.getTags()) {
						newList.addTag(t);
					}
				}
			}

			results.add(newList);
		}
		results = filterResult(results);
		return results;
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
		/**
		 * use full text search here
		 */
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
		if (searchTermQuery == null || searchTermQuery.equals("")) {
			return null;
		}

		String escapedQueryString = encodeQueryString(searchTermQuery);
		String queryUrl = baseurl + "search/" + escapedQueryString;
		return executeQuery(queryUrl);
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
		/**
		 * use full text search here
		 */
		return searchByFullTextQuery(encodeQueryString(authorQuery), args);
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

	private void insertTagsBasedOnTitle(ResultEntity e) {
		if (e != null) {
			if (e.getTitle() != null && !e.getTitle().equals("")) {
				String[] splitted = e.getTitle().split(" ");
				for (String s : splitted) {
					e.addTag(new ResultTag(s));
				}
			}
		}
	}

}
