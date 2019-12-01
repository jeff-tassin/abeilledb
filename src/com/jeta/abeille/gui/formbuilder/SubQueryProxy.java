package com.jeta.abeille.gui.formbuilder;

import java.io.StringWriter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.SQLFormatter;
import com.jeta.abeille.database.utils.RowInstance;

import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.abeille.query.Operator;

import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class SubQueryProxy extends ValueProxy {
	private ColumnMetaData m_formcolumn;
	private SubQuery m_subquery;

	/**
	 * ctor
	 * 
	 * @param subquery
	 *            the subquery that will get the value used as a basis for the
	 *            update
	 * @param formColumn
	 *            the column in the form(achortable) that will recieve the value
	 * @param linkedColumn
	 *            the column that is linked to the form and is used in the query
	 */
	public SubQueryProxy(SubQuery subquery, ColumnMetaData formColumn, ColumnMetaData linkedColumn) {
		super(linkedColumn, null);
		m_subquery = subquery;
		m_formcolumn = formColumn;
	}

	/**
	 * Gets the result of a subquery and prepares it for a subsequent update
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		QueryResultSet qset = m_subquery.getQueryResults();

		qset.first();
		if (qset.isEmpty()) {
			// resultset is empty. this is an error, we can't continue
			StringWriter writer = new StringWriter();
			writer.write(I18N.format("SubQuery_for_column_returned_no_results_1", m_formcolumn.getQualifiedName()));
			writer.write("\n");
			// m_subquery.prepareDebugStatement( formatter, writer );
			throw new SQLException(writer.toString());
		} else {
			int rowcount = qset.getRowCount();
			if (rowcount == 1) {
				// one result has been returned, this is correct
				ColumnMetaData linkedcolumn = getColumnMetaData();
				int columnindex = m_subquery.getResultSetIndex(linkedcolumn);
				RowInstance rowinstance = qset.getRowInstance(0);
				Object value = rowinstance.getObject(columnindex);
				pstmt.setObject(count, value);
			} else {
				// more that one result has been returned, this is an error
				if (rowcount == -1)
					rowcount = qset.getMaxRow();

				StringWriter writer = new StringWriter();
				writer.write(I18N.format("SubQuery_for_column_returned_multiple_results_2",
						m_formcolumn.getQualifiedName(), new Integer(rowcount)));
				writer.write("\n");
				throw new SQLException(writer.toString());
			}
		}
	}

	/**
	 * Shows the form plan
	 */
	public String getPlan(TSConnection conn, SQLFormatter formatter) throws SQLException {
		return m_subquery.getPlan(conn, formatter);
	}
}
