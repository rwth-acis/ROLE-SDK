define([ "com", "jquery" ], function(com, $) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#" ],
	
	load : function() {
		com.on("http://purl.org/role/ui/DOMReady#", "domReady", function() {
			$("<script src='/s/script/role_dashboard.js'></script>").appendTo(document.body);
		});
	}
	
}; });