/*jslint browser: true, plusplus: true, todo: true, vars: true, white: true */

/* TODO
* if metamagic is applied we could filter slots that can't be used (though maybe it's better to show them)
* wizard spells that are not in the spell book should be cleared from their slots when resting
*/

/**
* ScrollFix v0.1
* http://www.joelambert.co.uk
*
* Copyright 2011, Joe Lambert.
* Free to use under the MIT license.
* http://www.opensource.org/licenses/mit-license.php
*/

var ScrollFix = function (elem) {
	'use strict';

	// Variables to track inputs
	//var startY;
	var startTopScroll;

	elem = elem || document.querySelector(elem);

	// If there is no element, then do nothing
	if(!elem) {
		return;
	}

	// Handle the start of interactions
	elem.addEventListener('touchstart', function() {
		//startY = event.touches[0].pageY;
		startTopScroll = elem.scrollTop;

		if(startTopScroll <= 0) {
			elem.scrollTop = 1;
		}

		if(startTopScroll + elem.offsetHeight >= elem.scrollHeight) {
			elem.scrollTop = elem.scrollHeight - elem.offsetHeight - 1;
		}
	});
};

// frequently accessed elements (these are set on load and shouldn't ever change)
var spellList;
// frequently accessed tab elements (these are set when a prepare tab is selected)
// TODO could move these to the current tab element
var tabPrefix;				// id prefix for the current tab
var availableList;		// left hand list for prepare and learn type tabs
var slotList;				// right hand list for prepare and learn type tabs
var heightenCheckbox;	// heighten metamagic checkbox for prepare type tabs
var heightenDropdown;	// heighten metamagic dropdown for prepare type tabs
var lockButton;			// lock button for prepare type tabs

function getParentTab(el) {
	'use strict';

	while (el !== null && !el.className.match(/(?:^|\s)tab(?!\S)/)) {
		el = el.parentNode;
	}
	return el;
}

function isSelected(element) {
	'use strict';

	return element.className.match(/(?:^|\s)selected(?!\S)/);
}

// checks if there are any selected children of the supplied list. if there selected children then enable
// the attached button (if any), otherwise disable it.
function updateButton(list) {
	'use strict';
	var disable = true;
	var node, button;

	if (list.hasAttribute('button')) {
		for (node = list.firstElementChild; node !== null; node = node.nextElementSibling) {
			if (isSelected(node)) {
				disable = false;
			}
		}
		button = document.getElementById(list.getAttribute('button'));
		button.disabled = disable;
	}
}

function deselect(element) {
	'use strict';

	element.className = element.className.replace( /(?:^|\s)selected(?!\S)/g , '' );
	updateButton(element.parentElement);
}

function select(element) {
	'use strict';

	element.className += ' selected';
	updateButton(element.parentElement);
}

function updateSpell(elem) {
	'use strict';

	elem.textContent = elem.getAttribute('level') + ' ' + elem.getAttribute('spell');
}

function setupSpellLists(elems) {
	'use strict';
	var i, list, node;

	// set descriptions and base level on available list
	for (i = 0; i < elems.length; i++) {
		list = document.getElementById(elems[i].getAttribute('prefix')+'availableList');
		for (node = list.firstElementChild; node !== null; node = node.nextElementSibling) {
			node.setAttribute('baselevel', node.getAttribute('level'));
			updateSpell(node);
		}
	}
}

function setLearnt(list, node, learnt) {
	'use strict';
	var spell;

	for (spell = list.firstElementChild; spell !== null; spell = spell.nextElementSibling) {
		if (spell.textContent === node.textContent) {
			spell.setAttribute('always_hidden', !learnt);
		}
	}
}

function listItemClickHandler() {
	'use strict';

	if (this.getAttribute('locked') === 'true') {
		return;
	}
	if (isSelected(this)) {
		deselect(this);
	} else {
		select(this);
	}
}

