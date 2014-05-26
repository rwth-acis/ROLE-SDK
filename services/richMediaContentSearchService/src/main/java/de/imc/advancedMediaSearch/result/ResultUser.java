/**
 * @author weber
 * 24.11.2010
 */
package de.imc.advancedMediaSearch.result;


/**
 * @author weber
 *
 */
public class ResultUser {
	private String name;
	private String email;
	private String userId;
	private String profileUrl;
	private String source;
	
	public ResultUser() {
		setName(null);
		setEmail(null);
		setUserId(null);
		setProfileUrl(null);
		setSource(null);
	}
	
	public ResultUser(String name, String email, String userId, String profileUrl, String source) {
		this.setName(name);
		this.setEmail(email);
		this.setUserId(userId);
		this.setProfileUrl(profileUrl);
		this.setSource(source);
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

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param profileUrl the profileUrl to set
	 */
	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	/**
	 * @return the profileUrl
	 */
	public String getProfileUrl() {
		return profileUrl;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((profileUrl == null) ? 0 : profileUrl.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultUser other = (ResultUser) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (profileUrl == null) {
			if (other.profileUrl != null)
				return false;
		} else if (!profileUrl.equals(other.profileUrl))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	
	
}
