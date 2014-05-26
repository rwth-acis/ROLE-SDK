define([ "com", "jquery", "handlebars!./browser"], function(com, $, template) { 
	
	var node;	
	return {
		interfaces : [ "http://purl.org/role/ui/Content#" ],
	
		createUI : function(container) {
			node = $(template({})).appendTo(container);
		},
		
		setUrl : function(url, id) {
			if (url != null) {
				window.setTimeout(function() {
					node.find("iframe").attr("src", url);
				}, 1);
			} else {
				node = null;
			}
		},
		
		getUrl: function() {
			if (node) {
				return node.find("iframe").attr("src");	
			} else {
				return;
			}
		} 
}; });