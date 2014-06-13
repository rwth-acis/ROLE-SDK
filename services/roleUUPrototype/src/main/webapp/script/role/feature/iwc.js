define(
[ "jquery", "com", "../ui/ui", "domReady" ],
function($, com, ui, domReady) {
	
domReady(function() {

	$(document).bind(
			"openapp",
			function(event) {
				com.invoke("http://purl.org/openapp/int/EventListener#",
						"openAppEvent", event.envelope, event.message);
			});

	$(document)
			.bind(
					"openapp",
					function(event) {
						var envelope = event.envelope;
						var message = event.message;
						if (envelope.event === "openapp") {
							if (envelope.hello === true) {
								envelope.source
										.postMessage(
												JSON
														.stringify({
															OpenApplicationEvent : {
																event : "openapp",
																welcome : true,
																message : {
																	postParentOnly : true
																}
															}
														}), "*");
							} else if (envelope.receipt === true) {
								var widgetId = envelope.source.frameElement.parentElement.parentElement.id
										.match(/\d+/)[0];
								var dashboardButton = $("#dashboardButton-"
										+ widgetId);
								if (dashboardButton.size() > 0) {
									dashboardButton.stop(true, true);
									var origColor = dashboardButton
											.css("borderBottomColor");
									dashboardButton
											.css("border-color",
													"#ffff00")
											.css("background",
													"url(/s/images/wavebg4.png)");
									dashboardButton.animate({
										borderTopColor : origColor,
										borderLeftColor : origColor,
										borderBottomColor : origColor,
										borderRightColor : origColor
									}, 5000, "linear", function() {
										dashboardButton.css("border",
												"").css("border-color",
												"").css("background",
												"");
									});
								}
								var sideEntry = $("#sideEntry-"
										+ widgetId);
								if (sideEntry.size() > 0) {
									sideEntry.stop(true, true);
									var origColor = sideEntry
											.css("borderBottomColor");
									sideEntry
											.css("border-top",
													"1px solid #ffff00")
											.css("border-bottom",
													"1px solid #ffff00")
											.css("background",
													"url(/s/images/wavebg5.png) #ffff00");
									sideEntry.animate({
										borderTopColor : origColor,
										borderLeftColor : origColor,
										borderBottomColor : origColor,
										borderRightColor : origColor
									}, 5000, "linear", function() {
										sideEntry.css("padding", "")
												.css("border", "").css(
														"border-color",
														"").css(
														"background",
														"");
									});
								}
							}
						} else if (typeof envelope.source !== "undefined") {
							var data = JSON.stringify({
								OpenApplicationEvent : envelope
							});
							var frames = window.frames;
							for ( var i = 0; i < frames.length; i++) {
								frames[i].postMessage(data, "*");
							}
							if (ui.embedded && !ui.dashboard && window.parent && window.parent != window) {
								window.parent.postMessage(data, "*");
							}

							if (envelope.event === "select"
									&& typeof envelope.uri !== "undefined"
									&& typeof message[openapp.ns.dcterms
											+ "title"] !== "undefined"
									&& typeof message[openapp.ns.rdf
											+ "type"] !== "undefined"
									&& message[openapp.ns.rdf + "type"] === "http://purl.org/role/terms/OpenSocialGadget") {

								var sideEntry = $("#sideEntry-addSelectedWidget");
								sideEntry.find(".title").text(
										message[openapp.ns.dcterms
												+ "title"]);
								sideEntry.attr("href", envelope.uri);
								$("#sideEntry-addSelectedWidget")
										.show();
								sideEntry.stop(true, true);
								var origColor = sideEntry.css("color");
								sideEntry.css("background", "#ffff00")
										.css("color", "#000000");
								sideEntry.animate({
									backgroundColor : "#ffffff",
									color : origColor
								}, 5000, "linear", function() {
									sideEntry.css("background", "")
											.css("color", "");
								});

							} else {
								$("#sideEntry-addWidget").hide();
							}

						}
					});
	
});

});