define([ "com", "jquery", "../ui/ui", "../content/createactivity" ], function(
		com, $, ui, createActivityContent) { return {

	interfaces : [ "http://purl.org/role/ui/View#" ],
	
	query : function(state, views) {
		if (state.predicate === openapp.ns.role + "activityService") {
			views.push({
				component : this,
				score : 1
			});
		}
	},
	
	activate : function(state) {
		if (state.params.hasOwnProperty("mode") &&
				state.params.mode === "embedded") {
			ui.setLayout("empty");
		} else {
			ui.setLayout("standard");
		}
		com.add(createActivityContent);
	},
	
	getTitle : function() {
		return "Activities";
	}
	   	
}; });