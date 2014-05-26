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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.imc.advancedMediaSearch.helpers.ConversionHelper;
import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.restlet.resource.SearchResource;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.MimeType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultThumbnail;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class WikipediaTarget extends Target {

	public static final String ID = "wikipedia.com";
	private static Logger logger = Logger.getLogger(WikipediaTarget.class);
	private String language = "en";

	public WikipediaTarget() {
		super();
		initializeMetaData();
	}

	public WikipediaTarget(int maxDuration, int maxQueryResults) {
		super(maxDuration, maxQueryResults);
		initializeMetaData();
	}

	public void initializeMetaData() {
		name = "Wikipedia";
		url = "http://www.wikipedia.com";
		mediaTypeIconUrl = ""; // TODO: insert icon address
		description = "Wikipedia is a free, web-based, collaborative, multilingual encyclopedia project supported by the non-profit Wikimedia Foundation.";
		String[] mTypes = { MediaType.TEXT.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
		.getInstance()
		.getStringValue(
				"de.imc.advancedMediaSearch.iconurls.wikipedia",
				"http://role-demo.de:8080/richMediaContentSearchResources/icons/wikipedia.ico");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByTags(java.lang.String)
	 */
	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {
		//returning the same results as the fulltextquery
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
	public ResultSet searchByFullTextQuery(String searchTermQuery, QueryArguments args) {
		if (searchTermQuery == null || searchTermQuery.equals("")) {
			return null;
		}
		
		if(args!=null && args.getLanguage()!=null && !args.getLanguage().equals(SearchResource.DEFAULT_LANGUAGE)) {
			setLanguage(args.getLanguage());
		}
		
		String queryurl = "http://"
				+ getLanguage()
				+ ".wikipedia.org/w/api.php?action=opensearch&format=xml&search=";
		String queryString = encodeQueryString(searchTermQuery);
		if (this.getMaxQueryResults() > 0) {
			queryString += "&limit=" + getMaxQueryResults();
		}
		queryurl += queryString;
		return executeQuery(queryurl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByAuthor(java.lang.String)
	 */
	@Override
	public ResultSet searchByAuthor(String authorQuery, QueryArguments args) {
		//returning the same results as the fulltextquery
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

	private ResultSet executeQuery(String url) {
		RESTHttpClient client = new ApacheHttpClient();
		
		Date startDate = new Date();
		
		try {
			//execute http get
			Document doc = client.executeGETURL(new URL(url));
			//generate Results
			ResultSet s = parseXMLResponse(doc);
			
			
			
			//return results
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
		
		//calculate search time
		Date stopDate = new Date();
		long searchtime = stopDate.getTime() - startDate.getTime();
		logger.debug("WikipediaTarget searchtime: " + searchtime);
		
		return set;
	}

	private ResultSet parseXMLResponse(Document response) {
		if (response == null) {
			// return empty list on null arguments
			ResultSet set = new ResultSet();
			set.addSourceRepository(this.getUrl());
			return set;
		}

		// initialize resultset
		ResultSet results = new ResultSet();
		results.addSourceRepository(this.getUrl());

		NodeList itemList = response.getElementsByTagName("Item");
		// iterate through all items
		for (int i = 0; i < itemList.getLength(); i++) {
			Node node = itemList.item(i);
			// create a new resultentity to store the information
			ResultEntity entity = new ResultEntity();
			entity.setMediaType(MediaType.TEXT);
			entity.setMimeType(MimeType.text);
			entity.setSource(WikipediaTarget.ID);

			// get item nodes child nodes
			NodeList nodesChilds = node.getChildNodes();

			// iterate through child nodes
			for (int j = 0; j < nodesChilds.getLength(); j++) {
				Node subNode = nodesChilds.item(j);
				String subNodeName = subNode.getNodeName().toLowerCase();

				// title
				if (subNodeName.equals("text")) {
					Node n = subNode.getFirstChild();
					if (n != null) {
						entity.setTitle(n.getNodeValue());
					}
				}
				// description
				else if (subNodeName.equals("description")) {
					Node n = subNode.getFirstChild();
					if (n != null) {
						entity.setDescription(n.getNodeValue());
					}
				}
				// url
				else if (subNodeName.equals("url")) {
					Node n = subNode.getFirstChild();
					if (n != null) {
						entity.setUrl(n.getNodeValue());
					}
				}
				// preview image
				else if (subNodeName.equals("image")) {
					NamedNodeMap attributes = subNode.getAttributes();
					if (attributes != null) {
						ResultThumbnail th = new ResultThumbnail();

						Node sourceAttribute = attributes
								.getNamedItem("source");
						Node widthAttribute = attributes.getNamedItem("width");
						Node heightAttribute = attributes
								.getNamedItem("height");

						if (sourceAttribute != null) {
							try {
								th.setUrl(new URL(sourceAttribute
										.getNodeValue()));
							} catch (MalformedURLException e) {
								logger.error("An error occured while parsing the thumbnails url from xml: "
										+ e.getMessage());
							} catch (DOMException e) {
								logger.error("An error occured while parsing the thumbnails url from xml: "
										+ e.getMessage());
							}
						}

						if (widthAttribute != null) {
							th.setWidth(ConversionHelper.convertToInt(
									widthAttribute.getNodeValue(), 0));
						}

						if (heightAttribute != null) {
							th.setHeight(ConversionHelper.convertToInt(
									heightAttribute.getNodeValue(), 0));
						}
						
						//only set initialized thumbnails
						if (th.getUrl() != null) {
							entity.setThumbnail(th);
						}

					}
				}
			}
			results.add(entity);
		}
		results = filterResult(results);
		return results;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

}
