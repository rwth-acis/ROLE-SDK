define(["jquery", "handlebars!./sidepanelheader"], function($, template){
	var sidePanel = {
		createUI: function(isMobile){
			var container = $(".header-left");
			$(template()).appendTo(container);
			if (!isMobile){
				$("#hideconsole").show();
				$("#showconsole").hide();
			}
			$("#showconsole").click(function(){
				$("#sidebar").show();
				$("#hideconsole").show();
				$("#showconsole").hide();
			});
			$("#hideconsole").click(function(){
				$("#sidebar").hide();
				$("#hideconsole").hide();
				$("#showconsole").show();
			});
		}
	};
	return sidePanel;
});