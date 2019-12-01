package com.jeta.abeille.gui.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.SQLFormatter;
import com.jeta.abeille.database.utils.RowInstance;

import com.jeta.abeille.gui.formbuilder.ValueProxy;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

public class ImportValueProxy extends ValueProxy {
	private QueryResultSet m_rset;

	private TargetColumnInfo m_targetinfo;

	/**
	 * ctor
	 */
	public ImportValueProxy(QueryResultSet qset, TargetColumnInfo info) {
		super(info.getTarget(), null);
		m_rset = qset;
		m_targetinfo = info;
	}

	/**
	 * Prepares a prepared statement for a given column Note: this does not
	 * support BINARY types yet
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		SourceColumn srccol = m_targetinfo.getSourceColumn();
		RowInstance rowinstance = m_rset.getRowInstance(m_rset.getRow());
		assert (rowinstance != null);
		Object value = rowinstance.getObject(srccol.getIndex());
		if (value == null) {
			pstmt.setNull(count, srccol.getType());
		} else {
			pstmt.setObject(count, value);
		}
	}

	public String getPlan(TSConnection conn, SQLFormatter formatter) throws SQLException {
		return "";
	}

}
