package led_control;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

// TODO deleting dynamics should reset any patterns that use the dynamic

@SuppressWarnings("serial")
public class LEDTest extends JFrame {
	static List<Dynamic> dynamics = new ArrayList<>();
	{
		dynamics.add(new Dynamic(0, 0, 0));
		dynamics.add(new Dynamic(1, 0, 216));
		dynamics.add(new Dynamic(2, 0, 48));
		dynamics.add(new Dynamic(3, 0, 48));
	}

	static List<Region> regions = new ArrayList<>();
	{
		regions.add(new Region(0, true, 0, 24, new RainbowPattern(dynamics.get(0), 11)));
		regions.add(new Region(1, true, 24, 16, new RainbowPattern(dynamics.get(1), 16)));
		regions.add(new Region(2, true, 40, 12, new RainbowPattern(dynamics.get(2), 21)));
		regions.add(new Region(3, true, 52, 8, new RainbowPattern(dynamics.get(3), 32)));
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

//		if (System.getSecurityManager() == null) {
//			System.setSecurityManager(new SecurityManager());
//		}

		SwingUtilities.invokeLater(() -> {
			LEDTest inst = new LEDTest();
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);
		});
	}

	public LEDTest() {
		setTitle("LED Controller");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		final RegionsTableModel regionsModel = new RegionsTableModel();
		final JTable regionsTable = new JTable(regionsModel);
		regionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		regionsTable.getColumnModel().getColumn(5).setPreferredWidth(300);
		JComboBox<PatternType> typeCombo = new JComboBox<>();
		DefaultComboBoxModel<PatternType> typeModel = new DefaultComboBoxModel<>();
		typeModel.addElement(PatternType.RAINBOW);
		typeModel.addElement(PatternType.SOLID_HSV);
		typeModel.addElement(PatternType.SOLID_RGB);
		typeCombo.setModel(typeModel);
		regionsTable.setDefaultEditor(PatternType.class, new DefaultCellEditor(typeCombo));
		JScrollPane regionsScrollpane = new JScrollPane(regionsTable);
		regionsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!SwingUtilities.isLeftMouseButton(e)) return;
				int row = regionsTable.rowAtPoint(e.getPoint());
				int col = regionsTable.columnAtPoint(e.getPoint());
				if (col != 5) return;
				PatternOptionDialog dialog = new PatternOptionDialog(LEDTest.this, regions.get(row), regionsModel);
				dialog.setVisible(true);
			}
		});

		final DynamicsTableModel dynamicsModel = new DynamicsTableModel();
		final JTable dynamicsTable = new JTable(dynamicsModel);
		dynamicsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane dynamicsScrollPane = new JScrollPane(dynamicsTable);

		JButton applyButton = new JButton("Send Configuration");
		applyButton.addActionListener(e -> {
			sendConfig();
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(applyButton);

		JButton addRegionButton = new JButton("Add");
		addRegionButton.addActionListener(e -> {
			regionsModel.addRow();
		});
		JButton deleteRegionButton = new JButton("Delete");
		deleteRegionButton.addActionListener(e -> {
			regionsModel.deleteRow(regionsTable.getSelectedRow());
		});
		JPanel regionsButtons = new JPanel();
		regionsButtons.add(addRegionButton);
		regionsButtons.add(deleteRegionButton);

		JButton addDynamicsButton = new JButton("Add");
		addDynamicsButton.addActionListener(e -> {
			dynamicsModel.addRow();
		});
		JButton deleteDynamicsButton = new JButton("Delete");
		deleteDynamicsButton.addActionListener(e -> {
			dynamicsModel.deleteRow(dynamicsTable.getSelectedRow());
		});
		JPanel dynamicsButtons = new JPanel();
		dynamicsButtons.add(addDynamicsButton);
		dynamicsButtons.add(deleteDynamicsButton);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);

		c.gridx = 0;
		c.gridy = 0;
		add(new JLabel("Regions"), c);

		c.gridx = 1;
		add(new JLabel("Dynamics"), c);

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 2.0d;
		c.weighty = 1.0d;
		add(regionsScrollpane, c);

		c.gridx = 1;
		c.weightx = 1.0d;
		add(dynamicsScrollPane, c);

		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.NONE;
		add(regionsButtons, c);

		c.gridx = 1;
		add(dynamicsButtons, c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.weightx = 0;
		c.weighty = 0;
		add(buttonPanel, c);

		pack();
	}

	public void sendConfig() {
		URL url;
		try {
			url = new URL("http://192.168.1.35/config");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			String json = "[" + String.join(",", regions.stream().map(e -> e.getJSON()).toArray(String[]::new)) + "]";
//			System.out.println(json);
			byte[] body = json.getBytes(StandardCharsets.UTF_8);
			http.setFixedLengthStreamingMode(body.length);
			http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			http.connect();
			OutputStream os = http.getOutputStream();
			os.write(body);

			int status = http.getResponseCode();
			System.out.println("Response: "+status);
			for (Entry<String, List<String>> header : http.getHeaderFields().entrySet()) {
				System.out.println(header.getKey() + " = " + header.getValue());
			}

//			String contentType = http.getHeaderField("Content-Type");
//			String charset = null;
//
//			for (String param : contentType.replace(" ", "").split(";")) {
//				if (param.startsWith("charset=")) {
//					charset = param.split("=", 2)[1];
//					break;
//				}
//			}

//			if (charset != null) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String line;
			StringBuffer result = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				result.append(line).append("\n");
			}
			rd.close();
			System.out.println(result);
//			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	abstract class DynamicComboModel extends AbstractListModel<Dynamic> implements ComboBoxModel<Dynamic>
	{
		Object selected = null;

		DynamicComboModel(Object initial) {
			setSelectedItem(initial);
		}

		@Override
		public Dynamic getElementAt(int i) {
			return dynamics.get(i);
		}

		@Override
		public int getSize() {
			return dynamics.size();
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}

		@Override
		public void setSelectedItem(Object value) {
			if (value instanceof Dynamic) {
				selected = value;
				updateParameter(selected);
			} else {
				try {
					int val = Integer.parseInt(value.toString());
					if (val < 0) val = 0;
					if (val > 255) val = 255;
					selected = val;
					updateParameter(selected);
				} catch (NumberFormatException ex) {
				}
			}
			this.fireContentsChanged(this, 0, getSize());
		}

		abstract public void updateParameter(Object val);
	}

	class RegionsTableModel extends AbstractTableModel {
		public void addRow() {
			Region r = new Region(regions.size(), true, 0, 0, new SolidRGBPattern(0, 0, 0));
			regions.add(r);
			fireTableRowsInserted(regions.size() - 2, regions.size() - 1);
		}

		public void deleteRow(int row) {
			regions.remove(row);
			fireTableRowsDeleted(row, row);
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public int getRowCount() {
			return regions.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Region r = regions.get(row);
			switch (col) {
			case 0:
				return r.id;
			case 1:
				return r.start;
			case 2:
				return r.count;
			case 3:
				return r.enabled;
			case 4:
				return r.pattern.getType();
			case 5:
				return r.pattern.getOptionString();
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 3) return Boolean.class;
			if (col == 4) return PatternType.class;
			if (col == 5) return String.class;
			return Integer.class;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "ID";
			case 1:
				return "Start";
			case 2:
				return "Count";
			case 3:
				return "Enabled";
			case 4:
				return "Pattern";
			case 5:
				return "Options";
			}
			return super.getColumnName(col);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex != 5) return true;
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Region r = regions.get(rowIndex);
			switch (columnIndex) {
			case 0:
				r.id = (Integer) aValue;
				return;
			case 1:
				r.start = (Integer) aValue;
				return;
			case 2:
				r.count = (Integer) aValue;
				return;
			case 3:
				r.enabled = (Boolean) aValue;
				return;
			case 4:
				PatternType t = (PatternType) aValue;
				if (t != r.pattern.getType()) {
					r.pattern = t.createPattern();
				}
				this.fireTableCellUpdated(rowIndex, 4);
				return;
			}
		}
	}

	class DynamicsTableModel extends AbstractTableModel {
		public void addRow() {
			Dynamic d = new Dynamic(dynamics.size(), 0, 0);
			dynamics.add(d);
			fireTableRowsInserted(dynamics.size() - 2, dynamics.size() - 1);
		}

		public void deleteRow(int row) {
			dynamics.remove(row);
			fireTableRowsDeleted(row, row);
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return dynamics.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Dynamic d = dynamics.get(row);
			switch (col) {
			case 0:
				return d.id;
			case 1:
				return d.mode;
			case 2:
				return d.value;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return Integer.class;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "ID";
			case 1:
				return "Mode";
			case 2:
				return "Value";
			}
			return super.getColumnName(col);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Dynamic d = dynamics.get(rowIndex);
			switch (columnIndex) {
			case 0:
				d.id = (Integer) aValue;
				return;
			case 1:
				d.mode = (Integer) aValue;
				return;
			case 2:
				d.value = (Integer) aValue;
				return;
			}
		}
	}

	class PatternOptionDialog extends JDialog {
		public PatternOptionDialog(Component parent, Region region, RegionsTableModel model) {
			super(SwingUtilities.getWindowAncestor(parent), region.pattern.getType().toString() + " Options");

			setLayout(new GridLayout(0, 2));

			PatternType type = region.pattern.getType();
			switch (type) {
			case RAINBOW:
				createRainbowOptions(region);
				break;
			case SOLID_RGB:
				createRGBOptions(region);
				break;
			case SOLID_HSV:
				createHSVOptions(region);
				break;
			default:
			}

			setModal(true);
			pack();
			setLocationRelativeTo(parent);
		}

		void createRainbowOptions(Region region) {
			RainbowPattern pattern = (RainbowPattern) region.pattern;

			JComboBox<Dynamic> startCombo = new JComboBox<>();
			startCombo.setEditable(true);
			DynamicComboModel startModel = new DynamicComboModel(pattern.startHue) {
				@Override
				public void updateParameter(Object val) {
					pattern.startHue = val;
				}
			};
			startCombo.setModel(startModel);
			add(new JLabel("Start Hue: "));
			add(startCombo);

			JComboBox<Dynamic> deltaCombo = new JComboBox<>();
			deltaCombo.setEditable(true);
			DynamicComboModel deltaModel = new DynamicComboModel(pattern.delta) {
				@Override
				public void updateParameter(Object val) {
					pattern.delta = val;
				}
			};
			deltaCombo.setModel(deltaModel);
			add(new JLabel("Delta: "));
			add(deltaCombo);
		}

		void createRGBOptions(Region region) {
			SolidRGBPattern pattern = (SolidRGBPattern) region.pattern;

			JComboBox<Dynamic> redCombo = new JComboBox<>();
			redCombo.setEditable(true);
			DynamicComboModel redModel = new DynamicComboModel(pattern.red) {
				@Override
				public void updateParameter(Object val) {
					pattern.red = val;
				}
			};
			redCombo.setModel(redModel);
			add(new JLabel("Red: "));
			add(redCombo);

			JComboBox<Dynamic> greenCombo = new JComboBox<>();
			greenCombo.setEditable(true);
			DynamicComboModel greenModel = new DynamicComboModel(pattern.green) {
				@Override
				public void updateParameter(Object val) {
					pattern.green = val;
				}
			};
			greenCombo.setModel(greenModel);
			add(new JLabel("Green: "));
			add(greenCombo);

			JComboBox<Dynamic> blueCombo = new JComboBox<>();
			blueCombo.setEditable(true);
			DynamicComboModel blueModel = new DynamicComboModel(pattern.blue) {
				@Override
				public void updateParameter(Object val) {
					pattern.blue = val;
				}
			};
			blueCombo.setModel(blueModel);
			add(new JLabel("Blue: "));
			add(blueCombo);
		}

		void createHSVOptions(Region region) {
			SolidHSVPattern pattern = (SolidHSVPattern) region.pattern;

			JComboBox<Dynamic> hueCombo = new JComboBox<>();
			hueCombo.setEditable(true);
			DynamicComboModel hueModel = new DynamicComboModel(pattern.hue) {
				@Override
				public void updateParameter(Object val) {
					pattern.hue = val;
				}
			};
			hueCombo.setModel(hueModel);
			add(new JLabel("Hue: "));
			add(hueCombo);

			JComboBox<Dynamic> satCombo = new JComboBox<>();
			satCombo.setEditable(true);
			DynamicComboModel satModel = new DynamicComboModel(pattern.saturation) {
				@Override
				public void updateParameter(Object val) {
					pattern.saturation = val;
				}
			};
			satCombo.setModel(satModel);
			add(new JLabel("Saturation: "));
			add(satCombo);

			JComboBox<Dynamic> valueCombo = new JComboBox<>();
			valueCombo.setEditable(true);
			DynamicComboModel valueModel = new DynamicComboModel(pattern.value) {
				@Override
				public void updateParameter(Object val) {
					pattern.value = val;
				}
			};
			valueCombo.setModel(valueModel);
			add(new JLabel("Value: "));
			add(valueCombo);
		}
	}
}
