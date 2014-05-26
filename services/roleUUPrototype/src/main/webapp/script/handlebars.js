define([ "vendor/handlebars" ], function() {
	var templates = {};
	return {
		load : function(resourceName, parentRequire, callback, config) {
			if (resourceName.indexOf(".") === -1) {
				resourceName += ".html";
			}
			if (templates.hasOwnProperty(resourceName)) {
				callback(templates[resourceName]);
			} else {
				parentRequire([ ("text!" + resourceName) ], function(source) {
					var template = Handlebars.compile(source);
					templates[resourceName] = template;
					callback(template);
				});
			}
		}

	};
});