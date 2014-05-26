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
			$(document.body).toggleClass("is-power-member", space.isMemberAllowedToAddTools() || space.isOwner());
			$(document.body).toggleClass("is-not-power-member", !space.isMemberAllowedToAddTools() && ! space.isOwner());
			$(document.body).toggleClass("is-owner", space.isOwner());
			$(document.body).toggleClass("is-not-owner", !space.isOwner());
		});
		com.on("http://purl.org/role/ui/Space#", "update", function() {
			if (space._context === null) {
				return;
			}
			var owners = openapp.resource.context(space._context).sub(
					openapp.ns.openapp + "owner").list();
			var members = openapp.resource.context(space._context).sub(
					openapp.ns.foaf + "member").list();
			$(container).find("#memberEntries").html("");
			for ( var i = 0; i < members.length; i++) {
				var properties = openapp.resource.context(members[i])
						.properties();
				members[i].properties = properties;
				members[i].data = space._context.data;
				var memberSA = properties['http://www.w3.org/2002/07/owl#sameAs'];
				if (typeof space._context.data[memberSA] !== "undefined"
					&& typeof space._context.data[memberSA]['http://purl.org/dc/terms/title'] !== "undefined") {
					//Lets check if owner
					var isOwnerCls = "";
					for (var j = 0;j < owners.length; j++) {
						var ownerProperties = openapp.resource.context(owners[j]).properties();
						var ownerSA = ownerProperties['http://www.w3.org/2002/07/owl#sameAs'];
						if (memberSA === ownerSA) {
							isOwnerCls = "isOwner";
							break;
						}
					}

					$(memberTemplate({
						uri: properties['http://www.w3.org/2002/07/owl#sameAs'],
						id: this._jidToId(space._context.data[properties['http://www.w3.org/2002/07/owl#sameAs']]['http://xmlns.com/foaf/0.1/jabberID'][0].value),
						isOwner: isOwnerCls,
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