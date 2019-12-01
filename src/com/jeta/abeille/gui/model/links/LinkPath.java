package com.jeta.abeille.gui.model.links;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import com.jeta.abeille.gui.model.LinkWidget;

/**
 * A link path defines the 3 lines that make up a link.
 * 
 * @author Jeff Tassin
 */
public class LinkPath {
	/** the 3 lines that make up the link */
	private Line[] m_lines = new Line[3];
	private Rectangle[] m_boundingrects = new Rectangle[3];

	/** the directions for the source and destination terminals */
	private Direction m_src_direction = Direction.WEST;
	private Direction m_dest_direction = Direction.WEST;

	/** flag that indicates if this path has been modified. */
	private boolean m_modified = true;

	/**
	 * used for computing the intersection of table widget rectangles with this
	 * path
	 */
	private java.awt.geom.Rectangle2D.Float m_rect = new java.awt.geom.Rectangle2D.Float();

	{
		m_lines[0] = new Line();
		m_lines[1] = new Line();
		m_lines[2] = new Line();

		m_boundingrects[0] = new Rectangle();
		m_boundingrects[1] = new Rectangle();
		m_boundingrects[2] = new Rectangle();
	}

	/**
	 * ctor
	 */
	public LinkPath() {

	}

	/**
	 * ctor
	 */
	public LinkPath(Direction source_d, Direction dest_d) {
		m_src_direction = source_d;
		m_dest_direction = dest_d;
	}

	/**
	 * @return the bounding rectangles for this path
	 */
	public Rectangle[] getBoundingRects() {
		if (m_modified) {
			for (int index = 0; index < m_boundingrects.length; index++) {
				int x = Math.min(m_lines[index].p1.x, m_lines[index].p2.x);
				int y = Math.min(m_lines[index].p1.y, m_lines[index].p2.y);
				int width = Math.abs(m_lines[index].p1.x - m_lines[index].p2.x);
				int height = Math.abs(m_lines[index].p1.y - m_lines[index].p2.y);
				if (index == 2) {
					m_boundingrects[index].setBounds(x - LinkWidget.ARC_RADIUS - 2, y - LinkWidget.ARC_RADIUS - 2,
							width + 2 * LinkWidget.ARC_RADIUS + 2, height + LinkWidget.ARC_RADIUS * 2 + 2);
				} else {
					m_boundingrects[index].setBounds(x, y - 2, width, height + 4);
				}
			}
			setModified(false);
		}
		return m_boundingrects;
	}

	/**
	 * @return the linear distance of this path. (i.e. the sum length of all
	 *         lines in the path )
	 */
	public int getDistance() {
		int d1 = Math.abs(m_lines[0].p1.x - m_lines[0].p2.x);
		d1 += Math.abs(m_lines[1].p1.x - m_lines[1].p2.x);
		d1 += Math.abs(m_lines[2].p1.y - m_lines[2].p2.y);
		return d1;
	}

	/**
	 * @return the minimum x value that the path traverses
	 */
	public int getMinX() {
		int x1 = Math.min(m_lines[0].p1.x, m_lines[0].p2.x);
		int x2 = Math.min(m_lines[1].p1.x, m_lines[1].p2.x);
		int x3 = Math.min(m_lines[2].p1.x, m_lines[2].p2.x);

		x1 = Math.min(x1, x2);
		return Math.min(x1, x3);
	}

	public int getSourceTerminalX() {
		if (getSourceDirection().equals(Direction.WEST)) {
			return Math.max(m_lines[0].p1.x, m_lines[0].p2.x);
		} else {
			return Math.min(m_lines[0].p1.x, m_lines[0].p2.x);
		}
	}

	public int getSourceTerminalY() {
		return m_lines[0].p1.y;
	}

	public Direction getSourceDirection() {
		return m_src_direction;
	}

	public int getDestinationTerminalX() {
		if (getDestinationDirection().equals(Direction.WEST)) {
			return Math.max(m_lines[1].p1.x, m_lines[1].p2.x);
		} else {
			return Math.min(m_lines[1].p1.x, m_lines[1].p2.x);
		}
	}

	public int getDestinationTerminalY() {
		return m_lines[1].p1.y;
	}

	public Direction getDestinationDirection() {
		return m_dest_direction;
	}

	/**
	 * @return true if any of the lines in this path intersect the given
	 *         rectangle
	 */
	public boolean intersects(Rectangle rect) {
		m_rect.setRect((float) rect.x + 1, (float) rect.y, (float) rect.width - 2, (float) rect.height);
		for (int index = 0; index < m_lines.length; index++) {
			if (m_rect.intersectsLine((double) m_lines[index].p1.x, (double) m_lines[index].p1.y,
					(double) m_lines[index].p2.x, (double) m_lines[index].p2.y))
				return true;
		}
		return false;
	}

