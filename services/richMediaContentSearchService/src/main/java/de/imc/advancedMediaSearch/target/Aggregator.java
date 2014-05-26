package de.imc.advancedMediaSearch.target;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import de.imc.advancedMediaSearch.rankingAlgorithm.NoRanking;
import de.imc.advancedMediaSearch.rankingAlgorithm.RankingAlgorithm;
import de.imc.advancedMediaSearch.restlet.resource.QueryType;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.target.restrictions.TargetRestriction;
import de.imc.advancedMediaSearch.threading.ThreadHelper;
import de.imc.advancedMediaSearch.target.QueryArguments;

/**
 * @author Dahrendorf
 * 
 */
public class Aggregator extends Target {

	public final String ID = "aggregator";

	public static final boolean DEFAULT_RERANKONNEWRESULTS = false;

	/**
	 * a list of search targets
	 */
	private List<Target> targets;

	/**
	 * the time of the last result update
	 */
	private volatile Date lastUpdate;

	/**
	 * is the search completed?
	 */
	private volatile boolean searchCompleted;

	/**
	 * a set of latest results
	 */
	private volatile ResultSet lastResults;

	private volatile int repositoriesSearched;

	private volatile int repositoriesToSearch;

	private boolean rerankonnewresults = DEFAULT_RERANKONNEWRESULTS;

	private static Logger logger = Logger.getLogger(Aggregator.class);

	/**
	 * @param maxDuration
	 * @param maxQueryResults
	 * @param rankingalgorithm
	 */
	public Aggregator(int maxDuration, int maxQueryResults,
			RankingAlgorithm rankingalgorithm) {
		super(maxDuration, maxQueryResults);

		setRankingalgorithm(rankingalgorithm);
		targets = new ArrayList<Target>();
		lastResults = new ResultSet();
	}

	/**
	 * standard constructor initializing default values default ranking is
	 * NoRanking
	 */
	public Aggregator() {
		super();
		targets = new ArrayList<Target>();
		lastResults = new ResultSet();
		setRankingalgorithm(new NoRanking());
	}

