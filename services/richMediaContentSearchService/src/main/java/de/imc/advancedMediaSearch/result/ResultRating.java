/**
 * @author weber
 * 24.11.2010
 */
package de.imc.advancedMediaSearch.result;

/**
 * @author weber
 *
 */
public class ResultRating implements Comparable<ResultRating> {
	private String source;
	private double rating;
	
	public ResultRating(double rating) {
		this.rating = rating;
		this.source = null;
	}
	
	public ResultRating(double rating, String source) {
		this.rating = rating;
		this.source = source;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the rating
	 */
	public double getRating() {
		return rating;
	}

	/**
	 * @param rating the rating to set
	 */
	public void setRating(double rating) {
		this.rating = rating;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ResultRating arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
