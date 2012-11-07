package swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/*
 * JSubSection is a panel consisting of a coloured title bar and a collapsable detail section
 */
// TODO proper implementation of content pane (as with other containers)
// TODO allow setting of title colour, font etc

@SuppressWarnings("serial")
public class JSubSection extends JPanel {
	Color titleColor = new Color(60, 150, 255);
	JCheckBox toggle;
	JPanel leftPanel;
	JPanel contentPanel;
	JLabel titleLabel;
	String title;
	String collapsedTitle = null;

	public JSubSection (String t, JPanel content) {
		super(new BorderLayout());
		contentPanel = content;
		title = t;

		JPanel titlePanel = new JPanel();
		toggle = new JCheckBox(UIManager.getIcon("Tree.expandedIcon"));
		toggle.setSelectedIcon(UIManager.getIcon("Tree.collapsedIcon"));
		toggle.setOpaque(false);
		toggle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getSource() != toggle) return;
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (collapsedTitle != null) titleLabel.setText(collapsedTitle);
					leftPanel.setVisible(false);
					contentPanel.setVisible(false);
					firePropertyChange("collapsed",false,true);	// oldvalue assumes the state has really changed
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					titleLabel.setText(title);
					leftPanel.setVisible(true);
					contentPanel.setVisible(true);
					firePropertyChange("collapsed",true,false);	// oldvalue assumes the state has really changed
				}
			}
		});
		titlePanel.add(toggle);

		titleLabel = new JLabel(title);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2 ,2));
		titlePanel.add(titleLabel);
		titlePanel.setBackground(titleColor);

		leftPanel = new JPanel() {
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g;
		        Paint oldPaint = g2.getPaint();
		        Dimension d = getSize();
		        GradientPaint gradient = new GradientPaint(
		          0f, 0f, titleColor,
		          (float)d.width, (float)d.height, getBackground()
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

		add(titlePanel, BorderLayout.NORTH);
		add(leftPanel, BorderLayout.WEST);
		add(contentPanel, BorderLayout.CENTER);
	}

	public void setTitle(String t) {
		title = t;
		if (!isCollapsed() || collapsedTitle == null) titleLabel.setText(t);
	}

	public String getTitle() {
		return title;
	}

	public void setCollapsedTitle(String t) {
		collapsedTitle = t;
		if (isCollapsed()) titleLabel.setText(t);
	}

	public String getCollapsedTitle() {
		return collapsedTitle;
	}
	
	public void setContentPane(JPanel panel) {
		if (contentPanel != null && contentPanel != panel) {
			remove(contentPanel);
			contentPanel = panel;
			add(contentPanel, BorderLayout.CENTER);
		}
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
