import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import magicgenerator.Generator;
import magicgenerator.Item;
import magicgenerator.Table;
import magicgenerator.TableRoller;
import magicgenerator.TableRow;

// TODO Cancel button
public class ItemBuilderWizard implements Runnable {
	JFrame frame;
	Generator generator;
	String procedure;
	Thread wizThread;
	JTextArea itemArea;
	JTable jTable;
	JButton nextButton;
	JLabel tableLabel;
	ItemTarget target;

	public ItemBuilderWizard(Generator generator, String proc, ItemTarget target) {
		this.generator = generator;
		this.target = target;
		this.procedure = proc;
		frame = new JFrame("Build Item");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(600, 400);

		itemArea = new JTextArea();
		itemArea.setRows(8);
		JScrollPane itemScroll = new JScrollPane(itemArea);
		jTable = new JTable();
		JScrollPane scroll = new JScrollPane(jTable);
		tableLabel = new JLabel();

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		nextButton = new JButton("Next");
		nextButton.setEnabled(false);
		buttonPanel.add(nextButton);

		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		pane.add(itemScroll);
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		pane.add(tableLabel);
		pane.add(scroll);
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		pane.add(buttonPanel);

		frame.getContentPane().add(pane);
	}

	public void startWizard() {
		frame.setVisible(true);
		wizThread = new Thread(this);
		wizThread.start();
	}

	@SuppressWarnings("serial")
	private class GeneratorTableModel extends AbstractTableModel {
		Table table;

		public GeneratorTableModel(Table t) {
			table = t;
		}

		public int getColumnCount() {
			return 4;
		}

		public String getColumnName(int column) {
			if (column == 0) return "% for Minor";
			else if (column == 1) return "% for Medium";
			else if (column == 2) return "% for Major";
			else if (column == 3) return "Instructions";
			return super.getColumnName(column);
		}

		public int getRowCount() {
			return table.getRowCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			TableRow row = table.getRow(rowIndex);
			if (columnIndex >=0 && columnIndex < 3) return row.getChance(columnIndex);
			if (columnIndex == 3) return row.getInstructionString();
			return null;
		}
		
	}

	private class TableChooser extends TableRoller implements Runnable, ActionListener, ListSelectionListener {
		Item item;
		Table table;
		int selected = -1;

		public TableRow chooseRow(Item i, Table t)  {
			System.out.println("chooseRow "+t.name+" ("+t.comment+")");
			item = i;
			table = t;
			setSelected(-1);
			SwingUtilities.invokeLater(this);	// do all the ui setup

			// wait for the user's imput
			synchronized(wizThread) {
				while (getSelected() == -1) {
					try {
						wizThread.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			TableRow r = t.getRow(getSelected());
			return r;
		}

		private synchronized int getSelected() {
			return selected;
		}

		private synchronized void setSelected(int i) {
			selected = i;
		}

		public void run() {
			//final String text = "Choosing row for "+t.name+" ("+t.comment+")\n" + t;
			String text = DefaultItemFormatter.getItemDescription(item);
			itemArea.setText(text);
			tableLabel.setText(table.name + " " + table.comment);
			jTable.setModel(new GeneratorTableModel(table));
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTable.getSelectionModel().clearSelection();
			jTable.getColumnModel().getColumn(0).setPreferredWidth(5);
			jTable.getColumnModel().getColumn(1).setPreferredWidth(5);
			jTable.getColumnModel().getColumn(2).setPreferredWidth(5);
			jTable.getColumnModel().getColumn(3).setPreferredWidth(200);
			nextButton.addActionListener(this);
			jTable.getSelectionModel().addListSelectionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			setSelected(jTable.getSelectedRow());
			synchronized(wizThread) {
				wizThread.notify();
			}
			nextButton.removeActionListener(this);
			nextButton.setEnabled(false);
		}

		public void valueChanged(ListSelectionEvent e) {
			nextButton.setEnabled(jTable.getSelectedRowCount() == 1);
		}
	}

	public void run() {
		final Item item = generator.generate(Item.CLASS_MINOR, procedure, new TableChooser());
		final String text = DefaultItemFormatter.getItemDescription(item);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				itemArea.setText(text);
				jTable.setModel(new DefaultTableModel());
				tableLabel.setText("");
				nextButton.setText("Ok");
				nextButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.setVisible(false);
						target.addItem(item);
					}
				});
				nextButton.setEnabled(true);
			}
		});
	}
}
