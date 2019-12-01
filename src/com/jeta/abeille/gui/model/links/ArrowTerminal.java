package com.jeta.abeille.gui.model.links;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

/**
 * The small arrow tab drawn at the link destination This little widget is
 * composed of an arrow and a small tab at the base of the arrow. --------|>
 * (tab) (arrow)
 */
public class ArrowTerminal extends Terminal {
	private Polygon m_arrowhead = new Polygon();
	private static final int HEIGHT = 6;
	private static final int WIDTH = 8;

	public ArrowTerminal() {
		setBounds(0, 0, WIDTH, HEIGHT);
	}

	/**
	 * Renders the arrow on the graphics context
	 */
	public void draw(Graphics g) {
		g.fillPolygon(m_arrowhead);
	}

	/**
	 * Sets the terminal point for this object
	 */
	public void setTerminalPoint(int x, int y, Direction d) {
		super.setTerminalPoint(x, y, d);
		resetTerminal(x, y, getWidth(), getHeight(), d);
	}

	private void resetTerminal(int x, int y, int width, int height, Direction d) {
		if (d.equals(Direction.EAST)) {
			setBounds(x, y - height / 2, width, height);
		} else {
			setBounds(x - width, y - height / 2, width, height);
		}
		m_arrowhead.reset();
		m_arrowhead.addPoint(x, y);
		m_arrowhead.addPoint(x + d.getSign() * width, y - height / 2);
		m_arrowhead.addPoint(x + d.getSign() * width, y + height / 2);
		m_arrowhead.addPoint(x, y);
	}

	/**
	 * Sets the maximum height for this terminal. This is used mainly when the
	 * user changes the font size in the model view. We would like the terminal
	 * sizes to be somewhat proportional to the font size.
	 */
	public void setMaximumHeight(int height) {
		super.setMaximumHeight(height);
		Point pt = getTerminalPoint();
		height = height * 5 / 10;
		if (height < 2)
			height = 2;

		int width = height * 3 / 2;
		width = width - width % 2;

		resetTerminal(pt.x, pt.y, width, height, getDirection());
	}
}
