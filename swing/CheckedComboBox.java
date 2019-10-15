package swing;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.ComboPopup;

// Based on https://java-swing-tips.blogspot.co.nz/2016/07/select-multiple-jcheckbox-in-jcombobox.html (https://ateraimemo.com/Swing/CheckedComboBox.html)

// FIXME probably doesn't handle adding/removing elements correctly
// TODO needs convenience methods forwarding to the selection model. Also could allow setting selectionmodel
// TODO add "select all/none" element
// XXX bet preferred size calculation?

public class CheckedComboBox<E> extends JComboBox<E> {
	private static final long serialVersionUID = 1L;
	private boolean keepOpen;
	private transient ActionListener listener;
	private ListSelectionModel selectionModel;

	protected CheckedComboBox() {
		super();
	}

	public CheckedComboBox(ComboBoxModel<E> aModel) {
		super(aModel);
	}

	public ListSelectionModel getSelectionModel() {
		return selectionModel;
	}

	public void selectAll() {
		selectionModel.setSelectionInterval(0, dataModel.getSize()-1);
	}

	public boolean allSelected() {
		boolean all = true;
		for (int i = 0; all && i < dataModel.getSize(); i++) {
			all = all && selectionModel.isSelectedIndex(i);
		}
		return all;
	}

	// Always returns a set, which may be empty
	public Set<E> getSelectedItems() {
		HashSet<E> selected = new HashSet<>();
		for (int i = 0; i < dataModel.getSize(); i++) {
			if (selectionModel.isSelectedIndex(i)) {
				selected.add(dataModel.getElementAt(i));
			}
		}
		return selected;
	}

	// XXX not sure updateUI is the right place for this stuff
	@Override
	public void updateUI() {
		selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setRenderer(null);
		removeActionListener(listener);
		super.updateUI();
		listener = e -> {
			if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
				selectItem(getSelectedIndex());
				keepOpen = true;
			}
		};
		setRenderer(new CheckBoxCellRenderer<>(selectionModel));
		addActionListener(listener);
		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
		getActionMap().put("checkbox-select", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Accessible a = getAccessibleContext().getAccessibleChild(0);
				if (a instanceof ComboPopup) {
					ComboPopup pop = (ComboPopup) a;
					selectItem(pop.getList().getSelectedIndex());
				}
			}
		});
		setPreferredSize(new Dimension(200, 20));
	}

	protected void selectItem(int index) {
		if (isPopupVisible()) {
			E item = getItemAt(index);
			if (selectionModel.isSelectedIndex(index)) {
				selectionModel.removeSelectionInterval(index, index);
			} else {
				selectionModel.addSelectionInterval(index, index);
			}
//             ComboBoxModel m = getModel();
//             if (m instanceof CheckableComboBoxModel) {
//                 ((CheckableComboBoxModel) m).fireContentsChanged(index);
//             }
			// removeItemAt(index);
			// insertItemAt(item, index);
			setSelectedIndex(-1);
			setSelectedItem(item);
		}
	}

	@Override
	public void setPopupVisible(boolean v) {
		if (keepOpen) {
			keepOpen = false;
		} else {
			super.setPopupVisible(v);
		}
	}

	public static void main(String... args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}

	@SuppressWarnings("serial")
	public static void createAndShowGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
		JFrame frame = new JFrame("CheckedComboBox");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(new JPanel() {
			{
				setLayout(new BorderLayout());

				String[] m = {
						"aaa",
						"bbbbb",
						"111",
						"33333",
						"2222",
						"ccccccc",
						"ccdcccc",
						"ccceccc",
						"ccccfcc",
						"cccccgc",
						"cccccch",
						"ciccccc",
						"ccjcccc",
						"cckcccc"
				};

				JPanel p = new JPanel(new GridLayout(0, 1));
				p.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
				p.add(new JLabel("Default:"));
				p.add(new JComboBox<>(m));
				p.add(Box.createVerticalStrut(20));
				p.add(new JLabel("CheckedComboBox:"));
				p.add(new CheckedComboBox<>(new DefaultComboBoxModel<>(m)));
				// p.add(new CheckedComboBox<>(new CheckableComboBoxModel<>(m)));

				add(p, BorderLayout.NORTH);
				setPreferredSize(new Dimension(320, 240));
			}
		});
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}

class CheckBoxCellRenderer<E> implements ListCellRenderer<E> {
	private final JLabel label = new JLabel(" ");
	private final JCheckBox check = new JCheckBox(" ");
	private ListSelectionModel selectionModel;

	public CheckBoxCellRenderer(ListSelectionModel model) {
		selectionModel = model;
	}

	private String getCheckedItemString(ListModel<?> model) {
		List<String> sl = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			Object o = model.getElementAt(i);
			if (selectionModel.isSelectedIndex(i)) {
				sl.add(o.toString());
			}
		}
		return sl.stream().sorted().collect(Collectors.joining(", "));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
		if (index < 0) {
			boolean all = true;
			for (int i = 0; all && i < list.getModel().getSize(); i++) {
				all = all && selectionModel.isSelectedIndex(i);
			}
			if (all) {
				label.setText("(all)");
			} else {
				label.setText(getCheckedItemString(list.getModel()));
			}
			return label;
		} else {
			check.setText(Objects.toString(value, ""));
			check.setSelected(selectionModel.isSelectedIndex(index));
			if (isSelected) {
				check.setBackground(list.getSelectionBackground());
				check.setForeground(list.getSelectionForeground());
			} else {
				check.setBackground(list.getBackground());
				check.setForeground(list.getForeground());
			}
			return check;
		}
	}
}

