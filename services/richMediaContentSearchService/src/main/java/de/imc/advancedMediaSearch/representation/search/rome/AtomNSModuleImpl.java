package de.imc.advancedMediaSearch.representation.search.rome;

import com.sun.syndication.feed.module.ModuleImpl;

public class AtomNSModuleImpl extends ModuleImpl implements AtomNSModule {
    /**
     * 
     */
    private static final long serialVersionUID = 108678964200992739L;
    
    private String link;

    public AtomNSModuleImpl() {
        super(AtomNSModule.class, URI);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Class<AtomNSModule> getInterface() {
        return AtomNSModule.class;
    }

    public void copyFrom(Object obj) {
        AtomNSModule module = (AtomNSModule) obj;
        module.setLink(this.link);
    }
}

