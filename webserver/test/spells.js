/*jslint node: true, plusplus: true, todo: true, vars: true, white: true */

/*
TODO should require ability >= 10 (11 for those with no 0 level spells) to cast at all
TODO wizard specialisation

wizard: 
specialist can memorise one more per spell level (though at least one must be from spec school)
specialist loses two schools (one for diviner) (can't lose divination)
*/

var fs = require('fs');

var clericSlots = [
	[3,1],
	[4,2],
	[4,2,1],
	[5,3,2],
	[5,3,2,1],
	[5,3,3,2],
	[6,4,3,2,1],
	[6,4,3,3,2],
	[6,4,4,3,2,1],
	[6,4,4,3,3,2],
	[6,5,4,4,3,2,1],
	[6,5,4,4,3,3,2],
	[6,5,5,4,4,3,2,1],
	[6,5,5,4,4,3,3,2],
	[6,5,5,5,4,4,3,2,1],
	[6,5,5,5,4,4,3,3,2],
	[6,5,5,5,5,4,4,3,2,1],
	[6,5,5,5,5,4,4,3,3,2],
	[6,5,5,5,5,5,4,4,3,3],
	[6,5,5,5,5,5,4,4,4,4]
];

var paladinSlots = [
	[0],
	[0],
	[0],
	[0,0],
	[0,0],
	[0,1],
	[0,1],
	[0,1,0],
	[0,1,0],
	[0,1,1],
	[0,1,1,0],
	[0,1,1,1],
	[0,1,1,1],
	[0,2,1,1,0],
	[0,2,1,1,1],
	[0,2,2,1,1],
	[0,2,2,2,1],
	[0,3,2,2,1],
	[0,3,3,3,2],
	[0,3,3,3,3]
];

var sorcererPerDay = [
	[5,3],
	[6,4],
	[6,5],
	[6,6,3],
	[6,6,4],
	[6,6,5,3],
	[6,6,6,4],
	[6,6,6,5,3],
	[6,6,6,6,4],
	[6,6,6,6,5,3],
	[6,6,6,6,6,4],
	[6,6,6,6,6,5,3],
	[6,6,6,6,6,6,4],
	[6,6,6,6,6,6,5,3],
	[6,6,6,6,6,6,6,4],
	[6,6,6,6,6,6,6,5,3],
	[6,6,6,6,6,6,6,6,4],
	[6,6,6,6,6,6,6,6,5,3],
	[6,6,6,6,6,6,6,6,6,4],
	[6,6,6,6,6,6,6,6,6,6]
];

var sorcererKnown = [
	[4,2],
	[5,2],
	[5,3],
	[6,3,1],
	[6,4,2],
	[7,4,2,1],
	[7,5,3,2],
	[8,5,3,2,1],
	[8,5,4,3,2],
	[9,5,4,3,2,1],
	[9,5,5,4,3,2],
	[9,5,5,4,3,2,1],
	[9,5,5,4,4,3,2],
	[9,5,5,4,4,3,2,1],
	[9,5,5,4,4,4,3,2],
	[9,5,5,4,4,4,3,2,1],
	[9,5,5,4,4,4,3,3,2],
	[9,5,5,4,4,4,3,3,2,1],
	[9,5,5,4,4,4,3,3,3,2],
	[9,5,5,4,4,4,3,3,3,3]
];

var bardPerDay = [
	[2],
	[3,0],
	[3,1],
	[3,2,0],
	[3,3,1],
	[3,3,2],
	[3,3,2,0],
	[3,3,3,1],
	[3,3,3,2],
	[3,3,3,2,0],
	[3,3,3,3,1],
	[3,3,3,3,2],
	[3,3,3,3,2,0],
	[4,3,3,3,3,1],
	[4,4,3,3,3,2],
	[4,4,4,3,3,2,0],
	[4,4,4,4,3,3,1],
	[4,4,4,4,4,3,2],
	[4,4,4,4,4,4,3],
	[4,4,4,4,4,4,4]
];

var bardKnown = [
	[4],
	[5,2],
	[6,3],
	[6,3,2],
	[6,4,3],
	[6,4,3],
	[6,4,4,2],
	[6,4,4,3],
	[6,4,4,3],
	[6,4,4,4,2],
	[6,4,4,4,3],
	[6,4,4,4,3],
	[6,4,4,4,4,2],
	[6,4,4,4,4,3],
	[6,4,4,4,4,3],
	[6,5,4,4,4,4,2],
	[6,5,5,4,4,4,3],
	[6,5,5,5,4,4,3],
	[6,5,5,5,5,4,4],
	[6,5,5,5,5,5,4]
];

var wizardSlots = [
	[3,1],
	[4,2],
	[4,2,1],
	[4,3,2],
	[4,3,2,1],
	[4,3,3,2],
	[4,4,3,2,1],
	[4,4,3,3,2],
	[4,4,4,3,2,1],
	[4,4,4,3,3,2],
	[4,4,4,4,3,2,1],
	[4,4,4,4,3,3,2],
	[4,4,4,4,4,3,2,1],
	[4,4,4,4,4,3,3,2],
	[4,4,4,4,4,4,3,2,1],
	[4,4,4,4,4,4,3,3,2],
	[4,4,4,4,4,4,4,3,2,1],
	[4,4,4,4,4,4,4,3,3,2],
	[4,4,4,4,4,4,4,4,3,3],
	[4,4,4,4,4,4,4,4,4,4]
];