function filterLists() {
	'use strict';
	var node, nodeLevel;

	var levelFilter = document.getElementById(tabPrefix+'levelFilter');
	var level = levelFilter.value;
	var maxSlot = parseInt(getParentTab(levelFilter).getAttribute('max_slot'), 10);

	// available list: show only spells of the filtered level (if there is one)
	// if there is a showall filter that's checked then we show all rows, even 'always_hidden'
	var showAll = document.getElementById(tabPrefix+'showAll');
	if (showAll === null) {
		showAll = false;
	} else {
		showAll = showAll.checked;
	}
	for (node = availableList.firstElementChild; node !== null; node = node.nextElementSibling) {
		nodeLevel = node.getAttribute('level');
		if ((nodeLevel === level || (level === '-1' && nodeLevel <= maxSlot)) && (showAll || node.getAttribute('always_hidden') !== 'true')) {
			node.style.display = null; // 'block';
		} else {
			node.style.display = 'none';
			deselect(node);
		}
	}
	
	// slot list: show slots equal to or higher than the filtered level (if there is one)
	for (node = slotList.firstElementChild; node !== null; node = node.nextElementSibling) {
		nodeLevel = node.getAttribute('level');
		if (nodeLevel === level || level === '-1' || (nodeLevel > level && slotList.getAttribute('filter_allow_higher') === 'true')) {
			node.style.display = null; // 'block';
		} else {
			node.style.display = 'none';
			deselect(node);
		}
	}
}

function metamagicChange() {
	'use strict';
	var i, node, elems, adjusted;
	var levelAdj = 0;

	// if heightened is checked the level adjustment should be at least +1:
	if (heightenCheckbox && heightenCheckbox.checked) {
		if (heightenDropdown.value === 0) {
			heightenDropdown.value = 1;
			heightenCheckbox.setAttribute('level', 1);
		}
	}

	// get total level adjustment
	elems = document.querySelectorAll('#'+getParentTab(availableList).id+' [name=metamagic]');
	for (i = 0; i < elems.length; i++) {
		if (elems[i].checked) {
			levelAdj += parseInt(elems[i].getAttribute('level'), 10);
		}
	}

	// adjust the available spells list
	for (node = availableList.firstElementChild; node !== null; node = node.nextElementSibling) {
		// set the adjusted level
		adjusted = parseInt(node.getAttribute('baselevel'), 10) + levelAdj;
		node.setAttribute('level', adjusted);
		updateSpell(node);
	}

	filterLists();	// reapply any filter
}

function heightenChange() {
	'use strict';

	if (heightenDropdown.value === '0') {
		heightenCheckbox.checked = false;
	} else {
		heightenCheckbox.checked = true;
	}
	heightenCheckbox.setAttribute('level', heightenDropdown.value);
	metamagicChange();
}

// get the data that needs to be saved for each tab
function save() {
	'use strict';
	var output = {};
	var elems, i, slots, s;
	var tabOutput;
	var tabclass;

	elems = document.getElementsByClassName('tab');
	for (i = 0; i < elems.length; i++) {
		tabOutput = null;

		tabclass = elems[i].className.match(/(?:^|\s)(scribe|prepare|learn)(?!\S)/);
		if (!tabclass) {
			if (elems[i].id === 'tab_cast') {
				tabOutput = [];
				slots = document.getElementById('spellList');
				for (s = slots.firstElementChild; s !== null; s = s.nextElementSibling) {
					tabOutput.push({
						html: s.innerHTML
					});
				}
			}
		} else if (tabclass[1] === 'prepare' || tabclass[1] === 'learn') {
			tabOutput = [];
			slots = document.getElementById(elems[i].getAttribute('prefix')+'slotList');
			for (s = slots.firstElementChild; s !== null; s = s.nextElementSibling) {
				// currently saving used slots for learn tabs and locked slots for prepare tabs
				// probably need to save used slots for prepare tabs
				if (s.getAttribute('used') === 'true') {
					tabOutput.push({
						level: s.getAttribute('level'),
						locked: s.getAttribute('locked'),
						description: s.textContent
					});
				}
			}

		} else if (tabclass[1] === 'scribe') {
			tabOutput = [];
			slots = document.getElementById(elems[i].getAttribute('prefix')+'slotList');
			for (s = slots.firstElementChild; s !== null; s = s.nextElementSibling) {
				tabOutput.push({
					level: s.getAttribute('level'),
					description: s.textContent
				});
			}			
		}

		if (tabOutput !== null) {
			output[elems[i].id] = tabOutput;
		}
	}

	var req = new XMLHttpRequest();
	var url = new String(window.location);
	url = url.replace(/\/+$/, '');	// remove any trailing slashes
	url += document.getElementById('tab_cast').getAttribute('saveurl');
	req.open('PUT', url, true);
	req.setRequestHeader('Content-Type', 'application/json');
	req.onreadystatechange = function() {
		if (req.readyState === 4) {
			if (req.status !== 200) {
				alert("Error saving spells: "+req.statusText);
			}
		}
	};
	req.send(JSON.stringify(output));
}

