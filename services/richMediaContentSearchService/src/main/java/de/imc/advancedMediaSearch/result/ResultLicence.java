/**
 * @author weber
 * 24.11.2010
 */
package de.imc.advancedMediaSearch.result;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weber
 * 
 */
public class ResultLicence {
	private String name;
	private String url;
	private List<String> usage;
	private String owner;
	private String ownerEmail;
	private String ownerUrl;

	public ResultLicence() {
		setName(null);
		setUrl(null);
		usage = null;
		setOwner(null);
		setOwnerEmail(null);
		setOwnerUrl(null);
	}

	public ResultLicence(String name, String url, List<String> usage,
			String owner, String ownerEmail, String ownerUrl) {
		this.setName(name);
		this.setUrl(url);
		this.usage = usage;
		this.setOwner(owner);
		this.setOwnerEmail(ownerEmail);
		this.setOwnerUrl(ownerUrl);
	}

	public void addUsage(String usageString) {
		if (usageString != null) {
			if (this.usage == null) {
				this.usage = new ArrayList<String>();
			}
			this.usage.add(usageString);
		}
	}

	public void removeUsage(String usageString) {
		if (usageString != null) {
			if (this.usage != null) {
				this.usage.remove(usageString);
			}
		}
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
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param ownerEmail the ownerEmail to set
	 */
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	/**
	 * @return the ownerEmail
	 */
	public String getOwnerEmail() {
		return ownerEmail;
	}

	/**
	 * @param ownerUrl the ownerUrl to set
	 */
	public void setOwnerUrl(String ownerUrl) {
		this.ownerUrl = ownerUrl;
	}

	/**
	 * @return the ownerUrl
	 */
	public String getOwnerUrl() {
		return ownerUrl;
	}

	/**
	 * @return the usage
	 */
	public List<String> getUsage() {
		return usage;
	}

	/**
	 * @param usage the usage to set
	 */
	public void setUsage(List<String> usage) {
		this.usage = usage;
	}
	
	
	
}
