// global variables //
var TIMER = 5;
var SPEED = 10;
var WRAPPER = 'content';

// calculate the current window width //
function pageWidth() {
  return window.innerWidth != null ? window.innerWidth : document.documentElement && document.documentElement.clientWidth ? document.documentElement.clientWidth : document.body != null ? document.body.clientWidth : null;
}

// calculate the current window height //
function pageHeight() {
  return window.innerHeight != null? window.innerHeight : document.documentElement && document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body != null? document.body.clientHeight : null;
}

// calculate the current window vertical offset //
function topPosition() {
  return typeof window.pageYOffset != 'undefined' ? window.pageYOffset : document.documentElement && document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop ? document.body.scrollTop : 0;
}

// calculate the position starting at the left of the window //
function leftPosition() {
  return typeof window.pageXOffset != 'undefined' ? window.pageXOffset : document.documentElement && document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft ? document.body.scrollLeft : 0;
}

// build/show the dialog box, populate the data and call the fadeDialog function //
function showDialog(title, message, type, autohide) {
  if(!type) {
    type = 'error';
  }
  var dialog;
  var dialogheader;
  var dialogclose;
  var dialogtitle;
  var dialogcontent;
  var dialogmask;
  if(!document.getElementById('dialog')) {
    dialog = document.createElement('div');
    dialog.id = 'dialog';
    dialogheader = document.createElement('div');
    dialogheader.id = 'dialog-header';
    dialogtitle = document.createElement('div');
    dialogtitle.id = 'dialog-title';
    dialogclose = document.createElement('div');
    dialogclose.id = 'dialog-close'
    dialogcontent = document.createElement('div');
    dialogcontent.id = 'dialog-content';
    dialogmask = document.createElement('div');
    dialogmask.id = 'dialog-mask';
    document.body.appendChild(dialogmask);
    document.body.appendChild(dialog);
    dialog.appendChild(dialogheader);
    dialogheader.appendChild(dialogtitle);
    dialogheader.appendChild(dialogclose);
    dialog.appendChild(dialogcontent);;
    dialogclose.setAttribute('onclick','hideDialog()');
    dialogclose.onclick = hideDialog;
  } else {
    dialog = document.getElementById('dialog');
    dialogheader = document.getElementById('dialog-header');
    dialogtitle = document.getElementById('dialog-title');
    dialogclose = document.getElementById('dialog-close');
    dialogcontent = document.getElementById('dialog-content');
    dialogmask = document.getElementById('dialog-mask');
    dialogmask.style.visibility = "visible";
    dialog.style.visibility = "visible";
  }
  dialog.style.opacity = .00;
  dialog.style.filter = 'alpha(opacity=0)';
  dialog.alpha = 0;
  var width = pageWidth();
  var height = pageHeight();
  var left = leftPosition();
  var top = topPosition();
  var dialogwidth = dialog.offsetWidth;
  var dialogheight = dialog.offsetHeight;
  var topposition = top + (height / 3) - (dialogheight / 2);
  var leftposition = left + (width / 2) - (dialogwidth / 2);
  dialog.style.top = topposition + "px";
  dialog.style.left = leftposition + "px";
  dialogheader.className = type + "header";
  dialogtitle.innerHTML = title;
  dialogcontent.className = type;
  dialogcontent.innerHTML = message;
  var content = document.getElementById(WRAPPER);
  dialogmask.style.height = content.offsetHeight + 'px';
  dialog.timer = setInterval("fadeDialog(1)", TIMER);
  if(autohide) {
    dialogclose.style.visibility = "hidden";
    window.setTimeout("hideDialog()", (autohide * 1000));
  } else {
    dialogclose.style.visibility = "visible";
  }
}

// hide the dialog box //
function hideDialog() {
  var dialog = document.getElementById('dialog');
  clearInterval(dialog.timer);
  dialog.timer = setInterval("fadeDialog(0)", TIMER);
}

// fade-in the dialog box //
function fadeDialog(flag) {
  if(flag == null) {
    flag = 1;
  }
  var dialog = document.getElementById('dialog');
  var value;
  if(flag == 1) {
    value = dialog.alpha + SPEED;
  } else {
    value = dialog.alpha - SPEED;
  }
  dialog.alpha = value;
  dialog.style.opacity = (value / 100);
  dialog.style.filter = 'alpha(opacity=' + value + ')';
  if(value >= 99) {
    clearInterval(dialog.timer);
    dialog.timer = null;
  } else if(value <= 1) {
    dialog.style.visibility = "hidden";
    document.getElementById('dialog-mask').style.visibility = "hidden";
    clearInterval(dialog.timer);
  }
}

var rollTypeSuffix = {
	'initiative': '',
	'save': 'save',
	'ability': 'check',
	'weapon': 'attack/damage',
	'raw_attack': '',
	'skill': 'skill check',
	'sanity': ''
};

function nth(n) {
	return["st","nd","rd"][((n+90)%100-10)%10-1]||"th"
}

