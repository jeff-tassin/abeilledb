package com.jeta.abeille.gui.formbuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.update.InstanceComponent;

import com.jeta.abeille.query.Operator;

/**
 * This is used when running a sub query for the form builder view. It provides
 * the constraint value for foreign keys in the sub query.
 * 
 * @author Jeff Tassin
 */
public class LinkProxy extends ValueProxy {
	private ColumnMetaData m_formcolumn;
	private InstanceComponent m_component;

	public LinkProxy(ColumnMetaData formColumn, ColumnMetaData linkedColumn, InstanceComponent comp, Operator op) {
		super(linkedColumn, op);
		m_formcolumn = formColumn;
		m_component = comp;
	}

	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		m_component.prepareStatement(count, pstmt, formatter);
	}

	public String getPlan(TSConnection conn, SQLFormatter formatter) throws SQLException {
		return "";
	}
}