	/**
	 * Paints this path on the given graphics context
	 */
	public void paintComponent(Graphics g) {
		if (Math.abs(m_lines[0].p2.y - m_lines[1].p2.y) <= LinkWidget.ARC_RADIUS * 2) {
			g.drawLine(m_lines[0].p1.x, m_lines[0].p1.y, m_lines[0].p2.x, m_lines[0].p2.y);
			g.drawLine(m_lines[1].p1.x, m_lines[1].p1.y, m_lines[1].p2.x, m_lines[1].p2.y);
			g.drawLine(m_lines[0].p2.x, m_lines[0].p2.y, m_lines[1].p2.x, m_lines[1].p2.y);
		} else {
			for (int index = 0; index < m_lines.length; index++) {
				g.drawLine(m_lines[index].p1.x, m_lines[index].p1.y, m_lines[index].p2.x, m_lines[index].p2.y);
			}
			paintArc(g, m_lines[0].p2, m_lines[2].p1, m_lines[2].p1.x);
			paintArc(g, m_lines[1].p2, m_lines[2].p2, m_lines[2].p1.x);
		}
	}

	private void paintArc(Graphics g, Point src_pt, Point dest_pt, int vert_x) {

		if (src_pt.y < dest_pt.y) {
			if (src_pt.x > vert_x) {
				g.drawArc(vert_x, src_pt.y, LinkWidget.ARC_RADIUS * 2, LinkWidget.ARC_RADIUS * 2, 90, 90);
			} else {
				g.drawArc(vert_x - LinkWidget.ARC_RADIUS * 2, src_pt.y, LinkWidget.ARC_RADIUS * 2,
						LinkWidget.ARC_RADIUS * 2, 90, -90);
			}
		} else {
			if (src_pt.x > vert_x) {
				g.drawArc(vert_x, src_pt.y - LinkWidget.ARC_RADIUS * 2, LinkWidget.ARC_RADIUS * 2,
						LinkWidget.ARC_RADIUS * 2, 180, 90);
			} else {
				g.drawArc(vert_x - LinkWidget.ARC_RADIUS * 2, src_pt.y - LinkWidget.ARC_RADIUS * 2,
						LinkWidget.ARC_RADIUS * 2, LinkWidget.ARC_RADIUS * 2, 0, -90);
			}
		}
	}

	/**
	 * Print this path to the console
	 */
	public void print(String val) {
		System.out.println("----------------------- Link Path " + val + "  ------------------");
		for (int index = 0; index < m_lines.length; index++) {
			System.out.println("line" + index + ":  x1=" + m_lines[index].p1.x + "  y1=" + m_lines[index].p1.y
					+ "  x2 = " + m_lines[index].p2.x + "  y2 = " + m_lines[index].p2.y);
		}
		System.out.println("src.direction: " + m_src_direction.toString());
		System.out.println("dest.direction: " + m_dest_direction.toString());
	}

	/**
	 * This is the source line
	 */
	public void setLine0(int x1, int y1, int x2, int y2) {
		m_lines[0].p1.x = x1;
		m_lines[0].p1.y = y1;
		m_lines[0].p2.x = x2;
		m_lines[0].p2.y = y2;
		setModified(true);
	}

	/**
	 * This is the dest line
	 */
	public void setLine1(int x1, int y1, int x2, int y2) {
		m_lines[1].p1.x = x1;
		m_lines[1].p1.y = y1;
		m_lines[1].p2.x = x2;
		m_lines[1].p2.y = y2;
		setModified(true);

	}

	public void setLine2(int x1, int y1, int x2, int y2) {
		m_lines[2].p1.x = x1;
		m_lines[2].p1.y = y1;
		m_lines[2].p2.x = x2;
		m_lines[2].p2.y = y2;
		setModified(true);

	}

	/**
	 * Sets this path's attributes to the path passed in the method
	 */
	public void setLinkPath(LinkPath path) {
		for (int index = 0; index < m_lines.length; index++) {
			m_lines[index].p1.x = path.m_lines[index].p1.x;
			m_lines[index].p1.y = path.m_lines[index].p1.y;
			m_lines[index].p2.x = path.m_lines[index].p2.x;
			m_lines[index].p2.y = path.m_lines[index].p2.y;
		}
		m_src_direction = path.m_src_direction;
		m_dest_direction = path.m_dest_direction;
		setModified(true);

	}

	public void setSourceDirection(Direction sd) {
		m_src_direction = sd;
		setModified(true);

	}

	public void setDestinationDirection(Direction dd) {
		m_dest_direction = dd;
		setModified(true);

	}

	public void setModified(boolean bmod) {
		m_modified = bmod;
	}

	static class Line {
		Point p1 = new Point();
		Point p2 = new Point();
	}
}
