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
import de.imc.advancedMediaSearch.restlet.resource.SearchResource;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultPreview;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultThumbnail;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class FiveMinTarget extends Target {

	public static final String ID = "5min.com";

	private static final String DEFAULTAPIURL = "http://api.5min.com/search/";

	private String apiurl = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.baseurls.fivemin", DEFAULTAPIURL);

	private final String FEEDNAME = "videos.xml";

	private static Logger logger = Logger.getLogger(FiveMinTarget.class);

	public FiveMinTarget() {
		super();
		initializeMetaData();
	}

	public void initializeMetaData() {
		name = "5min";
		url = "http://www.5min.com";
		mediaTypeIconUrl = "";
		description = "5min is offering instructional videos on various kinds of topics.";
		String[] mTypes = { MediaType.VIDEO.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue("de.imc.advancedMediaSearch.iconurls.fivemin",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/5min.ico");
	}

	private Document executeQuery(String url) {
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

	private ResultSet parseResult(Document doc, String searchQuery) {
		ResultSet results = new ResultSet();
		results.addSourceRepository(ID);
		results.setSearchQuery(searchQuery);
		
		NodeList items = doc.getElementsByTagName("item");
		
		for(int i=0; i< items.getLength(); i++) {
			ResultEntity e = new ResultEntity();
			e.setMediaType(MediaType.VIDEO);
			e.setSource(ID);
			
			Node actualItem = items.item(i);
			NodeList childNodes = actualItem.getChildNodes();
			for(int j=0; j<childNodes.getLength(); j++) {
				Node curChild = childNodes.item(j);
				//title
				if(curChild.getNodeName().equals("title")) {
					e.setTitle(curChild.getFirstChild().getNodeValue());
				} 
				//description
				else if(curChild.getNodeName().equals("description")) {
					String descString = curChild.getFirstChild().getNodeValue();
					int startIndex = descString.indexOf("<p>") + 3;
					int endIndex = descString.indexOf("</p>");
					String realDescString = descString.substring(startIndex, endIndex);
					e.setDescription(realDescString);
				} 
				//url
				else if(curChild.getNodeName().equals("link")) {
					e.setUrl(curChild.getFirstChild().getNodeValue());
				} 
				//thumbnail
				else if(curChild.getNodeName().equals("media:thumbnail")) {
					String turl = curChild.getAttributes().getNamedItem("url").getNodeValue();
					String twidth = curChild.getAttributes().getNamedItem("width").getNodeValue();
					String theight = curChild.getAttributes().getNamedItem("height").getNodeValue();
					
				
					int width = 0;
					int height = 0;
					
					try {
						Integer.parseInt(twidth);
						Integer.parseInt(theight);
					} catch (Exception er) {
						//do nothing here
					}
					
					if(turl!=null) {
						try {
							ResultThumbnail tn = new ResultThumbnail(width, height, new URL(turl));
							e.setThumbnail(tn);
						} catch (MalformedURLException e1) {
							logger.debug(e1.getMessage());
						}
					}
				} 
				//tags
				else if(curChild.getNodeName().equals("media:keywords")) {
					String keywords = curChild.getFirstChild().getNodeValue();
					String[] tags = keywords.split(",");
					for(String s: tags) {
						ResultTag tag = new ResultTag(s, ID);
						e.addTag(tag);
					}
				}
				//preview
				else if(curChild.getNodeName().equals("media:player")) {
					NodeList clist = curChild.getChildNodes();
					for(int p=0; p<clist.getLength(); p++) {
						Node cNode = clist.item(p);
						if(cNode.getNodeType() == Node.CDATA_SECTION_NODE) {
							String previewhtml = cNode.getNodeValue();
							ResultPreview prev = new ResultPreview(true, previewhtml, null, ID);
							e.setPreview(prev);
							break;
						}
					}
				} 
				//publication date
				else if(curChild.getNodeName().toLowerCase().equals("pubdate")) {
					String datestring = curChild.getFirstChild().getNodeValue();
					
					//date format: Wed, 25 Jul 2007 15:12:37 GMT
					Date d = ConversionHelper.convertStringToDate(datestring, "EEE, d MMM yyyy HH:mm:ss z");
					if(d.getTime()!=0) {
						e.setPublished(d);
					}
				}
				
			}
			results.add(e);
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
		//currently: return full text results
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

		String url = apiurl + encodeQueryString(searchTermQuery) + "/"
				+ FEEDNAME;
		url += "?num_of_videos=" + getMaxQueryResults();
		url += "&width="
				+ AMSPropertyManager.getInstance().getStringValue(
						"de.imc.advancedMediaSearch.previewwidth",
						String.valueOf(SearchResource.DEFAULT_PREVIEW_WIDTH));
		
		url += "&height="
			+ AMSPropertyManager.getInstance().getStringValue(
					"de.imc.advancedMediaSearch.previewheight",
					String.valueOf(SearchResource.DEFAULT_PREVIEW_HEIGHT));
		
		if (args.isLanguageSet()) {
			url += "&video_language=" + args.getLanguage();
		}
		
		return parseResult(executeQuery(url), searchTermQuery);
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
		//currently: return full text results
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
