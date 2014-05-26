/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.representation.search.rome;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;

import de.imc.advancedMediaSearch.result.ResultComment;
import de.imc.advancedMediaSearch.result.ResultThumbnail;

/**
 * @author julian.weber@im-c.de
 *
 */
public class FeedModuleGenerator implements ModuleGenerator {

	private static final Namespace AMS_NS = Namespace.getNamespace("ams", IFeedModule.URI);

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
	

	/* (non-Javadoc)
	 * @see com.sun.syndication.io.ModuleGenerator#generate(com.sun.syndication.feed.module.Module, org.jdom.Element)
	 */
	public void generate(Module module, Element element) {
        // this is not necessary, it is done to avoid the namespace definition
        // in every item.
        Element root = element;
        while (root.getParent() != null && root.getParent() instanceof Element) {
            root = (Element) element.getParent();
        }

        root.addNamespaceDeclaration(AMS_NS);

        IFeedModule fm = (IFeedModule) module;
        element.addContent(generateSimpleElement("items-per-page", String.valueOf(fm.getItemsPerPage())));
        element.addContent(generateSimpleElement("search-query", String.valueOf(fm.getSearchQuery())));
        element.addContent(generateSimpleElement("search-url", String.valueOf(fm.getSearchUrl())));
        element.addContent(generateSimpleElement("search-time", String.valueOf(fm.getSearchTime())));
        element.addContent(generateSimpleElement("start-index", String.valueOf(fm.getStartIndex())));
        
        if(fm.getSourceRepositories()!=null) {
        	Element sourcerepos = new Element("source-repositories", AMS_NS);
        	for(String s : fm.getSourceRepositories()) {
        		sourcerepos.addContent(generateSimpleElement("repository", s));
        	}
        	element.addContent(sourcerepos);
        }
		
	}
	
    protected Element generateSimpleElement(String name, String value) {
        Element element = new Element(name, AMS_NS);
        element.addContent(value);
        return element;
    }
	
}
