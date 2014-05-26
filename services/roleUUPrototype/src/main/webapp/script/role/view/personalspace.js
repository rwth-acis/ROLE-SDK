define([ "com", "./space", "role/header/logo", "role/header/widgets", "role/header/bookmarklet",
         "role/header/expand", "../model/space", "../feature/dashboard" ], function(
		com, spaceView, logoHeader, widgetsHeader, bookmarkletHeader, expandHeader, space,
		dashboardFeature) { return {

	interfaces : [ "http://purl.org/role/ui/View#" ],
	
	_title : null,
	
	query : function(state, views) {
		if (state.predicate === openapp.ns.foaf + "member"
				&& state.params.hasOwnProperty("mode")
				&& state.params.mode === "dashboard") {
			views.push({
				component : this,
				score : 1
			});
		}
	},
	
	activate : function(state) {
		spaceView.activate(state);
		dashboardFeature.init();
		$(".header-title").hide();
		$(document.body).addClass("user-profile");
		com.add(logoHeader);
		com.add(widgetsHeader);
		com.add(bookmarkletHeader);
		com.add(expandHeader);
		com.on("http://purl.org/role/ui/Space#", "update", function(space) {
			this._title = space.getTitle();
			com.trigger(this, "http://purl.org/role/ui/View#", "update");
		}.bind(this));
	},
	
	getTitle : function() {
		return this._title;
	}
	
}; });