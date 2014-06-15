define([ "com", "jquery", "../model/space", "../ui/ui", "handlebars!./members", "handlebars!./member" ], function(com, $, space, ui, template, memberTemplate) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#" ],
	
	getTitle : function() {
		return "Members";
	},

	createUI : function(container) {
		$(template()).appendTo(container);
		com.on("http://purl.org/role/ui/Space#", "update", function() {
			//$(container).find("#sideEntry-join").css("display", space.isMember() ? "none" : "block");
			//$(container).find("#sideEntry-leave").css("display", space.isMember()  ? "block" : "none");
			$(document.body).toggleClass("is-member", space.isMember());
			$(document.body).toggleClass("is-not-member", !space.isMember());
			$(document.body).toggleClass("is-owner", space.isOwner());
			$(document.body).toggleClass("is-not-owner", !space.isOwner());
		});
		com.on("http://purl.org/role/ui/Space#", "update", function() {
			if (space._context === null) {
				return;
			}
			var members = openapp.resource.context(space._context).sub(
					openapp.ns.foaf + "member").list();
			$(container).find("#memberEntries").html("");
			for ( var i = 0; i < members.length; i++) {
				var properties = openapp.resource.context(members[i])
						.properties();
				members[i].properties = properties;
				members[i].data = space._context.data;
				if (typeof space._context.data[properties['http://www.w3.org/2002/07/owl#sameAs']] !== "undefined"
					&& typeof space._context.data[properties['http://www.w3.org/2002/07/owl#sameAs']]['http://purl.org/dc/terms/title'] !== "undefined") {
					$(memberTemplate({
						uri: properties['http://www.w3.org/2002/07/owl#sameAs'],
						id: this._jidToId(space._context.data[properties['http://www.w3.org/2002/07/owl#sameAs']]['http://xmlns.com/foaf/0.1/jabberID'][0].value),
						title: space._context.data[properties['http://www.w3.org/2002/07/owl#sameAs']]['http://purl.org/dc/terms/title'][0].value
					})).appendTo($(container).find("#memberEntries"));
				}
			}
		}.bind(this));
		$(container).find("#sideEntry-join").on("click", function() {
			space.join();
		});
		$(container).find("#sideEntry-leave").on("click", function() {
			space.leave();
		});
		$(container).on("click", ".sideEntry-member", function(event) {
			event.preventDefault();
			//ui.browse($(this).attr("href") + '/:app?mode=member', $(this).data("id"));
		});
	},
	
	_jidToId : function(jid) {
		return jid.substring(5).split('@')[0].replace('-', '_');
	}
	
}; });