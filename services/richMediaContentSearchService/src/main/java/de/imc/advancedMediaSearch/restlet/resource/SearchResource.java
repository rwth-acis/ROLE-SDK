/**
 * 
 */
package de.imc.advancedMediaSearch.restlet.resource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import de.imc.advancedMediaSearch.exceptions.IllegalArgumentException;
import de.imc.advancedMediaSearch.helpers.ConversionHelper;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.rankingAlgorithm.LuceneRanking;
import de.imc.advancedMediaSearch.rankingAlgorithm.NoRanking;
import de.imc.advancedMediaSearch.rankingAlgorithm.RankingFactory;
import de.imc.advancedMediaSearch.representation.json.JSONHelper;
import de.imc.advancedMediaSearch.representation.json.JSONResultListGenerator;
import de.imc.advancedMediaSearch.representation.search.ROMEFeedRepresentationGenerator;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.MimeType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultOrdering;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.target.Aggregator;
import de.imc.advancedMediaSearch.target.AggregatorFactory;
import de.imc.advancedMediaSearch.target.QueryArguments;

/**
 * The SearchResource represents the main search access point of the search
 * service.
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class SearchResource extends BaseServerResource {

	private static Logger logger = Logger.getLogger(SearchResource.class);

	public static final int DEFAULT_STARTINDEX = 0;
	public static final int DEFAULT_ITEMSPERPAGE = 0;
	public static final int DEFAULT_MAXRESULTS = 0;
	public static final int DEFAULT_MINRATING = 0;
	public static final int MAX_RATING = 5;
	public static final int DEFAULT_TIMEOUT = 30;
	public static final int MAX_TIMEOUT = 30;
	public static final boolean DEFAULT_MULTITHREADING = true;
	public static final boolean DEFAULT_PREVIEW = true;
	public static final String DEFAULT_RANKING = "lucene";
	public static final String DEFAULT_FORMAT = "json";
	public static final int DEFAULT_PREVIEW_WIDTH = 320;
	public static final int DEFAULT_PREVIEW_HEIGHT = 265;
	public static final int DEFAULTSINGLEREPOSITORYMAXQUERYRESULTS = 200;
	
	/**
	 * the default language for search requests, --> check for all value in
	 * targets
	 */
	public static final String DEFAULT_LANGUAGE = "all";

	private QueryType queryType;

	private String query;

	private String repository;

	private List<MediaType> mediaTypes;

	private List<MimeType> mimeTypes;

	private String orderBy;

	private String callUrl;

	private String callback;

	private ResultSet results;

	private double calculationTime;

	private Date time;

	private QueryArguments queryArguments;

	private String lang = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.defaultlanguage", DEFAULT_LANGUAGE);

	private String format = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.format", DEFAULT_FORMAT);

	private int startIndex = AMSPropertyManager.getInstance().getIntValue(
			"de.imc.advancedMediaSearch.startindex", DEFAULT_STARTINDEX);

	private int itemsPerPage = AMSPropertyManager.getInstance().getIntValue(
			"de.imc.advancedMediaSearch.itemsperpage", DEFAULT_ITEMSPERPAGE);

	private int maxResults = AMSPropertyManager.getInstance().getIntValue(
			"de.imc.advancedMediaSearch.maxresults", DEFAULT_MAXRESULTS);

	private int minRating = AMSPropertyManager.getInstance().getIntValue(
			"de.imc.advancedMediaSearch.minrating", DEFAULT_MINRATING);

	// preview arguments
	private boolean preview = AMSPropertyManager.getInstance().getBooleanValue(
			"de.imc.advancedMediaSearch.preview", DEFAULT_PREVIEW);

	private int previewWidth = AMSPropertyManager.getInstance().getIntValue(
			"de.imc.advancedMediaSearch.previewwidth", DEFAULT_PREVIEW_WIDTH);

	private int previewHeight = AMSPropertyManager.getInstance().getIntValue(
			"de.imc.advancedMediaSearch.previewheight", DEFAULT_PREVIEW_HEIGHT);

	private String ranking = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.ranking", DEFAULT_RANKING);

	private boolean multithreading = AMSPropertyManager.getInstance()
			.getBooleanValue("de.imc.advancedMediaSearch.multithreading",
					DEFAULT_MULTITHREADING);

	private int timeout = AMSPropertyManager.getInstance().getIntValue(
			"de.imc.advancedMediaSearch.timeout", DEFAULT_TIMEOUT);

	private int singleRepositoryMaxQueryResults = AMSPropertyManager
			.getInstance().getIntValue("de.imc.advancedMediaSearch.singlerepositorymaxresults",
					DEFAULTSINGLEREPOSITORYMAXQUERYRESULTS);

	@Override
	protected void doInit() throws ResourceException {
		
		// resetting calculation time and time
		calculationTime = 0;
		time = new Date();

		// retrieving call url
		callUrl = getReference().getIdentifier();

		// retrieving search access point
		String qType = (String) getRequest().getAttributes().get("queryType");
		queryType = QueryType.getQueryTypeFromString(qType);
		// abort if no valid point is given
		if (queryType == null) {
			setExisting(false);
		}

		// retrieving parameters

		// query
		query = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("q");
		
		//decode query
		try {
			query = URLDecoder.decode(query, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error("An error occured while decoding the query parameter: " + e.getMessage());
		}
		
		logger.debug("query = " + query);

		callback = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("callback");
		logger.debug("callback = " + callback);

		// repository
		repository = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("repository");
		logger.debug("repository = " + repository);

		// language
		String la = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("lang");
		if (la != null) {
			lang = la;
		}
		logger.debug("lang = " + lang);

		// ordering
		orderBy = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("orderby");
		logger.debug("orderBy = " + orderBy);

		// ranking
		String rank = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("ranking");
		if (rank != null) {
			ranking = rank;
		}
		logger.debug("ranking = " + ranking);

		// multithreading
		String sThreading = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("parallel");
		this.multithreading = ConversionHelper.convertToBoolean(sThreading,
				multithreading);
		logger.debug("multithreading = " + multithreading);

		// preview
		String sPreview = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("preview");
		this.preview = ConversionHelper.convertToBoolean(sPreview, true);
		logger.debug("preview = " + preview);

		// media-types
		String sMediaType = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("media-type");
		mediaTypes = MediaType.getTypesFromString(sMediaType);
		logger.debug("mediaTypes = " + mediaTypes);

		// mime-types
		String sMimeType = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("mime-type");
		mimeTypes = MimeType.getTypesFromString(sMimeType);
		logger.debug("mimeTypes = " + mimeTypes);

		// Format
		String sFormat = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("format");
		if (sFormat != null) {
			format = sFormat;
		}
		logger.debug("format = " + format);

		// parse integers
		String sIndex = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("start-index");
		startIndex = ConversionHelper.convertToInt(sIndex, startIndex, false);
		logger.debug("startIndex = " + startIndex);

		String sItemsPerPage = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("items-per-page");
		itemsPerPage = ConversionHelper.convertToInt(sItemsPerPage,
				itemsPerPage, false);
		logger.debug("itemsPerPage = " + itemsPerPage);

		String sMaxResults = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("max-results");
		maxResults = ConversionHelper.convertToInt(sMaxResults, maxResults,
				false);
		if (maxResults < 1) {
			int defaultMaxResults = AMSPropertyManager.getInstance()
					.getIntValue("de.imc.advancedMediaSearch.maxresults",
							DEFAULT_MAXRESULTS);
			logger.debug("maxResults value of: " + maxResults
					+ " is invalid! Resetting to default of: "
					+ defaultMaxResults);
			maxResults = defaultMaxResults;
		} else {
			logger.debug("maxResults = " + maxResults);
		}

		String sMinRating = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("min-rating");
		minRating = ConversionHelper.convertToInt(sMinRating, minRating, false,
				MAX_RATING);
		logger.debug("minRating = " + minRating);

		String sTimeout = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("timeout");
		timeout = ConversionHelper.convertToInt(sTimeout, timeout, false,
				MAX_TIMEOUT);
		logger.debug("timeout = " + timeout);

		String sPWidth = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("preview-width");
		previewWidth = ConversionHelper.convertToInt(sPWidth, previewWidth,
				false);
		logger.debug("previewWidth = " + previewWidth);

		String sPHeight = getRequest().getResourceRef().getQueryAsForm()
				.getFirstValue("preview-height");
		previewHeight = ConversionHelper.convertToInt(sPHeight, previewHeight,
				false);
		logger.debug("previewHeight = " + previewHeight);

	}

	/**
	 * Returns a Representation of the retrieved search results by evaluating
	 * the given query and search arguments
	 * 
	 * @return a Representation of the retrieved search results
	 */
	@Get
	public Representation represent() {

		// return error representation on empty results
		if (query == null || query.equals("")) {
			if (callback == null) {
				return JSONHelper.createJSONErrorRepresentation("EmptyQuery",
						"No search query defined!", null);
			} else {
				return JSONHelper.createJSONPErrorRepresentation("EmptyQuery",
						"No search query defined!", null, callback);
			}
		}

		AggregatorFactory af = new AggregatorFactory();
		Aggregator aggregator = null;

		if (repository != null) {
			try {
				aggregator = af.createAggregator(repository);
			} catch (IllegalArgumentException e) {
				logger.debug("couldn't parse repository argument: "
						+ repository);
				return JSONHelper.createJSONErrorRepresentation(
						"IllegalArgument",
						"The given repository argument is not valid!", null);
			}
		} else {
			aggregator = af.createAggregator();
		}

		// TODO: initialize QueryArguments member object
		queryArguments = new QueryArguments();

		// configure media types
		queryArguments.setMediaTypes(mediaTypes);
		queryArguments.setPreview(preview);
		queryArguments.setPreviewHeigth(previewHeight);
		queryArguments.setPreviewWidth(previewWidth);
		queryArguments.setLanguage(lang);

		// configuring aggreagator
		aggregator.setQueryString(query);
		aggregator.setQueryType(queryType);
		// setting arguments
		aggregator.setQueryArguments(queryArguments);

		if (maxResults > 0) {
			aggregator.setMaxQueryResults(maxResults);
		}

		RankingFactory fac = new RankingFactory();

		// disable lucene ranking if just one repository is selected for
		// searching
		// increase max search results for that target
		if (aggregator.getTargets().size() < 2) {
			aggregator.setRankingalgorithm(fac
					.createRanking(NoRanking.IDENTIFIER));
			aggregator.setMaxQueryResults(singleRepositoryMaxQueryResults);
		} else {

			// initialize Ranking Algorithm
			if (ranking != null) {
				aggregator.setRankingalgorithm(fac.createRanking(ranking));
			} else {
				aggregator.setRankingalgorithm(fac
						.createRanking(LuceneRanking.IDENTIFIER));
			}
		}

		// perform query
		if (queryType == QueryType.fullTextQuery) {
			performFullTextQuery(multithreading, aggregator, timeout);
		} else if (queryType == QueryType.authorQuery) {
			performAuthorQuery(multithreading, aggregator, timeout);
		} else if (queryType == QueryType.tagQuery) {
			performTagQuery(multithreading, aggregator, timeout);
		}

		// shrinking results if necessary
		results.shrinkNumberOfResultsToSize(maxResults);

		// set the results search url
		results.setSearchUrl(callUrl);

		// sort the results according to the given ordering
		if (orderBy != null) {
			ResultOrdering ordering = null;
			ordering = ResultOrdering.getOrderingFromString(orderBy);
			if (ordering != null) {
				Comparator<ResultEntity> c = ResultOrdering
						.getComparatorForOrdering(ordering);
				Collections.sort(results.getResults(), c);
			}
		}

		// setting calculation time
		Date actualDate = new Date();
		calculationTime = actualDate.getTime() - time.getTime();

		results.setCalculationTime(calculationTime);

		logger.debug("Total ResultList calculation time: " + calculationTime);

		RepresentationFormat aFormat = RepresentationFormat
				.getFormatfromString(format);

		// output format parameter
		// check---------------------------------------------
		if (aFormat == RepresentationFormat.atom) {
			// TODO: Implement
			ROMEFeedRepresentationGenerator atomGenerator = new ROMEFeedRepresentationGenerator(
					"atom_1.0", "AdvancedMediaSearch",
					"http://www.im-c.de/AdvancedMediaSearch",
					"julian.weber@im-c.de");

			return atomGenerator.generateRepresentation(results, this,
					getPreferredVariant(getVariants()));

			// return new StringRepresentation("Not implemented yet!");
		}
		// return json as default
		else {

			// JSON list generation
			JSONResultListGenerator gen = new JSONResultListGenerator();

			JSONObject resultJSON = gen.generateJSONResultList(results);

			// Callback
			if (callback != null) {
				return JSONHelper.createJsonPRepresentation(resultJSON,
						callback);
			} else {
				return JSONHelper.createJsonRepresentation(resultJSON);
			}
		}
	}

	private void performFullTextQuery(boolean parallel, Aggregator a,
			int timeout) {
		if (parallel) {
			runThreadedAggregator(a, timeout);
			results = a.getLastResults();
			results.setSearchQuery(query);
		} else {
			results = a.searchByFullTextQuery(query, queryArguments);
		}
	}

	private void performTagQuery(boolean parallel, Aggregator a, int timeout) {
		if (parallel) {
			runThreadedAggregator(a, timeout);
			results = a.getLastResults();
			results.setSearchQuery(query);
		} else {
			results = a.searchByTags(query, queryArguments);
		}
	}

	private void performAuthorQuery(boolean parallel, Aggregator a, int timeout) {
		if (parallel) {
			runThreadedAggregator(a, timeout);
			results = a.getLastResults();
			results.setSearchQuery(query);
		} else {
			results = a.searchByAuthor(query, queryArguments);
		}
	}

	private void runThreadedAggregator(Aggregator a, int timeout) {
		if (a != null) {
			Date startTime = new Date();
			Date actualTime = new Date();
			a.startThreadedSearch(false);
			// wait until timeout has occured or search is completed
			while (!a.isSearchCompleted()) {
				try {
					actualTime = new Date();
					long difference = actualTime.getTime()
							- startTime.getTime();
					if (difference > (timeout * 1000)) {
						logger.debug("the request took longer than the initialized timeout. stopping search");
						a.stopOnTimeout();
					}
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
