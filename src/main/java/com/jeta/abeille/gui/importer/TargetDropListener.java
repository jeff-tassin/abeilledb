package com.jeta.abeille.gui.importer;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;

import javax.swing.JTable;

import java.util.ArrayList;

import com.jeta.abeille.gui.model.DbObjectFlavor;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;

/**
 * The drop listener interface for the target window. Allows columns to be
 * dropped in the window
 */
public class TargetDropListener implements DropTargetListener {
	/**
	 * The view
	 */
	private TargetColumnsView m_view;
	private TargetColumnsModel m_model;

	/**
	 * ctor
	 */
	public TargetDropListener(TargetColumnsView view) {
		m_view = view;
		m_model = view.getModel();
	}

	/**
	 * Invoked when you are dragging over the DropSite
	 * 
	 */
	public void dragEnter(DropTargetDragEvent event) {

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
		boolean accept = false;
		if (event.isDataFlavorSupported(ImportObjectFlavor.SOURCE_COLUMN)) {
			JTable table = m_view.getTable();
			table.clearSelection();
			Point pt = event.getLocation();
			int col = table.columnAtPoint(pt);
			if (col == TargetColumnsModel.VALUE_COLUMN) {
				int row = table.rowAtPoint(pt);
				if (row >= 0) {
					table.setColumnSelectionInterval(col, col);
					table.setRowSelectionInterval(row, row);
					accept = true;
				}
			}
		} else if (event.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)) {
			accept = true;
		}

		if (accept)
			event.acceptDrag(DnDConstants.ACTION_COPY);
		else
			event.rejectDrag();

	}

	/**
	 * a drop has occurred
	 */
	public void drop(DropTargetDropEvent event) {
		Transferable transferable = event.getTransferable();
		if (transferable.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)
				|| transferable.isDataFlavorSupported(ImportObjectFlavor.SOURCE_COLUMN)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_LINK);
				drop(transferable, event.getLocation());
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
	public void drop(Transferable transferable, Point pt) {
		if (transferable.isDataFlavorSupported(ImportObjectFlavor.SOURCE_COLUMN)) {
			JTable table = m_view.getTable();
			table.clearSelection();
			int col = table.columnAtPoint(pt);

			if (col == TargetColumnsModel.VALUE_COLUMN) {
				int row = table.rowAtPoint(pt);

				if (row >= 0) {
					try {
						Object obj = transferable.getTransferData(ImportObjectFlavor.SOURCE_COLUMN);
						if (obj instanceof Object[]) {
							Object[] results = (Object[]) obj;
							if (results.length > 0) {
								SourceColumn sc = (SourceColumn) results[0];
								m_model.setTargetValue(row, sc);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else if (transferable.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)) {
			try {
				Object obj = transferable.getTransferData(DbObjectFlavor.COLUMN_METADATA);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					for (int index = 0; index < results.length; index++) {
						ColumnMetaData cmd = (ColumnMetaData) results[index];
						m_model.addColumn(new TargetColumnInfo(cmd));
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
