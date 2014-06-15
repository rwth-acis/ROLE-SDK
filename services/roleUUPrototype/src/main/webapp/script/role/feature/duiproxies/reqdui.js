/**
 * send http request to dui module
 */
define(["./duihttptools", "../../model/space"], function(duiHttpTools){

	var requestDui = {
			
		makeQuery: function(param){
			var query = "";
			var relCount = 0;
			for (rel in param) {
				if (param.hasOwnProperty(rel))
					relCount++;
			}
			if (relCount > 0){
				for (rel in param)
					if (param.hasOwnProperty(rel)){
						if (rel === openapp.ns.rdf+"predicate")
							query += ";predicate=" + encodeURIComponent(param[rel]);
						else
							query += ";" + encodeURIComponent(rel) + "=" + encodeURIComponent(param[rel]);
					}
			}
			return query;
		},
		
		get: function(action, query, callback){
			var uri = "http://" + document.location.host + "/dui/:";
			uri += this.makeQuery(query);
			var xmlhttp = duiHttpTools.createXMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				//console.log("response current device info" + xmlhttp.readyState + ", " + xmlhttp.status);
				if (xmlhttp.readyState == 4) {
					callback(xmlhttp);
				}
			};
			xmlhttp.open("GET", uri, true);
			xmlhttp.setRequestHeader("action", action);
			xmlhttp.send();
		},
		
		put: function(action, data, callback){
			var uri = "http://" + document.location.host + "/dui/:";
			uri += this.makeQuery(data);
			var xmlhttp = duiHttpTools.createXMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				//console.log("response current device info" + xmlhttp.readyState + ", " + xmlhttp.status);
				if (xmlhttp.readyState == 4) {
					callback(xmlhttp);
				}
			};
			xmlhttp.open("PUT", uri, true);
			xmlhttp.setRequestHeader("action", action);
			xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			xmlhttp.send("");
		},
		
		post: function(action, data, callback){
			var uri = "http://" + document.location.host + "/dui/:";
			uri += this.makeQuery(data);
			var xmlhttp = duiHttpTools.createXMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				//console.log("response current device info" + xmlhttp.readyState + ", " + xmlhttp.status);
				if (xmlhttp.readyState == 4) {
					callback(xmlhttp);
				}
			};
			xmlhttp.open("POST", uri, true);
			xmlhttp.setRequestHeader("action", action);
			xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			xmlhttp.send("");
		},
		
		del: function(action, query, callback){
			var uri = "http://" + document.location.host + "/dui/:";
			uri += this.makeQuery(query);
			var xmlhttp = duiHttpTools.createXMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				//console.log("response current device info" + xmlhttp.readyState + ", " + xmlhttp.status);
				if (xmlhttp.readyState == 4) {
					callback(xmlhttp);
				}
			};
			xmlhttp.open("DELETE", uri, true);
			xmlhttp.setRequestHeader("action", action);
			xmlhttp.send();
		}
	};
	return requestDui;
});