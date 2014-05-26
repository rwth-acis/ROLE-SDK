package de.imc.advancedMediaSearch.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/** */
public class ResultSet {

	
	private static Logger logger = Logger.getLogger(ResultEntity.class);
	private List<ResultEntity> results;

	private String searchUrl;
	private String nextPageUrl;
	private String previousPageUrl;
	private String searchQuery;
	private int startIndex;
	private int itemsPerPage;
	private List<String> sourceRepositories;

	private double calculationTime;

	/**
	 * creates a new ResultSet object with empty results and repository list
	 */
	public ResultSet() {
		results = new ArrayList<ResultEntity>();
		sourceRepositories = new ArrayList<String>();

		searchQuery = null;
		nextPageUrl = null;
		previousPageUrl = null;
		searchUrl = null;
		startIndex = 0;
		itemsPerPage = 0;
	}

	public void clear() {
		results.clear();
	}

	public void add(ResultEntity resultEntity) {
		if (resultEntity != null) {
			results.add(resultEntity);
		}

	}

	/**
	 * shrinks the list to the result size of the given count
	 * sets itemsperPage accordingly
	 * @param count
	 */
	public ResultSet shrinkNumberOfResultsToSize(int count) {
		//return this when there are less results than our limitation -> no change
		if(results.size()<=count || count < 1) {
			this.itemsPerPage = results.size();
			logger.debug("No shrinking needed!");
			return this;
		}
		
		logger.debug("Performing resultset shrinking from: " + results.size() + " to: " + count);
		this.results = results.subList(0, count);
		this.setItemsPerPage(results.size());
		return this;
	}
	
	public List<ResultEntity> getResults() {
		return results;
	}

	public int size() {
		return results.size();
	}

	public Iterator<ResultEntity> iterator() {
		return results.iterator();
	}

	/**
	 * @return the searchUrl
	 */
	public String getSearchUrl() {
		return searchUrl;
	}

	/**
	 * @param searchUrl
	 *            the searchUrl to set
	 */
	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}

	/**
	 * @return the nextPageUrl
	 */
	public String getNextPageUrl() {
		return nextPageUrl;
	}

	/**
	 * @param nextPageUrl
	 *            the nextPageUrl to set
	 */
	public void setNextPageUrl(String nextPageUrl) {
		this.nextPageUrl = nextPageUrl;
	}

	/**
	 * @return the previousPageUrl
	 */
	public String getPreviousPageUrl() {
		return previousPageUrl;
	}

	/**
	 * @param previousPageUrl
	 *            the previousPageUrl to set
	 */
	public void setPreviousPageUrl(String previousPageUrl) {
		this.previousPageUrl = previousPageUrl;
	}

	/**
	 * @return the searchQuery
	 */
	public String getSearchQuery() {
		return searchQuery;
	}

	/**
	 * @param searchQuery
	 *            the searchQuery to set
	 */
	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	/**
	 * @return the startIndex
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * @param startIndex
	 *            the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * @return the itemsPerPage
	 */
	public int getItemsPerPage() {
		return itemsPerPage;
	}

	/**
	 * @param itemsPerPage
	 *            the itemsPerPage to set
	 */
	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	/**
	 * @return the sourceRepositories
	 */
	public List<String> getSourceRepositories() {
		return sourceRepositories;
	}

	/**
	 * @param sourceRepositories
	 *            the sourceRepositories to set
	 */
	public void setSourceRepositories(List<String> sourceRepositories) {
		this.sourceRepositories = sourceRepositories;
	}
	

	public void addSourceRepository(String s) {
		if (!sourceRepositories.contains(s) && s!=null && !s.equals("")) {
			this.sourceRepositories.add(s);
		}
	}

	/**
	 * @param calculationTime
	 */
	public void setCalculationTime(double calculationTime) {
		this.calculationTime = calculationTime;
	}
	
	public double getCalculationTime() {
		return this.calculationTime;
	}

}
