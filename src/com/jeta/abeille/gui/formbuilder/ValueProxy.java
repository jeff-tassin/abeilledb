package com.jeta.abeille.gui.formbuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.SQLFormatter;
import com.jeta.abeille.query.Operator;

public abstract class ValueProxy {
	private ColumnMetaData m_cmd;
	private Operator m_op;

	/**
	 * ctor
	 */
	public ValueProxy(ColumnMetaData cmd, Operator op) {
		m_cmd = cmd;
		m_op = op;
	}

	/**
	 * This method returns the column meta data that forms the basis of the
	 * constrainer
	 */
	public ColumnMetaData getColumnMetaData() {
		return m_cmd;
	}

	/**
	 * @return the operator for this proxy
	 */
	public Operator getOperator() {
		return m_op;
	}

	/**
	 * Prepares a prepared statement for a given column
	 */
	public abstract void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter)
			throws SQLException;

	public abstract String getPlan(TSConnection conn, SQLFormatter formatter) throws SQLException;

}
