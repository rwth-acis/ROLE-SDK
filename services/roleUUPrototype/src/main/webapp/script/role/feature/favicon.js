define([ "com", "jquery" ], function(com, $) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#" ],
	
	load : function() {
		com.one("http://purl.org/role/ui/DOMReady#", "domReady", function() {
		    var link = document.createElement("link");
		    link.rel = "shortcut icon";
		    link.href = "/d/logo.icon";
		    document.getElementsByTagName("head")[0].appendChild(link);
		});
	}

}; });