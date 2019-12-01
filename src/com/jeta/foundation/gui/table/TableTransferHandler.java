/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

/**
 * Transfer handler for the TSTable classes
 * 
 * @author Jeff Tassin
 */
public class TableTransferHandler extends TransferHandler {
	/**
	 * @return the source actions supported by our table. Because TSTables are
	 *         immutable, we only support copy operations
	 */
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	/**
	 * @return false we don't allow imports
	 */
	public boolean canImport(JComponent comp, DataFlavor flavor[]) {
		return false;
	}

	/**
	 * Creates the transferable to be used as the source of the data transfer.
	 * For our tables, this object is always of TableTransferable type.
	 */
	public Transferable createTransferable(JComponent comp) {
		TSTablePanel tablepanel = null;
		if (comp instanceof TSTablePanel) {
			tablepanel = (TSTablePanel) comp;
		}

		if (tablepanel != null) {
			return null;
		} else
			return null;
	}

	/**
	 * Causes a transfer from the given component to the given clipboard. This
	 * method is called by the default cut and copy actions registered in a
	 * component's action map. The transfer will have been completed at the
	 * return of this call. The transfer will take place using the
	 * java.awt.datatransfer mechanism, requiring no further effort from the
	 * developer. The exportDone method will be called when the transfer has
	 * completed.
	 */
	public void exportToClipboard(JComponent comp, Clipboard clipboard, int action) {
		super.exportToClipboard(comp, clipboard, action);
	}

	/**
	 * @return false we don't allow imports
	 */
	public boolean importData(JComponent comp, Transferable t) {
		return false;
	}

	/**
	 * Sets the object responsible for formatting the output of a copy operation
	 * 
	 * @param formatter
	 *            the formatter object to set
	 */
	public void setFormatter(TableFormatter formatter) {
		// m_formatter = formatter;
	}
}
