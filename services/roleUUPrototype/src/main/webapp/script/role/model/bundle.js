define([ "com", "jquery", "rdf/converters" ], function(com, $, rdf) { 
	var Bundle = {
		interfaces : [],
		_graph: null,
		
		loadBundle : function(uri, callback) {
			osapi.http.get({
				href: uri,
					format: "string"
				}).execute(function(data) {
					var rdfurl = data.content.match(/<link[^>]*type=\"application\/rdf[^>]*>/g)[0].match(/href=\"([^\"]*)\"/)[1];
					rdfurl = uri.match(/http:\/\/[^/]*/)[0]+rdfurl;
					osapi.http.get({
						href: rdfurl,
						format: "string"
					}).execute(function(rdfdata) {
						callback(Bundle.createBundle(rdfdata.content));
					});
				});
		},
		createBundle: function(rdfstring) {
			var bundle = Object.create(Bundle);
			bundle._graph = rdf.rdfxml2graph(rdfstring);
			bundle._uri = bundle._graph.subject("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/role/terms/bundle").firstValue();
//			var sts = bundle._graph.find(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", {type: "uri", value: "http://purl.org/role/terms/bundle"});
	//		bundle._uri = sts[0].getSubject();
			return bundle;
		},
		getWidgetStoreUri: function() {
		},
		getWidgetXMLUri: function() {
			
		},
		getLabel: function() {
			return this._graph.findFirstValue(this._uri, "http://purl.org/dc/terms/title");
		},
		getDescription: function() {
			return this._graph.findFirstValue(this._uri, "http://purl.org/dc/terms/description");			
		},
		getWidgets: function() {
			var bundle = this;
			if (!this._widgets) {
				var sts = this._graph.find(this._uri, "http://purl.org/role/terms/toolConfiguration"); //Wrong class
				this._widgets = [];
				var extractWidget = function(wuri) {
					return {
						uri: wuri,
						label: bundle._graph.findFirstValue(wuri, "http://purl.org/dc/terms/title"),
						desc: bundle._graph.findFirstValue(wuri, "http://purl.org/dc/terms/description"),
						source: bundle._graph.findFirstValue(wuri, "http://purl.org/dc/terms/source")
					};
				};
				
				for (var i=0;i<sts.length;i++) {
					var desc = bundle._graph.findFirstValue(sts[i].getValue(), "http://purl.org/dc/terms/description");
					var wuri = bundle._graph.findFirstValue(sts[i].getValue(), "http://purl.org/role/terms/tool"); //Wrong property
					var w = extractWidget(wuri);
					if (desc) {
						w.pedagogicalDesc = desc;
					}
					this._widgets.push(w);
				}
			}
			return this._widgets;
		}
	};
	return Bundle;
});