/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.canvas;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.lang.ref.WeakReference;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;

public class ResizeController implements MouseListener, MouseMotionListener {

	private WeakReference m_canvasref;
	private HashMap m_controllers = new HashMap();
	private static final int HANDLE_OFFSET = 2;
	private static final int HANDLE_COUNT = 4;
	private JComponent m_component;
	private JComponent m_container;
	private static final String NORTHWEST = "northwest";
	private static final String NORTHEAST = "northeast";
	private static final String SOUTHEAST = "southeast";
	private static final String SOUTHWEST = "southwest";

	public ResizeController(ModelCanvas canvas) {
		m_canvasref = new WeakReference(canvas);
		ResizeHandle handle = new ResizeHandle(NORTHWEST);
		HandleController controller = new NorthwestHandler(handle);
		handle.addMouseMotionListener(this);
		handle.addMouseListener(this);
		m_controllers.put(NORTHWEST, controller);

		handle = new ResizeHandle(NORTHEAST);
		controller = new NortheastHandler(handle);
		handle.addMouseMotionListener(this);
		handle.addMouseListener(this);
		m_controllers.put(NORTHEAST, controller);

		handle = new ResizeHandle(SOUTHEAST);
		controller = new SoutheastHandler(handle);
		handle.addMouseMotionListener(this);
		handle.addMouseListener(this);
		m_controllers.put(SOUTHEAST, controller);

		handle = new ResizeHandle(SOUTHWEST);
		controller = new SouthwestHandler(handle);
		handle.addMouseMotionListener(this);
		handle.addMouseListener(this);
		m_controllers.put(SOUTHWEST, controller);
	}

	public void deselectComponent() {
		if (m_component == null || m_container == null)
			return;

		Iterator iter = m_controllers.keySet().iterator();
		while (iter.hasNext()) {
			HandleController controller = (HandleController) m_controllers.get((String) iter.next());
			ResizeHandle handle = controller.getHandle();
			m_container.repaint(handle.getBounds());
			m_container.remove(controller.getHandle());
		}
		// m_component.removeMouseListener(this);
		// m_component.removeMouseMotionListener( this );
		m_component = null;
		m_container = null;

	}

	public JComponent getComponent() {
		return m_component;
	}

	HandleController getController(String handleTag) {
		return (HandleController) m_controllers.get(handleTag);
	}

	ResizeHandle getHandle(String handleTag) {
		HandleController controller = getController(handleTag);
		if (controller != null)
			return controller.getHandle();
		else
			return null;
	}

	public void mouseDragged(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof ResizeHandle) {
			ResizeHandle handle = (ResizeHandle) source;
			HandleController controller = getController(handle.getTag());
			if (controller != null)
				controller.drag();
		} else if (source == m_component) {
			// repositionHandles();
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
	}

