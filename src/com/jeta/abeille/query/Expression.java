package com.jeta.abeille.query;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class represents a single expression in a query. It is of the form:
 * columnid op value Examples: lastname = smith age = 23
 * 
 * @author Jeff Tassin
 */
public class Expression implements JETAExternalizable {
	static final long serialVersionUID = 8881560024713763827L;

	public static int VERSION = 1;

	/** the name of the column that is to be constraint */
	private String m_columnname;

	/** the operator =, !=, <, > etc */
	private Operator m_op;

	/** the value of the constraint */
	private String m_value;

	/**
	 * sets the table id. If this value is not null, it takes precedence over
	 * the tablename.
	 */
	private TableId m_tableid;

	/** the column meta data */
	private ColumnMetaData m_cmd;

	public Expression() {

	}

	/**
	 * ctor
	 */
	public Expression(ColumnMetaData cmd, Operator op, String value) {
		m_tableid = cmd.getParentTableId();
		m_cmd = cmd;
		m_op = op;
		m_value = value;
	}

	/**
	 * ctor
	 */
	public Expression(TableId tableId, String colName, Operator op, String value) {
		m_tableid = tableId;
		m_columnname = colName;
		m_op = op;
		m_value = value;
	}

	/**
	 * @return the column metadata for this expression
	 */
	public ColumnMetaData getColumnMetaData() {
		return m_cmd;
	}

	/**
	 * @return the name of the column for this constraint
	 */
	public String getColumnName() {
		if (m_cmd == null)
			return m_columnname;
		else
			return m_cmd.getColumnName();
	}

	/**
	 * @return the name of the table that contains the column
	 */
	public String getTableName() {
		assert (m_tableid == null);
		return m_tableid.getTableName();
	}

	/**
	 * @return the id of the table that contains the column
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return the constraint operator
	 */
	public Operator getOperator() {
		return m_op;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return m_value;
	}

	/**
	 * Dump out contents
	 */
	public void print() {
		System.out.println(toString());
	}

	/**
	 * Sets the column name. No validation is made to determine if the column
	 * belongs to the table for this expression
	 */
	public void setColumnName(String colName) {
		assert (m_cmd == null);
		m_columnname = colName;
	}

	/**
	 * Sets the operator
	 */
	public void setOperator(Operator op) {
		m_op = op;
	}

	/**
	 * Sets the table id for this expression
	 */
	public void setTableId(TableId tableId) {
		m_tableid = tableId;
	}

	/**
	 * Sets the value
	 */
	public void setValue(String Value) {
		m_value = Value;
	}

	/**
	 * @param currentCatalog
	 *            the current catalog. If a table name is not in this catalog,
	 *            then the table is fully qualified.
	 * @param currentSchema
	 *            the current catalog. If a table name is not in this schema,
	 *            then the table is fully qualified.
	 * @return this constraint in SQL form
	 */
	public String toSQL(Catalog currentCatalog, Schema currentSchema) {
		assert (m_tableid != null);
		StringBuffer sqlbuff = new StringBuffer();
		sqlbuff.append(DbUtils.getQualifiedName(currentCatalog, currentSchema, m_tableid));
		sqlbuff.append(".");
		sqlbuff.append(getColumnName());
		sqlbuff.append(" ");
		sqlbuff.append(m_op.toString());
		sqlbuff.append(" ");
		sqlbuff.append(m_value);
		return sqlbuff.toString();
	}

	/**
	 * @return the string representation of this expression
	 */
	public String toString() {
		return toSQL(null, null);
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_columnname = (String) in.readObject();
		m_op = (Operator) in.readObject();
		m_value = (String) in.readObject();
		m_tableid = (TableId) in.readObject();
		m_cmd = (ColumnMetaData) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_columnname);
		out.writeObject(m_op);
		out.writeObject(m_value);
		out.writeObject(m_tableid);
		out.writeObject(m_cmd);
	}

}
