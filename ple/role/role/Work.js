dojo.provide("role.Work");
dojo.require("role.ControlledSpace");
dojo.require("role.LayoutManager");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit._Templated");
dojo.require("dijit.layout._LayoutWidget");
dojo.require("dijit.layout.TabContainer");
dojo.require("dojox.layout.ExpandoPane");

dojo.declare("role.Work", [dijit.layout._LayoutWidget, dijit._Templated, role.LayoutManager], {
	templatePath: dojo.moduleUrl("role", "WorkTemplate.html"),
    widgetsInTemplate: true,
	base: document.location.protocol + "//" + document.location.host,
	controlledSpaces: [],
	cockpitControlledSpace: null,
	_spacesInitialized: false,
	
	constructor: function(args) {
		this.controlledSpaces = [];
	},
	resize: function() {
		this.inherited("resize", arguments);
		if (this.bc.resize) {
			this.bc.resize();		
		}
		if (this.cockpit.resize) {
			this.cockpit.resize();
		}
	},
	startup: function() {
		this.inherited("startup", arguments);
		this.cockpit.startup();
	},
	postCreate: function() {
		this.containerNode = this.domNode;
		this.inherited("postCreate", arguments);
		console.log("work is created");
		//When a user selects a tab, make sure an openapp event is sent to all gadgets which tab is now active.
		dojo.connect(this.desktop, "selectChild", this, this._contSpaceSelected);
	},
	_contSpaceSelected: function(contSpace) {
		if (!this._spacesInitialized) {
			return;
		}
		var spaceId = contSpace.getSpace().spaceId;
		var envelope = {
			event: "state",
			type: "namespaced-properties",
			sharing: "public",
			date: new Date(),
			message: {
//					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type": "http://www.w3.org/2005/01/wai-rdf/GUIRoleTaxonomy#tab",
//					"http://www.w3.org/2000/01/rdf-schema#isDefinedBy": this.base+"/social/rest/people/"+spaceId,
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type": "http://www.role-project.eu/ple#Space",
		            "http://purl.org/dc/terms/title": contSpace.title,
					"http://www.role-project.eu/ple#spaceId": spaceId
		     }
		};
		//gadgets.pubsubrouter.publish("openapp", envelope);
		gadgets.openapp.publish(envelope);
	},
	createSpace: function(params) {
		console.log("createSpace is called");
		var contSpace = new role.ControlledSpace(params);
		this.controlledSpaces.push(contSpace);
		this.desktop.addChild(contSpace);
		var space = contSpace.getSpace();
		this.registerSpace(space);
		space.renderGadgets();
	},
	createCockpit: function(params) {
		params.cockpit = true;
		var contSpace = new role.ControlledSpace(params);
		this.cockpitControlledSpace = contSpace;
		contSpace.attr("region", "center");
		this.cockpitBC.addChild(contSpace);
		var space = contSpace.getSpace();
		this.registerSpace(space);
		space.renderGadgets();
	},
	setUser: function(user) {
		this.user = user;
		this.initCockpit();
		this.initSpaces();
	},
	getUser: function() {
		return this.user;
	},
	initCockpit: function() {
		//cockpit, get the gadgets:
		//"social/rest/appdata/"+user+"/@self"
		this.createCockpit({spaceDef: {spaceId: this.user}});
	},
	initSpaces: function() {
		//Get the spaces for user:
		//"social/rest/people/"+user+"/@spaces"
		var xhrArgs = {
			url: this.base+"/social/rest/people/"+this.user+"/@friends",
			handleAs: "json-comment-optional",
			headers: {"accept": "application/json",
						"Content-type": "application/json"},
			load: dojo.hitch(this, function(data) {
				var spaces = data.entry;
				if (dojo.isArray(spaces)) {
					dojo.forEach(spaces, dojo.hitch(this, function(space) {
						this.createSpace({spaceDef: {spaceId: space.id, title: this._getSpaceName(space)}});
					}));
					this._spacesInitialized = true;
					//If there is at least one space, send out the open application event to all the gadgets.
					if (this.controlledSpaces.length > 0) {
						this._contSpaceSelected(this.controlledSpaces[0]);
					}
				}
			})
		};
		dojo.xhrGet(xhrArgs);
		//
		// Then, for every spaceid:
		//"social/rest/people/"+spaceid+"/@members
		//"social/rest/people/"+spaceid+"/@administrators
		//"social/rest/appdata/"+spaceid+"/@self/ple"
		
	},
	_getSpaceName: function(space) {
		return space.displayName || space.name.formatted;
	},


	gadgetPublishedOAEvent: function(gadgetChromeId, event) {
		var gadgetId = typeof gadgetChromeId !== "undefined" ? this.getGadgetIdFromChromeId(gadgetChromeId) : "";
		dojo.forEach(this.controlledSpaces, function(csp) {
			csp.gadgetPublishedOAEvent(gadgetId, event);
		});
		if (this.cockpitControlledSpace) {
			this.cockpitControlledSpace.gadgetPublishedOAEvent(gadgetId, event);
		}
	},
	gadgetAcceptedOAEvent: function(gadgetChromeId) {
		var gadgetId = typeof gadgetChromeId !== "undefined" ? this.getGadgetIdFromChromeId(gadgetChromeId) : "";
		dojo.forEach(this.controlledSpaces, function(csp) {
			csp.gadgetAcceptedOAEvent(gadgetId);
		});
		if (this.cockpitControlledSpace) {
			this.cockpitControlledSpace.gadgetAcceptedOAEvent(gadgetId);
		}
	}

});