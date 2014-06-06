define(function(){return {
	createXMLHttpRequest: function() {
		var xmlhttp;
		if (window.XMLHttpRequest) {
			// code for IE7+, Firefox, Chrome, Opera, Safari
			xmlhttp = new XMLHttpRequest();
		} else {
			// code for IE6, IE5
			xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		}
		return xmlhttp;
	},
	
	buildParamString: function() {
		var paramString = "";
		for (var i = 0; i < arguments.length - 1; i++) {
			paramString = paramString + arguments[i] + "&";
		}
		paramString = paramString + arguments[arguments.length - 1];
		return paramString;
	},
	
	encodeParam: function(key, value) {
		var param = key + "=" + encodeURIComponent(value);
		return param;
	},
	
	loadXMLString: function(txt) {
		if (window.ActiveXObject) {
			// Internet Explorer
			xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
			xmlDoc.async = false;
			xmlDoc.loadXML(txt);
		}else if (document.implementation.createDocument) {
			parser = new DOMParser();
			xmlDoc = parser.parseFromString(txt, "text/xml");
		}
		return xmlDoc;
	}
};});