/**
 * rewrite the iwc.Proxy for the dui manager here
 */
define(["jquery", "xmpp/iwc"], function($, iwc){

/**
 * @see iwc
 * @author Christian Hocken (hocken@dbis.rwth-aachen.de)
 * @author Ke Li
 * @requires openapp.js
 */
duiXmppPortal = function() {
	this._componentName = "duimanager";
	//var that = this;
	
	//the resource part is to distinguish devices of the user
	//this._resourcePart = null;
	
	//switch to enable/disable interwidget communication
	this._connected = false;
	
	//a connected object of type XmppClient that is used for XMPP communication
	this._xmppClient = null;
	
	
	//the PubSub service that holds the pubsub node
	this._pubSubEntity = null;
	//the PubSub node intents are sent to / received from
	this._pubSubNode = null;
	
	//callbacks
	
	//onError is called when an error occurs. The passed object is a String
	this.onError = function(error){};
	
	//onIntent is called when an intent is received via web messaging. A JSON intent object is passed to the function
	//Override this method
	this.onIntent = function(intent){};
	
	//onXmppIntent is called when an intent is received via the xmpp
	//Override this method
	this.onXmppIntent = function(intent){};
	
	//onDroppedIntent is called when an intent is received but the trust threshold prohibits the delivery. A JSON intent object is passed to the function
	this.onDroppedIntent = function(){};
	
	//the callback that has primarily been registered in the XmppClient object.
	//Since only items with namespace 'http://dbis.rwth-aachen.de/~hocken/da/xsd/Intent'
	//shall be handled by the IWC framework we need to forward items of other types
	//this._primaryRegisteredOnEventCB = null;
	
	//variables used in the trust extension:
	
	//private functions:
	
	/**
	 * utility function that transforms a JSON intent object into a XML element
	 * @param intent (Object) a valid JSON intent object
	 * @returns (Object) XML element representing the passed JSON intent object
	 */
	this._jsonIntentToXmlIntent = function(intent) {
		
		if (intent.sender == null)
			intent.sender = this._componentName;
		
		var sender = this._xmppClient.jid + "?sender=" + intent.sender;
		
		var item = Strophe.xmlElement("intent",{"xmlns":iwc.util.NS.INTENT});
		
		var compe = Strophe.xmlElement("component",{},intent.component);
		item.appendChild(compe);
		
		var sende = Strophe.xmlElement("sender",{},sender);
		item.appendChild(sende);
		
		if (typeof intent.action == "string") {
			var acte = Strophe.xmlElement("action",{},intent.action);
			item.appendChild(acte);
		}
		
		var datae = Strophe.xmlElement("data",{"mime":intent.dataType},intent.data);
		item.appendChild(datae);
		
		if (typeof intent.categories != "undefined" && intent.categories.length > 0) {
			var nCategories = Strophe.xmlElement("categories");
			
			for (var i=0; i < intent.categories.length; i++) {
				var category = intent.categories[i];
				var catege = Strophe.xmlElement("category",{},category);
				nCategories.appendChild(catege);
			}
			item.appendChild(nCategories);
		}
		
		if (typeof intent.flags != "undefined" && intent.flags.length > 0) {
			var nFlags = Strophe.xmlElement("flags");
			for (var i=0; i < intent.flags.length; i++) {
				var flag = intent.flags[i];
				var flage = Strophe.xmlElement("flag",{},flag);
				nFlags.appendChild(flage);
			}
			item.appendChild(nFlags);
		}
		if (typeof intent.extras != "undefined") {
			var extrase = Strophe.xmlElement("extras",{},JSON.stringify(intent.extras));
			item.appendChild(extrase);
		}
		
		return(item);
	};
	
	/**
	 * sets the PubSub node and service that shall handle remote interwidget communication
	 * @param entity (String) The PubSub service
	 * @param node (String) The name of the node that shall handle remote interwidget communication
	 */
	this.setPubSubNode = function(entity, node) {
		this._pubSubEntity = entity;
		this._pubSubNode = node;
	};
	
	/**
	 * returns the PubSub node that handles remote interwidget communication
	 * @returns (String) The name of the node that handles remote interwidget communication
	 */
	this.getPubSubNode = function() {
		return this._pubSubNode;
	};

	/**
	 * returns the PubSub service
	 * @returns (String) the PubSub service
	 */
	this.getPubSubEntity = function() {
		return this._pubSubEntity;
	};
	
		
	/**
	 * connects the proxy to local interwidget communication
	 * @param liwcCallback (Function(intent)) is called when a JSON intent object is received via web messaging.
	 * @param riwcCallback (Function(intent)) is called when a JSON intent object is received via xmpp
	 */
	this.connect = function(liwcCallback, riwcCallback, callback){
		//console.log("Entering connect with protocol: "+this._xmppClient.service);
		//workaround for broken ECMAScript
		var self = this;
		this.onIntent = liwcCallback;
		this.onXmppIntent = riwcCallback;
		//register callback with 'this' as scope
		
		// The container will forward OpenApp events as custom jQuery events.
		// As gadgets.openapp.connect supports only one event handler, it is necessary
		// that the container performs only one gadgets.openapp.connect, and that
		// all others listen to the jQuery events. Otherwise, only the last caller of
		// gadgets.openapp.connect will be able to receive event messages.
		////gadgets.openapp.connect(function(envelope, message){});
		$(document).bind("openapp", function(event) {
			self.liwcEventHandler(
					typeof event.envelope !== "undefined" ? event.envelope : {},
					typeof event.message !== "undefined" ? event.message : {});
		});
		
		this._connected = true;
		
		//console.log("Connected");
		//check subscription
		//console.log("Pubsub " + this._pubSubEntity + "/" + this._pubSubNode);
		
		if (this._xmppClient != null &&
				this._pubSubEntity != null &&
				this._pubSubEntity != "" &&
				this._pubSubNode != null &&
				this._pubSubNode != "") {
			
			//console.log("Retrieving subscriptions");
			this._xmppClient.pubsub.retrieveSubscriptions(this._pubSubEntity, this._pubSubNode, function(result) {
				//console.log("Result: %o",result);
				if (result.type == "result") {
					var subscribedToNode = false;
					for (var i=0; i < result.subscriptions.length; i++) {
						var sub = result.subscriptions[i];
						if (sub.node == self._pubSubNode)
							subscribedToNode = true;
					}
					if (!subscribedToNode) {
						
						//console.log("Self JID: " + Strophe.getBareJidFromJid(self._xmppClient.jid));
						
		//console.log("Pubsub " + self._pubSubEntity + "/" + self._pubSubNode);
						self._xmppClient.pubsub.subscribe(
							Strophe.getBareJidFromJid(self._xmppClient.jid), 
							self._pubSubEntity, 
							self._pubSubNode,
							function(result){
								//console.log("Subscruption result: ");
								//console.log(result);
								return true;
							},
							function(result) {
								//console.log("At least there is a result---");
								//console.log(result);
								if (result.type == "result"){
									//console.log("Subscribed to " + self._pubSubNode + " at " + self._pubSubEntity);
									self.setPubSubNode(self._pubSubEntity,self._pubSubNode);
									var fields = [];
									fields[0] = {"var":"pubsub#deliver" , "val":["1"]};
									fields[1] = {"var":"pubsub#digest" , "val":["0"]};
									fields[2] = {"var":"pubsub#include_body" , "val":["true"]};
									fields[3] = {"var":"pubsub#show_values" , "val":["chat","online","away"]};
									
									self._xmppClient.pubsub.configureSubscription(self._pubSubEntity,self._pubSubNode,fields,function(result){
										//console.log("Configuration complete?");
										//console.log(result);
										//console.log("ROLE IWC Proxy initialized (new subscription)");
									});
									callback();
								}	
								else if(result.type == "error"){
									//console.log("############## Error");
									//console.log(result)
								}
							}
						);
					}
					else {
						//console.log("Already subscribed to " + self._pubSubNode + " at " + self._pubSubEntity);
						self.setPubSubNode(self._pubSubEntity,self._pubSubNode);
						callback();
						//console.log("ROLE IWC Proxy initialized (existing subscription)");
					}
				}
				
			});
			
		}
		else {
			throw new Error ("Framework is not properly initialized");
		}
	};

	/**
	 * disconnects the proxy from local interwidget communication
	 */
	
//	/*
	 // no need to disconnect from the liwc and no need to disconnect from the xmpp here either, the normal iwc.Proxy will manage the connection
	this.disconnect = function() {
		gadgets.openapp.disconnect();
		this._connected = false;
	};
//*/


	/**
	 * sets the XMPP client used for remote interwidget communication
	 * @param xmppClient (Object) a connected object of type XmppClient
	 */
	this.setXmppClient = function(xmppClient) {

		this._xmppClient = xmppClient;
		
		this._xmppClient.addHandler(this._messageHandler.bind(this), null, "message", null, null, this._pubSubEntity, null);

	};
	
	/**
	 * the handler function that handles events from xmpp wrapping the intents for DUI management.
	 * If you do not have good reason, do not overwrite it
	 * @param event (Object) The JSON intent object received via the Publish/Subscribe node
	 */
	 
	this.riwcEventHandler = function(event) {
		
		var that = this;
		for (var i = 0; i< event.items.length; i++) {
			var item = event.items[i];
			if (typeof item.payload != "undefined" && item.payload != null) {
				var nIntent = item.payload;
				
				if (typeof nIntent.getAttribute == "function" && nIntent.getAttribute("xmlns") == iwc.util.NS.INTENT) {
					//we have an intent element
					
//					//console.log("We have found an intent...");
					var intent = that._xmlIntentToJsonIntent(nIntent);
//					//console.log(intent);
					if (iwc.util.validateIntent(intent)) {
						var publisherJid = intent.sender;
						if (publisherJid.indexOf("?") > -1)
							publisherJid = publisherJid.split("?")[0];
						//check if this instance is the sender of the intent.
						//if so, do not process it
						if (this._xmppClient.jid !== publisherJid 
							//the target component must be defined
							&& typeof intent.component != "undefined"
							//the target component of the intent must be this or a broadcast 
							&& (intent.component == this._componentName || intent.component == "")
							//and this duixmppportal only deals with DUI related intents
							&& typeof intent.categories != "undefined" && intent.categories.indexOf("DUI") != -1)
								this.onXmppIntent(intent);
					}
				}
			}
		}
	};

	/**
	 * the handler function that handles DUI intents via web messaging.
	 * If you do not have good reason, do not overwrite it.
	 *
	 * @param event the JSON intent object sent from a local web messaging source
	 */
	this.liwcEventHandler = function(envelope, intent) {
		//'component' and 'sender' properties must always be available in 'intent' objects
		if (typeof intent.component != "undefined" && typeof intent.sender != "undefined" && intent.sender != "duimanager"
			//the target component of the intent must be this or a broadcast
			&& (intent.component == this._componentName || intent.component == "")
			//only accept local intents
			&& (typeof intent.flags == "undefined" || intent.flags.indexOf("PUBLISH_GLOBAL") == -1)
			//only accept DUI related intents
			&& typeof intent.categories != "undefined" && intent.categories.indexOf("DUI") != -1) {
				this.onIntent(intent);
			}
	};
		
	this._messageHandler = function(stanza){
		
		////console.log("Received Message: ");
		////console.log(stanza);
		var fchild=stanza.childNodes[0];
		
		if(fchild.nodeName === "event" && fchild.getAttribute("xmlns") == "http://jabber.org/protocol/pubsub#event"){
			
			////console.log("Pubsub event detected... ");
			var eventparams = {};
			eventparams["from"] = stanza.getAttribute("from");
			eventparams["to"] = stanza.getAttribute("to");
			nItems = fchild.getElementsByTagName("items");
			if (nItems.length == 0) {
				//auxiliary information. E.g. node has been deleted
				//TODO we should react on this
			}
			else {
				//items published
				if (nItems[0].getAttribute("node") != null) {
					eventparams["node"] = nItems[0].getAttribute("node");
				}
				eventparams["node"] = "";
				var nItemList = nItems[0].getElementsByTagName("item");
				var items = [];
				for (var i=0; i< nItemList.length; i++) {
					var nItem = nItemList[i];
					var item = {};
					item["id"] = nItem.getAttribute("id");
					if (nItem.childNodes.length > 0) { //if item has payload
						item["payload"] = nItem.childNodes[0];
					}
					items.push(item);
				}
				eventparams["items"] = items;
				////console.log("Event Parsed...");
				////console.log(eventparams);
				var fromNode = fchild.firstChild.attributes[0].nodeValue;
				if (fromNode == this._spacePubSubNode || fromNode == this._pubSubNode)
					this.riwcEventHandler(eventparams);
			}
		}
		
		return true;
	};



	/**
	 * publishes an intent on the Publish/Subscribe node
	 * @param intent (Object) The intent to be published
	 * @param callback (Function(result)) the response of {@link XmppClient#publishItem}
	 */
	this.publishIntent = function(intent, callback) {
	console.log("this is xmppportal %o",message);
		//accessing pseudo private variables is ugly - we need getters
		var self = this;
		if (this._xmppClient != null &&
				this._pubSubNode != null &&
				this._pubSubNode != "") {
			if (iwc.util.validateIntent(intent)) {
				var item = this._jsonIntentToXmlIntent(intent);
				
				/*
				var fields = [];
				fields[0] = {"var":"pubsub#deliver" , "val":["1"]};
				fields[1] = {"var":"pubsub#digest" , "val":["0"]};
				fields[2] = {"var":"pubsub#include_body" , "val":["true"]};
				fields[3] = {"var":"pubsub#show_values" , "val":["chat","online","away"]};
			
				this._xmppClient.pubsub.configureSubscription(this._pubSubEntity,this._pubSubNode,fields,function(result){
					//console.log("Configuration before publish complete?");
					//console.log(result);
				*/
					//console.log("Publishing intent... ");
					self._xmppClient.pubsub.publishItem(self._pubSubEntity, self._pubSubNode, item, callback);
				/* 
				});
				*/
			}
		}
		else {
			throw new Error ("Framework is not properly initialized");
		}
	};
	
	this._spacePubSubNode = null;
	this.setSpacePubSubNode = function(n){
		this._spacePubSubNode = n;
	};
	
	
	this.publishSpaceIntent = function(intent, callback) {
		//accessing pseudo private variables is ugly - we need getters
		var self = this;
		if (this._xmppClient != null &&
				this._pubSubNode != null &&
				this._pubSubNode != "") {
			if (iwc.util.validateIntent(intent)) {
				var item = this._jsonIntentToXmlIntent(intent);
				//console.log("Publishing intent... ");
				self._xmppClient.pubsub.publishItem(self._pubSubEntity, self._spacePubSubNode, item, callback);
			}
		}
		else {
			throw new Error ("Framework is not properly initialized");
		}
	};

	/**
	 * publishes an intent locally. If the flag {@link iwc.util.FLAGS}.PUBLISH_GLOBAL is set
	 * the intent will also be sent to the Publish/Subscribe node
	 * @param intent (Object) JSON intent object
	 */
	this.publish = function(intent) {
		if (intent.sender == null)
			intent.sender = this._componentName;
		if (iwc.util.validateIntent(intent)) {
			var envelope = {"type":"JSON", "event":"publish", "message":intent};
			gadgets.openapp.publish(envelope);
		}
	};

	/**
	 * utility function that transforms a XML element into a JSON intent object
	 * @param item (Object) XML element representing a JSON intent object
	 * @returns (Object) JSON intent object
	 */
	this._xmlIntentToJsonIntent = function(item) {
		intent = {};
		for (var i = 0; i < item.childNodes.length; i++) {
			var node = item.childNodes[i];
			
			if (node.nodeName == "component") {
				intent["component"] = "";
				if (node.childNodes.length > 0) {
					intent["component"] = node.childNodes[0].nodeValue;
				}
			}
			else if (node.nodeName == "sender") {
				intent["sender"] = "";
				if (node.childNodes.length > 0) {
					intent["sender"] = node.childNodes[0].nodeValue;
				}
			}
			else if (node.nodeName == "action") {
				intent["action"] = "";
				if (node.childNodes.length > 0) {
					intent["action"] = node.childNodes[0].nodeValue;
				}
			}
			else if (node.nodeName == "data") {
				intent["data"] = "";
				if (node.childNodes.length > 0) {
					intent["data"] = node.childNodes[0].nodeValue;
				}
				intent["dataType"] = node.getAttribute("mime");
			}
			else if (node.nodeName == "categories") {
				intent["categories"] = [];
				for (var j = 0; j < node.childNodes.length; j++) {
					var nCategory = node.childNodes[j];
					if (nCategory.childNodes.length > 0) {
						intent["categories"].push(nCategory.childNodes[0].nodeValue);
					}
				}
			}
			else if (node.nodeName == "flags") {
				intent["flags"] = [];
				for (var j = 0; j < node.childNodes.length; j++) {
					var nFlag = node.childNodes[j];
					if (nFlag.childNodes.length > 0) {
						intent["flags"].push(nFlag.childNodes[0].nodeValue);
					}
				}
			}
			else if (node.nodeName == "extras") {
				intent["extras"] = "";
				
				if (node.childNodes.length > 0) {
					var valu = node.childNodes[0].nodeValue;
					// somehow a dirty hack... what if other escaped chars appear? How do I unescape again?
					var dval = valu.replace(/&quot;/g,"\"");
					
					try{
					  intent["extras"] = JSON.parse(dval);
				 } catch(e){
				  console.log(e);
				  } 
				}
			}
		}
		
		return intent;
	};
};


return duiXmppPortal;

});
