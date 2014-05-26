package de.imc.vocabularyTrainer;

import java.util.List;

import org.json.JSONObject;

public class UserVItem extends VItem {

	public UserVItem(int itemId, String term, List<VItemContext> contexts,
			List<VItemTranslation> translations, List<VItemImage> images) {
		super(itemId, term, contexts, translations, images);

	}

	public UserVItem(JSONObject itemJSON) {
		super(itemJSON);

	}

	
}
