import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListDataListener;

import party.Party;

// TODO need to get character's initiative if one is selected
// TODO either have minute/hour increment or allow editing remaining duration of entries

@SuppressWarnings("serial")
public class NewEffectPanel extends JPanel implements ActionListener {
	protected JTextField effectField;
	protected JComboBox sourceField;
	protected JFormattedTextField initField;
	protected JFormattedTextField durationField;
	protected JComboBox unitsField;
	protected JButton addButton;
	protected JButton resetButton;
	protected JButton nextButton;

	protected EffectListModel model;
	protected SourceModel sourceModel;

	public class SourceModel implements ComboBoxModel {
		InitiativeListModel initiativeModel;
		String selected = "";

		SourceModel(InitiativeListModel ilm) {
			initiativeModel = ilm;
		}

		public Object getSelectedItem() {
			return selected;
		}

		public void setSelectedItem(Object arg0) {
			selected = arg0.toString();
		}

		public void addListDataListener(ListDataListener arg0) {
			initiativeModel.addListDataListener(arg0);
		}

		public Object getElementAt(int arg0) {
			return ((InitiativeEntry)initiativeModel.getElementAt(arg0)).getName();
		}

		public int getSize() {
			return initiativeModel.getSize();
		}

		public void removeListDataListener(ListDataListener arg0) {
			initiativeModel.removeListDataListener(arg0);
		}

		public int getInitiative(int index) {
			InitiativeEntry e = (InitiativeEntry)initiativeModel.getElementAt(index);
			return e.getTotal();
		}
	}

	public NewEffectPanel(Party p, EffectListModel m, InitiativeListModel im) {
		model = m;
		sourceModel = new SourceModel(im);

		effectField = new JTextField();
		//effectField.setColumns(100);

		sourceField = new JComboBox(sourceModel);
		sourceField.setEditable(true);
		sourceField.addActionListener(this);

		initField = new JFormattedTextField();
		initField.setValue(new Integer(0));
		initField.setColumns(3);

		durationField = new JFormattedTextField();
		durationField.setValue(new Integer(0));
		durationField.setColumns(3);

		String [] units = {"Rounds","Minutes","Hours"};
		unitsField = new JComboBox(units);

		addButton = new JButton("Add");
		addButton.addActionListener(this);
		resetButton = new JButton("Clear All");
		resetButton.addActionListener(this);
		nextButton = new JButton("Next Round");
		nextButton.addActionListener(this);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		c.weightx = 1.0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(effectField, c);
		c.gridwidth = 1;
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		add(durationField, c);
		add(unitsField, c);

		c.gridy = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(sourceField, c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		add(initField, c);
		add(addButton, c);
		add(resetButton, c);
		add(nextButton, c);
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == addButton) {
			int d = (Integer)durationField.getValue();
			if (unitsField.getSelectedItem().equals("Minutes")) d *= 10;
			if (unitsField.getSelectedItem().equals("Hours")) d *= 600;
			
			model.addEntry(effectField.getText(), sourceField.getSelectedItem().toString(),
					(Integer)initField.getValue(), d);
			model.sort();

		} else if (arg0.getSource() == resetButton) {
			model.clear();
			
		} else if (arg0.getSource() == nextButton) {
			// work from last to first to avoid issues when we remove entries
			for (int i=model.getSize()-1; i >= 0; i--) {
				EffectEntry e = (EffectEntry)model.getElementAt(i);
				e.setDuration(e.getDuration()-1);
				if (e.getDuration() < 0) model.removeEntry(e);
			}

		} else if (arg0.getSource() == sourceField) {
			int index = sourceField.getSelectedIndex();
			if (index != -1) {
				initField.setValue(new Integer(sourceModel.getInitiative(index)));
			}
		}
	}
}
