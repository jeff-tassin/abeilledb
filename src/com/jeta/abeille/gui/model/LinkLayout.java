package com.jeta.abeille.gui.model;

import java.awt.Rectangle;

import com.jeta.abeille.gui.model.links.Direction;
import com.jeta.abeille.gui.model.links.LinkPath;
import com.jeta.abeille.gui.model.links.Terminal;

/**
 * This class is responsible for laying out the link widget based on the
 * location of the source and destination tables
 * 
 * @author Jeff Tassin
 */
public class LinkLayout {
	/**
	 * The widget we are laying out
	 */
	private LinkWidget m_linkwidget;

	/** Linkpath objects for performing calculations */
	private static LinkPath m_west_path = new LinkPath(Direction.WEST, Direction.WEST);
	private static LinkPath m_east_path = new LinkPath(Direction.EAST, Direction.EAST);
	private static LinkPath m_path = new LinkPath();

	public LinkLayout(LinkWidget widget) {
		m_linkwidget = widget;
	}

	private LinkPath calculateWestULinkPath(Rectangle srcrect, Rectangle destrect) {
		/** reuse existing link path so we don't have to re-create everytime */
		LinkPath path = m_west_path;

		Terminal src_term = m_linkwidget.getSourceTerminal();
		int src_y_offset = m_linkwidget.getSourceYOffset() + srcrect.y;

		Terminal dest_term = m_linkwidget.getDestinationTerminal();
		int dest_y_offset = m_linkwidget.getDestinationYOffset() + destrect.y;
		int vert_x = srcrect.x - src_term.getWidth() - LinkWidget.LINKMARGIN;
		int v2 = destrect.x - dest_term.getWidth() - LinkWidget.LINKMARGIN;
		vert_x = Math.min(vert_x, v2);

		path.setLine0(srcrect.x, src_y_offset, vert_x + LinkWidget.ARC_RADIUS, src_y_offset);
		path.setLine1(destrect.x, dest_y_offset, vert_x + LinkWidget.ARC_RADIUS, dest_y_offset);
		if (dest_y_offset <= src_y_offset) {
			path.setLine2(vert_x, dest_y_offset + LinkWidget.ARC_RADIUS, vert_x, src_y_offset - LinkWidget.ARC_RADIUS);
		} else {
			path.setLine2(vert_x, dest_y_offset - LinkWidget.ARC_RADIUS, vert_x, src_y_offset + LinkWidget.ARC_RADIUS);
		}
		return path;
	}

	private LinkPath calculateEastULinkPath(Rectangle srcrect, Rectangle destrect) {
		/** reuse existing link path so we don't have to re-create everytime */
		LinkPath path = m_east_path;

		Terminal src_term = m_linkwidget.getSourceTerminal();
		int src_y_offset = m_linkwidget.getSourceYOffset() + srcrect.y;
		int src_x_offset = srcrect.x + srcrect.width;

		Terminal dest_term = m_linkwidget.getDestinationTerminal();
		int dest_y_offset = m_linkwidget.getDestinationYOffset() + destrect.y;
		int dest_x_offset = destrect.x + destrect.width;

		assert (src_term.getWidth() >= 0);
		assert (dest_term.getWidth() >= 0);

		int vert_x = src_x_offset + src_term.getWidth() + LinkWidget.LINKMARGIN;
		int v2 = dest_x_offset + dest_term.getWidth() + LinkWidget.LINKMARGIN;
		vert_x = Math.max(vert_x, v2);

		path.setLine0(src_x_offset, src_y_offset, vert_x - LinkWidget.ARC_RADIUS, src_y_offset);
		path.setLine1(dest_x_offset, dest_y_offset, vert_x - LinkWidget.ARC_RADIUS, dest_y_offset);
		if (dest_y_offset <= src_y_offset) {
			path.setLine2(vert_x, dest_y_offset + LinkWidget.ARC_RADIUS, vert_x, src_y_offset - LinkWidget.ARC_RADIUS);
		} else {
			path.setLine2(vert_x, dest_y_offset - LinkWidget.ARC_RADIUS, vert_x, src_y_offset + LinkWidget.ARC_RADIUS);
		}
		return path;
	}

