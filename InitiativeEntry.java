import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;

import party.Character;

@SuppressWarnings("serial")
public class InitiativeEntry extends JPanel implements PropertyChangeListener {
	JFormattedTextField rollField;
	JFormattedTextField modifierField;
	JFormattedTextField tiebreakField;
	JLabel total;
	JCheckBox onlyDM;
	JTextField nameField;
	JLabel nameLabel;
	boolean blank = true;

	Character character = null;

	EventListenerList listenerList = new EventListenerList();
	ChangeEvent changeEvent = null;

	public InitiativeEntry(Character c) {
		character = c;
		blank = false;
		createPanel();
	}

	public InitiativeEntry() {
		createPanel();
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	// Note: marks the entry as having been changed if it was blank, and if it has the roll is 0, randomly rolls
	protected void fireChange() {
		if (blank) {
			blank = false;
			if (rollField.getValue().equals(0)) {
				Random r = new Random();
				rollField.setValue(r.nextInt(20)+1);
			}
		}

		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ChangeListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
			}
		}
	}

	private void createPanel() {
		setLayout(new GridBagLayout());

		onlyDM = new JCheckBox();
		onlyDM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireChange();
			}
		});

		rollField = new JFormattedTextField();
		rollField.setValue(new Integer(0));
		rollField.setColumns(3);
		rollField.addPropertyChangeListener("value", this);

		modifierField = new JFormattedTextField();
		Integer mod;
		if( character == null) {
			mod = new Integer(0);
		} else {
			mod = new Integer(character.getInitiativeModifier());
		}
		modifierField.setValue(mod);
		modifierField.setColumns(3);
		modifierField.addPropertyChangeListener("value", this);

		tiebreakField = new JFormattedTextField();
		tiebreakField.setValue(new Integer(0));
		tiebreakField.setColumns(3);
		tiebreakField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				fireChange();
			}
		});

		total = new JLabel("= XXX");
		Dimension size = total.getPreferredSize();
		total.setPreferredSize(size);
		total.setText("= "+mod);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		add(onlyDM, c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		if (character == null) {
			nameField = new JTextField(20);
			nameField.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					fireChange();
				}

				public void insertUpdate(DocumentEvent e) {
					fireChange();
				}

				public void removeUpdate(DocumentEvent e) {
					fireChange();
				}
				
			});
			add(nameField, c);
		} else {
			onlyDM.setSelected(true);
			modifierField.setValue(new Integer(character.getInitiativeModifier()));
			nameLabel = new JLabel(character.getName());
			add(nameLabel, c);
		}
		c.weightx = 0.0;
		add(rollField, c);
		add(new JLabel("+"), c);
		add(modifierField, c);
		add(total, c);
		add(tiebreakField, c);
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == modifierField && character != null) {
			character.setInitiativeModifier((Integer)modifierField.getValue());
		}
		total.setText("= "+getTotal());
		fireChange();
	}

	public int getTotal() {
		int tot = (Integer)rollField.getValue();
		tot += (Integer)modifierField.getValue();
		return tot;
	}

	public int getModifier() {
		if (character != null) return character.getInitiativeModifier();
		return (Integer)modifierField.getValue();
	}

	public int getRoll() {
		return (Integer)rollField.getValue();
	}

	public void setRoll(int roll) {
		rollField.setValue(roll);
	}

	public void adjustRoll(int delta) {
		rollField.setValue(getRoll()+delta);
	}

	public String getName() {
		if (character == null) return nameField.getText();
		return character.getName();
	}

	public boolean isDMOnly() {
		return !onlyDM.isSelected();
	}

	public String toString() {
		return "InitiativeEntry (name="+getName()+", roll="+rollField.getValue()
			+", modifier="+getModifier()+", tiebreak="+tiebreakField.getValue()+")";
	}
}
