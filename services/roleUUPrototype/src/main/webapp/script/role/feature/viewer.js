define([ "com", "jquery" ], function(com, $) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#",
	               "http://purl.org/openapp/int/EventListener#" ],
	
	load : function() {
	},
	
	openAppEvent : function(envelope, message) {
		var state, views, view;
		if (envelope.source == window && envelope.event === "statechange") {
			state = {};
			state.uri = envelope.uri;
			state.context = message["http://purl.org/openapp/context"];
			state.predicate = message["http://purl.org/openapp/predicate"];
			state.params = $.url().param();
			if (typeof console !== "undefined") {
				console.log("Viewer changing state to " + JSON.stringify(state));
			}
			views = [];
			com.invoke("http://purl.org/role/ui/View#", "query", state, views);
			views.sort(function(a, b) {
				return b.score - a.score;
			});
			if (views.length === 0) {
				alert("No view is available for this resource: " + state.uri);
			} else {
				view = views[0].component;
				com.trigger(view, "http://purl.org/role/ui/View#", "select");
				view.activate(state);
			}
		}
	}
	
}; });