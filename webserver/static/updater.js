(function($) {
	var listeners = [];
	var source = null;
	var xsl = null;

	$(document).ready(function () {
		if (document.getElementById('webcam')) setupWebcam();
		if (document.getElementById('tab_sheet')) setupCharacterSheet();
		openConnection();
	});

	function setupWebcam() {
		$('#tokens1').click(openPhoto);
		$('#tokens1check').click(toggleTokens);
	
		$.getJSON('static/initiative.json?'+(new Date()).valueOf(), function(data) {
			updateInitiativeText(data);
		});
		
		$.getJSON('static/tokens.json?'+(new Date()).valueOf(), function(data) {
			updateTokensText(data);
		});
	
		addListener('initiative.json', function() {
			logMessage('updated initiatives');
			$.get('static/initiative.json?'+(new Date()).valueOf(), function(data) {
				updateInitiativeText(data);
			});
		});
		
		addListener('camera.jpg', function() {
			document.getElementById("photo1").src = "/assistantdm/static/camera.jpg?"+(new Date()).valueOf();
			logMessage('updated image');
		});
		
		addListener('tokens.png', function() {
			document.getElementById("tokens1").src = "/assistantdm/static/tokens.png?"+(new Date()).valueOf();
			logMessage('updated token overlay', true);
		});
		
		addListener('tokens.json', function() {
			logMessage('updated token legend', true);
			$.get('static/tokens.json?'+(new Date()).valueOf(), function(data) {
				updateTokensText(data);
			});
		});
	}
		
	function setupCharacterSheet() {
		var name = $('#tab_sheet').attr('character');
		if (name) {
			WRAPPER = 'tab_sheet';	// set element id for the dialog box to use

			$.get('/assistantdm/static/CharacterTemplate.xsl', function(data) {
				xsl = data;
				$.get('/assistantdm/'+name+'/xml?unique='+(new Date()).valueOf(), displayXML);
			});

			addListener(name+'.xml', function() {
				$.get('/assistantdm/'+name+'/xml?unique='+(new Date()).valueOf(), displayXML);
			});
		}
	}

	function openConnection() {
		var type = null;
		
		if (source !== null) return;
		if (listeners.length === 0) return;
		
		if(typeof(EventSource) === "undefined") {
			alert("Server-sent events not supported.\nGet a better browser - Firefox, Chrome, Safari, and Opera all support this, IE is shit");
			return false;
		} 
		
		for (var i = 0; i < listeners.length; i++) {
			if (type === null) {
				type = listeners[i].type;
			} else if (type !== listeners[i].type) {
				type = 'all';
				break;
			}
		}

		source = new EventSource('http://updates.stevehodge.net/assistantdm/updates/'+type, { withCredentials: true });

		logMessage('Connecting to server');

		source.addEventListener('open', function(e) {
			logMessage('Connection to server open');
		}, false);

		source.addEventListener('error', function(event) {
			if (event.target.readyState === EventSource.CLOSED) {
				if (source) source.close();
				logError('Lost connection to server');
			} else if (event.target.readyState === EventSource.CONNECTING) {
				logMessage('Lost connection to server, attempting reconnect');
			} else {
				logError('Lost connection to server, unknown error');
			}
		}, false);

		source.addEventListener('message', function(event) {
			var lines = event.data.split("\n");
			for (i = 0; i < lines.length; i++) {
				for (j = 0; j < listeners.length; j++) {
					if (lines[i] === listeners[j].type) {
						listeners[j].listener();
					}
				}
			}
		}, false);

		return true;
	}

	function updateInitiativeText(data) {
		var html = '<table>\n<tr><th>Combat Round</th><th>\n';
		html += (data.round == undefined ? 0 : data.round) + "</th><th>Initiative</th></tr>\n";
		
		$.each(data.order, function(i,entry) {
			html += "<tr><td colspan=2>"+entry.name+"</td>";
			html += "<td>"+entry.initiative+"</td></tr>\n";
		});
		html += "</table>\n";
		$('#initiative').html(html);
	}
	
	function updateTokensText(data) {
		var html = '<table>';
		$.each(data, function(i, entry) {
			html += '<tr><td>'+entry.token+'</td><td>'+entry.name+'</td></tr>';
		});
		$('#tokenlist').html(html+'</table>');
	}
	
	function displayXML(xml) {
		var div = document.getElementById("tab_sheet");
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

	function toggleTokens() {
		var checked = document.getElementById("tokens1check").checked;
		document.getElementById("tokens1").style.display = checked?"inline":"none";
		document.getElementById("tokenlist").style.display = checked?"inline":"none";
	}
	
	function openPhoto() {
		var win=window.open("http://stevehodge.net/assistantdm/static/camera.jpg", '_blank');
		win.focus();
	}

	function addListener(t, l) {
		listeners.push({type: t, listener: l});
	}

	function logMessage (msg, debug) {
		if (document.getElementById('messageDiv')) {
			if (document.getElementById('debugcheck').checked) {
				$('#messageDiv').append((new Date()).toLocaleTimeString()+': '+msg+'<br>');
			} else if (!debug) {
				$('#messageDiv').html((new Date()).toLocaleTimeString()+': '+msg+'<br>');
			}
		}
	}

	function logError (msg) {
		if (document.getElementById('messageDiv')) {
			if (document.getElementById('debugcheck').checked) {
				$('#messageDiv').append((new Date()).toLocaleTimeString()+': '+msg+'<br>');
			} else if (!debug) {
				$('#messageDiv').html((new Date()).toLocaleTimeString()+': '+msg+'<br>');
			}
		} else {
			alert(msg);
		}
	}

}(jQuery));
