package com.jeta.abeille.gui.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import com.jeta.abeille.database.model.ColumnMetaData;

/**
 * Base transfer handler for the table widget (allows draggin column meta data
 * objects)
 * 
 * @author Jeff Tassin
 */
public class TableWidgetTransferHandler extends TransferHandler {

	/**
	 * No imports
	 */
	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		return false;
	}

	/**
	 * Creates the transferable for our table widget
	 */
	protected Transferable createTransferable(JComponent comp) {
		if (comp instanceof JList) {
			MultiTransferable mt = new MultiTransferable();
			TableWidget widget = TableWidgetUI.getJListParent((JList) comp);
			if (widget != null) {
				JList list = widget.getJList();
				Object[] values = list.getSelectedValues();
				for (int index = 0; index < values.length; index++) {
					ColumnMetaData cmd = (ColumnMetaData) values[index];
					mt.addData(DbObjectFlavor.COLUMN_METADATA, cmd);
				}
			}
			return mt;
		}
		return null;
	}

	/**
	 * Always return copy for TableWidget
	 */
	public int getSourceActions(JComponent comp) {
		return TransferHandler.COPY;
	}

	/**
	 * No import for table widget
	 */
	public boolean importData(JComponent comp, Transferable t) {
		return false;
	}
}
