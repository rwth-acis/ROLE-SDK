(function(){'use strict';var openapp = {};
this.openapp = openapp;
openapp.event = {};
var gadgets = typeof this.gadgets !== "undefined" ? this.gadgets : {};
this.gadgets = gadgets;
gadgets.openapp = gadgets.openapp || {};
var usePostMessage = typeof window !== "undefined" && typeof window.parent !== "undefined" && typeof window.postMessage !== "undefined" && typeof JSON !== "undefined" && typeof JSON.parse !== "undefined" && typeof JSON.stringify !== "undefined", usePubSub = !usePostMessage && typeof gadgets !== "undefined" && typeof gadgets.pubsub !== "undefined" && typeof gadgets.pubsub.subscribe !== "undefined" && typeof gadgets.pubsub.unsubscribe !== "undefined" && typeof gadgets.pubsub.publish !== "undefined", 
init = {postParentOnly:!1}, ownData = null, doCallback = null, onMessage = null;
usePostMessage ? (onMessage = function(b) {
  if(typeof b.data === "string" && b.data.slice(0, 25) === '{"OpenApplicationEvent":{') {
    var d = JSON.parse(b.data).OpenApplicationEvent;
    if(d.event === "openapp" && d.welcome === !0 && b.source === window.parent) {
      for(var c in d.message) {
        d.message.hasOwnProperty(c) && (init[c] = d.message[c])
      }
    }else {
      d.source = b.source, d.origin = b.origin, d.toJSON = function() {
        var b = {}, a;
        for(a in this) {
          this.hasOwnProperty(a) && typeof this[a] !== "function" && a !== "source" && a !== "origin" && (b[a] = this[a])
        }
        return b
      }, typeof doCallback === "function" && doCallback(d, d.message) === !0 && window.parent.postMessage(JSON.stringify({OpenApplicationEvent:{event:"openapp", receipt:!0}}), "*")
    }
  }
}, typeof window.attachEvent !== "undefined" ? window.attachEvent("onmessage", onMessage) : window.addEventListener("message", onMessage, !1), typeof window.parent !== "undefined" && window.parent.postMessage(JSON.stringify({OpenApplicationEvent:{event:"openapp", hello:!0}}), "*")) : usePubSub && (onMessage = function(b, d) {
  d.source = void 0;
  d.origin = void 0;
  d.sender = b;
  typeof doCallback === "function" && doCallback(d, d.message) === !0 && gadgets.pubsub.publish("openapp-recieve", !0)
});
gadgets.openapp.RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
gadgets.openapp.connect = function(b) {
  doCallback = b;
  usePubSub && gadgets.pubsub.subscribe("openapp", onMessage)
};
gadgets.openapp.disconnect = function() {
  usePubSub && gadgets.pubsub.unsubscribe("openapp");
  doCallback = null
};
gadgets.openapp.publish = function(b, d) {
  b.event = b.event || "select";
  b.sharing = b.sharing || "public";
  b.date = b.date || new Date;
  b.message = d || b.message;
  if(usePostMessage) {
    if(init.postParentOnly === !1 && ownData === null) {
      ownData = {sender:"unknown", viewer:"unknown"};
      if(typeof window.location !== "undefined" && typeof window.location.search === "string" && typeof window.unescape === "function") {
        var c = window.location.search.substring(1).split("&"), e, a = {};
        if(!(c.length == 1 && c[0] === "")) {
          for(var h = 0;h < c.length;h++) {
            e = c[h].split("="), e.length == 2 && (a[e[0]] = window.unescape(e[1]))
          }
        }
        if(typeof a.url === "string") {
          ownData.sender = a.url
        }
      }
      if(typeof opensocial !== "undefined" && typeof opensocial.newDataRequest === "function") {
        c = opensocial.newDataRequest();
        c.add(c.newFetchPersonRequest(opensocial.IdSpec.PersonId.VIEWER), "viewer");
        var f = this;
        c.send(function(a) {
          a = a.get("viewer").getData();
          if(typeof a === "object" && a !== null && typeof a.getId === "function" && (a = a.getId(), typeof a === "string")) {
            ownData.viewer = a
          }
          f.publish(b, d)
        });
        return
      }
    }
    if(ownData !== null) {
      if(typeof ownData.sender === "string") {
        b.sender = ownData.sender
      }
      if(typeof ownData.viewer === "string") {
        b.viewer = ownData.viewer
      }
    }
    c = JSON.stringify({OpenApplicationEvent:b});
    if(window.parent !== "undefined") {
      if(window.parent.postMessage(c, "*"), !init.postParentOnly) {
        e = window.parent.frames;
        for(a = 0;a < e.length;a++) {
          e[a].postMessage(c, "*")
        }
      }
    }else {
      window.postMessage(c, "*")
    }
  }else {
    usePubSub && gadgets.pubsub.publish("openapp", b)
  }
};
openapp.io = {};
openapp.io.createXMLHttpRequest = function() {
  if(typeof XMLHttpRequest !== "undefined") {
    return new XMLHttpRequest
  }else {
    if(typeof ActiveXObject !== "undefined") {
      return new ActiveXObject("Microsoft.XMLHTTP")
    }else {
      throw{name:"XMLHttpRequestError", message:"XMLHttpRequest not supported"};
    }
  }
};
openapp.io.makeRequest = function(b, d, c) {
  gadgets.io.makeRequest(b, function(e) {
    var a, h, f, g, i, j, l, k;
    if(document.getElementById("oauthPersonalize") === null) {
      a = document.createElement("div"), h = document.createElement("input"), f = document.createElement("input"), g = document.createElement("div"), i = document.createElement("input"), j = document.createElement("div"), l = document.createElement("span"), k = document.createElement("input"), a.id = "oauthPersonalize", h.id = "oauthPersonalizeButton", f.id = "oauthPersonalizeDenyButton", g.id = "oauthPersonalizeDone", i.id = "oauthPersonalizeDoneButton", j.id = "oauthPersonalizeComplete", l.id = 
      "oauthPersonalizeMessage", k.id = "oauthPersonalizeHideButton", h.id = "oauthPersonalizeButton", a.style.display = "none", g.style.display = "none", j.style.display = "none", h.type = "button", f.type = "button", i.type = "button", k.type = "button", h.value = "Continue", f.value = "Ignore", i.value = "Done", k.value = "Hide", a.appendChild(document.createTextNode("In order to provide the full functionality of this tool, access to your personal data is being requested.")), g.appendChild(document.createTextNode("If you have provided authorization and are still reading this, click the Done button.")), 
      document.body.insertBefore(a, document.body.firstChild), document.body.insertBefore(g, document.body.firstChild), document.body.insertBefore(j, document.body.firstChild), a.appendChild(h), a.appendChild(f), g.appendChild(i), j.appendChild(l), j.appendChild(k), f.onclick = function() {
        a.style.display = "none"
      }, k.onclick = function() {
        j.style.display = "none"
      }
    }
    e.oauthApprovalUrl ? (e = function(a) {
      function b() {
        g && (window.clearInterval(g), g = null);
        h && (h.close(), h = null);
        j();
        return!1
      }
      function c() {
        if(!h || h.closed) {
          h = null, b()
        }
      }
      var e = a.destination, d = a.windowOptions, f = a.onOpen, j = a.onClose, h = null, g = null;
      return{createOpenerOnClick:function() {
        return function() {
          if(h = window.open(e, "_blank", d)) {
            g = window.setInterval(c, 100), f()
          }
          return!1
        }
      }, createApprovedOnClick:function() {
        return b
      }}
    }({destination:e.oauthApprovalUrl, windowOptions:"width=450,height=500", onOpen:function() {
      document.getElementById("oauthPersonalize").style.display = "none";
      document.getElementById("oauthPersonalizeDone").style.display = "block"
    }, onClose:function() {
      document.getElementById("oauthPersonalizeDone").style.display = "none";
      document.getElementById("oauthPersonalizeComplete").style.display = "block";
      openapp.io.makeRequest(b, d, c)
    }}), document.getElementById("oauthPersonalizeButton").onclick = e.createOpenerOnClick(), document.getElementById("oauthPersonalizeDoneButton").onclick = e.createApprovedOnClick(), document.getElementById("oauthPersonalizeMessage").innerText = "Please wait.", document.getElementById("oauthPersonalize").style.display = "block") : e.oauthError ? (document.getElementById("oauthPersonalizeMessage").innerText = "The authorization was not completed successfully. (" + e.oauthError + ")", document.getElementById("oauthPersonalizeComplete").style.display = 
    "block") : (document.getElementById("oauthPersonalizeMessage").innerText = "You have now granted authorization. To revoke authorization, go to your Privacy settings.", d(e))
  }, c)
};
openapp.ns = {};
openapp.ns.rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
openapp.ns.rdfs = "http://www.w3.org/2000/01/rdf-schema#";
openapp.ns.dcterms = "http://purl.org/dc/terms/";
openapp.ns.foaf = "http://xmlns.com/foaf/0.1/";
openapp.ns.rest = "http://kmr.csc.kth.se/rdf/rest/";
openapp.ns.role = "http://www.role-project.eu/rdf/";
openapp.resource = {};
var linkexp = /<[^>]*>\s*(\s*;\s*[^\(\)<>@,;:"\/\[\]\?={} \t]+=(([^\(\)<>@,;:"\/\[\]\?={} \t]+)|("[^"]*")))*(,|\$)/g, paramexp = /[^\(\)<>@,;:"\/\[\]\?={} \t]+=(([^\(\)<>@,;:"\/\[\]\?={} \t]+)|("[^"]*"))/g;
function unquote(b) {
  return b.charAt(0) === '"' && b.charAt(b.length - 1) === '"' ? b.substring(1, b.length - 1) : b
}
function parseLinkHeader(b) {
  var d = b.match(linkexp), b = {}, c = {}, e, a, h, f, g, i;
  for(e = 0;e < d.length;e++) {
    a = d[e].split(">");
    h = a[0].substring(1);
    f = a[1];
    a = {};
    a.href = h;
    h = f.match(paramexp);
    for(f = 0;f < h.length;f++) {
      g = h[f], g = g.split("="), i = g[0], a[i] = unquote(g[1])
    }
    a.rel !== void 0 && (b[a.rel] = a);
    a.title !== void 0 && (c[a.title] = a)
  }
  d = {};
  d.rels = b;
  d.titles = c;
  return d
}
openapp.resource.makeRequest = function(b, d, c, e, a, h) {
  var f = openapp.io.createXMLHttpRequest(), g = "", i;
  f.open(b, d, !0);
  f.setRequestHeader("Accept", "application/json");
  if(typeof e !== "undefined") {
    for(i in e) {
      e.hasOwnProperty(i) && (g.length > 0 && (g += ", "), g += "<" + e[i] + '>; rel="' + i + '"')
    }
  }
  a = a || "";
  if(a.length > 0 || b === "POST" || b === "PUT") {
    f.setRequestHeader("Content-Type", typeof h !== "undefined" ? h : "application/json")
  }
  g.length > 0 && f.setRequestHeader("Link", g);
  c = c || function() {
  };
  f.onreadystatechange = function() {
    if(f.readyState === 4) {
      var a = {data:f.responseText, link:f.getResponseHeader("link") !== null ? parseLinkHeader(f.getResponseHeader("link")).rels : {}};
      if(f.getResponseHeader("location") !== null) {
        a.uri = f.getResponseHeader("location")
      }else {
        if(f.getResponseHeader("content-base") !== null) {
          a.uri = f.getResponseHeader("content-base")
        }else {
          if(a.link.hasOwnProperty("http://purl.org/dc/terms/subject")) {
            a.uri = a.link["http://purl.org/dc/terms/subject"].href
          }
        }
      }
      if(f.getResponseHeader("content-location") !== null) {
        a.contentUri = f.getResponseHeader("content-location")
      }
      if(f.getResponseHeader("content-type") !== null && f.getResponseHeader("content-type").split(";")[0] === "application/json") {
        a.data = JSON.parse(a.data)
      }
      if(typeof f.responseText !== "undefined") {
        a.subject = a.data.hasOwnProperty("") ? a.data[""] : a.data[a.uri]
      }
      c(a)
    }
  };
  f.send(a)
};
if(typeof gadgets !== "undefined" && typeof gadgets.io !== "undefined" && typeof gadgets.io.makeRequest !== "undefined") {
  openapp.resource.makeRequest = function(b, d, c, e, a, h) {
    var f = {}, g = "", i;
    if(typeof e !== "undefined") {
      for(i in e) {
        e.hasOwnProperty(i) && (g.length > 0 && (g += ", "), g += "<" + e[i] + '>; rel="' + i + '"')
      }
    }
    f[gadgets.io.RequestParameters.GET_FULL_HEADERS] = !0;
    f[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.TEXT;
    f[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType.OAUTH;
    f[gadgets.io.RequestParameters.OAUTH_SERVICE_NAME] = "openapp";
    f[gadgets.io.RequestParameters.OAUTH_USE_TOKEN] = "always";
    f[gadgets.io.RequestParameters.METHOD] = b;
    typeof a !== "undefined" && a !== null && (f[gadgets.io.RequestParameters.HEADERS] = f[gadgets.io.RequestParameters.HEADERS] || {}, f[gadgets.io.RequestParameters.HEADERS]["content-type"] = typeof h !== "undefined" ? h : "application/json", f[gadgets.io.RequestParameters.POST_DATA] = a);
    if(g.length > 0) {
      f[gadgets.io.RequestParameters.HEADERS] = f[gadgets.io.RequestParameters.HEADERS] || {}, f[gadgets.io.RequestParameters.HEADERS].link = g
    }
    c = c || function() {
    };
    openapp.io.makeRequest(d, function(a) {
      var b = {data:a.data, link:typeof a.headers.link !== "undefined" ? parseLinkHeader(a.headers.link[0]).rels : {}};
      if(a.headers.hasOwnProperty("location")) {
        b.uri = a.headers.location[0]
      }else {
        if(a.headers.hasOwnProperty("content-base")) {
          b.uri = a.headers["content-base"][0]
        }else {
          if(b.link.hasOwnProperty("http://purl.org/dc/terms/subject")) {
            b.uri = b.link["http://purl.org/dc/terms/subject"].href
          }
        }
      }
      if(a.headers.hasOwnProperty("content-location")) {
        b.contentUri = a.headers["content-location"][0]
      }
      if(a.headers.hasOwnProperty("content-type") && a.headers["content-type"][0].split(";")[0] === "application/json") {
        b.data = gadgets.json.parse(b.data)
      }
      if(typeof a.data !== "undefined") {
        b.subject = b.data.hasOwnProperty("") ? b.data[""] : b.data[b.uri]
      }
      c(b)
    }, f)
  }
}
openapp.resource.get = function(b, d, c) {
  return openapp.resource.makeRequest("GET", b, d, c || {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":openapp.ns.rest + "info"})
};
openapp.resource.post = function(b, d, c, e, a) {
  return openapp.resource.makeRequest("POST", b, d, c, e, a)
};
openapp.resource.put = function(b, d, c, e, a) {
  return openapp.resource.makeRequest("PUT", b, d, c, e, a)
};
openapp.resource.del = function(b, d, c) {
  return openapp.resource.makeRequest("DELETE", b, d, c)
};
openapp.resource.context = function(b) {
  return{sub:function(d) {
    var c = {};
    return{control:function(b, a) {
      c[b] = a;
      return this
    }, type:function(b) {
      return this.control(openapp.ns.rdf + "type", b)
    }, seeAlso:function(b) {
      return this.control(openapp.ns.rdfs + "seeAlso", b)
    }, list:function() {
      var e = [], a = b.subject[d], h, f, g, i;
      if(typeof a === "undefined") {
        return e
      }
      g = 0;
      a:for(;g < a.length;g++) {
        h = a[g];
        f = b.data[h.value];
        for(i in c) {
          if(c.hasOwnProperty(i) && (!f.hasOwnProperty(i) || f[i][0].value !== c[i])) {
            continue a
          }
        }
        e.push({data:b.data, link:{}, uri:h.value, subject:f})
      }
      return e
    }, create:function(e) {
      if(!b.link.hasOwnProperty(d)) {
        throw"The context does not support the requested relation: " + d;
      }
      openapp.resource.post(b.link[d].href, function(a) {
        e(a)
      }, c)
    }}
  }, metadata:function() {
    return openapp.resource.context(b).content(openapp.ns.rest + "metadata")
  }, representation:function() {
    return openapp.resource.context(b).content(openapp.ns.rest + "representation")
  }, content:function(d) {
    return{get:function(c) {
      openapp.resource.get(b.uri, function(b) {
        c(b)
      }, {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":d})
    }, mediaType:function(c) {
      var e = null;
      return{string:function(a) {
        e = a;
        return this
      }, json:function(a) {
        e = JSON.stringify(a);
        return this
      }, put:function(a) {
        openapp.resource.put(b.uri, function(b) {
          typeof a === "function" && a(b)
        }, {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":d}, e, c)
      }}
    }, string:function(b) {
      return this.mediaType("text/plain").string(b)
    }, json:function(b) {
      return this.mediaType("application/json").json(b)
    }, graph:function() {
      var c = {}, e = "";
      return{subject:function(a) {
        e = a;
        return this
      }, resource:function(a, b) {
        c[e] = c[e] || {};
        c[e][a] = c[e][a] || [];
        c[e][a].push({value:b, type:"uri"});
        return this
      }, literal:function(a, b, d, g) {
        c[e] = c[e] || {};
        c[e][a] = c[e][a] || [];
        c[e][a].push({value:b, type:"literal", lang:d, datatype:g});
        return this
      }, put:function(a) {
        openapp.resource.put(b.uri, function(b) {
          typeof a === "function" && a(b)
        }, {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":d}, JSON.stringify(c))
      }}
    }}
  }, properties:function() {
    var d = {}, c;
    for(c in b.subject) {
      if(b.subject.hasOwnProperty(c)) {
        d[c] = b.subject[c][0].value
      }
    }
    return d
  }, string:function() {
    return typeof b.data === "string" ? b.data : gadgets.json.stringify(b.data)
  }, json:function() {
    return typeof b.data === "string" ? null : b.data
  }, followSeeAlso:function() {
    var d = b.subject[openapp.ns.rdfs + "seeAlso"], c = 0, e, a;
    if(typeof d !== "undefined") {
      d = d[0].value;
      for(a = 0;a < d.length && a < b.uri.length && d.charAt(a) === b.uri.charAt(a);a++) {
        d.charAt(a) === "/" && c++
      }
      for(e = c;a < d.length;a++) {
        d.charAt(a) === "/" && e++
      }
      return c < 3 || e > 4 ? this : openapp.resource.context({data:b.data, link:{}, uri:d, subject:b.data[d]})
    }else {
      return this
    }
  }}
};
})();