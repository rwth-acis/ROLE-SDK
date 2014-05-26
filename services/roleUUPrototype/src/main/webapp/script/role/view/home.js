define([ "com", "jquery", "../ui/ui", "../header/signin", "../feature/dashboardinit",
         "../content/openspace" ], function(
		com, $, ui, signInHeader, dashboardInitFeature, openSpaceContent) { return {

	interfaces : [ "http://purl.org/role/ui/View#" ],
	
	query : function(state, views) {
		if (state.predicate === openapp.ns.openapp + "domain") {
			views.push({
				component : this,
				score : 1
			});
		} else if (state.predicate === openapp.ns.role + "spaceService") {
			views.push({
				component : this,
				score : 0.5
			});
		}
	},
	
	activate : function(state) {
		ui.setLayout("standard");
		com.add(signInHeader);
		com.add(dashboardInitFeature);
		com.add(openSpaceContent);
		com.trigger(this, "http://purl.org/role/ui/Resource#", "select");
	},
	
	getTitle : function() {
		return "Learning Spaces";
	}
	   	
}; });