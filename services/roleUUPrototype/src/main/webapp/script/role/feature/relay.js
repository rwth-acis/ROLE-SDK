define(
        ["jquery", "xmpp/xmpp", "domReady", "require", "xmpp/iwc", "xmpp/strophejs-plugins/jingle/strophe.jingle",
          "xmpp/strophejs-plugins/pubsub/strophe.pubsub", "xmpp/strophejs-plugins/pubsub-relay/strophe.pubsubrelay"],
        function($, xmpp, domReady, require){
  var xmppRelay = {
    relayCheck: null,
    counter:0, //TODO only for status widget
    relayConnection: null,
    isRelay: false,
    xmppClient: null,
    pubsubservice: null,
    userpubsubnode: null,
    state: "disconnected",
    isConnected: false,
    ice_config: {iceServers: [{url: 'stun:stun.schlund.de'},{url: 'stun:stun.l.google.com:19302'},{url: 'stun:stun1.l.google.com:19302'},{url: 'stun:stun2.l.google.com:19302'},{url: 'stun:stun3.l.google.com:19302'},{url: 'stun:stun4.l.google.com:19302'},{url: 'stun:stun.ideasip.com'},{url: 'stun:stun.iptel.org'},{url: 'stun:stun.rixtelecom.se'},{url: 'stun:stunserver.org'},{url: 'stun:stun.softjoys.com'},{url: 'stun:stun.voiparound.com'},{url: 'stun:stun.voipbuster.com'},{url: 'stun:stun.voipstunt.com'}]},
    
    connectPubSub: function(pubsubservice, userpubsubnode, connection){
      this.xmppClient = connection;
      this.pubsubservice = pubsubservice;
      this.userpubsubnode = userpubsubnode;
      this.xmppClient.addHandler(this.onPubSub, null, "message", null, null, null, null);
      
      this.xmppClient.pubsub.retrieveSubscriptions(pubsubservice, userpubsubnode, function(result){
        if(result.subscriptions.length ){
          xmppRelay.checkRelay();
        }else{
          xmppRelay.createRelay();
        }
      });
    },

    /**
    * This function takes care of creating a relay, connecting the loopback to it and propagating the newly created relay over XMPP
    */
    createRelay: function(){
      if(xmppRelay.state == "connecting" || xmppRelay.state=="connected") {
        return;
      }
      xmppRelay.isRelay=true;

      if(Strophe.relay === undefined ) {
        clientConnection = new Strophe.Connection("lo://client");
        clientConnection.connect("client@relay", "empty", function(){});
        Strophe.relay = new Strophe.PubSub();
        serverConnection = new Strophe.Connection("lo://server");
        serverConnection.connect(xmpp.pubsubservice, "empty", function(){});

        clientConnection._proto.send = function(msg){
        setTimeout(function(){serverConnection._proto._onMessage(msg);});
       };
       serverConnection._proto.send = function(msg){
        setTimeout(function(){clientConnection._proto._onMessage(msg);});
       };
       
       xmppRelay.relayConnection = clientConnection;
       xmppRelay.setupIWC();
       
      var create = $build("pubsub-relay", {xmlns:"http://dbis.rwth-aachen.de/~bavendiek/da/xsd/Relay"})
        .c('relay-create',{owner:xmppRelay.xmppClient.jid});
      xmppRelay.xmppClient.pubsub.publishItem(xmppRelay.pubsubservice, xmppRelay.userpubsubnode, create.tree(), function(ev){return true;});
      xmppRelay.updateState("CONNECTED"); //TODO This is only for a widget test
  }else{
    console.log("Strophe.Relay is not undefined");
  }
    },

    onPubSub: function(stanza){
      if(!$(stanza).find('delay[xmlns="urn:xmpp:delay"]').length && $(stanza).find('pubsub-relay[xmlns="http://dbis.rwth-aachen.de/~bavendiek/da/xsd/Relay"]').length){
        if($(stanza).find('pubsub-relay>relay-check').length){
          if(xmppRelay.isRelay){
            var relay = $build("pubsub-relay", {xmlns:"http://dbis.rwth-aachen.de/~bavendiek/da/xsd/Relay"})
              .c('relay',{owner:xmppRelay.xmppClient.jid});
           xmppRelay.xmppClient.pubsub.publishItem(xmppRelay.pubsubservice, xmppRelay.userpubsubnode, relay.tree(), function(ev){return true;});
          }
        } else if(!xmppRelay.isRelay && $(stanza).find('pubsub-relay>relay-create').length){
          window.clearTimeout(xmppRelay.relayCheck);
          xmppRelay.connectToRelay($(stanza).find('pubsub-relay>relay-create').attr('owner'));
        } else if(!xmppRelay.isRelay && $(stanza).find('pubsub-relay>relay').length){
          window.clearTimeout(xmppRelay.relayCheck);
          xmppRelay.connectToRelay($(stanza).find('pubsub-relay>relay').attr('owner'));
        }
      }
      return true;
    },

    checkRelay: function(){
      var check = $build("pubsub-relay", {xmlns:"http://dbis.rwth-aachen.de/~bavendiek/da/xsd/Relay"})
        .c('relay-check',{});
      xmppRelay.xmppClient.pubsub.publishItem(xmppRelay.pubsubservice, xmppRelay.userpubsubnode, check.tree(), function(ev){return true;});
      xmppRelay.relayCheck = window.setTimeout(xmppRelay.createRelay, 5000); //Set back to 5 or 1 0000
    },

    connectToRelay: function(owner){
      if(xmppRelay.state == "disconnected" ){
        xmppRelay.state = "connecting";
        xmppRelay.xmppClient.jingle.initiate(owner, xmppRelay.xmppClient.jid, [{name:"test", constraints:{reliable:true}}]);
        xmppRelay.updateState("CONNECTING");//TODO This is only for a widget test
      }
    },

    onCallIncoming: function (event, sid) {
      var sess = xmppRelay.xmppClient.jingle.sessions[sid];
      sess.sendAnswer();
      sess.accept();
    },

    onDataChannelOpen: function  (event, connection, sid, channel) {
      var options = {channel:channel};
      var conn = new Strophe.Connection("p2p", options);
      globalConn = conn;
      conn.connect(xmppRelay.xmppClient.jingle.sessions[sid].peerjid, "empty", function (status) {
        if (status === Strophe.Status.CONNECTED) {
            xmppRelay.state = "connected";
            xmppRelay.isConnected = true;
            if(xmppRelay.xmppClient.jingle.sessions[sid].isInitiator) {
              xmppRelay.relayConnection = conn;
              xmppRelay.setupIWC();
            }else {
            /*
              //Enable to see input/output of relay
              conn.rawInput = console.log;
              conn.rawOutput = console.log;
            */  
              xmppRelay.counter++;
              xmppRelay.updateUsers();
            }
            xmppRelay.updateState("CONNECTED"); //TODO This is only for a widget test
        } else if (status === Strophe.Status.DISCONNECTED) {
            connection.jingle.terminateByJid(this.jid);
            if(!xmppRelay.isRelay){
              xmppRelay.state = "disconnected";
              xmppRelay.updateState("DISCONNECTED");//TODO This is only for a widget test
              xmppRelay.isConnected = false;
            }else {
              xmppRelay.counter--;
              xmppRelay.updateUsers();
            }
        }
      }); 
    },
    /**
    *This functions takes care of configuring the IWC. Both the specific DUI IWC and the genral IWC.
    *Here it is set wether intents shall be sent to both the peer group and the XMPP server or only one.
    */
    setupIWC: function(){
      //return; To not use relay.
       duimanager.duiIwcProxy.setPubSubNode(xmppRelay.pubsubservice, xmppRelay.userpubsubnode); //TODO Changed order as setXmppClient adds a handler with pubsubservice
       duimanager.duiIwcProxy.setXmppClient(xmppRelay.relayConnection);
       var twoWay = {
                relay:xmppRelay.relayConnection,
                xmpp:xmpp.connection,
                jid:xmpp.connection.jid,
                service:xmpp.connection.service,
                addHandler: function(handler, ns, name, type, id, from, options){
                  this.relay.addHandler(handler, ns, name, type, id, from, options);
                  //this.xmpp.addHandler(handler, ns, name, type, id, from, options);
                },
                pubsub:  {}
        };
        twoWay.pubsub.publishItem = function(pubSubEntity, pubSubNode, item, callback){
             twoWay.relay.pubsub.publishItem(pubSubEntity, pubSubNode, item, function(){});
             twoWay.xmpp.pubsub.publishItem(pubSubEntity, pubSubNode, item, function(){});
             callback();
        };
        xmppRelay.relayConnection.pubsub.subscribe(
                Strophe.getBareJidFromJid(xmppRelay.relayConnection.jid), 
                xmppRelay.pubsubservice,
                xmppRelay.userpubsubnode,
                function(){return true;},function(){return true;}
        );
        xmppRelay.relayConnection.pubsub.subscribe(
                Strophe.getBareJidFromJid(xmppRelay.relayConnection.jid), 
                xmppRelay.pubsubservice,
                xmpp.pubsubnode,
                function(){return true;},function(){return true;}
        );
        xmpp.iwcProxy.setXmppClient(xmppRelay.relayConnection);
        //xmpp.iwcProxy.setXmppClient(twoWay); //Use for notyfing outsiders as well
    },
    updateState: function(state){
        var intent = {
            "action": "RELAY_STATUS",
            "component": "",
            "data": state,
            "dataType":"string",
            "sender":"relay"
        };
        xmpp.iwcProxy.publish(intent);
    },
    updateUsers: function(){
        var intent = {
            "action": "RELAY_USER",
            "component": "",
            "data": ""+xmppRelay.counter,
            "dataType":"int",
            "sender":"relay",
            "flags":["PUBLISH_GLOBAL"]
        };
        xmpp.iwcProxy.publish(intent);
    }
  };
  domReady(function(){
    if(navigator.userAgent.indexOf("Firefox") != -1) {
      var version = parseInt(navigator.userAgent.match(/Firefox\/([0-9]+)\./)[1], 10);
      if(version < 27) return;
    }else if(navigator.userAgent.indexOf("Chrome") != -1){    
      var version = parseInt(navigator.userAgent.match(/Chrome\/([0-9]+)\./)[1], 10);
      if(version < 32) return;
    }else return;
    RTC = setupRTC();
    if (RTC) {
        RTCPeerconnection = RTC.peerconnection;
    }
    $(document).bind('callincoming.jingle', xmppRelay.onCallIncoming);
    $(document).bind('datachannelopen.jingle', xmppRelay.onDataChannelOpen);
    $(document).bind('user-xmpp-connected', function(){
        var pubsubservice = xmpp.pubsubservice;
        duimanager = require("./duimanager"); //TODO Why do we have circular depencies?
        connection = xmpp.connection;
        var userpubsubnode = xmpp.userpubsubnode;
        connection.jingle.ice_config = xmppRelay.ice_config;
        connection.jingle.media_constraints = {mandatory:{OfferToReceiveAudio:false,OfferToReceiveVideo:false}};
        xmppRelay.connectPubSub(pubsubservice, userpubsubnode, connection);
    });

  });
  return xmppRelay;

});
