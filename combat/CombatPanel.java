package combat;

import gamesystem.Creature;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableColumn;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import party.Party;
import swing.ReorderableList;
import swing.SpinnerCellEditor;
import util.Updater;
import util.XMLUtils;

// TODO consider removing ability to edit max hitpoints. Maybe have modifications on this tab be temporary
// TODO provide events when the combat state changes (round or initiative list), then file updater can just register as listener

@SuppressWarnings("serial")
public class CombatPanel extends JPanel {
	private static CombatPanel combatPanel;

	private Party party;
	private InitiativeListModel initiativeListModel;
	private EffectTableModel effectsTableModel;
	private int round = 0;
	private JLabel roundsLabel;
	private List<InitiativeListener> listeners = new ArrayList<InitiativeListener>();

	// TODO need to remove this static instance
	public static CombatPanel getCombatPanel() {
		return combatPanel;
	}

	public InitiativeListModel getInitiativeListModel() {
		return initiativeListModel;
	}

	public CombatPanel(Party p) {
		combatPanel = this;
		party = p;

		initiativeListModel = new InitiativeListModel(party);
		initiativeListModel.addListDataListener(new ListDataListener() {
			@Override
			public void contentsChanged(ListDataEvent arg0) {
				updateInitiative(round, initiativeListModel.getInitiativeText());
			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {
				updateInitiative(round, initiativeListModel.getInitiativeText());
			}

			@Override
			public void intervalRemoved(ListDataEvent arg0) {
				updateInitiative(round, initiativeListModel.getInitiativeText());
			}

		});
		JLayeredPane initiativeList = new ReorderableList(initiativeListModel);
		JScrollPane listScroller = new JScrollPane(initiativeList);

		JPanel initiativePanel = new JPanel();
		initiativePanel.setBorder(BorderFactory.createTitledBorder("Initiative Order"));
		initiativePanel.setLayout(new BorderLayout());
		initiativePanel.add(listScroller, BorderLayout.CENTER);

		effectsTableModel = new EffectTableModel();
		JTable table = new JTable(effectsTableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.setDefaultEditor(Integer.class, new SpinnerCellEditor());
		table.setAutoCreateRowSorter(true);
		ArrayList<RowSorter.SortKey> list = new ArrayList<RowSorter.SortKey>();
		list.add(new RowSorter.SortKey(EffectTableModel.DURATION_COLUMN, SortOrder.ASCENDING));
		table.getRowSorter().setSortKeys(list);
		TableColumn durationCol = table.getColumnModel().getColumn(EffectTableModel.DURATION_COLUMN);
		durationCol.setCellRenderer(new DurationCellRenderer());
		durationCol.setCellEditor(new DurationCellEditor());

		//effectsListModel = new EffectListModel();
		//ReorderableList effectsList = new ReorderableList(effectsListModel);
		//JScrollPane effectsScroller = new JScrollPane(effectsList);
		JScrollPane effectsScroller = new JScrollPane(table);

		JPanel effectsPanel = new JPanel();
		effectsPanel.setBorder(BorderFactory.createTitledBorder("Temporary Effects"));
		effectsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		effectsPanel.add(new NewEffectPanel(table, effectsTableModel, initiativeListModel), c);

		c.gridy = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		effectsPanel.add(effectsScroller, c);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initiativePanel, effectsPanel);
		splitPane.setOneTouchExpandable(true);

		roundsLabel = new JLabel("Round " + round);

		JButton resetCombatButton = new JButton("Reset Combat");
		resetCombatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				initiativeListModel.reset();
				round = 0;
				roundsLabel.setText("Round "+round);
				updateInitiative(round, initiativeListModel.getInitiativeText());
			}
		});

		JButton nextRoundButton = new JButton("Next Round");
		nextRoundButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// work from last to first to avoid issues when we remove entries
				for (int i=effectsTableModel.getRowCount()-1; i >= 0; i--) {
					if (effectsTableModel.addDuration(i, -1) < 0)
						effectsTableModel.expireEffect(i, CombatPanel.this, initiativeListModel);
				}
				round++;
				roundsLabel.setText("Round "+round);
				updateInitiative(round, initiativeListModel.getInitiativeText());
			}
		});

		JButton advanceTimeButton = new JButton("Advance Time");
		advanceTimeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SelectTimeDialog dialog = new SelectTimeDialog(SwingUtilities.getWindowAncestor((JButton)e.getSource()));
				dialog.setVisible(true);
				if (!dialog.isCancelled()) {
					int value = dialog.getValue();
					// work from last to first to avoid issues when we remove entries
					for (int i=effectsTableModel.getRowCount()-1; i >= 0; i--) {
						if (effectsTableModel.addDuration(i, -value) < 0)
							effectsTableModel.expireEffect(i, CombatPanel.this, initiativeListModel);
					}
					round+= value;
					roundsLabel.setText("Round "+round);
					updateInitiative(round, initiativeListModel.getInitiativeText());
				}
			}
		});

		JButton resetEffectsButton = new JButton("Reset Effects");
		resetEffectsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				effectsTableModel.clear();
			}
		});

		JPanel topPanel = new JPanel();
		topPanel.add(roundsLabel);
		topPanel.add(nextRoundButton);
		topPanel.add(advanceTimeButton);
		topPanel.add(resetCombatButton);
		topPanel.add(resetEffectsButton);

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
	}

	private String lastInitiativeOutput = "";

	private void updateInitiative(int round, String text) {
		String output = "round="+round+"\n"+text;
		if (!output.equals(lastInitiativeOutput)) {
			//System.out.println(output);
			lastInitiativeOutput = output;
			Updater.update(Updater.INITIATIVE_FILE, output.getBytes());
			for (InitiativeListener l : listeners) {
				l.initiativeUpdated(output);
			}
		}
	}

	public void addInitiativeListener(InitiativeListener l) {
		listeners.add(l);
		l.initiativeUpdated(lastInitiativeOutput);
	}

	public void removeInitiativeListener(InitiativeListener l) {
		listeners.remove(l);
	}

//	private String getCharacterName(int index) {
//		return party.get(index).getName();
//	}

	public static void addMonster(Creature m) {
		combatPanel.initiativeListModel.addEntry(new MonsterCombatEntry(m));
	}

	public void parseXML(File xmlFile) {
		System.out.println("Parsing " + xmlFile);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			InputStream is = getClass().getClassLoader().getResourceAsStream("combat.xsd");
			factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
			Document dom = factory.newDocumentBuilder().parse(xmlFile);
			//XMLUtils.printNode(dom, "");

			Node node = XMLUtils.findNode(dom,"Combat");
			if (node != null) parseDOM((Element) node);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parseDOM(Element e) {
		if (e.hasAttribute("round")) {
			round = Integer.parseInt(e.getAttribute("round"));
			roundsLabel.setText("Round " + round);
		}
		NodeList children = e.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeName().equals("InitiativeList")) {
				initiativeListModel.reset();
				initiativeListModel.parseDOM((Element) children.item(i));
			} else if (children.item(i).getNodeName().equals("EffectList")) {
				effectsTableModel.parseDOM((Element) children.item(i));
			}
		}
	}

	public Element getElement(Document doc) {
		Element el = doc.createElement("Combat");
		el.setAttribute("round", Integer.toString(round));
		el.appendChild(initiativeListModel.getElement(doc));
		el.appendChild(effectsTableModel.getElement(doc));
		return el;
	}
}