function addSpell(text, superfix, italic) {
	'use strict';
	var s, sup;

	var spell = document.createElement('div');
	spell.className = 'selectable';
	spell.addEventListener('click', listItemClickHandler);

	var el = spell;
	if (italic) {
		el = document.createElement('i');
		spell.appendChild(el);
	}

	el.textContent = text;
	
	// add superfix if required
	if (superfix !== null && superfix.length > 0) {
		sup = document.createElement('sup');
		sup.textContent = '('+superfix+')';
		el.appendChild(sup);
	}
	
	// insert the spell in the correct place by level and name
	for (s = spellList.firstElementChild; s !== null; s = s.nextElementSibling) {
		if (s.textContent > spell.textContent) {
			spellList.insertBefore(spell, s);
			spell = null;	// to indicate we've placed the spell
			break;
		}
	}
	if (spell !== null) {
		spellList.appendChild(spell);	// haven't placed the spell yet, must be last
	}
}

// adds the spell in a slot to the spell list
function addSpellFromSlot(slot) {
	'use strict';

	var superfix = null;
	var tab = getParentTab(slot);
	if (tab !== null) {
		superfix = tab.getAttribute('superfix');
	}
	addSpell(slot.textContent, superfix, false);
}

// memorise() populates slots with the selected spells from the available list. a spell can go into a slot of
// equal or higher level. the spell also remains in the available list (a spell can be memorised more than once).
// memorised spells are also added to the spell list on the 'cast' tab
// this is the divine preparation system and is also how wizards memorise known spells.
// clearSlots() is the inverse function that removes spells from selected slots.
function memorise() {
	'use strict';
	var node, elems, level, slot, i;
	var prefix = '';

	// deselect all rows in the slot list
	for (node = slotList.firstElementChild; node !== null; node = node.nextElementSibling) {
		deselect(node);
	}

	// get the metamagic prefix
	elems = document.querySelectorAll('#'+getParentTab(slotList).id+' [name=metamagic]');
	for (i = 0; i < elems.length; i++) {
		if (elems[i].checked) {
			prefix += elems[i].value + ' ';
		}
	}

	for (node = availableList.firstElementChild; node !== null; node = node.nextElementSibling) {
		if (isSelected(node)) {
			level = node.getAttribute('level');

			// find the first empty (used = false) div in the slot list that is at least the same level
			for (slot = slotList.firstElementChild; slot !== null; slot = slot.nextElementSibling) {
				//alert('spell level '+level+', testing slot '+slot.textContent+': level = '+slot.getAttribute('level')+', used = '+slot.getAttribute('used')
				//	+'\nlevel test = '+(slot.getAttribute('level') >= level)+', used test = '+(slot.getAttribute('used') !== true));
				if (slot.getAttribute('level') >= level && slot.getAttribute('used') !== 'true') {
					slot.setAttribute('used', true);
					slot.setAttribute('locked', false);
					select(slot);
					slot.textContent = slot.getAttribute('level') + ' ' + prefix + node.getAttribute('spell');
					if (slot.getAttribute('level') !== level) {
						slot.textContent += ' ('+level+')';
					}
					deselect(node);
					
					lockButton.disabled = false;
					
					addSpellFromSlot(slot);
					break;
				}
			}
		}
	}
}

