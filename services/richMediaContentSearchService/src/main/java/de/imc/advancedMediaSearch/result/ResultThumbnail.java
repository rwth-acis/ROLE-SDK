package de.imc.advancedMediaSearch.result;

import de.imc.advancedMediaSearch.representation.search.ROMEFeedRepresentationGenerator;

import java.net.URL;

import org.apache.log4j.Logger;

public class ResultThumbnail implements Cloneable{

    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ROMEFeedRepresentationGenerator.class);
    
    private int width;
    private int height;
    private URL url;

    /**
     * @param width
     * @param height
     * @param url
     */
    public ResultThumbnail(int width, int height, URL url) {
        super();
        this.width = width;
        this.height = height;
        this.url = url;
    }

    /**
     * 
     */
    public ResultThumbnail() {
        super();
        this.width = 0;
        this.height = 0;
       this.url = null;

    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the url
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
	public Object clone(){
        return null;
    }
    
}
