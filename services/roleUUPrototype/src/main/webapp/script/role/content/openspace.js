define([ "com", "jquery", "handlebars!./openspace" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		var baseUri = window.location.href.split("/");
		baseUri = baseUri[0] + "/" + baseUri[1] + "/" + baseUri[2];
		$(template({
			baseUri : baseUri
		})).appendTo(container);
		$(container).on("submit", "#openSpaceForm", function(event) {
			var uri;
			event.preventDefault();
			uri = $("#spaceId").val().trim();
			if (uri.length === 0) {
				alert("Please enter the name to be used within the space's URL.");
			} else if (uri.indexOf(" ") !== -1) {
				alert("Spaces are not allowed in the space's name.");
			} else {
				window.location = baseUri + "/spaces/" +
					encodeURIComponent(uri);
			}
		});
	}
	
}; });