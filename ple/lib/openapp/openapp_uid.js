gadgets.openapp = function() {
	return {
		RDF: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
		connect: function(callback) {
			gadgets.pubsub.subscribe("openapp", function(sender, envelope) {
				envelope.sender = sender;
				callback(envelope, envelope.message);
			});
		},
		disconnect: function() {
			gadgets.pubsub.unsubscribe("openapp");
		},
		publish: function(envelope, message) {
			envelope.event = envelope.event || "select";
			envelope.sharing = envelope.sharing || "public";
			envelope.date = envelope.date || new Date();
			envelope.message = message || envelope.message;
			
			// extensions...
			// müssen die explizit angegeben werden??
			envelope.userId = envelope.userId || "anonymous user";
			envelope.widgetUrl = envelope.widgetUrl || "anonymous widgetUrl";
			
			gadgets.pubsub.publish("openapp", envelope);
		}
	};
}();
