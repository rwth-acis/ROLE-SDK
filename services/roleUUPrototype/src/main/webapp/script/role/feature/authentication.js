define([ "com", "jquery" ], function(com, $) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#" ],
	
	_isAuthenticated : false,
	
	load : function() {
		com.on("http://purl.org/role/ui/User#", "load", function(user) {

			if (user._context.uri.indexOf("cTrvjuLrGC") !== -1) {
				$("body").addClass("not-authenticated");
				this._isAuthenticated = false;
			} else {
				$("body").addClass("is-authenticated");
				this._isAuthenticated = true;
			}
			
		});
	}
	
}; });