var openapp_forceXhr = true;

require(
		{
			priority: [ "vendor/jquery.ui", "vendor/jquery.validate", "vendor/jquery.url" ]
		},
		[
		 	"vendor/jquery.touch.punch",
		 	"vendor/es5-shim",
		 	"domReady",
		 	"com",
		 	"detectmobile",
		 	"container", // /gadgets/js/container.js?c=1&container=default&debug=1
		 	"openapp",
		 	"role"
	 	], function(touchpunch, es5shim, domReady, com, detectMobile) {
			function loadCss(url) {
			    var link = document.createElement("link");
			    link.type = "text/css";
			    link.rel = "stylesheet";
			    link.href = url;
			    document.getElementsByTagName("head")[0].appendChild(link);
			}
			
			domReady(function() {
				
				if (detectMobile.isMobile()){
					var meta = document.createElement("meta");
					var width = screen.width;
					console.log("width is " + width);
					console.log("length is " + screen.length)
					meta.content = "width="+width+"px; initial-scale=1.0; maximum-scale=1.0;";
					meta.name = "viewport";
					document.getElementsByTagName("head")[0].appendChild(meta);
					
					loadCss("/s/css/jquery-ui-1.8.13/themes/base/jquery-ui.css");
					//loadCss("/s/css/default.css");
					loadCss("/s/css/role-mobile.css");
				}
				else{
					loadCss("/s/css/jquery-ui-1.8.13/themes/base/jquery-ui.css");
					//loadCss("/s/css/default.css");
					if (detectMobile.isTouch())
						loadCss("/s/css/role-tablet.css");
					else
						loadCss("/s/css/role.css");
				}
				com.trigger({}, "http://purl.org/role/ui/DOMReady#", "domReady");
				window.postMessage(JSON.stringify(_openapp_event), "*");
			});
		}
);
