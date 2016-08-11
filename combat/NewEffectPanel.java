package combat;
import gamesystem.Buff;
import gamesystem.Creature;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;


// TODO consider moving into CombatPanel class

@SuppressWarnings("serial")
class NewEffectPanel extends JPanel {
	private JTextField effectField;
	private JComboBox<Object> sourceField;
	private JFormattedTextField initField;
	private JFormattedTextField durationField;
	private JComboBox<String> unitsField;
	private JButton addButton;
	private JButton buffButton;
	private JButton deleteButton;

	//protected EffectListModel model;
	private EffectTableModel model;
	private EffectSourceModel sourceModel;
	private JTable table;

	private Buff buff = null;
	private Set<Creature> targets;

	NewEffectPanel(JTable t, final EffectTableModel m, final InitiativeListModel im) {
		model = m;
		table = t;
		sourceModel = new EffectSourceModel(im);

		effectField = new JTextField();
		//effectField.setColumns(100);

		sourceField = new JComboBox<>(sourceModel);
		sourceField.setEditable(true);
		sourceField.addActionListener(e -> {
			int index = sourceField.getSelectedIndex();
			if (index != -1) {
				initField.setValue(new Integer(sourceModel.getInitiative(index)));
			}
		});

		initField = new JFormattedTextField();
		initField.setValue(new Integer(0));
		initField.setColumns(3);

		durationField = new JFormattedTextField();
		durationField.setValue(new Integer(0));
		durationField.setColumns(3);

		String [] units = {"Rounds","Minutes","Hours"};
		unitsField = new JComboBox<>(units);

		buffButton = new JButton("Buff...");
		buffButton.addActionListener(e -> {
			BuffDialog dialog = new BuffDialog(NewEffectPanel.this, im);
			if (dialog.okSelected()) {
				buff = dialog.getBuff();
				effectField.setText(buff.name);
				targets = dialog.getTargets();
				durationField.setValue(dialog.getDuration());
				unitsField.setSelectedItem(dialog.getDurationUnit());
				sourceField.setSelectedItem(dialog.getSourceName());
			}
		});

		addButton = new JButton("Add");
		addButton.addActionListener(e -> {
			int d = (Integer) durationField.getValue();
			if (unitsField.getSelectedItem().equals("Minutes")) d *= 10;
			if (unitsField.getSelectedItem().equals("Hours")) d *= 600;

			if (buff != null) {
				for (Creature c : targets) {
					c.addBuff(buff);
				}
			}
			model.addEntry(effectField.getText(), sourceField.getSelectedItem().toString(),
					(Integer) initField.getValue(), d, buff);
			//model.sort();

			if (buff != null) {
				//reset the buff so we don't add it again next time
				effectField.setText("");
				buff = null;
			}
		});

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> {
			int selectedRows[] = table.getSelectedRows();
			// XXX is selected[] always sorted?
			for (int s = selectedRows.length - 1; s >= 0; s--) {
				int index = table.convertRowIndexToModel(selectedRows[s]);
				m.removeEffect(index, NewEffectPanel.this, im);
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		add(new JLabel("Effect:"),c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(effectField, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		//c.fill = GridBagConstraints.NONE;
		add(new JLabel("Duration:"),c);
		add(durationField, c);
		add(unitsField, c);

		c.gridy = 1;
		c.gridx = 0;
		add(new JLabel("Source:"),c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(sourceField, c);
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		add(new JLabel("Initiative:"),c);
		add(initField, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		add(buffButton, c);
		add(addButton, c);
		add(deleteButton, c);
	}
}