var clericSpells = [
	{ level: '0', name: 'Create Water' },
	{ level: '0', name: 'Cure Minor Wounds' },
	{ level: '0', name: 'Detect Magic' },
	{ level: '0', name: 'Detect Poison' },
	{ level: '0', name: 'Guidance' },
	{ level: '0', name: 'Inflict Minor Wounds' },
	{ level: '0', name: 'Light' },
	{ level: '0', name: 'Mending' },
	{ level: '0', name: 'Purify Food and Drink' },
	{ level: '0', name: 'Read Magic' },
	{ level: '0', name: 'Resistance' },
	{ level: '0', name: 'Virtue' },
	{ level: '1', name: 'Bane' },
	{ level: '1', name: 'Bless' },
	{ level: '1', name: 'Bless Water' },
	{ level: '1', name: 'Cause Fear' },
	{ level: '1', name: 'Command' },
	{ level: '1', name: 'Comprehend Languages' },
	{ level: '1', name: 'Cure Light Wounds' },
	{ level: '1', name: 'Curse Water' },
	{ level: '1', name: 'Deathwatch' },
	{ level: '1', name: 'Detect Chaos' },
	{ level: '1', name: 'Detect Evil' },
	{ level: '1', name: 'Detect Good' },
	{ level: '1', name: 'Detect Law' },
	{ level: '1', name: 'Detect Undead' },
	{ level: '1', name: 'Divine Favor' },
	{ level: '1', name: 'Doom' },
	{ level: '1', name: 'Endure Elements' },
	{ level: '1', name: 'Entropic Shield' },
	{ level: '1', name: 'Hide from Undead' },
	{ level: '1', name: 'Inflict Light Wounds' },
	{ level: '1', name: 'Magic Stone' },
	{ level: '1', name: 'Magic Weapon' },
	{ level: '1', name: 'Obscuring Mist' },
	{ level: '1', name: 'Protection from Chaos' },
	{ level: '1', name: 'Protection from Evil' },
	{ level: '1', name: 'Protection from Good' },
	{ level: '1', name: 'Protection from Law' },
	{ level: '1', name: 'Remove Fear' },
	{ level: '1', name: 'Sanctuary' },
	{ level: '1', name: 'Shield of Faith' },
	{ level: '1', name: 'Summon Monster I' },
	{ level: '2', name: 'Aid' },
	{ level: '2', name: 'Align Weapon' },
	{ level: '2', name: 'Augury' },
	{ level: '2', name: "Bear's Endurance" },
	{ level: '2', name: "Bull's Strength" },
	{ level: '2', name: 'Calm Emotions' },
	{ level: '2', name: 'Consecrate' },
	{ level: '2', name: 'Cure Moderate Wounds' },
	{ level: '2', name: 'Darkness' },
	{ level: '2', name: 'Death Knell' },
	{ level: '2', name: 'Delay Poison' },
	{ level: '2', name: 'Desecrate' },
	{ level: '2', name: "Eagle's Splendor" },
	{ level: '2', name: 'Enthrall' },
	{ level: '2', name: 'Find Traps' },
	{ level: '2', name: 'Gentle Repose' },
	{ level: '2', name: 'Hold Person' },
	{ level: '2', name: 'Inflict Moderate Wounds' },
	{ level: '2', name: 'Make Whole' },
	{ level: '2', name: "Owl's Wisdom" },
	{ level: '2', name: 'Remove Paralysis' },
	{ level: '2', name: 'Resist Energy' },
	{ level: '2', name: 'Restoration, Lesser' },
	{ level: '2', name: 'Shatter' },
	{ level: '2', name: 'Shield Other' },
	{ level: '2', name: 'Silence' },
	{ level: '2', name: 'Sound Burst' },
	{ level: '2', name: 'Spiritual Weapon' },
	{ level: '2', name: 'Status' },
	{ level: '2', name: 'Summon Monster II' },
	{ level: '2', name: 'Undetectable Alignment' },
	{ level: '2', name: 'Zone of Truth' },
	{ level: '3', name: 'Animate Dead' },
	{ level: '3', name: 'Bestow Curse' },
	{ level: '3', name: 'Blindness/Deafness' },
	{ level: '3', name: 'Contagion' },
	{ level: '3', name: 'Continual Flame' },
	{ level: '3', name: 'Create Food and Water' },
	{ level: '3', name: 'Cure Serious Wounds' },
	{ level: '3', name: 'Daylight' },
	{ level: '3', name: 'Deeper Darkness' },
	{ level: '3', name: 'Dispel Magic' },
	{ level: '3', name: 'Glyph of Warding' },
	{ level: '3', name: 'Helping Hand' },
	{ level: '3', name: 'Inflict Serious Wounds' },
	{ level: '3', name: 'Invisibility Purge' },
	{ level: '3', name: 'Locate Object' },
	{ level: '3', name: 'Magic Circle against Chaos' },
	{ level: '3', name: 'Magic Circle against Evil' },
	{ level: '3', name: 'Magic Circle against Good' },
	{ level: '3', name: 'Magic Circle against Law' },
	{ level: '3', name: 'Magic Vestment' },
	{ level: '3', name: 'Meld into Stone' },
	{ level: '3', name: 'Obscure Object' },
	{ level: '3', name: 'Prayer' },
	{ level: '3', name: 'Protection from Energy' },
	{ level: '3', name: 'Remove Blindness/Deafness' },
	{ level: '3', name: 'Remove Curse' },
	{ level: '3', name: 'Remove Disease' },
	{ level: '3', name: 'Searing Light' },
	{ level: '3', name: 'Speak with Dead' },
	{ level: '3', name: 'Stone Shape' },
	{ level: '3', name: 'Summon Monster III' },
	{ level: '3', name: 'Water Breathing' },
	{ level: '3', name: 'Water Walk' },
	{ level: '3', name: 'Wind Wall' },
	{ level: '4', name: 'Air Walk' },
	{ level: '4', name: 'Control Water' },
	{ level: '4', name: 'Cure Critical Wounds' },
	{ level: '4', name: 'Death Ward' },
	{ level: '4', name: 'Dimensional Anchor' },
	{ level: '4', name: 'Discern Lies' },
	{ level: '4', name: 'Dismissal' },
	{ level: '4', name: 'Divination' },
	{ level: '4', name: 'Divine Power' },
	{ level: '4', name: 'Freedom of Movement' },
	{ level: '4', name: 'Giant Vermin' },
	{ level: '4', name: 'Imbue with Spell Ability' },
	{ level: '4', name: 'Inflict Critical Wounds' },
	{ level: '4', name: 'Magic Weapon, Greater' },
	{ level: '4', name: 'Neutralize Poison' },
	{ level: '4', name: 'Planar Ally, Lesser' },
	{ level: '4', name: 'Poison' },
	{ level: '4', name: 'Repel Vermin' },
	{ level: '4', name: 'Restoration' },
	{ level: '4', name: 'Sending' },
	{ level: '4', name: 'Spell Immunity' },
	{ level: '4', name: 'Summon Monster IV' },
	{ level: '4', name: 'Tongues' },
	{ level: '5', name: 'Atonement' },
	{ level: '5', name: 'Break Enchantment' },
	{ level: '5', name: 'Command, Greater' },
	{ level: '5', name: 'Commune' },
	{ level: '5', name: 'Cure Light Wounds, Mass' },
	{ level: '5', name: 'Dispel Chaos' },
	{ level: '5', name: 'Dispel Evil' },
	{ level: '5', name: 'Dispel Good' },
	{ level: '5', name: 'Dispel Law' },
	{ level: '5', name: 'Disrupting Weapon' },
	{ level: '5', name: 'Flame Strike' },
	{ level: '5', name: 'Hallow' },
	{ level: '5', name: 'Inflict Light Wounds, Mass' },
	{ level: '5', name: 'Insect Plague' },
	{ level: '5', name: 'Mark of Justice' },
	{ level: '5', name: 'Plane Shift' },
	{ level: '5', name: 'Raise Dead' },
	{ level: '5', name: 'Righteous Might' },
	{ level: '5', name: 'Scrying' },
	{ level: '5', name: 'Slay Living' },
	{ level: '5', name: 'Spell Resistance' },
	{ level: '5', name: 'Summon Monster V' },
	{ level: '5', name: 'Symbol of Pain' },
	{ level: '5', name: 'Symbol of Sleep' },
	{ level: '5', name: 'True Seeing' },
	{ level: '5', name: 'Unhallow' },
	{ level: '5', name: 'Wall of Stone' },
	{ level: '6', name: 'Animate Objects' },
	{ level: '6', name: 'Antilife Shell' },
	{ level: '6', name: 'Banishment' },
	{ level: '6', name: "Bear's Endurance, Mass" },
	{ level: '6', name: 'Blade Barrier' },
	{ level: '6', name: "Bull's Strength, Mass" },
	{ level: '6', name: 'Create Undead' },
	{ level: '6', name: 'Cure Moderate Wounds, Mass' },
	{ level: '6', name: 'Dispel Magic, Greater' },
	{ level: '6', name: "Eagle's Splendor, Mass" },
	{ level: '6', name: 'Find the Path' },
	{ level: '6', name: 'Forbiddance' },
	{ level: '6', name: 'Geas/Quest' },
	{ level: '6', name: 'Glyph of Warding, Greater' },
	{ level: '6', name: 'Harm' },
	{ level: '6', name: 'Heal' },
	{ level: '6', name: "Heroes' Feast" },
	{ level: '6', name: 'Inflict Moderate Wounds, Mass' },
	{ level: '6', name: "Owl's Wisdom, Mass" },
	{ level: '6', name: 'Planar Ally' },
	{ level: '6', name: 'Summon Monster VI' },
	{ level: '6', name: 'Symbol of Fear' },
	{ level: '6', name: 'Symbol of Persuasion' },
	{ level: '6', name: 'Undeath to Death' },
	{ level: '6', name: 'Wind Walk' },
	{ level: '6', name: 'Word of Recall' },
	{ level: '7', name: 'Blasphemy' },
	{ level: '7', name: 'Control Weather' },
	{ level: '7', name: 'Cure Serious Wounds, Mass' },
	{ level: '7', name: 'Destruction' },
	{ level: '7', name: 'Dictum' },
	{ level: '7', name: 'Ethereal Jaunt' },
	{ level: '7', name: 'Holy Word' },
	{ level: '7', name: 'Inflict Serious Wounds, Mass' },
	{ level: '7', name: 'Refuge' },
	{ level: '7', name: 'Regenerate' },
	{ level: '7', name: 'Repulsion' },
	{ level: '7', name: 'Restoration, Greater' },
	{ level: '7', name: 'Resurrection' },
	{ level: '7', name: 'Scrying, Greater' },
	{ level: '7', name: 'Summon Monster VII' },
	{ level: '7', name: 'Symbol of Stunning' },
	{ level: '7', name: 'Symbol of Weakness' },
	{ level: '7', name: 'Word of Chaos' },
	{ level: '8', name: 'Antimagic Field' },
	{ level: '8', name: 'Cloak of Chaos' },
	{ level: '8', name: 'Create Greater Undead' },
	{ level: '8', name: 'Cure Critical Wounds, Mass' },
	{ level: '8', name: 'Dimensional Lock' },
	{ level: '8', name: 'Discern Location' },
	{ level: '8', name: 'Earthquake' },
	{ level: '8', name: 'Fire Storm' },
	{ level: '8', name: 'Holy Aura' },
	{ level: '8', name: 'Planar Ally, Greater' },
	{ level: '8', name: 'Inflict Critical Wounds, Mass' },
	{ level: '8', name: 'Shield of Law' },
	{ level: '8', name: 'Spell Immunity, Greater' },
	{ level: '8', name: 'Summon Monster VIII' },
	{ level: '8', name: 'Symbol of Death' },
	{ level: '8', name: 'Symbol of Insanity' },
	{ level: '8', name: 'Unholy Aura' },
	{ level: '9', name: 'Astral Projection' },
	{ level: '9', name: 'Energy Drain' },
	{ level: '9', name: 'Etherealness' },
	{ level: '9', name: 'Gate' },
	{ level: '9', name: 'Heal, Mass' },
	{ level: '9', name: 'Implosion' },
	{ level: '9', name: 'Miracle' },
	{ level: '9', name: 'Soul Bind' },
	{ level: '9', name: 'Storm of Vengeance' },
	{ level: '9', name: 'Summon Monster IX' },
	{ level: '9', name: 'True Resurrection' }
];

