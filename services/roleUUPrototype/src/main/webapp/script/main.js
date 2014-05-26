var openapp_forceXhr = true;

requirejs.config({
    paths: {
        jqueryui: 'vendor/jqueryui'
    }
});

require(
		{
			priority: [ "jqueryui/dialog", "jqueryui/sortable", "vendor/jquery.validate", "vendor/jquery.url" ]
		},
		[
		 	"vendor/es5-shim",
		 	"vendor/console-shim",
		 	"domReady",
		 	"com",
		 	"container", // /gadgets/js/container.js?c=1&container=default&debug=1
		 	"openapp",
		 	"role"
	 	], function(es5shim, consoleShim, domReady, com) {
			function loadCss(url) {
			    var link = document.createElement("link");
			    link.type = "text/css";
			    link.rel = "stylesheet";
			    link.href = url;
			    document.getElementsByTagName("head")[0].appendChild(link);
			}
			
			domReady(function() {
				loadCss("http://ajax.googleapis.com/ajax/libs/jqueryui/1.9.1/themes/base/jquery-ui.css");
				//loadCss("//ajax.aspnetcdn.com/ajax/jquery.ui/1.8.13/themes/base/jquery-ui.css");
				//loadCss("/s/css/default.css");
				loadCss("/s/css/role.css");				
				com.trigger({}, "http://purl.org/role/ui/DOMReady#", "domReady");
				window.postMessage(JSON.stringify(_openapp_event), "*");
			});
		}
);