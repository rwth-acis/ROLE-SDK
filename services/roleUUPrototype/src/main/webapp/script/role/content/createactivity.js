define([ "com", "jquery", "handlebars!./createactivity" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		$(template()).appendTo(container);
		$(container).on("submit", "#activityForm", function(event) {
			event.preventDefault();
			var name, message;
			name = $("#activityName").val().trim();
			if (name.length === 0) {
				alert("Please enter an activity name.");
				return;
			}
			message = {};
			message[openapp.ns.rdf + "type"] = "http://purl.org/role/terms/Activity";
			message[openapp.ns.dcterms + "title"] = name;
			gadgets.openapp.publish({
				intent : "add",
				type : "namespaced-properties",
				message : message
			});
		});
	}
	
}; });