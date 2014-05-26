dojo.provide("role.ControlledSpace");
dojo.require("dijit.layout._LayoutWidget");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit._Templated");
dojo.require("role.ThreeColumnSpace");
dojo.require("role.ControlPanel");

dojo.declare("role.ControlledSpace", [dijit.layout._LayoutWidget, dijit._Templated], {
	templatePath: dojo.moduleUrl("role", "ControlledSpaceTemplate.html"),
	widgetsInTemplate: true,
	spaceClass: role.ThreeColumnSpace,
	controlClass: role.ControlPanel,
	constructor: function(params) {
		if(params.spaceDef.title) {
			this.title = params.spaceDef.title;
			if (dojo.isArray(params.spaceDef.participants)) {
				this.title += " ("+params.spaceDef.participants.length+")";
			}
		}
	},
	postCreate: function() {
		this.containerNode = this.domNode;
		this.inherited("postCreate", arguments);
		this.space = new this.spaceClass(this.spaceDef, this.spaceNode);
		this.control = new this.controlClass({space: this.space}, this.controlNode);
		dojo.connect(this.space, "afterRenderGadgets", this.control, "updateGadgets");
		if (!this.cockpit) {
			this.control.updateParticipants();
			this.control.updateFascilitators();			
		}
	},
	resize: function() {
		if (this.bc.resize) {
			this.bc.resize();	
		}
	},
	getSpace: function() {
		return this.space;
	},
	getControl: function() {
		return this.control;
	},
	startup: function() {
		this.inherited("startup", arguments);
		this.bc.startup();
	},
	gadgetPublishedOAEvent: function(gadgetId, event) {	
		this.control.gadgetPublishedOAEvent(gadgetId, event);
	},
	gadgetAcceptedOAEvent: function(gadgetId) {
		this.control.gadgetAcceptedOAEvent(gadgetId);
	}
});