package com.jeta.abeille.gui.export;

import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.gui.table.export.ColumnExportSetting;

/**
 * ColumnExportSetting for a sql query
 * 
 * @author Jeff Tassin
 */
public class TableColumnExportSetting extends ColumnExportSetting {
	/** the table id for the given column */
	private TableId m_tableid;

	/**
	 * the flag that indicates if we should export using sql formatting rules.
	 * For example, if we are exporting a date column type, then if we are using
	 * SQL rules, the exported value would be DATE('...').
	 */
	private boolean m_sqlformat;

	/** this is the JDBC data type if this column is for a result set */
	private int m_columntype;

	/**
	 * Flag that indicates if this column should be exported as text or not
	 */
	private boolean m_exportastext;

	public TableColumnExportSetting(TableId tableid, String columnname, int col, boolean bInclude, boolean sqlFormat,
			String regex) {
		super(columnname, col, bInclude, regex);
		m_tableid = tableid;
		m_sqlformat = sqlFormat;
	}

	/**
	 * @return the type of column (java.sql.Type).
	 */
	public int getColumnType() {
		return m_columntype;
	}

	/**
	 * @return the name of the table that contains the column associated with
	 *         this setting
	 */
	public String getTableName() {
		return m_tableid.getTableName();
	}

	/**
	 * @return the flag that indicates if this column should be exported as text
	 */
	public boolean isExportAsText() {
		return m_exportastext;
	}

	/**
	 * @return the flag that indicates if this column should be exported as SQL
	 */
	public boolean isSQLFormat() {
		return m_sqlformat;
	}

	/**
	 * Sets the type of column (java.sql.Type).
	 */
	public void setColumnType(int type) {
		m_columntype = type;
	}

	/**
	 * Sets the flag that indicates if this column should be exported as text
	 */
	public void setExportAsText(boolean btxt) {
		m_exportastext = btxt;
	}

	/**
	 * Sets the sql format flag
	 */
	public void setSQLFormat(boolean bsql) {
		m_sqlformat = bsql;
	}

}
