openapp=typeof openapp!=="undefined"?openapp:{};

/**
 * Open Application Event API.
 * 
 * Choice of communication channel:
 * 1. If requirements for postMessage are met, use postMessage.
 * 2. If requirements for pubsub are met, use pubsub.
 * 3. No channel; communication is disabled (and thus the Open Application events are disabled).
 */
(function() {
	
	var prefs = new gadgets.Prefs();
	openapp.remote = !prefs.getBool("spaceControl");
	if (openapp.remote) {		
		//Set up of remoteCall functionality, actual remoteCall has to be set further down.
		var callbackCounter = 0, id2callback = {};
		openapp.remoteCall = function(methodName, args, callback) {
			var uniqueId= "cb"+callbackCounter++;
			id2callback[uniqueId] = {callback: callback, initiated: new Date()};
			openapp.remoteCallImpl({method: methodName, args: args, id: uniqueId});
		};
		openapp.remoteCallback = function(obj) {
			var callback = id2callback[obj.id];
			delete id2callback[obj.callbackId];
			if (callback) {
				callback.callback(obj.value);
			}
		};
		
		//Initialization for openapp aware code should be done via openapp.registerOnLoadHandler
		//openapp.ready should be called by code below when openapp calls can be taken.  
		var oaInited = false, gInited = false, handlers = [];
		var isInited = function() {return oaInited && gInited;};
		var notifyIfReady = function() {
			if (isInited()) {
				for (var i=0;i<handlers.length;i++) {
					handlers[i]();
				}
			}
		};
		gadgets.util.registerOnLoadHandler(function() {
			gInited = true;
			notifyIfReady();
		});
		openapp.registerOnLoadHandler = function(handler) {
			if (isInited()) {
				handler();
			} else {
				handlers.push(handler);
			}
		};
		openapp.ready = function() {
			oaInited = true;
			notifyIfReady();
		};
	} else {
		var spaceUri = prefs.getString("spaceUri");
		if (spaceUri == null) {
			alert("space control gadget need to provide a user preference \"sapceUri\" that points to a space on a space service");
		}
		openapp.dm = new openapp.data.OSDataManager(spaceUri, prefs.getString("spaceUriExtention"));
	}
		
	/**
	 * Whether or not postMessage is the communication channel that is to be used.
	 *
	 * Openapp events when using postMessage:
	 * 1. When the script is loaded, notify the parent (i.e., the container) that "I exist and
	 *  want to use openapp event communication via postMessage".
	 * 2. When an event is published, broadcast the event to (1) the parent (container) and
	 *  (2) the parent's frames (all gadgets).
	 * 3. If a special event is received from the parent as a reply to 1. above, that says it
	 *  wants to handle broadcast of events to other frames, then in 2., skip (2).
	 * 4. When an event is received, then if the event is accepted (i.e., the callback returns
	 *  true), send a special event to the parent to notify it of the receipt.
	 */
	var usePostMessage = typeof window !== "undefined" && typeof window.parent !== "undefined"
	  && typeof window.postMessage !== "undefined" && typeof JSON !== "undefined"
	  && typeof JSON.parse !== "undefined" && typeof JSON.stringify !== "undefined";

	/**
	 * Whether or not gadgets.pubsub is the communication channel that is to be used.
	 */
	var usePubSub = !usePostMessage && typeof gadgets !== "undefined" && typeof gadgets.pubsub
	  !== "undefined" && typeof gadgets.pubsub.subscribe !== "undefined" &&
	  typeof gadgets.pubsub.unsubscribe !== "undefined" && typeof gadgets.pubsub.publish
	  !== "undefined";

	/**
	 * Initialization data received from the parent, with default values.
	 */
	var init = {
		/**
		 * Whether events should only be sent to the parent or be broadcasted to both the
		 *  parent and all the parent's frames.
		 */
		postParentOnly: false
	};

	/**
	 * The callback function specified by a call to connect is kept here.
	 */
	var doCallback = null;

	/**
	 * The internal callback function that in turn calls doCallback is kept here.
	 */
	var onMessage = null;
	if (usePostMessage) {

		onMessage = function(event) {
			if (typeof event.data === "string" && event.data.slice(0, 25)
			  === "{\"OpenApplicationEvent\":{") {
				var envelope = JSON.parse(event.data).OpenApplicationEvent;
				if (envelope.event === "openapp" && envelope.welcome === true
				  && event.source === window.parent) {
					for (var p in envelope.message) {
						if (envelope.message.hasOwnProperty(p)) {
							init[p] = envelope.message[p];
						}
					}
				} else {
					envelope.source = event.source;
					envelope.origin = event.origin;
					envelope.toJSON = function() {
						var json = {};
						for (var e in this) {
							if (this.hasOwnProperty(e)
							  && typeof this[e] !== "function"
							  && e !== "source" && e !== "origin") {
								json[e] = this[e];
							}
						}
						return json;
					};
					if (typeof doCallback === "function") {
						if (doCallback(envelope, envelope.message)
						  === true) {
							window.parent.postMessage(JSON.stringify(
							  { OpenApplicationEvent: { event:
							  "openapp", receipt: true } }), "*");
						}
					}
				}
			} else if (typeof event.data === "string" && event.data.slice(0, 23)
			  === "{\"OpenApplicationRpc\":{") {
				var obj = JSON.parse(event.data);
				if (openapp.remote) {
					if (obj.OpenApplicationRpc.control) {
						openapp.remoteCallImpl = function(data) {
							var str = JSON.stringify({"OpenApplicationRpc": data});
							event.source.postMessage(str, "*");
						};
						openapp.ready();					
					} else if (event.source != window) {
						openapp.remoteCallback(obj.OpenApplicationRpc);
					}
				} else {
					//First check if it is a hello call, then respond with a init call.
					if (obj.OpenApplicationRpc.hello) {
						var data = JSON.stringify({ OpenApplicationRpc: {control: true}});
						event.source.postMessage(data, "*");
					} else if (event.source !== window){
					  	//Upon receiving an rpc call, call the correct method on the dm.
						var args = obj.OpenApplicationRpc.args;
						//Make sure the callback uses postmessage to send back the results.
						args.push(function(value) {
							var result = {OpenApplicationRpc: {value: value, id: obj.OpenApplicationRpc.id}};
							event.source.postMessage(JSON.stringify(result), "*");
						});
						openapp[obj.OpenApplicationRpc.method].apply(openapp.dm, args);					
					}
				}
			}
		};
		window.addEventListener("message", onMessage, false);
		if (typeof window.parent !== "undefined") {
//			window.parent.postMessage(JSON.stringify({ OpenApplicationEvent:
//			  { event: "openapp", hello: true } }), "*");
			if (openapp.remote) {
				//Hello message to spaceControl
				var data = JSON.stringify({ OpenApplicationRpc: {hello: true}});
				window.parent.postMessage(data, "*");
				if (!init.postParentOnly) {
					var frames = window.parent.frames;
					for (var i = 0; i < frames.length; i++) {
						frames[i].postMessage(data, "*");
					}
				}
			} else {
				//Control message from spaceControl to all other frames
				var data = JSON.stringify({ OpenApplicationRpc: {control: true}});
				window.parent.postMessage(data, "*");
				var frames = window.parent.frames;
				for (var i = 0; i < frames.length; i++) {
					frames[i].postMessage(data, "*");
				}
			}
		}
	} else if (usePubSub) {

		onMessage = function(sender, envelope) {
			envelope.source = undefined;
			envelope.origin = undefined;
			envelope.sender = sender;
			if (typeof doCallback === "function") {
				if (doCallback(envelope, envelope.message) === true) {
					gadgets.pubsub.publish("openapp-recieve", true);  // [sic]
				}
			}
		};

	}
	
	/**
	 * The RDF namespace (specified here as it is commonly used).
	 */
	openapp.RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/**
	 * Sets the function to be called when an event has occurred. The callback function
	 *  will be called as: callback(envelope, message)
	 */
	openapp.connect = function(callback) {
		doCallback = callback;
		if (usePubSub) {

			gadgets.pubsub.subscribe("openapp", onMessage);

		}
	};

	/**
	 * Stops calls from being made to the callback function set using connect(callback).
	 */
	openapp.disconnect = function() {
		if (usePubSub) {

			gadgets.pubsub.unsubscribe("openapp");

		}
		doCallback = null;
	};

	/**
	 * Publishes an event. The message may be given either as envelope.message or as
	 *  the second argument.
	 */
	openapp.publish = function(envelope, message) {
		envelope.event = envelope.event || "select";
		envelope.sharing = envelope.sharing || "public";
		envelope.date = envelope.date || new Date();
		envelope.message = message || envelope.message;
		if (usePostMessage) {

			var data = JSON.stringify({ OpenApplicationEvent: envelope });
			if (window.parent !== "undefined") {
				window.parent.postMessage(data, "*");
				if (!init.postParentOnly) {
					var frames = window.parent.frames;
					for (var i = 0; i < frames.length; i++) {
						frames[i].postMessage(data, "*");
					}
				}
			} else {
				window.postMessage(data, "*");
			}

		} else if (usePubSub) {

			gadgets.pubsub.publish("openapp", envelope);

		}
	};
})();
