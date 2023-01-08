package monsters;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import digital_table.controller.TokenOptionsPanel;
import gamesystem.AbilityScore;
import gamesystem.Buff;
import gamesystem.BuffFactory;
import gamesystem.SizeCategory;
import gamesystem.core.PropertyListener;
import gamesystem.dice.HDDice;
import monsters.EncounterDialog.MonsterData;
import monsters.Monster.MonsterAttackForm;
import monsters.Monster.MonsterAttackRoutine;
import monsters.StatisticsBlock.Field;
import monsters.StatisticsBlock.HDAdvancement;
import swing.ImagePanel;
import util.XMLUtils;

@SuppressWarnings("serial")
class NamePanel extends DetailPanel {
	private Monster monster;
	private MonsterData monsterData;

	private JTextField nameField;
	private JCheckBox augSummonCheck;
	private ImagePanel imagePanel;
	private JButton prevImageButton;
	private JButton nextImageButton;
	private JPanel hdPanel;

	// XXX shared from AddMonsterDialog - this is messy
	private Map<StatisticsBlock, List<URL>> imageURLs;

	private static Buff augmentedSummoning = BuffFactory.AUGMENTED_SUMMONING.getBuff();

	NamePanel(Map<StatisticsBlock, List<URL>> urls) {
		imageURLs = urls;

		nameField = new JTextField(20);
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateName();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateName();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateName();
			}

		});

		augSummonCheck = new JCheckBox("Augmented Summoning");
		augSummonCheck.addItemListener(e -> {
			if (monster == null) return;
			if (augSummonCheck.isSelected()) {
				// if the buff has already been applied then we should not apply it again
				ListModel<Buff> buffs = monster.getBuffListModel();
				for (int i = 0; i < buffs.getSize(); i++) {
					Buff b = buffs.getElementAt(i);
					if (augmentedSummoning == b) return;
				}
				monster.addBuff(augmentedSummoning);
			} else {
				monster.removeBuff(augmentedSummoning.id);
			}
		});

		imagePanel = new ImagePanel(null);
		imagePanel.setPreferredSize(new Dimension(300, 300));

		JPanel buttonPanel = new JPanel();
		JButton imageButton = new JButton("Load Image");
		imageButton.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (TokenOptionsPanel.lastDir != null) chooser.setCurrentDirectory(TokenOptionsPanel.lastDir);
				if (chooser.showOpenDialog(NamePanel.this) == JFileChooser.APPROVE_OPTION) {
					try {
						File imageFile = chooser.getSelectedFile();
						List<URL> urls = imageURLs.get(monster.statisticsBlock);
						if (urls == null) {
							urls = new ArrayList<>();
							imageURLs.put(monster.statisticsBlock, urls);
						}
						urls.add(imageFile.toURI().toURL());
						setSelectedImage(imageURLs.size() - 1);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Cancelled");
				}
			}
		});
		buttonPanel.add(imageButton);

		prevImageButton = new JButton("<");
		prevImageButton.addActionListener(e -> setSelectedImage(monsterData.imageIndex - 1));
		buttonPanel.add(prevImageButton);

		nextImageButton = new JButton(">");
		nextImageButton.addActionListener(e -> setSelectedImage(monsterData.imageIndex + 1));
		buttonPanel.add(nextImageButton);

		JButton libraryButton = new JButton("Save to Library");
		libraryButton.addActionListener(e -> {
			if (monster == null) return;
			StatsBlockCreatureView view = StatsBlockCreatureView.getView(monster);
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				doc.appendChild(view.getXMLElement(doc));
				XMLUtils.writeDOM(doc, new PrintWriter(System.out));
				MonsterLibrary.instance.addMonster(monster);
			} catch (ParserConfigurationException x) {
				x.printStackTrace();
			}
		});

		JButton outputButton = new JButton("Write Out");
		outputButton.addActionListener(e -> {
			if (monster == null) return;
			StatsBlockCreatureView view = StatsBlockCreatureView.getView(monster);
			System.out.println(view.toString());
		});

		hdPanel = new JPanel();
		hdPanel.setLayout(new BoxLayout(hdPanel, BoxLayout.LINE_AXIS));

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0d;
		c.gridy = 0;
		c.gridx = 0;
		add(new JLabel("Name:"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5d;
		c.gridx = 1;
		add(nameField, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		c.weightx = 1.0d;
		add(augSummonCheck, c);

		c.gridy++;
		c.anchor = GridBagConstraints.WEST;
		add(hdPanel, c);

		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		JPanel p = new JPanel();
		p.add(libraryButton);
		p.add(outputButton);
		add(p, c);

		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		add(buttonPanel, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		c.gridy++;
		add(imagePanel, c);
	}

	@Override
	void setMonster(Monster m, MonsterData d) {
//		System.out.println("NamePanel.setMonster('" + m.getName() + "')");
		if (monster == m) return;
		if (monster != null) {
			//monster.setName(nameField.getText());	// should be unnecessary
			monster.removePropertyListener("name", listener);
			hdPanel.removeAll();
		}
		monster = m;
		monsterData = d;
		if (m != null) {
			nameField.setEnabled(true);
			nameField.setText(m.getName());
			monster.addPropertyListener("name", listener);
			setSelectedImage(monsterData.imageIndex);

			augSummonCheck.setSelected(false);
			ListModel<Buff> buffs = monster.getBuffListModel();
			for (int i = 0; i < buffs.getSize(); i++) {
				Buff b = buffs.getElementAt(i);
				if (augmentedSummoning == b) augSummonCheck.setSelected(true);
			}

			if (monsterData.hdAdvancements.size() > 0) {
				// expects the hdAdvancement list to be ordered, and to have an entry for the standard hd and size first
				int max = monsterData.hdAdvancements.get(monsterData.hdAdvancements.size() - 1).maxHD;
				int min = monsterData.hdAdvancements.get(0).minHD;
				int initial = monster.race.getHitDiceCount();	// XXX this is probably pretty fragile given the user could manually modify hitdice
				SpinnerNumberModel spinnerModel = new SpinnerNumberModel(initial, min, max, 1);
				spinnerModel.addChangeListener(e -> advanceHD(spinnerModel));
				JSpinner hdSpinner = new JSpinner(spinnerModel);
				Dimension size = hdSpinner.getMaximumSize();
				size.width = 40;
				hdSpinner.setMaximumSize(size);
				hdPanel.add(new JLabel("Advance HD: "));
				hdPanel.add(hdSpinner);
				hdPanel.add(Box.createHorizontalGlue());
			}
		}
	}

	static class SizeChangeMods {
		int strBonus;
		int dexBonus;
		int conBonus;
		int naturalArmorBonus;

		SizeChangeMods(int s, int d, int c, int na) {
			strBonus = s;
			dexBonus = d;
			conBonus = c;
			naturalArmorBonus = na;
		}
	}

	SizeChangeMods[] sizeChangeMods = {
			new SizeChangeMods(0, -2, 0, 0),	// fine -> diminutive
			new SizeChangeMods(2, -2, 0, 0),	// diminutive -> tiny
			new SizeChangeMods(4, -2, 0, 0),	// tiny -> small
			new SizeChangeMods(4, -2, 2, 0),	// small -> medium
			new SizeChangeMods(8, -2, 4, 2),	// medium -> large
			new SizeChangeMods(8, -2, 4, 3),	// large -> huge
			new SizeChangeMods(8, 0, 4, 4),		// huge -> gargantuan
			new SizeChangeMods(8, 0, 4, 5),		// gargantuan -> colossal
	};

	// TODO perhaps better to move the actual changes to Race
	void advanceHD(SpinnerNumberModel spinnerModel) {
		int newHD = (Integer) spinnerModel.getNumber();
		SizeCategory newSize = null;
		for (HDAdvancement adv : monsterData.hdAdvancements) {
			if (adv.minHD <= newHD && adv.maxHD >= newHD) {
				newSize = adv.size;
			}
		}
		if (newSize == null) return;

		//System.out.println("-------------Set HD to " + newHD + " -> size " + newSize);
		// handles HD advancement affecting hit points, bab (automatic), saves (automatic), skills, feats, possible ability score, cr,
		// possible size change -> (affecting str, dex, con, natural armor, ac, attack, damage)
		// not currently handled: damage due to size change (adds note)
		int oldHD = monster.race.getHitDiceCount();
		AdvancementNote.addNoteForChange(monsterData, Field.ABILITIES, newHD / 4 - oldHD / 4, "point");
		AbilityScore intScore = monster.getAbilityStatistic(AbilityScore.Type.INTELLIGENCE);
		int extraFeats = newHD / 3 - oldHD / 3;
		int skillPoints = monster.race.getType().getSkillPoints();
		if (intScore == null) {
			extraFeats = 0;
			skillPoints = 0;
		} else {
			skillPoints += AbilityScore.getModifier(intScore.getRegularValue());	// TODO using getRegularValue, but we really want the value including permanent but not temporary modifiers
			if (intScore.getRegularValue() > 0 && skillPoints < 1) skillPoints = 1;
			if (skillPoints < 0) skillPoints = 0;	// shouldn't really happen as it's odd to advance a monster with int 0 and is therefore unconscious
		}
		skillPoints *= (newHD - oldHD);
		AdvancementNote.addNoteForChange(monsterData, Field.FEATS, extraFeats, "feat");
		AdvancementNote.addNoteForChange(monsterData, Field.SKILLS, skillPoints, "skill point");

		int oldSize = monster.size.getBaseSize().ordinal();

		monster.race.setHitDiceCount(newHD);
		//System.out.println("hit dice set");
		int sizeChange = newSize.ordinal() - monster.size.getBaseSize().ordinal();
		if (sizeChange != 0) {
			int direction = sizeChange/Math.abs(sizeChange);
			SizeChangeMods totalMods = new SizeChangeMods(0, 0, 0, 0);
			int start = oldSize;
			if (direction == -1) start--;
			int end = newSize.ordinal();
			if (direction == 1) end--;
			//System.out.println("Direction = " + direction + ", start = " + start + ", end = " + end);
			for (int i = start;; i += direction) {
				SizeChangeMods mods = sizeChangeMods[i];
				totalMods.strBonus += direction * mods.strBonus;
				totalMods.dexBonus += direction * mods.dexBonus;
				totalMods.conBonus += direction * mods.conBonus;
				totalMods.naturalArmorBonus += direction * mods.naturalArmorBonus;
				if (i == end) break;
			}
			//System.out.println("Size change from " + monster.size.getBaseSize() + " to " + newSize + ", " + sizeChange + " steps");
			//System.out.println("  Str: " + totalMods.strBonus + ", Dex: " + totalMods.dexBonus + ", Con: " + totalMods.conBonus + ", NA: " + totalMods.naturalArmorBonus);
			monster.size.setBaseSize(newSize);
			advanceAbility(AbilityScore.Type.STRENGTH, totalMods.strBonus);
			advanceAbility(AbilityScore.Type.DEXTERITY, totalMods.dexBonus);
			advanceAbility(AbilityScore.Type.CONSTITUTION, totalMods.conBonus);
			monster.race.setNaturalArmor(monster.getACStatistic(), monster.race.getNaturalArmor() + totalMods.naturalArmorBonus);

			increaseAttackDamage(Field.ATTACK, newSize);
			increaseAttackDamage(Field.FULL_ATTACK, newSize);
			//System.out.println("Size set");
		}

		// CR is based on the difference from the base creature. If we're advancing a creature with an already advanced stat block then there could be compounded rounding errors.
		if (monster.statisticsBlock != null) {
			//System.out.println("Base CR = "+monster.statisticsBlock.get(Field.CR));
			HDDice baseHD = monster.statisticsBlock.getRacialHD();
			// XXX could check baseHD type vs racial type
			int oldCRdelta = (oldHD - baseHD.getNumber()) / monster.race.getType().getCRProgression();
			int newCRdelta = (newHD - baseHD.getNumber()) / monster.race.getType().getCRProgression();
			int baseSize = monster.statisticsBlock.getSize().ordinal();
			int oldSizeCR = oldSize > baseSize && oldSize >= SizeCategory.LARGE.ordinal() ? 1 : 0;
			int newSizeCR = monster.size.getBaseSize().ordinal() > baseSize && monster.size.getBaseSize().ordinal() >= SizeCategory.LARGE.ordinal() ? 1 : 0;
			try {
				int cr = Integer.parseInt(StatsBlockCreatureView.getView(monster).getField(Field.CR, false));
				cr += newCRdelta - oldCRdelta + newSizeCR - oldSizeCR;
				monster.setProperty("field."+Field.CR.name(), Integer.toString(cr));
			} catch (Exception x) {
				monsterData.addNote(Field.CR, "Couldn't calculate advanced CR as unable to parse existing CR");
			}
		} else {
			// XXX I think this probably isn't possible
			monsterData.addNote(Field.CR, "Couldn't calculate advanced CR as no base statistics block exists");
		}

		// update name, if the old name matches the stats block
		String currentName = monster.getName();
		String blockName = monster.statisticsBlock.getName();
		if (currentName.equals(blockName) || currentName.startsWith("Advanced " + blockName + " (" + oldHD + " HD)")) {
			// TODO if the monster has been reverted to base HD then we should revert to the blockname
			monster.setName("Advanced " + blockName + " (" + newHD + " HD)");
		}

		//System.out.println("-------------");
	}

	private void increaseAttackDamage(Field field, SizeCategory newSize) {
		boolean unknownAttack = false;
		List<MonsterAttackRoutine> routines = null;
		if (field == Field.ATTACK) routines = monster.attackList;
		if (field == Field.FULL_ATTACK) routines = monster.fullAttackList;
		if (routines == null) throw new IllegalArgumentException("increaseAttackDamage can't be applied to field " + field);
		for (MonsterAttackRoutine r : routines) {
			for (MonsterAttackForm f : r.attackForms) {
				if (f.attack != null) {
					if (!f.attack.setSize(newSize)) {
						System.out.println("Couldn't update damage " + f.attack.getBaseDamage() + " for size change to " + newSize);
						unknownAttack = true;
					}
				} else {
					unknownAttack = true;
				}
			}
		}
		if (unknownAttack)
			monsterData.addNote(field, "Not all damage has been updated to account for size change to " + newSize);
	}

	static private class AdvancementNote {
		int number;
		String type;

		static void addNoteForChange(MonsterData monsterData, Field f, int change, String type) {
			if (change == 0) return;
			AdvancementNote note = null;

			// find existing note, if any
			List<Object> notes = monsterData.notes.get(f);
			if (notes != null) {
				for (Object n : notes) {
					if (n instanceof AdvancementNote) {
						note = (AdvancementNote) n;
						break;
					}
				}
			}

			if (note == null) {
				note = new AdvancementNote();
				note.number = change;
				note.type = type;
			} else {
				notes.remove(note);	// we remove and replace the note to trigger repainting
				note.number += change;
			}
			if (note.number != 0)	// might be zero if we're adjusting an existing note
				monsterData.addNote(f, note);
		}

		@Override
		public String toString() {
			StringBuilder text = new StringBuilder();
			text.append(number > 0 ? "Add " : "Subtract ");
			text.append(number).append(" ").append(type);
			if (number > 1) text.append("s");
			text.append(" due to HD advancement");
			return text.toString();
		}
	}

	private void advanceAbility(AbilityScore.Type type, int delta) {
		AbilityScore ability = monster.getAbilityStatistic(type);
		if (ability == null) return;
		ability.setBaseValue(ability.getBaseValue() + delta);
	}

	// handles out of range indexes
	void setSelectedImage(int index) {
		if (monster == null) return;
		List<URL> urls = imageURLs.get(monster.statisticsBlock);
		if (index < 0) index = 0;
		if (index >= urls.size()) index = urls.size() - 1;
		monsterData.imageIndex = index;
		BufferedImage image = null;
		if (index >= 0) {
			URL url = urls.get(index);
			try {
				image = ImageIO.read(url);
			} catch (Exception e) {
				System.err.println("NamePanel.setSelectedImage exception for " + monster.getName() + ": " + e);
			}
		}
		imagePanel.setImage(image);
		prevImageButton.setEnabled(index > 0);
		nextImageButton.setEnabled(index < urls.size() - 1);
	}

	private void updateName() {
		if (monster != null) {
			String newName = nameField.getText();
			if (!monster.getName().equals(newName)) monster.setName(newName);
		}
	}

	final private PropertyListener listener = e -> {
		if (!nameField.getText().equals(e.source.getValue().toString())) {
			nameField.setText(e.source.getValue().toString());
		}
	};
}