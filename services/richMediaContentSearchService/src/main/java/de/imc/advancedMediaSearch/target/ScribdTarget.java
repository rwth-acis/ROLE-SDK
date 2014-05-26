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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.imc.advancedMediaSearch.helpers.ConversionHelper;
import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.restlet.resource.SearchResource;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultPreview;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultThumbnail;
import de.imc.advancedMediaSearch.result.ResultUser;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class ScribdTarget extends Target {

	public static final String ID = "scribd.com";

	private final String APIURL = "http://api.scribd.com/api";
	private final String APIKEY = "2wt5v6xowe5ob86zhac47";

	private String apiurl = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.baseurls.scribd", APIURL);

	private String apikey = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.apikeys.scribd", APIKEY);

	private static Logger logger = Logger.getLogger(ScribdTarget.class);

	public ScribdTarget() {
		super();
		initializeMetaData();
	}

	public void initializeMetaData() {
		name = "Scribd";
		url = "http://www.scribd.com";
		mediaTypeIconUrl = "";
		description = "Scribd provides access to documents in different formats, like pdf, doc, ....";
		String[] mTypes = { MediaType.TEXT.toString(), MediaType.PRESENTATION.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue("de.imc.advancedMediaSearch.iconurls.scribd",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/scribd.ico");
	}

	private ResultSet parseResults(Document doc, String query) {
		ResultSet results = new ResultSet();
		results.setSearchQuery(query);
		results.addSourceRepository(ID);

		if (doc != null) {
			NodeList resultNodes = doc.getElementsByTagName("result");
			for (int i = 0; i < resultNodes.getLength(); i++) {
				ResultEntity en = new ResultEntity();
				en.setMediaType(MediaType.TEXT);
				en.setSource(ID);
				String accesskey = null;
				String docid = null;  
				
				Node curNode = resultNodes.item(i);

				// get current Nodes childnodes to retrieve data
				NodeList childNodes = curNode.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node curChild = childNodes.item(j);

					if (curChild.getNodeType() == Node.ELEMENT_NODE) {
						String curChildName = curChild.getNodeName();

						// title node
						if (curChildName.equals("title")) {
							String title = getCDataValueFromChildNodes(curChild);
							en.setTitle(title);
						}
						// description node
						else if (curChildName.equals("description")) {
							String desc = getCDataValueFromChildNodes(curChild);
							en.setDescription(desc);
						}
						// page count node
						else if (curChildName.equals("page_count")) {
							en.setLength(ConversionHelper.convertToInt(curChild
									.getFirstChild().getNodeValue(), 0));
						}
						// doc id node
						else if (curChildName.equals("doc_id")) {
							en.setUrl("http://www.scribd.com/doc/"
									+ curChild.getFirstChild().getNodeValue());
							docid = curChild.getFirstChild().getNodeValue();
						}
						// uploaded node
						else if (curChildName.equals("when_uploaded")) {
							// TODO: implement date conversion
						}
						// tags
						else if (curChildName.equals("tags")) {
							String tagString = getCDataValueFromChildNodes(curChild);
							String[] tags = tagString.split(",");
							for (String s : tags) {
								en.addTag(new ResultTag(s));
							}
						}
						// thumbnail
						else if (curChildName.equals("thumbnail_url")) {
							String turl = curChild.getFirstChild().getNodeValue();
							
							if (turl != null) {
								try {
									en.setThumbnail(new ResultThumbnail(0, 0,
											new URL(turl)));
								} catch (MalformedURLException e) {
									logger.debug(e.getMessage());
								}
							}
						}
						// number of reads
						else if (curChildName.equals("reads")) {
							en.setViewCount(ConversionHelper.convertToInt(
									curChild.getFirstChild().getNodeValue(), 0));
						}
						// uploader
						else if (curChildName.equals("uploaded_by")) {
							String uname = getCDataValueFromChildNodes(curChild);
							en.setUploader(new ResultUser("", "", uname,
									"http://www.scribd.com/" + uname, ID));
						}
						else if (curChildName.equals("access_key")) {
							accesskey = curChild.getFirstChild().getNodeValue();
						}
					}
				}
				
				//add preview 
				if(accesskey!=null) {
					ResultPreview prev = new ResultPreview();
					prev.setAvailable(true);
					prev.setSource(ID);
					String embedCode = "<iframe class='scribd_iframe_embed' src='http://www.scribd.com/embeds/" + docid + "/content?start_page=1&view_mode=list&access_key='" + accesskey + "' data-auto-height='false' data-aspect-ratio='' scrolling='no' width='340' height='300' frameborder='0'></iframe>";
					prev.setEmbeddableHtml(embedCode);
					en.setPreview(prev);
				}
				
				//add result to results array
				results.add(en);

			}
			
			results = filterResult(results);
			
			return results;
		} else {
			return results;
		}
	}

	private String getCDataValueFromChildNodes(Node curChild) {
		NodeList nlist = curChild.getChildNodes();
		String ret = "";
		for (int l = 0; l < nlist.getLength(); l++) {
			Node cn = nlist.item(l);
			if (cn.getNodeType() == Node.CDATA_SECTION_NODE) {
				ret = cn.getNodeValue();
				break;
			}
		}
		return ret;
	}

	private Document executeFullTextQuery(String query, QueryArguments args)
			throws MalformedURLException, IOException {
		String url = apiurl + "?method=docs.search&api_key=" + apikey
				+ "&scope=all" + "&query=" + encodeQueryString(query);
		
		if(getMaxQueryResults()>0) {
			url += "&num_results=" + getMaxQueryResults();
		}
		
		url = addLanguageArgument(url, args);

		RESTHttpClient httpclient = new ApacheHttpClient();
		return httpclient.executeGETURL(new URL(url));
	}

	private String addLanguageArgument(String curQuery, QueryArguments args) {
		if (args != null && args.getLanguage() != null
				&& !args.getLanguage().equals(SearchResource.DEFAULT_LANGUAGE)) {
			curQuery += "&language=" + args.getLanguage();
		}
		return curQuery;
	}

	private Document executeTagQuery(String query, QueryArguments args)
			throws MalformedURLException, IOException {
		String url = apiurl + "?method=docs.search&api_key=" + apikey
				+ "&scope=all" + "&query=tags:" + encodeQueryString(query);
		
		if(getMaxQueryResults()>0) {
			url += "&num_results=" + getMaxQueryResults();
		}

		url = addLanguageArgument(url, args);
		RESTHttpClient httpclient = new ApacheHttpClient();
		return httpclient.executeGETURL(new URL(url));
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
		try {
			return parseResults(executeTagQuery(tagQuery, args), tagQuery);
		} catch (MalformedURLException e) {
			logger.debug(e.getMessage());
			return null;
		} catch (IOException e) {
			logger.debug(e.getMessage());
			return null;
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
		try {
			return parseResults(executeFullTextQuery(searchTermQuery, args),
					searchTermQuery);
		} catch (MalformedURLException e) {
			logger.debug(e.getMessage());
			return null;
		} catch (IOException e) {
			logger.debug(e.getMessage());
			return null;
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
