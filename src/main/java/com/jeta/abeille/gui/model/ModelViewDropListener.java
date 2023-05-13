package com.jeta.abeille.gui.model;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSConnectionMgr;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.i18n.I18N;

/**
 * The drop listener interface for the model view
 */
public class ModelViewDropListener implements DropTargetListener {
	/** the view we are listener for drop events */
	private ModelView m_view;

	/**
	 * this is the view port for the view. we use this to determine which part
	 * of the view is visible during a drop
	 */
	private JViewport m_viewport;

	/**
	 * ctor
	 */
	public ModelViewDropListener(ModelView view) {
		m_view = view;
	}

	/**
	 * Iterate over
	 */
	private boolean checkTableConnection(Object[] results) {
		if (!isFromCurrentConnection(results)) {
			// a table was dropped on the view that has a different connection
			// than
			// this connection
			String msg = I18N.getLocalizedMessage("Table_dropped_with_different_connection");
			String title = I18N.getLocalizedMessage("Confirm");
			int result = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
			return (result == JOptionPane.YES_OPTION);
		}
		return true;
	}

	/**
	 * Invoked when you are dragging over the DropSite
	 * 
	 */
	public void dragEnter(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)
				|| event.isDataFlavorSupported(DbObjectFlavor.TABLE_PROTOTYPE_REFERENCE)) {
			event.acceptDrag(DnDConstants.ACTION_COPY);
		} else
			event.rejectDrag();
	}

	/**
	 * Invoked when you are exit the DropSite without dropping
	 * 
	 */
	public void dragExit(DropTargetEvent event) {

	}

	/**
	 * Invoked when a drag operation is going on
	 * 
	 */
	public void dragOver(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)
				|| event.isDataFlavorSupported(DbObjectFlavor.TABLE_PROTOTYPE_REFERENCE)) {
			event.acceptDrag(DnDConstants.ACTION_COPY);
		} else
			event.rejectDrag();
	}

	/**
	 * a drop has occurred
	 */
	public void drop(DropTargetDropEvent event) {
		validateViewport();

		Transferable transferable = event.getTransferable();
		// table id is the most basic flavor. if any richer table flavors are in
		// the transfer, the table_id flavor
		// will be there as well, so we only need to check here for table id
		if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)
				|| event.isDataFlavorSupported(DbObjectFlavor.TABLE_PROTOTYPE_REFERENCE)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_LINK);
				Point pt = event.getLocation();
				// Point org = m_viewport.getViewPosition();
				// pt.x = pt.x + org.x;
				// pt.y = pt.y +org.y;
				drop(transferable, pt);
				event.dropComplete(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event.rejectDrop();
		}
	}

	/**
	 * Utility method to support dropping objects on model view
	 */
	public void drop(Transferable transferable, Point pt) {
		validateViewport();
		TreeSet drop_failures = new TreeSet();
		try {
			boolean bchecked = false;
			TreeSet dropped = new TreeSet();

			TSConnection current = getConnection();

			m_view.deselectAll();
			ModelViewModel viewmodel = m_view.getModel();

			// location is in component coordinates
			if (pt == null) {
				pt = m_viewport.getViewPosition();
			}
			if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_WIDGET_REFERENCE)) {
				Object obj = transferable.getTransferData(DbObjectFlavor.TABLE_WIDGET_REFERENCE);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;

					// if the user is copying tables from a different
					// connection, then
					// we need to warn them that tables from different
					// connections can only be copied
					// as new
					bchecked = true;
					if (checkTableConnection(results)) {
						/** calculate the offset of the entire group */
						int org_x = 0;
						int org_y = 0;
						for (int index = 0; index < results.length; index++) {
							TableWidgetReference twref = (TableWidgetReference) results[index];
							TableWidget tw = twref.getTableWidget();
							if (index == 0) {
								org_x = tw.getX();
								org_y = tw.getY();
							}

							if (org_x > tw.getX())
								org_x = tw.getX();

							if (org_y > tw.getY())
								org_y = tw.getY();
						}

						for (int index = 0; index < results.length; index++) {
							TableWidgetReference twref = (TableWidgetReference) results[index];
							TableWidget tw = twref.getTableWidget();

							TableId tableid = tw.getTableId();
							int x = pt.x;
							int y = pt.y;

							if (results.length > 1) {
								x = x + tw.getX() - org_x;
								y = y + tw.getY() - org_y;
							}

							if (isFromConnection(twref.getConnection(), current)) {
								TableWidget widget = viewmodel.getTableWidget(tableid);
								if (widget == null) {
									widget = viewmodel.addTable(tableid,
											new Rectangle(x, y, tw.getWidth(), tw.getHeight()));
									m_view.selectComponent(widget);
									dropped.add(tableid);
								} else {
									widget.setLocation(x, y);
									dropped.add(tableid);
								}
							} else {
								if (importTable(tw.getTableMetaData(), new Point(x, y))) {
									dropped.add(tableid);
								} else {
									if (!dropped.contains(tableid))
										drop_failures.add(tableid);
								}
							}
						}
					}
				}
			}

			if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)) {
				// this is for table objects that are being dropped from other
				// locations such as the object tree
				Object obj = transferable.getTransferData(DbObjectFlavor.TABLE_REFERENCE);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;

					if (!bchecked) {
						bchecked = checkTableConnection(results);
					}

					if (bchecked) {
						int x = pt.x;
						int y = pt.y;

						for (int index = 0; index < results.length; index++) {
							TableReference tref = (TableReference) results[index];
							TableId tableid = tref.getTableId();
							if (!dropped.contains(tableid)) {
								if (isFromConnection(tref.getConnection(), current)) {
									TableWidget widget = viewmodel.getTableWidget(tableid);
									if (widget == null) {
										widget = viewmodel.addTable(tableid, x, y);
										m_view.selectComponent(widget);
									} else {
										widget.setLocation(x, y);
										dropped.add(tableid);

									}
									x += 32;
									y += 32;
								} else {
									if (importTable(tref.getConnection().getTable(tableid), new Point(x, y))) {
										dropped.add(tableid);
									} else {
										if (!dropped.contains(tableid))
											drop_failures.add(tableid);
									}
								}
							}
						}
					}
				}
				m_view.repaint();
			}

			if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_PROTOTYPE_REFERENCE)) {
				// this is for table objects that have not yet been saved
				Object obj = transferable.getTransferData(DbObjectFlavor.TABLE_PROTOTYPE_REFERENCE);

				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;

					if (!bchecked) {
						bchecked = checkTableConnection(results);
					}

					if (bchecked) {
						int x = pt.x;
						int y = pt.y;
						for (int index = 0; index < results.length; index++) {
							TableMetaDataReference tmdref = (TableMetaDataReference) results[index];
							TableMetaData tmd = tmdref.getTableMetaData();
							TableId tableid = tmd.getTableId();
							if (!dropped.contains(tableid)) {
								if (isFromConnection(tmdref.getConnection(), current)) {
									TableWidget widget = viewmodel.getTableWidget(tableid);
									if (widget == null) {
										widget = viewmodel.addTable(tableid, x, y);
										m_view.selectComponent(widget);
									} else {
										widget.setLocation(x, y);
									}
									x += 32;
									y += 32;
								} else {
									if (importTable(tmd, new Point(x, y))) {
										dropped.add(tableid);
									} else {
										if (!dropped.contains(tableid))
											drop_failures.add(tableid);
									}
								}
							}
						}
					}
				}
			}

			if (transferable.isDataFlavorSupported(DbObjectFlavor.LINK)) {
				// this is for table objects that are being dropped from other
				// locations such as the object tree
				Object obj = transferable.getTransferData(DbObjectFlavor.LINK);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					for (int index = 0; index < results.length; index++) {
						Link link = (Link) results[index];
						if (link.isUserDefined()) {
							if (viewmodel.contains(link.getSourceTableId())
									&& viewmodel.contains(link.getDestinationTableId())) {
								viewmodel.getModeler().addUserLink(link);
							}
						}
					}
				}
			}

			m_view.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}

		m_view.notifyCanvasChanged();
		m_view.validateComponents();

		/**
		 * some of the given tables could not be dropped because the already
		 * exist, so let's display a message
		 */
		if (drop_failures.size() > 0) {
			StringBuffer mbuff = new StringBuffer();
			Iterator iter = drop_failures.iterator();
			while (iter.hasNext()) {
				TableId tableid = (TableId) iter.next();
				mbuff.append(tableid.getTableName());
				mbuff.append('\n');
			}
			String msg = I18N.format("Tables_dropped_already_exists_1", mbuff.toString());

			TSErrorDialog dlg = TSErrorDialog.createDialog(msg);
			dlg.showCenter();
		}
	}

	/**
	 * Utility method to support dropping objects on model view
	 */
	public void drop(Transferable transferable) {
		drop(transferable, null);
	}

	/**
	 * Invoked if the use modifies the current drop gesture
	 */
	public void dropActionChanged(DropTargetDragEvent event) {
	}

	/**
	 * @return the underlying database connection
	 */
	private TSConnection getConnection() {
		return m_view.getModel().getConnection();
	}

	/**
	 * Creates a new table prototype from the given table metadata. This is
	 * generally called when pasting/dropping a table from another connection
	 */
	private boolean importTable(TableMetaData tmd, Point pt) {
		if (tmd != null) {
			ModelViewController mvc = (ModelViewController) m_view.getController();
			tmd = (TableMetaData) tmd.clone();
			TableId tmdid = tmd.getTableId();

			TSConnection conn = getConnection();
			Schema schema = tmdid.getSchema();
			DbModel dbmodel = conn.getModel(tmdid.getCatalog());
			if (!conn.contains(tmdid.getCatalog()) || !dbmodel.contains(schema)) {
				// if the connection does not contain the given schema, set
				// the new table to the current schema
				Catalog catalog = conn.getDefaultCatalog();
				Schema newschema = conn.getCurrentSchema();

				tmdid = (TableId) tmdid.change(conn, catalog, newschema);

				// also update Schemas on the foreign keys
				Collection fkeys = tmd.getForeignKeys();
				Iterator iter = fkeys.iterator();
				while (iter.hasNext()) {
					DbForeignKey fkey = (DbForeignKey) iter.next();
					TableId srcid = fkey.getSourceTableId();

					srcid = (TableId) srcid.change(conn, catalog, newschema);
					fkey.setSourceTableId(srcid);

					TableId destid = fkey.getDestinationTableId();
					destid = (TableId) destid.change(conn, catalog, newschema);
					fkey.setDestinationTableId(destid);
				}
			} else {
				tmdid = tmdid.change(conn);
			}

			// now update the column types to the supported types for the given
			// database
			TSDatabase db = (TSDatabase) conn.getImplementation(TSDatabase.COMPONENT_ID);
			Collection cols = tmd.getColumns();
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				DataTypeInfo dtype = db.getDataTypeInfo(cmd.getTypeName());
				if (dtype == null) {
					String jdbc_type = DbUtils.getJDBCTypeName(cmd.getType());
					if (jdbc_type != null && !jdbc_type.equals("UNKNOWN")) {
						cmd.setTypeName(jdbc_type);
					}
				}
			}

			tmd.setTableId(tmdid);
			return (mvc.addPrototype(tmd, pt) != null);
		}
		return false;
	}

	/**
	 * @return true if all the dropped tables are from the current connection
	 */
	protected boolean isFromCurrentConnection(Object[] results) {
		TSConnection current = getConnection();
		for (int index = 0; index < results.length; index++) {
			TSConnection dropcon = null;
			Object obj = results[index];
			if (obj instanceof TableReference) {
				TableReference tref = (TableReference) obj;
				dropcon = tref.getConnection();
			} else if (obj instanceof TableWidgetReference) {
				TableWidgetReference w = (TableWidgetReference) obj;
				dropcon = w.getConnection();
			} else if (obj instanceof TableWidget) {
				assert (false);
			}

			if (dropcon != null) {
				if (!isFromConnection(dropcon, current)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return true if the tableid is defined in the given connection
	 */
	private boolean isFromConnection(TSConnection conn1, TSConnection conn2) {
		return conn1.equals(conn2);
	}

	/**
	 * Gets the viewport from the view. The view must have a valid viewport as a
	 * parent.
	 */
	private void validateViewport() {
		if (m_viewport == null) {
			java.awt.Component comp = m_view.getParent();
			while ((comp != null) && !(comp instanceof java.awt.Frame) && !(comp instanceof java.awt.Dialog)) {
				if (comp instanceof JViewport) {
					m_viewport = (JViewport) comp;
					break;
				}
			}
		}

		assert (m_viewport != null);
	}

}
