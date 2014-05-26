/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.exceptions;

/**
 * @author julian.weber@im-c.de
 *
 */
public class IllegalArgumentException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4424905213132040864L;
	private static final String DEFAULT_MESSAGE = "Illegal argument detected!";
	
	public IllegalArgumentException() {
		super(DEFAULT_MESSAGE);
	}
	
	public IllegalArgumentException(String msg) {
		super(msg);
	}
	
	
	public static void checkArgumentForNullValues(Object o) throws IllegalArgumentException {
		if(o==null) {
			throw new IllegalArgumentException("Null argument detected!");
		}
	}
}
