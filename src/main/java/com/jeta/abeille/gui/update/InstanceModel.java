package com.jeta.abeille.gui.update;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.i18n.I18N;

import com.jeta.plugins.abeille.mysql.MySQLTableType;
import com.jeta.plugins.abeille.mysql.MySQLUtils;

import com.jeta.foundation.utils.TSUtils;

/**
 * This model represents the columns that are displayed in the InstanceView.
 * Note this model is for *visible* columns only
 * 
 * @author Jeff Tassin
 */
public class InstanceModel {
	/**
	 * the underlying database connection manager
	 */
	private TSConnection m_connection;

	/**
	 * if the user invokes a add/modify/delete, then the model first acquire a
	 * writable connection (for commit/rollback support). This reference is null
	 * until the user invokes the action. Normally, we have a standard read only
	 * connection
	 */
	private ConnectionReference m_writeconnection = null;

	/** flag that determines if the data needs to be commited or not */
	private boolean m_commit = false;

	public static final int BUSY = 1;
	public static final int READY = 2;

	/** the current state of the model */
	private int m_state = READY;

	/** holds the instance metadata settings */
	private InstanceMetaData m_metadata;

	/**
	 * An array of ColumnSettings objects that correspond to the columns in this
	 * model
	 */
	private ArrayList m_columns = new ArrayList();

	/**
	 * The instance proxy
	 */
	private InstanceProxy m_instanceproxy;

	/** flag that indicates if this instance view can update the data */
	private boolean m_allowsupdates = true;

	/**
	 * ctor
	 */
	public InstanceModel(TSConnection connection, InstanceMetaData metadata) {
		m_connection = connection;
		m_metadata = metadata;
		reload();
	}

	/**
	 * @return true if the user can modify the data in the model
	 */
	public boolean allowsUpdates() {
		return m_allowsupdates;
	}

	/**
	 * Commits the write connection if it can be commited. Also, resets the
	 * commit status flag. Furthermore, releases the write connection back to
	 * the TSConnection object so that other objects can use it.
	 */
	synchronized public void commit() throws SQLException {
		assert (m_writeconnection != null);
		if (m_writeconnection != null) {
			m_writeconnection.commit();
			// m_connection.release( m_writeconnection );
			m_writeconnection = null;
			setCommitStatus(false);
		}
	}

	/**
	 * Closed the model and any opened connections
	 */
	synchronized void close() {
		if (m_writeconnection != null) {
			// m_connection.release( m_writeconnection );
			m_writeconnection = null;
		}
	}

	/**
	 * @return the underyling database connection mgr
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the object that is responsible for setting the values of the
	 *         various columns in the view
	 */
	public InstanceProxy getInstanceProxy() {
		return m_instanceproxy;
	}

	/**
	 * @return a writeable connection that we can rollback/commit
	 */
	synchronized public ConnectionReference getWriteConnection() throws SQLException {
		if (m_writeconnection == null) {
			Connection conn = m_connection.getWriteConnection();
			m_writeconnection = new ConnectionReference(m_connection, conn);
		}
		return m_writeconnection;
	}

	/**
	 * @return the number of columns in the model. (in this case the number of
	 *         columns in the underlying table )
	 * 
	 */
	public int getColumnCount() {
		return m_columns.size();
	}

	/**
	 * @return the ColumnMetaData object at the given index.
	 */
	public ColumnMetaData getColumn(int index) {
		ColumnSettings info = (ColumnSettings) m_columns.get(index);
		return info.getColumnMetaData();
	}

	/**
	 * @return an collection of the visible columns in this model
	 */
	public Collection getColumns() {
		LinkedList results = new LinkedList();
		for (int index = 0; index < getColumnCount(); index++)
			results.add(getColumn(index));

		return results;
	}

	/**
	 * Returns the display options for a given column. This is mainly used to
	 * determine the handler of a given column.
	 * 
	 * @param cmd
	 *            the column meta data which to retrieve the given option info
	 * @return the display options for the given column name
	 */
	public ColumnSettings getColumnSettings(ColumnMetaData cmd) {
		// @todo we need to cache this to make it more efficient
		Iterator iter = m_columns.iterator();
		while (iter.hasNext()) {
			ColumnSettings info = (ColumnSettings) iter.next();
			if (info.equals(cmd))
				return info;
		}

		return null;
	}

