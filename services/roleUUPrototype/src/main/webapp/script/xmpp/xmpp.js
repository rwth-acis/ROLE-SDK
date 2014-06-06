define([ "./strophe", "./strophejs-plugins/roster/strophe.roster", "./strophejs-plugins/pubsub/strophe.pubsub", 
         "./iwc", "./groupie", "domReady", "role/feature/duimanager" ], 
		function(Strophe, StropheRoster , StrophePubsub, iwc, Groupie, domReady, duiManager) {
	
	var xmpp = {
		connection: null,
		host: null,
		boshurl: null,
		mucroom: null,
		pubsubservice:  null,
		pubsubnode: null,
		iwcProxy:null,
		isConnected: false,
		userpubsubnode: null,
		jid: null,
		
		onIntent: function(intent){
//			console.log("duiduidui");
			return true;
		},

	//---------IWC CONNECT BGN -----------------------
		
		iwcconnect: function(){	
//				/*
				console.log("Initializing ROLE IWC Proxy");
//				/*
				this.iwcProxy = new iwc.Proxy();
				this.iwcProxy.onError = function(msg) {
					console.log("PROXY iwcProxy.onError() " + msg);
				};
				
				this.iwcProxy.setPubSubNode(this.pubsubservice, this.pubsubnode);
				this.iwcProxy.setXmppClient(this.connection);
				
//				var that = this;
//				this.iwcProxy.setPubSubNode(this.pubsubnode, "space-test");
				this.iwcProxy.connect(this.onIntent);
//				this.iwcProxy.registerDui(duiManager);
//				this.iwcProxy.createAndConfigurePubSubNodeForRIWC(this.pubsubservice,
//						this.pubsubnode,
//						 this.onIntent);
//						*/
			/*	
			this.iwcProxy = new duiXmpp();
			this.iwcProxy.onError = function(msg){};
			this.iwcProxy.setXmppClient(this.connection);
			this.iwcProxy.setPubSubNode(this.pubsubService, "space-test");
			this.iwcProxy.connect(this.onIntent, this.onIntent);
//				*/
				return true;
		 }

	//---------IWC CONNECT END -----------------------

	};
	
	
	
	domReady(function() {

		$(document).bind('space-xmpp-connect', function (ev, data) {
			
			console.log("Establishing XMPP connection to " + xmpp.boshurl);
			if (!xmpp.isConnected){
				if (xmpp.connection == null)
					xmpp.connection = new Strophe.Connection(xmpp.boshurl);
				Groupie.connection = xmpp.connection;
				
				xmpp.connection.connect(
					data.jid, data.password,
					function (status) {
						console.log("XMPP Strophe Connection Status: " + status);
						if (status === Strophe.Status.CONNECTED) {
							console.log("XMPP Strophe connected");
							$(document).trigger('chat-connected');
							$(document).trigger('space-xmpp-connected');
							$(document).trigger('user-xmpp-connected');
							xmpp.isConnected = true;
						} else if (status === Strophe.Status.DISCONNECTED) {
							console.log("XMPP Strophe disconnected");
							$(document).trigger('chat-disconnected');
							$(document).trigger('xmpp-disconnected');
							xmpp.isConnected == false;
						}
				});
			}
			else{
				$(document).trigger('chat-connected');
				$(document).trigger('space-xmpp-connected');
			}
		});
		/*
		$(document).bind('temp-xmpp-connect', function(ev, data){
			console.log("Establishing XMPP connection to " + xmpp.boshurl);
			if (xmpp.isConnected)
				return;
			else{
				xmpp.connection = new Strophe.Connection(xmpp.boshurl);
				xmpp.connection.connect(
						data.jid, data.password, 
						function(status){
							console.log("XMPP Strophe Connection Status: " + status);
							if (status === Strophe.Status.CONNECTED) {
								console.log("XMPP Strophe connected");
								$(document).trigger('temp-xmpp-connected');
								xmpp.isConnected = true;
							} else if (status === Strophe.Status.DISCONNECTED) {
								console.log("XMPP Strophe disconnected");
//								$(document).trigger('chat-disconnected');
								$(document).trigger('xmpp-disconnected');
								xmpp.isConnected == false;
							}
						});
			}
		});*/

		$(document).bind('space-xmpp-connected', function () {
			console.log("XMPP Connection up");
			var presence = $pres({"show":"online"});
			xmpp.connection.send(presence.tree());
			xmpp.iwcconnect();
		});

		$(document).bind('xmpp-disconnected', function () {
			console.log("XMPP Connection down");
			alert("you are disconnected from the xmpp server. Widget communiction is down.");
		});

	});
	
	return xmpp;
	
});
