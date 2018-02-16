---=== IN PROGRESS ===---
* Audit use of createOverrideIntegerField and look at moving other override controls to standard field (probably a new field type which only displays the override, not the total)
* Enhance standard fields with tool tip?
* Statistic - need to handle overrides correctly (only modifiers since the last override count).
* Overrides on statistics should be on the total. If a base value override is required then the base value should be a property. - implement this, plus handling of overrides for statistics including xml serialization
* Remove remaining PROPERTY constants? At least check where they are used
* Sort out hierarchy - consider if sub-properties should be registered with their parent or with the Creature.
* Consider a PropertyParent interface.
* Modifiers event/listener stuff should perhaps be similar to property in style

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
 magicgenerator - random magic item generator
 magicitems - shop panel ui and functionality
 monsters - monsters panel ui and functionality
 party - party panel functionality (should also contain ui for party and rolls panels)
 swing - extended generic swing components
 ui - ui for party and rolls panel and dialogs. should contain only common ui and dialogs
 util - util classes for external communication (XML handling, file uploading, logging)

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

Property - a value that can be overridden. can be a non-numeric value e.g. race. notifies changes.
Statistic - a numeric Property that can receive Modifiers. notifies changes. can also represent a collection of statistics (e.g. all skills, or attacks collectively).
Modifier - an adjustment to a Statistic. Modifiers have a type and Statistics follow the 3.5 rules of stacking.

FeatureDefinition - base class for templates that generate Features. Allows simple fixed effects to be defined.
Feature - base class for features that are comprised of a set of Modifiers and can be applied to creatures.
Buff - an instance of an effect that can be applied to a creature. the buff can be customised to set variable effects. when applied to a creature the effects are fixed. A type of Feature.
BuffFactory - template for a Buff. A type of FeatureDefinition.
Feat - A type of Feature.
Feat.FeatDefinition - template for Feat. A type of FeatureDefinition.
ClassFeature - A type of Feature. Can have parameters that modify the effect.
ClassFeature.ClassFeatureDefinition - template for ClassFeature. A type of FeatureDefinition. Can define parameter in the generated ClassFeatures and can provide descriptions that reflect current parameter values.

** Currently Buffs are all spells though they could easily encompass conditions as well. Currently Buffs are temporary effects and Feat/ClassFeature are more permanent, but I don't
think there is any significance to the distinction (though perhaps for determining what modifiers to an ability score should apply to spell memorisation?). Items should also fit into this framework.
Also need to incorporate effects that can be added to a creature but that don't apply Modifiers. Many spells fall into this category.

refactor: Buff/BuffFactory -> SpellEffect and AdhocEffect

(old) Proposed architecture:
A Statistic is a value that can be modified by bonuses and penalties and can also be overridden. Statistics can have
sub-Statistics which include all the parent's modifiers but can also be targeted separately. E.g. there will be a
hierarchy of Attacks -> Melee Attack -> specific melee attack form.
A Property is a value that can be overridden but is not a valid target for bonuses and penalties.
Both Statistic and Property provide change notification.
Creature is a collection of Statistics and Properties and acts as the root of the hierarchy. Creature also maintains
other data such as feats, special abilities/qualities, xp (Characters subclass).

There is a distinction between selected core feats, selected class bonus feats, and automatic class bonus feats - each
will need to be tracked. In addition there are cases where creatures are treated as having a feat in some circumstances
(e.g. ranger combat styles). Perhaps it would be best to have a list of "features" that can be tested for. Features
would include some feats, racial abilities, class abilities, etc. The "Two-Weapon Fighting" feature could be provided
by the "Two-Weapon Fighting" feat but it could also alternately be provided by a ranger's Combat Style class feature or
even perhaps as a racial feature.

---=== TODO ===---
Game system things to implement:
  (in progress) Size
  (in progress) Race
  (in progress) Feats
  (in progress) Grapple modifier
  Ability score checks
  (in progress) Class levels - negative levels
  Spell lists / spells per day (web version done)
  Damage reduction
  Spell resistance
  Magic items slots
  Weight/encumberance/ACP
  Skill synergies
  Skill named versions (Crafting, Profession etc)
  Speed
  (in progress) Items

