package com.jeta.abeille.gui.queryresults;

import com.jeta.abeille.database.model.*;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.database.utils.RowInstance;
import com.jeta.foundation.utils.TSUtils;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is used to represent a result set for a SQL query in a JTable.
 * 
 * @author Jeff Tassin
 */
public class QueryResultsModel extends AbstractTableModel {
	/** array of column metadata objects */
	private ColumnMetaData[] m_columns;

	/** this is the object that contains the data */
	private QueryResultSet m_queryresults;

	/** the underlying connection manager */
	private TSConnection m_tsconnection;

	/**
	 * this is a special case. Many queries are against a single table. If this
	 * is the case, this is the table id. We allow the user to directly edit the
	 * data for this case
	 */
	private TableId m_tableid;

	/**
	 * If m_tableid is not null, then this is the table metadata for that table
	 * id
	 */
	private TableMetaData m_tmd;


	/**
	 * ctor
	 */
	public QueryResultsModel(TSConnection tsconn, ResultSetReference ref) throws SQLException {
		this(tsconn, ref, null);
	}

	/**
	 * ctor
	 */
	public QueryResultsModel(TSConnection tsconn, QueryResultSet qset) throws SQLException {
		this(tsconn, qset, null);
	}

	/**
	 * ctor
	 */
	public QueryResultsModel(TSConnection tsconn, ResultSetReference ref, TableId tableId) throws SQLException {
		Catalog catalog = null;

		m_tsconnection = tsconn;
		m_tableid = tableId;
		if (ref == null) {
			m_columns = new ColumnMetaData[0];
		} else {
			ResultSetMetaData metadata = ref.getMetaData();
			if (m_tableid != null)
				catalog = m_tableid.getCatalog();
			m_columns = DbUtils.createColumnMetaData(m_tsconnection, catalog, metadata);
		}

		m_queryresults = new QueryResultSet(catalog, ref);
	}

	/**
	 * ctor
	 */
	public QueryResultsModel(TSConnection tsconn, QueryResultSet qset, TableId tableId) throws SQLException {

		m_tsconnection = tsconn;
		m_tableid = tableId;
		if (qset == null) {
			m_columns = new ColumnMetaData[0];
		} else {
			Catalog catalog = null;

			ResultSetMetaData metadata = qset.getMetaData();
			if (m_tableid != null)
				catalog = m_tableid.getCatalog();
			m_columns = DbUtils.createColumnMetaData(m_tsconnection, catalog, metadata);
		}

		m_queryresults = qset;
	}

	/**
	 * @return the class type of the column at the given index. We call
	 *         getValueAt because we allow the JDBC driver to specify the class
	 *         type.
	 */
	public Class getColumnClass(int col) {
		if (m_queryresults.isEmpty())
			return String.class;
		else {
			Object val = getValueAt(0, col);
			if (val == null) {
				return Object.class;
			} else
				return val.getClass();
		}
	}

	/**
	 * @return the column metadata associated with the given column index. This
	 *         class automatically creates column metadata objects based on the
	 *         result set metadata.
	 * @param col
	 *            the index of the column to retrieve the column metadata
	 * @return the column metadata object
	 * 
	 */
	public ColumnMetaData getColumnMetaData(int col) {
		return m_columns[col];
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_columns.length;
	}


	/**
	 * @return the underlying query result set
	 */
	public QueryResultSet getQueryResultSet() {
		return m_queryresults;
	}

	/**
	 * @return the underlying connection reference
	 */
	public ConnectionReference getConnectionReference() {
		ResultSetReference ref = getResultSetReference();
		if (ref != null)
			return ref.getConnectionReference();
		else
			return null;
	}

	/**
	 * @return the underlying result set reference object. Warning, you must
	 *         call addRef on this reference if you intend to use it for any
	 *         length of time
	 */
	public ResultSetReference getResultSetReference() {
		return m_queryresults.getResultSetReference();
	}

	/**
	 * @return the current row of the result set (this is 0 based)
	 */
	public int getRow() throws SQLException {
		return m_queryresults.getRow();
	}

	public RowInstance getRowInstance(int row) throws SQLException {
		return m_queryresults.getRowInstance(row);
	}