var druidSpells = [
	{ level: '0', name: 'Create Water' },
	{ level: '0', name: 'Cure Minor Wounds' },
	{ level: '0', name: 'Detect Magic' },
	{ level: '0', name: 'Detect Poison' },
	{ level: '0', name: 'Flare' },
	{ level: '0', name: 'Guidance' },
	{ level: '0', name: 'Know Direction' },
	{ level: '0', name: 'Light' },
	{ level: '0', name: 'Mending' },
	{ level: '0', name: 'Purify Food and Drink' },
	{ level: '0', name: 'Read Magic' },
	{ level: '0', name: 'Resistance' },
	{ level: '0', name: 'Virtue' },
	{ level: '1', name: 'Calm Animals' },
	{ level: '1', name: 'Charm Animal' },
	{ level: '1', name: 'Cure Light Wounds' },
	{ level: '1', name: 'Detect Animals or Plants' },
	{ level: '1', name: 'Detect Snares and Pits' },
	{ level: '1', name: 'Endure Elements' },
	{ level: '1', name: 'Entangle' },
	{ level: '1', name: 'Faerie Fire' },
	{ level: '1', name: 'Goodberry' },
	{ level: '1', name: 'Hide from Animals' },
	{ level: '1', name: 'Jump' },
	{ level: '1', name: 'Longstrider' },
	{ level: '1', name: 'Magic Fang' },
	{ level: '1', name: 'Magic Stone' },
	{ level: '1', name: 'Obscuring Mist' },
	{ level: '1', name: 'Pass without Trace' },
	{ level: '1', name: 'Produce Flame' },
	{ level: '1', name: 'Shillelagh' },
	{ level: '1', name: 'Speak with Animals' },
	{ level: '1', name: "Summon Nature's Ally I" },
	{ level: '2', name: 'Animal Messenger' },
	{ level: '2', name: 'Animal Trance' },
	{ level: '2', name: 'Barkskin' },
	{ level: '2', name: "Bear's Endurance" },
	{ level: '2', name: "Bull's Strength" },
	{ level: '2', name: "Cat's Grace" },
	{ level: '2', name: 'Chill Metal' },
	{ level: '2', name: 'Delay Poison' },
	{ level: '2', name: 'Fire Trap' },
	{ level: '2', name: 'Flame Blade' },
	{ level: '2', name: 'Flaming Sphere' },
	{ level: '2', name: 'Fog Cloud' },
	{ level: '2', name: 'Gust of Wind' },
	{ level: '2', name: 'Heat Metal' },
	{ level: '2', name: 'Hold Animal' },
	{ level: '2', name: "Owl's Wisdom" },
	{ level: '2', name: 'Reduce Animal' },
	{ level: '2', name: 'Resist Energy' },
	{ level: '2', name: 'Restoration, Lesser' },
	{ level: '2', name: 'Soften Earth and Stone' },
	{ level: '2', name: 'Spider Climb' },
	{ level: '2', name: "Summon Nature's Ally II" },
	{ level: '2', name: 'Summon Swarm' },
	{ level: '2', name: 'Tree Shape' },
	{ level: '2', name: 'Warp Wood' },
	{ level: '2', name: 'Wood Shape' },
	{ level: '3', name: 'Call Lightning' },
	{ level: '3', name: 'Contagion' },
	{ level: '3', name: 'Cure Moderate Wounds' },
	{ level: '3', name: 'Daylight' },
	{ level: '3', name: 'Diminish Plants' },
	{ level: '3', name: 'Dominate Animal' },
	{ level: '3', name: 'Magic Fang, Greater' },
	{ level: '3', name: 'Meld into Stone' },
	{ level: '3', name: 'Neutralize Poison' },
	{ level: '3', name: 'Plant Growth' },
	{ level: '3', name: 'Poison' },
	{ level: '3', name: 'Protection from Energy' },
	{ level: '3', name: 'Quench' },
	{ level: '3', name: 'Remove Disease' },
	{ level: '3', name: 'Sleet Storm' },
	{ level: '3', name: 'Snare' },
	{ level: '3', name: 'Speak with Plants' },
	{ level: '3', name: 'Spike Growth' },
	{ level: '3', name: 'Stone Shape' },
	{ level: '3', name: "Summon Nature's Ally III" },
	{ level: '3', name: 'Water Breathing' },
	{ level: '3', name: 'Wind Wall' },
	{ level: '4', name: 'Air Walk' },
	{ level: '4', name: 'Antiplant Shell' },
	{ level: '4', name: 'Blight' },
	{ level: '4', name: 'Command Plants' },
	{ level: '4', name: 'Control Water' },
	{ level: '4', name: 'Cure Serious Wounds' },
	{ level: '4', name: 'Dispel Magic' },
	{ level: '4', name: 'Flame Strike' },
	{ level: '4', name: 'Freedom of Movement' },
	{ level: '4', name: 'Giant Vermin' },
	{ level: '4', name: 'Ice Storm' },
	{ level: '4', name: 'Reincarnate' },
	{ level: '4', name: 'Repel Vermin' },
	{ level: '4', name: 'Rusting Grasp' },
	{ level: '4', name: 'Scrying' },
	{ level: '4', name: 'Spike Stones' },
	{ level: '4', name: "Summon Nature's Ally IV" },
	{ level: '5', name: 'Animal Growth' },
	{ level: '5', name: 'Atonement' },
	{ level: '5', name: 'Awaken' },
	{ level: '5', name: 'Baleful Polymorph' },
	{ level: '5', name: 'Call Lightning Storm' },
	{ level: '5', name: 'Commune with Nature' },
	{ level: '5', name: 'Control Winds' },
	{ level: '5', name: 'Cure Critical Wounds' },
	{ level: '5', name: 'Death Ward' },
	{ level: '5', name: 'Hallow' },
	{ level: '5', name: 'Insect Plague' },
	{ level: '5', name: 'Stoneskin' },
	{ level: '5', name: "Summon Nature's Ally V" },
	{ level: '5', name: 'Transmute Mud to Rock' },
	{ level: '5', name: 'Transmute Rock to Mud' },
	{ level: '5', name: 'Tree Stride' },
	{ level: '5', name: 'Unhallow' },
	{ level: '5', name: 'Wall of Fire' },
	{ level: '5', name: 'Wall of Thorns' },
	{ level: '6', name: 'Antilife Shell' },
	{ level: '6', name: "Bear's Endurance, Mass" },
	{ level: '6', name: "Bull's Strength, Mass" },
	{ level: '6', name: "Cat's Grace, Mass" },
	{ level: '6', name: 'Cure Light Wounds, Mass' },
	{ level: '6', name: 'Dispel Magic, Greater' },
	{ level: '6', name: 'Find the Path' },
	{ level: '6', name: 'Fire Seeds' },
	{ level: '6', name: 'Ironwood' },
	{ level: '6', name: 'Liveoak' },
	{ level: '6', name: 'Move Earth' },
	{ level: '6', name: "Owl's Wisdom, Mass" },
	{ level: '6', name: 'Repel Wood' },
	{ level: '6', name: 'Spellstaff' },
	{ level: '6', name: 'Stone Tell' },
	{ level: '6', name: "Summon Nature's Ally VI" },
	{ level: '6', name: 'Transport via Plants' },
	{ level: '6', name: 'Wall of Stone' },
	{ level: '7', name: 'Animate Plants' },
	{ level: '7', name: 'Changestaff' },
	{ level: '7', name: 'Control Weather' },
	{ level: '7', name: 'Creeping Doom' },
	{ level: '7', name: 'Cure Moderate Wounds, Mass' },
	{ level: '7', name: 'Fire Storm' },
	{ level: '7', name: 'Heal' },
	{ level: '7', name: 'Scrying, Greater' },
	{ level: '7', name: "Summon Nature's Ally VII" },
	{ level: '7', name: 'Sunbeam' },
	{ level: '7', name: 'Transmute Metal to Wood' },
	{ level: '7', name: 'True Seeing' },
	{ level: '7', name: 'Wind Walk' },
	{ level: '8', name: 'Animal Shapes' },
	{ level: '8', name: 'Control Plants' },
	{ level: '8', name: 'Cure Serious Wounds, Mass' },
	{ level: '8', name: 'Earthquake' },
	{ level: '8', name: 'Finger of Death' },
	{ level: '8', name: 'Repel Metal or Stone' },
	{ level: '8', name: 'Reverse Gravity' },
	{ level: '8', name: "Summon Nature's Ally VIII" },
	{ level: '8', name: 'Sunburst' },
	{ level: '8', name: 'Whirlwind' },
	{ level: '8', name: 'Word of Recall' },
	{ level: '9', name: 'Antipathy' },
	{ level: '9', name: 'Cure Critical Wounds, Mass' },
	{ level: '9', name: 'Elemental Swarm' },
	{ level: '9', name: 'Foresight' },
	{ level: '9', name: 'Regenerate' },
	{ level: '9', name: 'Shambler' },
	{ level: '9', name: 'Shapechange' },
	{ level: '9', name: 'Storm of Vengeance' },
	{ level: '9', name: "Summon Nature's Ally IX" },
	{ level: '9', name: 'Sympathy' }
];

var domainSpells = {
	'Air': ['Obscuring Mist','Wind Wall','Gaseous Form','Air Walk','Control Winds','Chain Lightning','Control Weather','Whirlwind','Elemental Swarm (Air only)'],
	'Animal': ['Calm Animals','Hold Animal','Dominate Animal',"Summon Nature's Ally IV (Animals only)",'Commune with Nature','Antilife Shell','Animal Shapes',"Summon Nature's Ally VIII (Animals only)",'Shapechange'],
	'Chaos': ['Protection from Law','Shatter','Magic Circle against Law','Chaos Hammer','Dispel Law','Animate Objects','Word of Chaos','Cloak of Chaos','Summon Monster IX (Chaos only)'],
	'Death': ['Cause Fear','Death Knell','Animate Dead','Death Ward','Slay Living','Create Undead','Destruction','Create Greater Undead','Wail of the Banshee'],
	'Destruction': ['Inflict Light Wounds','Shatter','Contagion','Inflict Critical Wounds','Inflict Light Wounds, Mass','Harm','Disintegrate','Earthquake','Implosion'],
	'Earth': ['Magic Stone','Soften Earth and Stone','Stone Shape','Spike Stones','Wall of Stone','Stoneskin','Earthquake','Iron Body','Elemental Swarm (Earth only)'],
	'Evil': ['Protection from Good','Desecrate','Magic Circle against Good','Unholy Blight','Dispel Good','Create Undead','Blasphemy','Unholy Aura','Summon Monster IX (Evil only)'],
	'Fire': ['Burning Hands','Produce Flame','Resist Energy (Cold or fire only)','Wall of Fire','Fire Shield','Fire Seeds','Fire Storm','Incendiary Cloud','Elemental Swarm (Fire only)'],
	'Good': ['Protection from Evil','Aid','Magic Circle against Evil','Holy Smite','Dispel Evil','Blade Barrier','Holy Word','Holy Aura','Summon Monster IX (Good only)'],
	'Healing': ['Cure Light Wounds','Cure Moderate Wounds','Cure Serious Wounds','Cure Critical Wounds','Cure Light Wounds, Mass','Heal','Regenerate','Cure Critical Wounds, Mass','Heal, Mass'],
	'Knowledge': ['Detect Secret Doors','Detect Thoughts','Clairaudience/Clairvoyance','Divination','True Seeing','Find the Path','Legend Lore','Discern Location','Foresight'],
	'Law': ['Protection from Chaos','Calm Emotions','Magic Circle against Chaos',"Order's Wrath",'Dispel Chaos','Hold Monster','Dictum','Shield of Law','Summon Monster IX (Law only)'],
	'Luck': ['Entropic Shield','Aid','Protection from Energy','Freedom of Movement','Break Enchantment','Mislead','Spell Turning','Moment of Prescience','Miracle'],
	'Magic': ["Nystul's Magic Aura",'Identify','Dispel Magic','Imbue with Spell Ability','Spell Resistance','Antimagic Field','Spell Turning','Protection from Spells',"Mordenkainen's Disjunction"],
	'Plant': ['Entangle','Barkskin','Plant Growth','Command Plants','Wall of Thorns','Repel Wood','Animate Plants','Control Plants','Shambler'],
	'Protection': ['Sanctuary','Shield Other','Protection from Energy','Spell Immunity','Spell Resistance','Antimagic Field','Repulsion','Mind Blank','Prismatic Sphere'],
	'Strength': ['Enlarge Person',"Bull's Strength",'Magic Vestment','Spell Immunity','Righteous Might','Stoneskin',"Bigby's Grasping Hand","Bigby's Clenched Fist","Bigby's Crushing Hand"],
	'Sun': ['Endure Elements','Heat Metal','Searing Light','Fire Shield','Flame Strike','Fire Seeds','Sunbeam','Sunburst','Prismatic Sphere'],
	'Travel': ['Longstrider','Locate Object','Fly','Dimension Door','Teleport','Find the Path','Teleport, Greater','Phase Door','Astral Projection'],
	'Trickery': ['Disguise Self','Invisibility','Nondetection','Confusion','False Vision','Mislead','Screen','Polymorph Any Object','Time Stop'],
	'War': ['Magic Weapon','Spiritual Weapon','Magic Vestment','Divine Power','Flame Strike','Blade Barrier','Power Word Blind','Power Word Stun','Power Word Kill'],
	'Water': ['Obscuring Mist','Fog Cloud','Water Breathing','Control Water','Ice Storm','Cone of Cold','Acid Fog','Horrid Wilting','Elemental Swarm (Water only)']
};