function clearSlots() {
	'use strict';
	var node, s, text;
	var unlocked = false;

	var superfix = null;
	var tab = getParentTab(slotList);
	if (tab !== null) {
		superfix = tab.getAttribute('superfix');
	}


	for (node = slotList.firstElementChild; node !== null; node = node.nextElementSibling) {
		if (isSelected(node)) {
			text = node.textContent;
			if (superfix) {
				text += '('+superfix+')';
			}

			// remove it from the spell list
			for (s = spellList.firstElementChild; s !== null; s = s.nextElementSibling) {
				if (s.textContent === text) {
					spellList.removeChild(s);
					break;
				}
			}

			deselect(node);
			node.textContent = node.textContent.substr(0,1);
			node.setAttribute('used', false);
			node.setAttribute('locked', true);
		}

		if (node.getAttribute('used') === 'true' && node.getAttribute('locked') === 'false') {
			unlocked = true;
		}
	}

	lockButton.disabled = !unlocked;
}

function sortList(list) {
	'use strict';
	var a = [], i;

	// remove all children into a
	while (list.firstElementChild) {
		a.push(list.firstElementChild);
		list.removeChild(list.firstElementChild);
	}

	// sort a
	a.sort(function (b,c) {
		if (b.getAttribute('level') !== c.getAttribute('level')) {
			return parseInt(b.getAttribute('level'), 10) - parseInt(c.getAttribute('level'), 10);
		}
		if (c.getAttribute('used') === 'false') { return -1; }		// anything beats an empty slot
		if (b.getAttribute('used') === 'false') { return 1; }
		return b.textContent.localeCompare(c.textContent);
	});
	
	// replace a
	for (i = 0; i < a.length; i++) {
		list.appendChild(a[i]);
	}
}

// learn() populates slots with the selected spells from the available list. a spell can only go into a slot
// or equal level. spells that are assigned to slots are removed from the available list (a spell cannot be
// learnt more than once). this is the arcane spontaneous caster system for generating a spell list.
// unlearn() is the inverse function that removes spells from selected slots.
function learn() {
	'use strict';
	var node, slot, level;

	// deselect all rows in the knwon list
	for (node = slotList.firstElementChild; node !== null; node = node.nextElementSibling) {
		deselect(node);
	}

	for (node = availableList.firstElementChild; node !== null; node = node.nextElementSibling) {
		if (isSelected(node)) {
			level = node.getAttribute('level');

			// find the first empty (used = false) div in the slot list that is at least the same level
			for (slot = slotList.firstElementChild; slot !== null; slot = slot.nextElementSibling) {
				//alert('spell level '+level+', testing slot '+slot.textContent+': level = '+slot.getAttribute('level')+', used = '+slot.getAttribute('used')
				//	+'\nlevel test = '+(slot.getAttribute('level') >= level)+', used test = '+(slot.getAttribute('used') !== true));
				if (slot.getAttribute('level') === level && slot.getAttribute('used') !== 'true') {
					slot.setAttribute('used', true);
					slot.setAttribute('locked', false);
					select(slot);
					slot.textContent = slot.getAttribute('level') + ' ' + node.getAttribute('spell');

					deselect(node);
					node.style.display = 'none';				// remove the spell from the full list
					node.setAttribute('always_hidden', true);		// ensure it doesn't get readded
					break;
				}
			}
		}
	}

	sortList(slotList);

	save();
}

function unlearn() {
	'use strict';
	var slot, node;

	for (slot = slotList.firstElementChild; slot !== null; slot = slot.nextElementSibling) {
		if (isSelected(slot)) {
			slot.setAttribute('used', false);
			slot.setAttribute('locked', true);
			deselect(slot);

			// make spell visible in full list again
			for (node = availableList.firstElementChild; node !== null; node = node.nextElementSibling) {
				if (slot.textContent === node.getAttribute('level') + ' ' + node.getAttribute('spell')) {
					node.style.display = null; // 'block';	// should check filter
					node.setAttribute('always_hidden', false);
					break;
				}
			}

			slot.textContent = slot.getAttribute('level');
		}
	}

	sortList(slotList);

	save();
}

