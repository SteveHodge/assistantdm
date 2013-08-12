package combat;
import gamesystem.Buff;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

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

import party.Character;

// TODO consider moving into CombatPanel class

@SuppressWarnings("serial")
class NewEffectPanel extends JPanel {
	private JTextField effectField;
	private JComboBox sourceField;
	private JFormattedTextField initField;
	private JFormattedTextField durationField;
	private JComboBox unitsField;
	private JButton addButton;
	private JButton buffButton;
	private JButton deleteButton;

	//protected EffectListModel model;
	private EffectTableModel model;
	private SourceModel sourceModel;
	private JTable table;

	private Buff buff = null;
	private Set<Character> targets;

	private class SourceModel implements ComboBoxModel {
		private InitiativeListModel initiativeModel;
		private String selected = "";
		private EventListenerList listenerList = new EventListenerList();

		private SourceModel(InitiativeListModel ilm) {
			initiativeModel = ilm;
			ilm.addListDataListener(new ListDataListener() {
				// forward list changes to listeners
				@Override
				public void contentsChanged(ListDataEvent e) {
					fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,e.getIndex0(),e.getIndex1());
				}

				@Override
				public void intervalAdded(ListDataEvent e) {
					fireListDataEvent(ListDataEvent.INTERVAL_ADDED,e.getIndex0(),e.getIndex1());
				}

				@Override
				public void intervalRemoved(ListDataEvent e) {
					fireListDataEvent(ListDataEvent.INTERVAL_REMOVED,e.getIndex0(),e.getIndex1());
				}
			});
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}

		@Override
		public void setSelectedItem(Object arg0) {
			if (arg0 != null) selected = arg0.toString();
			else selected = "";
			fireListDataEvent(ListDataEvent.CONTENTS_CHANGED,-1,-1);

		}

		@Override
		public Object getElementAt(int arg0) {
			return initiativeModel.getElementAt(arg0).getCreatureName();
		}

		@Override
		public int getSize() {
			return initiativeModel.getSize();
		}

		private int getInitiative(int index) {
			CombatEntry e = initiativeModel.getElementAt(index);
			return e.getTotal();
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listenerList.add(ListDataListener.class,l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listenerList.remove(ListDataListener.class,l);
		}

		private void fireListDataEvent(int type, int index0, int index1) {
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

	NewEffectPanel(JTable t, final EffectTableModel m, final InitiativeListModel im) {
		model = m;
		table = t;
		sourceModel = new SourceModel(im);

		effectField = new JTextField();
		//effectField.setColumns(100);

		sourceField = new JComboBox(sourceModel);
		sourceField.setEditable(true);
		sourceField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = sourceField.getSelectedIndex();
				if (index != -1) {
					initField.setValue(new Integer(sourceModel.getInitiative(index)));
				}
			}
		});

		initField = new JFormattedTextField();
		initField.setValue(new Integer(0));
		initField.setColumns(3);

		durationField = new JFormattedTextField();
		durationField.setValue(new Integer(0));
		durationField.setColumns(3);

		String [] units = {"Rounds","Minutes","Hours"};
		unitsField = new JComboBox(units);

		buffButton = new JButton("Buff...");
		buffButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BuffDialog dialog = new BuffDialog(NewEffectPanel.this, im);
				buff = dialog.getBuff();
				if (buff != null) {
					effectField.setText(buff.name);
					targets = dialog.getTargets();
				}
			}
		});

		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int d = (Integer) durationField.getValue();
				if (unitsField.getSelectedItem().equals("Minutes")) d *= 10;
				if (unitsField.getSelectedItem().equals("Hours")) d *= 600;

				if (buff != null) {
					for (Character c : targets) {
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
			}
		});

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRows[] = table.getSelectedRows();
				// XXX is selected[] always sorted?
				for (int s = selectedRows.length - 1; s >= 0; s--) {
					int index = table.convertRowIndexToModel(selectedRows[s]);
					m.removeEffect(index, NewEffectPanel.this, im);
				}
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
