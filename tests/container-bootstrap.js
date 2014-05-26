my = {};

//----------Stuff to override in each test------------------//

//Overrride this array with uris to gadget specifications (xml files).
my.gadgetSpecUrls = [];

//Overrride this function to do get logging of openapp events.
my.log = function(message) {
	  document.getElementById("output").innerHTML += gadgets.util.escapeString(message) + "<br/>";
}

//-----------End of stuff to override ----------------------//



my.baseUrl = "http://"+ window.location.host + "/";

my.init = function() {	
	gadgets.openapp.connect(function(envelope, message) {
		if (envelope.event === "openapp") {
			if (envelope.hello === true) {

				envelope.source.postMessage(JSON.stringify({ OpenApplicationEvent:
				  { event: "openapp", welcome: true, message:
				  { postParentOnly: true } } }), "*");
			} else if (envelope.receipt === true) {

				gadgets.container.gadgetAcceptedOAEvent(
				  envelope.source.frameElement.id);

			}
		} else if (typeof envelope.source !== "undefined") {
			if (envelope.source === window) {
				envelope.sender = "container";
			} else {
				var senderId = envelope.source.frameElement.id;
				envelope.sender = my.gadgetSpecUrls[parseInt(senderId[senderId.length - 1])];
			}
			envelope.viewer = "nouser";
			var data = JSON.stringify({ OpenApplicationEvent: envelope });
			var frames = window.frames;
			for (var i = 0; i < frames.length; i++) {
				frames[i].postMessage(data, "*");
			}
		
		}
	});
		gadgets.pubsubrouter.init(
		function(id) {
			return my.gadgetSpecUrls[parseInt(id[id.length - 1])];
		},
		{
    		onSubscribe: function(sender, channel) {
    		  my.log(sender + " subscribes to channel '" + channel + "'");
		      // return true to reject the request.
			},
			onUnsubscribe: function(sender, channel) {
		      my.log(sender + " unsubscribes from channel '" + channel + "'");
		      // return true to reject the request.
		    },
			onPublish: function(sender, channel, message) {
  			  my.log(sender + " publishes '" + message + "' to channel '" + channel + "'");
		      // return true to reject the request.
			}
	})
};

my.renderGadgets = function() {
	var chromeNames = [];
	var gadgets = [];
	
	my.getGadgetSpecs(function() {
		for (var i = 0; i < my.gadgetSpecUrls.length; ++i) {
			var gadget = shindig.container.createGadget({specUrl: my.gadgetSpecs[i].url});
			if (my.gadgetSpecs[i].height != null) {
				gadget.height = my.gadgetSpecs[i].height;
			}
			if (my.gadgetSpecs[i].title != null) {
				gadget.title = my.gadgetSpecs[i].title;
			}
			gadgets.push(gadget);
			shindig.container.addGadget(gadget);
			chromeNames.push("gadget-chrome-"+i);
	 	}
		shindig.container.layoutManager.setGadgetChromeIds(chromeNames);
		for (var j = 0; j < gadgets.length; ++j) {
			shindig.container.renderGadget(gadgets[j]);
	 	}		
	});
};


my.getGadgetSpecs = function(callback) {
	var _gadgets = [];
	for (var i = 0; i < my.gadgetSpecUrls.length; ++i) {
		my.gadgetSpecUrls[i] = my.baseUrl+my.gadgetSpecUrls[i];
		_gadgets.push({url: my.gadgetSpecUrls[i], moduleId: 1});
	}
	
	var request = {
		context: {
			country: "default",
			language: "default",
			view: "default",
			container: "default"
		},
		gadgets: _gadgets
	};
	
	var makeRequestParams = {
		"CONTENT_TYPE" : "JSON",
		"METHOD" : "POST",
		"POST_DATA" : gadgets.json.stringify(request)
	};
	
	var secureToken = "john.doe:john.doe:appid:cont:url:0:default";
	var serverBase = "/gadgets/";
	var url = serverBase +"metadata?st=" + secureToken;
	
	gadgets.io.makeNonProxiedRequest(url,
		handleJSONResponse,
		makeRequestParams,
		"application/javascript"
	);
	
	function handleJSONResponse(obj) {
		//Unfortunately the gadgetspecs sometimes come back in the wrong order, sort via my.gadgetSpecUrls.
		my.gadgetSpecs = [];
		var gSpecs = {};
		var toSort = obj.data.gadgets;
		for (var i=0;i<toSort.length;i++) {
			gSpecs[toSort[i].url] = toSort[i];
		}
		for (var j = 0; j < my.gadgetSpecUrls.length;j++) {
			my.gadgetSpecs[j] = gSpecs[my.gadgetSpecUrls[j]];
		}
		callback();
	}
};