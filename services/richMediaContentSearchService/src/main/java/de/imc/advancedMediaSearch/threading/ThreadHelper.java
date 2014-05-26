/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.threading;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class ThreadHelper {
	public static String getAllThreadNames() {
		Thread[] array = new Thread[Thread.activeCount()];

		Thread.enumerate(array);

		String s = "";
		for (Thread t : array) {
			if (t != null) {
				s += t.getName() + ", ";
			}
		}
		return s;
	}

	public static int getThreadCount() {
		return Thread.activeCount();
	}
}
