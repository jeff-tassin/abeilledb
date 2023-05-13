package com.jeta.abeille.gui.sql;

import java.sql.SQLException;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.queryresults.QueryResultsModel;

/**
 * We specialize QueryResultsModel here so that we can provide management of an
 * associated instance view. The application allows various views of the same
 * data. Specifically, we allow either an instance view or tabular view (query
 * results). Each of these views can launch the other and will typically
 * maintain a reference to the window that it launched so that if launched
 * again, it can simply bring the window to the top of the z-order rather than
 * create another one.
 * 
 * @author Jeff Tassin
 */
public class SQLResultsModel extends QueryResultsModel {
	/**
	 * Settings specific to this sql string
	 */
	private SQLSettings m_settings;

	/**
	 * ctor
	 */
	public SQLResultsModel(TSConnection tsconn, ResultSetReference ref, TableId tableId) throws SQLException {
		super(tsconn, ref, tableId);
	}

	/**
	 * ctor
	 */
	public SQLResultsModel(TSConnection tsconn, QueryResultSet qset, TableId tableId) throws SQLException {
		super(tsconn, qset, tableId);
	}

	/**
	 * @return the user settings object for this sql results
	 */
	public SQLSettings getSettings() {
		return m_settings;
	}

	/**
	 * @return the sql string with any input ? parameters unset.
	 */
	public String getUnprocessedSQL() {
		ResultSetReference rref = getResultSetReference();
		if (rref != null)
			return rref.getUnprocessedSQL();
		else
			return "";
	}

	/**
	 * Sets the sql settings
	 */
	public void setSettings(SQLSettings settings) {
		m_settings = settings;
	}
}
