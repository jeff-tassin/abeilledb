package com.jeta.abeille.gui.importer;

import com.jeta.abeille.database.model.ColumnMetaData;

public class SourceColumn extends ColumnMetaData {
	/** the (zero-based) index of this column in the source data */
	private int m_index;

	public SourceColumn(ColumnMetaData cmd, int index) {
		super(cmd);
		m_index = index;
	}

	/**
	 * @return the (zero-based) index of this column in the source data
	 */
	public int getIndex() {
		return m_index;
	}
}