var paladinSpells = [
	{ level: '1', name: 'Bless' },
	{ level: '1', name: 'Bless Water' },
	{ level: '1', name: 'Bless Weapon' },
	{ level: '1', name: 'Create Water' },
	{ level: '1', name: 'Cure Light Wounds' },
	{ level: '1', name: 'Detect Poison' },
	{ level: '1', name: 'Detect Undead' },
	{ level: '1', name: 'Divine Favor' },
	{ level: '1', name: 'Endure Elements' },
	{ level: '1', name: 'Magic Weapon' },
	{ level: '1', name: 'Protection from Chaos' },
	{ level: '1', name: 'Protection from Evil' },
	{ level: '1', name: 'Read Magic' },
	{ level: '1', name: 'Resistance' },
	{ level: '1', name: 'Restoration, Lesser' },
	{ level: '1', name: 'Virtue' },
	{ level: '2', name: "Bull's Strength" },
	{ level: '2', name: 'Delay Poison' },
	{ level: '2', name: "Eagle's Splendor" },
	{ level: '2', name: "Owl's Wisdom" },
	{ level: '2', name: 'Remove Paralysis' },
	{ level: '2', name: 'Resist Energy' },
	{ level: '2', name: 'Shield Other' },
	{ level: '2', name: 'Undetectable Alignment' },
	{ level: '2', name: 'Zone of Truth' },
	{ level: '3', name: 'Cure Moderate Wounds' },
	{ level: '3', name: 'Daylight' },
	{ level: '3', name: 'Discern Lies' },
	{ level: '3', name: 'Dispel Magic' },
	{ level: '3', name: 'Heal Mount' },
	{ level: '3', name: 'Magic Circle against Chaos' },
	{ level: '3', name: 'Magic Circle against Evil' },
	{ level: '3', name: 'Magic Weapon, Greater' },
	{ level: '3', name: 'Prayer' },
	{ level: '3', name: 'Remove Blindness/Deafness' },
	{ level: '3', name: 'Remove Curse' },
	{ level: '4', name: 'Break Enchantment' },
	{ level: '4', name: 'Cure Serious Wounds' },
	{ level: '4', name: 'Death Ward' },
	{ level: '4', name: 'Dispel Chaos' },
	{ level: '4', name: 'Dispel Evil' },
	{ level: '4', name: 'Holy Sword' },
	{ level: '4', name: 'Mark of Justice' },
	{ level: '4', name: 'Neutralize Poison' },
	{ level: '4', name: 'Restoration' }
];

var rangerSpells = [
	{ level: '1', name: 'Alarm' },
	{ level: '1', name: 'Animal Messenger' },
	{ level: '1', name: 'Calm Animals' },
	{ level: '1', name: 'Charm Animal' },
	{ level: '1', name: 'Delay Poison' },
	{ level: '1', name: 'Detect Animals or Plants' },
	{ level: '1', name: 'Detect Poison' },
	{ level: '1', name: 'Detect Snares and Pits' },
	{ level: '1', name: 'Endure Elements' },
	{ level: '1', name: 'Entangle' },
	{ level: '1', name: 'Hide from Animals' },
	{ level: '1', name: 'Jump' },
	{ level: '1', name: 'Longstrider' },
	{ level: '1', name: 'Magic Fang' },
	{ level: '1', name: 'Pass without Trace' },
	{ level: '1', name: 'Read Magic' },
	{ level: '1', name: 'Resist Energy' },
	{ level: '1', name: 'Speak with Animals' },
	{ level: '1', name: "Summon Nature's Ally I" },
	{ level: '2', name: 'Barkskin' },
	{ level: '2', name: "Bear's Endurance" },
	{ level: '2', name: "Cat's Grace" },
	{ level: '2', name: 'Cure Light Wounds' },
	{ level: '2', name: 'Hold Animal' },
	{ level: '2', name: "Owl's Wisdom" },
	{ level: '2', name: 'Protection from Energy' },
	{ level: '2', name: 'Snare' },
	{ level: '2', name: 'Speak with Plants' },
	{ level: '2', name: 'Spike Growth' },
	{ level: '2', name: "Summon Nature's Ally II" },
	{ level: '2', name: 'Wind Wall' },
	{ level: '3', name: 'Command Plants' },
	{ level: '3', name: 'Cure Moderate Wounds' },
	{ level: '3', name: 'Darkvision' },
	{ level: '3', name: 'Diminish Plants' },
	{ level: '3', name: 'Magic Fang, Greater' },
	{ level: '3', name: 'Neutralize Poison' },
	{ level: '3', name: 'Plant Growth' },
	{ level: '3', name: 'Reduce Animal' },
	{ level: '3', name: 'Remove Disease' },
	{ level: '3', name: 'Repel Vermin' },
	{ level: '3', name: "Summon Nature's Ally III" },
	{ level: '3', name: 'Tree Shape' },
	{ level: '3', name: 'Water Walk' },
	{ level: '4', name: 'Animal Growth' },
	{ level: '4', name: 'Commune with Nature' },
	{ level: '4', name: 'Cure Serious Wounds' },
	{ level: '4', name: 'Freedom of Movement' },
	{ level: '4', name: 'Nondetection' },
	{ level: '4', name: "Summon Nature's Ally IV" },
	{ level: '4', name: 'Tree Stride' }
];

