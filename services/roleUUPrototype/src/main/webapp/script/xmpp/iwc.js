define(["jquery"], function($){

/**
 * @class
 * 
 * iwc is a framework for local and remote interwidget communication that follows
 * the Publish/Subscribe approach. The sending widget publishs a message which is
 * then received at every other widget in the widget container. If remote interwidget
 * communication is configured the message is sent to a XMPP Publish/Subscribe node
 * that forwards it to remote widget containers that are subscribed to the particular node.
 * <p/>
 * Messages exchanged between widgets follow a definitive structure called an <b>Intent</b>.
 * Read about Intents in the <a href="http://developer.android.com/guide/topics/intents/intents-filters.html">Google Dev Guide</a>
 * <p/>
 * Intent messages (in the following called JSON intent objects) are of the following form:
 * <br/>
 * <b>{</b> <br/>
 * 	<b>component</b>	:	(String) &lt; the component name of the recipient (e.g. http://dbis.rwth-aachen.de/~hocken/da/listener.xml)
 * 							or the empty string to indicate broadcasting &gt;, <br/>
 * 	<b>sender</b>		:	(String) &lt; the component name of the sender (e.g. http://dbis.rwth-aachen.de/~hocken/da/writer.xml).
 * 							A value of the form node@domain.tld/resource?sender=&lt;component name&gt; indicates that the intent
 * 							has been received from a remote environment &gt;, <br/>
 * 	<b>action</b>		:	(String) &lt; the action to be performed by receivers (e.g. "ACTION_INVOKE_SERVICE") &gt;, 
 * 	<b>data</b> 		 	(String) &lt; data in form of an URI (e.g. http://example.org) &gt;, <br/>
 * 	<b>dataType</b>	:		(String) &lt; the data type in MIME notation (e.g. text/html) &gt;, <br/>
 * 	<b>categories</b>	:	(Array) &lt; categories of widgets that shall process the intent (e.g. ["editor","proxy" ]) &gt;, <br/>
 * 	<b>flags</b>		:	(Array) &lt; flags that control how the intent is processed (e.g. ["PUBLISH_GLOBAL"]) &gt;, <br/>
 * 	<b>extras</b>		:	(Object) &lt; auxiliary data that need not to be specified. (e.g. key value pairs: {"examplekey":"examplevalue"}) &gt; <br/>
 * <b>}</b> <br/>
 * <p/>
 * The framework is divided into three classes. 
 * <ol>
 * 	<li><b>{@link iwc.Client}</b> is responsible for local interwidget communication</li>
 * 	<li><b>{@link iwc.Proxy}</b> is responsible for remote interwidget communication</li>
 * 	<li><b>{@link iwc.util}</b> provides utility functions used by {@link iwc.Client} and {@link iwc.Proxy}</li>
 * </ol>
 * <p/>
 * Trust extension:
 * <br/>
 * The iwc framework comes with a trust extension that will be described in more detail in my thesis
 * //TODO
 * 
 * @author Christian Hocken (hocken@dbis.rwth-aachen.de)
 */
iwc = function(){
};

//================================================== definition of iwc.Proxy ==================================================//

/**
 * @class
 * 
 * An object of type {@link iwc.Proxy} handles remote interwidget communication.
 * <p/>
 * <ol>
 * 	<li>
 * 		It routes intents that have been published locally and that have the flag {@link iwc.util.FLAGS}.PUBLISH_GLOBAL set
 * 		to a XMPP Publish/Subscribe node.
 * 	</li>
 * 	<li>
 * 		It receives intents that remote widgets containers have published at the particular node and publishs them in the local widget container
 * 	</li>
 * </ol>
 * 
 * A proxy object is instantiated as follows:
 * <ol>
 * 	<li>
 * 		create an object of type {@link iwc.Proxy}. E.g var proxy = new iwc.Proxy();
 * 	</li>
 * 	<li>
 * 		use {@link iwc.Proxy#setXmppClient} to pass a connected object of type XmppClient to the iwc.Proxy object. E.g. proxy.setXmppClient(&lt; yourXmppClientObject &gt;)
 * 	</li>
 * 	<li>
 * 		use {@link iwc.Proxy#setPubSubNode} to specifiy the Publish/Subscribe service and node that should be used for publishing and receiving intents.
 * 		E.g. proxy.setPubSubEntity("pubsub.role.dbis.rwth-aachen.de", "riwc");
 * 		{@link iwc.Proxy#createAndConfigurePubSubNodeForRIWC} can be used to create and configure a working PubSub node.
 * 	</li>
 * 	<li>
 * 		connect the proxy to local interwidget communicaton via the {@link iwc.Proxy#connect} function.
 *  	Pass an onIntent handler to handle intents that have been sent by local or remote widgets.
 * 		E.g. var yourIntentHandler = function(intent){...}; proxy.connect(yourIntentHandler);
 * 		If you do not want to process intents, call the connect function without a parameter
 * 	</li>
 * 	<li>
 * 		publish an intent by means of {@link iwc.Proxy#publish}. If the intent shall be processed by remote widget containers make
 * 		sure that you set the flag {@link iwc.util.FLAGS}.PUBLISH_GLOBAL in the flag field of the passed JSON intent object.
 * 	</li>
 * </ol>
 * 
 * @author Christian Hocken (hocken@dbis.rwth-aachen.de)
 * @requires wsxmpp.js
 * @requires openapp.js
 * @requires lasAjaxClient.js	
 */
iwc.Proxy = function() {
	this._componentName = "unknown";
	var that = this;
	
	if (typeof window.location !== "undefined" &&
			typeof window.location.search === "string" &&
			typeof window.unescape === "function") {
		var pairs = window.location.search.substring(1).split("&"), pair, query = {};
		if (!(pairs.length == 1 && pairs[0] === "")) {
			for (var p = 0; p < pairs.length; p++) {
				pair = pairs[p].split("=");
				if (pair.length == 2) {
					query[pair[0]] =
						window.unescape(pair[1]);
				}
			}
		}
		if (typeof query.url === "string") {
			this._componentName = query.url;
		}
	};
	
	//private variables:
	
	//switch to enable/disable interwidget communication
	this._connected = false;
	
	//a connected object of type XmppClient that is used for XMPP communication
	this._xmppClient = null;
	
	//an object of type LasAjaxClient that is used for LAS service invocations
	this._lasClient = null;
	
	//the PubSub service that holds the pubsub node
	this._pubSubEntity = null;
	//the PubSub node intents are sent to / received from
	this._pubSubNode = null;
	
	//callbacks
	
	//onError is called when an error occurs. The passed object is a String
	this.onError = function(error){};
	
	//onIntent is called when an intent is received. A JSON intent object is passed to the function
	this.onIntent = function(){};

	
	//onDroppedIntent is called when an intent is received but the trust threshold prohibits the delivery. A JSON intent object is passed to the function
	this.onDroppedIntent = function(){};
	
	//the callback that has primarily been registered in the XmppClient object.
	//Since only items with namespace 'http://dbis.rwth-aachen.de/~hocken/da/xsd/Intent'
	//shall be handled by the IWC framework we need to forward items of other types
	//this._primaryRegisteredOnEventCB = null;
	
	//variables used in the trust extension:
	
	//switch to enable/disable trust extension
	this._useTrustExtension = false;
	 
	//the trust threshold used in the trust extension
	this._trustThreshold = 0;
	
	//private functions:
	
	/**
	 * sets the LAS client used in the trust extension.
	 * @param lasClient (Object) a connected object of type LasAjaxClient
	 */
	this.setLasClient = function(lasClient) {
		this._lasClient = lasClient;
	};
	
	/**
	 * utility function that transforms a JSON intent object into a XML element
	 * @param intent (Object) a valid JSON intent object
	 * @returns (Object) XML element representing the passed JSON intent object
	 */
	this._jsonIntentToXmlIntent = function(intent) {
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
	 * @param callback (Function(intent)) is called when a local or remote JSON intent object is received.
	 */
	this.connect = function(callback){
		//console.log("Entering connect");
		//workaround for broken ECMAScript
		var self = this;
		this.onIntent = callback;
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
				
				if (result.type == "result") {
					var subscribedToNode = false;
					for (var i=0; i < result.subscriptions.length; i++) {
						var sub = result.subscriptions[i];
						if (sub.node == self._pubSubNode)
							subscribedToNode = true;
					}
					if (!subscribedToNode) {
						
						//console.log("Self JID: " + Strophe.getBareJidFromJid(self._xmppClient.jid));
						
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
	this.disconnect = function() {
		gadgets.openapp.disconnect();
		this._connected = false;
	};



	/**
	 * sets the XMPP client used for remote interwidget communication
	 * @param xmppClient (Object) a connected object of type XmppClient
	 */
	this.setXmppClient = function(xmppClient) {

		this._xmppClient = xmppClient;
		
		this._xmppClient.addHandler(this._messageHandler.bind(this), null, "message", null, null, this._pubSubEntity, null);

	};

	/**
	 * creates and configures a PubSub node for remote interwidget communication
	 * @param entity (String) The PubSub service
	 * @param node (String) The name of the node that shall handle remote interwidget communication
	 * @param cb (Function(result)) The result of the call {@link XmppClient#createAndConfigurePubSubNode}
	 */
	this.createAndConfigurePubSubNodeForRIWC = function(entity, node, cb) {
		var self = this;
		this.setPubSubNode(entity,node);
		if (this._xmppClient != null) {
			var fields = [];
			fields[0] = {"var":"pubsub#deliver_payloads" , "val":["1"]};
			fields[1] = {"var":"pubsub#deliver_notifications" , "val":["1"]};
			fields[2] = {"var":"pubsub#persist_items" , "val":["0"]};
			fields[3] = {"var":"pubsub#access_model" , "val":["open"]};
			fields[4] = {"var":"pubsub#publish_model" , "val":["open"]};
			fields[5] = {"var":"pubsub#notification_type" , "val":["normal"]};
			fields[6] = {"var":"pubsub#send_last_published_item" , "val":["never"]};
			
			//fields[2] = {"var":"pubsub#notify_config" , "val":["0"]};
			//fields[3] = {"var":"pubsub#notify_delete" , "val":["0"]};
			//fields[4] = {"var":"pubsub#notify_retract" , "val":["0"]};
			//fields[7] = {"var":"pubsub#max_items" , "val":["10"]};
			//fields[7] = {"var":"pubsub#subscribe" , "val":["1"]};
			//fields[10] = {"var":"pubsub#roster_groups_allowed" , "val":[""]};
			
			//fields[10] = {"var":"pubsub#purge_offline" , "val":["1"]};
			
			//fields[12] = {"var":"pubsub#max_payload_size" , "val":["10000"]};
			//
			//fields[14] = {"var":"pubsub#presence_based_delivery" , "val":["1"]};
		
			this._xmppClient.pubsub.createAndConfigureNode(entity, node, fields, function(result) {
				self._pubSubEntity = entity;
				self._pubSubNode = node;
				
				if (result.type == "result"){
					self._xmppClient.pubsub.subscribe(
						Strophe.getBareJidFromJid(self._xmppClient.jid), 
						self._pubSubEntity, 
						self._pubSubNode,
						function(result){
							//console.log("Subsceeeeeription result: ");
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
							}	
							else if(result.type == "error"){
								//console.log("############## Error");
								//console.log(result)
							}
						}
					);
					
				} else{
					//console.log("############ Error on creating node " + node);
				}
				//console.log(that);
				self.connect(cb);
			});
		}
		
	};
	
	/**
	 * the handler function that handles remote interwidget communication.
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
					
					//console.log("We have found an intent...");
					var intent = that._xmlIntentToJsonIntent(nIntent);
					//console.log(intent);
					if (iwc.util.validateIntent(intent)) {
						
						var publisherJid = intent.sender;
						if (publisherJid.indexOf("?") > -1)
						  publisherJid = publisherJid.split("?")[0];
					
						//check if this instance is the sender of the intent.
						//if so, do not process it
						if (this._xmppClient.jid !== publisherJid) {
							if (typeof intent.categories != "undefined" && intent.categories.indexOf("DUI") != -1
								&& typeof intent.component != "undefined" 
								&& (intent.component == "duimanager" || intent.component == ""))
								return;
							//publish locally
							//console.log("");
							this.publish(intent);
						}
					}
				}
			}
		}
	};

	/**
	 * the handler function that handles local interwidget communication.
	 * If you do not have good reason, do not overwrite it.
	 *
	 * @param event the JSON intent object sent from a local widget
	 */
	this.liwcEventHandler = function(envelope, message) {
		var publishGlobal = false;
		//'component' and 'sender' properties must always be available in 'intent' objects
		if (typeof message.component != "undefined" && typeof message.sender != "undefined") {
			if (typeof message.categories != "undefined" && message.categories.indexOf("DUI") != -1
				//the target component of the intent must be this or a broadcast
				&& (message.component == "duimanager" || message.component == "")
				//only accept local intents
				&& (typeof message.flags == "undefined" || message.flags.indexOf("PUBLISH_GLOBAL") == -1))
				return;
			else if (message.component == this._componentName || message.component == "") {
				this.onIntent(message);
			}
			if (typeof message.flags != "undefined") {
				for (var i=0; i < message.flags.length; i++) {
					var flag = message.flags[i];
					if (flag == iwc.util.FLAGS.PUBLISH_GLOBAL) {
						publishGlobal = true;
					}
				}
			}
			//when flag PUBLISH_GLOBAL is set check if this intent originates from a pubsub event
			if (publishGlobal && message.sender.indexOf("@") == -1) {
				this.publishIntent(message,function(stanza){
					//console.log("Publish Callback");
					//console.log(stanza);
				});
			}
		}
	};
		
	this._messageHandler = function(stanza){
		
		//console.log("Received Message: ");
		//console.log(stanza);
		var fchild=stanza.childNodes[0];
		
		if(fchild.nodeName === "event" && fchild.getAttribute("xmlns") == "http://jabber.org/protocol/pubsub#event"){
			
			//console.log("Pubsub event detected... ");
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
				//console.log("Event Parsed...");
				//console.log(eventparams);
				var fromNode = fchild.firstChild.attributes[0].nodeValue;
				if (fromNode == this._pubSubNode) //messages from other space node are meaningless
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
					console.log("Configuration before publish complete?");
					console.log(result);
				*/
					//console.log("Publishing intent... "+self._xmppClient.jid+":"+self._xmppClient.service);
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
					
					intent["extras"] = JSON.parse(dval);
				}
			}
		}
		
		return intent;
	};
};

//==================================================  definition of iwc.Client ================================================== //

/** 
 * @class
 * 
 * An object of type {@link iwc.Client} handles local interwidget communication.
 * <p/>
 * A client object is instantiated as follows:
 * <ol>
 * 	<li>
 * 		create an object of type {@link iwc.Client}. E.g.var client = new iwc.Client()
 * 	</li>
 * 	<li>
 * 		call {@link iwc.Client#connect} to connect the client to local interwidget communication. Pass a callback function
 * 		that handles received intents. E.g var yourIntentHandler = function(intent){...}; var client = new iwc.Client(yourIntentHandler);
 * 	</li>
 * 	<li>
 * 		publish an intent by means of {@link iwc.Client#publish}. If the intent shall be processed by remote widget containers make
 * 		sure that you set the flag {@link iwc.util.FLAGS}.PUBLISH_GLOBAL in the flag field of the passed intent.
 * 	</li>
 * </ol>
 * 
 * @author Christian Hocken (hocken@dbis.rwth-aachen.de)
 * @requires openapp.js
 * @params categories (Array of String) to filter intents of particular types. //TODO
 */
iwc.Client = function(categories) {
	this._componentName = "unknown";
	
	if (typeof window.location !== "undefined" &&
			typeof window.location.search === "string" &&
			typeof window.unescape === "function") {
		var pairs = window.location.search.substring(1).split("&"), pair, query = {};
		if (!(pairs.length == 1 && pairs[0] === "")) {
			for (var p = 0; p < pairs.length; p++) {
				pair = pairs[p].split("=");
				if (pair.length == 2) {
					query[pair[0]] =
						window.unescape(pair[1]);
				}
			}
		}
		if (typeof query.url === "string") {
			this._componentName = query.url;
		}
	};
	
	//private variables
	this._connected = false;	
	this._categories = categories;
	
	//onIntent is called when an intent is received. A JSON intent object is passed to the function
	this.onIntent = function(){};
};

/**
 * connects the client to local interwidget communication
 * @param callback (Function(intent)) is called when a local or remote JSON intent object is received.
 */
iwc.Client.prototype.connect = function(callback) {
	this.onIntent = callback;
	
	//workaround for broken ECMAScript
	var self = this;
	//register callback with 'this' as scope
	gadgets.openapp.connect(function(envelope, message){self.liwcEventHandler(envelope, message);});
	this._connected = true;
};

/**
 * disconnects the client from local interwidget communication
 */
iwc.Client.prototype.disconnect = function() {
	gadgets.openapp.disconnect();
	this._connected = false;
};


/**
 * publishs an intent locally. If the flag {@link iwc.util.FLAGS}.PUBLISH_GLOBAL is set
 * the intent will also be sent to the Publish/Subscribe node if an object of type {@link iwc.Proxy}
 * is present in the widget container
 * @param intent (Object) JSON intent object
 */
iwc.Client.prototype.publish = function(intent) {
	if (intent.sender == null)
		intent.sender = this._componentName;
	if (iwc.util.validateIntent(intent)) {
		var envelope = {"type":"JSON", "event":"publish", "message":intent};
		gadgets.openapp.publish(envelope);
	}
};

/**
 * handler function that handles local interwidget communication.
 * If you do not have good reason, do not overwrite it
 * @param envelope (Object) the OpenApp envelope object
 * @param message (Object) the message wrapped in the envelope object
 */
iwc.Client.prototype.liwcEventHandler = function(envelope, message) {
	//'component' and 'sender' properties must always be available in 'intent' objects
	if (typeof message.component != "undefined" && typeof message.sender != "undefined") {
		if (message.component == this._componentName || message.component == "") {
			//explicit intent
			this.onIntent(message);
		}
	}
};

//==================================================  definition of iwc.util ==================================================//

/**
 * @class 
 * 
 * Class iwc.util provies utility functions used by {@link iwc.Proxy} and {@link iwc.Client}
 * 
 * @author Christian Hocken (hocken@dbis.rwth-aachen.de)
 */
iwc.util = function() {
};

/**
 * validates the passed intent
 * @params intent (Object) the JSON intent object to be validated
 * @returns true, if the intent is valid
 */
iwc.util.validateIntent = function(intent) {
	if (typeof intent.component != "string") {
		throw new Error("Intent object must possess property 'component' of type 'String'");
	}
	if (typeof intent.data != "string") {
		throw new Error("Intent object must possess property 'data' of type 'String'");
	}
	if (typeof intent.dataType != "string") {
		throw new Error("Intent object must possess property 'dataType' of type 'String'");
	}
	return true;
};

/**
 * namespaces used in remote interwidget communication
 */
iwc.util.NS = {
		//TODO: replace by new namespace; probably ROLE purl.org?
		INTENT : "http://dbis.rwth-aachen.de/~hocken/da/xsd/Intent"
};

/**
 * flags that are known to the framework and that are processed if set
 */
iwc.util.FLAGS = {
		PUBLISH_LOCAL : "PUBLISH_LOCAL",
		PUBLISH_GLOBAL : "PUBLISH_GLOBAL"
};

/**
 * actions that are known to the framework and that are processed if set
 */
iwc.util.ACTIONS = {
		INVOKE : "ACTION_INVOKE_SERVICE" //TODO
};

return iwc;

});
