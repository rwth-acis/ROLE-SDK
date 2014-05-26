define([ "com", "jquery", "handlebars!./loginwarning"], function(com, $, template) { return {
	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		com.one("http://purl.org/role/ui/User#", "load", function(user) {
			//If not guest account
			var isAuthenticated = user._context.uri.indexOf("cTrvjuLrGC") === -1;
			com.one("http://purl.org/role/ui/Space#", "load", function(space) {
				var isMember = space.isMember();
				var closed = false;
				var node = $(template({})).appendTo(container);
				node.find(".join_space").click(function() {space.join();container.hide();});
				node.find(".role_content_close").click(function() {closed = true; container.hide();});
				node.find(".role_sign_in").click(function() {				
					var returnurl = encodeURIComponent(window.location.pathname+window.location.search);
					window.location = "/:authentication?return="+returnurl+"&action=signin";
				});
				if (isAuthenticated && isMember) {
					container.hide();
					return;
				} else if (isAuthenticated && !isMember) { 
					node.find(".role_warning_3").html('<span class="important">Warning, you are not a member of this space.</span> <br/>'+
													  'Widgets in this space may not work as expected, '+
			                                        'because they are not authorized to perform modifying operations.');
					node.find(".role_sign_in").hide();
					node.find(".join_space").show();
				}
			});
		});

	}
}; });