// scribe() populates the known list with selected spells from the available list. spells that are assigned
// to the known list are removed from the available list (a spell cannot be learnt more than once). there
// are no restrictions on the number of spells known at each level. this is the wizard system for scribing
// spells into spellbooks.
// erase() is the inverse function that removes spells from the known list.
function scribe() {
	'use strict';
	var node, slot, listId, s;

	// deselect all rows in the knwon list
	for (node = slotList.firstElementChild; node !== null; node = node.nextElementSibling) {
		deselect(node);
	}

	for (node = availableList.firstElementChild; node !== null; node = node.nextElementSibling) {
		if (isSelected(node)) {
			slot = document.createElement('div');
			slot.className = 'selectable';
			slot.addEventListener('click', listItemClickHandler);
			slot.setAttribute('level', node.getAttribute('level'));
			slot.textContent = node.textContent;

			// put into sorted order
			for (s = slotList.firstElementChild; s !== null; s = s.nextElementSibling) {
				if (s.textContent > slot.textContent) {
					slotList.insertBefore(slot, s);
					break;
				}
			}
			if (s === null) {
				slotList.appendChild(slot);	// haven't placed the spell yet, must be last
			}

			select(slot);

			deselect(node);
			node.style.display = 'none';				// remove the spell from the full list
			node.setAttribute('always_hidden', true);		// ensure it doesn't get readded

			// set the spell as known in the available list on the memorise tab
			listId = getParentTab(slotList).getAttribute('memorise_tab_prefix')+'availableList';
			setLearnt(document.getElementById(listId), node, true);
		}
	}
	
	save();
}

function erase() {
	'use strict';
	var nextSlot, node, listId;

	var slot = slotList.firstElementChild;
	while (slot !== null) {
		nextSlot = slot.nextElementSibling;
		if (isSelected(slot)) {
			// make spell visible in full list again
			for (node = availableList.firstElementChild; node !== null; node = node.nextElementSibling) {
				if (slot.textContent === node.getAttribute('level') + ' ' + node.getAttribute('spell')) {
					node.style.display = null; // 'block';	// should check filter
					node.setAttribute('always_hidden', false);
					break;
				}
			}

			// set the spell as not known in the available list on the memorise tab
			listId = getParentTab(slotList).getAttribute('memorise_tab_prefix')+'availableList';
			setLearnt(document.getElementById(listId), slot, false);

			deselect(slot);
			slotList.removeChild(slot);
		}
		slot = nextSlot;
	}
	
	save();
}

// tab switching functionality
function switchToTab(tab) {
	'use strict';
	var i, node, unlocked;

	var elems = document.getElementsByClassName('tab');
	for (i = 0; i < elems.length; i++) {
		elems[i].style.display = 'none';
		document.getElementById('li_'+elems[i].id).className = '';
	}
	var currentTab = document.getElementById(tab.getAttribute('for'));
	currentTab.style.display = null; // 'block';
	tab.setAttribute('class', 'active');

	if (currentTab.className.match(/(?:^|\s)prepare(?!\S)/)) {
		// prepare tab, reset the globals
		tabPrefix = currentTab.getAttribute('prefix');
		availableList = document.getElementById(tabPrefix+'availableList');
		slotList = document.getElementById(tabPrefix+'slotList');
		heightenCheckbox = document.getElementById(tabPrefix+'heightenCheck');
		heightenDropdown = document.getElementById(tabPrefix+'heightenLevel');
		lockButton = document.getElementById(tabPrefix+'btnLock');

		// reset the controls as if this is the first time the tab has been used they may be incorrectly set
		if (heightenCheckbox) { heightenCheckbox.setAttribute('level', heightenDropdown.value); }
		metamagicChange();	// this will also reapply the filter
		updateButton(availableList);
		updateButton(slotList);

		// check status of lock button
		unlocked = false;
		for (node = slotList.firstElementChild; node !== null; node = node.nextElementSibling) {
			if (node.getAttribute('used') === 'true' && node.getAttribute('locked') === 'false') {
				unlocked = true;
			}
		}
		lockButton.disabled = !unlocked;

	} else if (currentTab.className.match(/(?:^|\s)learn(?!\S)/) || currentTab.className.match(/(?:^|\s)scribe(?!\S)/)) {
		// prepare tab, reset the globals
		tabPrefix = currentTab.getAttribute('prefix');
		availableList = document.getElementById(tabPrefix+'availableList');
		slotList = document.getElementById(tabPrefix+'slotList');

		filterLists();
		updateButton(availableList);
		updateButton(slotList);
	}
}

