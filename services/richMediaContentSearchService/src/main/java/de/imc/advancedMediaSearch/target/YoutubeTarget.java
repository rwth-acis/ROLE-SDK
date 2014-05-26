package de.imc.advancedMediaSearch.target;

import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.restlet.resource.SearchResource;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultRating;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultThumbnail;
import de.imc.advancedMediaSearch.result.ResultUser;
import de.imc.advancedMediaSearch.result.preview.ResultPreviewFactory;
import de.imc.advancedMediaSearch.result.preview.YoutubeResultPreviewFactory;

import com.google.gdata.client.Query;
import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.client.youtube.YouTubeQuery.Time;
import com.google.gdata.data.Category;
import com.google.gdata.data.Person;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.data.youtube.YtPublicationState;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import org.apache.log4j.Logger;

/** */
public class YoutubeTarget extends Target {

	private static Logger logger = Logger.getLogger(YoutubeTarget.class);

	public static final String DEFAULT_URL = "http://gdata.youtube.com/";
	
	private int embeddedHeight = SearchResource.DEFAULT_PREVIEW_HEIGHT;
	private int embeddedWidth = SearchResource.DEFAULT_PREVIEW_WIDTH;

	private int YOUTUBE_MAX_RESULTS = 50;
	
	private String baseurl = AMSPropertyManager.getInstance().getStringValue("de.imc.advancedMediaSearch.baseurls.youtube", DEFAULT_URL);

	public static final String ID = "youtube.com";

	public void initializeMetaData() {
		name = "Youtube";
		url = "http://www.youtube.com";
		mediaTypeIconUrl = ""; // TODO: insert url
		description = "YouTube is a video-sharing website on which users can upload, share, and view videos.";
		String[] mTypes = { MediaType.VIDEO.toString(),
				MediaType.AUDIO.toString(), MediaType.PRESENTATION.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
		.getInstance()
		.getStringValue(
				"de.imc.advancedMediaSearch.iconurls.youtube",
				"http://role-demo.de:8080/richMediaContentSearchResources/icons/youtube.ico");
	}

	public YoutubeTarget() {
		super();

		// Meta Data
		initializeMetaData();
	}

	public YoutubeTarget(int maxDuration, int maxQueryResults) {
		super(maxDuration, maxQueryResults);
		if (maxQueryResults > YOUTUBE_MAX_RESULTS) {
			setMaxQueryResults(YOUTUBE_MAX_RESULTS);
		}
		// Meta Data
		initializeMetaData();
	}

	@Override
	public ResultSet searchByAuthor(String author, QueryArguments args) {
		ResultSet result = null;

		logger.debug("Start searching for author \"" + author + "\"");

		// define url to query
		YouTubeQuery query;

		try {
			query = new YouTubeQuery(new URL( baseurl + "feeds/api/videos"));

			// set query term
			query.setAuthor(author);

			// order results by the number of views (most viewed first)
			query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);

			if(args!=null && args.getLanguage()!=null && args.getLanguage()!=SearchResource.DEFAULT_LANGUAGE) {
				query.setLanguageRestrict(args.getLanguage());
			}
			
			result = executeQuery(query);

		} catch (MalformedURLException e) {
			logger.error("Error by initializing url: " + e.getMessage());
		}

		return result;

	}

