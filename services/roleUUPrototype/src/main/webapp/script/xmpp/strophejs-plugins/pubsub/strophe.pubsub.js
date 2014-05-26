define(["../../strophe"], function(Strophe) {
/*
  Copyright 2008, Stanziq  Inc.
*/

Strophe.addConnectionPlugin('pubsub', {
/*
  Extend connection object to have plugin name 'pubsub'.  
*/
    _connection: null,

	//The plugin must have the init function.
	init: function(conn) {

	    this._connection = conn;

	    /*
	      Function used to setup plugin.
	    */
	    
	    /* extend name space 
	     *  NS.PUBSUB - XMPP Publish Subscribe namespace
	     *              from XEP 60.  
	     *
	     *  NS.PUBSUB_SUBSCRIBE_OPTIONS - XMPP pubsub
	     *                                options namespace from XEP 60.
	     */
	    Strophe.addNamespace('PUBSUB',"http://jabber.org/protocol/pubsub");
	    Strophe.addNamespace('PUBSUB_SUBSCRIBE_OPTIONS', Strophe.NS.PUBSUB+"#subscribe_options");
	    Strophe.addNamespace('PUBSUB_ERRORS', Strophe.NS.PUBSUB+"#errors");
	    Strophe.addNamespace('PUBSUB_EVENT', Strophe.NS.PUBSUB+"#event");
	    Strophe.addNamespace('PUBSUB_OWNER', Strophe.NS.PUBSUB+"#owner");
	    Strophe.addNamespace('PUBSUB_AUTO_CREATE', Strophe.NS.PUBSUB+"#auto-create");
	    Strophe.addNamespace('PUBSUB_PUBLISH_OPTIONS', Strophe.NS.PUBSUB+"#publish-options");
		Strophe.addNamespace('PUBSUB_SUBSCRIBE_OPTIONS',Strophe.NS.PUBSUB+"#subscribe_options");
	    Strophe.addNamespace('PUBSUB_NODE_CONFIG', Strophe.NS.PUBSUB+"#node_config");
	    Strophe.addNamespace('PUBSUB_CREATE_AND_CONFIGURE', Strophe.NS.PUBSUB+"#create-and-configure");
	    Strophe.addNamespace('PUBSUB_SUBSCRIBE_AUTHORIZATION', Strophe.NS.PUBSUB+"#subscribe_authorization");
	    Strophe.addNamespace('PUBSUB_GET_PENDING', Strophe.NS.PUBSUB+"#get-pending");
	    Strophe.addNamespace('PUBSUB_MANAGE_SUBSCRIPTIONS', Strophe.NS.PUBSUB+"#manage-subscriptions");
	    Strophe.addNamespace('PUBSUB_META_DATA', Strophe.NS.PUBSUB+"#meta-data");
	    
	},
	
	statusChanged: function(status){
		if (status === Strophe.Status.CONNECTED) {
			//console.log("pubsub plugin: connected");
			//this._connection.addHandler(this.onevent.bind(this), null, "message");
		} else if(status === Strophe.Status.DISCONNECTED){
			//console.log("pubsub plugin: disconnected");
		}
	},
	
	/***Function
	    
	Create a pubsub node on the given service with the given node
	name.
	
	Parameters:
	(String) jid - The node owner's jid.
	(String) service - The name of the pubsub service.
	(String) node -  The name of the pubsub node.
	(Dictionary) options -  The configuration options for the  node.
	(Function) call_back - Used to determine if node
	creation was sucessful.
	
	Returns:
	Iq id used to send subscription.
	*/
	createNode: function(service,node,options, call_back) {
	    
	    var iqid = this._connection.getUniqueId("pubsubcreatenode");
	    
	    var iq = $iq({to:service, type:'set', id:iqid});
	    
	    var c_options = Strophe.xmlElement("configure",[]);
	    var x = Strophe.xmlElement("x",[["xmlns","jabber:x:data"]]);
	    var form_field = Strophe.xmlElement("field",[["var","FORM_TYPE"],
							 ["type","hidden"]]);
	    var value = Strophe.xmlElement("value",[]);
	    var text = Strophe.xmlTextNode(Strophe.NS.PUBSUB+"#node_config");
	    value.appendChild(text);
	    form_field.appendChild(value);
	    x.appendChild(form_field);
	    
	    for (var i in options)
	    {
		var val = options[i];
		x.appendChild(val);
	    }
	    
	    if(options.length && options.length != 0)
	    {
		c_options.appendChild(x);
	    }
	    
	    iq.c('pubsub',
		{xmlns:Strophe.NS.PUBSUB}).c('create',
		    {node:node}).up().cnode(c_options);
		
	    this._connection.addHandler(call_back,
				  null,
				  'iq',
				  null,
				  iqid,
				  null);
	    this._connection.send(iq.tree());
	    return iqid;
	},
	/***Function
	    Subscribe to a node in order to receive event items.
	    
	    Parameters:
	    (String) jid - The node owner's jid.
	    (String) service - The name of the pubsub service.
	    (String) node -  The name of the pubsub node.
	    (Array) options -  The configuration options for the  node.
	    (Function) event_cb - Used to recieve subscription events.
	    (Function) call_back - Used to determine if node
	    creation was sucessful.
	    
	    Returns:
	    Iq id used to send subscription.
	*/
	subscribe: function(jid,service,node, event_cb, call_back) {
	    
	    var subid = this._connection.getUniqueId("subscribenode");
	    
	    //create subscription options
	    var sub = $iq({from:jid, to:service, type:'set', id:subid});
		sub.c('pubsub', { xmlns:Strophe.NS.PUBSUB }).c('subscribe',
		{node:node,jid:jid});
	    
		var cb=function(result){
			call_back(_parseIqResponse(result));
		};
		
	    this._connection.addHandler(cb,
				  null,
				  'iq',
				  null,
				  subid,
				  null);
	    
	    //add the event handler to receive items 
	    
		this._connection.addHandler(event_cb,
				  null,
				  'message',
				  null,
				  null,
				  null);
				  
		this._connection.send(sub.tree());
	    return subid;
	    
	},
	
	configureSubscription: function(entity,node,fields,callback){
		//console.log("Configuring Node Subscription... ");
		/*
		<iq type='set' to='pubsub.role-sandbox.eu' id='options2'>
			<pubsub xmlns='http://jabber.org/protocol/pubsub'>
				<options node='spacey-space-batmunch' jid='b816df26-3deb-4cfe-96df-263deb4cfee4@role-sandbox.eu'>
					<x xmlns='jabber:x:data' type='submit'>
						<field var='FORM_TYPE' type='hidden'><value>http://jabber.org/protocol/pubsub#subscribe_options</value></field>
						<field var='pubsub#deliver'><value>1</value></field>
						<field var='pubsub#digest'><value>0</value></field>
						<field var='pubsub#include_body'><value>true</value></field>
						<field var='pubsub#show-values'><value>chat</value>
							<value>online</value>
							<value>away</value>
						</field>
					</x>
				</options>
			</pubsub>
		</iq>
		*/
		var id=this._connection.getUniqueId();
		
		var stanza = $iq({id:id,type:"set",to:entity});
		
		var optionsElement = Strophe.xmlElement("options",{"node":node,"jid":Strophe.getBareJidFromJid(this._connection.jid)});
		var xElement = Strophe.xmlElement("x",{"xmlns":"jabber:x:data", "type":"submit"});
		
		var formTypeElement = Strophe.xmlElement("field",{"var":"FORM_TYPE","type":"hidden"});
		var formTypeValueElement = Strophe.xmlElement("value",{},Strophe.NS.PUBSUB_SUBSCRIBE_OPTIONS);
		formTypeElement.appendChild(formTypeValueElement);
		
		xElement.appendChild(formTypeElement);
		
		// now go through map of configuration fields
		for (var i=0; i < fields.length; i++){
			var field = fields[i];
			var fieldElement = Strophe.xmlElement("field",{"var":field["var"]});
			
			for (var j=0; j< field["val"].length ; j++) {
				var value = field["val"][j];
				var fieldValueElement = Strophe.xmlElement("value",{},value);
				fieldElement.appendChild(fieldValueElement);
			}
			
			xElement.appendChild(fieldElement);
		}
		
		optionsElement.appendChild(xElement);
		
		stanza.c('pubsub',{xmlns:Strophe.NS.PUBSUB}).cnode(optionsElement);	
		
		//console.log("Subscription configuration: ");
		//console.log(stanza);
		
		var cb=function(result){
			callback(_parseCreateNodeResponse(result));
		};
		
		this._connection.addHandler(cb,
				  null,
				  'iq',
				  null,
				  id,
				  null);
	    this._connection.send(stanza.tree());	
	},
	
	/***Function
	    Unsubscribe from a node.
	    
	    Parameters:
	    (String) jid - The node owner's jid.
	    (String) service - The name of the pubsub service.
	    (String) node -  The name of the pubsub node.
	    (Function) call_back - Used to determine if node
	    creation was sucessful.
	    
	*/    
	unsubscribe: function(jid,service,node, call_back) {
	    
	    var subid = this._connection.getUniqueId("unsubscribenode");
	    
	    
	    var sub = $iq({from:jid, to:service, type:'set', id:subid})
	    sub.c('pubsub', { xmlns:Strophe.NS.PUBSUB }).c('unsubscribe',
		{node:node,jid:jid});

	    
	    
	    this._connection.addHandler(call_back,
				  null,
				  'iq',
				  null,
				  subid,
				  null);
	    this._connection.send(sub.tree());
	    
	    
	    return subid;
	    
	},
	/***Function
	    
	Publish and item to the given pubsub node.
	
	Parameters:
	(String) jid - The node owner's jid.
	(String) service - The name of the pubsub service.
	(String) node -  The name of the pubsub node.
	(Array) items -  The list of items to be published.
	(Function) call_back - Used to determine if node
	creation was sucessful.
	*/    
	publish: function(jid, service, node, items, call_back) {
	    var pubid = this._connection.getUniqueId("publishnode");
	    
	    
	    var publish_elem = Strophe.xmlElement("publish",
						  [["node",
						    node]]);
	    for (var i in items)
	    {
			var item = Strophe.xmlElement("item",[]);
			var entry = Strophe.xmlElement("entry",[]);
			var t = Strophe.xmlTextNode(items[i]);
			entry.appendChild(t);
			item.appendChild(entry);
			publish_elem.appendChild(item);
	    }
	    
	    var pub = $iq({from:jid, to:service, type:'set', id:pubid})
	    pub.c('pubsub', { xmlns:Strophe.NS.PUBSUB }).cnode(publish_elem);
	    
	    
	    this._connection.addHandler(call_back,
				  null,
				  'iq',
				  null,
				  pubid,
				  null);
	    this._connection.send(pub.tree());
	    
	    
	    return pubid;
	},
	/*Function: items
	  Used to retrieve the persistent items from the pubsub node.
	  
	*/
	items: function(jid,service,node,ok_callback,error_back) {
	    var pub = $iq({from:jid, to:service, type:'get'})
	    
	    //ask for all items
	    pub.c('pubsub', 
		{ xmlns:Strophe.NS.PUBSUB }).c('items',{node:node});
	    
	    return this._connection.sendIQ(pub.tree(),ok_callback,error_back);
	},
	
	discoverNodeItems: function (service, node, callback) {
		return Strophe.service-discovery.discoverEntityItems(service,node,callback);
	},
	
	discoverNodeInformation: function (service, node, callback) {
		return Strophe.service-discovery.discoverEntityInformation(service,node,callback);
	},
	
	retrieveSubscriptions: function(service, node, callback){ 
		
		var pubid = this._connection.getUniqueId();
		var stanza = null;
		if (node != null && node != ""){	
			stanza = $iq({"id":pubid,"type":"get","to":service}).c("pubsub",{"xmlns":Strophe.NS.PUBSUB}).c("subscriptions", {"node":node});
		} else { 
			stanza = $iq({"id":pubid,"type":"get","to":service}).c("pubsub",{"xmlns":Strophe.NS.PUBSUB}).c("subscriptions", {});
		}
		
		var cb=function(result){
			callback(_parseSubscriptions(node,result));
		};
		
		this._connection.addHandler(cb,
				  null,
				  'iq',
				  null,
				  pubid,
				  null);
		
	    this._connection.send(stanza.tree());
	},
	
	/**
	 * retrieves all affiliations of the calling entity at the PubSub service. By passing a node parameter, 
	 * affiliations in a collection of nodes can be retrieved. If affiliations at the root level of 
	 * the service shall be retrieved, no node parameter must be passed.
	 * @param service (String) the JID of the PubSub service entity that stores nodes
	 * @param node (String) identifier of the collection-node from which affiliations should be retrieved
	 * @param callback (Function(result)) a callback for processing the retrieved affiliations represented 
	 * by a JSON object of one of the following forms, depending on the received iq stanza type (either 'result' or 'error'):
	 * 1. Success case: <tt>{"type":"result", "affiliations":[{"node":<node>,"affiliation":<e.g. publisher,owner>},...]}</tt>
	 * 2. Error case: <tt>{"type":error, "stanza":<stanza returned by server>}</tt>
	 * The 'type' property of the passed JSON object can be used to distinguish both cases.
	 * 
	 */
	retrieveAffiliations: function(service, node, callback) {
		
		var pubid = this._connection.getUniqueId();
		var stanza = null;
		if (node != null && node != ""){	
			stanza = $iq({"id":pubid,"type":"get","to":entity}).c("pubsub",{"xmlns":Strophe.NS.PUBSUB}).c("affiliations", {"node":node});
		} else { 
			stanza = $iq({"id":pubid,"type":"get","to":entity}).c("pubsub",{"xmlns":Strophe.NS.PUBSUB}).c("affiliations", {});
		}
		
		var cb=function(result){
			callback(_parseAffiliations(node,result));
		};
		
		this._connection.addHandler(cb,
				  null,
				  'iq',
				  null,
				  pubid,
				  null);
	    this._connection.send(stanza.tree());
		
	},
	
	createAndConfigureNode : function(entity, node, fields, callback) {
		
		var id=this._connection.getUniqueId();
		
		var stanza = $iq({id:id,type:"set",to:entity});
		
		var configureElement = Strophe.xmlElement("configure",{});
		var xElement = Strophe.xmlElement("x",{"xmlns":"jabber:x:data", "type":"submit"});
		
		var formTypeElement = Strophe.xmlElement("field",{"var":"FORM_TYPE","type":"hidden"});
		var formTypeValueElement = Strophe.xmlElement("value",{},Strophe.NS.PUBSUB_NODE_CONFIG);
		formTypeElement.appendChild(formTypeValueElement);
		
		xElement.appendChild(formTypeElement);
		
		// now go through map of configuration fields
		for (var i=0; i < fields.length; i++){
			var field = fields[i];
			var fieldElement = Strophe.xmlElement("field",{"var":field["var"]});
			
			for (var j=0; j< field["val"].length ; j++) {
				var value = field["val"][j];
				var fieldValueElement = Strophe.xmlElement("value",{},value);
				fieldElement.appendChild(fieldValueElement);
			}
			
			xElement.appendChild(fieldElement);
		}
		
		configureElement.appendChild(xElement);
		
		stanza.c('pubsub',{xmlns:Strophe.NS.PUBSUB}).c('create',{'node':node}).up().cnode(configureElement);	
		
		var cb=function(result){
			callback(_parseCreateNodeResponse(result));
		};
		
		this._connection.addHandler(cb,
				  null,
				  'iq',
				  null,
				  id,
				  null);
	    this._connection.send(stanza.tree());	
	},
	
	publishItem: function(entity, node, item, callback) {
		// Standard Reference: http://xmpp.org/extensions/xep-0060.html#publisher-publish
		// Example 99. Publisher publishes an item with an ItemID
		
		//<iq type='set'
		//    from='hamlet@denmark.lit/blogbot'
		//    to='pubsub.shakespeare.lit'
		//    id='publish1'>
		//  <pubsub xmlns='http://jabber.org/protocol/pubsub'>
		//    <publish node='princely_musings'>
		//      <item id='bnd81g37d61f49fgn581'>
		//        <entry xmlns='http://www.w3.org/2005/Atom'>
		//          <title>Soliloquy</title>
		//          <summary>
		//				To be, or not to be: that is the question...
		//          </summary>
		//          <link rel='alternate' type='text/html'
		//                href='http://denmark.lit/2003/12/13/atom03'/>
		//          <id>tag:denmark.lit,2003:entry-32397</id>
		//          <published>2003-12-13T18:30:02Z</published>
		//          <updated>2003-12-13T18:30:02Z</updated>
		//        </entry>
		//      </item>
		//    </publish>
		//  </pubsub>
		//</iq>
		
		var id=this._connection.getUniqueId();
		
		var stanza = $iq({id:id,type:"set",to:entity});
		
		//we let the server generate the item id to make sure that it is unique
		var pubsube = Strophe.xmlElement("pubsub",{"xmlns":Strophe.NS.PUBSUB});
		var publishe = Strophe.xmlElement("publish",{"node":node});
		var iteme = Strophe.xmlElement("item");
		iteme.appendChild(item);
		publishe.appendChild(iteme);
		pubsube.appendChild(publishe);
		stanza.cnode(pubsube);
		
		var cb=function(result){
			callback(_parsePublishItemResponse(result));
		};
		
		this._connection.addHandler(cb,
				  null,
				  'iq',
				  null,
				  id,
				  null);
	    this._connection.send(stanza.tree());	
	},
	
	/**
	 * deletes an item at a node that exists at a PubSub service.
	 * @param service (String) the JID of the PubSub service entity the node is located on. E.g. 'pubsub.shakespeare.lit'
	 * @param node (String) identifier of the node to retrieve items from
	 * @param itemId the item id of the item to be deleted
	 * @param callback (Function(result)) a callback for processing the response represented 
	 * by a JSON object of one of the following forms, depending on the received iq stanza type (either 'result' or 'error'):
	 * 1. Success case: <tt>{"type":"result"}</tt>
	 * 2. Error case: <tt>{"type":error, "stanza":<stanza returned by server>}</tt>
	 * The 'type' property of the passed JSON object can be used to distinguish both cases.
	 * 
	 */
	deleteItem: function(service, node, itemId, callback) {
		
		var pubid = this._connection.getUniqueId();
		
		var stanza = $iq({"id":pubid,"type":"set","to":service}).c("pubsub",{"xmlns":Strophe.NS.PUBSUB}).c("retract", {"node":node}).c("item", {"id":itemId});
		
		var cb=function(result){
			callback(_parseDeleteItemResponse(result));
		};
		
		this._connection.addHandler(cb,
				  null,
				  'iq',
				  null,
				  pubid,
				  null);
	    this._connection.send(stanza.tree());
		
	}
});

/*********** Internal helper functions **********************

/**
 * internal function to parse a stanza being the response on a retrieve subscriptions request
 * 
 * @param stanza (DOM) the stanza to be parsed
 * @returns {Object} a JSON object representing the response. The returned JSON object is of one of the following forms,
 * depending on the received iq stanza type (either 'result' or 'error'):
 * 1. Success case: <tt>{"type":"result", "subscriptions":[{"jid":<jid>,"node":<node>,"subid":<subid>,"subscription":<subscription state>},...]}</tt>
 * 2. Error case: <tt>{"type":error, "stanza":<stanza returned by server>}</tt>
 * The 'type' property of the returned JSON object can be used to distinguish both cases.
 * @private
 */
function _parseSubscriptions(node, stanza) {
	
	var type = stanza.getAttribute('type');
	
	if(type=="result") {
		var subscriptions = stanza.getElementsByTagName("pubsub")[0].getElementsByTagName("subscriptions")[0].getElementsByTagName("subscription");
		var sres =[];
		
		for (var i = 0; i< subscriptions.length; i++){
			var subscription = subscriptions[i];
			
			var sjid = subscription.getAttribute("jid");
			var snode = subscription.getAttribute("node");
			if(snode == null){
				snode=node;
			}
			var ssubid = subscription.getAttribute("subid");
			var ssubscription = subscription.getAttribute("subscription");
			
			var s = {"jid":sjid,"node":snode,"subid":ssubid,"subscription":ssubscription};
			sres.push(s);
		}
		return {"type":type,"subscriptions":sres};
	}
	else {
		return {"type":type,"stanza":stanza};
	}	
}


/**
 * internal function to parse a stanza being the response on a retrieve affiliations request
 * 
 * @param stanza (DOM) the stanza to be parsed
 * @returns {Object} a JSON object representing the response. The returned JSON object is of one of the following forms,
 * depending on the received iq stanza type (either 'result' or 'error'):
 * 1. Success case: <tt>{"type":"result", "affiliations":[{"node":<node>,"affiliation":<e.g. publisher,owner>},...]}</tt>
 * 2. Error case: <tt>{"type":error, "stanza":<stanza returned by server>}</tt>
 * The 'type' property of the returned JSON object can be used to distinguish both cases.
 * @private
 */
function _parseAffiliations(stanza) {
	var type = stanza.getAttribute('type');
	
	if(type=="result") {
		var affiliations = stanza.getElementsByTagName("pubsub")[0].getElementsByTagName("affiliations")[0].getElementsByTagName("affiliation");
		var ares =[];
		for (var i = 0; i< affiliations.length; i++){
			var affiliation = affiliations[i];

			var anode = affiliation.getAttribute("node");
			var aaffiliation = affiliation.getAttribute("affiliation");
			
			var a = {"node":anode,"affiliation":aaffiliation};
			ares.push(a);
		}
		return {"type":type,"affiliations":ares};
	}
	else {
		return {"type":type,"stanza":stanza};
	}	
}

/**
 * internal function to parse a stanza being the response on a publish item request
 * 
 * @param stanza (DOM) the stanza to be parsed
 * @returns {Object} a JSON object representing the response. The returned JSON object is of one of the following forms,
 * depending on the received iq stanza type (either 'result' or 'error'):
 * 1. Success case: <tt>{"type":"result"}</tt>
 * 2. Error case: <tt>{"type":error, "stanza":<stanza returned by server>}</tt>
 * The 'type' property of the returned JSON object can be used to distinguish both cases.
 * @private
 */
function _parsePublishItemResponse(stanza) {
	
	var type = stanza.getAttribute('type');
	
	if(type=="result") {
		return {"type":type};
	}
	else {
		return {"type":type, "stanza":stanza};
	}	
}

/**
 * internal function to parse a stanza being the response on the create node request
 * 
 * @param (DOM) the stanza to be parsed
 * @returns {Object} a JSON object representing the response. The returned JSON object is of one of the following forms,
 * depending on the received iq stanza type (either 'result' or 'error'):
 * 1. Success case: <tt>{"type":"result"}</tt>
 * 2. Error case: <tt>{"type":error, "stanza":<stanza returned by server>}</tt>
 * The 'type' property of the returned JSON object can be used to distinguish both cases.
 * @private
 */
function _parseCreateNodeResponse(stanza) {
	var type = stanza.getAttribute('type');
	
	if(type=="result") {
		return {"type":type};
	}
	else {
		return {"type":type, "stanza":stanza};
	}	
}

function _parseIqResponse(stanza) {
	var type = stanza.getAttribute('type');
	
	if(type=="result") {
		return {"type":type};
	}
	else {
		return {"type":type, "stanza":stanza};
	}	
}

});