	/**
	 * AbstractTableModel implementation
	 * 
	 * @return the number of rows currently in the model. Note: This might not
	 *         equal the total number of rows in the results set, because we
	 *         might not of downloaded all the results yet
	 */
	public int getRowCount() {
		int result = 0;
		if (m_queryresults.isRowCountKnown()) {
			result = m_queryresults.getRowCount();
		} else {
			result = m_queryresults.getMaxRow() + 1;
		}

		// System.out.println( "QueryResultsModel.getRowCount: " + result +
		// "  rowcountknown: " + m_queryresults.isRowCountKnown() );
		return result;
	}

	/**
	 * AbstractTableModel implementation
	 */
	public String getColumnName(int column) {
		ColumnMetaData cmd = m_columns[column];
		String colname = cmd.getAlias();
		if (colname == null || colname.trim().length() == 0)
			colname = cmd.getColumnName();

		return colname;
	}

	/**
	 * @return the underlying SQL
	 */
	public String getSQL() {
		return m_queryresults.getSQL();
	}

	/**
	 * @return a table id. If this query results is based on a single table,
	 *         then we return a valid reference to that table id here. If not,
	 *         then null is returned.
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * If this query results model represents data from a single table AND that
	 * table has a primary key, then that key will be returned. Otherwise null
	 * is returned.
	 */
	public DbKey getPrimaryKey() {
		TableId id = getTableId();
		if (id != null) {
			if (m_tmd == null)
				m_tmd = m_tsconnection.getModel(id.getCatalog()).getTable(id);

			if (m_tmd != null)
				return m_tmd.getPrimaryKey();
		}

		return null;
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getTSConnection() {
		return m_tsconnection;
	}

	/**
	 * @return the value for the given row and column
	 * 
	 */
	public Object getValueAt(int row, int column) {
		try {
			// do this first because the query results model might have to pull
			// in more data
			// into the cache and change the maxrow/isRowCountKnown attributes
			int lastrowcount = m_queryresults.getMaxRow();
			boolean is_row_count_known = m_queryresults.isRowCountKnown();

			// later, let's return the row instance, so we can test for
			// isModified flag
			// and set the row a different color for modified rows
			RowInstance rowinstance = m_queryresults.getRowInstance(row);
			if (TSUtils.isDebug() && rowinstance == null) {
				TSUtils.printMessage("QueryResultsModel.getValuAt rowinstance is null for row: " + row);
				assert (false);
			}

			if (!is_row_count_known && row == lastrowcount) {
				// this is the last row in the table, so let's tell the cache to
				// update itself
				RowInstance instance = m_queryresults.getRowInstance(row + 1);
				if (lastrowcount < m_queryresults.getMaxRow()) {
					final int firstrow = row + 1;
					final int lastrow = m_queryresults.getMaxRow();
					fireTableRowsInserted(firstrow, lastrow);
				}
			}

			return rowinstance.getObject(column);
		} catch (Exception e) {
			TSUtils.printException(e);
			return null;
		}
	}

	/**
	 * @return the value for the given row and column name
	 *
	 */
	public Object getValueAt(int row, String cname) {
		int col = 0;
		for( ; col < m_columns.length; col++ ) {
			if (Objects.equals(m_columns[col].getColumnName(), cname)) {
				break;
			}
		}
		if ( col < m_columns.length )
			return getValueAt(row, col );
		else
			return null;
	}

	/**
	 * @return true, if this model has no query results
	 */
	public boolean isEmpty() {
		if (m_queryresults == null)
			return true;
		else
			return m_queryresults.isEmpty();
	}

	/**
	 * @return true if we know the total number of rows in the query result set
	 *         For large sets, this might be an unknown until all data has been
	 *         downloaded
	 */
	public boolean isRowCountKnown() {
		return m_queryresults.isRowCountKnown();
	}

	
	/**
	 * @return true if the resultset is scrollable
	 */
	public boolean isScrollable() {
		return m_queryresults.isScrollable();
	}

	/**
	 * Tells the query results to load the last instance and moves the cursor to
	 * that position
	 */
	public void last() throws SQLException {
		m_queryresults.last();
		fireTableChanged(new TableModelEvent(this));
	}


}
