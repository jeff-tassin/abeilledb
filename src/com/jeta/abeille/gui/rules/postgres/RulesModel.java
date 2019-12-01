package com.jeta.abeille.gui.rules.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * @author Jeff Tassin
 */
public class RulesModel extends AbstractTableModel {
	/** an array of Trigger object */
	private ArrayList m_data;

	/** an array of column names for the table */
	private String[] m_colnames;

	/** an array of column types for the table */
	private Class[] m_coltypes;

	/** The database connection */
	private TSConnection m_connection;

	/** The id of the table we are showing the triggers for */
	private TableId m_tableid;

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int EXPRESSION_COLUMN = 1;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public RulesModel(TSConnection connection, TableId tableId) {
		super();

		m_connection = connection;
		m_tableid = tableId;
		m_data = new ArrayList();

		String[] values = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Rule") };

		m_colnames = values;
		Class[] types = { String.class, String.class };
		m_coltypes = types;
	}

	/**
	 * Adds the given trigger object to the table
	 */
	public void addRow(Rule check) {
		if (m_data == null)
			m_data = new ArrayList();

		m_data.add(check);
		fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
	}

	/**
	 * Creates the given rule in the database
	 * 
	 * @param rule
	 *            the Rule to create
	 */
	public void createRule(Rule rule) throws SQLException {
		SQLCommand.runMetaDataCommand(m_connection, I18N.format("Creating_1", rule.getName()), rule.getExpression());
	}

	/**
	 * Drops the given rule in the database
	 * 
	 * @param rule
	 *            the Rule to drop
	 */
	public void dropRule(Rule rule, boolean cascade) throws SQLException {
		SQLCommand.runMetaDataCommand(m_connection, I18N.format("Drop_1", rule.getName()),
				getDropSQL(rule, m_tableid, cascade));
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the SQL needed to drop the given rule
	 */
	private String getDropSQL(Rule rule, TableId tableId, boolean cascade) {
		StringBuffer sql = new StringBuffer();
		sql.append("DROP RULE ");
		sql.append(rule.getName());
		if (m_connection.supportsSchemas()) {
			sql.append(" ON ");
			sql.append(tableId.getFullyQualifiedName());
			if (cascade)
				sql.append(" CASCADE");
		}
		sql.append(';');
		return sql.toString();
	}

	/**
	 * @return the number of rows objects in this model
	 */
	public int getRowCount() {
		return m_data.size();
	}

	/**
	 * @return the name of a column at a given index
	 */
	public String getColumnName(int column) {
		return m_colnames[column];
	}

	/**
	 * @return the type of column at a given index
	 */
	public Class getColumnClass(int column) {
		return m_coltypes[column];
	}

	/**
	 * @return the object at the given row in the model
	 */
	public Rule getRow(int row) {
		return (Rule) m_data.get(row);
	}

	/**
	 * @return the current table id
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "Name", "Expression" */
		Rule rule = getRow(row);
		if (column == NAME_COLUMN) {
			return rule.getName();
		} else if (column == EXPRESSION_COLUMN) {
			return rule.getExpression();
		} else
			return "";
	}

	/**
	 * Modifies the given rule in the database. We pass the old rule simply for
	 * checking/diagnostics.
	 * 
	 * @param newRule
	 *            the rule with the new definition/expression. The name should
	 *            not have changed.
	 */
	public void modifyRule(Rule newRule, Rule oldRule) throws SQLException {
		if (m_connection.supportsSchemas() && I18N.equalsIgnoreCase(newRule.getName(), oldRule.getName())) {
			// use CREATE OR REPLACE RULE instead of dropping the old rule
			String sql = newRule.getExpression();
			SQLCommand.runMetaDataCommand(m_connection, I18N.format("Modify_1", oldRule.getName()), sql);
		} else {
			String sql1 = getDropSQL(oldRule, m_tableid, false);
			String sql2 = newRule.getExpression();
			SQLCommand.runMetaDataCommand(m_connection, I18N.format("Modify_1", oldRule.getName()), sql1, sql2);
		}
	}

	/**
	 * Reloads the model
	 */
	public void reload() {
		removeAll();

		if (m_tableid == null) {
			fireTableDataChanged();
			return;
		}

		try {

			// now get the index
			StringBuffer sqlbuff = new StringBuffer();

			StringBuffer subsqlbuff = new StringBuffer();
			// with namespaces
			if (m_connection.supportsSchemas()) {
				// select oid from pg_class where relname = 'foo' and
				// relkind='r' and relnamespace = (select oid from pg_namespace
				// where nspname='jeff')

				// SELECT rulename, pg_get_ruledef( pg_rewrite.oid) FROM
				// pg_rewrite where pg_rewrite.ev_class =
				// pg_class.oid(tablename)
				subsqlbuff.append("select oid from pg_catalog.pg_class where relname = '");
				subsqlbuff.append(m_tableid.getTableName());
				subsqlbuff
						.append("' and (relkind='r' or relkind='v') and relnamespace = (select oid from pg_namespace where nspname='");
				subsqlbuff.append(m_tableid.getSchema().getName());
				subsqlbuff.append("')");
				sqlbuff.append("SELECT rulename, pg_get_ruledef( pg_catalog.pg_rewrite.oid) FROM pg_catalog.pg_rewrite where pg_rewrite.rulename <> '_RETURN'::name AND pg_catalog.pg_rewrite.ev_class = (");
			} else {
				// without namespaces
				// "select oid from pg_class where relname = 'foo' and relkind='r'";
				subsqlbuff.append("select oid from pg_class where relname = '");
				subsqlbuff.append(m_tableid.getTableName());
				subsqlbuff.append("' and (relkind='r' or relkind='v')");
				sqlbuff.append("SELECT rulename, pg_get_ruledef( pg_rewrite.rulename ) FROM pg_rewrite where pg_rewrite.rulename !~ '^_RET'::text AND pg_rewrite.ev_class = (");
			}

			sqlbuff.append(subsqlbuff.toString());
			sqlbuff.append(")");

			Connection conn = m_connection.getMetaDataConnection();
			Statement stmt = null;

			try {
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {
					String name = rset.getString("rulename");
					String expr = rset.getString("pg_get_ruledef");
					Rule rule = new Rule(name, expr);
					addRow(rule);
				}

				if (m_connection.supportsTransactions()) {
					conn.commit();
				}
			} finally {
				if (stmt != null)
					stmt.close();
			}

			m_connection.release(conn);
		} catch (Exception se) {
			TSUtils.printException(se);
		}
		fireTableDataChanged();
	}

	/**
	 * Remove all data items from this model
	 */
	public void removeAll() {
		m_data.clear();
	}

	/**
	 * Sets the current table id for the model. Reloads the indices.
	 */
	public void setTableId(TableId tableId) {
		m_tableid = tableId;
		reload();
	}

}
