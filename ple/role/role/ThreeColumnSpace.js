dojo.provide("role.ThreeColumnSpace");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("role.Space");

dojo.declare("role.ThreeColumnSpace", [dijit._Widget, dijit._Templated, role.Space], {
	templatePath: dojo.moduleUrl("role", "ThreeColumnSpaceTemplate.html"),
	widgetsInTemplate: false,
	constructor: function() {
		this.column1Count = 0;
		this.column2Count = 0;
		this.column3Count = 0;		
	},
	createGadgetChrome: function(chromeId) {
		var columnNr;
		if (this.column2Count < this.column1Count) {
			columnNr = 2;
		} else if (this.column3Count < this.column2Count) {
			columnNr = 3;			
		} else {
			columnNr = 1;
		}
		this["column"+columnNr+"Count"]++;
		var node = document.createElement("div");
		this["column"+columnNr].appendChild(node);
		dojo.toggleClass(node, "gadgets-gadget-chrome");
		dojo.attr(node, "id", chromeId);
		gadgets.my.makeGadgetDnD(node, this.domNode);
		return node;
	},
	
	handleMaximize: function(gadget) {
	    //hide the columns and display the div that's full screen
	    dojo.style(this.gadgetsNode, "display", "none");
	    dojo.style(this.maximizedGadget, "display", "block");
	
	    //insert a placeholder before the gadget in question 
	    var placeholder = dojo.doc.createElement("div");
	    dojo.attr(placeholder, "id", "maximizedGadgetPlaceholder");
	    dojo.place(placeholder, gadget.getId(), "after");
	
	    //adjust the style on the maximized gadget so that the maximize "changes"
	    //to a minimize icon
	    dojo.style(gadget.getMaximizeButtonId(), "display", "none");
	    dojo.style(gadget.getMinimizeButtonId(), "display", "block");
	
	    //move the gadget into the maximizedGadget div
	    dojo.place(gadget.getId(), this.maximizedGadget, "first");
	
	    //IE doesn't necessarily refresh, so force it
	    dojo.style(gadget.getIframeId(), "display", "block");
	},
	handleMinimize: function(gadget) {
	    //restore the gadget to its placeholder
	    dojo.place(gadget.getId(), "maximizedGadgetPlaceholder", "after");
	    dojo.query("#maximizedGadgetPlaceholder").orphan();
	
	    //adjust the style on the maximized gadget so that the minimize "changes"
	    //to a maximize icon
	    dojo.style(gadget.getMaximizeButtonId(), "display", "block");
	    dojo.style(gadget.getMinimizeButtonId(), "display", "none");
	
	    //hide the columns and display the div that's full screen
	    dojo.style(this.gadgetsNode, "display", "block");
	    dojo.style(this.maximizedGadget, "display", "none");
	}
});