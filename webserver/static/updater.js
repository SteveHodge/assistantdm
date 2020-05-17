/*
* TODO:
* Dialog box should lock only the current tab, and should scroll overflow
*/

var updater = (function($) {
	var token = null;		// semi-unique token passed with each connection and download so the server can track our activity
	var listeners = [];	// list of files we are interested in (actually list of objects with type and listener properties)
	var source = null;
	var sheet1 = null;	// cached element for character sheet 1
	var sheet2 = null;	// cached element for character sheet 1
	var xsl1 = null;	// xslt transform for character sheet 1
	var xsl2 = null;	// xslt transform for character sheet 1
	var name = null;	// character name, if any

//	$(window).on('load', adjustHeight);

	$(document).ready(function () {
		if (token == null) token = (new Date()).valueOf();
		sheet1 = document.getElementById('tab_sheet1');
		sheet2 = document.getElementById('tab_sheet2');
		if (sheet1 || sheet2) setupCharacterSheet();
		if (document.getElementById('webcam')) setupWebcam();
		openConnection();
		toggleTokens();
	});

	function sendMessage(type, msg, success, failed) {
		msg.name = name;
		msg.type = type;

		//console.log(JSON.stringify(msg, null, '\t'));

		var a = document.createElement('a');
		a.href = new String(window.location);
		a.pathname = '/assistantdm/updates/input';

		var req = new XMLHttpRequest();
		req.open('PUT', a.href, true);
		req.setRequestHeader('Content-Type', 'application/json');
		req.onreadystatechange = function() {
			if (req.readyState === 4) {
				if (req.status !== 200) {
					if (typeof failed !== 'undefined') failed(req);
				} else {
					if (typeof success !== 'undefined') success(req);
				}
			}
		};
		req.send(JSON.stringify(msg, null, '\t'));
	}

	function moveToken(e) {
		var newloc = $('#movetoken input').val();
		if (!newloc) return;

		var output = {
			'moveto': newloc
		};

		sendMessage('move', output, (req) => $('#movetoken input').val(''), (req) => alert("Error moving token: "+req.statusText));
	}
	
	function sendRoll(msg) {
		var logStr = name + ' rolled '+msg['dice-type']+' for '+msg.title+' '+msg.suffix+':<br/>'+msg.rolls.join(' + ');
		if (msg.mod != 0) {
			logStr += (msg.mod > 0 ? ' + ' : ' ') + msg.mod;
		}
		logStr += ' = <b>' + msg.total + '</b><br/><br/>';
		addMessage(logStr);
		sendMessage('roll', msg);
	}

	// this stuff seems a bit hackish
//	function adjustHeight() {
//		var h = $('#photo1').height();
//		if (!h) return;
//		$('#webcam').css('min-height', h+'px');
//	}

	function setupWebcam() {
//		$('#tokens1').click(openPhoto);
		$('#overlaytoggle').click(toggleTokens);
		$('#tokenswitch').click(switchTokenList);
		$('#messageswitch').click(switchMessages);
		$('#status img').click(showLog);
		$('#zoomin').click(zoomIn);
		$('#zoomout').click(zoomOut);

//		$('#photo1').on('load', adjustHeight);
//		if (document.getElementById('tab_webcam')) {
//			document.getElementById('tab_webcam').onActivate = adjustHeight;
//		}

		if (name) {
			$('#movetoken')
				.text('Move to: ')
				.append($('<input type="text">').on('change', moveToken))
				.append($('<button>Go</button>').on('click', moveToken))
				.append($('<br>'));
		}
	
		$.getJSON('/assistantdm/static/initiative.json?token='+token+'&r='+(new Date()).valueOf(), function(data) {
			updateInitiativeText(data);
		});
		
		$.getJSON('/assistantdm/static/tokens.json?token='+token+'&r='+(new Date()).valueOf(), function(data) {
			updateTokensText(data);
		});
	
		addListener('initiative.json', function() {
			logMessage('updated initiatives');
			$.get('/assistantdm/static/initiative.json?token='+token+'&r='+(new Date()).valueOf(), function(data) {
				updateInitiativeText(data);
			});
		});
		
		addListener('map.png', function() {
			$('#photo1').attr('src', "/assistantdm/static/map.png?token="+token+"&r="+(new Date()).valueOf());
			logMessage('updated image');
		});
		
		addListener('tokens.png', function() {
			document.getElementById("tokens1").src = "/assistantdm/static/tokens.png?token="+token+"&r="+(new Date()).valueOf();
			logMessage('updated token overlay');
		});
		
		addListener('tokens.json', function() {
			logMessage('updated token legend');
			$.get('/assistantdm/static/tokens.json?token='+token+'&r='+(new Date()).valueOf(), function(data) {
				updateTokensText(data);
			});
		});
	}
		
	function setupCharacterSheet() {
		if (sheet1) {
			name = $(sheet1).attr('character');
			WRAPPER = 'tab_sheet1';	// set element id for the dialog box to use
		} else {
			name = $(sheet2).attr('character');
			WRAPPER = 'tab_sheet2';	// set element id for the dialog box to use
		}
		if (name) {
			$.get('/assistantdm/'+name+'/xml?token='+token, function(xml) {
				if (sheet1) {
					$.get('/assistantdm/static/CharacterTemplate.xsl', function(data) {
						xsl1 = data;
						displayCharacterXML(sheet1,xml, xsl1);
					});
				}
				if (sheet2) {
					$.get('/assistantdm/static/CharacterTemplate2.xsl', function(data) {
						xsl2 = data;
						displayCharacterXML(sheet2,xml, xsl2);
					});
				}

				var effectUpdater = $('#effectsList').data('updater');
				if (effectUpdater) effectUpdater(xml);
			});

			addListener(name+'.xml', function() {
				$.get('/assistantdm/'+name+'/xml?token='+token+'&r='+(new Date()).valueOf(), function(xml) {
					if (sheet1) displayCharacterXML(sheet1, xml, xsl1);
					if (sheet2) displayCharacterXML(sheet2, xml, xsl2);
					
					var effectUpdater = $('#effectsList').data('updater');
					if (effectUpdater) effectUpdater(xml);
				});
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

		source = new EventSource('http://updates.stevehodge.net/assistantdm/updates/'+type+'?token='+token, { withCredentials: true });

		logMessage('Connecting to server');
		updateStatus('pending');

		source.addEventListener('open', function(e) {
			logMessage('Connection to server open');
			updateStatus('connected');
		}, false);

		source.addEventListener('error', function(event) {
			if (event.target.readyState === EventSource.CLOSED) {
				logMessage('Lost connection to server');
				updateStatus('disconnected');
				if (source) source.close();
				source = null;
				setTimeout(openConnection, 10000);
			} else if (event.target.readyState === EventSource.CONNECTING) {
				logMessage('Lost connection to server, attempting reconnect');
				updateStatus('pending');
			} else {
				logError('Lost connection to server, unknown error');
				updateStatus('disconnected');
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
			html += '<tr><td>'+entry.token+'</td><td';
			if (entry.color) {
				html += ' bgcolor="'+entry.color+'"';
			}
			html += '>&nbsp;</td><td>'+entry.name+'</td></tr>';
		});
		$('#tokenlist > table').replaceWith(html+'</table>');
	}
	
	function displayCharacterXML(div,xml,xsl) {
		// code for IE
//		if (window.ActiveXObject) {
//			div.innerHTML=xml.transformNode(xsl);
//		}
		// code for Mozilla, Firefox, Opera, etc.
//		else if (document.implementation && document.implementation.createDocument) {
			xsltProcessor=new XSLTProcessor();
			xsltProcessor.importStylesheet(xsl);
			resultDocument = xsltProcessor.transformToFragment(xml,document);
			
			// compare current values to new values and mark differences
			if (div.hasChildNodes()) compareDOM(div.firstChild, resultDocument.firstChild, $(div).css('display') === 'none');
			
			$(div).empty().append(resultDocument.firstChild);
//		}
	}

	// set the 'changed' class on the after element if the after's textContent is different to before's or if copy is true and before has the 'changed' class
	// so set copy=true if the user is likely to have missed an update (e.g. if an element is hidden)
	function compareDOM(before, after, copy) {
		if (before.tagName !== after.tagName) return;	// hierarchy is different
		if (before.tagName === 'UL') {
			var map = {};	// map of textcontent to element for the old list
			for (var i = 0; i < before.children.length; i++) {
				map[before.children[i].textContent] = before.children[i];
			}
			// if a child of the new list is missing in the old list then it counts as changed
			for (var i = 0; i < after.children.length; i++) {
				if (!map[after.children[i].textContent] || copy && $(map[after.children[i].textContent]).hasClass('changed')) {
					$(after.children[i]).addClass('changed');
				}
			}			
		} else {
			if (before.children.length === 0 && after.children.length === 0) {
				if (before.textContent !== after.textContent || copy && $(before).hasClass('changed')) {
					$(after).addClass('changed');
				}
			} else if (before.children.length === after.children.length) {
				for (var i = 0; i < before.children.length; i++) {
					compareDOM(before.children[i], after.children[i], copy);
				}
			} else {
				// hierarchy is different
				$(after).addClass('changed');
				console.log('hierarchy different for '+before.tagName);
			}
		}
	}

	function zoomIn() {
		var mapImg = document.getElementById("photo1");
		var currWidth = mapImg.clientWidth;
		if (currWidth >= 2500)
			return false;
		setZoom(currWidth + 100);
	}
	
	function zoomOut() {
		var mapImg = document.getElementById("photo1");
		var currWidth = mapImg.clientWidth;
		if (currWidth <= 100)
			return false;
		setZoom(currWidth - 100);
	}

	function setZoom(width) {
		var mapImg = document.getElementById("photo1");
		var tokenImg = document.getElementById("tokens1");
		var topImg = document.getElementById("toplegend");
		mapImg.style.width = width + "px";
		tokenImg.style.width = width + "px";
		topImg.style.width  = width + "px";
		var leftImg = document.getElementById("leftlegend");
		leftImg.style.height = mapImg.clientHeight+"px";
		var scroller = document.getElementById("images");
		document.getElementById("toplegend").style.top = (scroller.scrollTop)+"px";
		document.getElementById("leftlegend").style.left = (scroller.scrollLeft)+"px";
	}

	function toggleTokens() {
		var $toggle = $('#overlaytoggle');
		$toggle.toggleClass('toggleon');
		if ($toggle.hasClass('toggleon')) {
			document.getElementById("tokens1").style.display = "inline";
			document.getElementById("tokenlist").style.display = "inline";
		} else {
			document.getElementById("tokens1").style.display = "none";
			document.getElementById("tokenlist").style.display = "none";			
		}
	}
	
	function switchTokenList() {
		var span = document.getElementById("tokenswitch");
		if (!span) return;
		var state = span.innerHTML == "\u2bc6";
		span.innerHTML = state ? "&#x2bc8" : "&#x2bc6";
		$('#tokenlist > table').toggle(!state);
	}

	function switchMessages() {
		var span = document.getElementById("messageswitch");
		if (!span) return;
		var state = span.innerHTML == "\u2bc6";
		span.innerHTML = state ? "&#x2bc8" : "&#x2bc6";
		$('#messages').toggle(!state);
	}

	function openPhoto() {
		var win=window.open("http://stevehodge.net/assistantdm/static/map.png", '_blank');
		win.focus();
	}

	function addListener(t, l) {
		listeners.push({type: t, listener: l});
	}
	
	function updateStatus(status) {
		$('#status img').attr("src", "/assistantdm/static/images/"+status+"_32px.png");
	}

	function addMessage (msg) {
		if (document.getElementById('messages')) {
			$('#messages').append(msg);
		}		
	}
	
	var log = "";

	function logMessage (msg) {
		log += (new Date()).toLocaleTimeString()+': '+msg+'<br>';
	}

	function logError (msg) {
		var msg = (new Date()).toLocaleTimeString()+': '+msg+'<br>';
		log += msg;
		if (document.getElementById('messages')) {
			$('#messages').append(msg);
		} else {
			alert(msg);
		}
	}
	
	function showLog() {
		showDialog('Debug Log', log, 'info', false);
	}

	return {
		sendRoll: sendRoll
	}
}(jQuery));