* rework combat panels so they have hps and sanity and popup to modify either. also fix monster stats tooltip, it's annoying
* website track saves to verify not overwriting other client changes
* Remote input - joysticks, web
* split hps statistic in max and current. modifiers to current are temporary hitpoints. modifiers to max are permanent changes (e.g. from feats) or penalties such as negative levels
* implement negative levels
* implement conditions.
* implement StatisticsCollection for Attacks  
* implement caster levels with saving to website. (probably easiest to have a dedicated field for the relevant ability for rememorising, to be eventually replaced with a system that knows what modifiers are temporary and therefore shouldn't be counted)
* implement periodic special abilities and item uses/charges.
* move definitions of FeatDefinition, ClassFeatureDefinition, and BuffFactory to XML.
* clarify how Feature "types" should work (simply the class or explicit field). clarify how Feature "source" should work.
* expand to allow override effects (property changes already implemented for buffs).
* implement items. probably better to rename Feature to Effect or something more generic.
* adhoc weapon bonuses: done
    ... Implementation only works for characters (via CharacterAttackForm), for monsters would want to refactor the id stuff back to AttackForm and come up with a way to locate given attack forms.
    ... Implementation includes the beginnings of Effect targets that select multiple Statistics based on some criterion (in this case id), this can be expanded for use with things like Feats
    ... that apply to a specific class or type of weapon. But will need a way to specify that such effects are ongoing and should be applied to future cases that match.
    ... Currently the dynamic targeting is handled in Character, it should probably be handled by the Attack statistic itself.
    ... TODO Need to look at names/description of targets, it's ugly in some cases, particularly with selectors ("attacks[id=1]")
* implement cross-class skills flag somehow, at least for straight class characters
    
 * Skill parsing for monsters
 * Make HitDiceProperty into Statistic (rename to HitDice) so that bonus hps from feats and race (constructs) can be added as a modifier
 * Saving throw modifiers in monster stats blocks
 *
 * Rework Statistic change notification. Consider making it a subclass of property (override would override total value). Consider factoring out interface.
 * ? properties for statistics: bab, convert temp hps
 * ? consider reimplementing hps. it's not really a statistic, really more a property of the creature or perhaps of the
 * ?    level or hitdice statistic. figure out how to implement hitdice/character levels. implement negative levels as well
 * ? review Statistics vs creature Properties
 * ? ... need to review how properties work on Character and BoundIntegerField
 * ? character is not registered as a listener on the attack forms so it doesn't get notified of changes. probably should revisit the whole property/statistic notification system
 * ? rework statistc notification system. a listener registered with the top of a tree (like Skills or Attacks)
 * ?    should get notification of all sub-statistics. consider whether statistics need to provide old and new values
 * ?    (this is desirable for mutable Modifiers at least)
 * ? ... convert ui classes that listen to Character to listen to the specific Statistics instead - could do a StatisticsProxy class
 * ?    that could be used as a base for statistics that rely on a common set of modifiers such as touch AC, skills etc
 *
 * BUG: Fractional weights for weapons
 * BUG: Fractional ranks for skill in table
 * BUG: Interface to add skills
 * BUG: Perform skills need subtype
 *
 * Special abilities: class and race
 * Turn/Rebuke
 * size (where is up to?)
 * AC Size modifiers should be correctly linked to size stat. Size should also modify carrying capacity
 * Remove misc modifiers from skills and saves - adhoc buffs replace these (should be read only fields in ui)
 * add damage statistic on attackforms. add extra_damage property to damage statistics (for e.g. flamming)
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
 * Fix the layout/sizing of the character panels - think we're going to need a customised splitpane controlling two scrollpanes
 * Continue to update for new module system (particularly digital table controller)
 * Perhaps make Updater a module
 * Clean up CameraPanel layout
 *
 * Copy in encounters dialog should copy the current creature, not the base
 * In encounters dialog, adding an image should select it for the current creature
 *
 * Combat panel should save full monsters, not just combat entries
 * EncounterDialog: calc encounter level, display CRs
 * Encounterdialog should load/save buffs and maybe DTT selected elements
 * EncounterDialog: allow editing of AC, feats, size, SQ, etc
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
 * Better support for instansiating monsters - size, HPs generation, select token image, show image, etc
 * camera: EOS camera support + refactoring of camera library
 * camera/dtt: Detect token movement
 * ability checks
 * enum conversions - property and statistics types
 * feats - selecting of feats with target skill/weapon/spells/school. change available list to remove already selected feats
 * equipment, particularly magic item slots, armor, weapons
 */
//TODO ultimately would like a live DOM. the DOM saved to the party XML file would be a filtered version
//WISH refactor classes that should be in ui package
//TODO add new party menu option, ask to save modified file


---=== DIGITAL TABLE PRIORITIES ===---
* layers for painting order
* Look at the map element order - should moving a tree move all children? - probably enough to have set layers and the ability to move between them
DONE? * Allow setting of DarknessMask and Mask colours
* standard visibility controls
* supoprt linking wall layouts to map images - if Walls element has image as parent then concatenate location, rotation, and mirroring
* lightsources attached to tokens should behave as if there was one on each corner of the token
* building wall layout from xml should be threaded for performance
PART * tilemapper element - tilemapper editor added and maps supported as groups of images. a custom element would still have advantages
* ENH: Reordering the elements resets the group expanded/collapsed state
* free paint element
* add caching of loaded files in MediaManager
* performance improvements in animation code - bounding boxes for elements
DONE? * grouping - changing parent should modify children to maintain position - probably need consistent location property to implement this
* BUG exception if image width or height is set to 0 - slightly fixed by returning the unscaled/rotated image
* asynchronous loading of images
* soft references for ImageMedia - at least for transformed images
* BUG: "Set Image" on token options panel doesn't always immediately repaint with the new image
* Pre-guess the screen layout
* Threaded remote display communication
* Recalibrate display - could be done using screen bounds element
* BUG: tries to pop up remote browser on the screen with the corresponding index, not the absolute screen number
* parsing display xml resets the node priorty - need to save the list model order
* BUG: LineTemplate: setting image after rotation draws the un-transformed image
* REF: Factor clear cells code into support class
* Hidden elements in table display should clear ImageMedia transforms
* ImageMedia could use a soft/weak/strong reference for transformed images
* Consider separate element for darkness cleared cells - should be parent-relative - or perhaps add to LightSource
* Allow reconnect to remote - partly works but seems to cause exception on 3rd reconnect
* Add colour to the overlay tokens. either indicator of health or settable
* Consider expanding "selected" support. Would need hierarchy support as with visibility
* Refactor common utility methods into MapElement (e.g. template creation)
* Alternate button dragging (e.g. resize, non-snapped to grid)
* Auto configure - set defaults according to OS screen layout
* Make line and spread templates editable?
* Swarm Token (editable token with replicated painting)
* dice roller element?
* thrown object scatter? compass rose?
