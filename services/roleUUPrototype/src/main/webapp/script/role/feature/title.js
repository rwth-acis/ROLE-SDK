define([ "com", "jquery" ], function(com, $) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#" ],
	
	load : function() {
		com.one("http://purl.org/role/ui/DOMReady#", "domReady", function() {
			com.on("http://purl.org/role/ui/View#", "update", function(view) {
				com.one("http://purl.org/role/ui/View#", "select", function(selectedView) {
					var title;
					if (view === selectedView) {
						title = view.getTitle();
						if (typeof title !== "undefined" && title !== null) {
							document.title = "ROLE - " + title;
						}
					}
				});
			});
		});
	}

}; });