function showInfo(element) {
	var contents = element.getAttribute('info');
	if (!contents || contents === '<p>') return;
	var roll = element.getAttribute('roll');
	if (roll && roll != '/') {
		contents += '<br><br>Extra modifier: <input id="rollmod" type="text"><br>';

		var rolls = roll.split('/');
		var rollType = element.getAttribute('roll-type');
		var suffixes = rollTypeSuffix[rollType].split('/');
		for (var i = 0; i < rolls.length; i++) {
			if (rolls[i].indexOf('d') == -1) rolls[i] = '1d20'+rolls[i];
			suffix = suffixes[0];
			if (rolls.length > 1) {
				suffix += ' ('+(i+1)+nth(i+1)+')';
			}

			contents += '<button type="button" onmousedown="clearRoll();" onclick="roll(\''+rolls[i]+"', '"
				+ element.getAttribute('roll-type')+"', '" + element.getAttribute('title').toLowerCase()+"', '"
				+ suffix + '\', \'rollmod\');">Roll '+rolls[i]+'</button>';
		}
	}

	var contents2 = element.getAttribute('info2');
	if (contents2 && contents !== '<p>') {
		contents += '<br><br>' + contents2;
	}
	roll = element.getAttribute('roll2');
	if (roll && roll != '/') {
		contents += '<br><br>Extra modifier: <input id="rollmod2" type="text"><br>';
		var rolls = roll.split('/');
		var rollType = element.getAttribute('roll-type');
		var suffixes = rollTypeSuffix[rollType].split('/');
		for (var i = 0; i < rolls.length; i++) {
			if (rolls[i].indexOf('d') == -1) rolls[i] = '1d20'+rolls[i];

			suffix = suffixes[suffixes.length-1];
			if (rolls.length > 1) {
				suffix += ' ('+(i+1)+nth(i+1)+')';
			}

			contents += '<button type="button" onmousedown="clearRoll();" onclick="roll(\''+rolls[i]+"', '"
				+ element.getAttribute('roll-type')+"', '" + element.getAttribute('title').toLowerCase()+"', '"
				+ suffix + '\', \'rollmod2\');">Roll '+rolls[i]+'</button>';
		}
	}
	contents += '<br><br><div id="roll"/>';

	showDialog(element.getAttribute('title'), contents, 'info', false);
}

function showRollRequest(msg) {
	var contents = 'Make a';
	if (msg['roll-type'] == 'initiative') contents += 'n';
	contents += ' '+msg['roll-type'] + ' roll: '+msg['dice-spec']+'<br><br>';

	contents += '<div id="roll"></div><br>';

	contents += '<button id="rollbutton" type="button" onmousedown="clearRoll();" onclick="rollForRequest(\''+msg['dice-spec']+"', '"
		+ msg['roll-type']+"', '"+msg['roll-type']+"', '', '', '"+msg['req-token']+'\');">Roll</button>';

	contents += '<button id="cancelrollbutton" type="button" onclick="cancelRollRequest(\''+msg['req-token']+'\');">Cancel</button>';

	showDialog('DM requests '+msg['roll-type'] + ' roll', contents, 'info', false);
}

function clearRoll()
{
	var rollDiv = document.getElementById('roll');
	rollDiv.innerHTML = '';
}

function cancelRollRequest(reqToken) {
	hideDialog();
}

// same args as roll() below
function rollForRequest(diceSpec, rollType, title, suffix, reqToken) {
	roll(diceSpec, rollType, title, suffix, reqToken);
	$('#rollbutton').hide();
	$('#cancelrollbutton').html("Ok");
}

// TODO add info for ac (no rolls)
// TODO add info and/or rolls for bab, grapple
// TODO do something about criticals?
// diceSpec is either a number representing the modifier to a d20 roll, or the complete roll specifier for a single die type, e.g. "2d6+5"
// rolltype is type of statistic (e.g. "initiative", "ability")
// title is the specific statistic (e.g. "wisdom", "will")
// suffix is an additional description that should be derived from the rollType (e.g. "attack" for attack rolls)
// customModId is the id of a textbox the user can enter additional modifications into
// reqToken is the token provided by the server for roll requests
function roll(diceSpec, rollType, title, suffix, customModId, reqToken) {
	var rollDiv = document.getElementById('roll');
	var dice = diceSpec.replace(/\s+/, '').replace('-', '+-').split('+');
	var rolls = [];
	var totalRolls = 0;
	var mod = 0;
	for (var i = 0; i <dice.length; i++) {
		if (dice[i].indexOf('d') == -1) {
			mod += parseInt(dice[i]);
		} else {
			var type = parseInt(dice[i].substr(dice[i].indexOf('d')+1));
			var num = parseInt(dice[i].substring(0, dice[i].indexOf('d')));
			for (var j = 0; j < num; j++) {
				var r = 	Math.floor(Math.random()*type) + 1;
				rolls.push(r);
				totalRolls += r;
			}
		}
	}

	var rollTxt = ''+totalRolls + ' rolled';
	if (rolls.length > 1) {
		rollTxt += ' (' + rolls.join(', ') + ')';
	}
	if (mod != 0) {
		totalRolls += mod;
		rollTxt += '<br>' + (mod > 0 ? '+' : '') + mod + ' modifier<br>';
	}
	
	var customMod = 0;
	if (customModId && document.getElementById(customModId) != null) {
		var val = parseInt(document.getElementById(customModId).value);
		if (!Number.isNaN(val))
			customMod = val;
	}
	if (customMod != 0) {
		totalRolls += customMod;
		rollTxt += (customMod > 0 ? '+' : '') + customMod + ' extra modifier<br>';		
	}
	
	if (mod != 0 || customMod != 0) {
		rollTxt += '<b>' + totalRolls + ' total</b>';
	} else {
		rollTxt = '<b>' + rollTxt + '</b>';
	}

	if (updater) {
		updater.sendRoll({
			'roll-type': rollType,
			'dice-type': diceSpec,
			'title': title,
			'suffix': suffix,
			'rolls': rolls,
			'mod': mod,
			'extra-mod': customMod,
			'total': totalRolls
		});
	}
	
	rollDiv.innerHTML = rollTxt;
}