	@Override
	public ResultSet searchByFullTextQuery(String searchTerm,
			QueryArguments args) {

		ResultSet result = null;

		logger.debug("Start querying for \"" + searchTerm + "\"");

		// define url to query
		YouTubeQuery query;

		try {
			query = new YouTubeQuery(new URL( baseurl + "feeds/api/videos"));

			// set query term
			query.setFullTextQuery(searchTerm);

			// order results by the number of views (most viewed first)
			query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
			
			
			if(args!=null && args.getLanguage()!=null && !args.getLanguage().equals(SearchResource.DEFAULT_LANGUAGE)) {
				query.setLanguageRestrict(args.getLanguage());
			}

			result = executeQuery(query);

		} catch (MalformedURLException e) {
			logger.error("Error by initializing url: " + e.getMessage());
		}
		
		result = filterResult(result);
		return result;

	}

	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {
		ResultSet result = null;

		logger.debug("Start searching for tags \"" + tagQuery.toString() + "\"");

		// define url to query
		YouTubeQuery query;

		try {
			query = new YouTubeQuery(new URL( baseurl + "feeds/api/videos"));

			// create category filter
			Query.CategoryFilter categoryFilter = new Query.CategoryFilter();

			// add each tag to the category

			categoryFilter.addCategory(new Category(
					YouTubeNamespace.KEYWORD_SCHEME, tagQuery));

			// set category filter
			query.addCategoryFilter(categoryFilter);
			
			if(args!=null && args.getLanguage()!=null && args.getLanguage()!=SearchResource.DEFAULT_LANGUAGE) {
				query.setLanguageRestrict(args.getLanguage());
			}

			// order results by the number of views (most viewed first)
			query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);

			result = executeQuery(query);

		} catch (MalformedURLException e) {
			logger.error("Error by initializing url: " + e.getMessage());
		}

