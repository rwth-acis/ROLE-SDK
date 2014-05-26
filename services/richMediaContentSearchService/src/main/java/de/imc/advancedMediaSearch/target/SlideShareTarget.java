package de.imc.advancedMediaSearch.target;

import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.restlet.resource.SearchResource;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultThumbnail;
import de.imc.advancedMediaSearch.result.ResultUser;
import de.imc.advancedMediaSearch.result.preview.ResultPreviewFactory;
import de.imc.advancedMediaSearch.result.preview.SlideshareResultPreviewFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** */
public class SlideShareTarget extends Target {

	private static Logger logger = Logger.getLogger(SlideShareTarget.class);

	private static final int THUMBNAIL_WIDTH = 170;
	private static final int THUMBNAIL_HEIGHT = 128;

	private static final int PREVIEW_WIDTH = SearchResource.DEFAULT_PREVIEW_WIDTH;
	private static final int PREVIEW_HEIGHT = SearchResource.DEFAULT_PREVIEW_HEIGHT;

	private static final String API_KEY = "naudtXCD";
	private static final String SHARED_SECRET = "saCasaLj";
	
	private String language = "en";

	public static final String ID = "slideshare.net";

	public void initializeMetaData() {
		name = "Slideshare";
		url = "http://www.slideshare.net";
		mediaTypeIconUrl = ""; // TODO: insert icon address
		description = "SlideShare is an online slide hosting service. Users can upload files in the following file formats: PowerPoint, PDF, Keynote or OpenOffice presentations.";
		String[] mTypes = { MediaType.PRESENTATION.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
		.getInstance()
		.getStringValue(
				"de.imc.advancedMediaSearch.iconurls.slideshare",
				"http://role-demo.de:8080/richMediaContentSearchResources/icons/slideshare.ico");
	}

	public SlideShareTarget() {
		super();

		// Meta data
		initializeMetaData();
	}

	public SlideShareTarget(int maxDuration, int maxQueryResults) {
		super(maxDuration, maxQueryResults);

		// Meta data
		initializeMetaData();
	}

	@Override
	public ResultSet searchByAuthor(String author, QueryArguments args) {

		// encode URL
		try {
			author = java.net.URLEncoder.encode(author, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			logger.error("Cannot encode url: " + author);
			return new ResultSet();
		}
		
		if(args!=null && args.getLanguage()!=null && !args.getLanguage().equals(SearchResource.DEFAULT_LANGUAGE)) {
			language = args.getLanguage();
		}

		String url = "http://www.slideshare.net/api/2/get_slideshows_by_user"
				+ "?username_for=" + author + "&limit=" + getMaxQueryResults();

		if(!language.equals("en")) {
			url += "&lang=" + language;
		}
		
		return executeQuery(url);

	}

	@Override
	public ResultSet searchByFullTextQuery(String searchTerm,
			QueryArguments args) {

		searchTerm = encodeQueryString(searchTerm);
		logger.debug("Starting querying for: " + searchTerm);

		if(args!=null && args.getLanguage()!=null && !args.getLanguage().equals(SearchResource.DEFAULT_LANGUAGE)) {
			language = args.getLanguage();
		}
		
		String url = "http://www.slideshare.net/api/2/search_slideshows"
				+ "?q=" + searchTerm + "&page=1&items_per_page="
				+ getMaxQueryResults();
		
		if(!language.equals("en")) {
			url += "&lang=" + language;
		}

		return executeQuery(url);

	}

	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {
		
		if(args!=null && args.getLanguage()!=null && !args.getLanguage().equals(SearchResource.DEFAULT_LANGUAGE)) {
			language = args.getLanguage();
		}

		String url = "http://www.slideshare.net/api/2/get_slideshows_by_tag"
				+ "?tag=" + tagQuery + "&limit=" + getMaxQueryResults();

		if(!language.equals("en")) {
			url += "&lang=" + language;
		}
		
		return executeQuery(url);

	}

	/**
	 * @param targetURL
	 * @return
	 */
	private ResultSet executeQuery(String targetURL) {

		ResultSet result = null;
		Date startDate = new Date();

		Long timestamp = System.currentTimeMillis() / 1000;

		targetURL += "&api_key=" + API_KEY + "&ts=" + timestamp + "&hash="
				+ computeHash(timestamp) + "&detailed=1";

		// targetURL = "http://www.slideshare.net";

		logger.debug("Try to query URL: " + targetURL);

		// RESTHttpClient httpclient = new RESTletHttpClient();
		// RESTHttpClient httpclient = new JakartaRESTHttpClient();
		RESTHttpClient httpclient = new ApacheHttpClient();

		try {
			result = parseDOMDocument(httpclient.executeGETURL(new URL(
					targetURL)));
			result.addSourceRepository(this.getUrl());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			logger.error("Error: " + e.getMessage(), e);
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			logger.error("Error: " + e.getMessage(), e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error("Error: " + e.getMessage(), e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error: " + e.getMessage(), e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error: " + e.getMessage(), e);
		}

		result = filterResult(result);

		Date endDate = new Date();
		searchTime = endDate.getTime() - startDate.getTime();
		logger.debug("SlideshareTarget searchtime: " + searchTime);
		return result;
	}

	/**
	 * @param document
	 * @return
	 * @throws MalformedURLException
	 * @throws DOMException
	 * @throws ParseException
	 */
	private ResultSet parseDOMDocument(Document document)
			throws MalformedURLException, DOMException, ParseException {

		// return empty list on null argument
		if (document == null) {
			ResultSet s = new ResultSet();
			s.addSourceRepository(this.getUrl());
			return s;
		}

		logger.debug("Try to parse document");

		ResultSet result = new ResultSet();
		result.addSourceRepository(this.getUrl());

		// get root and slideshows
		Node root = document.getFirstChild();

		logger.debug("Enter root " + root.getNodeName());

		// get child list
		NodeList slideshows = root.getChildNodes();

		// go throught slideshows
		for (int i = 0; i < slideshows.getLength(); i++) {

			Node currentSlideshowNode = slideshows.item(i);

			// check what kind of node we got
			if (currentSlideshowNode.getNodeName() == "Meta") {
				logger.debug("Enter node " + currentSlideshowNode.getNodeName());

			} else if (currentSlideshowNode.getNodeName() == "Slideshow") {
				logger.debug("Enter node " + currentSlideshowNode.getNodeName());

				NodeList slideshowEntryNodes = currentSlideshowNode
						.getChildNodes();

				ResultEntity resultEntity = new ResultEntity();

				// go throught slideshows
				for (int j = 0; j < slideshowEntryNodes.getLength(); j++) {

					// set source
					resultEntity.setSource(SlideShareTarget.ID);

					// set mediaType
					resultEntity.setMediaType(MediaType.PRESENTATION);

					Node slideshowEntryNode = slideshowEntryNodes.item(j);

					// check what kind of node we got
					if (slideshowEntryNode.getNodeName().equals("Title")) {

						resultEntity.setTitle(slideshowEntryNode
								.getFirstChild().getNodeValue());

					} else if (slideshowEntryNode.getNodeName().equals("Description")
							&& slideshowEntryNode.getFirstChild() != null) {

						resultEntity.setDescription(slideshowEntryNode
								.getFirstChild().getNodeValue());

					} else if (slideshowEntryNode.getNodeName().equals("Username")) {
						ResultUser aut = new ResultUser();
						aut.setSource(SlideShareTarget.ID);
						aut.setName(slideshowEntryNode.getFirstChild()
								.getNodeValue());

						resultEntity.addAuthor(aut);

					} else if (slideshowEntryNode.getNodeName().equals("URL")) {

						// resultEntity.setUrl(new
						// URL(slideshowEntryNode.getFirstChild().getNodeValue()));
						// resultEntity.setUri(slideshowEntryNode.getFirstChild().getNodeValue());
						resultEntity.setUrl(slideshowEntryNode.getFirstChild()
								.getNodeValue());

					} else if (slideshowEntryNode.getNodeName().equals("ThumbnailURL")) {

						ResultThumbnail thumbnail = new ResultThumbnail();
						thumbnail.setUrl(new URL(slideshowEntryNode
								.getFirstChild().getNodeValue()));
						thumbnail.setHeight(THUMBNAIL_HEIGHT);
						thumbnail.setWidth(THUMBNAIL_WIDTH);

						resultEntity.setThumbnail(thumbnail);

					} else if (slideshowEntryNode.getNodeName() == "Embed") {
						ResultPreviewFactory fac = new SlideshareResultPreviewFactory();

						if (getQueryArguments() != null) {
							if (getQueryArguments().isPreview()) {
								resultEntity.setPreview(fac
										.createResultPreview(
												SlideShareTarget.ID,
												slideshowEntryNode
														.getFirstChild()
														.getNodeValue(),
												getQueryArguments()
														.getPreviewWidth(),
												getQueryArguments()
														.getPreviewHeigth()));
							}
						} else {
							// generate using default values
							resultEntity.setPreview(fac.createResultPreview(
									SlideShareTarget.ID, slideshowEntryNode
											.getFirstChild().getNodeValue(),
									PREVIEW_WIDTH, PREVIEW_HEIGHT));
						}

					} else if (slideshowEntryNode.getNodeName() == "Created") {

						String dateString = slideshowEntryNode.getFirstChild()
								.getNodeValue();

						DateFormat formatter = new SimpleDateFormat(
								"EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);

						try {
							Date date = formatter.parse(dateString);
							resultEntity.setPublished(date);
							resultEntity.setUpdated(date);
						} catch (ParseException e) {
							logger.error("Can't parse the date: " + e);
						}

					} else if (slideshowEntryNode.getNodeName().equals("Language")) {

						// TODO: parse language
						resultEntity.addLanguage(slideshowEntryNode
								.getFirstChild().getNodeValue());

					} else if (slideshowEntryNode.getNodeName().equals("Format")) {

						// TODO: parse format
						resultEntity.setFormat(slideshowEntryNode
								.getFirstChild().getNodeValue());

					} else if (slideshowEntryNode.getNodeName() == "Tags") {
						NodeList slideshowTagNodes = slideshowEntryNode
								.getChildNodes();
						for (int k = 0; k < slideshowTagNodes.getLength(); k++) {
							Node slideshowTagNode = slideshowTagNodes.item(k);

							if (slideshowTagNode.getNodeName() == "Tag") {
								ResultTag t = new ResultTag(slideshowTagNode
										.getFirstChild().getNodeValue(),
										SlideShareTarget.ID);
								resultEntity.addTag(t);
							}
						}

					} else if (slideshowEntryNode.getNodeName() == "NumSlides") {

						resultEntity.setLength(Integer
								.parseInt(slideshowEntryNode.getFirstChild()
										.getNodeValue()));
					}

				}// end slideshowEntryNode selection loop

				result.add(resultEntity);
				logger.debug("Added result" + resultEntity.getTitle());

			}

		}// end slideshowsNode selection loop

		logger.debug("Found " + result.size() + " results");
		return result;
	}

	/**
	 * @param timestamp
	 * @return
	 */
	private static String computeHash(Long timestamp) {

		String msg = SHARED_SECRET + timestamp;

		String hash = "";

		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] encryptMsg = sha1.digest(msg.getBytes());
			hash = byteArray2Hex(encryptMsg);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hash;
	}

	/**
	 * @param hash
	 * @return
	 */
	private static String byteArray2Hex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	@Override
	public String getId() {
		return ID;
	}

}
