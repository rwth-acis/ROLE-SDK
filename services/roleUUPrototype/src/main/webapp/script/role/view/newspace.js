define([ "com", "jquery", "../ui/ui", "../header/signin", "../feature/dashboardinit",
         "../content/createspace" ], function(
		com, $, ui, signInHeader, dashboardInitFeature, createSpaceContent) { return {

	interfaces : [ "http://purl.org/role/ui/View#" ],
	
	query : function(state, views) {
		var match, id;
		if (state.predicate === openapp.ns.role + "spaceService") {
			match = window.location.href.match(/\/spaces\/(.*)/);
			id = (match !== null && match.length) > 1 ? match[1] : "";
			if (id.indexOf("?") !== -1) {
				id = id.substring(0, id.indexOf("?"));
			}
			if (id.indexOf("#") !== -1) {
				id = id.substring(0, id.indexOf("#"));
			}
			if (id.length > 0) {
				views.push({
					component : this,
					score : 1
				});
			}
		}
	},
	
	activate : function(state) {
		ui.setLayout("standard");
		com.add(signInHeader);
		com.add(dashboardInitFeature);
		com.add(createSpaceContent);
		com.trigger(this, "http://purl.org/role/ui/Resource#", "select");
	},
	
	getTitle : function() {
		return "New Space";
	}
	   	
}; });