define([ "com", "jquery", "handlebars!./info" ], function(com, $, template, config) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	space: null,
	
	createUI : function(container) {
		this.container = container;
		container.hide();
		var self = this;
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {
			self.space = space;
			var node = $(template({title: space.getTitle(), description: space.getDescription(), toolallowed: space.isMemberAllowedToAddTools() === true ? "checked" : ""}));
			node.appendTo(container);
			var f = function() {
				this.focus();
				this.select();
			};
			var c = function() {
				self.update();
			};
			node.find(".role_content_close").click(function() {self.toggleVisible();});
			node.find("#info-save-btn").click(function() {
				var t = node.find("#info-edit-title").val();
				var d = node.find("#info-edit-desc").val();
				var a = node.find("#info-edit-toolallowed").attr('checked') ? true : false;
				space.setTitleAndDescription(t, d, a);
				$("#header-space-title").html(t);
				$("#header-space-title-wrapper").attr("title", d);
			});
			if (space.isOwner()) {
				node.find("#info-save-btn").show();
				node.find("#cannotEditMessage").hide();
				node.find("#info-edit-title").removeAttr("disabled");
				node.find("#info-edit-desc").removeAttr("disabled");
				node.find("#info-edit-toolallowed").removeAttr("disabled");
			}
		});
		com.on("http://purl.org/role/ui/Activity#", "select", function(currentActivity) {
			self.currentActivity = currentActivity;
		});
	},
	
	
	toggleVisible: function() {
		this.container.toggle();
		this.update();
	},
	
	update: function() {
	}
}; });