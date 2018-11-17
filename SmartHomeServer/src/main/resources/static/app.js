const apiPrefix = "/api";

function getRq(url, callback, error) {
    console.log("Requesting url " + url);
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState === 4) {
            if (xmlHttp.status === 200) {
                callback(xmlHttp.responseText);
            } else {
                error(xmlHttp.status, xmlHttp.responseText)
            }
        }
    };
    xmlHttp.open("GET", url, true); // true for asynchronous
    xmlHttp.send(null);
}

function doApiRequestAndSetResultTo(request, setTextTo) {
    document.getElementById(setTextTo).innerHTML = "Loading...";
    getRq(apiPrefix + request,
        function (s) {
            document.getElementById(setTextTo).innerHTML = s;
        },
        function (err, s) {
            document.getElementById(setTextTo).innerHTML = "Error " + err;
        });
}

function getAliveNodes(objectId) {
    document.getElementById("divAliveNodes").innerHTML = "loading";
    getRq(apiPrefix + "/alive",
        function (s) {
            var aliveNodes = JSON.parse(s);
            if (aliveNodes.status === "ok") {
                document.getElementById("divAliveNodes").innerHTML = aliveNodes.status;
                var len = aliveNodes.nodes.length;
                for (var i = 0; i < 10; i++) {
                    var divId = "node" + i.toString() + "area";
                    var style = "none";
                    if (aliveNodes.nodes.includes(i)) {
                        style = "block";
                    }
                    if (document.getElementById(divId) != null) {
                        document.getElementById(divId).style.display = style;
                    }
                    console.log(i + " is " + style)
                }
            } else {
                document.getElementById("divAliveNodes").innerHTML = "Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(setTextTo).innerHTML = "Error " + err;
        });
}
//node1:
//leds: api/set[Relay|LED]/01/[2|3|4]/[0..255]

//node2:
//pir sensor: api/get/2/1

//node4:
//temperature: api/get/04/4/float
//temperature2: api/get/04/5/float
