define(["./rdfjson", "./Graph", "./Statement", "./terms", "./rdfparser"], function(rdfjson, Graph, Statement, terms, RDFParser) {

	var nss = {
			ical: "http://www.w3.org/2002/12/cal/ical#",
			role: "http://purl.org/role/terms/",
			dcterms: "http://purl.org/dc/terms/",
			rdfs: "http://www.w3.org/2000/01/rdf-schema#",
			rdf: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
			owl: "http://www.w3.org/2002/07/owl#", 
			vs: "http://www.w3.org/2003/06/sw-vocab-status/ns#", 
			foaf: "http://xmlns.com/foaf/0.1/", 
			wot: "http://xmlns.com/wot/0.1/", 
			dc: "http://purl.org/dc/elements/1.1/"
	};
	var nscounter = 0;
	var sp = "  ";
	var sp2 = "    ";	
	
	var string2xml = function(text){
	    if (window.ActiveXObject){
	      var doc=new ActiveXObject('Microsoft.XMLDOM');
	      doc.async='false';
	      doc.loadXML(text);
	    } else {
	      var parser=new DOMParser();
	      var doc=parser.parseFromString(text,'text/xml');
	    }
	    return doc;
	};

	return {
		string2xml: string2xml,
		xml2string: function(xml) {
		  if (window.ActiveXObject) {
		    return xml.xml;
		  } else {
		    return (new XMLSerializer()).serializeToString(xml);
		  }
		},
	
		/**
		 * Imports RDF/XML into a Graph
		 *  
		 * @param xmldocument this is the XML document or XML string from where the RDF will be parsed.
		 * @param {Graph} graph where all tripples will be added, if null a new graph will be created.
		 * @returns {Graph} where all found tripples have been added.
		 */
		rdfxml2graph: function(xml, graph) {
			if (rdfjson.isString(xml)) {
				xml = string2xml(xml);
			}
			
			var graph = graph || new Graph({});
			var RDFFormula = terms.RDFFormula;
			var store = new RDFFormula();
			store.add = function(s, p, o) {
				var subj, pred, obj = {};
				//Subject
				if (s instanceof terms.RDFBlankNode) {
					subj = s.toString()
					graph.registerBNode(subj);
				} else {
					subj = s.uri;
				}
				
				//Predicate
				if (p instanceof terms.RDFBlankNode) {
					pred = p.toString()
					graph.registerBNode(pred);
				} else {
					pred = p.uri;
				}
				
				//Object
				if (o instanceof terms.RDFLiteral) {
					obj.type = "literal";
					obj.value = o.value;
					if (o.lang) {
						obj.lang = o.lang;
					}
					if (o.datatype) {
						obj.datatype = o.datatype;
					}
				} else if (o instanceof terms.RDFSymbol) {
					obj.type = "uri";
					obj.value = o.uri;
				} else if (o instanceof terms.RDFBlankNode) {
					obj.value = o.toString();
					graph.registerBNode(obj.value);
					obj.type = "bnode";
				}
				graph.create(subj, pred, obj, true);
			};
			var parser = new RDFParser(store);
			parser.parse(xml, "", "");
			return graph;
		},
		rdfjson2rdfxml: function(graph) {
			var nsUsed = [], s, p, nsp, nskey, lname, o, props, objs, i, g = graph._graph || graph; //just in case a Graph is provided.
			var nsify = function(prop) {
				for (var ns in nss) {
					if (prop.indexOf(nss[ns]) === 0) {
						nsUsed.push(ns);
						return ns+":"+prop.substring(nss[ns].length);
					}
				}
				var slash = prop.lastIndexOf("/");
				var hash = prop.lastIndexOf("#");
				if (hash> slash) {
					slash = hash;
				}
				nscounter++;
				ns = "ns"+nscounter;
				nss[ns] = prop.substring(0,slash+1);
				nsUsed.push(ns);
				return ns+":"+prop.substring(slash+1); 
			};
			
			var strs = [];
			for (s in g) {
				if (g.hasOwnProperty(s)) {
					if (s.substr(0,2) === "_:") {
						strs.push(sp+'<rdf:Description rdf:nodeID="'+s+'">\n');				
					} else {
						strs.push(sp+'<rdf:Description rdf:about="'+s+'">\n');
					}
					props = g[s];
					for (p in props) {
						if (props.hasOwnProperty(p)) {
							objs = props[p];
							nsp = nsify(p);
							for (i=0;i<objs.length;i++) {
								o = objs[i];
								switch (o.type) {
									case "literal":
										if (o.language != null) {
											strs.push(sp2+'<'+nsp+' xml:lang="'+o.language+'">'+o.value+'</'+nsp+'>\n');									
										} else if (o.datatype != null) {
											strs.push(sp2+'<'+nsp+' rdf:datatype="'+o.datatype+'">'+o.value+'</'+nsp+'>\n');
										} else {
											strs.push(sp2+'<'+nsp+'>'+o.value+'</'+nsp+'>\n');
										}
									break;
									case "uri":
										strs.push(sp2+'<'+nsp+' rdf:resource="'+o.value+'"/>\n');
									break;
									case "blank":
										strs.push(sp2+'<'+nsp+' rdf:nodeID="'+o.value+'"/>\n');
									break;
								}
							}
						}
					}
					strs.push(sp+'</rdf:Description>\n');
				}
			}
			var initialStrs = ['<?xml version="1.0"?>\n<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"'];
			for (var j=0;j<nsUsed.length;j++) {
				initialStrs.push('\n\txmlns:'+nsUsed[j]+'="'+nss[nsUsed[j]]+'"');
			}
			initialStrs.push('>\n');
			strs.unshift(initialStrs.join(""));
			strs.push('</rdf:RDF>');
			return strs.join("");
		}
	};
});