function lockSlots(list, dontSave) {
	'use strict';
	var node;
	var tab;

	for (node = list.firstElementChild; node !== null; node = node.nextElementSibling) {
		if (node.getAttribute('used') === 'true') {
			node.setAttribute('locked',true);
			node.style.fontStyle='italic';
			deselect(node);
		}
	}

	if (!dontSave) { save(); }

	// find the next prepare tab with unlocked slots and switch to it
	// if there are no more prepare tabs then switch to 'cast'
	for (tab = getParentTab(list).nextElementSibling; tab !== null; tab = tab.nextElementSibling) {
		if (tab.className.match(/(?:^|\s)tab(?!\S)/) && tab.className.match(/(?:^|\s)prepare(?!\S)/)) {
			node = document.getElementById(tab.getAttribute('prefix')+'slotList');
			for (node = node.firstElementChild; node !== null; node = node.nextElementSibling) {
				if (node.getAttribute('used') !== 'true') {
					// found an unlocked slot
					switchToTab(document.getElementById('li_'+tab.id));
					return;
				}
			}
		}
	}
	// didn't find another prepare tab, switch to cast
	switchToTab(document.getElementById('li_tab_cast'));
}

function castSpell() {
	'use strict';
	var node, elems, list, i, nextNode;

	// check for unlocked slots. if there are any then pop up an alert
	var unlocked = false;
	elems = document.getElementsByClassName('prepare');
	for (i = 0; i < elems.length; i++) {
		list = document.getElementById(elems[i].getAttribute('prefix')+'slotList');
		for (node = list.firstElementChild; node !== null; node = node.nextElementSibling) {
			if (node.getAttribute('used') === 'true' && node.getAttribute('locked') === 'false') {
				unlocked = true;
				break;
			}
		}
	}

	if (unlocked) {
		if (window.confirm('All unlocked slots will be locked.\nAre you sure you want to cast?')) {
			elems = document.getElementsByClassName('prepare');
			for (i = 0; i < elems.length; i++) {
				list = document.getElementById(elems[i].getAttribute('prefix')+'slotList');
				lockSlots(list, true);	// true -> don't save. will save later
			}
		} else {
			return;
		}
	}
	
	node = spellList.firstElementChild;
	while (node !== null) {
		nextNode = node.nextElementSibling;
		if (isSelected(node)) {
			spellList.removeChild(node);
		}
		node = nextNode;
	}
	
	save();
}

// implemeted as closure so ordinals doesn't need to be global
var rest = (function () {
	'use strict';

	var ordinals = ['th','st','nd','rd'];

	return function() {
		var elems, i, list, j, k, node, perDay, ordinal;
	
		// remove all nodes from the memorised list
		while (spellList.firstElementChild) {
			spellList.removeChild(spellList.firstElementChild);
		}
		
		// unlock all slots and rebuild the memorised list
		elems = document.getElementsByClassName('prepare');
		for (i = 0; i < elems.length; i++) {
			list = document.getElementById(elems[i].getAttribute('prefix')+'slotList');
			for (node = list.firstElementChild; node !== null; node = node.nextElementSibling) {
				if (node.getAttribute('used') === 'true') {
					node.setAttribute('locked',false);
					node.style.fontStyle='';
					addSpellFromSlot(node);
				}
			}
		}
	
		// reset spontaneous caster's spells per day
		elems = document.getElementsByClassName('learn');
		for (i = 0; i < elems.length; i++) {
			if (elems[i].hasAttribute('per_day')) {
				perDay = elems[i].getAttribute('per_day').split(',');
				for (j = 0; j < perDay.length; j++) {
					ordinal = 'th';
					if (j < 3) {
						ordinal = ordinals[j];
					}
					for (k = 0; k < perDay[j]; k++) {
						addSpell(j+ordinal+' level '+elems[i].getAttribute('superfix')+' spell', null, true);
					}
				}
			}
		}

		save();
		
		// return to first prepare tab
		elems = document.getElementsByClassName('tab');
		for (i = 0; i < elems.length; i++) {
			if (elems[i].className.match(/(?:^|\s)prepare(?!\S)/)) {
				switchToTab(document.getElementById('li_'+elems[i].id));
				break;
			}
		}
	};
}());

