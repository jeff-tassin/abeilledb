package com.jeta.abeille.gui.model;

import java.awt.Rectangle;

import com.jeta.abeille.gui.model.links.Terminal;

/**
 * This is a special case of LinkWidget that supports dragging with the mouse
 * 
 * @author Jeff Tassin
 */
public class DragLink extends LinkWidget {
	int m_dragX; // the x location of the mouse during a drag operation
	int m_dragY; // the y location of the mouse during a drag operation
	boolean m_bdragging = false;

	/**
	 * set to true if we are dragging the source terminal. false if we dragging
	 * the destination terminal
	 */
	private boolean m_issource = true;

	/**
	 * ctor
	 */
	public DragLink(ModelView canvas) {
		super(canvas, null, null, null);
	}

	/**
	 * ctor Initializes this link based on the attributes of the passed in link
	 */
	public DragLink(ModelView canvas, LinkWidget lw) {
		super(canvas, lw);
	}

	public void doDrag(int x, int y) {
		Terminal term = getDragTerminal();
		if (term != null) {
			/**
			 * increase the bounds of the terminal that we are dragging. The
			 * canvas is not repainted 'perfectly' if we don't do this
			 */
			Rectangle rect = term.getBounds();
			int rx = rect.x - 1;
			int rwidth = rect.width + 2;
			getContainer().repaint(rx, rect.y, rwidth, rect.height);
		}
		m_dragX = x;
		m_dragY = y;
		recalc();
	}

	/**
	 * Override of LinkWidget implementation
	 */
	public Rectangle getDestinationBounds() {
		if (isDragSource())
			return super.getDestinationBounds();
		else
			return new Rectangle(m_dragX, m_dragY, 0, 0);
	}

	/**
	 * Override of LinkWidget
	 */
	public int getDestinationYOffset() {
		if (isDragSource())
			return super.getDestinationYOffset();
		else
			return 0;
	}

	public Terminal getDragTerminal() {
		if (isDragSource())
			return getSourceTerminal();
		else
			return getDestinationTerminal();
	}

	/**
	 * Override of LinkWidget implementation
	 */
	public Rectangle getSourceBounds() {
		if (isDragSource())
			return new Rectangle(m_dragX, m_dragY, 0, 0);
		else
			return super.getSourceBounds();
	}

	/**
	 * Override of LinkWidget
	 */
	public int getSourceYOffset() {
		if (isDragSource())
			return 0;
		else
			return super.getSourceYOffset();
	}

	public boolean isDragging() {
		return m_bdragging;
	}

	public boolean isDragSource() {
		return m_issource;
	}

	/**
	 * Starts the drag operation for this link
	 * 
	 * @param dragTab
	 *            the tab on the link that we are dragging This is either
	 *            SOURCE_TAB or DESTINATION_TAB
	 */
	public void startDrag(boolean issource, int x, int y) {
		m_issource = issource;
		m_dragX = x;
		m_dragY = y;
		m_bdragging = true;
		recalc();
	}

	public void stopDrag(int x, int y) {
		m_dragX = x;
		m_dragY = y;
		m_bdragging = false;
		recalc();
	}

}
