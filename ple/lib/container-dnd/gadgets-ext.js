/**
 * Copyright 2009 Life360 - http://life360.com 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License. 
 */

/**
 * Summary: Machinery that builds upon the gadgets.* namespace defined mostly in Shindig's
 *          gadgets.js file to provide some custom styling for the title bar and some
 *          functionality for implementing a maximized view (which expects there to be a
 *          "maximizedView" element in the HTML page somewhere. See the accompanying file
 *          demo.html for details.
 *
 * The stock gadgets.js file should be included first! This file builds upon that one.
 * This file has some minimalistic Dojo dependencies for easing common operatiions such
 * as manipulating classes, style, querying by CSS selectors, etc.
*/

gadgets.my = function(opt_params) {
  gadgets.Gadget.call(this, opt_params);
  this.serverBase_ = '/gadgets/' // default gadget server
};
gadgets.my.inherits(shindig.IfrGadget);

//shortcut
gadgets.my.prototype.getId = function() {
    return "gadget-chrome-"+ this.id;
};

//overridden to add in elements for rounding out corners
gadgets.my.prototype.render = function(chrome) {
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

gadgets.my.prototype.swapToggleButton = function() {
    dojo.toggleClass(this.getToggleButtonId(), "gadgets-gadget-title-toggle-button-closed");

    //FIXME: this doesn't (semantically) belong in here
    dojo.query("#gadget-chrome-"+ this.id + " .ft").toggleClass("minimized");
};
gadgets.my.prototype.getToggleButtonId = function() {
    return this.getIframeId() + "_toggleButton";
};
gadgets.my.prototype.getMaximizeButtonId = function() {
    return this.getIframeId() + "_maximizeButton";
};
gadgets.my.prototype.getMinimizeButtonId = function() {
    return this.getIframeId() + "_minimizeButton";
};
gadgets.my.prototype.getSettingsButtonId = function() {
    return this.getIframeId() + "_settingsButton";
};
gadgets.my.prototype.handleMaximize = function() {

    //hide the columns and display the div that's full screen
    dojo.style("gadgets", "display", "none");
    dojo.style("maximizedGadget", "display", "block");

    //insert a placeholder before the gadget in question 
    var placeholder = dojo.doc.createElement("div");
    dojo.attr(placeholder, "id", "maximizedGadgetPlaceholder");
    dojo.place(placeholder, this.getId(), "after");

    //adjust the style on the maximized gadget so that the maximize "changes"
    //to a minimize icon
    dojo.style(this.getMaximizeButtonId(), "display", "none");
    dojo.style(this.getMinimizeButtonId(), "display", "block");

    //move the gadget into the maximizedGadget div
    dojo.place(this.getId(), "maximizedGadget", "first");

    //IE doesn't necessarily refresh, so force it
    dojo.style(this.getIframeId(), "display", "block");
};

gadgets.my.prototype.handleMinimize = function() {

    //restore the gadget to its placeholder
    dojo.place(this.getId(), "maximizedGadgetPlaceholder", "after");
    dojo.query("#maximizedGadgetPlaceholder").orphan();

    //adjust the style on the maximized gadget so that the minimize "changes"
    //to a maximize icon
    dojo.style(this.getMaximizeButtonId(), "display", "block");
    dojo.style(this.getMinimizeButtonId(), "display", "none");

    //hide the columns and display the div that's full screen
    dojo.style("gadgets", "display", "block");
    dojo.style("maximizedGadget", "display", "none");
}

gadgets.my.prototype.expandSettings = function() {

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

gadgets.my.prototype.cssClassTitleToggleButton = 'gadgets-gadget-title-toggle-button';
gadgets.my.prototype.cssClassTitleSettingsButton = 'gadgets-gadget-title-settings-button';
gadgets.my.prototype.cssClassTitleMaximizeButton = 'gadgets-gadget-title-maximize-button';
gadgets.my.prototype.cssClassTitleMinimizeButton = 'gadgets-gadget-title-minimize-button';
gadgets.my.prototype.getTitleBarContent = function(continuation) {
    continuation(
        '<div id="' + this.cssClassTitleBar + '-' + this.id + '" class="' + this.cssClassTitleBar + '">' +
            '<span class="' + this.cssClassTitleButtonBar + '">' +

                '<a id="'+ this.getToggleButtonId() +'" href="#" onclick="gadgets.container.getGadget('+ this.id +').handleToggle();gadgets.container.getGadget('+ this.id +').swapToggleButton();return false;" class="' + this.cssClassTitleToggleButton + '"></a>' +

                '<span id="' + this.getIframeId() + '_title" class="' + this.cssClassTitle + '">' + (this.title || '') + '</span>' +
                '<a id="'+ this.getMinimizeButtonId() +'" style="display:none" href="#" onclick="gadgets.container.getGadget('+ this.id +').handleMinimize();return false;" class="' + this.cssClassTitleMinimizeButton + '"></a>' +
                '<a id="'+ this.getMaximizeButtonId() +'" href="#" onclick="gadgets.container.getGadget('+ this.id +').handleMaximize();return false;" class="' + this.cssClassTitleMaximizeButton + '"></a>' +

                '<a id="'+ this.getSettingsButtonId() +'" href="#" onclick="gadgets.container.getGadget('+ this.id +').expandSettings();return false;" class="' + this.cssClassTitleSettingsButton + '"></a>' +
                '<br>' +
            '</span>' +
            '<div style="clear:both"></div>'+
        '</div>'+
            //'<a href="#" onclick="gadgets.container.getGadget('+ this.id +').handleOpenUserPrefsDialog();return false;" class="' + this.cssClassTitleButton + '">settings</a>' +

        '</div>'
    );
};

gadgets.my.Container = function() {
    shindig.IfrContainer.call(this);
};
gadgets.my.Container.inherits(shindig.IfrContainer);

gadgets.my.Container.prototype.gadgetClass = gadgets.my;

//override line 802 of gadgets.js
gadgets.container = new gadgets.my.Container();
