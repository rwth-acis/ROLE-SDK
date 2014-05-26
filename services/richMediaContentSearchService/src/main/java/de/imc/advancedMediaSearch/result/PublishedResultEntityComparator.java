/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result;

import java.util.Comparator;

/**
 * compares ResultEntities by using their published date, earlier date > later
 * date
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class PublishedResultEntityComparator implements Comparator {

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

		if (args0.getPublished() != null && args1.getPublished() != null) {
			if (args0.getPublished().before(args1.getPublished())) {
				return 1;
			} else if (args0.getPublished().after(args1.getPublished())) {
				return -1;
			} else {
				return 0;
			}
		} else if (args0.getPublished() == null && args1.getPublished() != null) {
			return -1;
		} else if (args0.getPublished() != null && args1.getPublished() == null) {
			return 1;
		} else {
			return 0;
		}

	}
}