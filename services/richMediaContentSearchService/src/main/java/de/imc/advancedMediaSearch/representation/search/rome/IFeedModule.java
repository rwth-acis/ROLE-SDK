/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.representation.search.rome;

import java.util.List;

import com.sun.syndication.feed.module.Module;

/**
 * @author julian.weber@im-c.de
 *
 */
public interface IFeedModule extends Module {
	public static final String URI = "http://www.im-c.de/AdvancedMediaSearch";
	
	public List<String> getSourceRepositories();
	public void setSourceRepositories(List<String> repos);
	
	public int getItemsPerPage();
	public void setItemsPerPage(int itemsperpage);
	
	public String getSearchQuery();
	public void setSearchQuery(String query);
	
	public String getSearchUrl();
	public void setSearchUrl(String url);
	
	public double getSearchTime();
	public void setSearchTime(double time);
	
	public int getStartIndex();
	public void setStartIndex(int index);
	
}
