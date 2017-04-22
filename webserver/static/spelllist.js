/*jslint browser: true, plusplus: true, todo: true, vars: true, white: true */

/* TODO
* if metamagic is applied we could filter slots that can't be used (though maybe it's better to show them)
* wizard spells that are not in the spell book should be cleared from their slots when resting
*/

(function($) {
var ordinals = ['th','st','nd','rd'];

// frequently accessed elements (these are set on load and shouldn't ever change)
var spellList;
var castList;
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
	var button, buttons, i;

	if (list.hasAttribute('button')) {
		buttons = list.getAttribute('button').split(/\s+/);
		for (i in buttons) {
			button = document.getElementById(buttons[i]);
			if (button) {
				button.disabled = $(list).has('.selectable.selected').length == 0;
			}
		}
	}
}

function deselect(element) {
	'use strict';

	$(element).removeClass('selected');
	updateButton($('[button]').has(element)[0]);	// argument is the first ancestor with a button attribute
}

function select(element) {
	'use strict';

	$(element).addClass('selected');
	updateButton($('[button]').has(element)[0]);	// argument is the first ancestor with a button attribute
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
	var i, slots, s;

	output['tab_cast'] = $('#spellList div').map(function() {
		return { html: this.innerHTML };
	}).get();

	output['castList'] = $('#castList div').map(function() {
		return { html: this.innerHTML };
	}).get();

	output['dailies'] = $('#tab_cast tr[max]').map(function(i,e) {
		return {
			name: $(e).children('td').first().text(),
			max: $(e).children('td').last().children('input[type="checkbox"]').length,
			used: $(e).children('td').last().children('input[type="checkbox"]:checked').length
		};
	}).get();

	output['charges'] = 	$('#tab_cast td input[value="\u25b2"]').siblings('input[type="text"]').map(function(i,e) {
		return {
			name: $(e).parent().siblings().text(),
			remain: $(e).val()
		};
	}).get();

	$('.tab.prepare,.tab.learn').each(function(i,e) {
		output[e.id] = $('#'+e.getAttribute('prefix')+'slotList div[used="true"]').map(function() {
			return {
				level: this.getAttribute('level'),
				locked: this.getAttribute('locked'),
				description: this.textContent
			};
		}).get();
	});

	$('.tab.scribe').each(function(i,e) {
		output[e.id] = $('#'+e.getAttribute('prefix')+'slotList div').map(function() {
			return {
				level: this.getAttribute('level'),
				description: this.textContent
			};
		}).get();
	});

	var a = document.createElement('a');
	a.href = new String(window.location);
	a.pathname = document.getElementById('tab_cast').getAttribute('saveurl');

	var req = new XMLHttpRequest();
	req.open('PUT', a.href, true);
	req.setRequestHeader('Content-Type', 'application/json');
	req.onreadystatechange = function() {
		if (req.readyState === 4) {
			if (req.status !== 200) {
				alert("Error saving spells: "+req.statusText);
			}
		}
	};
	req.send(JSON.stringify(output, null, '\t'));
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
			// move to cast list and add double click handler
			castList.insertBefore(node, castList.firstChild);
			$(node).dblclick(addNote);
			deselect(node);	// this will update the buttons. if we didn't deselect we'd need to update the buttons explicitly
		}
		node = nextNode;
	}
	
	save();
}

function addNote() {
	$(this)
		.append($('<br>'))
		.append($('<input type="text">').on('change', noteChanged));
}

function noteChanged(e) {
	$(e.target).attr('value', $(e.target).val());	// set the value attribute so the note text gets saved
	save();
}

function deleteCast() {
	'use strict';
	var node, nextNode;

	node = castList.firstElementChild;
	while (node !== null) {
		nextNode = node.nextElementSibling;
		if (isSelected(node)) {
			castList.removeChild(node);
		}
		node = nextNode;
	}

	save();
	updateButton(castList);
}

function uncastSpell() {
	'use strict';
	var node, nextNode;

	node = castList.firstElementChild;
	while (node !== null) {
		nextNode = node.nextElementSibling;
		if (isSelected(node)) {
			castList.removeChild(node);
			$(node)
				.off('dblclick')
				.find('br').remove();
			$(node)
				.find('input').remove();
			spellList.insertBefore(node, spellList.firstChild);
			deselect(node);	// this will update the buttons. if we didn't deselect we'd need to update the buttons explicitly
		}
		node = nextNode;
	}

	sortList(spellList);

	save();
	updateButton(castList);
}

