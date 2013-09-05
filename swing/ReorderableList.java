package swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

@SuppressWarnings("serial")
public class ReorderableList extends JLayeredPane {
	private static final double DRAG_START_DISTANCE = 3;

	private ReorderableListModel model;

	private int maxWidth = 0;		// width of widest child

	private Component dragEntry;
	private Dimension dragEntrySize;	// size of drag entry
	private int yoffset;	// offset of mouse from top of dragged component
	private Point origin;	// mouse position when button is pressed - used to determine if we should start dragging
	private boolean dragging = false;
	private int gapTop;		// position of the top of the gap in the list. the height of the gap will be dragEntrySize.height

	public ReorderableList(ReorderableListModel model) {
		model.sort();
		model.addListDataListener(listListener);
		this.model = model;
		setLayout(null);
		//listeners are now attached to the entries themselves - this allows them to work even if the
		//entries have their own listeners (which would prevent the events failing through to this
		//component event if they weren't consumed
		//addMouseListener(this);
		//addMouseMotionListener(this);
		addEntries(0, model.getSize() - 1);
	}

	protected void addEntries(int first, int last) {
		for (int i = first; i <= last; i++) {
			Component entry = (Component) model.getElementAt(i);
			entry.addMouseListener(mouseListener);
			entry.addMouseMotionListener(mouseListener);
			add(entry);
		}
		layoutList();
	}

	protected void removeMissingEntries() {
		// remove all entries that are no longer in the list
		Component[] children = getComponents();
		for (Component child : children) {
			if (model.indexOf(child) == -1) remove(child);
		}
		layoutList();
	}

	private void layoutList() {
		if (model.getSize() == 0) {
			repaint();
			return;
		}

		int nextTop = getInsets().top;
		maxWidth = getWidth();
		for (int i = 0; i < model.getSize(); i++) {
			JComponent e = (JComponent) model.getElementAt(i);
			Dimension size = e.getPreferredSize();
			if (size.width > maxWidth) maxWidth = size.width;
		}
		for (int i = 0; i < model.getSize(); i++) {
			JComponent e = (JComponent) model.getElementAt(i);
			Dimension size = e.getPreferredSize();
			e.setBounds(getInsets().left, nextTop, maxWidth, size.height);
			nextTop += size.height;
		}
		setPreferredSize(new Dimension(maxWidth, nextTop));
		revalidate();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		layoutList();
	}

	@Override
	public void setBounds(Rectangle r) {
		super.setBounds(r);
		layoutList();
	}

	@Override
	public void setSize(Dimension d) {
		super.setSize(d);
		layoutList();
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		layoutList();
	}

	private MouseAdapter mouseListener = new MouseAdapter() {
		@Override
		public void mouseDragged(MouseEvent e) {
			Component comp = e.getComponent();
			// TODO check comp is an entry to drag
			Point p = comp.getLocation();
			p.translate(e.getX(), e.getY());
			if (dragEntry != null) {
				int y = p.y - yoffset;
				if (!dragging && p.distance(origin) > DRAG_START_DISTANCE) {
					// We've moved the minimum drag distance, set up drag
					dragging = true;
					setLayer(dragEntry, DRAG_LAYER);
				}
				if (dragging) {
					dragEntry.setBounds(0, y, dragEntrySize.width, dragEntrySize.height);
					// 1. find entry under mouse
					// TODO scanning the entries like this is pretty inefficient - could track the indexes instead
					Rectangle bounds = null;
					for (int i = 0; i < model.getSize(); i++) {
						Component ie = (Component) model.getElementAt(i);
						if (ie == dragEntry) continue;
						bounds = ie.getBounds(bounds);
						if (bounds.contains(p)) {
							if (p.y <= bounds.getCenterY() && p.y < gapTop) {
								// 2a. if we are in the top half of the entry and the gap is below
								// 3a.   then gapTop = entry.y and entry.y += dragEntrySize.height
								gapTop = bounds.y;
								bounds.y += dragEntrySize.height;
								ie.setBounds(bounds);
							} else if (p.y > bounds.getCenterY() && p.y > gapTop) {
								// 2b. else if we are in the bottom half of the entry and the gap is above
								// 3b.   then entryTop = gapTop and gapTop += entry.height
								bounds.y = gapTop;
								gapTop += bounds.height;
								ie.setBounds(bounds);
							}
						}
					}
				}
			}
		}

		// TODO fix for entry-based dragging
		@Override
		public void mousePressed(MouseEvent e) {
//			Component comp = getComponentAt(e.getPoint());
//			if (comp instanceof Component) {
//				dragEntry = (Component)comp;
//				dragEntrySize = dragEntry.getSize(dragEntrySize);
//				yoffset = e.getY() - dragEntry.getY();
//				origin = e.getPoint();
//				gapTop = comp.getY();
//			    e.consume();
//			}
			Component comp = e.getComponent();
			// TODO check if it's a child of this?
			dragEntry = comp;
			dragEntrySize = dragEntry.getSize(dragEntrySize);
			origin = comp.getLocation();
			origin.translate(e.getX(), e.getY());
			yoffset = e.getY();
			gapTop = comp.getY();
			e.consume();
		}

		// TODO fix for entry-based dragging
		@Override
		public void mouseReleased(MouseEvent e) {
			if (dragging) {
				setLayer(dragEntry, DEFAULT_LAYER);
				dragEntry.setBounds(0, gapTop, dragEntrySize.width, dragEntrySize.height);

				// find the new position - count how many items have y < gapTop
				int count = 0;
				for (int i = 0; i < model.getSize(); i++) {
					JComponent comp = (JComponent) model.getElementAt(i);
					if (comp.getY() < gapTop) count++;
				}
				model.moveTo(dragEntry, count);

				dragging = false;
				dragEntry = null;
			}
		}
	};

	private ListDataListener listListener = new ListDataListener() {
		@Override
		public void contentsChanged(ListDataEvent e) {
			// not sure if we can use getIndex0/1 to determine what has changed
			// assume the list is totally different

			// first add all entries that are not added
			for (int i = 0; i < model.getSize(); i++) {
				Component entry = (Component) model.getElementAt(i);
				if (!isAncestorOf(entry)) {
					entry.addMouseListener(mouseListener);
					entry.addMouseMotionListener(mouseListener);
					add(entry);
				}
			}

			// now remove all entries that are no longer in the list
			removeMissingEntries();
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			addEntries(e.getIndex0(), e.getIndex1());
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			removeMissingEntries();
		}
	};
}