var arcaneSpells = [
	{ school: 'Abjuration', level: '0', name: 'Resistance' },
	{ school: 'Conjuration', level: '0', name: 'Acid Splash' },
	{ school: 'Divination', level: '0', name: 'Detect Poison' },
	{ school: 'Divination', level: '0', name: 'Detect Magic' },
	{ school: 'Divination', level: '0', name: 'Read Magic' },
	{ school: 'Enchantment', level: '0', name: 'Daze' },
	{ school: 'Evocation', level: '0', name: 'Dancing Lights' },
	{ school: 'Evocation', level: '0', name: 'Flare' },
	{ school: 'Evocation', level: '0', name: 'Light' },
	{ school: 'Evocation', level: '0', name: 'Ray of Frost' },
	{ school: 'Illusion', level: '0', name: 'Ghost Sound' },
	{ school: 'Necromancy', level: '0', name: 'Disrupt Undead' },
	{ school: 'Necromancy', level: '0', name: 'Touch of Fatigue' },
	{ school: 'Transmutation', level: '0', name: 'Mage Hand' },
	{ school: 'Transmutation', level: '0', name: 'Mending' },
	{ school: 'Transmutation', level: '0', name: 'Message' },
	{ school: 'Transmutation', level: '0', name: 'Open/Close' },
	{ school: 'Universal', level: '0', name: 'Arcane Mark' },
	{ school: 'Universal', level: '0', name: 'Prestidigitation' },
	{ school: 'Abjuration', level: '1', name: 'Alarm' },
	{ school: 'Abjuration', level: '1', name: 'Endure Elements' },
	{ school: 'Abjuration', level: '1', name: 'Hold Portal' },
	{ school: 'Abjuration', level: '1', name: 'Protection from Chaos' },
	{ school: 'Abjuration', level: '1', name: 'Protection from Evil' },
	{ school: 'Abjuration', level: '1', name: 'Protection from Good' },
	{ school: 'Abjuration', level: '1', name: 'Protection from Law' },
	{ school: 'Abjuration', level: '1', name: 'Shield' },
	{ school: 'Conjuration', level: '1', name: 'Grease' },
	{ school: 'Conjuration', level: '1', name: 'Mage Armor' },
	{ school: 'Conjuration', level: '1', name: 'Mount' },
	{ school: 'Conjuration', level: '1', name: 'Obscuring Mist' },
	{ school: 'Conjuration', level: '1', name: 'Summon Monster I' },
	{ school: 'Conjuration', level: '1', name: 'Unseen Servant' },
	{ school: 'Divination', level: '1', name: 'Comprehend Languages' },
	{ school: 'Divination', level: '1', name: 'Detect Secret Doors' },
	{ school: 'Divination', level: '1', name: 'Detect Undead' },
	{ school: 'Divination', level: '1', name: 'Identify' },
	{ school: 'Divination', level: '1', name: 'True Strike' },
	{ school: 'Enchantment', level: '1', name: 'Charm Person' },
	{ school: 'Enchantment', level: '1', name: 'Hypnotism' },
	{ school: 'Enchantment', level: '1', name: 'Sleep' },
	{ school: 'Evocation', level: '1', name: 'Burning Hands' },
	{ school: 'Evocation', level: '1', name: 'Magic Missile' },
	{ school: 'Evocation', level: '1', name: 'Shocking Grasp' },
	{ school: 'Evocation', level: '1', name: "Tenser's Floating Disk" },
	{ school: 'Illusion', level: '1', name: 'Color Spray' },
	{ school: 'Illusion', level: '1', name: 'Disguise Self' },
	{ school: 'Illusion', level: '1', name: "Nystul's Magic Aura" },
	{ school: 'Illusion', level: '1', name: 'Silent Image' },
	{ school: 'Illusion', level: '1', name: 'Ventriloquism' },
	{ school: 'Necromancy', level: '1', name: 'Cause Fear' },
	{ school: 'Necromancy', level: '1', name: 'Chill Touch' },
	{ school: 'Necromancy', level: '1', name: 'Ray of Enfeeblement' },
	{ school: 'Transmutation', level: '1', name: 'Animate Rope' },
	{ school: 'Transmutation', level: '1', name: 'Enlarge Person' },
	{ school: 'Transmutation', level: '1', name: 'Erase' },
	{ school: 'Transmutation', level: '1', name: 'Expeditious Retreat' },
	{ school: 'Transmutation', level: '1', name: 'Feather Fall' },
	{ school: 'Transmutation', level: '1', name: 'Jump' },
	{ school: 'Transmutation', level: '1', name: 'Magic Weapon' },
	{ school: 'Transmutation', level: '1', name: 'Reduce Person' },
	{ school: 'Abjuration', level: '2', name: 'Arcane Lock' },
	{ school: 'Abjuration', level: '2', name: 'Obscure Object' },
	{ school: 'Abjuration', level: '2', name: 'Protection from Arrows' },
	{ school: 'Abjuration', level: '2', name: 'Resist Energy' },
	{ school: 'Conjuration', level: '2', name: 'Fog Cloud' },
	{ school: 'Conjuration', level: '2', name: 'Glitterdust' },
	{ school: 'Conjuration', level: '2', name: "Melf's Acid Arrow" },
	{ school: 'Conjuration', level: '2', name: 'Summon Monster II' },
	{ school: 'Conjuration', level: '2', name: 'Summon Swarm' },
	{ school: 'Conjuration', level: '2', name: 'Web' },
	{ school: 'Divination', level: '2', name: 'Detect Thoughts' },
	{ school: 'Divination', level: '2', name: 'Locate Object' },
	{ school: 'Divination', level: '2', name: 'See Invisibility' },
	{ school: 'Enchantment', level: '2', name: 'Daze Monster' },
	{ school: 'Enchantment', level: '2', name: "Tasha's Hideous Laughter" },
	{ school: 'Enchantment', level: '2', name: 'Touch of Idiocy' },
	{ school: 'Evocation', level: '2', name: 'Continual Flame' },
	{ school: 'Evocation', level: '2', name: 'Darkness' },
	{ school: 'Evocation', level: '2', name: 'Flaming Sphere' },
	{ school: 'Evocation', level: '2', name: 'Gust of Wind' },
	{ school: 'Evocation', level: '2', name: 'Scorching Ray' },
	{ school: 'Evocation', level: '2', name: 'Shatter' },
	{ school: 'Illusion', level: '2', name: 'Blur' },
	{ school: 'Illusion', level: '2', name: 'Hypnotic Pattern' },
	{ school: 'Illusion', level: '2', name: 'Invisibility' },
	{ school: 'Illusion', level: '2', name: "Leomund's Trap" },
	{ school: 'Illusion', level: '2', name: 'Magic Mouth' },
	{ school: 'Illusion', level: '2', name: 'Minor Image' },
	{ school: 'Illusion', level: '2', name: 'Mirror Image' },
	{ school: 'Illusion', level: '2', name: 'Misdirection' },
	{ school: 'Necromancy', level: '2', name: 'Blindness/Deafness' },
	{ school: 'Necromancy', level: '2', name: 'Command Undead' },
	{ school: 'Necromancy', level: '2', name: 'False Life' },
	{ school: 'Necromancy', level: '2', name: 'Ghoul Touch' },
	{ school: 'Necromancy', level: '2', name: 'Scare' },
	{ school: 'Necromancy', level: '2', name: 'Spectral Hand' },
	{ school: 'Transmutation', level: '2', name: 'Alter Self' },
	{ school: 'Transmutation', level: '2', name: "Bear's Endurance" },
	{ school: 'Transmutation', level: '2', name: "Bull's Strength" },
	{ school: 'Transmutation', level: '2', name: "Cat's Grace" },
	{ school: 'Transmutation', level: '2', name: 'Darkvision' },
	{ school: 'Transmutation', level: '2', name: "Eagle's Splendor" },
	{ school: 'Transmutation', level: '2', name: "Fox's Cunning" },
	{ school: 'Transmutation', level: '2', name: 'Knock' },
	{ school: 'Transmutation', level: '2', name: 'Levitate' },
	{ school: 'Transmutation', level: '2', name: "Owl's Wisdom" },
	{ school: 'Transmutation', level: '2', name: 'Pyrotechnics' },
	{ school: 'Transmutation', level: '2', name: 'Rope Trick' },
	{ school: 'Transmutation', level: '2', name: 'Spider Climb' },
	{ school: 'Transmutation', level: '2', name: 'Whispering Wind' },
	{ school: 'Abjuration', level: '3', name: 'Dispel Magic' },
	{ school: 'Abjuration', level: '3', name: 'Explosive Runes' },
	{ school: 'Abjuration', level: '3', name: 'Magic Circle against Chaos' },
	{ school: 'Abjuration', level: '3', name: 'Magic Circle against Evil' },
	{ school: 'Abjuration', level: '3', name: 'Magic Circle against Good' },
	{ school: 'Abjuration', level: '3', name: 'Magic Circle against Law' },
	{ school: 'Abjuration', level: '3', name: 'Nondetection' },
	{ school: 'Abjuration', level: '3', name: 'Protection from Energy' },
	{ school: 'Conjuration', level: '3', name: 'Phantom Steed' },
	{ school: 'Conjuration', level: '3', name: 'Sepia Snake Sigil' },
	{ school: 'Conjuration', level: '3', name: 'Sleet Storm' },
	{ school: 'Conjuration', level: '3', name: 'Stinking Cloud' },
	{ school: 'Conjuration', level: '3', name: 'Summon Monster III' },
	{ school: 'Divination', level: '3', name: 'Arcane Sight' },
	{ school: 'Divination', level: '3', name: 'Clairaudience/Clairvoyance' },
	{ school: 'Divination', level: '3', name: 'Tongues' },
	{ school: 'Enchantment', level: '3', name: 'Deep Slumber' },
	{ school: 'Enchantment', level: '3', name: 'Heroism' },
	{ school: 'Enchantment', level: '3', name: 'Hold Person' },
	{ school: 'Enchantment', level: '3', name: 'Rage' },
	{ school: 'Enchantment', level: '3', name: 'Suggestion' },
	{ school: 'Evocation', level: '3', name: 'Daylight' },
	{ school: 'Evocation', level: '3', name: 'Fireball' },
	{ school: 'Evocation', level: '3', name: "Leomund's Tiny Hut" },
	{ school: 'Evocation', level: '3', name: 'Lightning Bolt' },
	{ school: 'Evocation', level: '3', name: 'Wind Wall' },
	{ school: 'Illusion', level: '3', name: 'Displacement' },
	{ school: 'Illusion', level: '3', name: 'Illusory Script' },
	{ school: 'Illusion', level: '3', name: 'Invisibility Sphere' },
	{ school: 'Illusion', level: '3', name: 'Major Image' },
	{ school: 'Necromancy', level: '3', name: 'Gentle Repose' },
	{ school: 'Necromancy', level: '3', name: 'Halt Undead' },
	{ school: 'Necromancy', level: '3', name: 'Ray of Exhaustion' },
	{ school: 'Necromancy', level: '3', name: 'Vampiric Touch' },
	{ school: 'Transmutation', level: '3', name: 'Blink' },
	{ school: 'Transmutation', level: '3', name: 'Flame Arrow' },
	{ school: 'Transmutation', level: '3', name: 'Fly' },
	{ school: 'Transmutation', level: '3', name: 'Gaseous Form' },
	{ school: 'Transmutation', level: '3', name: 'Haste' },
	{ school: 'Transmutation', level: '3', name: 'Keen Edge' },
	{ school: 'Transmutation', level: '3', name: 'Magic Weapon, Greater' },
	{ school: 'Transmutation', level: '3', name: 'Secret Page' },
	{ school: 'Transmutation', level: '3', name: 'Shrink Item' },
	{ school: 'Transmutation', level: '3', name: 'Slow' },
	{ school: 'Transmutation', level: '3', name: 'Water Breathing' },
	{ school: 'Abjuration', level: '4', name: 'Dimensional Anchor' },
	{ school: 'Abjuration', level: '4', name: 'Fire Trap' },
	{ school: 'Abjuration', level: '4', name: 'Globe of Invulnerability, Lesser' },
	{ school: 'Abjuration', level: '4', name: 'Remove Curse' },
	{ school: 'Abjuration', level: '4', name: 'Stoneskin' },
	{ school: 'Conjuration', level: '4', name: 'Dimension Door' },
	{ school: 'Conjuration', level: '4', name: "Evard's Black Tentacles" },
	{ school: 'Conjuration', level: '4', name: "Leomund's Secure Shelter" },
	{ school: 'Conjuration', level: '4', name: 'Minor Creation' },
	{ school: 'Conjuration', level: '4', name: 'Solid Fog' },
	{ school: 'Conjuration', level: '4', name: 'Summon Monster IV' },
	{ school: 'Divination', level: '4', name: 'Arcane Eye' },
	{ school: 'Divination', level: '4', name: 'Detect Scrying' },
	{ school: 'Divination', level: '4', name: 'Locate Creature' },
	{ school: 'Divination', level: '4', name: 'Scrying' },
	{ school: 'Enchantment', level: '4', name: 'Charm Monster' },
	{ school: 'Enchantment', level: '4', name: 'Confusion' },
	{ school: 'Enchantment', level: '4', name: 'Crushing Despair' },
	{ school: 'Enchantment', level: '4', name: 'Geas, Lesser' },
	{ school: 'Evocation', level: '4', name: 'Fire Shield' },
	{ school: 'Evocation', level: '4', name: 'Ice Storm' },
	{ school: 'Evocation', level: '4', name: "Otiluke's Resilient Sphere" },
	{ school: 'Evocation', level: '4', name: 'Shout' },
	{ school: 'Evocation', level: '4', name: 'Wall of Fire' },
	{ school: 'Evocation', level: '4', name: 'Wall of Ice' },
	{ school: 'Illusion', level: '4', name: 'Hallucinatory Terrain' },
	{ school: 'Illusion', level: '4', name: 'Illusory Wall' },
	{ school: 'Illusion', level: '4', name: 'Invisibility, Greater' },
	{ school: 'Illusion', level: '4', name: 'Phantasmal Killer' },
	{ school: 'Illusion', level: '4', name: 'Rainbow Pattern' },
	{ school: 'Illusion', level: '4', name: 'Shadow Conjuration' },
	{ school: 'Necromancy', level: '4', name: 'Animate Dead' },
	{ school: 'Necromancy', level: '4', name: 'Bestow Curse' },
	{ school: 'Necromancy', level: '4', name: 'Contagion' },
	{ school: 'Necromancy', level: '4', name: 'Enervation' },
	{ school: 'Necromancy', level: '4', name: 'Fear' },
	{ school: 'Transmutation', level: '4', name: 'Enlarge Person, Mass' },
	{ school: 'Transmutation', level: '4', name: 'Polymorph' },
	{ school: 'Transmutation', level: '4', name: "Rary's Mnemonic Enhancer" },
	{ school: 'Transmutation', level: '4', name: 'Reduce Person, Mass' },
	{ school: 'Transmutation', level: '4', name: 'Stone Shape' },
	{ school: 'Abjuration', level: '5', name: 'Break Enchantment' },
	{ school: 'Abjuration', level: '5', name: 'Dismissal' },
	{ school: 'Abjuration', level: '5', name: "Mordenkainen's Private Sanctum" },
	{ school: 'Conjuration', level: '5', name: 'Cloudkill' },
	{ school: 'Conjuration', level: '5', name: "Leomund's Secret Chest" },
	{ school: 'Conjuration', level: '5', name: 'Major Creation' },
	{ school: 'Conjuration', level: '5', name: "Mordenkainen's Faithful Hound" },
	{ school: 'Conjuration', level: '5', name: 'Planar Binding, Lesser' },
	{ school: 'Conjuration', level: '5', name: 'Summon Monster V' },
	{ school: 'Conjuration', level: '5', name: 'Teleport' },
	{ school: 'Conjuration', level: '5', name: 'Wall of Stone' },
	{ school: 'Divination', level: '5', name: 'Contact Other Plane' },
	{ school: 'Divination', level: '5', name: 'Prying Eyes' },
	{ school: 'Divination', level: '5', name: "Rary's Telepathic Bond" },
	{ school: 'Enchantment', level: '5', name: 'Dominate Person' },
	{ school: 'Enchantment', level: '5', name: 'Feeblemind' },
	{ school: 'Enchantment', level: '5', name: 'Hold Monster' },
	{ school: 'Enchantment', level: '5', name: 'Mind Fog' },
	{ school: 'Enchantment', level: '5', name: 'Symbol of Sleep' },
	{ school: 'Evocation', level: '5', name: "Bigby's Interposing Hand" },
	{ school: 'Evocation', level: '5', name: 'Cone of Cold' },
	{ school: 'Evocation', level: '5', name: 'Sending' },
	{ school: 'Evocation', level: '5', name: 'Wall of Force' },
	{ school: 'Illusion', level: '5', name: 'Dream' },
	{ school: 'Illusion', level: '5', name: 'False Vision' },
	{ school: 'Illusion', level: '5', name: 'Mirage Arcana' },
	{ school: 'Illusion', level: '5', name: 'Nightmare' },
	{ school: 'Illusion', level: '5', name: 'Persistent Image' },
	{ school: 'Illusion', level: '5', name: 'Seeming' },
	{ school: 'Illusion', level: '5', name: 'Shadow Evocation' },
	{ school: 'Necromancy', level: '5', name: 'Blight' },
	{ school: 'Necromancy', level: '5', name: 'Magic Jar' },
	{ school: 'Necromancy', level: '5', name: 'Symbol of Pain' },
	{ school: 'Necromancy', level: '5', name: 'Waves of Fatigue' },
	{ school: 'Transmutation', level: '5', name: 'Animal Growth' },
	{ school: 'Transmutation', level: '5', name: 'Baleful Polymorph' },
	{ school: 'Transmutation', level: '5', name: 'Fabricate' },
	{ school: 'Transmutation', level: '5', name: 'Overland Flight' },
	{ school: 'Transmutation', level: '5', name: 'Passwall' },
	{ school: 'Transmutation', level: '5', name: 'Telekinesis' },
	{ school: 'Transmutation', level: '5', name: 'Transmute Mud to Rock' },
	{ school: 'Transmutation', level: '5', name: 'Transmute Rock to Mud' },
	{ school: 'Universal', level: '5', name: 'Permanency' },
	{ school: 'Abjuration', level: '6', name: 'Antimagic Field' },
	{ school: 'Abjuration', level: '6', name: 'Dispel Magic, Greater' },
	{ school: 'Abjuration', level: '6', name: 'Globe of Invulnerability' },
	{ school: 'Abjuration', level: '6', name: 'Guards and Wards' },
	{ school: 'Abjuration', level: '6', name: 'Repulsion' },
	{ school: 'Conjuration', level: '6', name: 'Acid Fog' },
	{ school: 'Conjuration', level: '6', name: 'Planar Binding' },
	{ school: 'Conjuration', level: '6', name: 'Summon Monster VI' },
	{ school: 'Conjuration', level: '6', name: 'Wall of Iron' },
	{ school: 'Divination', level: '6', name: 'Analyze Dweomer' },
	{ school: 'Divination', level: '6', name: 'Legend Lore' },
	{ school: 'Divination', level: '6', name: 'True Seeing' },
	{ school: 'Enchantment', level: '6', name: 'Geas/Quest' },
	{ school: 'Enchantment', level: '6', name: 'Heroism, Greater' },
	{ school: 'Enchantment', level: '6', name: 'Suggestion, Mass' },
	{ school: 'Enchantment', level: '6', name: 'Symbol of Persuasion' },
	{ school: 'Evocation', level: '6', name: "Bigby's Forceful Hand" },
	{ school: 'Evocation', level: '6', name: 'Chain Lightning' },
	{ school: 'Evocation', level: '6', name: 'Contingency' },
	{ school: 'Evocation', level: '6', name: "Otiluke's Freezing Sphere" },
	{ school: 'Illusion', level: '6', name: 'Mislead' },
	{ school: 'Illusion', level: '6', name: 'Permanent Image' },
	{ school: 'Illusion', level: '6', name: 'Programmed Image' },
	{ school: 'Illusion', level: '6', name: 'Shadow Walk' },
	{ school: 'Illusion', level: '6', name: 'Veil' },
	{ school: 'Necromancy', level: '6', name: 'Circle of Death' },
	{ school: 'Necromancy', level: '6', name: 'Create Undead' },
	{ school: 'Necromancy', level: '6', name: 'Eyebite' },
	{ school: 'Necromancy', level: '6', name: 'Symbol of Fear' },
	{ school: 'Necromancy', level: '6', name: 'Undeath to Death' },
	{ school: 'Transmutation', level: '6', name: "Bear's Endurance, Mass" },
	{ school: 'Transmutation', level: '6', name: "Bull's Strength, Mass" },
	{ school: 'Transmutation', level: '6', name: "Cat's Grace, Mass" },
	{ school: 'Transmutation', level: '6', name: 'Control Water' },
	{ school: 'Transmutation', level: '6', name: 'Disintegrate' },
	{ school: 'Transmutation', level: '6', name: "Eagle's Splendor, Mass" },
	{ school: 'Transmutation', level: '6', name: 'Flesh to Stone' },
	{ school: 'Transmutation', level: '6', name: "Fox's Cunning, Mass" },
	{ school: 'Transmutation', level: '6', name: "Mordenkainen's Lucubration" },
	{ school: 'Transmutation', level: '6', name: 'Move Earth' },
	{ school: 'Transmutation', level: '6', name: "Owl's Wisdom, Mass" },
	{ school: 'Transmutation', level: '6', name: 'Stone to Flesh' },
	{ school: 'Transmutation', level: '6', name: "Tenser's Transformation" },
	{ school: 'Abjuration', level: '7', name: 'Banishment' },
	{ school: 'Abjuration', level: '7', name: 'Sequester' },
	{ school: 'Abjuration', level: '7', name: 'Spell Turning' },
	{ school: 'Conjuration', level: '7', name: "Drawmij's Instant Summons" },
	{ school: 'Conjuration', level: '7', name: "Mordenkainen's Magnificent Mansion" },
	{ school: 'Conjuration', level: '7', name: 'Phase Door' },
	{ school: 'Conjuration', level: '7', name: 'Plane Shift' },
	{ school: 'Conjuration', level: '7', name: 'Summon Monster VII' },
	{ school: 'Conjuration', level: '7', name: 'Teleport, Greater' },
	{ school: 'Conjuration', level: '7', name: 'Teleport Object' },
	{ school: 'Divination', level: '7', name: 'Arcane Sight, Greater' },
	{ school: 'Divination', level: '7', name: 'Scrying, Greater' },
	{ school: 'Divination', level: '7', name: 'Vision' },
	{ school: 'Enchantment', level: '7', name: 'Hold Person, Mass' },
	{ school: 'Enchantment', level: '7', name: 'Insanity' },
	{ school: 'Enchantment', level: '7', name: 'Power Word Blind' },
	{ school: 'Enchantment', level: '7', name: 'Symbol of Stunning' },
	{ school: 'Evocation', level: '7', name: "Bigby's Grasping Hand" },
	{ school: 'Evocation', level: '7', name: 'Delayed Blast Fireball' },
	{ school: 'Evocation', level: '7', name: 'Forcecage' },
	{ school: 'Evocation', level: '7', name: "Mordenkainen's Sword" },
	{ school: 'Evocation', level: '7', name: 'Prismatic Spray' },
	{ school: 'Illusion', level: '7', name: 'Invisibility, Mass' },
	{ school: 'Illusion', level: '7', name: 'Project Image' },
	{ school: 'Illusion', level: '7', name: 'Shadow Conjuration, Greater' },
	{ school: 'Illusion', level: '7', name: 'Simulacrum' },
	{ school: 'Necromancy', level: '7', name: 'Control Undead' },
	{ school: 'Necromancy', level: '7', name: 'Finger of Death' },
	{ school: 'Necromancy', level: '7', name: 'Symbol of Weakness' },
	{ school: 'Necromancy', level: '7', name: 'Waves of Exhaustion' },
	{ school: 'Transmutation', level: '7', name: 'Control Weather' },
	{ school: 'Transmutation', level: '7', name: 'Ethereal Jaunt' },
	{ school: 'Transmutation', level: '7', name: 'Reverse Gravity' },
	{ school: 'Transmutation', level: '7', name: 'Statue' },
	{ school: 'Universal', level: '7', name: 'Limited Wish' },
	{ school: 'Abjuration', level: '8', name: 'Dimensional Lock' },
	{ school: 'Abjuration', level: '8', name: 'Mind Blank' },
	{ school: 'Abjuration', level: '8', name: 'Prismatic Wall' },
	{ school: 'Abjuration', level: '8', name: 'Protection from Spells' },
	{ school: 'Conjuration', level: '8', name: 'Incendiary Cloud' },
	{ school: 'Conjuration', level: '8', name: 'Maze' },
	{ school: 'Conjuration', level: '8', name: 'Planar Binding, Greater' },
	{ school: 'Conjuration', level: '8', name: 'Summon Monster VIII' },
	{ school: 'Conjuration', level: '8', name: 'Trap the Soul' },
	{ school: 'Divination', level: '8', name: 'Discern Location' },
	{ school: 'Divination', level: '8', name: 'Moment of Prescience' },
	{ school: 'Divination', level: '8', name: 'Prying Eyes, Greater' },
	{ school: 'Enchantment', level: '8', name: 'Antipathy' },
	{ school: 'Enchantment', level: '8', name: 'Binding' },
	{ school: 'Enchantment', level: '8', name: 'Charm Monster, Mass' },
	{ school: 'Enchantment', level: '8', name: 'Demand' },
	{ school: 'Enchantment', level: '8', name: "Otto's Irresistible Dance" },
	{ school: 'Enchantment', level: '8', name: 'Power Word Stun' },
	{ school: 'Enchantment', level: '8', name: 'Symbol of Insanity' },
	{ school: 'Enchantment', level: '8', name: 'Sympathy' },
	{ school: 'Evocation', level: '8', name: "Bigby's Clenched Fist" },
	{ school: 'Evocation', level: '8', name: "Otiluke's Telekinetic Sphere" },
	{ school: 'Evocation', level: '8', name: 'Polar Ray' },
	{ school: 'Evocation', level: '8', name: 'Shout, Greater' },
	{ school: 'Evocation', level: '8', name: 'Sunburst' },
	{ school: 'Illusion', level: '8', name: 'Scintillating Pattern' },
	{ school: 'Illusion', level: '8', name: 'Screen' },
	{ school: 'Illusion', level: '8', name: 'Shadow Evocation, Greater' },
	{ school: 'Necromancy', level: '8', name: 'Clone' },
	{ school: 'Necromancy', level: '8', name: 'Create Greater Undead' },
	{ school: 'Necromancy', level: '8', name: 'Horrid Wilting' },
	{ school: 'Necromancy', level: '8', name: 'Symbol of Death' },
	{ school: 'Transmutation', level: '8', name: 'Iron Body' },
	{ school: 'Transmutation', level: '8', name: 'Polymorph Any Object' },
	{ school: 'Transmutation', level: '8', name: 'Temporal Stasis' },
	{ school: 'Abjuration', level: '9', name: 'Freedom' },
	{ school: 'Abjuration', level: '9', name: 'Imprisonment' },
	{ school: 'Abjuration', level: '9', name: "Mordenkainen's Disjunction" },
	{ school: 'Abjuration', level: '9', name: 'Prismatic Sphere' },
	{ school: 'Conjuration', level: '9', name: 'Gate' },
	{ school: 'Conjuration', level: '9', name: 'Refuge' },
	{ school: 'Conjuration', level: '9', name: 'Summon Monster IX' },
	{ school: 'Conjuration', level: '9', name: 'Teleportation Circle' },
	{ school: 'Divination', level: '9', name: 'Foresight' },
	{ school: 'Enchantment', level: '9', name: 'Dominate Monster' },
	{ school: 'Enchantment', level: '9', name: 'Hold Monster, Mass' },
	{ school: 'Enchantment', level: '9', name: 'Power Word Kill' },
	{ school: 'Evocation', level: '9', name: "Bigby's Crushing Hand" },
	{ school: 'Evocation', level: '9', name: 'Meteor Swarm' },
	{ school: 'Illusion', level: '9', name: 'Shades' },
	{ school: 'Illusion', level: '9', name: 'Weird' },
	{ school: 'Necromancy', level: '9', name: 'Astral Projection' },
	{ school: 'Necromancy', level: '9', name: 'Energy Drain' },
	{ school: 'Necromancy', level: '9', name: 'Soul Bind' },
	{ school: 'Necromancy', level: '9', name: 'Wail of the Banshee' },
	{ school: 'Transmutation', level: '9', name: 'Etherealness' },
	{ school: 'Transmutation', level: '9', name: 'Shapechange' },
	{ school: 'Transmutation', level: '9', name: 'Time Stop' },
	{ school: 'Universal', level: '9', name: 'Wish' }
];

