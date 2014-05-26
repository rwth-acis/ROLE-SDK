/**
 * @author weber
 * 24.11.2010
 */
package de.imc.advancedMediaSearch.result;

/**
 * @author weber
 *
 */
public class ResultTag {
	private String name;
	private String source;
	
	public ResultTag(String name) {
		this.name = name;
		this.source = null;
	}
	
	public ResultTag(String name, String source) {
		this.name = name;
		this.source = source;
	}
	
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	
}
