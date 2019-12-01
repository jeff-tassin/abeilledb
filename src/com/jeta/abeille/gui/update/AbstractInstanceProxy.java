package com.jeta.abeille.gui.update;

import java.sql.SQLException;
import java.sql.ResultSet;

import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

/**
 * This class provides instance proxy for dealing with a query result set
 * 
 * @author Jeff Tassin
 */
public abstract class AbstractInstanceProxy implements InstanceProxy {
	private QueryResultSet m_qset;

	/**
	 * ctor
	 */
	public AbstractInstanceProxy(QueryResultSet qset) {
		m_qset = qset;
	}

	/**
	 * @return the result set for the last query. This can be null if no query
	 *         was performed or the user cleared the form.
	 */
	public QueryResultSet getQueryResults() {
		return m_qset;
	}

	public boolean isFirst() throws SQLException {
		return m_qset.isFirst();

	}

	public boolean isLast() throws SQLException {
		return m_qset.isLast();

	}

	public boolean isEmpty() throws SQLException {
		return m_qset.isEmpty();

	}

	public boolean isScrollable() throws SQLException {
		return m_qset.isScrollable();
	}

	public boolean first() throws SQLException {
		return m_qset.first();
	}

	/**
	 * @return the binary stream for the given column. Note that the column must
	 *         be a binary object
	 */
	public byte[] getBinaryData(String columnName) throws SQLException {
		return m_qset.getBinaryData(columnName);
	}

	/**
	 * @return the clob data for the given column. Note that the column must be
	 *         a clob type
	 */
	public String getClobData(String columnName) throws SQLException {
		return m_qset.getClobData(columnName);
	}

	public int getRow() throws SQLException {
		return m_qset.getRow();

	}

	public int getRowCount() {
		return m_qset.getRowCount();

	}

	/**
	 * @return the maximum row downloaded so far. The will change for large
	 *         result sets
	 */
	public int getMaxRow() {
		return m_qset.getMaxRow();
	}

	public boolean last() throws SQLException {
		return m_qset.last();
	}

	public boolean next() throws SQLException {
		if (!isLast())
			return m_qset.next();
		else
			return false;
	}

	public boolean previous() throws SQLException {
		if (!isFirst())
			return m_qset.previous();
		else
			return false;
	}

}
