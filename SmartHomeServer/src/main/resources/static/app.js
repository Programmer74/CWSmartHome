const apiPrefix = "/api";
const alarmApiPrefix = "/api/alarm";

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

function getAliveNodes(objectId) {
    document.getElementById(objectId).innerHTML = "loading";
    getRq(apiPrefix + "/alive",
        function (s) {
            var aliveNodes = JSON.parse(s);
            if (aliveNodes.status === "ok") {
                var len = aliveNodes.nodes.length;
                document.getElementById(objectId).innerHTML = "ok " + len + " nodes";
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
                document.getElementById(objectId).innerHTML = "Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(setTextTo).innerHTML = "Error " + err;
        });
}

function getTempInside(initialText, objectId) {
    document.getElementById(objectId).innerHTML = "loading";
    getRq(apiPrefix + "/get/04/4/float",
        function (s) {
            var temp = JSON.parse(s);
            if (temp.status === "ok") {
                document.getElementById(objectId).innerHTML = initialText + ": " + temp.value + "C";
            } else {
                document.getElementById(objectId).innerHTML = initialText + " Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(setTextTo).innerHTML = initialText + " Error " + err;
        });
}
function getTempOutside(initialText, objectId) {
    document.getElementById(objectId).innerHTML = "loading";
    getRq(apiPrefix + "/get/04/5/float",
        function (s) {
            var temp = JSON.parse(s);
            if (temp.status === "ok") {
                document.getElementById(objectId).innerHTML = initialText + ": " + temp.value + "C";
            } else {
                document.getElementById(objectId).innerHTML = initialText + " Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(setTextTo).innerHTML = initialText + " Error " + err;
        });
}

function setRelay(initialText, objectId, statusObjectId, node, pin, value) {
    document.getElementById(objectId).innerHTML = "loading";
    getRq(apiPrefix + "/setRelay/" + node + "/" + pin + "/" + value,
        function (s) {
            var temp = JSON.parse(s);
            if (temp.status === "ok") {
                document.getElementById(objectId).innerHTML = initialText;
                getRelay("State: ", statusObjectId, node, pin);
            } else {
                document.getElementById(objectId).innerHTML = initialText + " Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(objectId).innerHTML = initialText + " Error " + err;
        });
}
function getRelay(initialText, objectId, node, pin) {
    document.getElementById(objectId).innerHTML = "loading";
    getRq(apiPrefix + "/get/" + node + "/" + pin + "/byte",
        function (s) {
            var temp = JSON.parse(s);
            if (temp.status === "ok") {
                if (temp.value === 0) {
                    document.getElementById(objectId).innerHTML = initialText + "Off";
                    document.getElementById(objectId).style.background = "#5b5449";
                } else {
                    document.getElementById(objectId).innerHTML = initialText + "On";
                    document.getElementById(objectId).style.background = "#f2cf9a";
                }
            } else {
                document.getElementById(objectId).innerHTML = initialText + " Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(objectId).innerHTML = initialText + " Error " + err;
        });
}

function getAlarmState(initialText, objectId) {
    document.getElementById(objectId).innerHTML = "loading";
    getRq(alarmApiPrefix + "/current",
        function (s) {
            var temp = JSON.parse(s);
            if (temp.status === "ok") {
                if (temp.value === 0) {
                    document.getElementById(objectId).innerHTML = initialText + "Off";
                    document.getElementById(objectId).style.background = "#3a903a";
                } else {
                    document.getElementById(objectId).innerHTML = initialText + "On";
                    document.getElementById(objectId).style.background = "#f81200";
                }
            } else {
                document.getElementById(objectId).innerHTML = initialText + " Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(objectId).innerHTML = initialText + " Error " + err;
        });
}
function getAlarmLastTriggered(initialText, objectId) {
    document.getElementById(objectId).innerHTML = "loading";
    getRq(alarmApiPrefix + "/lastAlarm",
        function (s) {
            var temp = JSON.parse(s);
            if (temp.status === "ok") {
                if (temp.value != 0) {
                    var adate = new Date(temp.value);
                    document.getElementById(objectId).innerHTML = initialText + adate.toString()
                } else {
                    document.getElementById(objectId).innerHTML = initialText + "Never";
                }
            } else {
                document.getElementById(objectId).innerHTML = initialText + " Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(objectId).innerHTML = initialText + " Error " + err;
        });
}
function getAlarmCount(initialText, objectId) {
    document.getElementById(objectId).innerHTML = "loading";
    getRq(alarmApiPrefix + "/totalAlarms",
        function (s) {
            var temp = JSON.parse(s);
            if (temp.status === "ok") {
                document.getElementById(objectId).innerHTML = initialText + temp.value;
            } else {
                document.getElementById(objectId).innerHTML = initialText + " Error: " + s;
            }
        },
        function (err, s) {
            document.getElementById(objectId).innerHTML = initialText + " Error " + err;
        });
}

function analogWrite(initialText, objectId, node, pin, value) {
    if (document.getElementById(objectId) != null) {
        document.getElementById(objectId).innerHTML = "loading";
    }
    getRq(apiPrefix + "/analogWrite/" + node + "/" + pin + "/" + value,
        function (s) {
            var temp = JSON.parse(s);
            if (temp.status === "ok") {
                if (document.getElementById(objectId) != null) {
                    document.getElementById(objectId).innerHTML = initialText;
                }
            } else {
                if (document.getElementById(objectId) != null) {
                    document.getElementById(objectId).innerHTML = initialText + " Error: " + s;
                }
            }
        },
        function (err, s) {
            if (document.getElementById(objectId) != null) {
                document.getElementById(objectId).innerHTML = initialText + " Error " + err;
            }
        });
}

//node1:
//leds: api/set[Relay|LED]/01/[2|3|4]/[0..255]

//node2:
//pir sensor: api/get/2/1

//node4:
//temperature: api/get/04/4/float
//temperature2: api/get/04/5/float

function updateRect(jscolor) {
    // 'jscolor' instance can be used as a string
    document.getElementById('rect').style.backgroundColor = '#' + jscolor;
}
function updateRGB(jscolor) {
    // 'jscolor' instance can be used as a string
    document.getElementById('rect').style.backgroundColor = '#' + jscolor;
    var rgb = hexToRgb('#' + jscolor);
    console.log("updated");
    analogWrite(null, null, 1, 3, rgb.r);
    analogWrite(null, null, 1, 5, rgb.g);
    analogWrite(null, null, 1, 6, rgb.b);
}
function hexToRgb(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : null;
}

var alarmsAutoUpdate = false;
var timerId = 0;

function cmdAlarmsAutoUpdateClicked() {
    if (alarmsAutoUpdate === false) {
        alarmsAutoUpdate = true;
        document.getElementById("cmdAlarmsAutoUpdate").innerHTML = "AutoUpdate: On";
        timerId = window.setInterval(function(){
            document.getElementById("cmdAlarmState").click();
            document.getElementById("cmdAlarmLastTriggered").click();
            document.getElementById("cmdAlarmsTotal").click();
        }, 1000);
    } else {
        window.clearInterval(timerId);
        alarmsAutoUpdate = false;
        document.getElementById("cmdAlarmsAutoUpdate").innerHTML = "AutoUpdate: Off";
    }
}
function showOrHideElement(elementId) {
    if (document.getElementById(elementId) != null) {
        if (document.getElementById(elementId).style.display != "none") {
            document.getElementById(elementId).style.display = "none";
        } else {
            document.getElementById(elementId).style.display = "block";
        }
    }
}