package com.jeta.abeille.gui.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.gui.model.links.BranchTerminal;
import com.jeta.abeille.gui.model.links.Direction;
import com.jeta.abeille.gui.model.links.LinkPath;
import com.jeta.abeille.gui.model.links.RectangleTerminal;
import com.jeta.abeille.gui.model.links.Terminal;

/**
 * A widget that draws a line between two tables representing a foreign key
 * relationship.
 * 
 * @author Jeff Tassin
 */
public class LinkWidget {
	private TableWidget m_srctable;
	private TableWidget m_desttable;
	private Link m_link;

	/**
	 * Flag that indicates if this widget has been selected with the mouse. Once
	 * selected, a widget will change color to the selection color
	 */
	private boolean m_selected;

	/**
	 * cache of points defining the link and bounds. we store this way for
	 * performance
	 */
	private Point[][] m_points = null;
	private Rectangle[] m_boundingrects;

	/**
	 * The small rectangular tab drawn at the link source
	 */
	private Terminal m_srcTab;

	/**
	 * The small arrow tab drawn at the link destination
	 */
	private Terminal m_destTab;

	/**
	 * This object is responsible for calculating the position and orientation
	 * of this link based on the source and destination locations
	 */
	private LinkLayout m_layout;

	/**
	 * Defines the actual points that make up the link.
	 */
	private LinkPath m_path = new LinkPath();

	/**
	 * The parent container that this link is rendered on
	 */
	private Container m_container;

	/** flag that indicates if this widget is visible or not */
	private boolean m_visible = true;

	private static Point m_dest_arc_pt = new Point();
	private static Point m_src_arc_pt = new Point();

	public static final int ARC_RADIUS = 2;
	public static final int LINKMARGIN = 10;
	public static final int GAP = 50;

	/**
	 * The tolerance of space for selecting a line with the mouse
	 */
	static final int LINE_SELECTION_TOLERANCE = 6;

	/**
	 * Strokes that define the line pattern/thickness. We show different lines
	 * for a link depending on if the link is a foreign key link, localally user
	 * defined, or globally user defined.
	 */
	private static BasicStroke m_basicstroke = new BasicStroke(0.3f);
	private static BasicStroke m_globalstroke;

	private static final Color SELECTED_COLOR = Color.blue;
	private static final Color USER_DEFINED_COLOR = new Color(128, 0, 0);

	static {
		float[] gdash = { 2.1f };
		m_globalstroke = new BasicStroke(0.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, gdash, 0.0f);
	}

	/**
	 * ctor
	 */
	public LinkWidget(Container owner, TableWidget srcTable, TableWidget destTable, Link link) {
		m_container = owner;
		m_srctable = srcTable;
		m_desttable = destTable;
		m_link = link;
		m_layout = new LinkLayout(this);

		m_srcTab = new RectangleTerminal();
		m_destTab = new BranchTerminal();
	}

	/**
	 * ctor
	 */
	public LinkWidget(Container owner, LinkWidget lw) {
		m_container = owner;
		m_srctable = lw.m_srctable;
		m_desttable = lw.m_desttable;
		m_link = lw.m_link;
		m_layout = new LinkLayout(this);

		m_srcTab = new RectangleTerminal();
		m_destTab = new BranchTerminal();
	}

	/**
	 * @return true if this widget contains the given point. Note that this
	 *         widget has an irregular boundary, so we can't just use a
	 *         rectangular bounding area. The main reason is that there can
	 *         easily be multiple widgets within this widget's 'rectangular'
	 *         bounds, and it would be difficult to select other widgets in this
	 *         region with the mouse.
	 */
	public boolean contains(Point pt) {
		boolean bresult = false;
		// check intersection of tabs
		Rectangle rect = m_srcTab.getBounds();
		bresult = rect.contains(pt);
		if (!bresult) {
			rect = m_destTab.getBounds();
			bresult = rect.contains(pt);
		}
		return bresult;
	}

