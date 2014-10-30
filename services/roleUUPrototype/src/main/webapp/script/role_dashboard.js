(function(window, realWindow) {
// jQQ START
if(typeof window.jQQ !== 'object'){
(function() {
    var callbacks = [],jq;
    function loadScript(url, callback){
        var script = document.createElement("script")
        script.type = "text/javascript";
        if (typeof script.readyState === 'undefined'){  
            script.onload = function(){
                callback();
            };            
        } else { // IE LAST!
            script.onreadystatechange = function(){
                if (script.readyState === "loaded" || script.readyState === "complete"){
                    script.onreadystatechange = null;
                    callback();
                }
            };            
        };
        script.src = url;
        document.getElementsByTagName("head")[0].appendChild(script);
        return script;
    };
    function validateCallback(callback) {
        if(typeof callback === 'undefined') throw "Cannot validate callback: undefined";
        if(callback && callback.length<1) throw "Callback missing at least 1 placeholder argument";
        return callback;
    };
    function fillArray(data,qty) {
        var array  = [];
        for(var i=qty;i>0;i--) array.push(data);
        return array;
    };
    window.jQQ = {
      isReady: false,
      isolate: function() {
          var callback = validateCallback(arguments[0]);
          if( !window.jQQ.isReady ) return callbacks.push( callback );
          return callback.apply( this, fillArray( jq, callback.length ) );

      },
      setup: function(version) {
          // wait for document to load...
          if(!document.body) return window.onload = function(){ window.jQQ.setup(version) };
          //var url = 'http://ajax.googleapis.com/ajax/libs/jquery/'+(version||'1.4.2')+'/jquery.min.js';
          var url = '/s/script/vendor/jquery.'+(version||'1.4.2')+'.min.js';
          loadScript( url , function() {
              window.jQQ.isReady = true;
              // this stores the new version and gives back the old one, completely.              
              jq = jQuery.noConflict(true);
              callbacks.forEach(window.jQQ.isolate);
              delete(callbacks);
              
          });
      }
    };
})(window.jQuery,window.$)
}
//jQQ END
var jQQ = window.jQQ;
jQQ.setup('1.7.1');
jQQ.isolate(function($){
	window = realWindow;
	// Dashboard START
	var role = role || {};
	role.dashboard = (function(){
		
		var dashboardToggle = function() {
			var expandedHeight = Math.floor(screen.height/3);
			var collapsedHeight = 25;
			if ($("#_role_dashboard_container").hasClass("dashboard-expanded")) {
				$("#_role_dashboard_overlay").css("bottom", "0px");
				$('#_role_dashboard_container').animate({
					height : collapsedHeight + 'px'
				}).removeClass('dashboard-expanded')
				.find("iframe").attr("scrolling", "auto");
				shadeOverlay.bind("mousedown", dashboardCollapse);
				shadeOverlay.hide();
			} else {
				$('#_role_dashboard_container').animate({
					height : expandedHeight + 'px'
				}).addClass('dashboard-expanded')
				.find("iframe").attr("scrolling", "auto");
				$("#_role_dashboard_overlay").css("bottom",
						(expandedHeight - collapsedHeight) + "px");
				shadeOverlay.bind("mousedown", dashboardCollapse);
				shadeOverlay.show();
			}
		};
		
		var dashboardCollapse = function() {
			shadeOverlay.unbind("mousedown", dashboardCollapse);
			if ($("#_role_dashboard_container").hasClass("dashboard-expanded")) {
				dashboardToggle();
			}
		};
		
		var dragOrigin, originalHeight, dragLayer;
		var dashboardPull = function(event) {
			var dragDistance, newHeight;
			if (!dragLayer) {
				dragLayer = $("<div/>").css("position", "fixed").css(
						"left", "0px").css("top", "0px")
						.css("right", "0px").css("bottom", "0px").css(
								"z-index", "999999");
				dragLayer.appendTo(document.body);
				$(dragLayer).on('mousemove',
						dashboardPull);
				$(dragLayer).on('mouseup',
						dashboardPullEnd);
				$(dragLayer).on('touchmove',
						dashboardPull);
				$(dragLayer).on('touchend',
						dashboardPullEnd);
			}
			if (typeof event.targetTouches !== "undefined") {
				dragDistance = event.targetTouches[0].clientY - dragOrigin;
				newHeight = Math.max(originalHeight - dragDistance, 25);
			} else {
				dragDistance = event.pageY - dragOrigin;
				newHeight = Math.max(originalHeight - dragDistance, 25);
			}
			$("#_role_dashboard_container").height(newHeight);
			$("#_role_dashboard_overlay").css("bottom", (newHeight - 25) + "px");
			if (newHeight === 25) {
				$("#_role_dashboard_container").removeClass('dashboard-expanded').find("iframe").attr("scrolling", "auto");
				shadeOverlay.unbind("mousedown", dashboardCollapse);
				shadeOverlay.hide();
			} else {
				$("#_role_dashboard_container").addClass('dashboard-expanded').find("iframe").attr("scrolling", "auto");
				shadeOverlay.bind("mousedown", dashboardCollapse);
				shadeOverlay.show();
			}
		};

		var dashboardPullEnd = function(event) {
			var dragDistance, newHeight;
			if (typeof event.targetTouches !== "undefined") {
				newHeight = $("#_role_dashboard_container").height();
			} else {
				dragDistance = event.pageY - dragOrigin;
				newHeight = Math.max(originalHeight - dragDistance, 25);
			}
			var dashboardOverlay =
					document.getElementById("_role_dashboard_overlay");
			$(dashboardOverlay).off('mousemove',
					dashboardPull);
			$(dashboardOverlay).off('mouseup',
					dashboardPullEnd);
			$(dashboardOverlay).off('touchmove',
					dashboardPull);
			$(dashboardOverlay).off('touchend',
					dashboardPullEnd);
			if (dragLayer) {
				$(dragLayer).remove();
			}
			dragLayer = null;
			if (newHeight < 200 && newHeight !== 25) {
				$("#_role_dashboard_container").animate({
					height : "25px"
				}).removeClass('dashboard-expanded').find("iframe").attr("scrolling", "auto");
				$(dashboardOverlay).css("bottom", "0px");
				shadeOverlay.hide();
			}
		};

		var dashboardPullStart = function(event) {
			if (event.target.tagName === "A") {
				return;
			}
			if ($("#_role_dashboard_container").css("top") !== "auto") {
				dashboardRemoveSurface();
			}
			if (navigator.userAgent.match(/Android/i)) {
				event.preventDefault();
			}
			if (typeof event.targetTouches !== "undefined") {
				dragOrigin = event.targetTouches[0].clientY;
			} else {
				dragOrigin = event.pageY;
			}
			originalHeight = $("#_role_dashboard_container").height();
			var dashboardOverlay =
				document.getElementById("_role_dashboard_overlay");
			$(dashboardOverlay).on('mousemove',
					dashboardPull);
			$(dashboardOverlay).on('mouseup',
					dashboardPullEnd);
			$(dashboardOverlay).on('touchmove',
					dashboardPull);
			$(dashboardOverlay).on('touchend',
					dashboardPullEnd);
			return false;
		};
		
		var dashboardToggleSurface = function() {
			//shadeOverlay.bind("mousedown", dashboardRemoveSurface);
			//shadeOverlay.show();
			$("#_role_dashboard_container").css("top", "0px").css("height", "auto");
		};
		
		var dashboardRemoveSurface = function() {
			//shadeOverlay.unbind("mousedown", dashboardRemoveSurface);
			//shadeOverlay.hide();
			$("#_role_dashboard_container").css("top", "").css("height", "25px");
		};
		
		var publishPageEvent = function() {
			var metas = document.getElementsByTagName('meta'), nme, descr, title;
			for (var x=0; x<metas.length; x++) {
			  nme = metas[x].name || (typeof metas[x].attributes["property"] !== "undefined" ? metas[x].attributes["property"].value : undefined) || "";
			  if (nme.toLowerCase() == "description" || nme.toLowerCase() == "og:description") {
			    descr = metas[x].content;
			  }
			}
			var msg = {
					OpenApplicationEvent: {
						event: "select",
						uri: document.location.href,
						type: "namespaced-properties",
						message: {
							"http://purl.org/dc/terms/title": $("title").text()
						}
					}
				};
			if (descr) {
				msg.OpenApplicationEvent.message["http://purl.org/dc/terms/description"] = descr;
			}
			frameWindow.postMessage(JSON.stringify(msg), "*");
		};
		
		var publishTextEvent = function(text) {
			var msg = {
					OpenApplicationEvent: {
						event: "select",
						type: "namespaced-properties",
						message: {
							"http://www.role-project.eu/rdf/words/term": text
						}
					}
				};
			lastText = text;
			frameWindow.postMessage(JSON.stringify(msg), "*");
		};
	
		var shadeOverlay;
		var frameWindow;
		var initialize = function() {
			if (document.getElementById("_role_dashboard_container")) {
				frameWindow = $("#_role_dashboard_container").find("iframe").get(0).contentWindow;
				publishPageEvent();
				return;
			}
			var messageListener = function(message) {
				if (message.data.substring(0, 21) === "{\"ROLEDashboardCall\":") {
					var call = JSON.parse(message.data).ROLEDashboardCall;
					switch (call.name) {
					case "dashboardToggle":
						dashboardToggle();
						break;
					case "dashboardToggleSurface":
						dashboardToggleSurface();
						break;
					case "dashboardRemoveSurface":
						dashboardRemoveSurface();
						break;
					}
				}
			};
			if (typeof window.addEventListener !== "undefined") {
				window.addEventListener("message", messageListener, false);
			} else if (typeof window.attachEvent !== "undefined") {
				window.attachEvent("onmessage", messageListener);
			}
			var dashboard = $("<div/>")
				.attr("id", "_role_dashboard_container").css("z-index", "9999")
				.css("position", "fixed").css("left", "0px").css("right", "0px")
				.css("bottom", "0px").css("width", "100%").css("height", "25px");
			var frame = $("<iframe/>").attr("src", _role_userprofile_href)
				.attr("id", "_role_dashboard_iframe")
				.attr("scrolling", "auto").attr("marginwidth", "0").attr("marginheight", "0")
				.attr("frameborder", "0").attr("vspace", "0").attr("hspace", "0")
				.attr("width", "100%").attr("height", "100%");
			var dashboardOverlay = $("<div/>").css("position", "fixed").css("left", "0px")
				.css("bottom", "0px").css("width", "150px").css("height", "25px")
				.css("z-index", "10000").attr("id", "_role_dashboard_overlay");
			shadeOverlay = $("<div/>")
				.css("z-index", "9998")
				.css("position", "fixed")
				.css("left", "0px")
				.css("right", "0px")
				.css("top", "0px")
				.css("bottom", "0px")
				.css("background", "#000")
				.css("opacity", "0.3")
				.css("display", "none");
			
			dashboardOverlay.add(dashboardToggle);
			dashboardOverlay.mousedown('mousedown', dashboardPullStart);
			dashboardOverlay.bind('touchstart', dashboardPullStart);
			frame.appendTo(dashboard);
			dashboard.appendTo(document.body);
			dashboardOverlay.appendTo(document.body);
			shadeOverlay.appendTo(document.body);
			
			frameWindow = frame.get(0).contentWindow;
			if (typeof _role_dashboard_manualinit === "undefined") {
				frame.load(function() {
					window.setTimeout(function() {
						publishPageEvent();
					}, 1000);
				});
			}
			
			$("html").mouseup(function() {
				window.setTimeout(function() {
					var selObj = window.getSelection();
					var selectedText = selObj.toString();
					if (selectedText.trim().length > 0) {
						publishTextEvent(selectedText);
					}
				}, 1);
			});
		};
		
		return {
			init: initialize,
			dashboardToggle : dashboardToggle,
			dashboardToggleSurface : dashboardToggleSurface,
			dashboardRemoveSurface : dashboardRemoveSurface
		};
		
	})();
	if (typeof _role_dashboard_manualinit === "undefined") {
		role.dashboard.init();
	} else {
		window.role_dashboard = role.dashboard;
	}
	// Dashboard END
});
})({}, window);