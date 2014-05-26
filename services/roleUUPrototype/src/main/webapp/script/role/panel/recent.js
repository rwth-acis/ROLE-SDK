define([ "com", "jquery", "../ui/ui", "handlebars!./recent" ], function(com, $, ui, recentTemplate) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#",
			"http://purl.org/openapp/int/EventListener#",
			"http://purl.org/role/ui/DOMReady#"],

	_container : null,

	_recentIndex : 0,

	getTitle : function() {
		return "Selections";
	},

	createUI : function(cont) {
		this._container = cont;
	},

	openAppEvent : function(envelope, message) {
		if (envelope.event === "select"
				&& typeof envelope.uri !== "undefined"
				&& typeof message[openapp.ns.dcterms + "title"] !== "undefined"
				&& (typeof envelope.republished === "undefined" || envelope.republished === false)) {
			var offsetWidth = $(this._container).width();
			var recentUI = $(recentTemplate({
				recentIndex : ++this._recentIndex,
				uri : envelope.uri,
				title : message['http://purl.org/dc/terms/title'],
				stringified : JSON.stringify(envelope)
			}));
			$(recentUI).on("click", function(event) {
				event.preventDefault();
				if (!$(event.target).is(".recentReselectButton")) {
					ui.browse(this.href, "recent" + $(this).attr("data-index"));
				}
			});
			recentUI.prependTo(this._container);
			var count = 0;
			$(this._container).find(".sideEntry").each(
					function() {
						if (count == 0) {
							// Prevent element being
							// too narrow
							// during animation
							// (if total height
							// would cause
							// scrollbar to appear)
							//$(this).css("min-width",
							//		(offsetWidth - 20) + "px");
							$(this).effect("blind", {
								mode : "show"
							}, "normal", function() {
								$(this).css("min-width", "0");
							});
						}
						if (++count > 3
								&& !$(this)
										.hasClass("sideEntrySel")) {
							$(this).effect("blind", {
								mode : "hide"
							}, "normal", function() {
								this.parentNode.removeChild(this);
							});
						}
					});
		}
	}

}; });