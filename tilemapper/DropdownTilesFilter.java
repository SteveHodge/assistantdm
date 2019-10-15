package tilemapper;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;

import swing.CheckedComboBox;

public class DropdownTilesFilter implements TilesFilter {
	OptionComboBoxModel includeStylesModel = new OptionComboBoxModel();
	OptionComboBoxModel excludeStylesModel = new OptionComboBoxModel();
	OptionComboBoxModel setsModel = new OptionComboBoxModel();
	CheckedComboBox<String> includeStylesCombo;
	CheckedComboBox<String> excludeStylesCombo;
	CheckedComboBox<String> setsCombo;
	JCheckBox matchAll;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(new Object());

	DropdownTilesFilter() {
		TileManager.addPropertyChangeListener(e -> {
			if (e.getPropertyName().equals("addTile")) {
				Tile t = (Tile) e.getNewValue();
				if (!setsModel.options.contains(t.tileSet)) {
					setsModel.addElement(t.tileSet);
				}
				for (String s : t.styles) {
					if (!includeStylesModel.options.contains(s)) {
						includeStylesModel.addElement(s);
					}
					if (!excludeStylesModel.options.contains(s)) {
						excludeStylesModel.addElement(s);
					}
				}
			}
		});
	}

	JPanel getFilterPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		includeStylesCombo = new CheckedComboBox<>(includeStylesModel);
		includeStylesCombo.setMaximumRowCount(30);
		includeStylesCombo.getSelectionModel().addListSelectionListener(e -> selectionChange());
		excludeStylesCombo = new CheckedComboBox<>(excludeStylesModel);
		excludeStylesCombo.setMaximumRowCount(30);
		excludeStylesCombo.getSelectionModel().addListSelectionListener(e -> selectionChange());
		setsCombo = new CheckedComboBox<>(setsModel);
		setsCombo.setMaximumRowCount(30);
		setsCombo.getSelectionModel().addListSelectionListener(e -> selectionChange());
		matchAll = new JCheckBox("Include only if tile matches all styles");
		matchAll.addActionListener(e -> selectionChange());

		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 4, 0, 4);
		p.add(new JLabel("Include tiles with style:"), c);
		c.gridwidth = 4;
		p.add(matchAll, c);
		c.gridwidth = 1;
		p.add(new JLabel("Exclude tiles with style:"), c);
		p.add(new JLabel("Restrict to sets:"), c);

		c.gridx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridy = 0;
		p.add(includeStylesCombo, c);
		c.gridy = 2;
		p.add(excludeStylesCombo, c);
		c.gridy = 3;
		p.add(setsCombo, c);

		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		JButton includeAll = new JButton("All");
		includeAll.addActionListener(e -> {
			includeStylesCombo.selectAll();
		});
		c.gridy = 0;
		p.add(includeAll, c);
		includeAll = new JButton("All");
		includeAll.addActionListener(e -> {
			excludeStylesCombo.selectAll();
		});
		c.gridy = 2;
		p.add(includeAll, c);
		includeAll = new JButton("All");
		includeAll.addActionListener(e -> {
			setsCombo.selectAll();
		});
		c.gridy = 3;
		p.add(includeAll, c);

		c.gridx = 3;
		JButton excludeAll = new JButton("None");
		excludeAll.addActionListener(e -> {
			includeStylesCombo.getSelectionModel().clearSelection();
		});
		c.gridy = 0;
		p.add(excludeAll, c);
		excludeAll = new JButton("None");
		excludeAll.addActionListener(e -> {
			excludeStylesCombo.getSelectionModel().clearSelection();
		});
		c.gridy = 2;
		p.add(excludeAll, c);
		excludeAll = new JButton("None");
		excludeAll.addActionListener(e -> {
			setsCombo.getSelectionModel().clearSelection();
		});
		c.gridy = 3;
		p.add(excludeAll, c);
		return p;
	}

	private void selectionChange() {
		pcs.firePropertyChange("selection", 0, -1);
	}

	@Override
	public List<Tile> getTiles() {
		List<Tile> list = new ArrayList<Tile>();

		if (includeStylesCombo != null) {
			Set<String> included = includeStylesCombo.getSelectedItems();
			Set<String> excluded = excludeStylesCombo.getSelectedItems();
			Set<String> sets = setsCombo.getSelectedItems();
//			System.out.println("Filtering: included = " + included.size() + ", excluded = " + excluded.size() + ", sets = " + sets.size());

			for (Tile t : TileManager.tiles) {
				if (!sets.contains(t.tileSet)) continue;
				boolean include = false;
				for (String s : t.styles) {
					if (excluded.contains(s)) {
						include = false;
						break;
					} else if (included.contains(s)) {
						include = true;
					} else if (matchAll.isSelected()) {
						include = false;	// we're matching all styles and this tile includes a style that's not included
						break;
					}
				}
				if (include && matchAll.isSelected()) {
					// still need to verify that this tile matches all the included styles. since we've already excluded tiles which have extraneous styles, all we
					// need to check now is that the tile has the same number of styles as are in the included set
					if (included.size() != t.styles.size()) include = false;
				}
				if (include) list.add(t);
			}
		}
		System.out.println("Filtered to " + list.size() + " tiles");
		return list;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	@SuppressWarnings("serial")
	private class OptionComboBoxModel extends AbstractListModel<String> implements MutableComboBoxModel<String> {
		List<String> options = new ArrayList<String>();
		String selected = null;

		@Override
		public String getElementAt(int index) {
			return options.get(index);
		}

		@Override
		public int getSize() {
			return options.size();
		}
		@Override
		public Object getSelectedItem() {
			return selected;
		}

		@Override
		public void setSelectedItem(Object anItem) {
			if (selected == null && anItem == null)
				return;
			if (selected != null && selected.equals(anItem))
				return;
			if (anItem != null && options.indexOf(anItem) == -1)
				return;

			if (anItem instanceof String) {
				selected = (String) anItem;	// TODO should restrict to available options?
				fireContentsChanged(this, -1, -1);
			}
		}

		@Override
		public void addElement(String item) {
			int index = options.size();
			options.add(item);
			fireIntervalAdded(this, index, index);
			options.sort(null);
			fireContentsChanged(this, 0, options.size() - 1);
		}

		@Override
		public void insertElementAt(String item, int index) {
			options.add(index, item);
			fireIntervalAdded(this, index, index);
			options.sort(null);
			fireContentsChanged(this, 0, options.size() - 1);
		}

		@Override
		public void removeElement(Object obj) {
			int index = options.indexOf(obj);
			if (index != -1) {
				options.remove(obj);
				fireIntervalRemoved(this, index, index);
			}
		}

		@Override
		public void removeElementAt(int index) {
			options.remove(index);
			fireIntervalRemoved(this, index, index);
		}
	}
}
