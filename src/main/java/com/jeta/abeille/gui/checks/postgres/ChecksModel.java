package com.jeta.abeille.gui.checks.postgres;

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
public class ChecksModel extends AbstractTableModel {
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
	public ChecksModel(TSConnection connection, TableId tableId) {
		super();

		m_connection = connection;
		m_tableid = tableId;
		m_data = new ArrayList();

		String[] values = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Expression") };

		m_colnames = values;
		Class[] types = { String.class, String.class };
		m_coltypes = types;
	}

	/**
	 * Adds the given trigger object to the table
	 */
	public void addRow(CheckConstraint check) {
		if (m_data == null)
			m_data = new ArrayList();

		m_data.add(check);
		fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
	}

	/**
	 * Creates the check constraint in the database
	 */
	public void createCheck(CheckConstraint cc) throws SQLException {
		SQLCommand.runMetaDataCommand(m_connection, I18N.format("Creating_1", cc.getName()), getCreateSQL(cc));
	}

	/**
	 * Drops the check constraint in the database
	 */
	public void dropCheck(CheckConstraint cc, boolean cascade) throws SQLException {

		SQLCommand.runMetaDataCommand(m_connection, I18N.format("Drop_1", cc.getName()), getDropSQL(cc, cascade));
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
	 * @return the SQL used to create the given check
	 */
	String getCreateSQL(CheckConstraint cc) {
		String expression = cc.getExpression().trim();
		if (expression.length() > 0) {
			if (expression.charAt(expression.length() - 1) == ';') {
				expression = expression.substring(0, expression.length() - 1);
			}
		}
		TableId tableid = cc.getTableId();

		StringBuffer sqlbuff = new StringBuffer();
		// ALTER TABLE distributors ADD CONSTRAINT zipchk CHECK
		// (char_length(zipcode) = 5);
		sqlbuff.append("ALTER TABLE ");
		sqlbuff.append(tableid.getFullyQualifiedName());
		sqlbuff.append(" ADD CONSTRAINT ");
		sqlbuff.append(cc.getName());
		sqlbuff.append(" CHECK (");
		sqlbuff.append(expression);
		sqlbuff.append(")");
		return sqlbuff.toString();
	}

	/**
	 * @return the SQL used to drop a constraint
	 */
	String getDropSQL(CheckConstraint cc, boolean cascade) {
		TableId tableid = cc.getTableId();
		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append("ALTER TABLE ");
		sqlbuff.append(tableid.getFullyQualifiedName());
		sqlbuff.append(" DROP CONSTRAINT ");
		sqlbuff.append(cc.getName());
		if (cascade)
			sqlbuff.append(" CASCADE");

		sqlbuff.append(';');

		return sqlbuff.toString();
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
	public CheckConstraint getRow(int row) {
		if (row >= 0 && row < m_data.size()) {
			return (CheckConstraint) m_data.get(row);
		} else {
			return null;
		}
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

		CheckConstraint check = getRow(row);
		if (column == NAME_COLUMN) {
			return check.getName();
		} else if (column == EXPRESSION_COLUMN) {
			return check.getExpression();
		} else
			return "";
	}

	/**
	 * Modifies the check constraint in the database This is a drop then create
	 * command
	 */
	public void modifyCheck(CheckConstraint cc) throws SQLException {
		String dropsql = getDropSQL(cc, false);
		String createsql = getCreateSQL(cc);

		LinkedList list = new LinkedList();
		list.add(dropsql);
		list.add(createsql);
		SQLCommand.runMetaDataCommand(m_connection, I18N.format("Creating_1", cc.getName()), list);
	}

	/**
	 * Remove all data items from this model
	 */
	public void removeAll() {
		m_data.clear();
	}

	/**
	 * Reloads the model
	 */
	void reload() {
		removeAll();

		try {
			if (m_tableid != null) {
				// now get the index
				StringBuffer sqlbuff = new StringBuffer();

				StringBuffer subsqlbuff = new StringBuffer();
				// with namespaces
				if (m_connection.supportsSchemas()) {
					// select oid from pg_class where relname = 'foo' and
					// relkind='r' and relnamespace = (select oid from
					// pg_namespace where nspname='jeff')
					subsqlbuff.append("select oid from pg_catalog.pg_class where relname = '");
					subsqlbuff.append(m_tableid.getTableName());
					subsqlbuff
							.append("' and relkind='r' and relnamespace = (select oid from pg_namespace where nspname='");
					subsqlbuff.append(m_tableid.getSchema().getName());
					subsqlbuff.append("')");
					sqlbuff.append("select * from pg_catalog.pg_constraint where contype='c' and conrelid=(");
				} else {
					// without namespaces
					// "select oid from pg_class where relname = 'foo' and relkind='r'";
					subsqlbuff.append("select oid from pg_class where relname = '");
					subsqlbuff.append(m_tableid.getTableName());
					subsqlbuff.append("' and relkind='r'");
					sqlbuff.append("select * from pg_relcheck where rcrelid=(");
				}

				sqlbuff.append(subsqlbuff.toString());
				sqlbuff.append(")");

				Connection conn = m_connection.getMetaDataConnection();
				Statement stmt = null;

				try {
					stmt = conn.createStatement();
					ResultSet rset = stmt.executeQuery(sqlbuff.toString());
					while (rset.next()) {
						if (m_connection.supportsSchemas()) {
							String name = rset.getString("conname");
							String expr = rset.getString("consrc");
							CheckConstraint check = new CheckConstraint(m_tableid, name, expr);
							addRow(check);
						} else {
							String name = rset.getString("rcname");
							String expr = rset.getString("rcsrc");
							CheckConstraint check = new CheckConstraint(m_tableid, name, expr);
							addRow(check);
						}
					}

					if (m_connection.supportsTransactions()) {
						conn.commit();
					}
				} finally {
					if (stmt != null)
						stmt.close();
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		fireTableDataChanged();
	}

	/**
	 * Sets the current table id for the model. Reloads the indices.
	 */
	public void setTableId(TableId tableId) {
		m_tableid = tableId;
		reload();
	}
}
