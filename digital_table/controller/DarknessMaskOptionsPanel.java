package digital_table.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import digital_table.controller.DisplayManager.Mode;
import digital_table.elements.DarknessMask;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;

@SuppressWarnings("serial")
class DarknessMaskOptionsPanel extends OptionsPanel<DarknessMask> {
	private JPanel colorPanel;
	private JSlider alphaSlider;
	private JCheckBox lowLightCheck;
	private JCheckBox visibleCheck;
	private JCheckBox trackHistory;
	private JPanel historyColorPanel;
	private JButton resetHistory;

	DarknessMaskOptionsPanel(MapElement parent, DisplayManager r) {
		super(r);
		element = new DarknessMask();
		element.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.VISIBLE);
		display.addElement(element, parent);
		element.setProperty(DarknessMask.PROPERTY_ALPHA, 0.5f);
		element.addPropertyChangeListener(listener);

		colorPanel = createColorControl(DarknessMask.PROPERTY_COLOR);
		alphaSlider = createSliderControl(DarknessMask.PROPERTY_ALPHA, Mode.LOCAL);
		lowLightCheck = createCheckBox(DarknessMask.PROPERTY_LOW_LIGHT, Mode.ALL, "Lowlight vision");
		visibleCheck = createVisibilityControl();
		visibleCheck.setSelected(true);
		trackHistory = createCheckBox(DarknessMask.PROPERTY_TRACK_HISTORY, Mode.ALL, "Show previously seen");
		historyColorPanel = createColorControl(DarknessMask.PROPERTY_HISTORY_COLOR);
		resetHistory = new JButton("Reset");
		resetHistory.addActionListener(e -> {
			display.setProperty(element, DarknessMask.PROPERTY_HISTORY, "");
		});

		//@formatter:off
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1; add(new JLabel("Colour:"), c);
		c.gridy = 2; add(new JLabel("Transparency:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0d;
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;
		add(visibleCheck, c);
		add(colorPanel, c);
		add(alphaSlider, c);
		add(lowLightCheck, c);
		add(trackHistory, c);
		add(historyColorPanel, c);
		add(resetHistory, c);

		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0d;
		add(new JPanel(), c);

		//@formatter:on
	}

	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(MapElement.PROPERTY_VISIBLE)) {
				visibleCheck.setSelected(e.getNewValue().equals(MapElement.Visibility.VISIBLE));

			} else if (e.getPropertyName().equals(DarknessMask.PROPERTY_ALPHA)) {
				alphaSlider.setValue((int)(100*(Float)e.getNewValue()));

			} else if (e.getPropertyName().equals(DarknessMask.PROPERTY_COLOR)) {
				colorPanel.setBackground((Color)e.getNewValue());

			} else if (e.getPropertyName().equals(DarknessMask.PROPERTY_LOW_LIGHT)) {
				lowLightCheck.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(DarknessMask.PROPERTY_TRACK_HISTORY)) {
				trackHistory.setSelected((Boolean) e.getNewValue());

			} else if (e.getPropertyName().equals(DarknessMask.PROPERTY_HISTORY_COLOR)) {
				historyColorPanel.setBackground((Color) e.getNewValue());

			} else {
				System.out.println("Unknown property changed: " + e.getPropertyName());
			}
		}
	};

	@Override
	MapElementMouseListener getMouseListener() {
		return mouseListener;
	}

	private MapElementMouseListener mouseListener = new MapElementMouseListener() {
		private boolean dragging = false;
		private int button;
		private boolean dragClear;	// when dragging if true then we clear cells, otherwise we reset cells

		private void setMasked(Point p, boolean mask) {
			element.setMasked(p, mask);
			if (mask) {
				display.setProperty(element, DarknessMask.PROPERTY_MASKCELL, p, Mode.REMOTE);
			} else {
				display.setProperty(element, DarknessMask.PROPERTY_UNMASKCELL, p, Mode.REMOTE);
			}
		}

		@Override
		public void mousePressed(MouseEvent e, Point2D gridloc) {
			button = e.getButton();
			dragClear = element.isMasked(gridCell(gridloc));
		}

		@Override
		public void mouseReleased(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				setMasked(gridCell(gridloc), !dragClear);	// TODO might not be necessary - not sure if a mouseDragged event is generated or not for location of the release
				dragging = false;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e, Point2D gridloc) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;
			setMasked(gridCell(gridloc), !element.isMasked(gridCell(gridloc)));
		}

		@Override
		public void mouseDragged(MouseEvent e, Point2D gridloc) {
			if (dragging) {
				setMasked(gridCell(gridloc), !dragClear);
			} else if (button == MouseEvent.BUTTON1) {
				dragging = true;
			}
		}

		// by default work in coordinates relative to the canvas
		@Override
		public MapElement getCoordElement() {
			return null;
		}
	};

	// ---- XML serialisation methods ----
	final static String XML_TAG = "DarknessMask";
	private final static String CLEARED_CELL_LIST_ATTRIBUTE = "cleared_cells";

	@Override
	Element getElement(Document doc) {
		Element e = doc.createElement(XML_TAG);
		setAllAttributes(e);
		setAttribute(e, REMOTE_PREFIX + MapElement.PROPERTY_VISIBLE, visibleCheck.isSelected() ? Visibility.VISIBLE : Visibility.HIDDEN);
		setCellListAttribute(e, CLEARED_CELL_LIST_ATTRIBUTE, element.getCells());
		setAttribute(e, DarknessMask.PROPERTY_HISTORY, element.getProperty(DarknessMask.PROPERTY_HISTORY));
		return e;
	}

	@Override
	void parseDOM(Element e, OptionsPanel<?> parent) {
		if (!e.getTagName().equals(XML_TAG)) return;

		parseColorAttribute(DarknessMask.PROPERTY_COLOR, e, Mode.ALL);
		parseFloatAttribute(DarknessMask.PROPERTY_ALPHA, e, Mode.LOCAL);
		parseBooleanAttribute(DarknessMask.PROPERTY_LOW_LIGHT, e, Mode.ALL);
//		parseBooleanAttribute(DarknessMask.PROPERTY_TRACK_HISTORY, e, Mode.ALL);
		parseColorAttribute(DarknessMask.PROPERTY_HISTORY_COLOR, e, Mode.ALL);
		parseStringAttribute(DarknessMask.PROPERTY_HISTORY, e, Mode.ALL);
		parseCellList(DarknessMask.PROPERTY_UNMASKCELL, e, CLEARED_CELL_LIST_ATTRIBUTE, Mode.ALL);
		parseVisibility(e, visibleCheck);
	}
}
