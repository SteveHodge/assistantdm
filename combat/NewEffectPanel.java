package combat;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

// TODO consider moving into CombatPanel class

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
	protected JButton deleteButton;

	//protected EffectListModel model;
	protected EffectTableModel model;
	protected SourceModel sourceModel;
	protected JTable table;

	public class SourceModel implements ComboBoxModel {
		InitiativeListModel initiativeModel;
		String selected = "";
		EventListenerList listenerList = new EventListenerList();

		SourceModel(InitiativeListModel ilm) {
			initiativeModel = ilm;
			ilm.addListDataListener(new ListDataListener() {
				// forward list changes to listeners
				public void contentsChanged(ListDataEvent e) {
					fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,e.getIndex0(),e.getIndex1());
				}

				public void intervalAdded(ListDataEvent e) {
					fireListDataEvent(ListDataEvent.INTERVAL_ADDED,e.getIndex0(),e.getIndex1());
				}

				public void intervalRemoved(ListDataEvent e) {
					fireListDataEvent(ListDataEvent.INTERVAL_REMOVED,e.getIndex0(),e.getIndex1());
				}
			});
		}

		public Object getSelectedItem() {
			return selected;
		}

		public void setSelectedItem(Object arg0) {
			if (arg0 != null) selected = arg0.toString();
			else selected = "";
			fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,-1,-1);
			
		}

		public Object getElementAt(int arg0) {
			return ((CombatEntry)initiativeModel.getElementAt(arg0)).getCreatureName();
		}

		public int getSize() {
			return initiativeModel.getSize();
		}

		public int getInitiative(int index) {
			CombatEntry e = (CombatEntry)initiativeModel.getElementAt(index);
			return e.getTotal();
		}

		public void addListDataListener(ListDataListener l) {
			listenerList.add(ListDataListener.class,l);
		}

		public void removeListDataListener(ListDataListener l) {
			listenerList.remove(ListDataListener.class,l);
		}

		protected void fireListDataEvent(int type, int index0, int index1) {
			ListDataEvent e = null;
			Object[] listeners = listenerList.getListenerList();
			for (int i = listeners.length-2; i>=0; i-=2) {
				if (listeners[i]==ListDataListener.class) {
					if (e == null) e = new ListDataEvent(this, type, index0, index1);
					switch(type) {
					case ListDataEvent.CONTENTS_CHANGED:
						((ListDataListener)listeners[i+1]).contentsChanged(e);
						break;
					case ListDataEvent.INTERVAL_ADDED:
						((ListDataListener)listeners[i+1]).intervalAdded(e);
						break;
					case ListDataEvent.INTERVAL_REMOVED:
						((ListDataListener)listeners[i+1]).intervalRemoved(e);
						break;
					}
				}
			}
		}
	}

	public NewEffectPanel(JTable t, EffectTableModel m, InitiativeListModel im) {
		model = m;
		table = t;
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
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(this);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		add(new JLabel("Effect:"),c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.gridwidth = 2;
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
		add(addButton, c);
		add(deleteButton, c);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addButton) {
			int d = (Integer)durationField.getValue();
			if (unitsField.getSelectedItem().equals("Minutes")) d *= 10;
			if (unitsField.getSelectedItem().equals("Hours")) d *= 600;
			
			model.addEntry(effectField.getText(), sourceField.getSelectedItem().toString(),
					(Integer)initField.getValue(), d);
			//model.sort();

		} else if (e.getSource() == sourceField) {
			int index = sourceField.getSelectedIndex();
			if (index != -1) {
				initField.setValue(new Integer(sourceModel.getInitiative(index)));
			}

		} else if (e.getSource() == deleteButton) {
			int selected = table.getSelectedRow();
			while (selected > -1) {
				model.removeEntry(selected);
				selected = table.getSelectedRow();
			}
		}
		
	}
}