	/**
	 * For debugging purposes
	 */
	private void debugDraw(Graphics g) {
		Rectangle[] rects = getBoundingRects();
		for (int index = 0; index < rects.length; index++) {
			Rectangle rect = rects[index];
			if (index <= 1) {
				g.setColor(Color.blue);
			} else {
				g.setColor(Color.red);
			}
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
	}

	public Rectangle[] getBoundingRects() {
		if (m_boundingrects == null) {
			m_boundingrects = new Rectangle[5];
			for (int index = 0; index < m_boundingrects.length; index++)
				m_boundingrects[index] = new Rectangle();
		}

		m_boundingrects[0] = m_srcTab.getBounds();
		m_boundingrects[1] = m_destTab.getBounds();
		Rectangle[] rects = m_path.getBoundingRects();
		m_boundingrects[2] = rects[0];
		m_boundingrects[3] = rects[1];
		m_boundingrects[4] = rects[2];
		return m_boundingrects;
	}

	/**
	 * @return the start point of the connector line
	 */
	public Point getConnectorEndPoint() {
		return m_destTab.getTerminalPoint();
	}

	/**
	 * @return the start point of the connector line
	 */
	public Point getConnectorStartPoint() {
		return m_srcTab.getTerminalPoint();
	}

	/**
	 * @return the container for this widget
	 */
	public Container getContainer() {
		return m_container;
	}

	/**
	 * @return the bounds defined by the destination for this link
	 */
	public Rectangle getDestinationBounds() {
		return m_desttable.getBounds();
	}

	/**
	 * @return the column in the destination table that we are linked to
	 */
	public String getDestinationColumn() {
		return m_link.getDestinationColumn();
	}

	/**
	 * @return the destination table widget for this link widget
	 */
	public TableWidget getDestinationTable() {
		return m_desttable;
	}

	/**
	 * @return the table id for the source table we are anchored to. Null is
	 *         returned if there is no source table
	 */
	public TableId getDestinationTableId() {
		TableWidget w = getDestinationTable();
		if (w != null)
			return w.getTableMetaData().getTableId();
		else
			return null;
	}

	public Terminal getDestinationTerminal() {
		return m_destTab;
	}

	/**
	 * @return the y offset from the destination rectangle for the end of the
	 *         link
	 */
	public int getDestinationYOffset() {
		return m_desttable.getFieldY(getDestinationColumn());
	}

	/**
	 * @return the underlying database link (can be null)
	 */
	public Link getLink() {
		return m_link;
	}

	/**
	 * @return the object that defines the points that make up the link
	 */
	public LinkPath getLinkPath() {
		return m_path;
	}

	/**
	 * @return the rectangle that defines the source boundary for this link
	 */
	public Rectangle getSourceBounds() {
		return m_srctable.getBounds();
	}

	/**
	 * @return the column from the source table for this link
	 */
	public String getSourceColumn() {
		return m_link.getSourceColumn();
	}

	/**
	 * @return the source table widget for this link widget
	 */
	public TableWidget getSourceTable() {
		return m_srctable;
	}

	/**
	 * @return the table id for the source table we are anchored to. Null is
	 *         returned if there is no source table
	 */
	public TableId getSourceTableId() {
		TableWidget w = getSourceTable();
		if (w != null)
			return w.getTableMetaData().getTableId();
		else
			return null;
	}

	public Terminal getSourceTerminal() {
		return m_srcTab;
	}

	/**
	 * @return the y offset from the destination rectangle for the end of the
	 *         link
	 */
	public int getSourceYOffset() {
		return m_srctable.getFieldY(getSourceColumn());
	}

	/**
	 * @return the stroke for this link
	 */
	private Stroke getStroke() {
		Stroke result = m_basicstroke;
		if (m_link.isUserDefined()) {
			result = m_globalstroke;
		}
		return result;
	}

	/**
	 * Invalidates the region occupied by this link
	 */
	public void repaint() {
		if (m_container != null) {
			Rectangle[] rects = getBoundingRects();
			for (int index = 0; index < rects.length; index++) {
				Rectangle rect = rects[index];
				m_container.repaint(rect.x, rect.y, rect.width, rect.height);
			}

			Rectangle rect = m_srcTab.getBounds();
			m_container.repaint(rect.x, rect.y, rect.width, rect.height);
			rect = m_destTab.getBounds();
			m_container.repaint(rect.x, rect.y, rect.width, rect.height);
		}
	}

	/**
	 * @return true if this widget has been selected with the mouse
	 */
	public boolean isSelected() {
		return m_selected;
	}

	/**
	 * @return true if the given terminal is the source terminal
	 */
	public boolean isSource(Terminal terminal) {
		return (terminal == m_srcTab);
	}

	/**
	 * Returns the flag that indicates if this link is user defined globally or
	 * not A user defined link is one that is created by the user and not
	 * defined in the database. For example, a foreign key is a database defined
	 * link and NOT a user defined. A link that the user drags and creates with
	 * the gui in the formbuilder and query builder is user defined and local to
	 * that view. A link that the user drags in the model view is global and
	 * available to all views.
	 */
	public boolean isUserDefined() {
		return m_link.isUserDefined();
	}

	/**
	 * @return the flag indicating if this widget is visible on the screen
	 */
	public boolean isVisible() {
		return m_visible;
	}

	/**
	 * Renders this component on the graphics object
	 */
	public void paintComponent(Graphics g, JComponent parentCanvas) {
		if (isVisible()) {

			boolean bclipped = false;
			Rectangle clip_rect = g.getClipBounds();
			Rectangle[] rects = getBoundingRects();
			for (int index = 0; index < rects.length; index++) {
				Rectangle rect = rects[index];
				if (clip_rect.intersects(rect)) {
					bclipped = true;
					break;
				}
			}

			if (bclipped) {
				Graphics2D g2 = (Graphics2D) g;

				Stroke stroke = getStroke();
				Stroke old_stroke = g2.getStroke();
				g2.setStroke(stroke);
				g.setColor(Color.black);
				Direction sd = m_srcTab.getDirection();
				Direction dd = m_destTab.getDirection();

				m_srcTab.draw(g);
				m_destTab.draw(g);
				m_path.paintComponent(g);
				g2.setStroke(old_stroke);
			}
		}
	}

	/**
	 * Print this component's information to std out. Used for debugging
	 */
	public void print() {
		m_link.print();
	}

	/**
	 * Recalculate the position and orientation of the link as well as the
	 * bounding rectangle
	 */
	public void recalc() {
		assert (m_layout != null);
		if (m_layout != null) {
			m_layout.doLayout();
			repaint();
		}
	}

	/**
	 * Sets the container for this widget
	 */
	public void setContainer(Container container) {
		m_container = container;
		recalc();
	}

	/**
	 * Sets the destination point of the link widget.
	 * 
	 * @param x
	 *            the x point of the widget's destination
	 * @param y
	 *            the y point of the widget's destination
	 * @param d
	 *            the direction for the destination tab (east or west)
	 */
	public void setDestination(int x, int y, Direction d) {
		m_destTab.setTerminalPoint(x, y, d);
	}

	public void setLinkPath(LinkPath path) {
		m_path.setLinkPath(path);
		m_srcTab.setTerminalPoint(path.getSourceTerminalX(), path.getSourceTerminalY(), path.getSourceDirection());

		m_destTab.setTerminalPoint(path.getDestinationTerminalX(), path.getDestinationTerminalY(),
				path.getDestinationDirection());
	}

	/**
	 * Sets this widget to selected.
	 */
	public void setSelected(boolean bSelected) {
		m_selected = bSelected;
		repaint();
	}

	/**
	 * Sets the origin point of the link widget.
	 * 
	 * @param x
	 *            the x point of the widget's origin
	 * @param y
	 *            the y point of the widget's origin
	 * @param d
	 *            the direction for the origin tab (east or west)
	 */
	public void setSource(int x, int y, Direction d) {
		m_srcTab.setTerminalPoint(x, y, d);
	}

	/**
	 * Sets the source table widget for this linke
	 */
	public void setSourceTable(TableWidget sourceWidget) {
		m_srctable = sourceWidget;
	}

	public void setTerminalHeight(int height) {
		m_srcTab.setMaximumHeight(height);
		m_destTab.setMaximumHeight(height);
	}

	/**
	 * Sets the flag indicating if this widget is visible on the screen
	 */
	public void setVisible(boolean visible) {
		m_visible = visible;
		repaint();
	}

	/**
	 * @return the type of the tab that is nearest to the given point (either
	 *         SOURCE or DESTINATION are returned)
	 */
	Terminal tabFromPoint(Point pt) {
		Rectangle rect = m_srcTab.getBounds();
		if (rect.contains(pt))
			return m_srcTab;

		rect = m_destTab.getBounds();
		if (rect.contains(pt))
			return m_destTab;

		return null;
	}

}
