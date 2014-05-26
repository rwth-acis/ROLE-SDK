define([ "com", "jquery", "../model/user", "xmpp/xmpp", "config" ], function(com, $, user, xmpp, config) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#" ],
	
	load : function() {
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {

			var spaceProperties = openapp.resource.context(
					space._context).properties();
			var userProperties = openapp.resource.context(
					user._context).properties();
			
			// extract connection information from user properties
			var userjid = userProperties["http://xmlns.com/foaf/0.1/jabberID"].substring("xmpp:".length);
			var userpass = userProperties[openapp.ns.role + "externalPassword"];
			var usert = userjid.split("@");
			var userid = usert[0];
			var host = usert[1];
			
			var boshurl;
			if (config.usewebsocket && WebSocket) {
				boshurl = "ws://" + host;
				if (config.xmppwsport != 80) {
					boshurl += ':' + config.xmppwsport;
				}
				if (config.xmppwspath !== "") {
					boshurl += "/" + config.xmppwspath;
				}
			} else {
				boshurl = "http://" + host;
				if (config.xmppboshport != 80) {
					boshurl += ':' + config.xmppboshport;
				}
				if (config.xmppboshpath !== "") {
					boshurl += "/" + config.xmppboshpath;
				}
			}
			// extract connection information from space properties
			var mucroom = spaceProperties["http://xmlns.com/foaf/0.1/jabberID"]
			.substring("xmpp:".length);
			
			// TODO: since configuration done by ROLE realtime service is not creating usable pubsub nodes, prefix the nodes to be created by client side IWC lib 
			var pubsubnode = "spacey-" + mucroom.split("@")[0];
			var pubsubservice = config.xmpppubsubservice + "." + host;
			
			xmpp.host = host;
			xmpp.boshurl = boshurl;
			xmpp.mucroom = mucroom;
			xmpp.pubsubservice = pubsubservice;
			xmpp.pubsubnode = pubsubnode;
			
			if (typeof console !== "undefined") {
				console.log("XMPP User JID: " + userjid);
				console.log("XMPP User Password: " + userpass);
				console.log("XMPP Host: " + xmpp.host);
				console.log("XMPP Connection Manager URL: " + xmpp.boshurl);
				console.log("XMPP MUC Room: " + xmpp.mucroom);
				console.log("XMPP Pubsub Service: " + xmpp.pubsubservice);
				console.log("XMPP Pubsub Node: " + xmpp.pubsubnode);
			}
			
			if(typeof xmpp.host === 'undefined' || xmpp.host.length == 0){
				if (typeof console !== "undefined") {
					console.log("Connection to XMPP server disabled. Enable with specifying parameters xmpp.host, xmpp.port");
				}
			} else {
				if (space.isMember()) {
					$(document).trigger('xmpp-connect', {
						jid : userjid,
						password : userpass
					});
				}
			}
			
			window.onbeforeunload = function() {
				if (typeof xmpp.connection !== "undefined" &&
						xmpp.connection !== null) {
					xmpp.connection.disconnect();
				}
			};			
			
		});
	}
	
}; 

});
