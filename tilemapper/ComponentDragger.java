package tilemapper;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

/* New implementation
 * 1. Drag sources create components that represent the dragged data and also provide default rendering
 * 2. Drag targets take control of the rendering of the component once the cursor enters them
 *
 * a problem with this implementation is that the mouse events during the drag only seem to go to the source component,
 * so any additional processing required by the drag needs to be handled by the source component
 * TODO need a solution for this. perhaps propogate the event to the dragComponent
 * TODO this issue of event propogation means that move doesn't work properly as we remove the source component before the drag is complete. perhaps just make it invisible
 */

public class ComponentDragger implements MouseMotionListener, MouseListener {
	public static final int DRAG_START_DISTANCE = 3;	// minimum distance to travel before drag begins
	protected JFrame frame;
	protected JLayeredPane pane;
	protected int startx, starty;
	protected Point origin;
	protected DragSource source = null;
	protected Component sourceComponent = null;	// the component provided by the source to drag
	protected Component dragComponent = null;	// the component currently being dragged
	protected Component over = null;	// last component the drag was over

	public ComponentDragger(JFrame frame) {
		this.frame = frame;
		pane = frame.getLayeredPane();
	}

	public void registerSource(Component src) {
		src.addMouseListener(this);
		src.addMouseMotionListener(this);
	}

	public void unregisterSource(Component src) {
		src.removeMouseListener(this);
		src.removeMouseMotionListener(this);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) return;
		Component c = e.getComponent();
		if (c instanceof DragSource) {
			//System.out.println("Click at " + e.getX() + "," + e.getY());
			source = (DragSource)c;
			over = null;
			startx = e.getX();
			starty = e.getY();
			origin = SwingUtilities.convertPoint(c, e.getX(), e.getY(), pane);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) return;
		if (dragComponent != null) {
			pane.remove(dragComponent);

			Point p = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), pane);
			Component c = getComponentUnderDrag(p);
			if (c instanceof DragTarget) {
				p = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), c);
				p.x -= startx;
				p.y -= starty;
				((DragTarget)c).dragCompleted(p);
				source.dragFinished(false);
			} else {
				source.dragFinished(true);
			}
		}
		dragComponent = null;
		pane.repaint();	// TODO is this necessary?
	}

	// p is in coordinate system of pane (the JLayeredPane)
	private Component getComponentUnderDrag(Point panePoint) {
		Point p = SwingUtilities.convertPoint(pane, panePoint, frame.getContentPane());
		Component deepest = SwingUtilities.getDeepestComponentAt(frame.getContentPane(), p.x, p.y);
		// look for an ancestor that is DragTarget - if there is then we'll use that, if not then we'll use the deepest component
		Component c = deepest;
		while (c != frame.getContentPane() && c != null) {
			p = SwingUtilities.convertPoint(pane, panePoint, c);
			if (c instanceof DragTarget && c.contains(p)) {
				return c;
			}
			c = c.getParent();
		}
		return deepest;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point loc = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), pane);
		int x = loc.x - startx;
		int y = loc.y - starty;
		if (dragComponent != null) {
			Component c = getComponentUnderDrag(loc);
			if (c != over) {
				Component newComponent = sourceComponent;
				if (over instanceof DragTarget) {
					((DragTarget)over).dragExited(sourceComponent);
				}
				over = c;
				if (over instanceof DragTarget) {
					newComponent = ((DragTarget)over).dragEntered(sourceComponent);
					if (newComponent == null) newComponent = sourceComponent;
				}
				if (dragComponent != newComponent) {
					pane.remove(dragComponent);
					dragComponent = newComponent;
					pane.add(dragComponent,new Integer(JLayeredPane.DRAG_LAYER));
					pane.repaint();
				}
			}
			dragComponent.setLocation(x, y);
		} else if (source != null) {
			if (loc.distance(origin) > DRAG_START_DISTANCE) {
				// We've moved 3 pixels, set up drag
				sourceComponent = source.getDragComponent();
				if (sourceComponent != null) {
					registerSource(sourceComponent);
				} else {
					source = null;
					return;
				}

				dragComponent = sourceComponent;
				dragComponent.setLocation(x,y);
				pane.add(dragComponent,new Integer(JLayeredPane.DRAG_LAYER));
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}

}
