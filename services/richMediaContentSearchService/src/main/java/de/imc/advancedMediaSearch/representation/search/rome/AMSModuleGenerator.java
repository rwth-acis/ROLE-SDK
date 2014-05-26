package de.imc.advancedMediaSearch.representation.search.rome;


import de.imc.advancedMediaSearch.result.ResultComment;
import de.imc.advancedMediaSearch.result.ResultThumbnail;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;


public class AMSModuleGenerator implements ModuleGenerator {

    private static final Namespace AMS_NS = Namespace.getNamespace("ams", AMSModule.URI);

    public String getNamespaceUri() {
        return AMSModule.URI;
    }

    private static final Set<Namespace> NAMESPACES;

    static {
        Set<Namespace> nameSpaces = new HashSet<Namespace>();
        nameSpaces.add(AMS_NS);
        NAMESPACES = Collections.unmodifiableSet(nameSpaces);
    }

    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    public void generate(Module module, Element element) {
        // this is not necessary, it is done to avoid the namespace definition
        // in every item.
        Element root = element;
        while (root.getParent() != null && root.getParent() instanceof Element) {
            root = (Element) element.getParent();
        }

        root.addNamespaceDeclaration(AMS_NS);

        AMSModule fm = (AMSModule) module;
        if (fm.getSource() != null) {
            element.addContent(generateSimpleElement("source", fm.getSource()));
        }
        if (fm.getScore() != null) {
            element.addContent(generateSimpleElement("score", String.valueOf(fm.getScore())));
        }
        if (fm.getThumbnail() != null && fm.getThumbnail().getUrl() != null) {
            ResultThumbnail thumbnail = fm.getThumbnail();
            
            Element thumbElem = new Element("thumbnail", AMS_NS);   
            
            thumbElem.addContent(generateSimpleElement("link", thumbnail.getUrl().toString()));
            thumbElem.addContent(generateSimpleElement("height", String.valueOf(thumbnail.getHeight())));
            thumbElem.addContent(generateSimpleElement("width", String.valueOf(thumbnail.getWidth())));
            
            element.addContent(thumbElem);
        }
        if (fm.getLength() != null) {
            element.addContent(generateSimpleElement("length", String.valueOf(fm.getLength())));
        }
        if (fm.getSourceRating() != null) {
            element.addContent(generateSimpleElement("sourceRating", String.valueOf(fm.getSourceRating())));
        }
        if (fm.getUserRating() != null) {
            element.addContent(generateSimpleElement("userRating", String.valueOf(fm.getUserRating())));
        }
             
        if (fm.getEmbeddedHTML() != null) {
            element.addContent(generateSimpleElement("embeddedHTML", fm.getEmbeddedHTML()));
        }
        if (fm.getMediaType() != null) {
            element.addContent(generateSimpleElement("mediaType", fm.getMediaType().toString()));
        }
        if (fm.getMimeType() != null) {
            element.addContent(generateSimpleElement("mimeType", fm.getMimeType()));
        }        
        if (fm.getNotes() != null) {
            List<ResultComment> commentList = fm.getNotes();
            
            for(ResultComment tmpComment : commentList){           
                Element commentElem = new Element("userComment", AMS_NS);   
                
                commentElem.addContent(generateSimpleElement("author", tmpComment.getUserId()));
                commentElem.addContent(generateSimpleElement("email", tmpComment.getUserEmail()));
                commentElem.addContent(generateSimpleElement("text", tmpComment.getText()));
                
                element.addContent(commentElem);
            }
        }
  
    }
      

    protected Element generateSimpleElement(String name, String value) {
        Element element = new Element(name, AMS_NS);
        element.addContent(value);
        return element;
    }

}
