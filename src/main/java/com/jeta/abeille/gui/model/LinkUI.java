package com.jeta.abeille.gui.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.jeta.abeille.gui.model.links.Terminal;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.i18n.I18N;

public class LinkUI implements MouseListener, MouseMotionListener {
	private static final int INITIAL_STATE = 0;
	private static final int DRAG_INITIALIZE_STATE = 1;
	private static final int DRAGGING_STATE = 2;
	private static final int DISABLED_STATE = 3;

	// always start in disabled state. user must explictly set state to allow
	// dragging
	private int m_state = INITIAL_STATE;
	private ModelView m_canvas;

	/**
	 * This is the widget that we use to drag a link on the canvas.
	 */
	private DragLink m_draglink;

	/**
	 * Used to denote the widget that we selected to start a drag
	 */
	private LinkWidget m_selectedlink;

	/**
	 * This is a cache of tables on the canvas at the time of the drag op This
	 * is basically an optimization in case there are other types of objects on
	 * the canvas.
	 */
	private TableWidget[] m_tables;

	/**
	 * This is the widget that is the source of the drag operation
	 */
	private TableWidget m_draganchor;

	/**
	 * The table widget that the mouse is dragging over currently
	 */
	private TableWidget m_dragtarget;

	/**
	 * This flag allows the user to click on a column in the table widget and
	 * drag a link out to another column. This mode is only available when the
	 * user clicks the link tool on the toolbar. Otherwise, when the user clicks
	 * on a column in the widget, the column is dragged.
	 */
	private boolean m_enablecreate;

	public LinkUI() {
		m_enablecreate = false;
	}

	/**
	 * ctor
	 */
	public LinkUI(ModelView canvas) {
		this();
		m_canvas = canvas;
		m_draglink = new DragLink(canvas);
	}

	/**
	 * Gets all table widgets from the view and caches them. This is an
	 * optimization for drag operations
	 */
	void cacheViewTables() {
		ArrayList tables = new ArrayList();
		for (int compindex = 0; compindex < m_canvas.getComponentCount(); compindex++) {
			Object comp = m_canvas.getComponent(compindex);
			if (comp instanceof TableWidget) {
				tables.add(comp);
			}
		}
		m_tables = (TableWidget[]) tables.toArray(new TableWidget[0]);
	}

	/**
	 * @return the model view
	 */
	public ModelView getView() {
		return m_canvas;
	}

	/**
	 * This flag allows the user to click on a column in the table widget and
	 * drag a link out to another column. This mode is only available when the
	 * user clicks the link tool on the toolbar. Otherwise, when the user clicks
	 * on a column in the widget, the column is dragged.
	 * 
	 * @param benablecreate
	 *            set to true if you want the link to be created and dragged
	 *            when the user clicks on a table widget column.
	 * 
	 */
	void enableLinkCreation(boolean benablecreate) {
		m_enablecreate = benablecreate;
	}

	/**
	 * @return the table widget at the given point on the canvas
	 */
	TableWidget getTableWidget(Point pt) {
		for (int index = 0; index < m_tables.length; index++) {
			TableWidget w = m_tables[index];
			Rectangle rect = w.getBounds();
			if (rect.contains(pt))
				return w;

		}
		return null;
	}

	/**
	 * @return the flag that enables/disables dragging of links
	 */
	public boolean isEnabled() {
		return (m_state != DISABLED_STATE);
	}

	public void mouseClicked(MouseEvent e) {

	}

	/**
	 * MouseMotionListener implementation. This method gets called when we are
	 * dragging the mouse. We need to check the drag state to see if we are
	 * currently dragging a link. If so, we need to update the view.
	 */
	public void mouseDragged(MouseEvent e) {
		if (m_state == DRAG_INITIALIZE_STATE) {
			if (m_selectedlink != null) {
				m_selectedlink.repaint();
				m_selectedlink.setVisible(false);
			}
			m_state = DRAGGING_STATE;
		}

		if (m_state == DRAGGING_STATE) {
			m_draglink.repaint();
			Object obj = e.getSource();
			Point canvaspt = SwingUtilities.convertPoint((java.awt.Component) obj, e.getPoint(), m_canvas);
			m_draglink.doDrag(canvaspt.x, canvaspt.y);

			if (m_dragtarget != null) {
				Rectangle rect = m_dragtarget.getBounds();
				if (!rect.contains(canvaspt)) {
					JList list = m_dragtarget.getJList();
					list.clearSelection();
				}
			}

			TableWidget w = getTableWidget(canvaspt);
			if (w != null && w != m_draganchor) {
				// the mouse is over a table widget, now determine if the mouse
				// is over
				// the list component
				JList list = w.getJList();
				Point listpt = SwingUtilities.convertPoint(m_canvas, canvaspt, list);
				int listindex = list.locationToIndex(listpt);
				list.setSelectedIndex(listindex);
				// list.ensureIndexIsVisible( listindex );
				m_dragtarget = w;
			}

		}
		e.consume();
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger())
			return;

