package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import digital_table.elements.DarknessMask;
import digital_table.elements.MapElement;
import digital_table.server.TableDisplay;

@SuppressWarnings("serial")
public class DarknessMaskOptionsPanel extends OptionsPanel {
	DarknessMask darkness;
	JPanel colorPanel;
	JSlider alphaSlider;
	JCheckBox lowLightCheck;

	public DarknessMaskOptionsPanel(MapElement parent, TableDisplay r) {
		super(r);
		darkness = new DarknessMask();
		darkness.setProperty(MapElement.PROPERTY_VISIBLE, true);
		sendElement(darkness, parent);
		darkness.setProperty(DarknessMask.PROPERTY_ALPHA, 0.5f);
		darkness.addPropertyChangeListener(listener);

		colorPanel = createColorControl(darkness, DarknessMask.PROPERTY_COLOR);
		alphaSlider = createSliderControl(darkness, DarknessMask.PROPERTY_ALPHA, Mode.LOCAL);
		lowLightCheck = this.createCheckBox(darkness, DarknessMask.PROPERTY_LOW_LIGHT, Mode.BOTH, "Lowlight vision");
		JCheckBox visibleCheck = createVisibilityControl(darkness);
		visibleCheck.setSelected(true);

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridy++; add(new JLabel("Colour:"), c);
		c.gridy++; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = 0; add(visibleCheck, c);
		c.gridy++; add(colorPanel, c);
		c.gridy++; add(alphaSlider, c);
		c.gridy++; add(lowLightCheck, c);

		c.fill = GridBagConstraints.BOTH; c.weighty = 1.0d;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		add(new JPanel(), c);
		//@formatter:on
	}

	@Override
	public DarknessMask getElement() {
		return darkness;
	}

	protected PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(DarknessMask.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(DarknessMask.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(DarknessMask.PROPERTY_LOW_LIGHT)) {
				lowLightCheck.setSelected((Boolean) e.getNewValue());

			} else {
				System.out.println("Unknown property: "+e.getPropertyName());
			}
		}
	};

	@Override
	public MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	protected MapElementMouseListener mouseListener = new MapElementMouseListener() {
		protected boolean dragging = false;
		protected int button;
		protected boolean dragClear;	// when dragging if true then we clear cells, otherwise we reset cells

		protected void setMasked(Point p, boolean mask) {
			darkness.setMasked(p, mask);
			if (mask) {
				setRemote(darkness.getID(), DarknessMask.PROPERTY_MASKCELL, p);
			} else {
				setRemote(darkness.getID(), DarknessMask.PROPERTY_UNMASKCELL, p);
			}
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {
			button = e.getButton();
			Point p = new Point((int)gridloc.getX(), (int)gridloc.getY());
			dragClear = darkness.isMasked(p);
		}

		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				Point p = new Point((int)gridloc.getX(), (int)gridloc.getY());
				setMasked(p, !dragClear);	// TODO might not be necessary - not sure if a mouseDragged event is generated or not for location of the release
				dragging = false;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;

			Point p = new Point((int)gridloc.getX(), (int)gridloc.getY());
			setMasked(p, !darkness.isMasked(p));
		}

		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				Point p = new Point((int)gridloc.getX(), (int)gridloc.getY());
				setMasked(p, !dragClear);
			} else if (button == MouseEvent.BUTTON1) {
				dragging = true;
			}
		}
	};
}
