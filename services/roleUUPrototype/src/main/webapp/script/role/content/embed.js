define([ "com", "jquery", "handlebars!./embed" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	space: null,
	
	createUI : function(container) {
		this.container = container;
		container.hide();
		var self = this;
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {
			self.space = space;
			var node = $(template({spaceurl: space.getUri()}));
			node.appendTo(container);
			var f = function() {
				this.focus();
				this.select();
			};
			var c = function() {
				self.update();
			};
			self.urlNode = node.find(".embed_alts input").first().click(f);
			self.scriptNode = node.find("#script_embed").click(f);
			self.iframeNode = node.find("#iframe_embed").click(f);
			self.widthNode = node.find("#space_width").change(c);
			self.heightNode = node.find("#space_height").change(c);
			self.dashboardNode = node.find("#dashboard_check").change(c);
			node.find(".role_content_close").click(function() {self.toggleVisible();});
		});
	},
	
	toggleVisible: function() {
		this.container.toggle();
		this.update();
		this.initializeSocialSharing();
	},
	
	update: function() {
		var uri = this.space.getUri();
		var _w = this.widthNode.val();
		var _h = this.heightNode.val();
		var _s = uri.substr(uri.lastIndexOf('/')+1);
		var _base = window.location.href.substr(0, window.location.href.indexOf(window.location.pathname));
		var dashboard = this.dashboardNode.is(":checked");
		if (dashboard) {
			this.urlNode.attr("value", uri);			
		} else {
			this.urlNode.attr("value", uri+"?dashboard=false");
		}
		var scriptUri = _base + "/s/script/embed.js"+
			"?width="+_w+
			"&height="+_h+
			"&space="+_s+
			"&dashboard="+dashboard;
		var iframeUri = uri+"?dashboard="+dashboard;
		this.scriptNode.text('<script src="'+scriptUri+'"></script>');
		this.iframeNode.text('<iframe '+
			'frameborder="0" style="'+
			'width:'+_w+';'+
			'height:'+_h+';" '+
			'src="'+iframeUri+'"></iframe>');
	},
	
	initializeSocialSharing: function() {
		if (this._socialSharingInited) {
			return;
		}
		this._socialSharingInited = true;
		
		//Facebook
		(function(d, s, id) {
  			var js, fjs = d.getElementsByTagName(s)[0];
  			if (d.getElementById(id)) return;
  			js = d.createElement(s); js.id = id;
  			js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
  			fjs.parentNode.insertBefore(js, fjs);
		})(document, 'script', 'facebook-jssdk');
		
		//Twitter
		(function(d,s,id){
			var js,fjs=d.getElementsByTagName(s)[0];
			if(!d.getElementById(id)){
				js=d.createElement(s);
				js.id=id;
				js.src="//platform.twitter.com/widgets.js";
				fjs.parentNode.insertBefore(js,fjs);
			}
		})(document,"script","twitter-wjs");
		
		//Google plus
		(function() {
	    	var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
	    	po.src = 'https://apis.google.com/js/plusone.js';
    		var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
  		})();
	}
}; });