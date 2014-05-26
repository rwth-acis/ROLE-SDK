define([ "com", "jquery" ], function(com, $) { return {

	interfaces : [ "http://purl.org/role/ui/User#",
	               "http://purl.org/role/ui/Feature#" ],
	
	_uri : null,
	
	_context : null,
	
	load : function() {
		com.on("http://purl.org/role/ui/DOMReady#", "domReady", function() {
			var userUri = "http://" + document.location.host + "/user";
			openapp.resource.get(userUri, this.initialize.bind(this));
		}.bind(this));
	},
	
	initialize : function(context) {
		this._context = context;
		this._uri = context.uri;
		com.trigger(this, "http://purl.org/role/ui/User#", "load");
	}
	
}; });