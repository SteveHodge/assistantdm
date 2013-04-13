package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import digital_table.server.TableDisplay;
import digital_table.elements.Grid;

@SuppressWarnings("serial")
public class GridOptionsPanel extends OptionsPanel {
	Grid grid;
	JTextField rulerRowField;
	JTextField rulerColumnField;
	JPanel colorPanel;
	JPanel bgColorPanel;
	JSlider alphaSlider;

	public GridOptionsPanel(Grid g, TableDisplay r) {
		super(r);
		grid = g;
		grid.addPropertyChangeListener(listener);

		rulerRowField = new JTextField(8);
		if (grid.getRulerRow() != null) rulerRowField.setText(""+grid.getRulerRow());
		rulerRowField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Integer newRow = null;
					if (rulerRowField.getText().length() > 0) newRow = Integer.parseInt(rulerRowField.getText());
					//grid.setRulerRow(newRow);
					remote.setElementProperty(grid.getID(), Grid.PROPERTY_RULER_ROW, newRow);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		
		rulerColumnField = new JTextField(8);
		if (grid.getRulerColumn() != null) rulerColumnField.setText(""+grid.getRulerColumn());
		rulerColumnField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Integer newColumn= null;
					if (rulerColumnField.getText().length() > 0) newColumn = Integer.parseInt(rulerColumnField.getText());
					//grid.setRulerColumn(newColumn);
					remote.setElementProperty(grid.getID(), Grid.PROPERTY_RULER_COLUMN, newColumn);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		
		colorPanel = createColorControl(grid, Grid.PROPERTY_COLOR);
		bgColorPanel = createColorControl(grid, Grid.PROPERTY_BACKGROUND_COLOR);
		alphaSlider = this.createSliderControl(grid, Grid.PROPERTY_ALPHA);
		
		JCheckBox visibleCheck = createVisibilityControl(grid);
		visibleCheck.setSelected(true);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy = 1; add(new JLabel("Ruler Row:"), c);
		c.gridy = 2; add(new JLabel("Ruler Column:"), c);
		c.gridy = 3; add(new JLabel("Colour:"), c);
		c.gridy = 4; add(new JLabel("Background:"), c);
		c.gridy = 5; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 1; add(rulerRowField, c);
		c.gridy = 2; add(rulerColumnField, c);
		c.gridy = 3; add(colorPanel, c);
		c.gridy = 4; add(bgColorPanel, c);
		c.gridy = 5; add(alphaSlider, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
		add(new JPanel(), c);
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Grid.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));
				
			} else if (e.getPropertyName().equals(Grid.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());
				
			} else if (e.getPropertyName().equals(Grid.PROPERTY_BACKGROUND_COLOR)) {
				bgColorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(Grid.PROPERTY_RULER_ROW)) {
				rulerRowField.setText(e.getNewValue().toString());
				
			} else if (e.getPropertyName().equals(Grid.PROPERTY_RULER_COLUMN)) {
				rulerColumnField.setText(e.getNewValue().toString());
				
			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};
}
