(function(){var openapp = {};
this.openapp = openapp;
openapp.event = {};
var gadgets = typeof this.gadgets !== "undefined" ? this.gadgets : {};
this.gadgets = gadgets;
gadgets.openapp = gadgets.openapp || {};
var usePostMessage = typeof window !== "undefined" && typeof window.parent !== "undefined" && typeof window.postMessage !== "undefined" && typeof JSON !== "undefined" && typeof JSON.parse !== "undefined" && typeof JSON.stringify !== "undefined", usePubSub = !usePostMessage && typeof gadgets !== "undefined" && typeof gadgets.pubsub !== "undefined" && typeof gadgets.pubsub.subscribe !== "undefined" && typeof gadgets.pubsub.unsubscribe !== "undefined" && typeof gadgets.pubsub.publish !== "undefined", 
init = {postParentOnly:true}, ownData = null, doCallback = null, onMessage = null;
usePostMessage ? (onMessage = function(a) {
  if(typeof a.data === "string" && a.data.slice(0, 25) === '{"OpenApplicationEvent":{') {
    var b = JSON.parse(a.data).OpenApplicationEvent;
    if(b.event === "openapp" && b.welcome === true && a.source === window.parent) {
      for(var c in b.message) {
        b.message.hasOwnProperty(c) && (init[c] = b.message[c])
      }
    }else {
      b.source = a.source, b.origin = a.origin, b.toJSON = function() {
        var a = {}, b;
        for(b in this) {
          this.hasOwnProperty(b) && typeof this[b] !== "function" && b !== "source" && b !== "origin" && (a[b] = this[b])
        }
        return a
      }, typeof doCallback === "function" && doCallback(b, b.message) === true && window.parent.postMessage(JSON.stringify({OpenApplicationEvent:{event:"openapp", receipt:true}}), "*")
    }
  }
}, typeof window.attachEvent !== "undefined" ? window.attachEvent("onmessage", onMessage) : window.addEventListener("message", onMessage, false), typeof window.parent !== "undefined" && window.parent.postMessage(JSON.stringify({OpenApplicationEvent:{event:"openapp", hello:true}}), "*")) : usePubSub && (onMessage = function(a, b) {
  b.source = void 0;
  b.origin = void 0;
  b.sender = a;
  typeof doCallback === "function" && doCallback(b, b.message) === true && gadgets.pubsub.publish("openapp-recieve", true)
});
gadgets.openapp.RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
gadgets.openapp.connect = function(a) {
  doCallback = a;
  usePubSub && gadgets.pubsub.subscribe("openapp", onMessage)
};
gadgets.openapp.disconnect = function() {
  usePubSub && gadgets.pubsub.unsubscribe("openapp");
  doCallback = null
};
gadgets.openapp.publish = function(a, b) {
  a.event = a.event || "select";
  a.type = a.type || "namespaced-properties";
  a.sharing = a.sharing || "public";
  a.date = a.date || new Date;
  a.message = b || a.message;
  if(usePostMessage) {
    if(init.postParentOnly === false && ownData === null) {
      ownData = {sender:"unknown", viewer:"unknown"};
      if(typeof window.location !== "undefined" && typeof window.location.search === "string" && typeof window.unescape === "function") {
        var c = window.location.search.substring(1).split("&"), d, e = {};
        if(!(c.length == 1 && c[0] === "")) {
          for(var g = 0;g < c.length;g++) {
            d = c[g].split("="), d.length == 2 && (e[d[0]] = window.unescape(d[1]))
          }
        }
        if(typeof e.url === "string") {
          ownData.sender = e.url
        }
      }
      if(typeof opensocial !== "undefined" && typeof opensocial.newDataRequest === "function") {
        c = opensocial.newDataRequest();
        c.add(c.newFetchPersonRequest(opensocial.IdSpec.PersonId.VIEWER), "viewer");
        var f = this;
        c.send(function(c) {
          c = c.get("viewer").getData();
          if(typeof c === "object" && c !== null && typeof c.getId === "function" && (c = c.getId(), typeof c === "string")) {
            ownData.viewer = c
          }
          f.publish(a, b)
        });
        return
      }
    }
    if(ownData !== null) {
      if(typeof ownData.sender === "string") {
        a.sender = ownData.sender
      }
      if(typeof ownData.viewer === "string") {
        a.viewer = ownData.viewer
      }
    }
    c = JSON.stringify({OpenApplicationEvent:a});
    if(window.parent !== "undefined") {
      if(window.parent.postMessage(c, "*"), !init.postParentOnly) {
        d = window.parent.frames;
        for(e = 0;e < d.length;e++) {
          d[e].postMessage(c, "*")
        }
      }
    }else {
      window.postMessage(c, "*")
    }
  }else {
    usePubSub && gadgets.pubsub.publish("openapp", a)
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
openapp.io.makeRequest = function(a, b, c) {
  gadgets.io.makeRequest(a, function(d) {
    var e, g, f, h, i, j, k, m;
    if(document.getElementById("oauthPersonalize") === null) {
      e = document.createElement("div");
      g = document.createElement("input");
      f = document.createElement("input");
      h = document.createElement("div");
      i = document.createElement("input");
      j = document.createElement("div");
      k = document.createElement("span");
      m = document.createElement("input");
      e.id = "oauthPersonalize";
      g.id = "oauthPersonalizeButton";
      f.id = "oauthPersonalizeDenyButton";
      h.id = "oauthPersonalizeDone";
      i.id = "oauthPersonalizeDoneButton";
      j.id = "oauthPersonalizeComplete";
      k.id = "oauthPersonalizeMessage";
      m.id = "oauthPersonalizeHideButton";
      g.id = "oauthPersonalizeButton";
      e.style.display = "none";
      h.style.display = "none";
      j.style.display = "none";
      g.type = "button";
      f.type = "button";
      i.type = "button";
      m.type = "button";
      g.value = "Continue";
      f.value = "Ignore";
      i.value = "Done";
      m.value = "Hide";
      e.appendChild(document.createTextNode("In order to provide the full functionality of this tool, access to your personal data is being requested."));
      h.appendChild(document.createTextNode("If you have provided authorization and are still reading this, click the Done button."));
      var l = document.getElementById("openappDialog");
      l == null && (l = document.createElement("div"), document.body.firstChild != null ? document.body.insertBefore(l, document.body.firstChild) : document.body.appendChild(l));
      l.appendChild(e);
      l.appendChild(h);
      l.appendChild(j);
      e.appendChild(g);
      e.appendChild(f);
      h.appendChild(i);
      j.appendChild(k);
      j.appendChild(m);
      f.onclick = function() {
        e.style.display = "none"
      };
      m.onclick = function() {
        j.style.display = "none"
      }
    }
    d.oauthApprovalUrl ? (d = function(a) {
      function b() {
        h && (window.clearInterval(h), h = null);
        j && (j.close(), j = null);
        f();
        return false
      }
      function c() {
        if(!j || j.closed) {
          j = null, b()
        }
      }
      var d = a.destination, e = a.windowOptions, g = a.onOpen, f = a.onClose, j = null, h = null;
      return{createOpenerOnClick:function() {
        return function() {
          if(j = window.open(d, "_blank", e)) {
            h = window.setInterval(c, 100), g()
          }
          return false
        }
      }, createApprovedOnClick:function() {
        return b
      }}
    }({destination:d.oauthApprovalUrl, windowOptions:"width=450,height=500", onOpen:function() {
      document.getElementById("oauthPersonalize").style.display = "none";
      document.getElementById("oauthPersonalizeDone").style.display = "block"
    }, onClose:function() {
      document.getElementById("oauthPersonalizeDone").style.display = "none";
      document.getElementById("oauthPersonalizeComplete").style.display = "block";
      openapp.io.makeRequest(a, b, c)
    }}), document.getElementById("oauthPersonalizeButton").onclick = d.createOpenerOnClick(), document.getElementById("oauthPersonalizeDoneButton").onclick = d.createApprovedOnClick(), g = "Please wait.", document.all ? document.getElementById("oauthPersonalizeMessage").innerText = g : document.getElementById("oauthPersonalizeMessage").textContent = g, document.getElementById("oauthPersonalize").style.display = "block") : d.oauthError ? (g = "The authorization was not completed successfully. (" + 
    d.oauthError + ")", document.all ? document.getElementById("oauthPersonalizeMessage").innerText = g : document.getElementById("oauthPersonalizeMessage").textContent = g, document.getElementById("oauthPersonalizeComplete").style.display = "block") : (g = "You have now granted authorization. To revoke authorization, go to your Privacy settings.", document.all ? document.getElementById("oauthPersonalizeMessage").innerText = g : document.getElementById("oauthPersonalizeMessage").textContent = g, 
    b(d))
  }, c)
};
openapp.ns = {};
openapp.ns.rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
openapp.ns.rdfs = "http://www.w3.org/2000/01/rdf-schema#";
openapp.ns.dcterms = "http://purl.org/dc/terms/";
openapp.ns.foaf = "http://xmlns.com/foaf/0.1/";
openapp.ns.rest = "http://purl.org/openapp/";
openapp.ns.conserve = "http://purl.org/openapp/";
openapp.ns.openapp = "http://purl.org/openapp/";
openapp.ns.role = "http://purl.org/role/terms/";
openapp.ns.widget = "http://purl.org/role/widget/";
openapp.resource = {};
var linkexp = /<[^>]*>\s*(\s*;\s*[^\(\)<>@,;:"\/\[\]\?={} \t]+=(([^\(\)<>@,;:"\/\[\]\?={} \t]+)|("[^"]*")))*(,|\$)/g, paramexp = /[^\(\)<>@,;:"\/\[\]\?={} \t]+=(([^\(\)<>@,;:"\/\[\]\?={} \t]+)|("[^"]*"))/g;
function unquote(a) {
  return a.charAt(0) === '"' && a.charAt(a.length - 1) === '"' ? a.substring(1, a.length - 1) : a
}
function parseLinkHeader(a) {
  var b = (a + ",").match(linkexp), a = {}, c = {}, d = {}, e, g, f, h, i, j;
  for(e = 0;e < b.length;e++) {
    g = b[e].split(">");
    f = g[0].substring(1);
    h = g[1];
    g = {};
    g.href = f;
    f = h.match(paramexp);
    for(h = 0;h < f.length;h++) {
      i = f[h], i = i.split("="), j = i[0], g[j] = unquote(i[1])
    }
    g.rel !== void 0 && typeof g.anchor === "undefined" && (a[g.rel] = g);
    g.title !== void 0 && typeof g.anchor === "undefined" && (c[g.title] = g);
    f = d[g.anchor || ""] || {};
    h = f[g.rel || "http://purl.org/dc/terms/relation"] || [];
    h.push({type:"uri", value:g.href});
    f[g.rel || "http://purl.org/dc/terms/relation"] = h;
    d[g.anchor || ""] = f
  }
  b = {};
  b.rels = a;
  b.titles = c;
  b.rdf = d;
  return b
}
openapp.resource.makeRequest = function(a, b, c, d, e, g) {
  var f = openapp.io.createXMLHttpRequest(), h, i = 0;
  if(typeof d !== "undefined") {
    for(h in d) {
      d.hasOwnProperty(h) && i++
    }
    if(i > 0) {
      i = "";
      b.indexOf("?") !== -1 && (i = b.substring(b.indexOf("?")), b = b.substring(0, b.length - i.length));
      switch(b.substring(b.length - 1)) {
        case "/":
          b += ":";
          break;
        case ":":
          break;
        default:
          b += "/:"
      }
      for(h in d) {
        d.hasOwnProperty(h) && (b += h === openapp.ns.rdf + "predicate" ? ";predicate=" + encodeURIComponent(d[h]) : ";" + encodeURIComponent(h) + "=" + encodeURIComponent(d[h]))
      }
      b += i
    }
  }
  f.open(a, b, true);
  f.setRequestHeader("Accept", "application/json");
  e = e || "";
  if(e.length > 0 || a === "POST" || a === "PUT") {
    f.setRequestHeader("Content-Type", typeof g !== "undefined" ? g : "application/json")
  }
  c = c || function() {
  };
  f.onreadystatechange = function() {
    if(f.readyState === 4) {
      var a = {data:f.responseText, link:f.getResponseHeader("link") !== null ? parseLinkHeader(f.getResponseHeader("link")) : {}};
      if(f.getResponseHeader("location") !== null && f.getResponseHeader("location").length > 0) {
        a.uri = f.getResponseHeader("location")
      }else {
        if(f.getResponseHeader("content-base") !== null && f.getResponseHeader("content-base").length > 0) {
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
      a.subject = typeof f.responseText !== "undefined" ? a.data.hasOwnProperty("") ? a.data[""] : a.data[a.uri] || {} : {};
      c(a)
    }
  };
  f.send(e)
};
if(typeof openapp_forceXhr === "undefined" && typeof gadgets !== "undefined" && typeof gadgets.io !== "undefined" && typeof gadgets.io.makeRequest !== "undefined") {
  openapp.resource.makeRequest = function(a, b, c, d, e, g) {
    var f = {}, h, i = 0;
    if(typeof d !== "undefined") {
      for(h in d) {
        d.hasOwnProperty(h) && i++
      }
      if(i > 0) {
        i = "";
        b.indexOf("?") !== -1 && (i = b.substring(b.indexOf("?")), b = b.substring(0, b.length - i.length));
        switch(b.substring(b.length - 1)) {
          case "/":
            b += ":";
            break;
          case ":":
            break;
          default:
            b += "/:"
        }
        for(h in d) {
          d.hasOwnProperty(h) && (b += h === openapp.ns.rdf + "predicate" ? ";predicate=" + encodeURIComponent(d[h]) : ";" + encodeURIComponent(h) + "=" + encodeURIComponent(d[h]))
        }
        b += i
      }
    }
    f[gadgets.io.RequestParameters.GET_FULL_HEADERS] = true;
    f[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.TEXT;
    f[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType.OAUTH;
    f[gadgets.io.RequestParameters.OAUTH_SERVICE_NAME] = "openapp";
    f[gadgets.io.RequestParameters.OAUTH_USE_TOKEN] = "always";
    f[gadgets.io.RequestParameters.METHOD] = a;
    f[gadgets.io.RequestParameters.HEADERS] = f[gadgets.io.RequestParameters.HEADERS] || {};
    typeof e !== "undefined" && e !== null && (f[gadgets.io.RequestParameters.HEADERS]["Content-Type"] = typeof g !== "undefined" ? g : "application/json", f[gadgets.io.RequestParameters.POST_DATA] = e);
    f[gadgets.io.RequestParameters.HEADERS].Accept = "application/json";
    c = c || function() {
    };
    openapp.io.makeRequest(b, function(a) {
      var b = {data:a.data, link:typeof a.headers.link !== "undefined" ? parseLinkHeader(a.headers.link[0]) : {}};
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
      b.subject = typeof a.data !== "undefined" ? b.data.hasOwnProperty("") ? b.data[""] : b.data[b.uri] || {} : {};
      c(b)
    }, f)
  }
}
openapp.resource.get = function(a, b, c) {
  return openapp.resource.makeRequest("GET", a, b, c || {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":openapp.ns.conserve + "info"})
};
openapp.resource.post = function(a, b, c, d, e) {
  return openapp.resource.makeRequest("POST", a, b, c, d, e)
};
openapp.resource.put = function(a, b, c, d, e) {
  return openapp.resource.makeRequest("PUT", a, b, c, d, e)
};
openapp.resource.del = function(a, b, c) {
  return openapp.resource.makeRequest("DELETE", a, b, c)
};
openapp.resource.context = function(a) {
  return{sub:function(b) {
    var c = {};
    return{control:function(a, b) {
      c[a] = b;
      return this
    }, type:function(a) {
      return this.control(openapp.ns.rdf + "type", a)
    }, seeAlso:function(a) {
      return this.control(openapp.ns.rdfs + "seeAlso", a)
    }, list:function() {
      var d = [], e = a.subject[b], g, f, h, i, j, k;
      if(typeof e === "undefined") {
        return d
      }
      h = 0;
      a:for(;h < e.length;h++) {
        g = e[h];
        f = a.data[g.value];
        for(i in c) {
          if(c.hasOwnProperty(i)) {
            if(!f.hasOwnProperty(i)) {
              continue a
            }
            k = false;
            for(j = 0;j < f[i].length;j++) {
              if(f[i][j].value === c[i]) {
                k = true;
                break
              }
            }
            if(!k) {
              continue a
            }
          }
        }
        d.push({data:a.data, link:{}, uri:g.value, subject:f})
      }
      return d
    }, create:function(d) {
      if(!a.link.rdf.hasOwnProperty(b)) {
        throw"The context does not support the requested relation: " + b;
      }
      var e = a.uri;
      c[openapp.ns.rdf + "predicate"] = b;
      openapp.resource.post(e, function(a) {
        d(a)
      }, c)
    }}
  }, metadata:function() {
    return openapp.resource.context(a).content(openapp.ns.rest + "metadata")
  }, representation:function() {
    return openapp.resource.context(a).content(openapp.ns.rest + "representation")
  }, content:function(b) {
    return{get:function(c) {
      openapp.resource.get(a.uri, function(a) {
        c(a)
      }, {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":b})
    }, mediaType:function(c) {
      var d = null;
      return{string:function(a) {
        d = a;
        return this
      }, json:function(a) {
        d = JSON.stringify(a);
        return this
      }, put:function(e) {
        openapp.resource.put(a.uri, function(a) {
          typeof e === "function" && e(a)
        }, {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":b}, d, c)
      }}
    }, string:function(a) {
      return this.mediaType("text/plain").string(a)
    }, json:function(a) {
      return this.mediaType("application/json").json(a)
    }, graph:function() {
      var c = {}, d = "";
      return{subject:function(a) {
        d = a;
        return this
      }, resource:function(a, b) {
        c[d] = c[d] || {};
        c[d][a] = c[d][a] || [];
        c[d][a].push({value:b, type:"uri"});
        return this
      }, literal:function(a, b, f, h) {
        c[d] = c[d] || {};
        c[d][a] = c[d][a] || [];
        c[d][a].push({value:b, type:"literal", lang:f, datatype:h});
        return this
      }, put:function(d) {
        openapp.resource.put(a.uri, function(a) {
          typeof d === "function" && d(a)
        }, {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":b}, JSON.stringify(c))
      }}
    }}
  }, properties:function() {
    var b = {}, c;
    for(c in a.subject) {
      if(a.subject.hasOwnProperty(c)) {
        b[c] = a.subject[c][0].value
      }
    }
    return b
  }, string:function() {
    return typeof a.data === "string" ? a.data : gadgets.json.stringify(a.data)
  }, json:function() {
    return typeof a.data === "string" ? null : a.data
  }, followSeeAlso:function() {
    var b = a.subject[openapp.ns.rdfs + "seeAlso"], c = 0, d, e;
    if(typeof b !== "undefined") {
      b = b[0].value;
      for(e = 0;e < b.length && e < a.uri.length && b.charAt(e) === a.uri.charAt(e);e++) {
        b.charAt(e) === "/" && c++
      }
      for(d = c;e < b.length;e++) {
        b.charAt(e) === "/" && d++
      }
      return c < 3 || d > 4 ? this : openapp.resource.context({data:a.data, link:{}, uri:b, subject:a.data[b]})
    }else {
      return this
    }
  }}
};
openapp.resource.content = function(a) {
  return{properties:function() {
    return openapp.resource.context(a).properties()
  }, string:function() {
    return openapp.resource.context(a).string()
  }, json:function() {
    return openapp.resource.context(a).json()
  }}
};
openapp.oo = {};
openapp.oo.Resource = function(a, b) {
  this.uri = a;
  this.context = b
};
var OARP = openapp.oo.Resource.prototype;
OARP.getURI = function() {
  return this.uri
};
OARP._call = function(a) {
  var b = this;
  this.context == null ? this._deferred == null ? (this._deferred = [a], openapp.resource.get(this.uri, function(a) {
    b.context = a;
    for(a = 0;a < b._deferred.length;a++) {
      b._deferred[a].call(b)
    }
    delete b._deferred
  })) : this._deferred.push(a) : a.call(b)
};
OARP.refresh = function() {
  delete this.context
};
OARP.getSubResources = function(a) {
  this._call(function() {
    for(var b = a.type != null ? openapp.resource.context(this.context).sub(a.relation).type(a.type).list() : openapp.resource.context(this.context).sub(a.relation).list(), c = [], d = 0;d < b.length;d++) {
      var e = b[d].uri;
      if(a.followReference) {
        var g = this.context.data[e][openapp.ns.rdfs + "seeAlso"];
        if(g != null && g.length > 0) {
          e = g[0].value
        }
      }
      g = new openapp.oo.Resource(e);
      if(a.followReference == null && (g._referenceLoaded = true, e = this.context.data[e][openapp.ns.rdfs + "seeAlso"], e != null && e.length > 0)) {
        g._reference = e[0].value
      }
      if(a.onEach) {
        a.onEach(g)
      }
      a.onAll && c.push(g)
    }
    if(a.onAll) {
      a.onAll(c)
    }
  })
};
OARP.followReference = function(a) {
  this._referenceLoaded ? a(this._reference != null ? new openapp.oo.Resource(this._reference) : this) : this._call(function() {
    var b = this.context.data[this.uri][openapp.ns.rdfs + "seeAlso"];
    b != null && b.length > 0 ? a(new openapp.oo.Resource(b[0].value)) : a(this)
  })
};
OARP.getReference = function(a) {
  this._referenceLoaded ? a(this._reference) : this._call(function() {
    var b = this.context.subject[openapp.ns.rdfs + "seeAlso"];
    b != null && b.length > 0 ? a(this.context.subject[openapp.ns.rdfs + "seeAlso"][0].value) : a()
  })
};
OARP.getMetadata = function(a, b) {
  this._call(function() {
    openapp.resource.context(this.context).metadata().get(function(c) {
      switch(a || "properties") {
        case "properties":
          b(openapp.resource.context(c).properties());
          break;
        case "graph":
          b(openapp.resource.content(c).graph());
          break;
        case "rdfjson":
          b(openapp.resource.content(c).json())
      }
    })
  })
};
OARP.getRepresentation = function(a, b) {
  this._call(function() {
    openapp.resource.context(this.context).representation().get(function(c) {
      switch(a || "text/html") {
        case "properties":
          b(openapp.resource.context(c).properties());
          break;
        case "graph":
          b(openapp.resource.content(c).graph());
          break;
        case "rdfjson":
          b(openapp.resource.content(c).json());
          break;
        case "text/html":
          b(openapp.resource.content(c).string())
      }
    })
  })
};
OARP.setMetadata = function(a, b, c) {
  var d = {};
  switch(b || "properties") {
    case "properties":
      var b = {}, e;
      for(e in a) {
        b[e] = [{type:"literal", value:a[e]}]
      }
      d[this.context.uri] = b;
      break;
    case "rdfjson":
      d = a;
      break;
    case "graph":
      graph.put(c);
      return
  }
  openapp.resource.put(this.context.uri, c, {"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate":openapp.ns.rest + "metadata"}, JSON.stringify(d))
};
OARP.setRepresentation = function(a, b, c) {
  this._call(function() {
    var d = openapp.resource.context(this.context).representation().mediaType(b);
    typeof a === "string" ? d.string(a).put(c) : d.json(a).put(c)
  })
};
OARP.create = function(a) {
  this._call(function() {
    var b = openapp.resource.context(this.context).sub(a.relation || openapp.ns.role + "data");
    a.referenceTo != null && (b = b.seeAlso(a.referenceTo));
    a.type != null && (b = b.type(a.type));
    b.create(function(b) {
      var d = new openapp.oo.Resource(b.uri, b);
      a.metadata ? d.setMetadata(a.metadata, a.format, function() {
        a.representation ? d.setRepresentation(a.representation, a.medieType || "application/json", function() {
          a.callback(d)
        }) : a.callback(d)
      }) : a.representation ? d.setRepresentation(a.representation, a.medieType || "application/json", function() {
        a.callback(d)
      }) : a.callback(d)
    })
  })
};
OARP.del = function(a) {
  openapp.resource.del(this.uri, a)
};
openapp.param = {};
var parseQueryParams = function(a) {
  var b, c, d = {};
  if(a.indexOf("?") < 0) {
    return{}
  }
  a = a.substr(a.indexOf("?") + 1).split("&");
  if(!(a.length == 1 && a[0] === "")) {
    for(c = 0;c < a.length;c++) {
      b = a[c].split("="), b.length == 2 && (d[b[0]] = window.unescape(b[1]))
    }
  }
  return d
}, parseOpenAppParams = function(a) {
  var b = {}, c = {}, d, e;
  for(d in a) {
    a.hasOwnProperty(d) && d.substring(0, 11) === "openapp.ns." && (b[d.substr(11)] = a[d])
  }
  for(d in a) {
    a.hasOwnProperty(d) && (e = d.split("."), e.length === 3 && e[0] === "openapp" && b.hasOwnProperty(e[1]) && (c[b[e[1]] + e[2]] = a[d]))
  }
  return c
}, _openAppParams = parseOpenAppParams(parseQueryParams(parseQueryParams(window.location.href).url || ""));
openapp.param.get = function(a) {
  return _openAppParams[a]
};
openapp.param.space = function() {
  return openapp.param.get("http://purl.org/role/terms/space")
};
openapp.param.user = function() {
  return openapp.param.get("http://purl.org/role/terms/user")
};
})();
