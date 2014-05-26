_role_space_width = window._role_space_width || "100%"; //Default width
_role_space_height = window._role_space_height || "700px"; //Default height
_role_space_dashboard =window._role_space_dashboard || true; //If true (default) the dashboard is shown on the surrounding page rather than embedded with the space

function __role_space_maximize() {
    if (window._role_space_maximize) {
		window._role_space_maximize();
    } else {
		document.body.style.height = "0px";	
    }

    var space = document.getElementById("_role_space_iframe");
    space.style.height = "100%";
    space.style.width = "100%";
    space.style.position = "fixed";		  
    space.style["z-index"] = "1000";
    space.style.left = "0px";
    space.style.top = "0px";
}

function __role_space_unmaximize() {
    if (window._role_space_unmaximize) {
		window._role_space_unmaximize();
    } else {
	document.body.style.height = "auto";
    }

    var space = document.getElementById("_role_space_iframe");
    space.style.height = _role_space_height;
    space.style.width = _role_space_width;
    space.style.position = "static";
    space.style.left = "auto";
    space.style.top = "auto";
}

(function() {
	//Callback for capturing messages from the space (via postMessage).
	var onMessage = function(event) {
  		if (typeof event.data === "string") {
    		var mess = JSON.parse(event.data).rolePLE;
	  		if (mess) {
	  			if (mess.windowSize === "maximum") {
	      			__role_space_maximize();
	  			}
	  			if (mess.windowSize === "normal") {
	      			__role_space_unmaximize();
	  			}
      		} else if (_role_space_dashboard) {
	  			var dashframe = document.getElementById("_role_dashboard_iframe");
	  			if (dashframe) {
					dashframe.contentWindow.postMessage(event.data, "*")
	  			}
      		}
  		}
	};

	if (typeof window.attachEvent !== "undefined") {
  		window.attachEvent("onmessage", onMessage);
	} else {
  		window.addEventListener("message", onMessage, false);
	}

	// Utility function for extracting parameters from a named script in the page.
	var getScriptParams = function(script_name) {
		// Find all script tags
	  	var scripts = document.getElementsByTagName("script");
	  
		// Look through them trying to find ourselves
		for(var i=0; i<scripts.length; i++) {
			if(scripts[i].src.indexOf("/" + script_name) > -1) {      
				// Get an array of key=value strings of params
	    	  	var pa = scripts[i].src.split("?").pop().split("&");
	
				// Split each key=value into array, the construct js object
				var p = {};
				for(var j=0; j<pa.length; j++) {
					var kv = pa[j].split("=");
					if (kv.length == 2) {
						p[kv[0]] = kv[1];
					} else {
						p[kv[0]] = "";
					}
				}
				p._src = scripts[i].src;
				return p;
			}
		}
		// No scripts match
		return {};
	};
	
	var params = getScriptParams("s/script/embed.js");
	if (params._src) {
	    var pos = params._src.substr(10).indexOf('/');
	    var base = params._src.substr(0, 11+pos);
	    _role_userprofile_href=base + 'user/:app?mode=dashboard';
	    var spaceurl = base + "spaces/" + params.space+"?embedded";
	    for (var attr in params) {
			if (params.hasOwnProperty(attr) && attr !== "width" && attr !== "height" && attr !== "space" && attr !== "dashboard" && attr !== "_src") {
			    spaceurl += "&"+attr;
		    	if (params[attr] !== "") {
					spaceurl += "="+params[attr];
		    	}
			}
	    }
	    spaceurl += "&dashboard=false";     //1) Never show the dashboard internally when we can show it on the page.
	    if (params.dashboard === "false") { //2) Should we show it on the page?
			_role_space_dashboard = false;
	    }
	    _role_space_width = params["width"] || _role_space_width;
	    _role_space_height = params["height"] || _role_space_height;
	    var dimensions = "width:" + _role_space_width + ";height:" + _role_space_height + ";";
	    document.writeln('<iframe id="_role_space_iframe" frameborder="0" style="border-left: 1px solid lightgrey;'+dimensions+'" src="'+spaceurl+'"></iframe>');
	    if (_role_space_dashboard) {
		    document.writeln('<script src="'+base+'s/script/role_dashboard.js"></script>');	    	
	    }
	}
})();