window.addEventListener('load', function() {
	'use strict';
	var elems, i, knownList, spell, slotList;

	// load extra stylesheet for non-ios browsers:
	if(!navigator.userAgent.match(/iPhone/i) && !navigator.userAgent.match(/iPad/i)) {
 		var fileref=document.createElement('link');
		fileref.setAttribute('rel', 'stylesheet');
		fileref.setAttribute('type', 'text/css');
		fileref.setAttribute('href', '/assistantdm/static/spelllist_nonios.css');
		document.getElementsByTagName('head')[0].appendChild(fileref);
	}
	// above can be replaced in php:
	//if(strstr($_SERVER['HTTP_USER_AGENT'],'iPhone') || strstr($_SERVER['HTTP_USER_AGENT'],'iPod'))

	// prevent rubber-banding but preventing scrolling on iOS
//	document.ontouchmove = function(event) {
//		event.preventDefault();
//	}

	// for elements that should be scrollable prevent event propogation and apply the ScrollFix hack
//	var elems = document.getElementsByClassName('scrollable');
//	for (i = 0; i < elems.length; i++) {
//		elems[i].addEventListener('touchmove', function(event) {
//			// we should stop progagation only if this container is currently scrollable (not if it's not overflowing)
//			if (event.currentTarget.scrollHeight >= event.currentTarget.offsetHeight)
//				event.stopPropagation();
//		});
//		new ScrollFix(elems[i]);
//	}

	// add the click handler for selectable list items
	elems = document.getElementsByClassName('selectable');
	for (i = 0; i < elems.length; i++) {
		elems[i].addEventListener('click', listItemClickHandler);
	}

	// add the event handler needed for metamagic changes
	elems = document.getElementsByName('metamagic');
	for (i = 0; i < elems.length; i++) {
		elems[i].addEventListener('change', metamagicChange);
	}

	// setup global element pointers
	spellList = document.getElementById('spellList');

	// setup spell list(s) correctly
	setupSpellLists(document.getElementsByClassName('prepare'));
	setupSpellLists(document.getElementsByClassName('learn'));
	setupSpellLists(document.getElementsByClassName('scribe'));

	// setup known spells for wizards
	elems = document.getElementsByClassName('scribe');
	for (i = 0; i < elems.length; i++) {
		if (elems[i].hasAttribute('memorise_tab_prefix')) {
			// first reset all spells to not known
			knownList = document.getElementById(elems[i].getAttribute('memorise_tab_prefix')+'availableList');
			for (spell = knownList.firstElementChild; spell !== null; spell = spell.nextElementSibling) {
				spell.setAttribute('always_hidden', true);
			}

			// set known spells to learnt
			slotList = document.getElementById(elems[i].getAttribute('prefix')+'slotList');
			for (spell = slotList.firstElementChild; spell !== null; spell = spell.nextElementSibling) {
				setLearnt(knownList, spell, true);
			}
		}
	}

	// build the tabs
	var tabsList = document.createElement('ul');
	var first = null;
	var tab, link;
	var onclick = function() {switchToTab(this);};
	elems = document.getElementsByClassName('tab');
	for (i = 0; i < elems.length; i++) {
		tab = document.createElement('li');
		tab.setAttribute('for',elems[i].id);
		tab.id = 'li_'+elems[i].id;
		tab.onclick = onclick;
		link = document.createElement('a');
		link.textContent = elems[i].getAttribute('name');
		tab.appendChild(link);
		tabsList.appendChild(tab);
		if (first === null) {
			first = tab;
		}
	}
	document.getElementById('tabs').appendChild(tabsList);
	if (first !== null) {
		switchToTab(first);
	}
});
