package com.jeta.abeille.gui.sql;

import java.io.InputStream;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.ColumnId;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceProxy;

import com.jeta.foundation.gui.table.TableSorter;

/**
 * This class provides instance proxy for dealing with a query result set
 * 
 * @author Jeff Tassin
 */
public class QueryInstanceProxy implements InstanceProxy {
	private int m_currentrow;

	/**
	 * the table model for the query results view. We absolutely need this
	 * because the user can reorder the rows/columns and the instance view
	 * should reflect this when launched
	 */
	private TableSorter m_tablemodel;

	/**
	 * the table for the query results view. Eventhough the query results allows
	 * split tables, the instance view will be based on only *one* of those
	 * tables. We need this table to get the TableColumn so we can get the
	 * correct indices in case the user reordered the columns after the instance
	 * view is visible
	 */
	private JTable m_table;

	private InstanceModel m_instancemodel;
	private SQLResultsModel m_sqlmodel;

	/**
	 * this is a has map of ColumnId (key) and TableColumn objects. This allows
	 * us to get the TableColumn quickly when the user calls setValue
	 */
	private HashMap m_columns;

	/**
	 * ctor.
	 * 
	 * @param table
	 *            this table must have a TableSorter as its table model
	 */
	public QueryInstanceProxy(SQLResultsModel sqlmodel, InstanceModel imodel, JTable table, int currentRow) {
		m_sqlmodel = sqlmodel;
		m_instancemodel = imodel;
		m_table = table;
		m_tablemodel = (TableSorter) table.getModel();
		assert (m_table != null);
		assert (m_tablemodel != null);
		m_currentrow = currentRow;

		m_columns = new HashMap();
		TableColumnModel colmodel = m_table.getColumnModel();
		for (int index = 0; index < colmodel.getColumnCount(); index++) {
			TableColumn col = colmodel.getColumn(index);
			ColumnMetaData cmd = (ColumnMetaData) col.getIdentifier();
			m_columns.put(cmd.getColumnId(), col);
		}

	}

	/**
	 * @return the binary stream for the given column. Note that the column must
	 *         be a binary object
	 */
	public byte[] getBinaryData(String columnName) throws SQLException {
		QueryResultSet qset = m_sqlmodel.getQueryResultSet();
		return qset.getBinaryData(columnName);
	}

	/**
	 * @return the clob data for the given column. Note that the column must be
	 *         a clob object
	 */
	public String getClobData(String columnName) throws SQLException {
		QueryResultSet qset = m_sqlmodel.getQueryResultSet();
		return qset.getClobData(columnName);
	}

	public boolean isFirst() throws SQLException {
		return (m_currentrow == 0);

	}

	public boolean isLast() throws SQLException {
		return (m_currentrow + 1 >= m_tablemodel.getRowCount());

	}

	public boolean isEmpty() throws SQLException {
		return (getRowCount() == 0);
	}

	public boolean first() throws SQLException {
		m_currentrow = 0;

		if (isEmpty())
			return false;
		else
			return true;
	}

	public int getRow() throws SQLException {
		return m_currentrow;

	}

	public int getRowCount() {
		return m_tablemodel.getRowCount();

	}

	/**
	 * @return the maximum row downloaded so far. The will change for large
	 *         result sets
	 */
	public int getMaxRow() {
		return getRowCount();
	}

	public boolean isScrollable() throws SQLException {
		return m_sqlmodel.isScrollable();
	}

	public boolean last() throws SQLException {
		int rowcount = getRowCount();
		if (rowcount == 0) {
			m_currentrow = 0;
			return false;
		} else {
			m_currentrow = rowcount - 1;
			return true;
		}
	}

	public boolean next() throws SQLException {
		if (!isLast()) {
			m_currentrow++;
			return true;
		} else
			return false;
	}

	public boolean previous() throws SQLException {
		if (!isFirst()) {
			m_currentrow--;
			return true;
		} else
			return false;
	}

	/**
	 * Sets the value of the given instance component with the value associated
	 * with the given column meta data
	 */
	public void setValue(InstanceComponent comp, ColumnMetaData cmd) throws SQLException {
		TableColumn col = (TableColumn) m_columns.get(cmd.getColumnId());
		if (col == null) {
			System.out.println("unable to get table column for: " + cmd);
			cmd.print();

			Iterator iter = m_columns.keySet().iterator();
			while (iter.hasNext()) {
				ColumnId cid = (ColumnId) iter.next();
				String key = cid.getFullyQualifiedName();
				String setcol = cmd.getColumnId().getFullyQualifiedName();
				if (key.equalsIgnoreCase(setcol)) {
					col = (TableColumn) m_columns.get(cid);
					break;
				}
			}
		}
		comp.setValue(m_tablemodel.getValueAt(m_currentrow, col.getModelIndex()));
	}

}