	/**
	 * adds a Target to the Aggregator sets the targets ranking to NoRanking
	 * because everything will be ranked in the aggregator instead
	 * 
	 * @param target
	 */
	public void addTarget(Target target) {
		if (target != null) {
			targets.add(target);
			target.setAggregator(this);
			target.setRankingalgorithm(new NoRanking());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByAuthor(java.lang.String)
	 */
	@Override
	public ResultSet searchByAuthor(String author, QueryArguments args) {
		ResultSet resultSet = new ResultSet();

		List<ResultSet> targetResultSets = new ArrayList<ResultSet>();

		for (Target tmpTarget : targets) {

			logger.debug("Query is is executing in repository: "
					+ tmpTarget.getName());

			ResultSet actualResultSet = tmpTarget.searchByAuthor(author, args);
			// add target's source url to the resultset
			actualResultSet.addSourceRepository(tmpTarget.getUrl());
			targetResultSets.add(actualResultSet);
		}

		resultSet = getRankingalgorithm().computeRankedList(targetResultSets,
				author, getMaxQueryResults());

		// setting search query string
		resultSet.setSearchQuery(author);

		// adding source repositories to final ResultSet
		for (ResultSet s : targetResultSets) {
			resultSet.getSourceRepositories().addAll(s.getSourceRepositories());
		}

		return resultSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchBySearchTerm(java.lang
	 * .String)
	 */
	@Override
	public ResultSet searchByFullTextQuery(String searchTerm,
			QueryArguments args) {

		ResultSet resultSet = new ResultSet();

		List<ResultSet> targetResultSets = new ArrayList<ResultSet>();

		for (Target tmpTarget : targets) {

			logger.debug("Query is is executing in repository: "
					+ tmpTarget.getName());

			ResultSet actualResultSet = tmpTarget.searchByFullTextQuery(
					searchTerm, args);
			// add target's source url to the resultset
			actualResultSet.addSourceRepository(tmpTarget.getUrl());
			targetResultSets.add(actualResultSet);
		}

		resultSet = getRankingalgorithm().computeRankedList(targetResultSets,
				searchTerm, getMaxQueryResults());

		// setting search query string
		resultSet.setSearchQuery(searchTerm);

		// adding source repositories to final ResultSet
		for (ResultSet s : targetResultSets) {
			resultSet.getSourceRepositories().addAll(s.getSourceRepositories());
		}
		return resultSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByTags(java.lang.String)
	 */
	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {
		ResultSet resultSet = new ResultSet();

		List<ResultSet> targetResultSets = new ArrayList<ResultSet>();

		for (Target tmpTarget : targets) {

			logger.debug("Query is is executing in repository: "
					+ tmpTarget.getName());

			ResultSet actualResultSet = tmpTarget.searchByTags(tagQuery, args);
			// add target's source url to the resultset
			actualResultSet.addSourceRepository(tmpTarget.getUrl());
			targetResultSets.add(actualResultSet);
		}

		resultSet = getRankingalgorithm().computeRankedList(targetResultSets,
				tagQuery, getMaxQueryResults());

		// setting search query string
		resultSet.setSearchQuery(tagQuery);

		// adding source repositories to final ResultSet
		for (ResultSet s : targetResultSets) {
			resultSet.getSourceRepositories().addAll(s.getSourceRepositories());
		}
		return resultSet;

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

	@Override
	public void setTimeout(int maxDuration) {
		super.setTimeout(maxDuration);
		if (targets != null && targets.size() > 0) {
			for (Target t : targets) {
				t.setTimeout(maxDuration);
			}
		}
	}

	@Override
	public void setMaxQueryResults(int maxQueryResults) {
		super.setMaxQueryResults(maxQueryResults);
		if (targets != null && targets.size() > 0) {
			for (Target t : targets) {
				t.setMaxQueryResults(maxQueryResults);
			}
		}
	}

	@Override
	public void setQueryType(QueryType t) {
		super.setQueryType(t);
		if (targets != null && targets.size() > 0) {
			for (Target ta : targets) {
				ta.setQueryType(t);
			}
		}
	}

	@Override
	public void setQueryString(String query) {
		super.setQueryString(query);
		if (targets != null && targets.size() > 0) {
			for (Target ta : targets) {
				ta.setQueryString(query);
			}
		}
	}

	@Override
	public void setQueryArguments(QueryArguments queryArguments) {
		super.setQueryArguments(queryArguments);
		if (targets != null && targets.size() > 0) {
			for (Target ta : targets) {
				ta.setQueryArguments(queryArguments);
			}
		}
	}

	public List<Target> getTargets() {
		return this.targets;
	}

	public synchronized Date getLastUpdate() {
		return lastUpdate;
	}

	public synchronized void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		try {
		
		logger.debug("running Thread names: "
				+ ThreadHelper.getAllThreadNames());
		logger.debug("running Thread count: " + ThreadHelper.getThreadCount());

		logger.debug("setting run start values!");

		lastUpdate = null;
		setSearchCompleted(false);
		lastResults.clear();
		repositoriesSearched = 0;
		repositoriesToSearch = targets.size();

		if(targets.size()<1) {
			logger.error("No repositories where added to the Aggregator before! Returning immediately");
			this.searchCompleted = true;
		} else {
			logger.debug("starting target threads!");
			for (Target actualTarget : targets) {
				logger.debug("starting thread for target: " + actualTarget.getId());
				Thread actualThread = new Thread(actualTarget);
				actualThread.start();
			}
		}
		} catch (Exception e) {
			logger.error("An error occured while starting the targe threads: " + e.getMessage());
			this.searchCompleted = true;
		}
	}

	/**
	 * starts a search using the configured member variables using multiple
	 * threads
	 * 
	 * @param alwaysRerankOnNewResults
	 *            always rerank when new results arrive???
	 */
	public void startThreadedSearch(boolean alwaysRerankOnNewResults) {
		this.rerankonnewresults = alwaysRerankOnNewResults;
		run();
	}

	/**
	 * adds the given targets to the aggregators results
	 * 
	 * @param targetResults
	 *            the results to add
	 * @param targetid
	 *            the source repository id to use for identification of the
	 *            given results
	 */
	public synchronized void addTargetResults(ResultSet targetResults,
			String targeturl) {

		logger.debug("calling addTargetResults function for target: "
				+ targeturl);
		logger.debug("adding target results for target: " + targeturl);
		// increase counter
		repositoriesSearched++;

		// updating lastDate
		lastUpdate = new Date();

		// add results
		if (targetResults != null) {
			lastResults.getResults().addAll(targetResults.getResults());

			// add queried repository
			if (targetResults.getSourceRepositories() != null) {
				lastResults.addSourceRepository(targetResults
						.getSourceRepositories().get(0));
			}
		} else {
			// also add it even if no results were returned
			lastResults.addSourceRepository(targeturl);
		}

		// rank new result list
		if (getRankingalgorithm() != null) {
			if (rerankonnewresults) {
				getRankingalgorithm().computeRankedList(lastResults,
						getQueryString(), getMaxQueryResults());
				logger.debug("reranking actual results!");
			}

		}

		if (repositoriesToSearch == repositoriesSearched) {
			finish(false, false);
		}
	}

	public synchronized ResultSet getLastResults() {
		return lastResults;
	}

	public synchronized int getRepositoriesSearched() {
		return repositoriesSearched;
	}

	public void setSearchCompleted(boolean searchCompleted) {
		this.searchCompleted = searchCompleted;
	}

	public boolean isSearchCompleted() {
		return searchCompleted;
	}

	@Override
	public String toString() {
		String ret = "Aggregator: \n";
		ret += "currentResults Size = " + lastResults.size() + "\n";
		ret += "repositories Searched = " + repositoriesSearched + "\n";
		ret += "finished = " + searchCompleted + "\n";
		return ret;
	}

	@Override
	public void addRestriction(TargetRestriction restriction) {
		super.addRestriction(restriction);
		if (restriction != null) {
			for (Target t : targets) {
				t.addRestriction(restriction);
			}
		}
	}

	/**
	 * 
	 */
	public synchronized void finish(boolean errorOccured, boolean timeoutOccured) {
		logger.debug("all targets searched! executing finish function!");

		// rank results if needed
		if (getRankingalgorithm() != null) {
			if (!rerankonnewresults) {
				logger.debug("ranking final results!");
				lastResults = getRankingalgorithm().computeRankedList(
						lastResults, getQueryString(), getMaxQueryResults());

			}
		}

		setSearchCompleted(true);
		lastUpdate = new Date();

		logger.debug("running Thread names: "
				+ ThreadHelper.getAllThreadNames());
		logger.debug("running Thread count: " + ThreadHelper.getThreadCount());

	}

	/**
	 * stops all targets
	 */
	@Override
	public synchronized void stopOnTimeout() {
		logger.debug("called stop function. stopping all targets.");
		for (Target t : targets) {
			t.stopOnTimeout();
		}
		this.finish(false, true);
	}

}
