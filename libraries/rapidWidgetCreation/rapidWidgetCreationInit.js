// Load two libraries for allowing the user to rate this gadget and for capturing
// interactions in CAM format
$.getScript("http://widgets.onlinesjtu.com/gadgets/libs/rating.js", 
            function(){
                $.getScript("http://widgets.onlinesjtu.com/gadgets/libs/interactioncapture.js",
                function(){
                    var rating = new ROLE_module.rating("#importedGadget"); 
                    var interactioncapture  = 
                        new ROLE_module.interactioncapture("#importedGadget");
                    })
            }
        );
