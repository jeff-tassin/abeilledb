package com.jeta.abeille.database.utils;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class caches metadata information. It is needed to keep metadata around
 * in case the resultset is closed by the driver. This can happen when working
 * with multiple resultset.
 * 
 * @author Jeff Tassin
 */
public class ResultSetMetaDataCache implements ResultSetMetaData {
	private CMDInfo[] m_cols;
	private TSConnection m_connection;

	public ResultSetMetaDataCache(TSConnection tsconn, ResultSetMetaData md) throws SQLException {
		m_connection = tsconn;
		m_cols = new CMDInfo[md.getColumnCount()];
		for (int index = 1; index <= md.getColumnCount(); index++) {
			CMDInfo ci = new CMDInfo();
			m_cols[index - 1] = ci;

			try {
				ci.m_auto_increment = md.isAutoIncrement(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_case_sensitive = md.isCaseSensitive(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_searchable = md.isSearchable(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_currency = md.isCurrency(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_nullable = md.isNullable(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_signed = md.isSigned(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_column_display_size = md.getColumnDisplaySize(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_column_label = md.getColumnLabel(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_column_name = md.getColumnName(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_schem_name = md.getSchemaName(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_precision = md.getPrecision(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_scale = md.getScale(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_table_name = md.getTableName(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_catalog_name = md.getCatalogName(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_column_type = md.getColumnType(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				if (!Database.SYBASE.equals(m_connection.getDatabase())) {
					ci.m_column_type_name = md.getColumnTypeName(index);
				}
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_read_only = md.isReadOnly(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_writable = md.isWritable(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_definitely_writable = md.isDefinitelyWritable(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}

			try {
				ci.m_column_class_name = md.getColumnClassName(index);
			} catch (Throwable e) {
				if (TSUtils.isDebug()) {
					TSUtils.printException(e);
				}
			}
		}
	}

	private CMDInfo getColumnData(int column) throws SQLException {
		column--;
		if (column < 0 || column >= m_cols.length) {
			throw new SQLException("Invalid Column " + column);
		} else {
			return (CMDInfo) m_cols[column];
		}
	}

	/**
	 * Returns the number of columns in this <code>ResultSet</code> object.
	 * 
	 * @return the number of columns
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getColumnCount() throws SQLException {
		if (m_cols == null)
			return 0;
		else
			return m_cols.length;
	}

	/**
	 * Indicates whether the designated column is automatically numbered, thus
	 * read-only.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isAutoIncrement(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_auto_increment;
	}

	/**
	 * Indicates whether a column's case matters.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isCaseSensitive(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_case_sensitive;
	}

	/**
	 * Indicates whether the designated column can be used in a where clause.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isSearchable(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_searchable;
	}

	/**
	 * Indicates whether the designated column is a cash value.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isCurrency(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_currency;
	}

	/**
	 * Indicates the nullability of values in the designated column.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return the nullability status of the given column; one of
	 *         <code>columnNoNulls</code>, <code>columnNullable</code> or
	 *         <code>columnNullableUnknown</code>
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int isNullable(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_nullable;
	}

	/**
	 * Indicates whether values in the designated column are signed numbers.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isSigned(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_signed;
	}

	/**
	 * Indicates the designated column's normal maximum width in characters.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return the normal maximum number of characters allowed as the width of
	 *         the designated column
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getColumnDisplaySize(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_column_display_size;
	}

	/**
	 * Gets the designated column's suggested title for use in printouts and
	 * displays.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return the suggested column title
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public String getColumnLabel(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_column_label;
	}

	/**
	 * Get the designated column's name.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return column name
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public String getColumnName(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_column_name;
	}

	/**
	 * Get the designated column's table's schema.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return schema name or "" if not applicable
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public String getSchemaName(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_schem_name;
	}

	/**
	 * Get the designated column's number of decimal digits.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return precision
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getPrecision(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_precision;
	}

	/**
	 * Gets the designated column's number of digits to right of the decimal
	 * point.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return scale
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getScale(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_scale;
	}

	/**
	 * Gets the designated column's table name.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return table name or "" if not applicable
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public String getTableName(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_table_name;
	}

	/**
	 * Gets the designated column's table's catalog name.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return the name of the catalog for the table in which the given column
	 *         appears or "" if not applicable
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public String getCatalogName(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_catalog_name;
	}

	/**
	 * Retrieves the designated column's SQL type.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return SQL type from java.sql.Types
	 * @exception SQLException
	 *                if a database access error occurs
	 * @see Types
	 */
	public int getColumnType(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_column_type;
	}

	/**
	 * Retrieves the designated column's database-specific type name.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return type name used by the database. If the column type is a
	 *         user-defined type, then a fully-qualified type name is returned.
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public String getColumnTypeName(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_column_type_name;
	}

	/**
	 * Indicates whether the designated column is definitely not writable.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isReadOnly(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_read_only;
	}

	/**
	 * Indicates whether it is possible for a write on the designated column to
	 * succeed.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isWritable(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_writable;
	}

	/**
	 * Indicates whether a write on the designated column will definitely
	 * succeed.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isDefinitelyWritable(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_definitely_writable;
	}

	// --------------------------JDBC 2.0-----------------------------------

	/**
	 * <p>
	 * Returns the fully-qualified name of the Java class whose instances are
	 * manufactured if the method <code>ResultSet.getObject</code> is called to
	 * retrieve a value from the column. <code>ResultSet.getObject</code> may
	 * return a subclass of the class returned by this method.
	 * 
	 * @param column
	 *            the first column is 1, the second is 2, ...
	 * @return the fully-qualified name of the class in the Java programming
	 *         language that would be used by the method
	 *         <code>ResultSet.getObject</code> to retrieve the value in the
	 *         specified column. This is the class name used for custom mapping.
	 * @exception SQLException
	 *                if a database access error occurs
	 * @since 1.2
	 */
	public String getColumnClassName(int column) throws SQLException {
		CMDInfo cd = getColumnData(column);
		return cd.m_column_class_name;
	}

	private static class CMDInfo {
		boolean m_auto_increment;
		boolean m_case_sensitive;
		boolean m_searchable;
		boolean m_currency;
		int m_nullable;
		boolean m_signed;
		int m_column_display_size;
		String m_column_label;
		String m_column_name;
		String m_schem_name;
		int m_precision;
		int m_scale;
		String m_table_name;
		String m_catalog_name;
		int m_column_type;
		String m_column_type_name;
		boolean m_read_only;
		boolean m_writable;
		boolean m_definitely_writable;
		String m_column_class_name;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
}
