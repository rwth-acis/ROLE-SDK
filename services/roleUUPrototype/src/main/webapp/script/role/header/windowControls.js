define([ "com", "jquery", "role/ui/ui", "handlebars!./windowControls" ], function(com, $, ui, template) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "left",
	
	createUI : function(container) {
		$(template({})).appendTo(container);
		var wc = this;
		setTimeout(function() {
			var max = $("#maximize");
			var norm = $("#normal");
			if (ui.embedded) {
				max.show();
				max.click(function() {
					max.hide();
					norm.show();
					window.parent.postMessage(JSON.stringify({"rolePLE": {windowSize: "maximum"}}), "*");
				});
				norm.click(function() {
					norm.hide();
					max.show();
					window.parent.postMessage(JSON.stringify({"rolePLE": {windowSize: "normal"}}), "*");
				});
			}
			
			var stickSide = $("#fixedSidePanel");
			var unStickSide = $("#autoHideSidePanel");
			stickSide.click(function() {
				ui.autoHideSidePanel(false, 200);
			});
			unStickSide.click(function() {
				ui.autoHideSidePanel(true, 200);
			});
			com.on("http://purl.org/role/ui/UI#", "autoHideSidePanel", wc.checkSidePanelAutoHide);
		}, 1);
	},
	
	checkSidePanelAutoHide: function() {
		var fixedSP = $("#fixedSidePanel");
		var autoHideSP = $("#autoHideSidePanel");

		if (ui.autoHide) {
			fixedSP.show();
			autoHideSP.hide();
		} else {
			autoHideSP.show();
			fixedSP.hide();			
		}
	}
}; });