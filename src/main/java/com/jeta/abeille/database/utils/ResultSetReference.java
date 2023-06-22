package com.jeta.abeille.database.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.LinkedList;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a reference counted sql ResultSet and Statement. In the
 * application, we allow multiple views for a given result set. So, we need a
 * way to cleanly close the result set when the last view is closed. We use this
 * reference counted class to handle that. When the ref count goes to zero, we
 * close the connection. This puts the responsibility on the user of this class
 * to call addRef, releaseRef at the appropriate times. Furthermore, some views
 * may make modifications and commits/rollbacks with the given connection. This
 * class allows other users to get notified of these events and react
 * accordingly.
 * 
 * @author Jeff Tassin
 */
public class ResultSetReference {
	private Statement m_statement;
	private ResultSet m_rset;
	private String m_sql;
	private ConnectionReference m_connectionref;

	/**
	 * Caches the metadata for the result set. This is needed in case the
	 * resultset is closed but we still need to access the set data. This can
	 * happen when we want to view multiple resultsets for a statement but the
	 * database does not support this.
	 */
	private ResultSetMetaDataCache m_metadatacache;

	/**
	 * Some sql statements might be in a form similar to a PreparedStatement
	 * with ? for inputs (which we allow). If this is the case, this variable is
	 * this sql with the ? and not any constraints.
	 */
	private String m_rawsql;


	private ResultSetReference() {
	}

	/**
	 * ctor
	 */
	private ResultSetReference(Statement stmt, ResultSet rset, String sql) {
		m_connectionref = null;
		m_statement = stmt;
		m_rset = rset;
		m_sql = sql;
	}

	/**
	 * ctor
	 */
	public ResultSetReference(ConnectionReference ref, Statement stmt, ResultSet rset, String sql) {
		m_connectionref = ref;
		m_statement = stmt;
		m_rset = rset;
		m_sql = sql;
	}

	public static ResultSetReference build( TSConnection tsconn, String sql, String rawSql) throws SQLException {
		Statement stmt = tsconn.createStatement();
		ResultSetReference rref = new ResultSetReference(new ConnectionReference(tsconn, stmt.getConnection()), stmt, stmt.executeQuery(sql), sql);
		rref.setUnprocessedSQL(rawSql);
		return rref;
	}

	public static ResultSetReference empty( TSConnection tsconn ) throws SQLException {
		ResultSetReference rref = new ResultSetReference();
		rref.m_rawsql = "select * from (select 0 as col1 from dual) ALWAYS_EMPTY where ALWAYS_EMPTY.col1 = 1";
		rref.m_sql = rref.m_rawsql;
		rref.m_connectionref = new ConnectionReference(tsconn, tsconn.getWriteConnection());
		rref.m_rset = new EmptyResultSet();
		return rref;
	}

	/**
	 * Caches the metadata for the result set. This is needed in case the
	 * resultset is closed but we still need to access the set data. This can
	 * happen when we want to view multiple resultsets for a statement but the
	 * database does not support this.
	 */
	public synchronized void cacheMetaData() throws SQLException {
		if (m_metadatacache == null) {
			ResultSetMetaData metadata = getMetaData();
			if (metadata != null) {
				m_metadatacache = new ResultSetMetaDataCache(m_connectionref.getTSConnection(), metadata);
			}
		}
	}

	/**
	 * Closes the connection. Notifies any listener that the resultset/statement
	 * was closed. We allow any caller to close the connection even if the ref
	 * count is not zero. This allows other users of the same connection
	 * reference to handle the closure accordingly.
	 */
	public void close() throws SQLException {
		m_connectionref = null;
	}

	/**
	 * For debugging
	 */
	protected void finalize() throws Throwable {
		super.finalize();

		m_statement = null;
		m_rset = null;
		m_connectionref = null;
		m_metadatacache = null;

		if (TSUtils.isDebug()) {
			// TSUtils.printDebugMessage("ResultSetReference.finalize: " + m_sql);
		}
	}

	/**
	 * @return the underlying reference to the connection
	 */
	public ConnectionReference getConnectionReference() {
		return m_connectionref;
	}

	/**
	 * @return the underlying result set metdata. Do not cache this result since
	 *         it could be closed underneath you. Null is returned if the result
	 *         set is invalid
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		if (m_metadatacache != null) {
			return m_metadatacache;
		} else if (m_rset != null)
			return m_rset.getMetaData();
		else
			return null;
	}

	/**
	 * @return the actual sql (with the question mark inputs).
	 */
	public String getUnprocessedSQL() {
		return m_rawsql;
	}

	/**
	 * @return the underlying result set. Do not cache this result since it
	 *         could be closed underneath you.
	 */
	public ResultSet getResultSet() {
		return m_rset;
	}

	/**
	 * @return the underlying sql for the result
	 */
	public String getSQL() {
		return m_sql;
	}

	/**
	 * Sets the underlying sql that is the basis for this result set
	 */
	public void setSQL(String sql) {
		m_sql = sql;
	}

	/**
	 * Sets the actual sql (with the question mark inputs).
	 */
	public void setUnprocessedSQL(String sql) {
		m_rawsql = sql;
	}

}
