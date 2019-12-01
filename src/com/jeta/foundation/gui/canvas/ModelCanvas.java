/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.canvas;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is a basic canvas class that allows clients to create widgets and
 * drag/resize them on the canvas.
 * 
 * @author Jeff Tassin
 */
public class ModelCanvas extends TSPanel implements MouseListener, MouseMotionListener {
	static final long serialVersionUID = 1408165987646849598L;

	protected transient PositionController m_positioncontroller = new PositionController(this);
	protected transient RubberBand m_rubberband;
	protected transient LinkedList m_selectedItems = new LinkedList();

	/** CanvasEvent listeners */
	private LinkedList m_listeners = new LinkedList();

	/**
	 * Time stamp that tracks the last modified time
	 */
	private long m_modified_stamp = 0;

	/**
	 * ctor
	 */
	public ModelCanvas() {
		m_rubberband = new RubberBand(this);
		setLayout(new CanvasLayoutManager());

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * Appends the specified component to the end of this container.
	 */
	public Component add(Component comp, int index) {
		if (!(comp instanceof ResizeHandle)) {
			// this is for the ModelCanvas to handle select/deselect.
			comp.addMouseListener(this);
			// the position controller allows the user to move the tablewidget
			// with the mouse
			comp.addMouseListener(m_positioncontroller);
			comp.addMouseMotionListener(m_positioncontroller);

			notifyComponentChanged(new CanvasEvent(CanvasEvent.ID_COMPONENT_ADDED, comp));
		}

		return super.add(comp, index);
	}

	/**
	 * Appends the specified component to the end of this container.
	 */
	public Component add(Component comp) {
		return add(comp, -1);
	}

	public void addListener(CanvasListener listener) {
		m_listeners.remove(listener);
		m_listeners.add(listener);
	}

	/**
	 * Cancels the selection rectangle drag
	 */
	public void cancelRubberBand() {
		m_rubberband.stopDrag(0, 0);
	}

	public void deselectAll() {
		Iterator iter = m_selectedItems.iterator();
		while (iter.hasNext()) {
			ResizeController rc = (ResizeController) iter.next();
			rc.deselectComponent();
			iter.remove();
		}
	}

	/**
	 * Deselects the given component if it is selected in the view
	 */
	public void deselectComponent(JComponent c) {
		if (c == null)
			return;

		Iterator iter = m_selectedItems.iterator();
		while (iter.hasNext()) {
			ResizeController rc = (ResizeController) iter.next();
			if (rc.getComponent() == c) {
				rc.deselectComponent();
				iter.remove();
			}
		}
	}

	/**
	 * Returns the last time the canvas (or a component on the canvas) was
	 * changed.
	 */
	public long getLastModifiedTime() {
		return m_modified_stamp;
	}

	/**
	 * @return the resize controller for a component if it is selected. Null is
	 *         returned if the component is not selected
	 */
	public ResizeController getResizeController(JComponent comp) {
		LinkedList list = new LinkedList();
		Iterator iter = m_selectedItems.iterator();
		while (iter.hasNext()) {
			ResizeController rc = (ResizeController) iter.next();
			if (rc.getComponent() == comp)
				return rc;
		}
		return null;
	}

	/**
	 * @return the collection of widgets that are currently selected by the user
	 *         If no items are selected, an emtpy collection is returned
	 */
	public Collection getSelectedItems() {
		LinkedList list = new LinkedList();
		Iterator iter = m_selectedItems.iterator();
		while (iter.hasNext()) {
			ResizeController rc = (ResizeController) iter.next();
			list.add(rc.getComponent());
		}
		return list;
	}

	/**
	 * @return true if the given component is selected
	 */
	public boolean isSelected(JComponent comp) {
		return (getResizeController(comp) != null);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.getSource() == this) {
			if (!e.isPopupTrigger()) {
				boolean bselected = false;
				int index = 0;
				while (index < getComponentCount()) {
					Component c = this.getComponent(index);
					if (c.hasFocus()) {
						this.requestFocus();
						break;
					}
					index++;
				}

				deselectAll();
				m_rubberband.startDrag(e.getX(), e.getY());
				this.repaint();
				this.requestFocus();
			}
		} else {
			if (!e.isPopupTrigger()) {
				JComponent comp = (JComponent) e.getSource();
				if (e.isControlDown()) {
					if (isSelected(comp))
						deselectComponent(comp);
					else
						selectComponent(comp);
				} else {
					if (!isSelected(comp)) {
						Iterator iter = m_selectedItems.iterator();
						while (iter.hasNext()) {
							ResizeController rc = (ResizeController) iter.next();
							if (rc.getComponent() != comp) {
								rc.deselectComponent();
								iter.remove();
							}
						}
						selectComponent(comp);
					}
				}
				this.requestFocus();
			}
		}
	}

	/**
	 * MouseListener implementation of events on the canvas. Check if we are
	 * dragging the rubberband. If so, then stop the drag on this release event
	 * and select any components that intersect with the rubberband bounds.
	 */
	public void mouseReleased(MouseEvent e) {
		if (m_rubberband.isDragging()) {
			m_rubberband.stopDrag(e.getX(), e.getY());

			Rectangle rbrect = m_rubberband.getBounds();
			int count = getComponentCount();
			for (int index = 0; index < count; index++) {
				JComponent childcomp = (JComponent) getComponent(index);
				if (rbrect.intersects(childcomp.getBounds())) {
					selectComponent(childcomp);
				}
			}
		}
		this.requestFocus();
	}

