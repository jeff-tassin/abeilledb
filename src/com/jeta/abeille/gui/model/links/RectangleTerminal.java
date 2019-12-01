package com.jeta.abeille.gui.model.links;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class RectangleTerminal extends Terminal {
	private static final int HEIGHT = 4;
	private static final int WIDTH = 16;

	public RectangleTerminal() {
		setBounds(0, 0, WIDTH, HEIGHT);
	}

	public void draw(Graphics g) {
		int height = getHeight();
		int width = getWidth();

		Rectangle rect = getBounds();
		Point pt = getTerminalPoint();
		Direction d = getDirection();
		if (d.getSign() > 0) {
			g.fillRect(pt.x, pt.y - height / 2, width / 2, height);
			g.drawLine(pt.x, pt.y, pt.x + width, pt.y);
		} else {
			g.fillRect(pt.x - width / 2, pt.y - height / 2, width / 2, height);
			g.drawLine(pt.x - width, pt.y, pt.x, pt.y);
		}
	}

	/**
	 * Sets the terminal point for this object
	 */
	public void setTerminalPoint(int x, int y, Direction d) {
		super.setTerminalPoint(x, y, d);
		int width = getWidth();
		int height = getHeight();
		if (d.equals(Direction.EAST)) {
			setBounds(x, y - height / 2, width, height);
		} else {
			setBounds(x - width, y - height / 2, width, height);
		}
	}

	/**
	 * Sets the maximum height for this terminal. This is used mainly when the
	 * user changes the font size in the model view. We would like the terminal
	 * sizes to be somewhat proportional to the font size.
	 */
	public void setMaximumHeight(int height) {
		super.setMaximumHeight(height);

		Point pt = getTerminalPoint();
		height = height * 3 / 10;
		if (height < 2)
			height = 2;

		int width = height * 3;
		width = width - width % 2;
		setBounds(pt.x, pt.y, width, height);
	}

}