		if (m_state == INITIAL_STATE) {
			Object obj = e.getSource();
			cacheViewTables();

			if (m_enablecreate && obj instanceof JList) {
				m_selectedlink = null;

				JList list = (JList) obj;
				int index = list.locationToIndex(e.getPoint());
				if (index >= 0) {
					// System.out.println( "LinkUI  start: " +
					// list.getModel().getElementAt( index ) );
					m_state = DRAG_INITIALIZE_STATE;
					Point pt = SwingUtilities.convertPoint(list, e.getPoint(), m_canvas);
					TableWidget widget = TableWidgetUI.getJListParent(list);
					if (widget != null) {
						list.setSelectedIndex(index);
						ColumnMetaData cmd = (ColumnMetaData) list.getModel().getElementAt(index);

						// create half a link since we don't have a destination
						// yet - only a source
						Link link = new Link(widget.getTableMetaData().getTableId(), cmd.getColumnName(), null, null);

						LinkWidget lw = new LinkWidget(m_canvas, widget, null, link);
						m_draglink = new DragLink(m_canvas, lw);
						m_canvas.setDragLink(m_draglink);
						m_draglink.startDrag(false, pt.x, pt.y);
						m_draganchor = widget;
					}
				}
			} else if (obj instanceof ModelView) {
				// if the source of the mouse pressed event is the canvas
				// itself, then
				// let's see if the any link was hit. If so, select the link and
				// start to drag the link. Furthermore, we need to cancel any
				// rubberband selection
				// on the canvas at this point
				ModelView modelview = (ModelView) obj;
				LinkedList linkwidgets = modelview.getLinkWidgets();
				m_selectedlink = null;
				Iterator iter = linkwidgets.iterator();
				while (iter.hasNext()) {
					LinkWidget lw = (LinkWidget) iter.next();
					lw.setSelected(false);
					if (m_selectedlink == null && lw.contains(e.getPoint())) {
						m_selectedlink = lw;
						m_selectedlink.setSelected(true);
						Terminal terminal = m_selectedlink.tabFromPoint(e.getPoint());
						if (terminal != null) {
							// This can also be the start of a drag operation,
							// so let's set the
							// drag link
							if (m_selectedlink.isSource(terminal)) {
								m_draglink = new DragLink(m_canvas, lw);
								Point pt = e.getPoint();
								m_canvas.setDragLink(m_draglink);
								m_canvas.cancelRubberBand();
								m_draglink.startDrag(true, pt.x, pt.y);
								m_state = DRAG_INITIALIZE_STATE;
								m_draganchor = lw.getDestinationTable();
								cacheViewTables();
							} else {
								m_draglink = new DragLink(m_canvas, lw);
								Point pt = e.getPoint();
								m_canvas.setDragLink(m_draglink);
								m_canvas.cancelRubberBand();
								m_draglink.startDrag(false, pt.x, pt.y);
								m_state = DRAG_INITIALIZE_STATE;
								m_draganchor = lw.getSourceTable();
								cacheViewTables();
							}
						}
					}
				} // while

				if (m_selectedlink != null) {
					// move the selected link to the top of the link widgets
					// list
					linkwidgets.remove(m_selectedlink);
					linkwidgets.add(m_selectedlink);
				}
			}
		}
	}

	/**
	 * MouseListener implementation
	 */
	public void mouseReleased(MouseEvent e) {
		if (m_state == DRAG_INITIALIZE_STATE) {
			m_state = INITIAL_STATE;
			m_draglink.stopDrag(0, 0);
			m_draganchor = null;
			m_dragtarget = null;
			if (m_selectedlink != null)
				m_selectedlink.repaint();
		}
		if (m_state == DRAGGING_STATE) {
			Object obj = e.getSource();
			Point mousept = e.getPoint();
			if (obj instanceof JList) {
				JList srclist = (JList) obj;
				// if the source is a list component, then the mouse coordinates
				// are
				// in the list coordinate space, so we need to convert mousept
				// to canvas coordinates
				mousept = SwingUtilities.convertPoint(srclist, e.getPoint(), m_canvas);
			}

			// if the source is a ModelView, then the user is moving an existing
			// link
			m_draglink.repaint();
			m_draglink.stopDrag(mousept.x, mousept.y);
			m_state = INITIAL_STATE;

			TableWidget targetw = getTableWidget(mousept);
			if (targetw != null) {
				// the mouse is over a table widget, now determine if the mouse
				// is over
				// the list component
				JList list = targetw.getJList();
				Point listpt = SwingUtilities.convertPoint(m_canvas, mousept, list);
				int listindex = list.locationToIndex(listpt);
				if (listindex >= 0) {
					list.setSelectedIndex(listindex);
					if (m_draglink.isDragSource()) {
						// we are dragging the base
						TableMetaData srctmd = targetw.getTableMetaData();
						ColumnMetaData srccol = (ColumnMetaData) list.getModel().getElementAt(listindex);
						TableId srctableid = srctmd.getTableId();

						// check if the sourcetable/sourcecolumn are the same as
						// the selected link, if
						// so, do nothing and make the selected link visible
						if (m_selectedlink != null && srctableid.equals(m_selectedlink.getSourceTableId())
								&& srccol.getName().equals(m_selectedlink.getSourceColumn())) {
							// the link was not moved, so just make it visible
							// again
							m_selectedlink.setVisible(true);
						} else {
							if (!tryCreateLink(m_selectedlink, srctableid, srccol.getName(),
									m_draglink.getDestinationTableId(), m_draglink.getDestinationColumn())) {
								if (m_selectedlink != null)
									m_selectedlink.setVisible(true);
							}
						}
					} else {
						// we are dragging the arrow head
						TableMetaData desttmd = targetw.getTableMetaData();
						ColumnMetaData destcol = (ColumnMetaData) list.getModel().getElementAt(listindex);
						TableId desttableid = desttmd.getTableId();

						// check if the desttable/destcolumn are the same as the
						// selected link, if
						// so, do nothing and make the selected link visible
						if (m_selectedlink != null && desttableid.equals(m_selectedlink.getDestinationTableId())
								&& destcol.getName().equals(m_selectedlink.getDestinationColumn())) {
							// the link was not moved, so just make it visible
							// again
							m_selectedlink.setVisible(true);
						} else {

							if (!tryCreateLink(m_selectedlink, m_draglink.getSourceTableId(),
									m_draglink.getSourceColumn(), desttableid, destcol.getName())) {
								if (m_selectedlink != null)
									m_selectedlink.setVisible(true);
							}

						}
					}
				} // if ( listindex >= 0 )
				else {
					if (!tryRemoveLink(m_selectedlink)) {
						if (m_selectedlink != null)
							m_selectedlink.setVisible(true);
					}
				}
				list.repaint();
			} else {
				if (!tryRemoveLink(m_selectedlink)) {
					if (m_selectedlink != null)
						m_selectedlink.setVisible(true);
				}
			}
		}

		Object obj = e.getSource();
		if (obj instanceof JList) {
			JList srclist = (JList) obj;
			srclist.repaint();
		}

	}

	/**
	 * Shows an error message dialog on the screen
	 * 
	 * @param msg
	 *            the message to show. This string should already have been
	 *            converted to I18N
	 */
	protected void showErrorMessage(String msg) {
		String title = I18N.getLocalizedMessage("Error");
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Creates a link and sets its user defined attribute to true. Specialized
	 * UI classes can override this method to provide their own creation logic.
	 * This method can return null, in which case, the link will not be created.
	 * Also, we only allow links to be created between single columns. If a user
	 * wants to create a multi column foreign key, he must do it through the
	 * TableEditorDialog.
	 * 
	 * @param moveLink
	 *            if this value is non-null, then the user is moving a link from
	 *            one location to another. Otherwise, the user is a attempting
	 *            to create a new link.
	 * @param sourceId
	 *            the id of the source table for the link
	 * @param sourceCol
	 *            the name of the source column.
	 * @param destId
	 *            the id of the destination table for the link
	 * @param destCol
	 *            the name of the destination column.
	 * @return the created link. If null is returned, the link was not able to
	 *         be created because of some condition. For example, if this is for
	 *         the model view, we don't allow links to be created on two tables
	 *         that have already been saved, unless the link is user defined.
	 *         However, if the tables are prototype tables, then we allow the
	 *         link to be defined as a foreign key.
	 */
	protected boolean tryCreateLink(LinkWidget moveLink, TableId sourceId, String sourceCol, TableId destId,
			String destCol) {
		if (sourceId.equals(destId) && I18N.equals(sourceCol, destCol)) {
			// we don't allow links from link to same column in the same table
			showErrorMessage(I18N.getLocalizedMessage("Cannot link to the same column"));
			return false;
		}

		Link link = Link.createUserDefinedLink(sourceId, sourceCol, destId, destCol);
		// remove the selected link from the canvas
		if (moveLink != null)
			m_canvas.removeLinkWidget(moveLink);

		LinkWidget lw = m_canvas.addLinkWidget(link);
		lw.recalc();
		return true;
	}

	/**
	 * Called when the user attempts to remove a link by dragging it off of a
	 * table. Specialized UI classes should override to do their own checks to
	 * see if a link can be removed
	 * 
	 * @return true if the link was successfully removed
	 */
	protected boolean tryRemoveLink(LinkWidget linkw) {
		// remove the selected link from the canvas
		if (linkw != null)
			m_canvas.removeLinkWidget(linkw);
		return true;
	}

	/**
	 * Sets the flag that enables/disables dragging of links
	 */
	public void setEnabled(boolean bEnable) {
		if (bEnable)
			setState(INITIAL_STATE);
		else
			setState(DISABLED_STATE);
	}

	private void setState(int state) {
		m_state = state;
	}

	/**
	 * Sets the view for this link ui. This should only be called once
	 */
	public void setView(ModelView canvas) {
		m_canvas = canvas;
		m_draglink = new DragLink(canvas);
	}

}
