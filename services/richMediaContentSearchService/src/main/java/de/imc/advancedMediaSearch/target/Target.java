package de.imc.advancedMediaSearch.target;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.imc.advancedMediaSearch.rankingAlgorithm.RankingAlgorithm;
import de.imc.advancedMediaSearch.restlet.resource.QueryType;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.target.restrictions.TargetRestriction;

/** */
public abstract class Target implements Runnable {

	private static Logger targetLogger = Logger.getLogger(Target.class);
	private int timeOut;
	private int maxQueryResults;

	private QueryType queryType;
	private volatile Aggregator aggregator;
	private volatile boolean running = false;
	private String queryString;
	private List<TargetRestriction> restrictions;
	private QueryArguments queryArguments;

	protected double searchTime;

	/**
	 * the ranking algorithm used
	 */
	private RankingAlgorithm rankingalgorithm;

	public static final int DEFAULT_TIMEOUT = 30;
	public static final int DEFAULT_MAXQUERYRESULTS = 25;

	// Meta Data
	protected String name;
	protected String url;
	protected String iconUrl;
	protected String mediaTypeIconUrl;
	protected String description;
	protected String[] mediaTypes;

	/**
	 * Target standard constructor initializing with default values
	 */
	public Target() {
		setTimeout(DEFAULT_TIMEOUT);
		setMaxQueryResults(DEFAULT_MAXQUERYRESULTS);
	}

	public Target(int maxDuration, int maxQueryResults) {
		setTimeout(maxDuration);
		setMaxQueryResults(maxQueryResults);
	}

	/**
	 * searches for results by using tag information. remember to call the
	 * applyRestrictions method in sub-class implementations
	 * */
	public abstract ResultSet searchByTags(String tagQuery, QueryArguments args);

	/**
	 * searches for results by using a full text query. remember to call the
	 * applyRestrictions method in sub-class implementations
	 * */
	public abstract ResultSet searchByFullTextQuery(String searchTermQuery,
			QueryArguments args);

	/**
	 * searches for results by using authors information. remember to call the
	 * applyRestrictions method in sub-class implementations
	 * */
	public abstract ResultSet searchByAuthor(String authorQuery,
			QueryArguments args);

	/**
	 * translates the given queryString to a query string corresponding to the
	 * current Targets repository api
	 * 
	 * @param queryString the query String to convert
	 * @return a query string in the format of the Targets repository api
	 */
	public String encodeQueryString(String queryString) {
		try {
			String ret = queryString.trim();
			ret = java.net.URLEncoder.encode(ret, "UTF-8");
			ret = ret.replaceAll("%2B", "%20");
			return ret;
		} catch (UnsupportedEncodingException e) {
			targetLogger.error("An error occured while encoding the query string: " + e.getMessage());
			return queryString;
		}
	}

