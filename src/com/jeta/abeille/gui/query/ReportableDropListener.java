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

import com.jeta.abeille.query.Reportable;

import com.jeta.foundation.gui.dnd.DnDSupport;
import com.jeta.foundation.utils.TSUtils;

/**
 * The drop listener interface for the reportables view. Allows columns,tables
 * to be dropped in the window
 */
public class ReportableDropListener implements DropTargetListener {
	/**
	 * The constraint view
	 */
	private ReportablesView m_view;
	private ReportablesModel m_model;

	/**
	 * ctor
	 */
	public ReportableDropListener(ReportablesView view) {
		m_view = view;
		m_model = view.getModel();
	}

	/**
	 * Helper method that creates an array of reportables for all columns in a
	 * given table.
	 */
	Reportable[] createReportables(TSConnection conn, TableId tableid) {
		ArrayList result = new ArrayList();
		try {
			TableMetaData tmd = conn.getTable(tableid);
			if (tmd != null) {
				for (int index = 0; index < tmd.getColumnCount(); index++) {
					ColumnMetaData cmd = tmd.getColumn(index);
					assert (tableid.equals(cmd.getTableId()));
					Reportable report = new Reportable(cmd);
					result.add(report);
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		return (Reportable[]) result.toArray(new Reportable[0]);
	}

	/**
	 * Invoked when you are dragging over the DropSite
	 * 
	 */
	public void dragEnter(DropTargetDragEvent event) {
		// System.out.println( "Reportable dragEnter");
	}

	/**
	 * Invoked when you are exit the DropSite without dropping
	 * 
	 */
	public void dragExit(DropTargetEvent event) {
		// System.out.println( "Reportable dragExit");
	}

	/**
	 * Invoked when a drag operation is going on
	 * 
	 */
	public void dragOver(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)
				|| event.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE))
			DnDSupport.acceptDrag(event, DnDConstants.ACTION_COPY);
		else
			DnDSupport.rejectDrag(event);
	}

	/**
	 * a drop has occurred
	 */
	public void drop(DropTargetDropEvent event) {
		// System.out.println( "dropped on reportable view: " );
		Transferable transferable = event.getTransferable();
		if (transferable.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_COPY);
				drop(transferable);
				event.dropComplete(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_COPY);
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
	 * Drops the transferable onto the view
	 */
	void drop(Transferable transferable) {
		if (transferable.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)) {
			try {
				Object obj = transferable.getTransferData(DbObjectFlavor.COLUMN_METADATA);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					for (int index = 0; index < results.length; index++) {
						ColumnMetaData cmd = (ColumnMetaData) results[index];
						m_model.addReportable(new Reportable(cmd));
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
						Reportable[] reportables = createReportables(tref.getConnection(), tref.getTableId());
						for (int count = 0; count < reportables.length; count++) {
							m_model.addReportable(reportables[count]);
						}
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
