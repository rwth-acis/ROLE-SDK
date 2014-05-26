dojo.provide("role.ControlPanel");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.declare("role.ControlPanel", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("role", "ControlPanelTemplate.html"),
	widgetsInTemplate: false,
	base: document.location.protocol + "//" + document.location.host,
	gadgetId2Node: {},
	constructor: function() {
	},
	gadgetPublishedOAEvent: function(gadgetId, event) {
		dojo.query("img", this.gadgetsNode).style("visibility", "hidden");
		var node = this.gadgetId2Node[gadgetId];
		if (node) {
			var img = dojo.query("img", node)[0];
			dojo.attr(img, "src", "theme/send.png");
			dojo.style(img, "visibility", "visible");
		}
	},
	gadgetAcceptedOAEvent: function(gadgetId) {
		var node = this.gadgetId2Node[gadgetId];
		if (node) {
			var img = dojo.query("img", node)[0];
			dojo.attr(img, "src", "theme/recieve.png");
			dojo.style(img, "visibility", "visible");
		}
	},
	updateGadgets: function() {
		dojo.attr(this.gadgetsNode, "innerHTML", "");
		this.gadgetId2Node = {};
		dojo.forEach(this.space.getGadgets(), function(gadget, index) {
			var node = document.createElement("div");
			this.gadgetId2Node[gadget.id] = node;
/*			if (index == 1) {
				dojo.attr(node, "innerHTML", "<img src='theme/send.png'></img><span>"+gadget.title+"</span>");				
			} else if (index == 3 || index== 4) {*/
				dojo.attr(node, "innerHTML", "<img style='visibility: hidden' src='theme/recieve.png'></img><span>"+gadget.title+"</span>");				
/*			} else {
				dojo.attr(node, "innerHTML", "<img src='theme/recieve.png'></img><span>"+gadget.title+"</span>");
			}*/
			this.gadgetsNode.appendChild(node);
		}, this);
	},
	updateParticipants: function() {
		var xhrArgs = {
			url: this.base+"/social/rest/people/"+this.space.spaceId+"/@friends",
			handleAs: "json-comment-optional",
			headers: {"accept": "application/json",
						"Content-type": "application/json"},
			load: dojo.hitch(this, function(data) {
				if (dojo.isArray(data.entry)) {
					this._updateParticipants(data.entry);
				}
			})
		};
		dojo.xhrGet(xhrArgs);
	},
	_onLoadFriends: function(dataResponse) {
		var ownerFriends = dataResponse.get('ownerFriends').getData();
		ownerFriends.each(dojo.hitch(this, function(person) {
			var node = document.createElement("div");
			dojo.attr(node, "innerHTML", person.getDisplayName());
			this.participantsNode.appendChild(node);
		}));
	},
	_updateParticipants: function(participants) {
		if (participants.length == 0) {
			dojo.style(this.participantsHead, "display", "none");
			return;
		}
		dojo.style(this.participantsHead, "display", "");
		dojo.attr(this.participantsNode, "innerHTML", "");
		dojo.forEach(participants, function(part) {
			var node = document.createElement("div");
			dojo.attr(node, "innerHTML", this._getPersonName(part));
			this.participantsNode.appendChild(node);
		}, this);
	},
	_getPersonName: function(person) {
		return person.displayName || person.name.formatted;
	},
	updateFascilitators: function() {
		var fascilitators = this.space.getFascilitators();
		if (fascilitators.length == 0) {
			dojo.style(this.fascilitatorsHead, "display", "none");
			return;
		}
		dojo.style(this.fascilitatorsHead, "display", "");
		dojo.attr(this.gadgetsNode, "innerHTML", "");
		dojo.forEach(fascilitators, function(fasc) {
			var node = document.createElement("div");
			dojo.attr(node, "innerHTML", fasc);
			this.fascilitatorsNode.appendChild(node);
		}, this);
	}
});