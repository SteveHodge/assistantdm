package swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/*
 * JSubSection is a panel consisting of a coloured title bar and a collapsable detail section.
 * The right-hand end of the title bar can be set to display test when the detail area is collapsed.
 * Maximum height is set to the preferred height of the content.
 */
// TODO proper implementation of content pane (as with other containers)
// TODO allow setting of title colour, font etc
// TODO look into the maximum/preferred height code - might be better to use the content's maximum height. probably better to overload those methods

@SuppressWarnings("serial")
public class JSubSection extends JPanel {
	Color titleColor = new Color(60, 150, 255);
	JCheckBox toggle;
	JLabel titleLabel;
	JLabel infoLabel;
	JPanel leftPanel;
	JPanel contentPanel;
	String infoText = null;

	public JSubSection (String t, JPanel content) {
		super(new GridBagLayout());
		contentPanel = content;
		setMaximumSize(new Dimension(Integer.MAX_VALUE, contentPanel.getPreferredSize().height));

		toggle = new JCheckBox(UIManager.getIcon("Tree.expandedIcon"));
		toggle.setSelectedIcon(UIManager.getIcon("Tree.collapsedIcon"));
		toggle.setOpaque(true);
		toggle.addItemListener(e -> {
			if (e.getSource() != toggle) return;
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (infoText != null) infoLabel.setText(infoText);
				leftPanel.setVisible(false);
				contentPanel.setVisible(false);
				firePropertyChange("collapsed",false,true);	// oldvalue assumes the state has really changed
				JSubSection.this.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLabel.getHeight()));
				revalidate();
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				infoLabel.setText("");
				leftPanel.setVisible(true);
				contentPanel.setVisible(true);
				firePropertyChange("collapsed",true,false);	// oldvalue assumes the state has really changed
				JSubSection.this.setMaximumSize(new Dimension(Integer.MAX_VALUE, contentPanel.getPreferredSize().height));
				revalidate();
			}
		});
		toggle.setBackground(titleColor);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0; c.weighty = 0;
		add(toggle, c);

		titleLabel = new JLabel(t);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setBackground(titleColor);
		titleLabel.setOpaque(true);
		c.gridx = 1; c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.ipadx = 2; c.ipady = 2;
		add(titleLabel, c);

		infoLabel = new JLabel();
		infoLabel.setForeground(Color.WHITE);
		infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD));
		infoLabel.setBackground(titleColor);
		infoLabel.setOpaque(true);
		infoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		c.gridx = 2; c.gridy = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_END;
		add(infoLabel, c);

		leftPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g;
				Paint oldPaint = g2.getPaint();
				Dimension d = getSize();
				GradientPaint gradient = new GradientPaint(
						0f, 0f, titleColor,
						d.width, d.height, getBackground()
						);

				g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
				g2.setPaint(gradient);
				g.fillRect(0, 0, d.width, d.height);
				g2.setPaint(oldPaint);

			}
		};
		leftPanel.setMinimumSize(new Dimension(16, 0));
		leftPanel.setMaximumSize(new Dimension(16, Integer.MAX_VALUE));
		leftPanel.setPreferredSize(new Dimension(16, 0));
		c.gridx = 0; c.gridy = 1;
		c.weightx = 0; c.weighty = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		c.ipadx = 0; c.ipady = 0;
		add(leftPanel, c);

		c.gridx = 1; c.gridy = 1;
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(1,1,1,1);
		add(contentPanel, c);
	}

	public void setTitle(String t) {
		titleLabel.setText(t);
	}

	public String getTitle() {
		return titleLabel.getText();
	}

	public void setInfoText(String t) {
		infoText = t;
		if (isCollapsed()) infoLabel.setText(t);
	}

	public String getInfoText() {
		return infoText;
	}

	// TODO broken...
	public void setContentPane(JPanel panel) {
		System.err.println("broken setContentPane");
//		if (contentPanel != null && contentPanel != panel) {
//			remove(contentPanel);
//			contentPanel = panel;
//			add(contentPanel, BorderLayout.CENTER);
//		}
	}

	public JPanel getContentPane() {
		return contentPanel;
	}

	public void setCollapsed(boolean val) {
		toggle.setSelected(val);
	}

	public boolean isCollapsed() {
		return toggle.isSelected();
	}
}
