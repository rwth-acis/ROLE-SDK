package de.imc.advancedMediaSearch.representation.search.rome;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;

public class AMSModuleParser implements ModuleParser{

	private static final Namespace AMS_NS  = Namespace.getNamespace("ams", AMSModule.URI);
//TODO: fix PARSER
	
	public String getNamespaceUri() {
		return AMSModule.URI;
	}

	public Module parse(Element dcRoot) {
        boolean foundSomething = false;
        
        AMSModule fm = new AMSModuleImpl();

        Element elem = dcRoot.getChild("entrySource", AMS_NS);
        if (elem != null) {
            foundSomething = true;
            fm.setSource(elem.getText());
        }
        
        elem = dcRoot.getChild("score", AMS_NS);
        if (elem != null) {
            foundSomething = true;
            fm.setScore(Double.parseDouble(elem.getText()));
        }
        
        elem = dcRoot.getChild("thumbnailUrl", AMS_NS);
        if (elem != null) {
            foundSomething = true;
            //fm.setThumbnail(elem.getText());
        }
        
        elem = dcRoot.getChild("length", AMS_NS);
        if (elem != null) {
            foundSomething = true;
            //fm.setLength(Double.parseDouble(elem.getText()));
        }
        
        elem = dcRoot.getChild("sourceRating", AMS_NS);
        if (elem != null) {
            foundSomething = true;
            fm.setSourceRating(Double.parseDouble(elem.getText()));
        }
        
        elem = dcRoot.getChild("userRating", AMS_NS);
        if (elem != null) {
            foundSomething = true;
            fm.setUserRating(Integer.parseInt(elem.getText()));
        }
        
        
        if(foundSomething){
        	return fm;
        }else{
        	return null;
        }
	}

}
