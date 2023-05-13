package com.jeta.abeille.gui.model;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.TreePath;

/**
 * This class is used to provide auto scrolling during drag and drop in an
 * ObjectTree. We have this class because the Swing Autoscroll implementation
 * seems a little screwy.
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeAutoScroller implements ActionListener {

	private ObjectTree m_tree;

	/**
	 * the view port for the object tree. The tree must have view port as its
	 * ancestor
	 */
	private JViewport m_viewport;

	private javax.swing.Timer m_timer;

	/** the last known mouse position */
	private Point m_lastpoint = new Point(0, 0);

	/**
	 * ctor
	 */
	public ObjectTreeAutoScroller(ObjectTree tree) {
		m_timer = new javax.swing.Timer(200, this);
		m_tree = tree;
	}

	/**
	 * Invoked when a drag operation is going on
	 * 
	 */
	public Point dragOver(DropTargetDragEvent event) {
		Point pt = event.getLocation();

		if (!m_timer.isRunning())
			start(pt);

		return dragOver(pt);
	}

	/**
	 * Invoked when a drag operation is going on. Check if the mouse is within
	 * the drag boundary at top and bottom of tree. If so, auto scroll if
	 * possible.
	 * 
	 * @param pt
	 *            the mouse point (in ObjectTree coordinates - as passed by the
	 *            DropTargetDragEvent)
	 */
	private Point dragOver(Point pt) {
		int SCROLL_DISTANCE = 16;
		JViewport viewport = getViewport();

		Rectangle rect = m_tree.getVisibleRect();
		Rectangle rect2 = viewport.getViewRect();

		// System.out.println( "point: " + pt + "  visible rect:  " + rect );
		// System.out.println( "point: " + pt + "  viewport view rect:  " +
		// rect2 );

		rect.grow(-10, -10);
		if (pt.y <= rect.y) {
			// scroll up
			rect = m_tree.getVisibleRect();
			rect.setLocation(0, rect.y - SCROLL_DISTANCE);
			if (rect.y >= 0) {
				System.out.println("scroll up: " + rect);
				// viewport.scrollRectToVisible( rect );
				viewport.setViewPosition(new Point(0, rect.y));
				pt.y -= SCROLL_DISTANCE;
			}
		} else if (pt.y >= (rect.y + rect.height)) {
			// scroll down
			rect = m_tree.getVisibleRect();
			rect.translate(0, SCROLL_DISTANCE);
			// System.out.println( "Scroll down: " + rect + "    tree height = "
			// + m_tree.getHeight() );
			if ((rect.y + rect.height) <= m_tree.getHeight()) {
				System.out.println("scroll down: " + rect);
				// viewport.scrollRectToVisible( rect );
				viewport.setViewPosition(new Point(0, rect.y));
				pt.y += SCROLL_DISTANCE;
			}
		}

		m_lastpoint = pt;
		return pt;
	}

	/**
	 * @return the viewport for the given tree
	 */
	JViewport getViewport() {
		if (m_viewport == null) {
			Container parent = m_tree.getParent();
			if (parent instanceof JViewport)
				m_viewport = (JViewport) parent;
			else {
				parent = parent.getParent();
				if (parent instanceof JViewport)
					m_viewport = (JViewport) parent;
			}

			assert (m_viewport != null);
		}
		return m_viewport;
	}

	/**
	 * Timer callback
	 */
	public void actionPerformed(ActionEvent evt) {
		dragOver(m_lastpoint);
	}

	/**
	 * Starts the timer for the auto scroller
	 */
	void start(Point pt) {
		m_lastpoint = pt;
		m_timer.start();
	}

	/**
	 * Stops the timer for the auto scroller
	 */
	void stop() {
		m_timer.stop();
	}
}
