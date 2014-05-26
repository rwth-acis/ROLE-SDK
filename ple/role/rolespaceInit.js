dojo.registerModulePath("role","../role/role");
dojo.require("role.Work");
dojo.require("role.Container");
console.log("before addOnLoad init");

// Hack to get pubsub working: 
gadgets.rpc.getRelayUrl = function() { return document.location.protocol + "//" + document.location.host; };

// Hack to allow our own gadget ids:
/*shindig.Container.prototype.addGadget = function(gadget) {
  // If the id is already set, then keep it
  gadget.id = typeof gadget.id !== "undefined" ? gadget.id : this.getNextGadgetInstanceId();
  this.gadgets_[this.getGadgetKey_(gadget.id)] = gadget;
};*/

dojo.addOnLoad(function() {
	gadgets.openapp.connect(function(envelope, message) {
		if (envelope.event === "openapp") {
			if (envelope.hello === true) {

				envelope.source.postMessage(JSON.stringify({ OpenApplicationEvent:
				  { event: "openapp", welcome: true, message:
				  { postParentOnly: true } } }), "*");

			} else if (envelope.receipt === true) {

				shindig.container.gadgetAcceptedOAEvent(
				  envelope.source.frameElement.id);

			}
		} else if (typeof envelope.source !== "undefined") {

			if (envelope.source === window) {
				envelope.sender = "container";
			} else {
				var senderId = envelope.source.frameElement.id;
				var senderGadget = shindig.container.getGadget(
					senderId.substring(senderId.lastIndexOf('_') + 1));
				envelope.sender = senderGadget.specUrl;
				shindig.container.gadgetPublishedOAEvent(senderId, envelope);
			}
			envelope.viewer = shindig.container.layoutManager.getUser();
			var data = JSON.stringify({ OpenApplicationEvent: envelope });
			var frames = window.frames;
			for (var i = 0; i < frames.length; i++) {
				frames[i].postMessage(data, "*");
			}
		
		}
	});
	gadgets.pubsubrouter.init(function(id) {
		var index = id.lastIndexOf('_');
		return shindig.container.getGadget(id.substring(index+1)).specUrl;
	  }, {
	    onSubscribe: function(sender, channel) {
	      //console.log(sender + " subscribes to channel '" + channel + "'");
	      // return true to reject the request.
	    },
	    onUnsubscribe: function(sender, channel) {
	      //console.log(sender + " unsubscribes from channel '" + channel + "'");
	      // return true to reject the request.
	    },
	    onPublish: function(sender, channel, message) {
			var blocked = shindig.container.openappBlocked();
			if (!blocked) {
				if (channel == "openapp") {
					message.viewer = shindig.container.layoutManager.getUser();
					shindig.container.gadgetPublishedOAEvent(sender, message);
				}
				if (channel == "openapp-recieve") { // [sic]
					shindig.container.gadgetAcceptedOAEvent(sender);
				}
		      	//console.log(sender + " publishes '" + message + "' to channel '" + channel + "'");
			}
			return blocked;
	    }
	  });
	shindig.container = new role.Container();
    shindig.container.layoutManager = new role.Work({region: "center"});
	dijit.byId("rolespace").addChild(shindig.container.layoutManager);
	shindig.container.layoutManager.startup();
	shindig.container.layoutManager.resize();
});
