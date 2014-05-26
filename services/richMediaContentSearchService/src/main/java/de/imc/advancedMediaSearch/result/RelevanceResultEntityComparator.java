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
public class RelevanceResultEntityComparator implements Comparator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if (o1 == null && o2 != null) {
			return -1;
		}
		if (o1 != null && o2 == null) {
			return 1;
		}
		if (o1 == null && o2 == null) {
			return 0;
		}

		ResultEntity args1 = (ResultEntity) o1;
		ResultEntity args2 = (ResultEntity) o2;

		if (args1.getScore() < args2.getScore()) {
			return -1;
		} else if (args1.getScore() > args2.getScore()) {
			return 1;
		} else {
			return 0;
		}
	}

}
