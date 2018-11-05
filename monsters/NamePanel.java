package monsters;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
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
import monsters.EncounterDialog.MonsterData;
import monsters.StatisticsBlock.HDAdvancement;
import swing.ImagePanel;
import util.XMLUtils;

@SuppressWarnings("serial")
class NamePanel extends DetailPanel {
	private Monster monster;
	private MonsterData customisation;

	private JTextField nameField;
	private JCheckBox augSummonCheck;
	private ImagePanel imagePanel;
	private JButton prevImageButton;
	private JButton nextImageButton;
	private JPanel hdPanel;

	// XXX shared from AddMonsterDialog - this is messy
	private Map<StatisticsBlock, List<URL>> imageURLs;
	private Map<Monster, MonsterData> customisations;

	private static Buff augmentedSummoning = BuffFactory.AUGMENTED_SUMMONING.getBuff();

	NamePanel(Map<StatisticsBlock, List<URL>> urls, Map<Monster, MonsterData> customisations) {
		imageURLs = urls;
		this.customisations = customisations;

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
		prevImageButton.addActionListener(e -> setSelectedImage(customisation.imageIndex - 1));
		buttonPanel.add(prevImageButton);

		nextImageButton = new JButton(">");
		nextImageButton.addActionListener(e -> setSelectedImage(customisation.imageIndex + 1));
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

		hdPanel = new JPanel();

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
		add(hdPanel, c);

		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		add(libraryButton, c);

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
	void setMonster(Monster m) {
//		System.out.println("NamePanel.setMonster('" + m.getName() + "')");
		if (monster == m) return;
		if (monster != null) {
			//monster.setName(nameField.getText());	// should be unnecessary
			monster.removePropertyListener("name", listener);
		}
		monster = m;
		if (m != null) {
			customisation = customisations.get(monster);
			nameField.setEnabled(true);
			nameField.setText(m.getName());
			monster.addPropertyListener("name", listener);
			setSelectedImage(customisation.imageIndex);

			augSummonCheck.setSelected(false);
			ListModel<Buff> buffs = monster.getBuffListModel();
			for (int i = 0; i < buffs.getSize(); i++) {
				Buff b = buffs.getElementAt(i);
				if (augmentedSummoning == b) augSummonCheck.setSelected(true);
			}

			if (customisation.hdAdvancements.size() > 0) {
				// expects the hdAdvancement list to be ordered, and to have an entry for the standard hd and size first
				int max = customisation.hdAdvancements.get(customisation.hdAdvancements.size() - 1).maxHD;
				int min = customisation.hdAdvancements.get(0).minHD;
				int initial = monster.race.getHitDiceCount();	// XXX this is probably pretty fragile given the user could manually modify hitdice
				SpinnerNumberModel spinnerModel = new SpinnerNumberModel(initial, min, max, 1);
				spinnerModel.addChangeListener(e -> advanceHD(spinnerModel));
				JSpinner hdSpinner = new JSpinner(spinnerModel);
				hdPanel.add(hdSpinner);
			} else {
				hdPanel.removeAll();
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
		for (HDAdvancement adv : customisation.hdAdvancements) {
			if (adv.minHD <= newHD && adv.maxHD >= newHD) {
				newSize = adv.size;
			}
		}
		if (newSize != null) {
			System.out.println("-------------Set HD to " + newHD + " -> size " + newSize);
			// * possible size change -> str, dex, con, natural armor, ac, attack
			// possible size change -> damage
			// hit points
			// * bab (automatic)
			// * saves (automatic)
			// * skills
			// * feats
			// * possible ability score
			// cr
			int oldHD = monster.race.getHitDiceCount();
			AbilityScore intScore = monster.getAbilityStatistic(AbilityScore.Type.INTELLIGENCE);
			int statIncreases = newHD / 4 - oldHD / 4;
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
			System.out.println("Extra ability points = " + statIncreases + ", extra feats = " + extraFeats + ", extra skills = " + skillPoints);
			monster.race.setHitDiceCount(newHD);
			int sizeChange = newSize.ordinal() - monster.size.getBaseSize().ordinal();
			if (sizeChange != 0) {
				int direction = sizeChange/Math.abs(sizeChange);
				SizeChangeMods totalMods = new SizeChangeMods(0, 0, 0, 0);
				int start = monster.size.getBaseSize().ordinal();
				if (direction == -1) start--;
				int end = newSize.ordinal();
				if (direction == 1) end--;
				System.out.println("Direction = " + direction + ", start = " + start + ", end = " + end);
				for (int i = start;; i += direction) {
					SizeChangeMods mods = sizeChangeMods[i];
					totalMods.strBonus += direction * mods.strBonus;
					totalMods.dexBonus += direction * mods.dexBonus;
					totalMods.conBonus += direction * mods.conBonus;
					totalMods.naturalArmorBonus += direction * mods.naturalArmorBonus;
					if (i == end) break;
				}
				System.out.println("Size change from " + monster.size.getBaseSize() + " to " + newSize + ", " + sizeChange + " steps");
				System.out.println("  Str: " + totalMods.strBonus + ", Dex: " + totalMods.dexBonus + ", Con: " + totalMods.conBonus + ", NA: " + totalMods.naturalArmorBonus);
				monster.size.setBaseSize(newSize);
				advanceAbility(AbilityScore.Type.STRENGTH, totalMods.strBonus);
				advanceAbility(AbilityScore.Type.DEXTERITY, totalMods.dexBonus);
				advanceAbility(AbilityScore.Type.CONSTITUTION, totalMods.conBonus);
				monster.race.setNaturalArmor(monster.getACStatistic(), monster.race.getNaturalArmor() + totalMods.naturalArmorBonus);
			}
			System.out.println("-------------");
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
		customisation.imageIndex = index;
		BufferedImage image = null;
		if (index >= 0) {
			URL url = urls.get(index);
			try {
				image = ImageIO.read(url);
			} catch (IOException e) {
				e.printStackTrace();
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