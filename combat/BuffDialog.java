package combat;

import gamesystem.Buff;
import gamesystem.Creature;
import gamesystem.Spell;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import party.Character;
import swing.JListWithToolTips;
import ui.BuffUI;
import ui.BuffUI.BuffEntry;
import ui.BuffUI.BuffListModel;

@SuppressWarnings("serial")
class BuffDialog extends JDialog {
	private JComponent owner;
	private BuffUI ui;
	private EffectSourceModel sourceModel;

	private boolean okSelected = false;
	private Map<JCheckBox, Creature> targets = new HashMap<>();

	BuffDialog(JComponent own, InitiativeListModel ilm) {
		super(SwingUtilities.windowForComponent(own), "Choose buff", Dialog.ModalityType.DOCUMENT_MODAL);
		owner = own;

		ui = new BuffUI();

		JListWithToolTips<BuffEntry> buffs = ui.getBuffList(true);
		buffs.addListSelectionListener(e -> pack());

		JScrollPane scroller = new JScrollPane(buffs);
		//scroller.setPreferredSize(preferredSize);

		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
		right.add(ui.getOptionsPanel());

		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		right.add(separator);

		for (int i = 0; i < ilm.getSize(); i++) {
			CombatEntry e = ilm.getElementAt(i);
			if (!e.blank) {
				JCheckBox cb = new JCheckBox(e.creature.getName());
				right.add(cb);
				targets.put(cb, e.creature);
			}
		}

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(e -> {
			dispose();
			okSelected = true;
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());

		JPanel buttons = new JPanel();
		buttons.add(okButton);
		buttons.add(cancelButton);

		add(buttons, BorderLayout.SOUTH);
		buffs.setSelectedIndex(0);

		JTextField filter = new JTextField();
		filter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateFilter();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateFilter();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateFilter();
			}

			private void updateFilter() {
				BuffUI.BuffListModel model = (BuffListModel) buffs.getModel();
				String text = filter.getText().toLowerCase();
				if (text.length() == 0) {
					model.filter((BuffEntry e) -> true);
				} else {
					model.filter((BuffEntry e) -> e.name.toLowerCase().contains(text));
				}
			}
		});
		JCheckBox casterOnly = new JCheckBox("Caster Only");

		sourceModel = new EffectSourceModel(ilm);
		JComboBox<Object> sourceField = new JComboBox<>(sourceModel);
		sourceField.setEditable(true);
		sourceField.addActionListener((e) -> {
			if (sourceField.getSelectedIndex() != -1) {
				CombatEntry ce = ilm.getElementAt(sourceField.getSelectedIndex());
				if (ce.creature instanceof Character) {
					Character c = (Character)ce.creature;
					ui.setCasterLevel(c.getLevel());
				}
			}
		});

		JButton infoButton = new JButton("Info");
		infoButton.addActionListener(e -> popupInfo(buffs.getSelectedValue()));

		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		pane.add(filter, c);

		c.gridy = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.anchor = GridBagConstraints.LINE_START;
		pane.add(casterOnly, c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_END;
		pane.add(infoButton, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 1;
		pane.add(scroller, c);

		c.gridx = 2;
		c.gridy = 0;
		c.weighty = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		pane.add(new JLabel("Caster: "));

		c.weightx = 0.5;
		c.weighty = 1;
		c.gridheight = 2;
		c.gridwidth = 2;
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		pane.add(right, c);

		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		pane.add(sourceField, c);

		add(pane);

		pack();
		setLocationRelativeTo(SwingUtilities.getWindowAncestor(owner));
		setVisible(true);
	}

	private void popupInfo(BuffEntry buff) {
		if (buff.source instanceof Spell) {
			Spell s = (Spell) buff.source;

			StringWriter output = new StringWriter();
			try {
				Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new File("rulesets/core3.5/Spells.xsl")));
				transformer.transform(new DOMSource(s.domNode), new StreamResult(output));
			} catch (TransformerFactoryConfigurationError | TransformerException e) {
				e.printStackTrace();
			}

			JEditorPane p = new JEditorPane();
			p.setEditable(false);
			p.setContentType("text/html; charset=utf-8");
			p.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
			p.setText(output.toString());

			JScrollPane sp = new JScrollPane(p);
			sp.setSize(new Dimension(800, 600));
			sp.setPreferredSize(new Dimension(800, 600));

			JFrame frame = new JFrame(s.name);
			frame.add(sp);
			frame.setSize(new Dimension(800, 600));
			frame.pack();
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
		}
	}

	Buff getBuff() {
		if (okSelected) return ui.getBuff();
		return null;
	}

	String getSourceName() {
		if (okSelected) return sourceModel.getSelectedItem().toString();
		return null;
	}

	Set<Creature> getTargets() {
		HashSet<Creature> targetted = new HashSet<>();
		if (okSelected) {
			for (JCheckBox cb : targets.keySet()) {
				if (cb.isSelected()) targetted.add(targets.get(cb));
			}
		}
		return targetted;
	}
}