	/**
	 * Returns the display options for a given column. This is mainly used to
	 * determine the handler of a given column.
	 * 
	 * @param columnName
	 *            the name of the column which to retrieve the given option info
	 * @return the display options for the given column name
	 */
	private ColumnSettings getColumnSettings(String columnName) {
		// @todo we need to cache this to make it more efficient
		Iterator iter = m_columns.iterator();
		while (iter.hasNext()) {
			ColumnSettings info = (ColumnSettings) iter.next();
			if (I18N.equals(columnName, info.getColumnName()))
				return info;
		}
		return null;
	}

	/**
	 * @return the metadata object for this instance
	 */
	public InstanceMetaData getMetaData() {
		return m_metadata;
	}

	/**
	 * @return a unique identifier for this model. If this is a table model,
	 *         then the UID is basically the schema.tablename. If this is a
	 *         query result, then the UID is the query string. If this is from a
	 *         saved query, then this is the UID of the saved query
	 */
	public String getUID() {
		return m_metadata.getUID();
	}

	/**
	 * @return the state of the model
	 */
	public int getState() {
		return m_state;
	}

	/**
	 * @return true if the given column name is a primary key
	 */
	public boolean isPrimaryKey(ColumnMetaData cmd) {
		return m_metadata.isPrimaryKey(cmd);
	}

	private void reload() {
		m_columns = new ArrayList();
		for (int index = 0; index < m_metadata.getColumnCount(); index++) {
			ColumnSettings setting = m_metadata.getColumnSettings(index);
			if (setting.isVisible()) {
				m_columns.add(setting);
			}
		}

	}

	/**
	 * Releases any existing connections
	 */
	synchronized public void resetConnection() {
		try {
			rollback();
			setInstanceProxy(null);
		} catch (Exception e) {
			// eat here
			TSUtils.printException(e);
		}
		close();
	}

	/**
	 * Rollsback the write connection if it can be commited. Also, resets the
	 * commit status flag. Furthermore, releases the write connection back to
	 * the TSConnection object so that other objects can use it.
	 */
	synchronized public void rollback() throws SQLException {
		assert (m_writeconnection != null);
		if (m_writeconnection != null) {
			m_writeconnection.rollback();
			m_writeconnection = null;
			setCommitStatus(false);
			setInstanceProxy(null);
		}
	}

	/**
	 * Sets the flag that indicates if the user can modify the data in the model
	 */
	public void setAllowsUpdates(boolean allows) {
		m_allowsupdates = allows;
	}

	/**
	 * Sets the commit status flag
	 */
	private void setCommitStatus(boolean bcommit) {
		m_commit = bcommit;
	}

	/**
	 * Sets the object used to get data from an instance for a given column
	 */
	public void setInstanceProxy(InstanceProxy proxy) {
		m_instanceproxy = proxy;
	}

	public void setMetaData(InstanceMetaData imetadata) {
		assert (imetadata != null);
		m_metadata = imetadata;
		reload();
	}

	/**
	 * flag that determines if the data needs to be commited
	 */
	public void setModified() {
		setCommitStatus(true);
	}

	/**
	 * Sets the state of the model
	 */
	void setState(int state) {
		m_state = state;
	}

	/**
	 * flag that determines if we whould commit
	 */
	public boolean shouldCommit() {
		return m_commit;
	}

	/**
	 * @return true if the database connection for this view supports
	 *         transactions
	 */
	public boolean supportsTransactions() {
		try {
			if (m_connection.isAutoCommit()) {
				return false;
			} else if (m_metadata instanceof TableInstanceMetaData) {
				/**
				 * if we are running MySQL, then we need to check the table
				 * type. Only InnoDb supports transactions
				 */
				if (m_connection.getDatabase().equals(Database.MYSQL)) {
					TableInstanceMetaData timd = (TableInstanceMetaData) m_metadata;
					TableId tableid = timd.getTableId();
					TableMetaData tmd = m_connection.getModel(tableid.getCatalog()).getTableEx(tableid,
							TableMetaData.LOAD_ALL);
					MySQLTableType ttype = MySQLUtils.getTableType(tmd);
					if (ttype.equals(MySQLTableType.InnoDB)) {
						return true;
					}
				}
			}
			TSDatabase db = (TSDatabase) m_connection.getImplementation(TSDatabase.COMPONENT_ID);
			return db.supportsTransactions();
		} catch (Exception e) {
			TSUtils.printException(e);
		}
		return false;
	}
}
