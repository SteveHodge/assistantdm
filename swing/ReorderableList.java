package swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

// TODO this should probably use a renderer rather than expecting all added entries to be ReorderableListEntries

@SuppressWarnings("serial")
public class ReorderableList extends JLayeredPane implements MouseMotionListener, MouseListener, ListDataListener {
	private static final double DRAG_START_DISTANCE = 3;

	protected ReorderableListModel model;

	protected int maxWidth = 0;		// width of widest child 

	protected ReorderableListEntry dragEntry;
	protected Dimension dragEntrySize;	// size of drag entry
	protected int yoffset;	// offset of mouse from top of dragged component 
	protected Point origin;	// mouse position when button is pressed - used to determine if we should start dragging
	protected boolean dragging = false;
	protected int gapTop;		// position of the top of the gap in the list. the height of the gap will be dragEntrySize.height

	public ReorderableList(ReorderableListModel model) {
		model.addListDataListener(this);
		this.model = model;
		setLayout(null);
		addMouseListener(this);
		addMouseMotionListener(this);
		addEntries(0, model.getSize()-1);
	}

	protected void addEntries(int first, int last) {
		for (int i = first; i <= last; i++) {
			ReorderableListEntry entry = (ReorderableListEntry)model.getElementAt(i);
			add(entry);
		}
		layoutList();
	}

	public void contentsChanged(ListDataEvent e) {
		// not sure if we can use getIndex0/1 to determine what has changed
		// assume the list is totally different

		// first add all entries that are not added
		for (int i = 0; i < model.getSize(); i++) {
			ReorderableListEntry entry = (ReorderableListEntry)model.getElementAt(i);
			if (!isAncestorOf(entry)) add(entry);
		}

		// now remove all entries that are no longer in the list
		Component[] children = getComponents();
		for (Component child : children) {
			if (model.indexOf(child) == -1) remove(child);
		}
		layoutList();
	}

	public void intervalAdded(ListDataEvent e) {
		addEntries(e.getIndex0(), e.getIndex1());
	}

	public void intervalRemoved(ListDataEvent e) {
		for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
			ReorderableListEntry entry = (ReorderableListEntry)model.getElementAt(i);
			remove(entry);
		}
		layoutList();
	}

	private void layoutList() {
		int nextTop = getInsets().top;
		maxWidth = 0;
		for (int i=0; i<model.getSize(); i++) {
			JComponent e = (JComponent)model.getElementAt(i);
			Dimension size = e.getPreferredSize();
			if (size.width > maxWidth) maxWidth = size.width;
		}
		for (int i=0; i<model.getSize(); i++) {
			JComponent e = (JComponent)model.getElementAt(i);
			Dimension size = e.getPreferredSize();
			e.setBounds(getInsets().left, nextTop, maxWidth, size.height);
			nextTop += size.height;
		}
		setPreferredSize(new Dimension(maxWidth,nextTop));
		revalidate();
	}

	public void mouseDragged(MouseEvent e) {
		if (dragEntry != null) {
			int y = e.getY() - yoffset;
			if (!dragging && e.getPoint().distance(origin) > DRAG_START_DISTANCE) {
				// We've moved the minimum drag distance, set up drag
				dragging = true;
				setLayer(dragEntry,DRAG_LAYER);
			}
			if (dragging) {
				dragEntry.setBounds(0, y, dragEntrySize.width, dragEntrySize.height);
				// 1. find entry under mouse
				// TODO: scanning the entries like this is pretty inefficient - could track the indexes instead
				Rectangle bounds = null;
				for (int i=0; i<model.getSize(); i++) {
					ReorderableListEntry ie = (ReorderableListEntry)model.getElementAt(i);
					if (ie == dragEntry) continue;
					bounds = ie.getBounds(bounds);
					if (bounds.contains(e.getPoint())) {
						if (e.getY() <= bounds.getCenterY() && e.getY() < gapTop) {
							// 2a. if we are in the top half of the entry and the gap is below
							// 3a.   then gapTop = entry.y and entry.y += dragEntrySize.height
							gapTop = bounds.y;
							bounds.y += dragEntrySize.height;
							ie.setBounds(bounds);
						} else if (e.getY() > bounds.getCenterY() && e.getY() > gapTop) {
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

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		Component comp = getComponentAt(e.getPoint());
		if (comp instanceof ReorderableListEntry) {
			dragEntry = (ReorderableListEntry)comp;
			dragEntrySize = dragEntry.getSize(dragEntrySize);
			yoffset = e.getY() - dragEntry.getY();
			origin = e.getPoint();
			gapTop = comp.getY();
		    e.consume();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (dragging) {
			setLayer(dragEntry,DEFAULT_LAYER);
			dragEntry.setBounds(0, gapTop, dragEntrySize.width, dragEntrySize.height);

			// find the new position - count how many items have y < gapTop
			int count = 0;
			for (int i=0; i<model.getSize(); i++) {
				JComponent comp = (JComponent)model.getElementAt(i);
				if (comp.getY() < gapTop) count++;
			}
			model.moveTo(dragEntry, count);

			dragging = false;
			dragEntry = null;
		}
	}
}
