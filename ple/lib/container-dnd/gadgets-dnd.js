/**
 * Copyright 2009 Life360 - http://life360.com 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License. 
 */

/**
 * Summary: Infrastructure that builds upon Shindig's gadgets.js file to provide drag-and-drop
 *          functionality similar to something like iGoogle. Dojo is used extensively to simplify
 *          tasks such as manipulating classes, style, querying via CSS selectors, and for a very
 *          lightweight drag-and-drop abstraction. These patterns could be ported to another
 *          JS toolkit or rewritten in "raw" JavaScript if you want to handle all of the browser
 *          quirks "manually".
 *
 *          This file should be included after gadgets-ext.js. See the accompanying file demo.html
 *          for details.
 *
 *          Fairly extensive comments are included inline. This has been tested on FF3, Safari3,
 *          and IE7. Overall, the concepts are simple; it's the implementation details that were
 *          a bit tricky.
 */

dojo.require("dojo.dnd.Moveable");
dojo.addOnLoad(function() {

    //Create Moveables out of each div.gadgets-gadget-chrome and set up 
    //handlers for dnd events
    dojo.query(".gadgets-gadget-chrome").forEach(function(x) {
        var m = new dojo.dnd.Moveable(x); 

        //For IE - when a node is moved from a column, the underlying iframe changes size
        //so we need to track and it and reapply when the node becomes position:absolute
        var widthInColumn;

        //for determining if a drag actually started (vs something like a click on a button)
        var didMove = false;

        dojo.connect(m, "onMoveStart", function(mover){ 
            dojo.style(mover.node, "zIndex", 1000);

            didMove = false;

            //for every drag operation, create an overlay for each iframe
            //to prevent the iframe from intercepting mouse events
            //which kills drag performance
            dojo.query(".gadgets-gadget-content").forEach(function(fc) {
                var overlay = dojo.doc.createElement("div");
                dojo.style(overlay, {
                    position: "absolute",
                    height : dojo.style(fc, "height") + "px",
                    width : dojo.style(fc, "width") + "px"
                });

                dojo.addClass(overlay, "dndOverlay");

                dojo.place(overlay, fc, "first");

                widthInColumn = dojo.style(fc.parentNode, "width");
            });

            //console.log("onMoveStart");
        }); 

        var placeholder = dojo.doc.createElement("div");
        dojo.addClass(placeholder, "gadgets-gadget-placeholder");
        var isMoving = false;

        //Leave a placeholder in for the element that's being drug around
        //once it's actually on the move
        dojo.connect(m, "onMoved", function(mover, leftTop) {
            didMove = true;
            if (isMoving) return;
            isMoving = true;

            dojo.style(placeholder, "height", dojo.style(mover.node, "height")- 4 + "px"/*border*/);
            dojo.place(placeholder, m.node, "before");

            //console.log("onMoved");
        });

        var lastY;
        dojo.connect(m, "onMoving", function(mover, leftTop) {

            //there's a "false start" b/c of the nature of a node
            //transitioning to absolute positioning, so skip out the first time.
            //and apply the widthInColumn that was tracked from onMoveStart
            if (!isMoving) {
                lastY = leftTop.t;
                dojo.style(mover.node, "width", widthInColumn + "px");

                //also take this opportunity to style the overlay...
                dojo.query(".dndOverlay", mover.node).style({
                    opacity : 0.5,
                    background : "white"
                });

                return;
            }

            //maintain if nodes are moving up or down
            var dy = lastY - leftTop.t;
            lastY = leftTop.t;

            var coords = dojo.coords(mover.node);
    
            //calculate center of what's being moved and use it as the
            //basis of whether or not to juggle the nodes
            var cx = coords.l + coords.w/2;
            var cy = coords.t + coords.h/2;

            //determine if the center is overlapping with any .gadgets-gadget-chrome nodes
            //via bounding rect intersection 
            var targets = dojo.query(".gadgets-gadget-chrome")
            .filter (function(x) {
                return x != mover.node;
            })
            .filter(function(x) {
                var coords = dojo.coords(x);

                return (coords.x < cx && 
                    coords.x+coords.w > cx &&
                    coords.y < cy && 
                    coords.y+coords.h > cy);
            });

            //if not over any targets, then see if we're below a column
            if (!targets.length) {
                var col;
                dojo.forEach([1,2,3], function(colNum) {
                    var coords = dojo.coords("col"+colNum);
                    if (coords.l < cx && coords.l + coords.w > cx) col = colNum;

                    //the column may be empty
                    else if (!dojo.query(".gadgets-gadget-chrome", "col"+colNum).length && cx > coords.l)
                        col = colNum;
                });
                
                if (col && cy > dojo.coords("col"+col).t+dojo.coords("col"+col).h) {
                    dojo.place(placeholder, "col"+col, "last");
                } else return;
            } 

            dojo.place(placeholder, targets[0], dy <= 0 ? "after" : "before");

            //console.log("onMoving", targets);
        });

        dojo.connect(m, "onMoveStop", function(mover){ 

            dojo.style(m.node, {
                "zIndex" :  1,
                "width" : "auto"     
            });

            isMoving = false;
            dojo.query(".dndOverlay").orphan();

            //if no move occurred (like if someone clicked on a button, for example)
            //then stop short.
            if (!didMove) return; 

            //insert the node back where the placeholder is at
            //and reset style so next drag is consistent with this one
            dojo.place(mover.node, placeholder, "after");
            dojo.style(mover.node, {
                position : "static",
                top : "",
                left : "" 
            });
            placeholder.parentNode.removeChild(placeholder);

            //If columns were variable width, could adjust width of moved item for
            //new column here.
            
            //console.log("onMoveStop", mover.node);
        });
    });
});
