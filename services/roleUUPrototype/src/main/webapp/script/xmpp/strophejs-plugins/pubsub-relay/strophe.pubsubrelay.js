define(["../../strophe", "../disco/strophe.disco"], function(Strophe) {

/* WebRTC plugin
**
** This plugin implements data channels via WebRTC
**
*/

Strophe.addConnectionPlugin('pubsubrelay', {
    connection: null,
    init: function (conn) {
        if(Strophe.relay === undefined ){
          console.log("Need to have a pubsub relay running!");
          return;
        }else{
          console.log("Init connection plugin for connection "+ conn.service);
        }
        if (conn.disco) {
            // http://xmpp.org/extensions/xep-0167.html#support
            // http://xmpp.org/extensions/xep-0176.html#support
            conn.disco.addFeature('http://jabber.org/protocol/pubsub');
            for(var key in Strophe.relay.nodes){
              conn.disco.addItem("pubsub", key, Strophe.relay.nodes[key].name, Strophe.relay.nodes[key].callback);
            };
        }else {
          console.log("Disco is needed");
        }
        conn.addHandler(this.onPubSub.bind(this), 'http://jabber.org/protocol/pubsub', null, null, null, null, null);
        conn.addHandler(this.onPubSubError.bind(this), 'http://jabber.org/protocol/pubsub#error', null, null, null, null, null);
        conn.addHandler(this.onPubSubEvent.bind(this), 'http://jabber.org/protocol/pubsub#event', null, null, null, null, null);
        conn.addHandler(this.onPubSubOwner.bind(this), 'http://jabber.org/protocol/pubsub#owner', null, null, null, null, null);
        this.connection = conn;
    },
    onPubSub: function(iq) {
        if($(iq).find('>pubsub>create').length){
          var nodeName = $(iq).find('>pubsub>create').attr('node');
          console.log("create node", nodeName);
          if (nodeName == undefined){
            var error = $iq({
              type: 'error',
              to: iq.getAttribute('from'),
              id: iq.getAttribute('id')})
            .c('error', {type:'modify'})
            .c('not-acceptable', {xmlns:'urn:ietf:params:xml:ns:xmpp-stanzas'})
            .up()
            .c('nodeid-required', {xmlns:'http://jabber.org/protocol/pubsub#errors'});
            this.connection.sendIQ(error);
          }else if(Strophe.relay.createNode(nodeName)){
            this.connection.disco.addItem("pubsub", nodeName, Strophe.relay.nodes[nodeName].name, Strophe.relay.nodes[nodeName].callback);
            var ack = $iq({type: 'result',
              to: iq.getAttribute('from'),
              id: iq.getAttribute('id')
              });
            this.connection.sendIQ(ack);
          }else{
            var error = $iq({
              type: 'error',
              to: iq.getAttribute('from'),
              id: iq.getAttribute('id')})
            .c('error', {type:'cancel'})
            .c('conflict', {xmlns:'urn:ietf:params:xml:ns:xmpp-stanzas'});
            this.connection.sendIQ(error);
          }
        }else if($(iq).find('>pubsub>subscribe').length){
          var nodeName = $(iq).find('>pubsub>subscribe').attr('node');
          if(!Strophe.relay.nodes[nodeName]){
            if(Strophe.relay.createNode(nodeName)){
              console.log("Node created succesfully");
            }else{
              console.log("Node could not be created");
            }
          }
            Strophe.relay.nodes[nodeName].subscribe(this.connection);
            var response = $iq({type:'result',
                to: iq.getAttribute('from'),
                from: this.connection.jid,
                id: iq.getAttribute('id')})
              .c('pubsub', {xmlns:'http://jabber.org/protocol/pubsub'})
              .c('subscription',{
                node:nodeName,
                jid:$(iq).find('>pubsub>subscribe').attr('jid'),
                subscription:'subscribed'
              });
             this.connection.sendIQ(response);
          /*}else{
              var error = $iq({
                type: 'error',
                to: iq.getAttribute('from'),
                id: iq.getAttribute('id')})
                .c('error', {type:'auth'})
                .c('item-not-found',{ xmlns:'urn:ietf:params:xml:ns:xmpp-stanzas'});
              this.connection.sendIQ(error);
          }*/
        }else if($(iq).find('>pubsub>publish').length){
            var request = $(iq).find('>pubsub>publish');
            var evt = $build("message",{from:$(iq).attr('to')}).
                      c("event", {xmlns:"http://jabber.org/protocol/pubsub#event"})
                      .c("items", {node:request.attr('node')})
                      .cnode($(iq).find('>pubsub>publish>item').get(0),{});
            Strophe.relay.nodes[request.attr('node')].publish(evt.tree());
        }else if($(iq).find('>pubsub>unsubscribe').length){
          var nodeName = $(iq).find('>pubsub>unsubscribe').attr('node');
          if(Strophe.relay.nodes[nodeName]){
            //TODO Strophe.relay.nodes[nodeName].unsubscribe($(iq).find('>pubsub>unsubscribe').attr('jid'));
            var ack = $iq({type: 'result',
              to: iq.getAttribute('from'),
              from: this.connection.jid,
              id: iq.getAttribute('id')
              });
            this.connection.sendIQ(ack);
          }
        }else if($(iq).find('>pubsub>subscriptions').length){
          var ack = $iq({type: 'result',
                id: iq.getAttribute('id')
              })
              .c('pubsub', {xmlns:'http://jabber.org/protocol/pubsub'})
              .c('subscriptions',{});
            this.connection.sendIQ(ack);
        }else if($(iq).find('>pubsub>options').length){
          var error = $iq({type: 'error',
                to: iq.getAttribute('from'),
                from: this.connection.jid,
                id: iq.getAttribute('id')})
            .c('error', {type:'cancel'})
            .c('feature-not-implemented', {xmlns:'urn:ietf:params:xml:ns:xmpp-stanzas'})
            .up()
            .c('unsupported', {xmlns:'http://jabber.org/protocol/pubsub#errors',
                               feautre: 'subscription-options'});
          this.connection.sendIQ(error);
        }
        return true;
    },
    onPubSubError: function(iq) {
      return true;
    },
    onPubSubEvent: function(iq) {
      return true;
    },
    onPubSubOwner: function(iq) {
      if($(iq).find('>pubsub>configure').length){
        var error = $iq({type: 'error',
                to: iq.getAttribute('from'),
                id: iq.getAttribute('id')})
            .c('error', {type:'cancel'})
            .c('feature-not-implemented', {xmlns:'urn:ietf:params:xml:ns:xmpp-stanzas'})
            .up()
            .c('unsupported', {xmlns:'http://jabber.org/protocol/pubsub#errors',
                               feautre: 'config-node'});
        this.connection.send(error);
      }else if ($(iq).find('>pubsub>delete').length){
        var error;
        var deleteNode = $(iq).find('>pubsub>delete');
        var nodeName = deleteNode.attr('node');
        if(!this.relay.nodes[nodeName]){
          error = this.connection.errorIQ('cancel', $build('item-not-found',{ xmlns:'urn:ietf:params:xml:ns:xmpp-stanzas'}));
        }else{
          error = this.connection.errorIQ('auth', $build('forbidden',{ xmlns:'urn:ietf:params:xml:ns:xmpp-stanzas'}));
        }
        this.connection.sendIQ(error);
      }
      return true;
    },
    errorIQ: function(type, content){
      var error = $iq({
            type: 'error'})
            .c('error', {type:type})
            .cnode(content.node);
      return error;
    }
});
});
