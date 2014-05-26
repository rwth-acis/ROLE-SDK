dojo.provide("role.LayoutManager");

/**
 * The ROLE LayoutManager is responsible for displaying several spaces where each 
 * space corresponds to a set of gadgets and a controlpanel.
 * This class is abstract bookeping class that keeps track of spaces and delegates
 * how to get hold of the gadgetChromes (the node inside where each gadget will live).
 * @see role.Work for an example for a Layout can be realized using tabs and splitpanes. 
 */
dojo.declare("role.LayoutManager", shindig.LayoutManager, {
	constructor: function() {
		this.spaces = {};
		this.spaceCount = 0;
	},
	getChromeIdFromGadgetId: function(gadgetId) {
		return 'gadget-chrome-'+gadgetId;		
	},
	getGadgetIdFromChromeId: function(chromeId) {
		return chromeId.substring(14);
	},
	getGadgetChrome: function(gadget) {
		var chromeId = this.getChromeIdFromGadgetId(gadget.id);
		var space = this.getSpace(gadget.spaceId);
		return space ? space.getGadgetChrome(chromeId) : null;
	},
	getSpace: function(spaceId) {
		return this.spaces[spaceId];
	},
	registerSpace: function(space) {
		this.spaceCount++;
		this.spaces[this.spaceCount] = space;
		space.id = this.spaceCount;
	}
});
