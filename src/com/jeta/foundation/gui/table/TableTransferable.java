/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import com.jeta.foundation.utils.TSUtils;

/**
 * This is the implementation of the Transferable interface for handling data
 * transfer from TSTable components to the clipboard or drag-n-drop
 * 
 * @author Jeff Tassin
 */
public class TableTransferable implements Transferable {
	public static final DataFlavor TABLE_DATAFLAVOR = new DataFlavor(TableTransferable.class, null);
	private static final DataFlavor m_flavors[] = { TABLE_DATAFLAVOR, DataFlavor.stringFlavor };

	private TSTablePanel m_tablepanel; // the source of the data transfer
	private Object[][] m_transferdata; // can be a vector or a matrix
	private String[] m_columnnames; // the column names associated with the data

	/**
	 * ctor
	 * 
	 * @param table
	 *            the source of the data transfer
	 */
	public TableTransferable(Object[][] data, String[] colnames) {
		TSUtils._assert(data[0].length == colnames.length);
		m_transferdata = data;
		m_columnnames = colnames;
	}

	/**
	 * @return an object which represents the data to be transferred. The class
	 *         of the object returned is defined by the representation class of
	 *         the flavor.
	 */
	public Object getTransferData(DataFlavor flavor) {
		if (flavor.equals(DataFlavor.stringFlavor)) {
			// return m_formatter.formatData( m_transferdata, m_columnnames );
			return "";
		} else if (flavor.equals(TABLE_DATAFLAVOR)) {
			return this;
		}
		return null;
	}

	/**
	 * @return an array of DataFlavor objects indicating the flavors the data
	 *         can be provided in. The array should be ordered according to
	 *         preference for providing the data (from most richly descriptive
	 *         to least descriptive).
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return m_flavors;
	}

	/**
	 * @return whether or not the specified data flavor is supported for this
	 *         object.
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (int index = 0; index < m_flavors.length; index++)
			if (flavor.equals(m_flavors[index]))
				return true;

		return false;
	}
}