	/**
	 * @return the timeOut
	 */
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * @param maxTimeOut
	 *            the timeOut to set
	 */
	public void setTimeout(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * @return the maxQueryResults
	 */
	public int getMaxQueryResults() {
		return maxQueryResults;
	}

	/**
	 * @param maxQueryResults
	 *            the maxQueryResults to set
	 */
	public void setMaxQueryResults(int maxQueryResults) {
		this.maxQueryResults = maxQueryResults;
	}

	public abstract String getId();

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getMediaTypeIconUrl() {
		return mediaTypeIconUrl;
	}

	public String getDescription() {
		return description;
	}

	public String[] getMediaTypes() {
		return mediaTypes;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public synchronized void setAggregator(Aggregator aggregator) {
		this.aggregator = aggregator;
	}

	public synchronized Aggregator getAggregator() {
		return aggregator;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getQueryString() {
		return queryString;
	}

	/**
	 * applys the Target Objects QueryArguments objects filters to the given
	 * result, returns the given resultset on empty QueryArguments objects
	 * @param s the ResultSet to filter
	 * @return a filtered ResultSet according to the QueryArguments settings
	 */
	public ResultSet filterResult(ResultSet s) {
		if (this.getQueryArguments() == null) {
			return s;
		}

		boolean filterMediaTypes = false;

		if (getQueryArguments().getMediaTypes() != null) {
			filterMediaTypes = true;
		}

		// add new restrictions here

		ResultSet newResults = new ResultSet();
		newResults.setSourceRepositories(s.getSourceRepositories());
		newResults.setSearchQuery(s.getSearchQuery());
		newResults.setCalculationTime(s.getCalculationTime());
		newResults.setSearchUrl(s.getSearchUrl());
		newResults.setStartIndex(s.getStartIndex());

		for (ResultEntity e : s.getResults()) {
			// filter media types
			if (filterMediaTypes) {
				if (getQueryArguments().getMediaTypes().contains(
						e.getMediaType())) {
					newResults.add(e);
				}
			}
			// add other filters here

			// add all elements if no filter is active
			else {
				newResults.add(e);
			}

		}

		return newResults;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// only run when a search query is set
		if (getQueryString() != null) {

			// only run when an aggregator is set
			if (this.getAggregator() != null) {

				try {
					this.running = true;
					ResultSet results = null;

					// check for query types
					if (getQueryType() == QueryType.authorQuery) {
						results = searchByAuthor(getQueryString(),
								getQueryArguments());
					} else if (getQueryType() == QueryType.tagQuery) {
						results = searchByTags(getQueryString(),
								getQueryArguments());
					} else if (getQueryType() == QueryType.fullTextQuery) {
						results = searchByFullTextQuery(getQueryString(),
								getQueryArguments());
					}

					// if query type is null or invalid --> results=null
					targetLogger.debug("Finished search for target: " + getId()
							+ ", returning results to aggregator!");
					this.getAggregator().addTargetResults(results,
							this.getUrl());
				} catch (Exception e) {
					targetLogger.error("An error occured while collecting results: " + e.getMessage());
					e.printStackTrace();
					targetLogger.error("Returning null to the aggregator addTargetResults function!");
					this.getAggregator().addTargetResults(null, this.getUrl());
				}

			}
		}
		// bugfix threading problem --> return and addTargetResults immediately
		else {
			targetLogger
					.debug("no query specified! returning null to the aggregator addTargetResults function!");
			if (this.getAggregator() != null) {
				this.getAggregator().addTargetResults(null, this.getId());
			}
		}

		this.running = false;
		targetLogger.debug("exiting run function of target: " + getId());
	}

	/**
	 * adds the given TargetRestriction to all targets, does nothing for null
	 * arguments.
	 * 
	 * @param restriction
	 *            the restriction to add
	 */
	public void addRestriction(TargetRestriction restriction) {
		if (restriction != null) {
			if (this.restrictions == null) {
				this.restrictions = new ArrayList<TargetRestriction>();
			}
			restriction.setTarget(this);
			this.restrictions.add(restriction);
		}
	}

	public List<TargetRestriction> getRestrictions() {
		return restrictions;
	}

	/**
	 * calls the restrictions applyRestriction methods, does nothing when no
	 * restrictions have been set
	 */
	protected void applyRestrictions() {
		if (restrictions != null) {
			for (TargetRestriction r : restrictions) {
				r.applyRestriction();
			}
		}
	}

	public void setRankingalgorithm(RankingAlgorithm rankingalgorithm) {
		this.rankingalgorithm = rankingalgorithm;
	}

	public RankingAlgorithm getRankingalgorithm() {
		return rankingalgorithm;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void stopOnTimeout() {
		if (isRunning()) {
			targetLogger.debug("calling stop function for target: " + getId());
			this.getAggregator().addTargetResults(null, this.getId());
			this.running = false;
		}
	}

	public double getSearchTime() {
		return searchTime;
	}

	public void setQueryArguments(QueryArguments queryArguments) {
		this.queryArguments = queryArguments;
	}

	public QueryArguments getQueryArguments() {
		return queryArguments;
	}
}
