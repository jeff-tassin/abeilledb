package com.jeta.abeille.gui.query;

import java.awt.datatransfer.Transferable;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;

import java.util.ArrayList;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSConnectionMgr;

import com.jeta.abeille.gui.model.DbObjectFlavor;
import com.jeta.abeille.gui.model.TableReference;

import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.query.Operator;

import com.jeta.foundation.gui.dnd.DnDSupport;
import com.jeta.foundation.utils.TSUtils;

/**
 * The drop listener interface for the constraint view. Allows columns to be
 * dropped in the window
 */
public class ConstraintDropListener implements DropTargetListener {
	/**
	 * The constraint view
	 */
	private ConstraintView m_view;
	private ConstraintModel m_model;

	/**
	 * ctor
	 */
	public ConstraintDropListener(ConstraintView view) {
		m_view = view;
		m_model = view.getModel();
	}

	/**
	 * Builds an array of constraints based on the columns of the given table
	 */
	private QueryConstraint[] buildConstraints(TSConnection conn, TableId tableid) {
		ArrayList result = new ArrayList();

		try {
			DbModel model = conn.getModel(tableid.getCatalog());
			TableMetaData tmd = model.getTable(tableid);
			if (tmd != null) {
				for (int index = 0; index < tmd.getColumnCount(); index++) {
					ColumnMetaData cmd = tmd.getColumn(index);
					assert (tableid.equals(cmd.getTableId()));
					QueryConstraint qc = null;
					if (m_model.isQualified()) {
						qc = new QueryConstraint(tableid, cmd.getColumnName(), Operator.EQUALS, "?", null, null);
					} else {
						qc = new QueryConstraint(tableid, cmd.getColumnName(), Operator.EQUALS, "?",
								m_model.getCatalog(), m_model.getSchema());
					}
					result.add(qc);
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		return (QueryConstraint[]) result.toArray(new QueryConstraint[0]);
	}

	/**
	 * Invoked when you are dragging over the DropSite
	 * 
	 */
	public void dragEnter(DropTargetDragEvent event) {
		// System.out.println( "Constraint dragEnter");
	}

	/**
	 * Invoked when you are exit the DropSite without dropping
	 * 
	 */
	public void dragExit(DropTargetEvent event) {
		// System.out.println( "Constraint dragExit");
	}

	/**
	 * Invoked when a drag operation is going on
	 * 
	 */
	public void dragOver(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)
				|| event.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE))
			DnDSupport.acceptDrag(event, DnDConstants.ACTION_MOVE);
		else
			DnDSupport.rejectDrag(event);
	}

	/**
	 * a drop has occurred
	 */
	public void drop(DropTargetDropEvent event) {
		Transferable transferable = event.getTransferable();
		if (transferable.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_LINK);
				drop(transferable);
				event.dropComplete(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_LINK);
				drop(transferable);
				event.dropComplete(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (transferable.isDataFlavorSupported(QueryFlavor.QUERY_CONSTRAINT)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_LINK);
				drop(transferable);
				event.dropComplete(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event.rejectDrop();
		}
	}

	/**
	 * a drop has occurred
	 */
	public void drop(Transferable transferable) {
		if (transferable.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)) {
			try {
				Object obj = transferable.getTransferData(DbObjectFlavor.COLUMN_METADATA);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					for (int index = 0; index < results.length; index++) {
						ColumnMetaData cmd = (ColumnMetaData) results[index];
						QueryConstraint qc = new QueryConstraint(cmd.getTableId(), cmd.getColumnName(),
								Operator.EQUALS, "?", m_model.getCatalog(), m_model.getSchema());
						m_model.addRow(qc);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)) {
			try {
				Object obj = transferable.getTransferData(DbObjectFlavor.TABLE_REFERENCE);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					for (int index = 0; index < results.length; index++) {
						TableReference tref = (TableReference) results[index];
						QueryConstraint[] constraints = buildConstraints(tref.getConnection(), tref.getTableId());
						for (int count = 0; count < constraints.length; count++) {
							m_model.addRow(constraints[count]);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (transferable.isDataFlavorSupported(QueryFlavor.QUERY_CONSTRAINT)) {
			try {
				Object obj = transferable.getTransferData(QueryFlavor.QUERY_CONSTRAINT);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					for (int index = 0; index < results.length; index++) {
						QueryConstraint qc = (QueryConstraint) results[index];
						m_model.addRow(qc);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Invoked if the use modifies the current drop gesture
	 */
	public void dropActionChanged(DropTargetDragEvent event) {

	}

}
