/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result;

import java.util.Comparator;

/**
 * @author julian.weber@im-c.de
 * 
 */
public enum ResultOrdering {
	relevance, published, viewcount, rating;

	/**
	 * creates a ResultOrdering type from the given string, returns null if no
	 * type could be matched to the given string
	 * 
	 * @param s
	 * @return
	 */
	public static ResultOrdering getOrderingFromString(String s) {
		if (s == null) {
			return null;
		} else if (s.equals(relevance.toString())) {
			return ResultOrdering.relevance;
		} else if (s.equals(published.toString())) {
			return ResultOrdering.published;
		} else if (s.equals(viewcount.toString())) {
			return ResultOrdering.viewcount;
		} else if (s.equals(rating.toString())) {
			return ResultOrdering.rating;
		} else {
			return null;
		}
	}

	/**
	 * returns a comparator for ResultEntities according to the given
	 * ResultOrdering option,
	 * returns null, if no matching ordering was found
	 * 
	 * @param ordering
	 * @return
	 */
	public static Comparator<ResultEntity> getComparatorForOrdering(
			ResultOrdering ordering) {
		if (ordering == ResultOrdering.relevance) {
			return new RelevanceResultEntityComparator();
		} else if (ordering == ResultOrdering.published) {
			return new PublishedResultEntityComparator();
		} else if (ordering == ResultOrdering.viewcount) {
			return new ViewcountResultEntityComparator();
		} else if (ordering == ResultOrdering.rating) {
			return new RatingResultEntityComparator();
		} else {
			return null;
		}
	}
}
