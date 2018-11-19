---=== IN PROGRESS ===---
* Implement AttackForm.setSize weapon damage reductions
* Scan monsters for HP fields and feat fields that don't match. Scan monsters for feats that are in the special qualities field
* BUG: Some feats not applied when manually added (e.g. "Weapon Focus (Gore)" on Triceratops
* Monster library needs to check for dupes and confirm replacement.
* Implement editing for remaining fields: Spec. Qualities, Space/Reach, AC, Size/type, Grapple (DONE Feats)
* Show calculations for hit dice/hit points, abilities. Or use character panels for editing (which have info popups).
* Reopen encounter window from combat/menu
* Export to html file? Maybe live?
* Advance monster by template: select template, flag fields that need updates, show template text for each field as selected - may be tricky as some templates change the type but don't recalculate
* Advance monster by character class
*
* Nightcrawler: "Attempted to add null modifier to Total skills.concentration = 0"
*
* DTT: Points of interest should be excluded from the tree view. Probably want to refactor UI around element tree/list anyway
* DTT: make walls a sub-element of map (as mask is)
* 	supoprt linking wall layouts to map images - if Walls element has image as parent then concatenate location, rotation, and mirroring
* DTT: split simple image (which supports animation) from map (which supports masks and walls)
* DTT: split rendering into layers, have separate ui elements for selecting maps, tokens, and perhaps lightsources
* 	layers for painting order
* 	Look at the map element order - should moving a tree move all children? - probably enough to have set layers and the ability to move between them
* Slots: xml output/parsing (done). apply effects (done). tooltips - descriptions (todo). fix up notifications - probably best to make each slot a property
* Check how natural armor bonus is implemented. It's both a modifier type (that only applies to AC), and a statistic that can itself be enhanced - seems to be right?
* Implement Spell Resistance
* Weapon/armor proficiencies: ui done but no effects implemented
* Weapon focus/spec: seems to be not working
* Auto numbers for tokens not coming through on ui
* Modifiers with a value of 0 should be suppressed in hints and website unless they are ability score mods or BAB mod.
* Size - think this is done for monsters? needs to be added to character and/or character ui
* Feat prereqs.
* Better handling of feats and skills with specific instances (knowledges, weapon focus, etc)
* UI for adding skill type (really only need for specific instances)
* Items - simple (template style) implementation done for armor, shields, and weapons
* BUG: creatures with missing abilities, e.g. colour out of space - issue seems to be with attacks that can't be parsed correctly (crash fixed but parsing needs fixing)
* Other feedback from website (spells cast etc)
* Audit use of createOverrideIntegerField and look at moving other override controls to standard field (probably a new field type which only displays the override, not the total)
* Enhance standard fields with tool tip?
* Statistic - need to handle overrides correctly (only modifiers since the last override count).
* Overrides on statistics should be on the total. If a base value override is required then the base value should be a property. - implement this, plus handling of overrides for statistics including xml serialization
* Remove remaining PROPERTY constants? At least check where they are used
* Sort out hierarchy - consider if sub-properties should be registered with their parent or with the Creature.
* Consider a PropertyParent interface.
* Modifiers event/listener stuff should perhaps be similar to property in style
* BUG floating labels on tokens: website token key has no description, floating label checkbox is not restored on reload, if the label itself is deleted then changing the remote label will cause an exception
* Support conditions (and change barbarian rage to a condition) 
* Clean up grid references before A0
* Allow grid references to be entered for element x/y coordinates in ui
* BUG "reset" button caused index out of bounds error in RemoteImageDisplay
* Drawing lines should optionally add walls and/or allow editing of walls elements
* Skill focus feat needs targeting implemented
* StatisticsCollection should be expanded to provide the filtering required for selecting targets like weapons for spells. Also consider how it fits into notification of changes to collective properties

---=== CODE STRUCTURE ===---
 camera - camera panel ui and functionality
 combat - combat panel ui and functionality
 digital_table - local and remote digital table functionality
 digital_table.controller - local digital table controller ui
 digital_table.elements - displayable elements common to both local and remote displays
 digital_table.server - core functionality for both local and remote displays and remote only classes
 gamesystem - core code related to the 3.5 mechanics
 gamesystem.core - low level classes (statistic and property) not intimately tied to the ruleset
 gamesystem.dice - classes representing dice
 led_control - remote control of LED strip
 magicgenerator - random magic item generator
 magicitems - shop panel ui and functionality
 monsters - monsters panel ui and functionality
 party - party panel functionality (could also contain ui for party and rolls panels, but also Party itself is more of a "campaign" class now)
 swing - extended generic swing components
 ui - ui for party and rolls panel and dialogs. should contain only common ui and dialogs
 util - util classes for external communication (XML handling, file uploading, logging)
 
 maptool - standalone tool for setting map scale and masks
 tilemapper - standalone tool for creating tile-based maps
 

---=== NEW HIGH LEVEL APP ARCHITECTURE ===---
The Module system will be used for coordinating parts of the system, and loading/saving app and installation specific settings (server addresses, hardware related settings, directory defaults etc).
Party (which may need a rename) will coordinate campaign related stuff including ruleset preferences, characters, and current encounter state.
Might want to make CharacterLibrary to be the core "campaign" object that does the loading/saving and handles rulesets and other campaign level stuff. Party would still handle the Party element.  

---=== NEW NOTIFICATION IMPLEMENTATION ===---
Plan is for there to be a single listener list (per character). Listeners will provide a string template for the sources they are interested in. Events will then be sent to interested listeners.
Statistics will generate "value" and "modifiers" events and may have a "base" event (though the base value may itself be a Property). Properties will generate "value" and "overrides" events.
E.g. event on "abilities.strength.value" would be received by listeners on "abilities", "abilities.strength", and "abilities.strength.value".
The same naming hierarchy will be used for effect targets. 

---=== ARCHITECTURE ===---

Property - a value that can be overridden. can be a non-numeric value (e.g. race), even a list. used for most end-user visible attributes of a creature. notifies changes.
Statistic - a numeric Property that can receive Modifiers. notifies changes. can also represent a collection of statistics (e.g. all skills, or attacks collectively).

Propertys and Statistics notify changes through a PropertyCollection which is currently always the base creature. This is used to provide support for a hierarchy of
notification, e.g. a listener can listen for changes to a Property that represents a collection of values (e.g. "Skills") or a specific Property (e.g. "Disable Device").
This also allows a listener to listen for a Property that doesn't yet exist. 

Modifier - an adjustment to a Statistic. Modifiers have a type and Statistics follow the 3.5 rules of stacking.

EffectorDefinition - base class for templates that generate Effector. Allows simple fixed effects to be defined.
Effector - base class for things that are modify creatures via Modifiers or Property changes, e.g. feats, class features, spells, conditions, etc.
Buff - an instance of an effect that can be applied to a creature. the buff can be customised to set variable effects. when applied to a creature the effects are fixed. A type of Effector.
BuffFactory - template for a Buff. A type of EffectorDefinition.
Feat - A type of Effector.
Feat.FeatDefinition - template for Feat. A type of EffectorDefinition.
ClassFeature - A type of Effector. Can have parameters that modify the effect.
ClassFeature.ClassFeatureDefinition - template for ClassFeature. A type of EffectorDefinition. Can define parameter in the generated ClassFeatures and can provide descriptions that reflect current parameter values.

** Currently Buffs are all spells though they could easily encompass conditions as well. Currently Buffs are temporary effects and Feat/ClassFeature are more permanent, but I don't
think there is any significance to the distinction (though perhaps for determining what modifiers to an ability score should apply to spell memorisation?). Items should also fit into this framework.
Also need to incorporate effects that can be added to a creature but that don't apply Modifiers. Many spells fall into this category.

refactor: Buff/BuffFactory -> SpellEffect and AdhocEffect [not sure what I intended the significance of the difference to be, perhaps duration or are AdhocEffects ones that are created on the fly like custom modifiers?] 

There is a distinction between selected core feats, selected class bonus feats, and automatic class bonus feats - each
will need to be tracked. In addition there are cases where creatures are treated as having a feat in some circumstances
(e.g. ranger combat styles). Perhaps it would be best to have a list of "features" that can be tested for. Features
would include some feats, racial abilities, class abilities, etc. The "Two-Weapon Fighting" feature could be provided
by the "Two-Weapon Fighting" feat but it could also alternately be provided by a ranger's Combat Style class feature or
even perhaps as a racial feature.

---=== GAMESYSTEM PACKAGE CLASSES ===---
Statistics:
	AbilityScore
	AC
	Attacks
	GrappleModifier
	HPs
	InititativeModifier
	Levels (probably should be a Property)
	SavingThrow
	Size
	Skills

Properties:
	BAB
	Race
	Sanity

3.5 System classes:
	AbstractModifier
	CalculatedValue (probably should be a factory that creates Propertys)
	CR
	CreatureProcessor
	DoubleModifier
	ImmutableModifier
	LimitModifier
	Modifier
	RuleSet
	Statistic
	StatisticsCollection
	XMLOutputHelper
	XMLParserHelper
	XP

Rule definition classes:
	BABProgression
	Buff
	BuffFactory
	CharacterClass
	ClassFeature
	Creature
	Effector
	EffectorFactory
	Feat
	HitDiceProperty
	ItemDefinition
	MonsterType
	SaveProgression
	SizeCategory
	SkillType
	Spell

---=== TODO ===---
Game system things to implement:
  (in progress) Size - mostly done. just ui and xml for characters?
  (in progress) Race - monster advancement is done. still need to do character race features etc.
  (in progress) Feats - better UI support for targetted feats, implement weapon and armor proficiencies
  (in progress) Grapple modifier - done?
  Ability score checks
  (in progress) Class levels - negative levels
  Spell lists / spells per day (web version done)
  Damage reduction
  Spell resistance
  (in progress) Magic items slots - done?
  Weight/encumberance/ACP
  Skill named versions (Crafting, Profession etc)
  Speed
  (in progress) Items

* fix monster stats tooltip, it's annoying
* website track saves to verify not overwriting other client changes
* Remote input - joysticks, web
* implement negative levels
* implement conditions
* implement StatisticsCollection for Attacks so they can be used for custom buffs
* implement caster levels with saving to website. (probably easiest to have a dedicated field for the relevant ability for rememorising, to be eventually replaced with a system that knows what modifiers are temporary and therefore shouldn't be counted)
* implement periodic special abilities and item uses/charges.
* move definitions of ClassFeatureDefinition to XML. FeatDefinition and BuffFactory are done.
* clarify how Effector "types" should work (simply the class or explicit field). clarify how Effector "source" should work.
* expand to allow Effectors to have effects that apply overrides (property changes already implemented for buffs).
* implement items - partly done
* adhoc weapon bonuses: done
    ... Implementation only works for characters (via CharacterAttackForm), for monsters would want to refactor the id stuff back to AttackForm and come up with a way to locate given attack forms.
    ... Implementation includes the beginnings of Effect targets that select multiple Statistics based on some criterion (in this case id), this can be expanded for use with things like Feats
    ... that apply to a specific class or type of weapon. But will need a way to specify that such effects are ongoing and should be applied to future cases that match.
    ... Currently the dynamic targeting is handled in Character, it should probably be handled by the Attack statistic itself.
    ... TODO Need to look at names/description of targets, it's ugly in some cases, particularly with selectors ("attacks[id=1]")
* implement cross-class skills flag somehow, at least for straight class characters
    
 * Skill parsing for monsters
 *
 * Rework Statistic change notification. Consider making it a subclass of property (override would override total value). Consider factoring out interface. (all done)
 * ? properties for statistics: bab, convert temp hps
 * ? consider reimplementing hps. it's not really a statistic, really more a property of the creature or perhaps of the
 * ?    level or hitdice statistic. figure out how to implement hitdice/character levels. implement negative levels as well
 * ? review Statistics vs creature Properties
 * ? ... need to review how properties work on Character and BoundIntegerField (done)
 * ? character is not registered as a listener on the attack forms so it doesn't get notified of changes. probably should revisit the whole property/statistic notification system
 * ? rework statistc notification system. a listener registered with the top of a tree (like Skills or Attacks)
 * ?    should get notification of all sub-statistics (done). consider whether statistics need to provide old and new values
 * ?    (this is desirable for mutable Modifiers at least)
 * ? ... convert ui classes that listen to Character to listen to the specific Statistics instead - could do a StatisticsProxy class
 * ?    that could be used as a base for statistics that rely on a common set of modifiers such as touch AC, skills etc
 *
 * BUG: Fractional weights for weapons
 * BUG: Fractional ranks for skill in table
 * BUG: Interface to add skills
 * BUG: Perform skills need subtype
 *
 * Turn/Rebuke
 * AC Size modifiers should be correctly linked to size stat (done). Size should also modify carrying capacity
 * Remove misc modifiers from skills and saves - adhoc buffs replace these (should be read only fields in ui)
 * add damage statistic on attackforms (done). add extra_damage property to damage statistics (for e.g. flaming)
 * implement buffs on attackforms - need to implement add action in AttackFormPanel.AttackFormInfoDialog
 * Sort out magic shops: make them fully configurable in XML
 * Upload character sheet should update caster config
 * Online character sheet: conditional modifiers, updatable posessions, slots, notes, money
 *
 * live character sheet: fix up incomplete fading of character sheet when dialog appears
 * ui: add info dialog for remaining statistics (ac, weapons, armor, level, size)
 * live character sheet: add calculations for remaining statistics (attacks, ac, weapon damage, armor, level, size)
 * live character sheet: consider adding list of buffs/effects
 *
 * rework attacks - they need an interface to filter properties like type etc. then filters can be used to build
 *    target lists (e.g  "type=bludgeoning and subclass=one handed melee")
 *
 * Fix the layout/sizing of the character panels - think we're going to need a customised splitpane controlling two scrollpanes (done?)
 * Continue to update for new module system (particularly digital table controller)
 * Perhaps make Updater a module
 * Clean up CameraPanel layout (obsolete now)
 *
 * Copy in encounters dialog should copy the current creature, not the base
 * In encounters dialog, adding an image should select it for the current creature (done?)
 *
 * Combat panel should save full monsters, not just combat entries
 * EncounterDialog: calc encounter level, display CRs
 * Encounterdialog should load/save buffs and maybe DTT selected elements
 * clear all for images. also cleared squares should be translucent on local
 * spell lists in AssistantDM
 * In party.xml consider changing "base" attribute on saves and attacks to "baseOverride" or "override"
 *
 * cleanup hitpoints/hitdice
 * implement remaining monster statistics
 * cleanup AttackForms in Attack, StatisticBlock and DetailedMonster
 *
 * look at standardising attribute naming style in xml documents: should be lower case with dashes - currently have camel case for combat.xml, lower with underscores most other cases but a few cases of lower with dashes in party.xml
 * ... change 'value' attributes in xml. these should either be 'base' or 'total' attributes (support 'value' as 'base' for loading only). also fix differences in ac
 *
 * BUG handle io exceptions while reading display.xml
 * when temporary hitpoints from a buff are gone the buff should be removed if it has no other effect
 * should be able to temporarily disable armor/shield (once inventory is tracked should have way of selecting items in inventory)
 * rearrange images. also find animal names - stats blocks
 * website: simplify updating - updates can be missed at the moment
 *
 * add combat panel section for pending xp/defeated monsters
 * camera: EOS camera support + refactoring of camera library (obsolete)
 * camera/dtt: Detect token movement (obsolete)
 * ability checks
 * enum conversions - property and statistics types
 * feats - selecting of feats with target skill/weapon/spells/school. change available list to remove already selected feats
 * equipment, particularly magic item slots, armor, weapons
 */
//TODO ultimately would like a live DOM. the DOM saved to the party XML file would be a filtered version
//WISH refactor classes that should be in ui package
//TODO add new party menu option, ask to save modified file


---=== DIGITAL TABLE PRIORITIES ===---
* Drawing element: freehand paint, eraser, map symbols
* Allow modifying Mask colours
* standard visibility controls
* lightsources attached to tokens should behave as if there was one on each corner of the token (auto set property?)
PART * tilemapper element - tilemapper editor added and maps supported as groups of images. a custom element would still have advantages
* ENH: Reordering the elements resets the group expanded/collapsed state
* add caching of loaded files in MediaManager
* performance improvements in animation code - bounding boxes for elements
DONE? * grouping - changing parent should modify children to maintain position - probably need consistent location property to implement this
* BUG exception if image width or height is set to 0 - slightly fixed by returning the unscaled/rotated image
* asynchronous loading of images
* soft references for ImageMedia - at least for transformed images
* ImageMedia could use a soft/weak/strong reference for transformed images
* BUG: "Set Image" on token options panel doesn't always immediately repaint with the new image
* Pre-guess the screen layout
* Auto configure - set defaults according to OS screen layout
* Threaded remote display communication
* Recalibrate display - could be done using screen bounds element
* BUG: tries to pop up remote browser on the screen with the corresponding index, not the absolute screen number
* parsing display xml resets the node priorty - need to save the list model order
* BUG: LineTemplate: setting image after rotation draws the un-transformed image
* REF: Factor clear cells code into support class
* Hidden elements in table display should clear ImageMedia transforms
* Consider separate element for darkness cleared cells - should be parent-relative - or perhaps add to LightSource
* Allow reconnect to remote - partly works but seems to cause exception on 3rd reconnect
* Add colour to the overlay tokens. either indicator of health or settable
* Consider expanding "selected" support. Would need hierarchy support as with visibility
* Refactor common utility methods into MapElement (e.g. template creation)
* Alternate button dragging (e.g. resize, non-snapped to grid)
* Make line and spread templates editable?
* Swarm Token (editable token with replicated painting)
* dice roller element?
* thrown object scatter? compass rose?