function rest() {
	'use strict';

	$(spellList).empty();		// remove all nodes from the memorised list
	
	// unlock all slots and rebuild the memorised list
	$('.tab.prepare .slotlist div[used="true"]')
		.attr('locked','false')
		.css('font-style','')
		.each(function(i,e) {
			addSpellFromSlot(e);
		});

	// reset spontaneous caster's spells per day
	$('.tab.learn[per_day]').each(function(i,e) {
		var j, k, ordinal, perDay;
		perDay = e.getAttribute('per_day').split(',');
		for (j = 0; j < perDay.length; j++) {
			ordinal = ordinals[0];
			if (j < 3) {
				ordinal = ordinals[j];
			}
			for (k = 0; k < perDay[j]; k++) {
				addSpell(j+ordinal+' level '+e.getAttribute('superfix')+' spell', null, true);
			}
		}
	});

	// reset daily abilities
	$('#tab_cast tr[max] input[type="checkbox"]').each(function(i,e) {
		$(e).prop('checked', false);
	});

	// remove all cast spells
	$('#castList').empty();
	updateButton(castList);

	save();
	
	// return to first prepare tab
	$('.tab.prepare').each(function(i,e) {
		switchToTab(document.getElementById('li_'+e.id));
		return false;
	});
}

function deleteAbility() {
	if( confirm('Are you sure you want to delete the selected abilities/items?')) {
		$('table.abilities tr.selected').remove();
		save();
		updateButton($('table.abilities')[0]);
	}
}

function newItem() {
	var name = prompt('Enter name of new item', '');
	if (name) {
		var num = parseInt(prompt('Enter number of uses', '0'));
		if (isNaN(num)) num = 0;
		
		var tr = $('<tr/>').addClass('selectable').on('click', listItemClickHandler);
		$('<td/>').text(name).appendTo(tr);
		var td = $('<td/>');
		$('<input/>', {type: 'text', value: num})
			.prop('size', '4')
			.on('click', usesChanged)
			.on('click', function(e) {e.stopPropagation();})
			.appendTo(td);
		$('<input/>', {type: 'button', value: '\u25b2'}).on('click', increaseUses).appendTo(td);
		$('<input/>', {type: 'button', value: '\u25bc'}).on('click', decreaseUses).appendTo(td);
		td.appendTo(tr);
		var last = $('table').has('#btnItem').append(tr);
		save();
	}
}

function newAbility() {
	var name = prompt('Enter name of new daily ability', '');
	if (name) {
		var num = parseInt(prompt('Enter number of uses per day', '1'));
		if (isNaN(num)) num = 1;

		var tr = $('<tr/>', {used: 0, max: num})
			.addClass('selectable')
				.on('change',save)
				.on('click', function(e) {e.stopPropagation();});
		$('<td/>').text(name).appendTo(tr);
		var td = $('<td/>');
		for (var i = 0; i < num; i++) {
			$('<input/>', {type: 'checkbox'})
				.on('change',save)
				.on('click', function(e) {e.stopPropagation();})	// prevents parent row from selecting/deselecting
				.appendTo(td);
		}
		td.appendTo(tr);
		$('tr').has('#btnItem').before(tr);
		save();
	}
}

// item usage event handlers:

function increaseUses(e) {
	e.stopPropagation();
	var control = $(e.target).siblings('input[type="text"]');
	control.val(parseInt(control.val())+1);
	save();
}

function decreaseUses(e) {
	e.stopPropagation();
	var control = $(e.target).siblings('input[type="text"]');
	var val = parseInt(control.val());
	if (val > 0) {
		val--;
		control.val(val);
		save();
	}
}

function usesChanged(e) {
	var val = parseInt($(e.target).val(), 10);
	if (isNaN(val)) val = 0;
	if ($(e.target).val() !== val.toString()) {
		$(e.target).val(val);
		save();
	}
}

function updateEffects(xml) {
	var $xml = $(xml);
	$('#effectsList').empty();
	var $buffs = $xml.find('Buffs > Buff');
	$buffs.each(function(i) {
		var name = $(this).attr('name');
		var $mods = $(this).find('Modifier');
		var details = [];
		$mods.each(function(j) {
			details.push($(this).attr('description').replace(' (from '+name+')', ''));
		});
		$('<div class="selectable">'+name+'</div>')
			.append($('<pre>').text(details.join('\n')))
			.appendTo($('#effectsList'));
	});
}

