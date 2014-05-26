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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.lom.LomHelper;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class IcoperTarget extends Target {

	public static final String ID = "icoper.org";

	// TODO:
	private static final String DEFAULTAPIURL = "http://oics.icoper.org/oics.atom";

	// TODO
	private String apiurl = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.baseurls.icoper", DEFAULTAPIURL);

	private static Logger logger = Logger.getLogger(IcoperTarget.class);

	public IcoperTarget() {
		super();
		initializeMetaData();
	}

	public void initializeMetaData() {
		name = "iCoper";
		url = "http://www.icoper.org";
		mediaTypeIconUrl = "";
		// TODO
		description = "ICOPER is a Best Practice Network co-funded by the eContentplus programme of the European Community. The 30-months-project started in September 2008 and has the mission to collect and further develop best practices for higher education tackling issues like creating learning designs and teaching methods, authoring content for re-use, transferring knowledge in an outcome-oriented way and assessing it, or evaluating learning activities.";
		String[] mTypes = { MediaType.TEXT.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue("de.imc.advancedMediaSearch.iconurls.icoper",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/icoper.ico");
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
		String url = apiurl;
		url += "?query=" + encodeQueryString(tagQuery);
		url += "&page_size=" + getMaxQueryResults();

		if (args.isLanguageSet()) {
			url += "&filter_expression=general.language=" + args.getLanguage();
		}
		url+="=&filter=general.keyword";
		
		return parseResult(executeQueryUrl(url), tagQuery);
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
		String url = apiurl;
		url += "?query=" + encodeQueryString(searchTermQuery);
		url += "&page_size=" + getMaxQueryResults();

		if (args.isLanguageSet()) {
			url += "&filter_expression=general.language=" + args.getLanguage();
		}
		return parseResult(executeQueryUrl(url), searchTermQuery);
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
		String url = apiurl;
		url += "?query=" + encodeQueryString(authorQuery);
		url += "&page_size=" + getMaxQueryResults();

		if (args.isLanguageSet()) {
			url += "&filter_expression=general.language=" + args.getLanguage();
		}
		url+="=&filter=lifeCycle.contribute";
		return parseResult(executeQueryUrl(url), authorQuery);
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

	private ResultSet parseResult(Document doc, String queryString) {
		ResultSet results = new ResultSet();
		results.addSourceRepository(ID);
		results.setSearchQuery(queryString);

		try {
			// get all entry nodes with xmlns=atom
			NodeList entries = doc.getElementsByTagName("entry");
			for (int i = 0; i < entries.getLength(); i++) {
				Node curEntry = entries.item(i);
				if (curEntry.getAttributes() != null) {
					if (curEntry.getAttributes().getNamedItem("xmlns") != null) {
						if (curEntry
								.getAttributes()
								.getNamedItem("xmlns")
								.getNodeValue()
								.equalsIgnoreCase("http://www.w3.org/2005/Atom")) {

							// we are in the correct entry node now

							Element curEntryElement = (Element) curEntry;

							// pick the metadata node

							if (curEntryElement
									.getElementsByTagName("metadata") != null) {
								Node metaDataNode = curEntryElement
										.getElementsByTagName("metadata").item(
												0);
								// switch to the lom node now
								Element metaDataElement = (Element) metaDataNode;
								if (metaDataElement.getElementsByTagName("lom") != null) {
									Node lomNode = metaDataElement
											.getElementsByTagName("lom")
											.item(0);

									// step into the lom main structure now
									// we are inside the lom main node now
									try {
										ResultEntity et = new ResultEntity();
										et.setSource(ID);
										NodeList lomNodes = lomNode
												.getChildNodes();
										// parse entries lom sub-nodes
										for (int j = 0; j < lomNodes
												.getLength(); j++) {
											Node lomSubNode = lomNodes.item(j);

											if (lomSubNode
													.getNodeName()
													.equalsIgnoreCase("general")) {
												et = LomHelper
														.parseLomGeneralNode(
																lomSubNode, et,
																ID);
											} else if (lomSubNode.getNodeName()
													.equalsIgnoreCase(
															"technical")) {
												et = LomHelper
														.parseLomTechnicalNode(
																lomSubNode, et);
											} else if (lomSubNode.getNodeName()
													.equalsIgnoreCase(
															"metametadata")) {
												et = LomHelper
														.parseLomMetaMetadataNode(
																lomSubNode, et,
																ID);
											} else if (lomSubNode.getNodeName()
													.equalsIgnoreCase(
															"lifecycle")) {
												et = LomHelper
														.parseLomLifecycleNode(
																lomSubNode, et,
																ID);
											}

										}
										results.add(et);

									} catch (Exception err) {
										logger.debug("An error occured while parsing: "
												+ err.getMessage());
									}

								}

							}
						}
					}
				}

				results = filterResult(results);
			}

		} catch (Exception e) {
			logger.debug("An error occured while parsing: " + e.getMessage());
			return results;
		}
		return results;
	}

	private Document executeQueryUrl(String url) {
		RESTHttpClient client = new ApacheHttpClient();
		try {
			return client.executeGETURL(new URL(url));
		} catch (MalformedURLException e) {
			logger.debug(e.getMessage());
			return null;
		} catch (IOException e) {
			logger.debug(e.getMessage());
			return null;
		}
	}

}