	public void mouseDragged(MouseEvent e) {
		if (m_rubberband.isDragging())
			m_rubberband.doDrag(e.getX(), e.getY());
	}

	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * Notifies andy listeners the canvas component has changed. We do this
	 * because for many cases we don't want to burden some clients with adding
	 * listeners to ever component on the canvas.
	 * 
	 * @param comp
	 *            the component that changed position or size
	 */
	public void notifyCanvasChanged() {
		notifyComponentChanged(new CanvasEvent(CanvasEvent.ID_CANVAS_CHANGED, null));
	}

	/**
	 * Notifies andy listeners that a component has changed. We do this because
	 * for many cases we don't want to burden some clients with adding listeners
	 * to ever component on the canvas.
	 * 
	 * @param comp
	 *            the component that changed position or size
	 */
	public void notifyComponentChanged(CanvasEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			CanvasListener listener = (CanvasListener) iter.next();
			listener.canvasEvent(evt);
		}
		touchModifiedTime();
	}

	/**
	 * Sets the last modified time stamp to the current time
	 */
	public void touchModifiedTime() {
		m_modified_stamp = System.currentTimeMillis();
	}

	/**
	 * Sets the modified time to zero for initialization.
	 */
	public void resetModifiedTime() {
		m_modified_stamp = 0;
	}

	public void paint(Graphics g) {
		if (g instanceof java.awt.print.PrinterGraphics) {
			setDoubleBuffered(false);
			super.paint(g);
			setDoubleBuffered(true);
		} else {
			super.paint(g);
			if (m_rubberband.isDragging())
				m_rubberband.paint(g);
		}

	}

	public void remove(Component c) {
		if (c == null)
			return;

		deselectComponent((JComponent) c);
		if (!(c instanceof ResizeHandle)) {
			notifyComponentChanged(new CanvasEvent(CanvasEvent.ID_COMPONENT_REMOVED, c));
		}

		super.remove(c);
	}

	public void removeListener(CanvasListener listener) {
		m_listeners.remove(listener);
	}

	/**
	 * Repaints the given component as well as any resize handles that are
	 * present if the component is selected
	 */
	public void repaint(JComponent comp) {
		Rectangle rect = comp.getBounds();
		comp.repaint(rect.x - 1, rect.y - 1, rect.width + 2, rect.height + 2);
		ResizeController rc = getResizeController(comp);
		if (rc != null)
			rc.repositionHandles();
	}

	/**
	 * Selects all components in the canvas
	 */
	public void selectAll() {
		int count = getComponentCount();
		for (int index = 0; index < count; index++) {
			JComponent comp = (JComponent) getComponent(index);
			if (!isSelected(comp))
				selectComponent(comp);
		}
	}

	public void selectComponent(JComponent comp) {
		if (comp == null)
			return;

		if (comp instanceof ResizeHandle)
			return;

		boolean bselect = true;
		Iterator iter = m_selectedItems.iterator();
		while (iter.hasNext()) {
			ResizeController rc = (ResizeController) iter.next();
			if (rc.getComponent() == comp) {
				bselect = false;
				break;
			}
		}

		if (bselect) {
			ResizeController rz = new ResizeController(this);
			rz.selectComponent(this, comp);
			repaint();
			m_selectedItems.add(rz);
		}
	}

	/**
	 * This method iterates over all components in the canvas and makes sure
	 * that they are at valid locations on the canvas. Any components that are
	 * at negative locations or are at positions greater than width/height of
	 * canvas are moved to valid locations
	 */
	public void validateComponents() {
		int width = getWidth();
		int height = getHeight();
		int count = getComponentCount();

		for (int index = 0; index < count; index++) {
			JComponent comp = (JComponent) getComponent(index);
			if (!(comp instanceof ResizeHandle)) {
				int x = comp.getX();
				int y = comp.getY();
				if (x < 0) {
					TSUtils.printMessage("ModelCanvas failed validation. getX() < 0 for component");
					x = 0;
				}

				if (y < 0) {
					TSUtils.printMessage("ModelCanvas failed validation. getY() < 0 for component");
					y = 0;
				}

				if ((x + comp.getWidth()) > width) {
					TSUtils.printMessage("ModelCanvas failed validation. component off right edge of canvase");
					x = width - comp.getWidth();
					if (x < 0)
						x = 0;
				}

				if ((y + comp.getHeight()) > height) {
					TSUtils.printMessage("ModelCanvas failed validation. component off bottom edge of canvase");
					y = height - comp.getHeight();
					if (y < 0)
						y = 0;
				}

				if (x != comp.getX() || y != comp.getY())
					comp.setLocation(x, y);
			}
		}
	}

	class CanvasLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			int count = parent.getComponentCount();
			for (int index = 0; index < count; index++) {
				Component c = parent.getComponent(index);
				if (c.getWidth() == 0 || c.getHeight() == 0) {
					Dimension d = c.getPreferredSize();
					c.setSize(d);
				}
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(12, 12);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(1600, 1600);
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

}
