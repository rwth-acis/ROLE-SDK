/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class LearningList extends ResultEntity {
	// TODO: Implement LearningList class
	private ResultSet items;

	public LearningList() {
		this.id = 0;
		this.title = null;
		this.description = null;
		this.items = null;
		this.setMediaType(MediaType.LIST);
	}

	public LearningList(long id, String title, String description) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.items = null;
		this.setMediaType(MediaType.LIST);
	}

	public LearningList(long id, String title, String description,
			ResultSet items) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.items = items;
		this.setMediaType(MediaType.LIST);
	}


	public ResultSet getItems() {
		return items;
	}

	public boolean isEmpty() {
		if (this.items == null) {
			return true;
		} else {
			if (this.items.size() < 1) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * adds an Entity to the learning list
	 * @param item the entity to add
	 */
	public void addEntity(ResultEntity item) {
		if (item != null) {
			if (this.items == null) {
				this.items = new ResultSet();
			}
			this.items.add(item);
		}
	}

}