var bardSpells = [
	{ level: '0', name: 'Dancing Lights' },
	{ level: '0', name: 'Daze' },
	{ level: '0', name: 'Detect Magic' },
	{ level: '0', name: 'Flare' },
	{ level: '0', name: 'Ghost Sound' },
	{ level: '0', name: 'Know Direction' },
	{ level: '0', name: 'Light' },
	{ level: '0', name: 'Lullaby' },
	{ level: '0', name: 'Mage Hand' },
	{ level: '0', name: 'Mending' },
	{ level: '0', name: 'Message' },
	{ level: '0', name: 'Open/Close' },
	{ level: '0', name: 'Prestidigitation' },
	{ level: '0', name: 'Read Magic' },
	{ level: '0', name: 'Resistance' },
	{ level: '0', name: 'Summon Instrument' },
	{ level: '1', name: 'Alarm' },
	{ level: '1', name: 'Animate Rope' },
	{ level: '1', name: 'Cause Fear' },
	{ level: '1', name: 'Charm Person' },
	{ level: '1', name: 'Comprehend Languages' },
	{ level: '1', name: 'Cure Light Wounds' },
	{ level: '1', name: 'Detect Secret Doors' },
	{ level: '1', name: 'Disguise Self' },
	{ level: '1', name: 'Erase' },
	{ level: '1', name: 'Expeditious Retreat' },
	{ level: '1', name: 'Feather Fall' },
	{ level: '1', name: 'Grease' },
	{ level: '1', name: 'Hypnotism' },
	{ level: '1', name: 'Identify' },
	{ level: '1', name: 'Lesser Confusion' },
	{ level: '1', name: 'Magic Mouth' },
	{ level: '1', name: "Nystul's Magic Aura" },
	{ level: '1', name: 'Obscure Object' },
	{ level: '1', name: 'Remove Fear' },
	{ level: '1', name: 'Silent Image' },
	{ level: '1', name: 'Sleep' },
	{ level: '1', name: 'Summon Monster I' },
	{ level: '1', name: "Tasha's Hideous Laughter" },
	{ level: '1', name: 'Undetectable Alignment' },
	{ level: '1', name: 'Unseen Servant' },
	{ level: '1', name: 'Ventriloquism' },
	{ level: '2', name: 'Alter Self' },
	{ level: '2', name: 'Animal Messenger' },
	{ level: '2', name: 'Animal Trance' },
	{ level: '2', name: 'Blindness/Deafness' },
	{ level: '2', name: 'Blur' },
	{ level: '2', name: 'Calm Emotions' },
	{ level: '2', name: "Cat's Grace" },
	{ level: '2', name: 'Cure Moderate Wounds' },
	{ level: '2', name: 'Darkness' },
	{ level: '2', name: 'Daze Monster' },
	{ level: '2', name: 'Delay Poison' },
	{ level: '2', name: 'Detect Thoughts' },
	{ level: '2', name: "Eagle's Splendor" },
	{ level: '2', name: 'Enthrall' },
	{ level: '2', name: "Fox's Cunning" },
	{ level: '2', name: 'Glitterdust' },
	{ level: '2', name: 'Heroism' },
	{ level: '2', name: 'Hold Person' },
	{ level: '2', name: 'Hypnotic Pattern' },
	{ level: '2', name: 'Invisibility' },
	{ level: '2', name: 'Locate Object' },
	{ level: '2', name: 'Minor Image' },
	{ level: '2', name: 'Mirror Image' },
	{ level: '2', name: 'Misdirection' },
	{ level: '2', name: 'Pyrotechnics' },
	{ level: '2', name: 'Rage' },
	{ level: '2', name: 'Scare' },
	{ level: '2', name: 'Shatter' },
	{ level: '2', name: 'Silence' },
	{ level: '2', name: 'Sound Burst' },
	{ level: '2', name: 'Suggestion' },
	{ level: '2', name: 'Summon Monster II' },
	{ level: '2', name: 'Summon Swarm' },
	{ level: '2', name: 'Tongues' },
	{ level: '2', name: 'Whispering Wind' },
	{ level: '3', name: 'Blink' },
	{ level: '3', name: 'Charm Monster' },
	{ level: '3', name: 'Clairaudience/Clairvoyance' },
	{ level: '3', name: 'Confusion' },
	{ level: '3', name: 'Crushing Despair' },
	{ level: '3', name: 'Cure Serious Wounds' },
	{ level: '3', name: 'Daylight' },
	{ level: '3', name: 'Deep Slumber' },
	{ level: '3', name: 'Dispel Magic' },
	{ level: '3', name: 'Displacement' },
	{ level: '3', name: 'Fear' },
	{ level: '3', name: 'Gaseous Form' },
	{ level: '3', name: 'Geas, Lesser' },
	{ level: '3', name: 'Glibness' },
	{ level: '3', name: 'Good Hope' },
	{ level: '3', name: 'Haste' },
	{ level: '3', name: 'Illusory Script' },
	{ level: '3', name: 'Invisibility Sphere' },
	{ level: '3', name: "Leomund's Tiny Hut" },
	{ level: '3', name: 'Major Image' },
	{ level: '3', name: 'Phantom Steed' },
	{ level: '3', name: 'Remove Curse' },
	{ level: '3', name: 'Scrying' },
	{ level: '3', name: 'Sculpt Sound' },
	{ level: '3', name: 'Secret Page' },
	{ level: '3', name: 'See Invisibility' },
	{ level: '3', name: 'Sepia Snake Sigil' },
	{ level: '3', name: 'Slow' },
	{ level: '3', name: 'Speak with Animals' },
	{ level: '3', name: 'Summon Monster III' },
	{ level: '4', name: 'Break Enchantment' },
	{ level: '4', name: 'Cure Critical Wounds' },
	{ level: '4', name: 'Detect Scrying' },
	{ level: '4', name: 'Dimension Door' },
	{ level: '4', name: 'Dominate Person' },
	{ level: '4', name: 'Freedom of Movement' },
	{ level: '4', name: 'Hallucinatory Terrain' },
	{ level: '4', name: 'Hold Monster' },
	{ level: '4', name: 'Invisibility, Greater' },
	{ level: '4', name: 'Legend Lore' },
	{ level: '4', name: "Leomund's Secure Shelter" },
	{ level: '4', name: 'Locate Creature' },
	{ level: '4', name: 'Modify Memory' },
	{ level: '4', name: 'Neutralize Poison' },
	{ level: '4', name: 'Rainbow Pattern' },
	{ level: '4', name: 'Repel Vermin' },
	{ level: '4', name: 'Shadow Conjuration' },
	{ level: '4', name: 'Shout' },
	{ level: '4', name: 'Speak with Plants' },
	{ level: '4', name: 'Summon Monster IV' },
	{ level: '4', name: 'Zone of Silence' },
	{ level: '5', name: 'Cure Light Wounds, Mass' },
	{ level: '5', name: 'Dispel Magic, Greater' },
	{ level: '5', name: 'Dream' },
	{ level: '5', name: 'False Vision' },
	{ level: '5', name: 'Heroism, Greater' },
	{ level: '5', name: 'Mind Fog' },
	{ level: '5', name: 'Mirage Arcana' },
	{ level: '5', name: 'Mislead' },
	{ level: '5', name: 'Nightmare' },
	{ level: '5', name: 'Persistent Image' },
	{ level: '5', name: 'Seeming' },
	{ level: '5', name: 'Shadow Evocation' },
	{ level: '5', name: 'Shadow Walk' },
	{ level: '5', name: 'Song of Discord' },
	{ level: '5', name: 'Suggestion, Mass' },
	{ level: '5', name: 'Summon Monster' },
	{ level: '6', name: 'Analyze Dweomer' },
	{ level: '6', name: 'Animate Objects' },
	{ level: '6', name: "Cat's Grace, Mass" },
	{ level: '6', name: 'Charm Monster, Mass' },
	{ level: '6', name: 'Cure Moderate Wounds, Mass' },
	{ level: '6', name: "Eagle's Splendor, Mass" },
	{ level: '6', name: 'Eyebite' },
	{ level: '6', name: 'Find the Path' },
	{ level: '6', name: "Fox's Cunning, Mass" },
	{ level: '6', name: 'Geas/Quest' },
	{ level: '6', name: "Heroes' Feast" },
	{ level: '6', name: "Otto's Irresistible Dance" },
	{ level: '6', name: 'Permanent Image' },
	{ level: '6', name: 'Programmed Image' },
	{ level: '6', name: 'Project Image' },
	{ level: '6', name: 'Scrying, Greater' },
	{ level: '6', name: 'Shout, Greater' },
	{ level: '6', name: 'Summon Monster VI' },
	{ level: '6', name: 'Sympathetic Vibration' },
	{ level: '6', name: 'Veil' }
];

