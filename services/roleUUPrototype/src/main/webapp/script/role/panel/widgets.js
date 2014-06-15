define([ "com", "jquery", "../model/space", "../feature/widget", "../ui/ui",
         "handlebars!./widgets", "handlebars!./widget", "../feature/duimanager"], function(
        		 com, $, space,widgetFeature, ui, template, widgetTemplate, duiManager) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#",
	               "http://purl.org/role/ui/Feature#"],
	
	getTitle : function() {
		return "Widgets";
	},

	createUI : function(container) {
		$(container).parent().addClass("activitiesPanel").addClass("activitiesPanelLast");
		$(template()).appendTo(container);
		com.on("http://purl.org/role/ui/Space#", "load", function() {
		});
		com.on("http://purl.org/role/ui/Widget#", "add", function(widget) {
			//added by Ke Li: when the widget is in the widget list but not in the to-be-displayed list, do not render it.
			if (space.isMember() && duiManager.widgets.indexOf(widget.getRegionWidgetId()) != -1 
					&& duiManager.widgetsWhiteList.indexOf(widget.getRegionWidgetId()) == -1)
				return;
			var element = $(widgetTemplate({
				title: widget.getTitle(),
				regionWidgetId: widget.getRegionWidgetId()
			}));
			element.appendTo($(container).find("#widgetEntries"));
			element.slideDown();
		});
		com.on("http://purl.org/role/ui/Widget#", "remove", function(widget) {
			$("#sideEntry-" + widget.getRegionWidgetId()).remove();
		});
	},
	
	load : function() {
		com.on("http://purl.org/role/ui/DOMReady#", "domReady", function() {
			$(document.body).on("click", "#sideEntry-widgetStore", function(event) {
				event.preventDefault();
				ui.browse('http://embedded.role-widgetstore.eu', 'widgetStore');
			});
			$(document.body).on("click", "#sideEntry-addWidgetURL", function(event) {
				event.preventDefault();
				widgetFeature.addWidget();
			});
			$(document.body).on("click", "#sideEntry-addSelectedWidget", function(event) {
				event.preventDefault();
				$(this).hide();
				widgetFeature.addWidget($(this).attr('href'));
			});
			$(document.body).on("click", ".sideEntryTool", function(event) {
				event.preventDefault();
				$(this).stop(true, true);
				if (this.id.match(/\d+/) != null)	//added by Ke Li: the device .sideEntryTool does not have the id like other .sideEntryTools and acts differently
					ui.canvas(this.id.match(/\d+/)[0], false);
				window.setTimeout(function(){
					widgetFeature._fixIframes();
				}, 100);
			});
		
			$(document.body).on("dblclick", ".sideEntryTool", function() {
				if (this.id.match(/\d+/) != null)	//added by Ke Li: the device .sideEntryTool does not has the id like other .sideEntryTools and acts differently
					ui.canvas(this.id.match(/\d+/)[0], true);
				window.setTimeout(function(){
					widgetFeature._fixIframes();
				}, 100);
			});
			
			$(document.body).on("dblclick", ".widget-title-bar", function() {
				ui.canvas(this.id.match(/\d+/)[0], true);
				window.setTimeout(function(){
					widgetFeature._fixIframes();
				}, 100);
			});
			
			$(document.body).on("click", ".widget-toolbar-max-btn", function() {
				ui.canvas(this.id.match(/\d+/)[0], true);
				window.setTimeout(function(){
					widgetFeature._fixIframes();
				}, 100);
			});
		}.bind(this));
	}
	
}; });
