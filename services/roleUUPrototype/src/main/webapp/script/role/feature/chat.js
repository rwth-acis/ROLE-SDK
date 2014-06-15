define([ "com", "jquery", "../model/user", "xmpp/groupie" ], function(com, $, user, Groupie) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#" ],
	
	_isConnected : false, 
	
	load : function() {
		com.on("http://purl.org/role/ui/Space#", "update", function(space) {
			if (typeof space._context === "undefined") {
				return;
			}
			if (space.isMember() && !this._isConnected) {
				this._isConnected = true;
				
				var spaceProperties = openapp.resource.context(
						space._context).properties();
				var userProperties = openapp.resource.context(
						user._context).properties();

				Groupie.room = spaceProperties["http://xmlns.com/foaf/0.1/jabberID"]
						.substring("xmpp:".length);
				Groupie.nickname = userProperties[openapp.ns.dcterms
						+ "title"].split(" ")[0]
				+ Math.floor(1000 + Math.random() * 8999.99);
				
				//Commented out this trigger, since XMPP feature already triggers connection setup.
				//Furthermore, chat and xmpp should share the same connection instead of setting up one for each.
				/*
				$(document).trigger('connect', {
					jid : userProperties["http://xmlns.com/foaf/0.1/jabberID"]
							.substring("xmpp:".length),
					password : userProperties[openapp.ns.role
							+ "externalPassword"]
				});
				*/
				
				$("#chatHeader").show();
				$("#chatEntries").show();
			} else if (this._isConnected) {
				this._isConnected = false;
				
				if (typeof Groupie.connection !== "undefined") {
					Groupie.connection.send($pres({
						to : Groupie.room + "/" + Groupie.nickname,
						type : "unavailable"
					}));
//					Groupie.connection.disconnect();
				}
				$("#chatHeader").hide();
				$("#chatEntries").hide();
			}
			window.onbeforeunload = function() {
				if (typeof Groupie.connection !== "undefined" &&
						Groupie.connection !== null) {
					Groupie.connection.send($pres({
						to : Groupie.room + "/" + Groupie.nickname,
						type : "unavailable"
					}));
					//Groupie.connection.disconnect();
				}
			};			
			
		});
	}
	
}; });