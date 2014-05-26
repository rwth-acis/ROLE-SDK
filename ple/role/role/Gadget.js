/*global dojo, role, shindig*/
dojo.provide("role.Gadget");

role.Gadget = function(opt_params) {
		shindig.BaseIfrGadget.call(this, opt_params);
	    this.serverBase_ = '/gadgets/'; // default gadget server
};
role.Gadget.inherits(shindig.BaseIfrGadget);

role.Gadget.prototype.serverBase_ = '/gadgets/'; // default gadget server
role.Gadget.prototype.cssClassTitleToggleButton = 'gadgets-gadget-title-toggle-button';
role.Gadget.prototype.cssClassTitleSettingsButton = 'gadgets-gadget-title-settings-button';
role.Gadget.prototype.cssClassTitleMaximizeButton = 'gadgets-gadget-title-maximize-button';
role.Gadget.prototype.cssClassTitleMinimizeButton = 'gadgets-gadget-title-minimize-button';
role.Gadget.prototype.getId = function() {
	return "gadget-chrome-"+ this.id;
};
role.Gadget.prototype.getSpace = function() {
	return shindig.container.layoutManager.getSpace(this.spaceId);
};

	//overridden to add in elements for rounding out corners
role.Gadget.prototype.render = function(chrome) {
		if (chrome) {
		    this.getContent(function(content) {
		        var c = "";
		        if(navigator.userAgent.match(/Windows/)) {
		            c += " win";
		        }
		        if(navigator.userAgent.match(/Safari/)) {
		            c += " webkit";
		        }
		
		        //rounded corners...
		        var header = ''+
		            '<div class="hd'+ c +'">'+
		                '<div class="tl"></div>'+ 
		                '<div class="tlf"></div>'+ 
		                '<div class="tr"></div>'+ 
		                '<div class="trf"></div>'+ 
		            '</div>'
		            ;
		
		        var footer = ''+
		            '<div class="ft">'+
		                '<div class="bl"></div>'+ 
		                '<div class="br"></div>'+ 
		            '</div>'
		            ;
		
		        chrome.innerHTML = header + content + footer;
		    }); 
	}
};

role.Gadget.prototype.swapToggleButton = function() {
	dojo.toggleClass(this.getToggleButtonId(), "gadgets-gadget-title-toggle-button-closed");

	//FIXME: this doesn't (semantically) belong in here
	dojo.query("#gadget-chrome-"+ this.id + " .ft").toggleClass("minimized");
};

role.Gadget.prototype.getToggleButtonId = function() {
	return this.getIframeId() + "_toggleButton";
};

role.Gadget.prototype.getMaximizeButtonId = function() {
	return this.getIframeId() + "_maximizeButton";
};

role.Gadget.prototype.getMinimizeButtonId = function() {
	return this.getIframeId() + "_minimizeButton";
};
role.Gadget.prototype.getSettingsButtonId = function() {
    return this.getIframeId() + "_settingsButton";
};
role.Gadget.prototype.handleMaximize = function() {
	this.getSpace().handleMaximize(this);
};
role.Gadget.prototype.handleMinimize = function() {
	this.getSpace().handleMinimize(this);
};
role.Gadget.prototype.expandSettings = function() {
	if (dojo.hasClass(this.getSettingsButtonId(), "gadgets-gadget-title-settings-button-active")) return;
	
	    dojo.addClass(this.getSettingsButtonId(), "gadgets-gadget-title-settings-button-active");
	
	    var menu = dojo.doc.createElement("div");
	    dojo.attr(menu, "id", this.getSettingsButtonId() + "_menu");
	    dojo.addClass(menu, "gadgets-gadget-title-settings-button-menu");
	
	
	    //set up handler for clicking so that we can analyze the exact click location
	    //and map back to a menu item in the image
	    var menuClickHandle = dojo.connect(menu, "onclick", function(evt) {
	        if (evt.layerY <= 22) { //image is roughly 44px
	            alert("not yet implemented : share action");
	        } else {
	            alert("not yet implemented : delete action");
	        }
	    });
	
	    var coords = dojo.coords(this.getSettingsButtonId());
	    dojo.style(menu, {
	        left : coords.x-42 + "px"/*based on width of image in css class*/,
	        top : coords.y + coords.h + "px",
	        display : "block",
	        zIndex : 1000
	    });
	
	    var overlay = dojo.doc.createElement("div");
	    dojo.style(overlay, {
	        position: "absolute",
	        height : dojo.style("gadget-chrome-"+this.id, "height") + "px",
	        width : dojo.style("gadget-chrome-"+this.id, "width") + "px",
	        zIndex : 999,
	        display : "block"
	    });
	
	    dojo.addClass(overlay, "gadgetOverlay");
	    dojo.place(overlay, "gadget-chrome-"+this.id, "first");
	    dojo.body().appendChild(menu);
	
	    var self=this;
	    setTimeout(function() {
	        var h = dojo.connect(document, "onclick", self, function(evt) {
	            dojo.query(".gadgetOverlay").orphan();
	            dojo.disconnect(menuClickHandle);
	            menu.parentNode.removeChild(menu);
	            dojo.removeClass(this.getSettingsButtonId(), "gadgets-gadget-title-settings-button-active");
	            dojo.disconnect(h);
	        })
		}, 100); /*FIXME: cleaner way to miss the current onclick event? */
	};
	
	role.Gadget.prototype.getTitleBarContent = function(continuation) {
	    continuation(
	        '<div id="' + this.cssClassTitleBar + '-' + this.id + '" class="' + this.cssClassTitleBar + '">' +
	            '<span class="' + this.cssClassTitleButtonBar + '">' +
	
	                '<a id="'+ this.getToggleButtonId() +'" href="#" onclick="shindig.container.getGadget('+ this.id +').handleToggle();shindig.container.getGadget('+ this.id +').swapToggleButton();return false;" class="' + this.cssClassTitleToggleButton + '"></a>' +
	
	                '<span id="' + this.getIframeId() + '_title" class="' + this.cssClassTitle + '">' + (this.title || '') + '</span>' +
	                '<a id="'+ this.getMinimizeButtonId() +'" style="display:none" href="#" onclick="shindig.container.getGadget('+ this.id +').handleMinimize();return false;" class="' + this.cssClassTitleMinimizeButton + '"></a>' +
	                '<a id="'+ this.getMaximizeButtonId() +'" href="#" onclick="shindig.container.getGadget('+ this.id +').handleMaximize();return false;" class="' + this.cssClassTitleMaximizeButton + '"></a>' +
	
	                '<a id="'+ this.getSettingsButtonId() +'" href="#" onclick="shindig.container.getGadget('+ this.id +').expandSettings();return false;" class="' + this.cssClassTitleSettingsButton + '"></a>' +
	                '<br>' +
	            '</span>' +
	            '<div style="clear:both"></div>'+
	        '</div>'+
	            //'<a href="#" onclick="shindig.container.getGadget('+ this.id +').handleOpenUserPrefsDialog();return false;" class="' + this.cssClassTitleButton + '">settings</a>' +
	
	        '</div>'
	    );
	};
role.Gadget.prototype.getAdditionalParams = function() {
	return "&bpc=1";
};