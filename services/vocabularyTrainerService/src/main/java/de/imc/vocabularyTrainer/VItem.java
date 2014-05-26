package de.imc.vocabularyTrainer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VItem {
/*
{"itemId":number,"term":String,"contexts":JSONArray,
	"translations":JSONArray,"images":JSONArray}	
*/
	
	int itemId;
	String term;
	List<VItemContext> contexts;
	List<VItemTranslation> translations;
	List<VItemImage> images;
	
	public VItem(int itemId, String term, List<VItemContext> contexts,
			List<VItemTranslation> translations, List<VItemImage> images) {
		super();
		setItemId(itemId);
		setTerm(term);
		setContexts(contexts);
		setTranslations(translations);
		setImages(images);
	}
	
	public VItem(JSONObject itemJSON) {
		super();
		
		try {
			setItemId(itemJSON.getInt("itemId"));

			setTerm(itemJSON.getString("term"));
			//TODO:

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in VItem getting id and term: "+ itemJSON.toString() + e.toString());
		}			
		
		try {
			//set contexts
			JSONArray contextJSONArray = itemJSON.getJSONArray("contexts");
			List<VItemContext> contextList = new ArrayList<VItemContext>();
			for(int i = 0;i<contextJSONArray.length();i++){
				VItemContext tmpContext = 
					new VItemContext(contextJSONArray.getJSONObject(i));
				contextList.add(tmpContext);
			}
			setContexts(contextList);
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in VItem getting contexts: "+ itemJSON.toString() + e.toString());
		}		
		
		try {
			//set translations
			JSONArray translationJSONArray = itemJSON.getJSONArray("translations");
			List<VItemTranslation> translationList = new ArrayList<VItemTranslation>();
			for(int i = 0;i<translationJSONArray.length();i++){
				VItemTranslation tmpTranslation = 
					new VItemTranslation(translationJSONArray.getJSONObject(i));
				translationList.add(tmpTranslation);
			}
			setTranslations(translationList);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in VItem getting translations: "+ itemJSON.toString() + e.toString());
		}		
		
		try {
			//set images
			JSONArray imageJSONArray = itemJSON.getJSONArray("images");
			List<VItemImage> imageList = new ArrayList<VItemImage>();
			for(int i = 0;i<imageJSONArray.length();i++){
				VItemImage tmpImage = 
					new VItemImage(imageJSONArray.getJSONObject(i));
				imageList.add(tmpImage);
			}
			setImages(imageList);
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in VItem getting images: "+ itemJSON.toString() + e.toString());
		}
	}	

	public JSONObject toJSON(){
		JSONObject itemJSON = new JSONObject();
		
		try {
			itemJSON.put("itemId", getItemId());
			itemJSON.put("term", getTerm());
			
			JSONArray contextsJSON = new JSONArray();
			Iterator<VItemContext> contextIter = contexts.iterator();
			while(contextIter.hasNext()){
				contextsJSON.put(contextIter.next().toJSON());
			}
			itemJSON.put("contexts", contextsJSON);
			
			JSONArray translationsJSON = new JSONArray();
			Iterator<VItemTranslation> translationIter = translations.iterator();
			while(translationIter.hasNext()){
				translationsJSON.put(translationIter.next().toJSON());
			}
			itemJSON.put("translations", translationsJSON);
			
			JSONArray imagesJSON = new JSONArray();
			Iterator<VItemImage> imagesIter = images.iterator();
			while(imagesIter.hasNext()){
				imagesJSON.put(imagesIter.next().toJSON());
			}
			itemJSON.put("images", imagesJSON);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return itemJSON;
	}	
	
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public List<VItemContext> getContexts() {
		return contexts;
	}

	public void setContexts(List<VItemContext> contexts) {
		if(contexts != null){
			this.contexts = contexts;
		}else{
			this.contexts = new ArrayList<VItemContext>();
		}
	}

	public List<VItemTranslation> getTranslations() {
		return translations;
	}

	public void setTranslations(List<VItemTranslation> translations) {
		if(translations != null){
			this.translations = translations;
		}else{
			this.translations = new ArrayList<VItemTranslation>();
		}
	}

	public List<VItemImage> getImages() {
		return images;
	}

	public void setImages(List<VItemImage> images) {
		if(images != null){
			this.images = images;
		}else{
			this.images = new ArrayList<VItemImage>();
		}
	}
	
	
}
