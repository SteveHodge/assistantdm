package digital_table.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import digital_table.elements.Callibrate;
import digital_table.elements.MapElement;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class CallibrateOptionsPanel extends OptionsPanel {
	Callibrate callibrate;
	JCheckBox visibleCheck;
	JCheckBox showBGCheck;

	public CallibrateOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		callibrate = new Callibrate();
		sendElement(callibrate, parent);
		callibrate.addPropertyChangeListener(listener);

		showBGCheck = createCheckBox(callibrate, Callibrate.PROPERTY_SHOW_BACKGROUND, Mode.BOTH, "show background?");
		visibleCheck = createCheckBox(callibrate, MapElement.PROPERTY_VISIBLE, Mode.BOTH, "visible?");

		// @formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(showBGCheck, c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 1;

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
		// @formatter:on
	}

	@Override
	public Callibrate getElement() {
		return callibrate;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Callibrate.PROPERTY_SHOW_BACKGROUND)) {
				showBGCheck.setSelected((Boolean) e.getNewValue());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};
}
