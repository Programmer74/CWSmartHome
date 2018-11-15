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

function doApiRequest(request, setTextTo) {
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
    doApiRequest("/alive", objectId)
}