package com.jeta.abeille.gui.model.overview;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class OverviewComponent {
	private JComponent m_widget;
	private float m_x;
	private float m_y;
	private float m_width;
	private float m_height;

	public OverviewComponent(JComponent w) {
		m_widget = w;
	}

	public JComponent getComponent() {
		return m_widget;
	}

	public float getX() {
		return m_x;
	}

	public float getY() {
		return m_y;
	}

	public float getWidth() {
		return m_width;
	}

	public float getHeight() {
		return m_height;
	}

	public void setScale(double scale_x, double scale_y) {
		m_width = (float) (scale_x * m_widget.getWidth());
		m_height = (float) (scale_y * m_widget.getHeight());
		m_x = (float) (scale_x * m_widget.getX());
		m_y = (float) (scale_y * m_widget.getY());
	}

	/**
	 * Paints the widget representation onto the graphics context.
	 * 
	 * @param g
	 *            the graphics context
	 * @param scale_x
	 *            the scale factor in the x direction
	 * @param scale_y
	 *            the scale factor in the y direction
	 */
	public void paintComponent(Graphics g) {

		g.setColor(Color.white);
		g.fillRect((int) m_x, (int) m_y, (int) m_width, (int) m_height);
		g.setColor(Color.black);
		g.drawRect((int) m_x, (int) m_y, (int) m_width, (int) m_height);
	}

	public void setComponent(JComponent comp) {
		m_widget = comp;
	}

}
