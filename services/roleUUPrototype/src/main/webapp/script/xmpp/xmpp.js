define([ "./strophe", "./strophejs-plugins/roster/strophe.roster", "./strophejs-plugins/pubsub/strophe.pubsub", "./iwc", "./groupie", "domReady", "config" ], function(Strophe, StropheRoster , StrophePubsub, iwc, Groupie, domReady, config) {
	
	var xmpp = {
		connection: null,
		host: null,
		boshurl: null,
		mucroom: null,
		pubsubservice:  null,
		pubsubnode: null,
		iwcProxy:null,
		
		onIntent: function(intent){
			return true;
		},

	//---------IWC CONNECT BGN -----------------------
		
		iwcconnect: function(){	
				
				console.log("Initializing ROLE IWC Proxy");
				
				this.iwcProxy = new iwc.Proxy();
				this.iwcProxy.onError = function(msg) {
					console.log("PROXY iwcProxy.onError() " + msg);
				}
				
				this.iwcProxy.setXmppClient(this.connection);
				
				var that = this;
				this.iwcProxy.createAndConfigurePubSubNodeForRIWC(this.pubsubservice,this.pubsubnode,this.onIntent);
				return true;
		 }

	//---------IWC CONNECT END -----------------------

	};
	
	
	
	domReady(function() {

		$(document).bind('xmpp-connect', function (ev, data) {
			
			console.log("Establishing XMPP connection to " + xmpp.boshurl);
			xmpp.connection = new Strophe.Connection(xmpp.boshurl);
			Groupie.connection = xmpp.connection;
			
			/*
			  xmpp.connection.rawInput = function(log) {
				console.log("RCVD: " + (new Date()).toGMTString());
				console.log(log);
			  };
			  
			  xmpp.connection.rawOutput = function(log) {
				console.log("SENT:" + log);
			  };
			*/
			
			xmpp.connection.connect(
				Strophe.getBareJidFromJid(data.jid) + "/" + xmpp.pubsubnode,
				data.password,
				function (status) {
					console.log("XMPP Strophe Connection Status: " + status);
					if (status === Strophe.Status.CONNECTED) {
						//console.log("XMPP Strophe connected");
						$(document).trigger('chat-connected');
						$(document).trigger('xmpp-connected');
						
					} else if (status === Strophe.Status.DISCONNECTED) {
						//console.log("XMPP Strophe disconnected");
						$(document).trigger('chat-disconnected');
						$(document).trigger('xmpp-disconnected');
					}
				});
		});

		$(document).bind('xmpp-connected', function () {
			console.log("XMPP Connection up");
			var presence = $pres({"show":"online"});
			xmpp.connection.send(presence.tree());
			xmpp.iwcconnect();
		});

		$(document).bind('xmpp-disconnected', function () {
			console.log("XMPP Connection down");
		});
	
	});
	
	return xmpp;
	
});
