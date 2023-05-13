package com.jeta.abeille.gui.importer;

import java.io.StringWriter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.RowInstance;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.formbuilder.SubQuery;
import com.jeta.abeille.gui.formbuilder.SubQueryProxy;
import com.jeta.abeille.gui.formbuilder.ValueProxy;

import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.abeille.query.Operator;

import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class ImporterSubQueryProxy extends ValueProxy {
	private ConnectionReference m_connectionref;
	private SubQuery m_subquery;
	private SubQueryProxy m_subqueryproxy;

	/**
	 * ctor
	 * 
	 * @param subquery
	 *            the subquery that will get the value used as a basis for the
	 *            update
	 * @param formColumn
	 *            the column in the target table list will recieve the value
	 * @param linkedColumn
	 *            the column that is linked to the target and is used in the
	 *            query
	 */
	public ImporterSubQueryProxy(ConnectionReference cref, SubQuery subquery, ColumnMetaData targetCol,
			ColumnMetaData linkedColumn) {
		super(targetCol, null);
		m_connectionref = cref;
		m_subquery = subquery;
		m_subqueryproxy = new SubQueryProxy(subquery, targetCol, linkedColumn);
	}

	/**
	 * Gets the result of a subquery and prepares it for a subsequent update
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		m_subquery.executeQuery(m_connectionref, formatter);
		m_subqueryproxy.prepareStatement(count, pstmt, formatter);
	}

	/**
	 * Shows the form plan
	 */
	public String getPlan(TSConnection conn, SQLFormatter formatter) throws SQLException {
		return "";
	}
}
