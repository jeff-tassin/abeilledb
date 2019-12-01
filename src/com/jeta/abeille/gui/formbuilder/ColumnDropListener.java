package com.jeta.abeille.gui.formbuilder;

import java.awt.datatransfer.Transferable;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;

import com.jeta.abeille.gui.model.DbObjectFlavor;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.query.Reportable;

import com.jeta.abeille.gui.update.InstanceOptionsModel;

import com.jeta.foundation.gui.dnd.DnDSupport;

/**
 * The drop listener interface for the reportables view. Allows columns,tables
 * to be dropped in the window
 */
public class ColumnDropListener implements DropTargetListener {
	/**
	 * The constraint view
	 */
	private ColumnsView m_view;
	private FormInstanceMetaData m_columnmodel;

	/**
	 * ctor
	 */
	public ColumnDropListener(ColumnsView view) {
		m_view = view;
		m_columnmodel = (FormInstanceMetaData) view.getMetaData();
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
				event.acceptDrop(DnDConstants.ACTION_MOVE);
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
		if (transferable.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)
				|| transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)) {
			try {
				Object obj = transferable.getTransferData(DbObjectFlavor.COLUMN_METADATA);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					for (int index = 0; index < results.length; index++) {
						ColumnMetaData cmd = (ColumnMetaData) results[index];
						m_columnmodel.addColumn(cmd);
					}

					InstanceOptionsModel guimodel = m_view.getGuiModel();
					guimodel.fireModelChanged();
					m_view.repaint();
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
