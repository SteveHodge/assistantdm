<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link rel="stylesheet" type="text/css" href="character.css"/>
	<script type="text/javascript" src="dialog_box.js"></script>
	<script type="text/javascript">
<?php
echo 'var name = "'.$_GET['name'].'";'."\n";
?>

var xsl;

function getRequest() {
	if (window.XMLHttpRequest) {
		// code for IE7+, Firefox, Chrome, Opera, Safari
		return new XMLHttpRequest();
	} else {
		try { return new ActiveXObject("Msxml2.XMLHTTP.6.0"); } catch (e) {}
		try { return new ActiveXObject("Msxml2.XMLHTTP"); } catch (e) {}
		throw new Error("This browser does not support XMLHttpRequest.");
	}
}

function sendRequest() {
	//alert("sending request");
	var req = getRequest();
	req.onreadystatechange = function() {
		if (req.readyState == 4) {
			if (req.status == 200) {
				var xml = req.responseXML;
				if (xml) displayXML(xml);
			} else {
				document.getElementById("content").innerHTML = "Error waiting for refresh:<br/>"+req.status;
				//displayXML(loadXMLDoc(name+".xml"));
			}
			sendRequest();
		}
	};
	req.open("GET", "old_updatexml.php?name="+name+"&unique="+(new Date()).valueOf(), true);
	req.send();
}

function createEventSource() {
	var source=new EventSource("updatexml.php?name="+name);
	source.onerror=function(e) {
		alert("Error with EventSource: "+e);
	}
	source.onmessage=function(event) {
		//alert("update "+event.data);
		if (event.data == name+".xml") {
			displayXML(loadXMLDoc(name+".xml?unique="+(new Date()).valueOf()));
		}
	};
}

function loadXMLDoc(dname) {
	var req = getRequest();
	req.open("GET", dname, false);
	req.send();
	return req.responseXML;
}

function displayXML(xml) {
	var div = document.getElementById("content");
	// code for IE
	if (window.ActiveXObject) {
		div.innerHTML=xml.transformNode(xsl);
	}
	// code for Mozilla, Firefox, Opera, etc.
	else if (document.implementation && document.implementation.createDocument) {
		xsltProcessor=new XSLTProcessor();
		xsltProcessor.importStylesheet(xsl);
		resultDocument = xsltProcessor.transformToFragment(xml,document);
		while (div.hasChildNodes()) div.removeChild(div.firstChild);
		div.appendChild(resultDocument);
	}

}

function load() {
	if (name == "") return;
	document.title = name;

	xsl=loadXMLDoc("CharacterTemplate.xsl");

	displayXML(loadXMLDoc(name+".xml?unique="+(new Date()).valueOf()));

	if(typeof(EventSource)!=="undefined") {
		createEventSource();
	} else {
		alert("Server-sent events not supported.\nGet a better browser - Firefox, Chrome, Safari, and Opera all support this, IE is shit");
		sendRequest();
	} 
}
	</script>
</head>

<body onLoad="load();" style="font-size:62.5%;">
	<div id="content"></div>
</body>
</html>