function bonusSpells(level, ability) {
	'use strict';

	return Math.floor(Math.max(0,parseInt(ability,10)-2-2*level)/8);
}

// sets up tab.slots. expects tab.maxslot to be correct
// if ability is null or undefined then it is not considered
// if spells is defined it should be an array of slots to use first at each level
function setupSlots(tab, has0, ability, perday, spells) {
	'use strict';
	var num, j, k, used, locked;

	tab.slots = [];
	for (j = (has0?0:1); j <= tab.maxslot; j++) {
		num = perday[j];
		if (j > 0 && ability != null) {		// != in order to test both null and undefined
			num += bonusSpells(j, ability);
		}
		
		used = 0;
		if (spells) {
			for (k = 0; k < spells.length; k++) {
				if (spells[k].level === j.toString()) {
					locked = false;
					if (spells[k].locked && spells[k].locked !== 'false') { locked = true; }
					tab.slots.push({ level: spells[k].level, description: spells[k].description, used: true, locked: locked });
					used++;
				}
			}
		}
		
		for (k = used; k < num; k++) {
			tab.slots.push({ level: j, description: j });
		}
	}
}

// sets up tab.availablespells. expects tab.maxslot to be correct
// if spells is defined any spells found in spells will be set to hidden
function setupAvailable(tab, spellList, spells) {
	'use strict';
	var j, s, k;

	tab.availablespells = [];
	for (j = 0; j < spellList.length; j++) {
		if (spellList[j].level <= tab.maxslot) {
			s = { level: spellList[j].level, name: spellList[j].name };
			if (spellList[j].school) { s.school = spellList[j].school; }
			if (spells) {
				for (k = 0; k < spells.length; k++) {
					if (s.level+" "+s.name === spells[k].description) {
						s.hidden = true;
					}
				}
			}
			tab.availablespells.push(s);
		}
	}
	tab.availablespells.sort(function(a,b) {
		if (a.level === b.level) {
			return a.name.localeCompare(b.name);
		}
		return a.level - b.level;
	});
}