		result = filterResult(result);
		return result;
	}

	/**
	 * executes the given youtubequery, returns an empty ResultSet when no
	 * results were found
	 * 
	 * @param query
	 * @return
	 */
	private ResultSet executeQuery(YouTubeQuery query) {

		Date startDate = new Date();

		ResultSet result = null;

		// create service
		YouTubeService service = new YouTubeService("AvancedMediaSearch Widget");

		// no safe search
		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);

		// set time
		query.setTime(Time.ALL_TIME);

		// set time
		query.setMaxResults(getMaxQueryResults());

		// run query
		VideoFeed videoFeed;
		try {
			videoFeed = service.query(query, VideoFeed.class);

			result = createResultSet(videoFeed);
			result.addSourceRepository(getUrl());
			logger.debug("Found " + result.size() + " results");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IOError by generating feed: " + e.getMessage());
		} catch (ServiceException e) {
			logger.error("ServiceException by generating feed: "
					+ e.getMessage());
		}

		Date endDate = new Date();
		searchTime = endDate.getTime() - startDate.getTime();

		// filter result
		result = filterResult(result);
		logger.debug("YoutubeTarget searchtime: " + searchTime);
		return result;
	}

	/**
	 * creates a ResultSet from the given videoFeed, returns an empty ResultSet
	 * if no results were found
	 * 
	 * @param videoFeed
	 * @return
	 */
	private ResultSet createResultSet(VideoFeed videoFeed) {

		ResultSet result = new ResultSet();

		for (VideoEntry videoEntry : videoFeed.getEntries()) {
			result.add(createResultEntry(videoEntry));
		}

		return result;
	}

	/**
	 * creates a ResultEntity from the given VideoEntry object
	 * 
	 * @param videoEntry
	 * @return
	 */
	private ResultEntity createResultEntry(VideoEntry videoEntry) {

		ResultEntity resultEntity = new ResultEntity();

		String titleString = videoEntry.getTitle().getPlainText();
		resultEntity.setTitle(titleString);

		if (videoEntry.isDraft()) {
			YtPublicationState pubState = videoEntry.getPublicationState();
			if (pubState.getState() == YtPublicationState.State.PROCESSING) {
				resultEntity.setDescription("Video is still being processed.");
			} else if (pubState.getState() == YtPublicationState.State.REJECTED) {
				String rejectedString = "Video has been rejected because: \n";
				rejectedString += pubState.getDescription() + "\n";
				rejectedString += "For help visit: \n";
				rejectedString += pubState.getHelpUrl() + "\n";

				resultEntity.setDescription(rejectedString);

			} else if (pubState.getState() == YtPublicationState.State.FAILED) {
				String failedString = "Video failed uploading because: \n";
				failedString += pubState.getDescription() + "\n";
				failedString += "For help visit: \n";
				failedString += pubState.getHelpUrl() + "\n";

				resultEntity.setDescription(failedString);
			}
		} else {

			// get Mediagroup for detailed information
			YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();

			// set uploader
			for (Person author : videoEntry.getAuthors()) {

				ResultUser aut = new ResultUser();
				aut.setProfileUrl("http://www.youtube.com/" + author.getName());
				aut.setUserId(author.getName());
				aut.setEmail(author.getEmail());

				resultEntity.setUploader(aut);

			}

			// set description`

			String descriptionString = mediaGroup.getDescription()
					.getPlainTextContent();
			resultEntity.setDescription(descriptionString);

			// set url
			MediaPlayer mediaPlayer = mediaGroup.getPlayer();
			resultEntity.setUrl(mediaPlayer.getUrl());

			// set uri
			// resultEntity.setUri(mediaPlayer.getUrl());

			// set source name
			resultEntity.setSource(YoutubeTarget.ID);

			// skip score

			// set thumbnail
			if (!mediaGroup.getThumbnails().isEmpty()) {
				MediaThumbnail mediaThumbnail = mediaGroup.getThumbnails().get(
						0);

				ResultThumbnail thumbnail = new ResultThumbnail();

				thumbnail.setHeight(mediaThumbnail.getHeight());
				thumbnail.setWidth(mediaThumbnail.getWidth());
				try {
					thumbnail.setUrl(new URL(mediaThumbnail.getUrl()));
				} catch (MalformedURLException e) {
					logger.error("Error in thumbnail url: " + e.getMessage());
				}

				resultEntity.setThumbnail(thumbnail);
			}

			// set length
			if (!mediaGroup.getYouTubeContents().isEmpty()) {
				YouTubeMediaContent mediaContent = mediaGroup
						.getYouTubeContents().get(0);
				resultEntity.setLength(mediaContent.getDuration());
				// )mediaContent.getType();
			}

			// set published date
			resultEntity.setPublished(new Date(videoEntry.getPublished()
					.getValue()));

			// set updated date
			resultEntity
					.setUpdated(new Date(videoEntry.getUpdated().getValue()));

			// set source rating
			Rating rating = videoEntry.getRating();
			if (rating != null) {
				ResultRating rat = new ResultRating(rating.getAverage(),
						YoutubeTarget.ID);
				resultEntity.addRating(rat);
			}

			// skip user rating

			// set tags
			MediaKeywords keywords = mediaGroup.getKeywords();
			for (String keyword : keywords.getKeywords()) {

				ResultTag t = new ResultTag(keyword, YoutubeTarget.ID);
				resultEntity.addTag(t);
			}

			// set embedded HTML
			ResultPreviewFactory fac = new YoutubeResultPreviewFactory();
			if (getQueryArguments() != null) {
				// read embedded data from QueryArguments
				if (getQueryArguments().isPreview()) {
					resultEntity.setPreview(fac.createResultPreview(
							YoutubeTarget.ID, mediaGroup.getVideoId(),
							getQueryArguments().getPreviewWidth(),
							getQueryArguments().getPreviewHeigth()));
				}
			} else {
				resultEntity.setPreview(fac.createResultPreview(
						YoutubeTarget.ID, mediaGroup.getVideoId(),
						embeddedWidth, embeddedHeight));
			}
			
			// set format
			if (!mediaGroup.getYouTubeContents().isEmpty()) {
				YouTubeMediaContent mediaContent = mediaGroup
						.getYouTubeContents().get(0);
				resultEntity.setFormat(mediaContent.getType());
			}

			// set media type
			resultEntity.setMediaType(MediaType.VIDEO);

			// skip notes
		}

		return resultEntity;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.imc.advancedMediaSearch.target.Target#setMaxQueryResults(int)
	 */
	@Override
	public void setMaxQueryResults(int maxQueryResults) {
		if (maxQueryResults < YOUTUBE_MAX_RESULTS) {
			super.setMaxQueryResults(maxQueryResults);
		} else {
			super.setMaxQueryResults(YOUTUBE_MAX_RESULTS);
		}
	}

}
