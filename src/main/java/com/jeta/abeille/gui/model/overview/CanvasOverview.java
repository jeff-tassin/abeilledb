package com.jeta.abeille.gui.model.overview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.lang.ref.WeakReference;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JViewport;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewFrame;
import com.jeta.abeille.gui.model.TableWidget;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class displays an entire ModelView in a small window
 * 
 * @author Jeff Tassin
 */
public class CanvasOverview extends JComponent {
	/** the frame that contains the view we are displaying */
	private WeakReference m_frameref;

	private ArrayList m_ov_comps = new ArrayList();

	private Color m_cross_color = Color.black;

	public CanvasOverview(ModelViewFrame frame) {
		m_frameref = new WeakReference(frame);
		setOpaque(true);

		PositionController controller = new PositionController(this);
		addMouseListener(controller);
		addMouseMotionListener(controller);
		m_cross_color = javax.swing.UIManager.getColor("control").darker();
	}

	public ModelViewFrame getFrame() {
		return (ModelViewFrame) m_frameref.get();
	}

	/**
	 * @returns the component at the given CanvasOView coordinates
	 */
	public OverviewComponent getComponent(int x, int y) {
		for (int index = 0; index < m_ov_comps.size(); index++) {
			OverviewComponent ov_comp = (OverviewComponent) m_ov_comps.get(index);
			if (x >= ov_comp.getX() && x <= (ov_comp.getX() + ov_comp.getWidth())) {
				if (y >= ov_comp.getY() && y <= (ov_comp.getY() + ov_comp.getHeight())) {
					return ov_comp;
				}
			}
		}
		return null;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(javax.swing.UIManager.getColor("control"));
		g.fillRect(0, 0, (int) getWidth(), (int) getHeight());

		sync(g);

		g.setColor(m_cross_color);
		g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
		g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
	}

	public void sync(Graphics g) {
		ModelViewFrame frame = getFrame();
		if (frame != null) {
			Dimension canvas_size = frame.getCanvasSize();
			ModelView view = frame.getModelView();
			JViewport viewport = (JViewport) view.getParent();

			Component[] comps = view.getComponents();

			double scale_x = ((double) getWidth()) / canvas_size.getWidth();
			double scale_y = ((double) getHeight()) / canvas_size.getHeight();

			int o_index = 0;
			for (int index = 0; index < comps.length; index++) {
				Component comp = comps[index];
				if (comp instanceof TableWidget) {
					TableWidget w = (TableWidget) comp;
					TSUtils.ensureSize(m_ov_comps, o_index + 1);
					OverviewComponent ov_comp = (OverviewComponent) m_ov_comps.get(o_index);
					if (ov_comp == null) {
						ov_comp = new OverviewComponent(w);
						m_ov_comps.set(o_index, ov_comp);
					} else {
						if (ov_comp.getComponent() != w) {
							ov_comp.setComponent(w);
						}
					}
					ov_comp.setScale(scale_x, scale_y);

					if (g != null) {
						ov_comp.paintComponent(g);
					}
					o_index++;
				}

			}

			if (g != null) {
				Point pt = viewport.getViewPosition();
				Dimension sz = viewport.getExtentSize();
				pt.x = (int) (pt.x * scale_x);
				pt.y = (int) (pt.y * scale_y);
				sz.width = (int) (sz.width * scale_x);
				sz.height = (int) (sz.height * scale_y);

				g.setColor(m_cross_color);
				g.drawRect(pt.x, pt.y, sz.width, sz.height);
			}

			/**
			 * this can happen if the user deleted a table widget since the last
			 * time we repainted
			 */
			if (m_ov_comps.size() > o_index) {
				TSUtils.trim(m_ov_comps, o_index);
				// System.out.println(
				// "CanvasOverview.paint  triming ov components   comps_size:" +
				// m_ov_comps.size() );
			}
		}
	}

	public void updateUI() {
		super.updateUI();
		m_cross_color = javax.swing.UIManager.getColor("control").darker();
	}

}
