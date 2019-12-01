package com.jeta.abeille.gui.model;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;

import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.datatransfer.FlavorMap;

import javax.swing.SwingUtilities;

/**
 * Workaround for autoscrolling bug in java.awt.dnd.DropTarget. Downloaded from
 * javasoft bug parade
 * 
 */
public class ObjectTreeDropTarget extends DropTarget {

	public ObjectTreeDropTarget(Component c, int ops, DropTargetListener dtl, boolean act, FlavorMap fm) {
		super(c, ops, dtl, act, fm);
	}

	public ObjectTreeDropTarget(Component c, int ops, DropTargetListener dtl, boolean act) {
		this(c, ops, dtl, act, null);
	}

	public ObjectTreeDropTarget() {
		this(null, DnDConstants.ACTION_COPY_OR_MOVE, null, true, null);
	}

	public ObjectTreeDropTarget(Component c, DropTargetListener dtl) {
		this(c, DnDConstants.ACTION_COPY_OR_MOVE, dtl, true, null);
	}

	public ObjectTreeDropTarget(Component c, int ops, DropTargetListener dtl) {
		this(c, ops, dtl, true);
	}

	/**
	 * this protected nested class works around the real bug, which is in
	 * DropTarget.DropTargetAutoScroller
	 */
	protected static class DropTargetAutoScrollerWorkaround extends DropTarget.DropTargetAutoScroller {
		private Component component2;
		private Autoscroll autoScroll2;

		private Point locn2;
		private Point prev2;

		private Rectangle outer2 = new Rectangle();
		private Rectangle inner2 = new Rectangle();

		private int hysteresis2 = 10;

		protected DropTargetAutoScrollerWorkaround(Component c, Point p) {
			super(c, p);
			component2 = c;
			autoScroll2 = (Autoscroll) c;

			Toolkit t = Toolkit.getDefaultToolkit();

			locn2 = p;
			prev2 = p;

			try {
				hysteresis2 = ((Integer) t.getDesktopProperty("DnD.Autoscroll.cursorHysteresis")).intValue();
			} catch (Exception e) {
				// ignore
			}
		}

		/**
		 * update the geometry of the autoscroll region
		 */
		private void updateRegionCorrectly() {
			Insets i = autoScroll2.getAutoscrollInsets();
			computeVisibleRect(component2, outer2);
			inner2.setBounds(outer2.x + i.left, outer2.y + i.top, outer2.width - (i.left + i.right), outer2.height
					- (i.top + i.bottom));
		}

		// This is copied from JComponent because that method is
		// package-access only. Surely there's an equivalent in awt
		// somewhere ... but if so, why does JComponent have this?
		protected void computeVisibleRect(Component c, Rectangle visibleRect) {
			Container p = c.getParent();
			Rectangle bounds = c.getBounds();

			if (p == null || p instanceof Window || p instanceof java.applet.Applet) {
				visibleRect.setBounds(0, 0, bounds.width, bounds.height);
			} else {
				computeVisibleRect(p, visibleRect);
				visibleRect.x -= bounds.x;
				visibleRect.y -= bounds.y;
				SwingUtilities.computeIntersection(0, 0, bounds.width, bounds.height, visibleRect);
			}
		}

		protected synchronized void updateLocation(Point newLocn) {
			super.updateLocation(newLocn);
			prev2 = locn2;
			locn2 = newLocn;
		}

		public synchronized void actionPerformed(ActionEvent e) {
			// updateRegionCorrectly();

			// if (outer2.contains(locn2) && !inner2.contains(locn2))
			// autoScroll2.autoscroll(locn2);
		}

	}

	protected DropTargetAutoScroller createDropTargetAutoScroller(Component c, Point p) {
		System.out.println("DropTargetAutoScroll.createScroller  p = " + p);
		return new DropTargetAutoScrollerWorkaround(c, p);
	}
}
