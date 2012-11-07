<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link rel="stylesheet" type="text/css" href="character.css"/>
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
				document.getElementById("character").innerHTML = "Error waiting for refresh:<br/>"+req.status;
			}
			sendRequest();
		}
	};
	req.open("GET", "updatexml.php?name="+name+"&unique="+(new Date()).valueOf(), true);
	req.send();
}

function loadXMLDoc(dname) {
	var req = getRequest();
	req.open("GET", dname, false);
	req.send();
	return req.responseXML;
}

function displayXML(xml) {
	var div = document.getElementById("character");
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

	displayXML(loadXMLDoc(name+".xml"));
	sendRequest();
}
	</script>
</head>

<body onLoad="load();" style="font-size:62.5%;">
	<div id="character"></div>
</body>
</html>