	public void mouseReleased(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof ResizeHandle) {
			ResizeHandle handle = (ResizeHandle) source;
			HandleController controller = getController(handle.getTag());
			if (controller != null)
				controller.drag();
		}
	}

	public void repositionHandles() {
		Iterator iter = m_controllers.keySet().iterator();
		while (iter.hasNext()) {
			String tag = (String) iter.next();
			HandleController controller = (HandleController) m_controllers.get(tag);
			controller.reposition();
		}
	}

	public void selectComponent(JComponent container, JComponent comp) {
		m_component = comp;
		m_container = container;
		repositionHandles();

		Iterator iter = m_controllers.keySet().iterator();
		while (iter.hasNext()) {
			String tag = (String) iter.next();
			HandleController controller = (HandleController) m_controllers.get(tag);
			m_container.add(controller.getHandle());
		}
		// m_component.addMouseListener(this);
		// m_component.addMouseMotionListener( this );
	}

	void setBounds(JComponent comp, int x, int y, int width, int height) {
		comp.setBounds(x, y, width, height);
		ModelCanvas canvas = (ModelCanvas) m_canvasref.get();
		if (canvas != null) {
			canvas.notifyComponentChanged(new CanvasEvent(CanvasEvent.ID_COMPONENT_RESIZED, comp));
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	// handle controllers
	abstract class HandleController {
		protected ResizeHandle handle;

		HandleController(ResizeHandle h) {
			handle = h;
		}

		abstract void drag();

		ResizeHandle getHandle() {
			return handle;
		}

		abstract void reposition();

		void syncHandles(String syncX, String syncY) {
			ResizeHandle xhandle = ResizeController.this.getHandle(syncX);
			xhandle.setLocation(handle.getX(), xhandle.getY());

			ResizeHandle yhandle = ResizeController.this.getHandle(syncY);
			yhandle.setLocation(yhandle.getX(), handle.getY());

		}
	}

	class NorthwestHandler extends HandleController {
		NorthwestHandler(ResizeHandle handle) {
			super(handle);
		}

		public void drag() {
			int orgx = handle.getX() + handle.getWidth() + HANDLE_OFFSET;
			int orgy = handle.getY() + handle.getHeight() + HANDLE_OFFSET;

			int newwidth = m_component.getWidth() - (orgx - m_component.getX());
			int newheight = m_component.getHeight() - (orgy - m_component.getY());

			if (!resizeComponent(orgx, orgy, newwidth, newheight))
				reposition();

			syncHandles(SOUTHWEST, NORTHEAST);
		}

		void reposition() {
			int x = m_component.getX() - HANDLE_OFFSET - handle.getWidth();
			int y = m_component.getY() - HANDLE_OFFSET - handle.getHeight();
			handle.setLocation(x, y);
		}

		boolean resizeComponent(int orgx, int orgy, int newwidth, int newheight) {
			boolean bresize = true;
			Dimension d = m_component.getMinimumSize();
			if (newwidth < d.getWidth()) {
				newwidth = (int) d.getWidth();
				bresize = false;
			}
			if (newheight < d.getHeight()) {
				newheight = (int) d.getHeight();
				bresize = false;
			}

			if (bresize) {
				setBounds(m_component, orgx, orgy, newwidth, newheight);
			} else {
				int neworgx = m_component.getX() + m_component.getWidth() - newwidth;
				int neworgy = m_component.getY() + m_component.getHeight() - newheight;
				setBounds(m_component, neworgx, neworgy, newwidth, newheight);
			}

			return bresize;
		}
	}

	class NortheastHandler extends HandleController {
		NortheastHandler(ResizeHandle handle) {
			super(handle);
		}

		public void drag() {
			int orgx = m_component.getX();
			int orgy = handle.getY() + handle.getHeight() + HANDLE_OFFSET;

			int newwidth = handle.getX() - HANDLE_OFFSET - orgx;
			int newheight = m_component.getHeight() - (orgy - m_component.getY());

			if (!resizeComponent(orgx, orgy, newwidth, newheight))
				reposition();

			syncHandles(SOUTHEAST, NORTHWEST);
		}

		void reposition() {
			int x = m_component.getX() + m_component.getWidth() + HANDLE_OFFSET;
			int y = m_component.getY() - HANDLE_OFFSET - handle.getHeight();
			handle.setLocation(x, y);
		}

		boolean resizeComponent(int orgx, int orgy, int newwidth, int newheight) {
			boolean bresize = true;
			Dimension d = m_component.getMinimumSize();
			if (newwidth < d.getWidth()) {
				newwidth = (int) d.getWidth();
				bresize = false;
			}
			if (newheight < d.getHeight()) {
				newheight = (int) d.getHeight();
				bresize = false;
			}

			if (bresize)
				setBounds(m_component, orgx, orgy, newwidth, newheight);
			else {
				int neworgy = m_component.getY() + m_component.getHeight() - newheight;
				setBounds(m_component, orgx, neworgy, newwidth, newheight);
			}

			return bresize;
		}
	}

	class SouthwestHandler extends HandleController {
		SouthwestHandler(ResizeHandle handle) {
			super(handle);
		}

		public void drag() {
			int orgx = handle.getX() + handle.getWidth() + HANDLE_OFFSET;
			int orgy = m_component.getY();

			int newwidth = m_component.getWidth() - (orgx - m_component.getX());
			int newheight = handle.getY() - HANDLE_OFFSET - m_component.getY();

			if (!resizeComponent(orgx, orgy, newwidth, newheight))
				reposition();

			syncHandles(NORTHWEST, SOUTHEAST);
		}

		void reposition() {
			int x = m_component.getX() - HANDLE_OFFSET - handle.getWidth();
			int y = m_component.getY() + m_component.getHeight() + HANDLE_OFFSET;
			handle.setLocation(x, y);
		}

		boolean resizeComponent(int orgx, int orgy, int newwidth, int newheight) {
			boolean bresize = true;
			Dimension d = m_component.getMinimumSize();
			if (newwidth < d.getWidth()) {
				newwidth = (int) d.getWidth();
				bresize = false;
			}
			if (newheight < d.getHeight()) {
				newheight = (int) d.getHeight();
				bresize = false;
			}

			if (bresize)
				setBounds(m_component, orgx, orgy, newwidth, newheight);
			else {
				int neworgx = m_component.getX() + m_component.getWidth() - newwidth;
				setBounds(m_component, neworgx, orgy, newwidth, newheight);
			}
			return bresize;
		}

	}

	class SoutheastHandler extends HandleController {
		SoutheastHandler(ResizeHandle handle) {
			super(handle);
		}

		public void drag() {
			int newwidth = handle.getX() - HANDLE_OFFSET - m_component.getX();
			int newheight = handle.getY() - HANDLE_OFFSET - m_component.getY();

			if (!resizeComponent(m_component.getX(), m_component.getY(), newwidth, newheight))
				reposition();

			syncHandles(NORTHEAST, SOUTHWEST);
		}

		boolean resizeComponent(int orgx, int orgy, int newwidth, int newheight) {
			boolean bresize = true;
			Dimension d = m_component.getMinimumSize();
			if (newwidth < d.getWidth()) {
				newwidth = (int) d.getWidth();
				bresize = false;
			}
			if (newheight < d.getHeight()) {
				newheight = (int) d.getHeight();
				bresize = false;
			}

			setBounds(m_component, orgx, orgy, newwidth, newheight);
			return bresize;
		}

		void reposition() {
			int x = m_component.getX() + m_component.getWidth() + HANDLE_OFFSET;
			int y = m_component.getY() + m_component.getHeight() + HANDLE_OFFSET;
			handle.setLocation(x, y);
		}

	}

}
