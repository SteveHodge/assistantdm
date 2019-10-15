package tilemapper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tilemapper.TileManager.Visibility;

@SuppressWarnings("serial")
class RadioFilterPanel extends JPanel {
	private String title;
	private String propName;
	private Map<String, Visibility> visibility = new HashMap<>();
	private PropertyChangeSupport pcs = new PropertyChangeSupport(new Object());

	static class RadioFilters implements TilesFilter {
		RadioFilterPanel sets = new RadioFilterPanel("Set", "sets") {
			@Override
			void addTile(Tile t) {
				addOption(t.tileSet, Visibility.INCLUDED);
			}
		};

		RadioFilterPanel styles = new RadioFilterPanel("Style", "styles") {
			@Override
			public void addTile(Tile t) {
				for (String s : t.styles) {
					addOption(s, Visibility.IGNORED);
				}
			}
		};

		@Override
		public List<Tile> getTiles() {
			List<Tile> list = new ArrayList<Tile>();
			for (Tile t : TileManager.tiles) {
				if (sets.getVisibility(t.tileSet) == Visibility.EXCLUDED) continue; // set not visible; skip
				boolean include = false;
				for (String s : t.styles) {
					if (styles.getVisibility(s) == Visibility.EXCLUDED) {
						include = false;
						break;
					} else if (styles.getVisibility(s) == Visibility.INCLUDED) {
						include = true;
					}
				}
				if (include) list.add(t);
			}
			return list;
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener l) {
			sets.pcs.addPropertyChangeListener(l);
			styles.pcs.addPropertyChangeListener(l);
		}
	}

	private RadioFilterPanel(String title, String propName) {
		this.title = title;
		this.propName = propName;
		setLayout(new GridBagLayout());
		TileManager.addPropertyChangeListener(e -> {
			if (e.getPropertyName().equals("addTile")) {
				Tile t = (Tile) e.getNewValue();
				addTile(t);
			}
		});
	}

	void addTile(Tile t) {
	}

	void layoutPanel() {
		removeAll();
		GridBagConstraints c = new GridBagConstraints();

		c.gridy = 0;
		c.insets = new Insets(0, 4, 0, 4);
		add(new JLabel("Include"), c);
		add(new JLabel("Ignore"), c);
		add(new JLabel("Exclude"), c);
		c.anchor = GridBagConstraints.WEST;
		add(new JLabel(title), c);

		c.gridy++;
		c.anchor = GridBagConstraints.CENTER;
		ButtonGroup group = new ButtonGroup();

		Map<JRadioButton, String> includeButtons = new HashMap<>();
		Map<JRadioButton, String> ignoreButtons = new HashMap<>();
		Map<JRadioButton, String> excludeButtons = new HashMap<>();

		JRadioButton inc = new JRadioButton();
		inc.addActionListener(e -> {
			for (JRadioButton b : includeButtons.keySet()) {
				String s = includeButtons.get(b);
				b.setSelected(true);
				visibility.put(s, Visibility.INCLUDED);
				pcs.firePropertyChange(propName, s, Visibility.INCLUDED);
			}
		});
		group.add(inc);
		add(inc, c);

		JRadioButton ign = new JRadioButton();
		ign.addActionListener(e -> {
			for (JRadioButton b : ignoreButtons.keySet()) {
				String s = ignoreButtons.get(b);
				b.setSelected(true);
				visibility.put(s, Visibility.IGNORED);
				pcs.firePropertyChange(propName, s, Visibility.IGNORED);
			}
		});
		group.add(ign);
		add(ign, c);

		JRadioButton exc = new JRadioButton();
		exc.addActionListener(e -> {
			for (JRadioButton b : excludeButtons.keySet()) {
				String s = excludeButtons.get(b);
				b.setSelected(true);
				visibility.put(s, Visibility.EXCLUDED);
				pcs.firePropertyChange(propName, s, Visibility.EXCLUDED);
			}
		});
		group.add(exc);
		add(exc, c);

		c.anchor = GridBagConstraints.WEST;
		add(new JLabel("All"), c);

		ArrayList<String> names = new ArrayList<String>(visibility.keySet());
		Collections.sort(names);
		for (String s : names) {
			Visibility vis = visibility.get(s);

			c.gridy++;
			c.anchor = GridBagConstraints.CENTER;
			group = new ButtonGroup();

			inc = new JRadioButton();
			includeButtons.put(inc, s);
			inc.addActionListener(e -> {
				visibility.put(s, Visibility.INCLUDED);
				pcs.firePropertyChange(propName, s, Visibility.INCLUDED);
			});
			inc.setSelected(vis == Visibility.INCLUDED);
			group.add(inc);
			add(inc, c);

			ign = new JRadioButton();
			ignoreButtons.put(ign, s);
			ign.addActionListener(e -> {
				visibility.put(s, Visibility.IGNORED);
				pcs.firePropertyChange(propName, s, Visibility.IGNORED);
			});
			ign.setSelected(vis == Visibility.IGNORED);
			group.add(ign);
			add(ign, c);

			exc = new JRadioButton();
			excludeButtons.put(exc, s);
			exc.addActionListener(e -> {
				visibility.put(s, Visibility.EXCLUDED);
				pcs.firePropertyChange(propName, s, Visibility.EXCLUDED);
			});
			group.add(exc);
			add(exc, c);

			c.anchor = GridBagConstraints.WEST;
			add(new JLabel(s), c);
		}
	}

	void setVisible(String option, boolean include) {
		if (visibility.containsKey(option)) {
			visibility.put(option, include ? Visibility.INCLUDED : Visibility.IGNORED);
			pcs.firePropertyChange(propName, option, include);
		}
	}

	// if the option is not already known then adds the option with the specified visibility
	void addOption(String option, Visibility vis) {
		if (!visibility.containsKey(option)) visibility.put(option, vis);
	}

	Set<String> getOptions() {
		return visibility.keySet();
	}

	Visibility getVisibility(String option) {
		if (visibility.containsKey(option)) return visibility.get(option);
		return Visibility.EXCLUDED;	// shouldn't happen as all option values should have been added to the map
	}
}