	private LinkPath calculateULinkPath(Rectangle srcrect, Rectangle destrect) {
		LinkPath west_path = calculateWestULinkPath(srcrect, destrect);
		LinkPath east_path = calculateEastULinkPath(srcrect, destrect);

		LinkPath result = west_path;

		if (west_path.getMinX() <= 0) {
			result = east_path;
		} else {

			if (west_path.intersects(srcrect) || west_path.intersects(destrect)) {
				if (east_path.intersects(srcrect) || east_path.intersects(destrect)) {
					result = getMinimumPath(west_path, east_path);
				} else {
					result = east_path;
				}
			} else if (east_path.intersects(srcrect) || east_path.intersects(destrect)) {
				result = west_path;
			} else {
				result = getMinimumPath(west_path, east_path);
			}
		}

		return result;
	}

	public LinkPath createInflectionPath(int src_x, int src_y, Direction sd, int dest_x, int dest_y, Direction dd) {
		Terminal src_term = m_linkwidget.getSourceTerminal();
		int src_link_y = m_linkwidget.getSourceYOffset() + src_y;

		Terminal dest_term = m_linkwidget.getDestinationTerminal();
		int dest_link_y = m_linkwidget.getDestinationYOffset() + dest_y;

		int delta = Math.abs(src_x - dest_x) / 2;
		int mid_point_x = src_x + sd.getSign() * delta;

		// src horz line
		m_path.setLine0(src_x, src_link_y, mid_point_x - sd.getSign() * LinkWidget.ARC_RADIUS, src_link_y);

		// draw dest horizontal line
		m_path.setLine1(dest_x, dest_link_y, mid_point_x - dd.getSign() * LinkWidget.ARC_RADIUS, dest_link_y);

		// draw the vertical line
		if (src_link_y > dest_link_y) {
			m_path.setLine2(mid_point_x, src_link_y - LinkWidget.ARC_RADIUS, mid_point_x, dest_link_y
					+ LinkWidget.ARC_RADIUS);
		} else {
			m_path.setLine2(mid_point_x, src_link_y + LinkWidget.ARC_RADIUS, mid_point_x, dest_link_y
					- LinkWidget.ARC_RADIUS);
		}
		m_path.setSourceDirection(sd);
		m_path.setDestinationDirection(dd);
		return m_path;
	}

	/**
	 * Performs the layout for the widget we are associated with
	 */
	public void doLayout() {
		Rectangle srcrect = m_linkwidget.getSourceBounds();
		Rectangle destrect = m_linkwidget.getDestinationBounds();

		int orgx = 0;
		int orgy = srcrect.y + m_linkwidget.getSourceYOffset();
		Direction orgdirection = Direction.EAST;

		int destx = 0;
		int desty = destrect.y + m_linkwidget.getDestinationYOffset();
		Direction destdirection = Direction.EAST;

		int src_x2 = srcrect.x + srcrect.width;
		int dest_x2 = destrect.x + destrect.width;

		if ((srcrect.x >= destrect.x && srcrect.x <= dest_x2) || (destrect.x >= srcrect.x && destrect.x <= src_x2)) {
			LinkPath path = calculateULinkPath(srcrect, destrect);
			m_linkwidget.setLinkPath(path);
		} else if ((Math.abs(destrect.x - (src_x2)) < LinkWidget.GAP)
				|| (Math.abs(srcrect.x - (dest_x2)) < LinkWidget.GAP)
				|| (Math.abs(srcrect.x - destrect.x) < LinkWidget.GAP) || (Math.abs(dest_x2 - src_x2) < LinkWidget.GAP)) {

			LinkPath path = calculateULinkPath(srcrect, destrect);
			m_linkwidget.setLinkPath(path);
		} else if ((srcrect.x + srcrect.width) < destrect.x) {
			orgx = srcrect.x + srcrect.width;
			orgdirection = Direction.EAST;

			destx = destrect.x;
			destdirection = Direction.WEST;
			LinkPath path = createInflectionPath(orgx, srcrect.y, orgdirection, destx, destrect.y, destdirection);
			m_linkwidget.setLinkPath(path);
		} else {
			orgx = srcrect.x;
			orgdirection = Direction.WEST;

			destx = destrect.x + destrect.width;
			destdirection = Direction.EAST;
			LinkPath path = createInflectionPath(orgx, srcrect.y, orgdirection, destx, destrect.y, destdirection);
			m_linkwidget.setLinkPath(path);
		}
		m_linkwidget.repaint();
	}

	public LinkPath getMinimumPath(LinkPath path1, LinkPath path2) {
		int d1 = path1.getDistance();
		int d2 = path2.getDistance();

		if (d1 <= d2)
			return path1;
		else
			return path2;
	}
}
