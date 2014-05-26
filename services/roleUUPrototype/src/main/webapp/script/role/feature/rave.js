define([ "com", "rave" ], function(com, rave) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#" ],
	
	load : function() {
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {
			rave.setContext(space.getUri() + "/role:rave/");
			rave.initProviders();
			rave.layout.init();
		});
	}
	
}; });