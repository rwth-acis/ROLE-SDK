/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.representation.search.rome;

import java.util.List;

import com.sun.syndication.feed.module.ModuleImpl;

/**
 * @author julian.weber@im-c.de
 *
 */
public class FeedModuleImpl extends ModuleImpl implements IFeedModule {

	private List<String> sourceRepos;
	private int itemsPerPage;
	private String searchQuery;
	private String searchUrl;
	private double searchTime;
	private int startIndex;
	
	/**
	 * @param beanClass
	 * @param uri
	 */
	public FeedModuleImpl() {
		super(IFeedModule.class, IFeedModule.URI);
	}

	/* (non-Javadoc)
	 * @see com.sun.syndication.feed.CopyFrom#getInterface()
	 */
	public Class<IFeedModule> getInterface() {
		return IFeedModule.class;
	}

	/* (non-Javadoc)
	 * @see com.sun.syndication.feed.CopyFrom#copyFrom(java.lang.Object)
	 */
	public void copyFrom(Object obj) {
		IFeedModule mod = (IFeedModule) obj;
		setSourceRepositories(mod.getSourceRepositories());
		setItemsPerPage(mod.getItemsPerPage());
		setSearchQuery(mod.getSearchQuery());
		setSearchUrl(mod.getSearchUrl());
		setSearchTime(mod.getSearchTime());
		setStartIndex(mod.getStartIndex());
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#getSourceRepositories()
	 */
	public List<String> getSourceRepositories() {
		return sourceRepos;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#setSourceRepositories(java.util.List)
	 */
	public void setSourceRepositories(List<String> repos) {
		this.sourceRepos = repos;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#getItemsPerPage()
	 */
	public int getItemsPerPage() {
		return itemsPerPage;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#setItemsPerPage(int)
	 */
	public void setItemsPerPage(int itemsperpage) {
		this.itemsPerPage = itemsperpage;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#getSearchQuery()
	 */
	public String getSearchQuery() {
		return this.searchQuery;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#setSearchQuery(java.lang.String)
	 */
	public void setSearchQuery(String query) {
		this.searchQuery = query;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#getSearchUrl()
	 */
	public String getSearchUrl() {
		return this.searchUrl;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#setSearchUrl(java.lang.String)
	 */
	public void setSearchUrl(String url) {
		this.searchUrl = url;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#getSearchTime()
	 */
	public double getSearchTime() {
		return this.searchTime;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#setSearchTime(int)
	 */
	public void setSearchTime(double time) {
		this.searchTime = time;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#getStartIndex()
	 */
	public int getStartIndex() {
		return this.startIndex;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.representation.search.rome.IFeedModule#setStartIndex(int)
	 */
	public void setStartIndex(int index) {
		this.startIndex = index;
	}

}