$(document).ready(function() {
	'use strict';
	var i, knownList, spell, slotList;

	$('#effectsList').data('updater', updateEffects);	// attach the function that processes the buffs data attached to the character so the updater code can find it

	// TODO probably better to do this by detecting lack of "ontouchstart"
	// load extra stylesheet for non-ios browsers:
	if(!navigator.userAgent.match(/iPhone/i) && !navigator.userAgent.match(/iPad/i)) {
 		var fileref=document.createElement('link');
		fileref.setAttribute('rel', 'stylesheet');
		fileref.setAttribute('type', 'text/css');
		fileref.setAttribute('href', '/assistantdm/static/spelllist_nonios.css');
		document.getElementsByTagName('head')[0].appendChild(fileref);
	}

	$('#btnSpells').on('click', castSpell);
	$('#btnRest').on('click', rest);
	$('#btnAbility').on('click', newAbility);
	$('#btnItem').on('click', newItem);
	$('#btnDeleteAbility').on('click', deleteAbility);
	$('#btnDeleteCast').on('click', deleteCast);
	$('#btnUncast').on('click', uncastSpell);

	// event handlers for prepare tabs
	$('.tab.prepare').each(function(i,e) {
		var prefix = '#'+e.getAttribute('prefix');
		$(prefix+'levelFilter').on('change', filterLists);
		$(prefix+'showAll').on('change', filterLists);
		$(prefix+'heightenLevel').on('change', heightenChange);
		$(prefix+'btnMemorise').on('click', memorise);
		$(prefix+'btnClear').on('click', clearSlots);
		$(prefix+'btnLock').on('click', function(ev){
			lockSlots(document.getElementById(e.getAttribute('prefix')+'slotList'));
		});
	});
	
	// event handlers for scribe tabs
	$('.tab.scribe').each(function(i,e) {
		var prefix = '#'+e.getAttribute('prefix');
		$(prefix+'levelFilter').on('change', filterLists);
		$(prefix+'btnScribe').on('click', scribe);
		$(prefix+'btnErase').on('click', erase);
	});

	// event handlers for learn tabs
	$('.tab.learn').each(function(i,e) {
		var prefix = '#'+e.getAttribute('prefix');
		$(prefix+'levelFilter').on('change', filterLists);
		$(prefix+'btnLearn').on('click', learn);
		$(prefix+'btnUnlearn').on('click', unlearn);
	});

	// add the click handler for selectable list items
	$('.selectable').on('click', listItemClickHandler);

	// events for cast spells
	$('#castList .selectable')
		.on('dblclick', addNote)
		.find('input')
			.on('change', noteChanged);
	$('#castList input[value=""]').remove();	// remove empty notes

	// add the event handler needed for metamagic changes
	$("input[name='metamagic']").on('change', metamagicChange);

	// setup global element pointers
	spellList = document.getElementById('spellList');
	castList = document.getElementById('castList');

	// setup spell list(s) correctly
	setupSpellLists(document.getElementsByClassName('prepare'));
	setupSpellLists(document.getElementsByClassName('learn'));
	setupSpellLists(document.getElementsByClassName('scribe'));

	// setup known spells for wizards
	$('.tab.scribe[memorise_tab_prefix]').each(function(i,e) {
		// first reset all spells to not known
		knownList = document.getElementById(e.getAttribute('memorise_tab_prefix')+'availableList');
		for (spell = knownList.firstElementChild; spell !== null; spell = spell.nextElementSibling) {
			spell.setAttribute('always_hidden', true);
		}

		// set known spells to learnt
		slotList = document.getElementById(e.getAttribute('prefix')+'slotList');
		for (spell = slotList.firstElementChild; spell !== null; spell = spell.nextElementSibling) {
			setLearnt(knownList, spell, true);
		}
	});

	// setup check boxes for dailies
	$('#tab_cast tr[max]').each(function(i,e) {
		var j;
		var td = $(e).children('td').last();
		var max = parseInt($(e).attr('max'));
		var used = parseInt($(e).attr('used'));
		for (j = 0; j < max; j++) {
			$('<input/>', {type: 'checkbox', checked: (j < used)})
				.on('change',save)
				.on('click', function(e) {e.stopPropagation();})	// prevents parent row from selecting/deselecting
				.appendTo(td);
		}
	});

	// setup events for other usage tracking controls - arrow buttons and input validation
	// force the button to enabled as we sometimes see them disabled by default (maybe firefox plugin causing this?)
	$('#tab_cast td input[value="\u25b2"]').on('click', increaseUses).prop('disabled',false);
	$('#tab_cast td input[value="\u25bc"]').on('click', decreaseUses).prop('disabled',false);
	$('#tab_cast td input[value="\u25b2"]').siblings('input[type="text"]')
		.on('change', usesChanged)
		.on('click', function(e) {e.stopPropagation();});

	// build the tabs
	var tabsList = document.createElement('ul');
	var first = null;
	var tab, link;
	var onclick = function() {switchToTab(this);};
	$('.tab').each(function(i,e) {
		tab = document.createElement('li');
		tab.setAttribute('for',e.id);
		tab.id = 'li_'+e.id;
		tab.onclick = onclick;
		link = document.createElement('a');
		link.textContent = e.getAttribute('name');
		tab.appendChild(link);
		tabsList.appendChild(tab);
		if (first === null) {
			first = tab;
		}
	});
	document.getElementById('tabs').appendChild(tabsList);
	if (first !== null) {
		switchToTab(first);
	}
});

}(jQuery));
