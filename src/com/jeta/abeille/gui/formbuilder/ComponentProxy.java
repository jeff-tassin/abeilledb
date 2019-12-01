package com.jeta.abeille.gui.formbuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.update.InstanceComponent;

import com.jeta.abeille.query.Operator;

public class ComponentProxy extends ValueProxy {
	private InstanceComponent m_component;

	public ComponentProxy(ColumnMetaData formColumn, InstanceComponent comp, Operator op) {
		super(formColumn, op);
		m_component = comp;
	}

	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		m_component.prepareStatement(count, pstmt, formatter);
	}

	public String getPlan(TSConnection conn, SQLFormatter formatter) throws SQLException {
		return m_component.toSQLString(formatter);
	}
}
