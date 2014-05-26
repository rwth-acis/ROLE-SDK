define(
	[ "jquery", "com", "../ui/ui", "domReady" ],
	function($, com, ui, domReady) {
		
	//If you feel the need to debug iwc.js, it is recommended to try without the dashbord first:
	//http://127.0.0.1:8073/spaces/test?dashboard=false

	domReady(function() {
	
		//Using the com.js infrastructure to invoke methods on all components that implement the event interface.
		//Two methods, the openAppEvent and the openAppReciept are invoked.
		$(document).bind("openapp", function(event) {
			if (event.envelope.hello) {
				return;
			}
			if (event.envelope.receipt) {
				com.invoke("http://purl.org/openapp/int/EventListener#",
					"openAppReceipt", event.envelope, event.message);
			} else {
				com.invoke("http://purl.org/openapp/int/EventListener#",
					"openAppEvent", event.envelope, event.message);
			}
		});
	
		$(document).bind("openapp", function(event) {
			if (event.envelope.receipt) {
				return;
			}
			var envelope = event.envelope;
			var message = event.message;
			if (envelope.event === "openapp") {
				if (envelope.hello === true) {
					envelope.source.postMessage(
						JSON.stringify({
							OpenApplicationEvent : {
													event : "openapp",
													welcome : true,
													message : { postParentOnly : true }
												}
											}), "*");
				}
			} else if (typeof envelope.source !== "undefined") {
				var data = JSON.stringify({
					OpenApplicationEvent : envelope
				});
				var frames = window.frames;
				
				for ( var i = 0; i < frames.length; i++) {
					frames[i].postMessage(data, "*");
				}
				if (ui.embedded && !ui.dashboard && window.parent && window.parent !== window) {
					window.parent.postMessage(data, "*");
				}
			}
		});
	});
});