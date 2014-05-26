/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.target.restrictions;

import de.imc.advancedMediaSearch.target.Target;

/**
 * abstract base class for all types of restriction restrictions have to
 * analyse. the exact subtype of Target to gain access to the specific search
 * query details related to that target. So all applyRestrictions methods should
 * implement private sub-methods for each available Target and select them
 * within a condition or case statement
 * 
 * @author julian.weber@im-c.de
 * 
 */
public abstract class TargetRestriction {
	private Target target;
	private String argumentString;

	/**
	 * constructs a TargetRestriction using the given string containing
	 * arguments
	 * 
	 * @param argumentString
	 */
	public TargetRestriction(String argumentString) {
		this.argumentString = argumentString;
	}

	public void setTarget(Target target) {
		this.target = target;
	}

	public Target getTarget() {
		return target;
	}

	/**
	 * override this function to do the restriction work in the target
	 */
	public abstract void applyRestriction();

	/**
	 * checks whether the assigned target is supported by this restriction.
	 * override accordingly
	 * 
	 * @return
	 */
	public abstract boolean targetSupported();

	public String getArgumentString() {
		return argumentString;
	}
	
	/**
	 * returns the http call parameter name of the restriction
	 * override this to reflect the proper argument
	 * @return the call parameter name of the restriction
	 */
	public abstract String getParameterName(); 
}
