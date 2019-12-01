package com.jeta.abeille.gui.model.overview;

import java.awt.Rectangle;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewFrame;

public class PositionController implements MouseListener, MouseMotionListener {
	private int m_mouse_x;
	private int m_mouse_y;

	private CanvasOverview m_overview;

	private DragItem m_dragitem;

	public PositionController(CanvasOverview overview) {
		m_overview = overview;
	}

	public void centerView(int x, int y) {
		ModelView view = m_overview.getFrame().getModelView();

		int view_x = view.getWidth() * x / m_overview.getWidth();
		int view_y = view.getHeight() * y / m_overview.getHeight();

		m_overview.getFrame().centerView(view, view_x, view_y);
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		m_overview.sync(null);

		OverviewComponent comp = m_overview.getComponent(e.getX(), e.getY());
		if (comp == null) {
			centerView(e.getX(), e.getY());
		} else {
			float deltax = e.getX() - comp.getX();
			float deltay = e.getY() - comp.getY();

			m_dragitem = new DragItem(comp, deltax, deltay);
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (m_dragitem != null) {
			float x = e.getX() - m_dragitem.getDeltaX();
			float y = e.getY() - m_dragitem.getDeltaY();
			OverviewComponent ov_comp = m_dragitem.getComponent();
			setLocation(ov_comp, x, y);
		}
	}

	/**
	 * Sets the location of the overview component relative to the
	 * CanvasOverview
	 * 
	 * @param ov_comp
	 *            the OverviewComponen to move
	 * @param x
	 *            the x position to set ( relative to the CanvasOverview)
	 * @param y
	 *            the y position to set ( relative to the CanvasOverview)
	 */
	private void setLocation(OverviewComponent ov_comp, float x, float y) {
		JComponent comp = ov_comp.getComponent();
		if (comp != null) {
			ModelView view = m_overview.getFrame().getModelView();
			float view_x = view.getWidth() * x / m_overview.getWidth();
			float view_y = view.getHeight() * y / m_overview.getHeight();

			comp.setLocation((int) view_x, (int) view_y);
			view.repaint(comp);
			m_overview.repaint();
		}
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
		if (m_dragitem != null) {
			float x = e.getX() - m_dragitem.getDeltaX();
			float y = e.getY() - m_dragitem.getDeltaY();
			OverviewComponent ov_comp = m_dragitem.getComponent();
			setLocation(ov_comp, x, y);
		}
		ModelView view = m_overview.getFrame().getModelView();
		view.validateComponents();
		m_dragitem = null;
	}

	/**
	 * Class that keeps information about a component that we are dragging as
	 * part of a set of components. The delta offsets are relative to the main
	 * component of the selection.
	 */
	class DragItem {
		private OverviewComponent m_component;
		private float m_deltax;
		private float m_deltay;

		DragItem(OverviewComponent comp, float deltaX, float deltaY) {
			m_component = comp;
			m_deltax = deltaX;
			m_deltay = deltaY;
		}

		public OverviewComponent getComponent() {
			return m_component;
		}

		public float getDeltaX() {
			return m_deltax;
		}

		public float getDeltaY() {
			return m_deltay;
		}
	}
}
