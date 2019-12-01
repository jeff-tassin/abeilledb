/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.canvas;

import java.awt.Rectangle;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;

public class PositionController implements MouseListener, MouseMotionListener {
	private int m_mouse_x;
	private int m_mouse_y;

	private ModelCanvas m_canvas;

	/**
	 * this is a list of items we are dragging in addition and relative to the
	 * drag source
	 */
	private LinkedList m_dragitems = new LinkedList();

	public PositionController() {

	}

	public PositionController(ModelCanvas canvas) {
		m_canvas = canvas;
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

		JComponent source = (JComponent) e.getSource();
		m_mouse_x = e.getX();
		m_mouse_y = e.getY();
		repaintComponent(source);

		m_dragitems = new LinkedList();
		// now get any other selected components and their relative positions
		// from this component
		// assert( m_canvas != null );
		if (m_canvas != null) {
			Collection selitems = m_canvas.getSelectedItems();
			Iterator iter = selitems.iterator();
			while (iter.hasNext()) {
				JComponent comp = (JComponent) iter.next();
				if (comp != source) {
					int deltax = source.getX() - comp.getX();
					int deltay = source.getY() - comp.getY();

					DragItem item = new DragItem(comp, deltax, deltay);
					m_dragitems.add(item);
					repaintComponent(comp);
				}
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		JComponent source = (JComponent) e.getSource();
		setLocation(source, source.getX() + e.getX() - m_mouse_x, source.getY() + e.getY() - m_mouse_y);
		repaintComponent(source);

		if (m_canvas != null) {
			Iterator iter = m_dragitems.iterator();
			while (iter.hasNext()) {
				DragItem item = (DragItem) iter.next();
				int x = source.getX() - item.getDeltaX();
				int y = source.getY() - item.getDeltaY();
				JComponent comp = item.getComponent();
				setLocation(comp, x, y);
				repaintComponent(comp);
			}
		}
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
		JComponent source = (JComponent) e.getSource();
		setLocation(source, source.getX() + e.getX() - m_mouse_x, source.getY() + e.getY() - m_mouse_y);
		repaintComponent(source);

		if (m_canvas != null) {
			Iterator iter = m_dragitems.iterator();
			while (iter.hasNext()) {
				DragItem item = (DragItem) iter.next();
				int x = source.getX() - item.getDeltaX();
				int y = source.getY() - item.getDeltaY();
				JComponent comp = item.getComponent();
				setLocation(comp, x, y);
				repaintComponent(comp);
			}

			m_canvas.validateComponents();
		}

	}

	/**
	 * Repaints the component. If the canvas is not null, then we forward the
	 * call to the canvas so it can also repaint any resize handles as well
	 */
	private void repaintComponent(JComponent comp) {
		if (m_canvas == null) {
			// Rectangle rect = comp.getBounds();
			// comp.repaint( rect.x - 1, rect.y - 1, rect.width + 2, rect.height
			// + 2);
			comp.repaint();
		} else {
			m_canvas.repaint(comp);
		}
	}

	private CanvasEvent m_comp_moved = new CanvasEvent(CanvasEvent.ID_COMPONENT_MOVED, null);

	public void setLocation(JComponent comp, int x, int y) {
		assert (comp != null);
		if (comp.getX() != x || comp.getY() != y) {
			comp.setLocation(x, y);
			if (m_canvas != null) {
				m_comp_moved.setComponent(comp);
				m_canvas.notifyComponentChanged(m_comp_moved);
			}
		}
	}

	/**
	 * Class that keeps information about a component that we are dragging as
	 * part of a set of components. The delta offsets are relative to the main
	 * component of the selection.
	 */
	class DragItem {
		private JComponent m_component;
		private int m_deltax;
		private int m_deltay;

		DragItem(JComponent comp, int deltaX, int deltaY) {
			m_component = comp;
			m_deltax = deltaX;
			m_deltay = deltaY;
		}

		public JComponent getComponent() {
			return m_component;
		}

		public int getDeltaX() {
			return m_deltax;
		}

		public int getDeltaY() {
			return m_deltay;
		}
	}
}