// sets up tab.spelllevels. expects tab.maxslot to be correct
function setupSpellLevels(tab, has0) {
	'use strict';
	var j;

	tab.spelllevels = [];
	for (j = (has0?0:1); j <= tab.maxslot; j++) {
		tab.spelllevels.push({ level: j });
	}
}

// populates tab with metamagic options based on feats. expects tab.maxslot to be already set
function setupMetamagic(tab, feats) {
	'use strict';
	var j;
	
	tab.hasempower = feats['empower spell'];
	tab.hasenlarge = feats['enlarge spell'];
	tab.hasextend = feats['extend spell'];
	tab.hasmaximize = feats['maximize spell'];
	tab.hasquicken = feats['quicken spell'];
	tab.hassilent = feats['silent spell'];
	tab.hasstill = feats['still spell'];
	tab.haswiden = feats['widen spell'];
	if (feats['heighten spell']) {
		tab.hasheighten = true;
		tab.heightenoptions = [];
		for (j = 1; j < tab.maxslot; j++) {
			tab.heightenoptions.push({ levels: j });
		}
	}
}

// sets tab.id, tab.prefix, and tab.superfix based on name
function setupTab(type, name, maxslot) {
	'use strict';
	var tab = {};
	tab.type = type;
	tab.id = name.toLowerCase();
	tab.tabname = name;
	tab.idprefix = tab.id+"_";
	tab.maxslot = maxslot;
	tab.superfix = tab.id;
	return tab;
}

function buildConfig(character, spells) {
	'use strict';
	var output = [];
	var i, j, k;
	var feats, perday, maxslot;
	var tab, used, locked;

	for (i = 0; i < character.length; i++) {
		feats = {};
		if (character[i].feats) {
			for (j = 0; j < character[i].feats.length; j++) {
				feats[character[i].feats[j].toLowerCase()] = true;
			}
		}

		if (character[i].class === 'Cleric') {
			// TODO: alignment restrictions
			perday = clericSlots[character[i].level-1];
			maxslot = Math.min(perday.length-1, parseInt(character[i].ability,10)-10);
			
			tab = setupTab('prepare', 'Cleric', maxslot);
			setupSpellLevels(tab, true);
			setupMetamagic(tab, feats);
			setupAvailable(tab, clericSpells);
			setupSlots(tab, true, character[i].ability, perday, spells['tab_'+tab.id]);
			output.push(tab);

			tab = setupTab('prepare', 'Domain', maxslot);
			setupSpellLevels(tab, false);
			setupMetamagic(tab, feats);

			tab.availablespells = [];
			for (j = 1; j <= tab.maxslot; j++) {
				for (k = 0; k < character[i].domains.length; k++) {
					tab.availablespells.push({ level: j, name: domainSpells[character[i].domains[k]][j-1] });
				}
			}

			tab.slots = [];
			for (j = 1; j <= tab.maxslot; j++) {
				used = false;

				if (spells['tab_'+tab.id]) {
					for (k = 0; k < spells['tab_'+tab.id].length; k++) {
						if (spells['tab_'+tab.id][k].level === j.toString()) {
							locked = false;
							if (spells['tab_'+tab.id][k].locked && spells['tab_'+tab.id][k].locked !== 'false') { locked = true; }
							tab.slots.push({ level: spells['tab_'+tab.id][k].level, description: spells['tab_'+tab.id][k].description, used: true, locked: locked });
							used = true;
							break;
						}
					}
				}

				if (!used) { tab.slots.push({ level: j.toString(), description: j.toString() }); }
			}
			output.push(tab);

		} else if (character[i].class === 'Druid') {
			// TODO: alignment restrictions
			perday = clericSlots[character[i].level-1];
			maxslot = Math.min(perday.length-1, parseInt(character[i].ability,10)-10);
			
			tab = setupTab('prepare', 'Druid', maxslot);
			setupSpellLevels(tab, true);
			setupMetamagic(tab, feats);

			tab.availablespells = [];
			for (j = 0; j < druidSpells.length; j++) {
				if (druidSpells[j].level <= tab.maxslot) {
					tab.availablespells.push(druidSpells[j]);
				}
			}

			setupSlots(tab, true, character[i].ability, perday, spells['tab_'+tab.id]);
			output.push(tab);

		} else if (character[i].class === 'Paladin') {
			perday = paladinSlots[character[i].level-1];
			if (perday.length > 2 || (perday.length === 2 && bonusSpells(1, character[i].ability) > 0)) {
				maxslot = Math.min(perday.length-1, parseInt(character[i].ability,10)-10);
				
				tab = setupTab('prepare', 'Paladin', maxslot);
				setupSpellLevels(tab, false);
				setupMetamagic(tab, feats);
				setupAvailable(tab, paladinSpells);
				setupSlots(tab, false, character[i].ability, perday, spells['tab_'+tab.id]);
				output.push(tab);
			}

		} else if (character[i].class === 'Ranger') {
			perday = paladinSlots[character[i].level-1];
			if (perday.length > 2 || (perday.length === 2 && bonusSpells(1, character[i].ability) > 0)) {
				maxslot = Math.min(perday.length-1, parseInt(character[i].ability,10)-10);
				
				tab = setupTab('prepare', 'Ranger', maxslot);
				setupSpellLevels(tab, false);
				setupMetamagic(tab, feats);
				setupAvailable(tab, rangerSpells);
				setupSlots(tab, false, character[i].ability, perday, spells['tab_'+tab.id]);
				output.push(tab);
			}

		} else if (character[i].class === 'Sorcerer') {
			perday = sorcererPerDay[character[i].level-1].concat();		// concat to get a copy of the array (as we'll be modifying it)
			maxslot = Math.min(perday.length-1, parseInt(character[i].ability,10)-10);
			
			tab = setupTab('learn', 'Sorcerer', maxslot);

			for (j = 1; j < perday.length; j++) {
				perday[j] += bonusSpells(j, character[i].ability);
			}
			tab.perday = perday;

			setupSpellLevels(tab, true);
			setupAvailable(tab, arcaneSpells, spells['tab_'+tab.id]);
			setupSlots(tab, true, null, sorcererKnown[character[i].level-1], spells['tab_'+tab.id]);
			output.push(tab);

		} else if (character[i].class === 'Bard') {
			perday = bardPerDay[character[i].level-1].concat();		// concat to get a copy of the array (as we'll be modifying it)
			maxslot = Math.min(perday.length-1, parseInt(character[i].ability,10)-10);
			if (perday[maxslot] === 0) {
				maxslot--;
			}
			
			tab = setupTab('learn', 'Bard', maxslot);

			for (j = 1; j < perday.length; j++) {
				perday[j] += bonusSpells(j, character[i].ability);
			}
			tab.perday = perday;

			setupSpellLevels(tab, true);
			setupAvailable(tab, bardSpells, spells['tab_'+tab.id]);
			setupSlots(tab, true, null, bardKnown[character[i].level-1], spells['tab_'+tab.id]);
			output.push(tab);

		} else if (character[i].class === 'Wizard') {
			perday = wizardSlots[character[i].level-1];
			maxslot = Math.min(perday.length-1, parseInt(character[i].ability,10)-10);
			
			tab = setupTab('scribe', 'Spellbook', maxslot);
			tab.memtabprefix = 'wizard_';
			setupSpellLevels(tab, true);
			setupAvailable(tab, arcaneSpells, spells['tab_'+tab.id]);
			
			if (spells['tab_'+tab.id]) {
				tab.slots = [];
				for (j = 0; j < spells['tab_'+tab.id].length; j++) {
					tab.slots.push({ level: spells['tab_'+tab.id][j].level, description: spells['tab_'+tab.id][j].description });
				}
			}
			
			output.push(tab);

			tab = setupTab('prepare', 'Wizard', maxslot);
			setupSpellLevels(tab, true);
			tab.hasshowall = true;
			setupMetamagic(tab, feats);
			setupAvailable(tab, arcaneSpells);
			setupSlots(tab, true, character[i].ability, perday, spells['tab_'+tab.id]);
			output.push(tab);
		}
	}

	tab = {};
	tab.type = 'cast';
	tab.spells = [];
//	console.log("castable spells = "+require('util').inspect(spells['tab_cast'], { depth: null }));
	if (spells.tab_cast) {
		for (i = 0; i < spells.tab_cast.length; i++) {
//			console.log("spell: "+require('util').inspect(spells['tab_cast'][i], { depth: null }));
			tab.spells.push({ html: spells.tab_cast[i].html });
		}
	}
	output.push(tab);

	return output;
}

function getCharacter(name, callback) {
	'use strict';

	fs.readFile(name+'.character', function(err, data) {
		var config;

		if (!err) {
			try {
				config = JSON.parse(data);
			} catch (e) {
				err = e;
			}
		}

		callback(err, config);
	});
}

function getSpells(name, callback) {
	'use strict';

	fs.readFile(name+'.spells', function(err, data) {
		var spells;

		if (!err) {
			try {
				spells = JSON.parse(data);
			} catch (e) {
				err = e;
			}
		}

		callback(err, spells);
	});
}

function getConfig(name, callback) {
	'use strict';

	getSpells(name, function(err, spells) {
		if (err) {
			spells = {};
		}

		getCharacter(name, function(err, character) {
			var config;
	
			if (!err) {
				try {
					config = buildConfig(character, spells);
				} catch (e) {
					err = e;
				}
			}
	
			callback(err, config);
		});
	});
}

function setSpells(name, data, callback) {
	'use strict';
	
	fs.writeFile(name+'.spells', JSON.stringify(data), function(err) {
		callback(err);
	});
}


// check domain spells
var domain;
for (domain in domainSpells) {
	if (domainSpells.hasOwnProperty(domain) && domainSpells[domain].length !== 9) {
		console.log("Domain "+domain+" does not have 9 spells defined (found "+domainSpells[domain].length+")");
	}
}

exports.getConfig = getConfig;
exports.getCharacter = getCharacter;
exports.setSpells = setSpells;
exports.getSpells = getSpells;
