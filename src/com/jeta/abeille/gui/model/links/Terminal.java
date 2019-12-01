package com.jeta.abeille.gui.model.links;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * The end view/point of a link.
 * 
 * @author Jeff Tassin
 */
public abstract class Terminal {
	/** this is the point that connects to the table widget */
	private Point m_terminal_pt = new Point();

	/** the direction of the terminal (EAST or WEST) */
	private Direction m_direction = Direction.EAST;

	/**
	 * The point at which the link connects to this terminal
	 */
	private Point m_linkpoint = new Point();

	private Rectangle m_bounds = new Rectangle();

	/**
	 * The maximum height for this terminal. This is used mainly when the user
	 * changes the font size in the model view. We would like the terminal sizes
	 * to be somewhat proportional to the font size.
	 */
	private int m_maxheight = 6;

	/**
	 * @return the point at which the link connects to this tab
	 */
	public Point getLinkPoint() {
		m_linkpoint.x = m_terminal_pt.x + m_direction.getSign() * getWidth();
		m_linkpoint.y = m_terminal_pt.y;
		return m_linkpoint;
	}

	/**
	 * Draws the terminal on the given graphics context
	 * 
	 * @param g
	 *            the graphics context to draw the link on
	 * @param fm
	 *            the font metrics of the table widget. We use this to calculate
	 *            the terminal height when printing. The terminal sizes seem out
	 *            of scale if we don't do this.
	 */
	public abstract void draw(Graphics g);

	public Rectangle getBounds() {
		return m_bounds;
	}

	/**
	 * @return the height of this terminal
	 */
	public int getHeight() {
		return m_bounds.height;
	}

	/**
	 * @return the maximum height for this terminal. This is used mainly when
	 *         the user changes the font size in the model view. We would like
	 *         the terminal sizes to be somewhat proportional to the font size.
	 */
	public int getMaximumHeight() {
		return m_maxheight;
	}

	/**
	 * @return the point that is connected to the table widget
	 */
	public Point getTerminalPoint() {
		return m_terminal_pt;
	}

	/**
	 * @return the width of this terminal
	 */
	public int getWidth() {
		return m_bounds.width;
	}

	/**
	 * @return the direction of this terminal. The direction is relative to this
	 *         Terminal's terminal point
	 */
	public Direction getDirection() {
		return m_direction;
	}

	/**
	 * Sets the bounds for this terminal
	 */
	public void setBounds(int x, int y, int width, int height) {
		assert (width > 0);
		assert (height > 0);
		m_bounds.setBounds(x, y, width, height);
	}

	/**
	 * Sets the maximum height for this terminal. This is used mainly when the
	 * user changes the font size in the model view. We would like the terminal
	 * sizes to be somewhat proportional to the font size.
	 */
	public void setMaximumHeight(int height) {
		m_maxheight = height;
	}

	/**
	 * Sets the terminal point for this object
	 */
	public void setTerminalPoint(int x, int y, Direction d) {
		m_terminal_pt.x = x;
		m_terminal_pt.y = y;
		m_direction = d;
	}
}
