/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result;

import java.util.Comparator;

/**
 * compares ResultEntities by using their overall rating
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class RatingResultEntityComparator implements Comparator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		if (arg0 == null && arg1 == null) {
			return 0;
		}
		if (arg0 == null && arg1 != null) {
			return -1;
		}
		if (arg0 != null && arg1 == null) {
			return 1;
		}

		ResultEntity args0 = (ResultEntity) arg0;
		ResultEntity args1 = (ResultEntity) arg1;

		if (args0.calculateOverallRating() > args1.calculateOverallRating()) {
			return 1;
		} else if (args0.calculateOverallRating() < args1
				.calculateOverallRating()) {
			return -1;
		} else {
			return 0;
		}
	}
}