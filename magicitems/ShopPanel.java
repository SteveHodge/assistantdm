package magicitems;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magicgenerator.Item;

@SuppressWarnings("serial")
public class ShopPanel extends JPanel {
	private JScrollPane jScrollPane1;
	private JLabel[] categoryLabels = new JLabel[3];
	private JSpinner[] categorySpinners = new JSpinner[3];
	private SpinnerNumberModel[] categorySpinnerModels = new SpinnerNumberModel[3];
	private String categoryNames[] = {"Minor","Medium","Major"};
	private JPanel multiJPanel;
	private AbstractAction actionRecreate;
	private AbstractAction actionDelete;
	private AbstractAction actionCreateMajor;
	private AbstractAction actionCreateMedium;
	private AbstractAction actionCreateMinor;
	private AbstractAction actionCreateItems;
	private AbstractAction actionBuildItem;
	private JButton redoJButton;
	private JList<Item> itemJList;
	private JButton deleteJButton;
	private JTextArea itemTextArea;
	private JButton createMinorJButton;
	private JButton createMajorJButton;
	private JButton createItemsJButton;
	private JButton createMediumJButton;
	private JButton buildItemJButton;

	Shop shop;

	public ShopPanel(Shop s) {
		shop = s;
		try {
			GroupLayout thisLayout = new GroupLayout(this);
			setLayout(thisLayout);
			{
				jScrollPane1 = new JScrollPane();
				{
					itemJList = new JList<>();
					jScrollPane1.setViewportView(itemJList);
					itemJList.setModel(shop);
					itemJList.addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent evt) {
							itemJListValueChanged(evt);
						}
					});
				}
			}
			{
				itemTextArea = new JTextArea();
				itemTextArea.setWrapStyleWord(true);
			}
			{
				deleteJButton = new JButton();
				deleteJButton.setText("Delete");
				deleteJButton.setAction(getActionDelete());
			}
			{
				redoJButton = new JButton();
				redoJButton.setText("Recreate");
				redoJButton.setAction(getActionRecreate());
			}
			{
				createMinorJButton = new JButton();
				createMinorJButton.setAction(getActionCreateMinor());
				createMinorJButton.setText("Create Minor Item");
			}
			{
				createMajorJButton = new JButton();
				createMajorJButton.setAction(getActionCreateMajor());
				createMajorJButton.setText("Create Major Item");
			}
			{
				createMediumJButton = new JButton();
				createMediumJButton.setAction(getActionCreateMedium());
				createMediumJButton.setText("Create Medium Item");
			}
			{
				buildItemJButton = new JButton();
				buildItemJButton.setAction(getActionBuildItem());
				buildItemJButton.setText("Build Item");
			}
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(thisLayout.createParallelGroup()
							.addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
									.addComponent(createMinorJButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(createMediumJButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(createMajorJButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(getMultiJPanel(), GroupLayout.PREFERRED_SIZE, 148, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(buildItemJButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
									.addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
											.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 131, Short.MAX_VALUE)
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
													.addComponent(deleteJButton, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
													.addComponent(redoJButton, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
													.addComponent(itemTextArea, GroupLayout.Alignment.LEADING, 0, 300, Short.MAX_VALUE)));
			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(thisLayout.createParallelGroup()
							.addComponent(createMinorJButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
							.addComponent(createMediumJButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
							.addComponent(createMajorJButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
							.addComponent(getMultiJPanel(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
							.addComponent(buildItemJButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addGroup(thisLayout.createParallelGroup()
									.addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
											.addComponent(deleteJButton, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(redoJButton, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
											.addGap(68))
											.addComponent(jScrollPane1, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 278, GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(itemTextArea, GroupLayout.PREFERRED_SIZE, 198, Short.MAX_VALUE)
											.addContainerGap());
			this.setSize(709, 345);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private AbstractAction getActionCreateItems() {
		if(actionCreateItems == null) {
			actionCreateItems = new AbstractAction("CreateItems", null) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					// we are making assumptions about the item category constants...
					for (int i=0; i<3; i++) {
						int num = categorySpinnerModels[i].getNumber().intValue();
						for (int j=0; j<num; j++) {
							shop.createItem(i);
						}
					}
				}
			};
		}
		return actionCreateItems;
	}

	private AbstractAction getActionCreateMinor() {
		if(actionCreateMinor == null) {
			actionCreateMinor = new AbstractAction("CreateMinor", null) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					shop.createItem(Item.CLASS_MINOR);
				}
			};
		}
		return actionCreateMinor;
	}

	private AbstractAction getActionCreateMedium() {
		if(actionCreateMedium == null) {
			actionCreateMedium = new AbstractAction("CreateMedium", null) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					shop.createItem(Item.CLASS_MEDIUM);
				}
			};
		}
		return actionCreateMedium;
	}

	private AbstractAction getActionCreateMajor() {
		if(actionCreateMajor == null) {
			actionCreateMajor = new AbstractAction("CreateMajor", null) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					shop.createItem(Item.CLASS_MAJOR);
				}
			};
		}
		return actionCreateMajor;
	}

	private AbstractAction getActionBuildItem() {
		if(actionBuildItem == null) {
			actionBuildItem = new AbstractAction("BuildItem", null) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					ItemBuilderWizard wizard = new ItemBuilderWizard(shop.generator,shop.procName,shop);
					//wizard.addItemCreatedListener(this);
					wizard.startWizard();
				}
			};
		}
		return actionBuildItem;
	}

	private AbstractAction getActionDelete() {
		if(actionDelete == null) {
			actionDelete = new AbstractAction("Delete", null) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					while (itemJList.getSelectedIndex() != -1) {
						shop.deleteItemAt(itemJList.getSelectedIndex());
					}
				}
			};
		}
		return actionDelete;
	}

	private AbstractAction getActionRecreate() {
		if(actionRecreate == null) {
			actionRecreate = new AbstractAction("Recreate", null) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					int[] selected = itemJList.getSelectedIndices();
					for (int i : selected) {
						shop.recreateItemAt(i);
					}
					Item i = shop.getElementAt(itemJList.getSelectedIndex());
					itemTextArea.setText(DefaultItemFormatter.getItemDescription(i));
				}
			};
		}
		return actionRecreate;
	}

	private void itemJListValueChanged(ListSelectionEvent evt) {
		if (!evt.getValueIsAdjusting()) {
			if (itemJList.getSelectedIndex() == -1) {
				itemTextArea.setText("");
			} else {
				Item i = shop.getElementAt(itemJList.getSelectedIndex());
				itemTextArea.setText(DefaultItemFormatter.getItemDescription(i));
			}
		}
	}

	private JPanel getMultiJPanel() {
		if(multiJPanel == null) {
			multiJPanel = new JPanel();
			GroupLayout multiJPanelLayout = new GroupLayout(multiJPanel);
			multiJPanel.setLayout(multiJPanelLayout);
			multiJPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
			multiJPanelLayout.setHorizontalGroup(multiJPanelLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(multiJPanelLayout.createParallelGroup()
							.addGroup(multiJPanelLayout.createSequentialGroup()
									.addGroup(multiJPanelLayout.createParallelGroup()
											.addComponent(getCategoryJSpinner(Item.CLASS_MINOR), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
											.addComponent(getCategoryJSpinner(Item.CLASS_MEDIUM), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
											.addComponent(getCategoryJSpinner(Item.CLASS_MAJOR), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(multiJPanelLayout.createParallelGroup()
													.addComponent(getCategoryJLabel(Item.CLASS_MINOR), GroupLayout.Alignment.LEADING, 0, 94, Short.MAX_VALUE)
													.addComponent(getCategoryJLabel(Item.CLASS_MEDIUM), GroupLayout.Alignment.LEADING, 0, 94, Short.MAX_VALUE)
													.addComponent(getCategoryJLabel(Item.CLASS_MAJOR), GroupLayout.Alignment.LEADING, 0, 94, Short.MAX_VALUE)))
													.addComponent(getCreateItemsJButton(), GroupLayout.Alignment.LEADING, 0, 150, Short.MAX_VALUE))
													.addContainerGap());
			multiJPanelLayout.setVerticalGroup(multiJPanelLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(multiJPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(getCategoryJSpinner(Item.CLASS_MINOR), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getCategoryJLabel(Item.CLASS_MINOR), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addGroup(multiJPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(getCategoryJSpinner(Item.CLASS_MEDIUM), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(getCategoryJLabel(Item.CLASS_MEDIUM), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
									.addGroup(multiJPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
											.addComponent(getCategoryJSpinner(Item.CLASS_MAJOR), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(getCategoryJLabel(Item.CLASS_MAJOR), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(getCreateItemsJButton(), GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
											.addContainerGap(51, 51));
		}
		return multiJPanel;
	}

	private JSpinner getCategoryJSpinner(int cat) {
		if(categorySpinners[cat] == null) {
			categorySpinnerModels[cat] = new SpinnerNumberModel(0, 0, 1000, 1);
			categorySpinners[cat] = new JSpinner();
			categorySpinners[cat].setModel(categorySpinnerModels[cat]);
		}
		return categorySpinners[cat];
	}

	private JLabel getCategoryJLabel(int cat) {
		if(categoryLabels[cat] == null) {
			categoryLabels[cat] = new JLabel();
			categoryLabels[cat].setText(categoryNames[cat]+" Items");
		}
		return categoryLabels[cat];
	}

	private JButton getCreateItemsJButton() {
		if(createItemsJButton == null) {
			createItemsJButton = new JButton();
			createItemsJButton.setText("Create Items");
			createItemsJButton.setAction(getActionCreateItems());
		}
		return createItemsJButton;
	}

}
