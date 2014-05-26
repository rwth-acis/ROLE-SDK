dojo.provide("role.Container");
dojo.require("role.Gadget");

dojo.declare("role.Container", shindig.IfrContainer, {
	gadgetClass: role.Gadget,
	constructor: function() {
		this.view_ = "home";
		this.nocache_ = 1;
	},
	
	/**
	 * @return true if openapp event broadcasted is disabled.
	 */
	openappBlocked: function() {
		return false;
	},
	/**
	 * 
	 * @param {Object} gadgetChromeId of the gadget who just initated an openapplication event to be broadcasted.
	 * @param {Object} event the initiated event
	 */
	gadgetPublishedOAEvent: function(gadgetChromeId, event) {
		this.layoutManager.gadgetPublishedOAEvent(gadgetChromeId, event);
	},
	
	/**
	 * 
	 * @param {Object} gadgetChromeId of a gadget who recieved an understood an event (accepted it). 
	 */
	gadgetAcceptedOAEvent: function(gadgetChromeId) {
		this.layoutManager.gadgetAcceptedOAEvent(gadgetChromeId);
	}
});