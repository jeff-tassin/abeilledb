package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.sql.DatabaseMetaData;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class represents a column in a table. It defines all the meta data
 * attributes for the column
 * 
 * @author Jeff Tassin
 */
public class ColumnMetaData implements Comparable, JETAExternalizable, DatabaseObject {
	/** serializable */
	static final long serialVersionUID = -7347883574347809995L;

	public static int VERSION = 1;

	protected String m_columnname; // the name of the column
	protected int m_datatype; // the data type (as defined in java.sql.Types)
	protected String m_typename; // data source dependent type name (if datatype
									// is not defined, this takes precedence)
	protected TableId m_parentid; // the table object that owns this field
	protected int m_colSize; // the size of the column
	protected int m_nullable; // if this column can contain nulls
	protected int m_scale = 0; // the number of digits after the decimal point
								// if this type supports
								// decimals

	/**
	 * the ordinal position. This is transient because we only need it for
	 * informational purposes
	 */
	private transient int m_ordinalposition;

	/** a different name of display label for this column */
	protected String m_alias;

	/** flag that indicates if this column is an auto increment type */
	private boolean m_autoincrement = false;

	/** the expression for the default value for this column (can be null ) */
	private String m_defaultvalue;

	/**
	 * database dependent attributes for this column. Note, this object must be
	 * serializable
	 */
	private ColumnAttributes m_attributes;

	private transient ColumnId m_columnid = null;

	/** this is the columnname:type:(size) */
	private transient String m_signature;

	/**
	 * ctor only for serialization
	 */
	public ColumnMetaData() {

	}

	/**
	 * copy ctor
	 */
	public ColumnMetaData(ColumnMetaData cmd) {
		m_columnname = cmd.m_columnname;
		m_datatype = cmd.m_datatype;
		m_typename = cmd.m_typename;
		m_parentid = cmd.m_parentid;
		m_colSize = cmd.m_colSize;
		m_nullable = cmd.m_nullable;
		m_scale = cmd.m_scale;
		m_autoincrement = cmd.m_autoincrement;
		m_defaultvalue = cmd.m_defaultvalue;
		if (cmd.m_attributes != null) {
			m_attributes = (ColumnAttributes) cmd.m_attributes.clone();
		}
	}

	/**
	 * ctor
	 */
	public ColumnMetaData(String fieldName, int dataType, int colSize, TableId parentId, int nullable) {
		m_columnname = fieldName;
		m_datatype = dataType;
		m_parentid = parentId;
		m_colSize = colSize;
		m_nullable = nullable;
	}

	/**
	 * ctor
	 */
	public ColumnMetaData(String colName, int dataType, String typeName, int colSize, TableId parentId,
			boolean bNullable) {
		m_columnname = colName;
		m_datatype = dataType;
		m_typename = typeName;
		m_parentid = parentId;
		m_colSize = colSize;
		if (bNullable)
			m_nullable = DatabaseMetaData.columnNullable;
		else
			m_nullable = DatabaseMetaData.columnNoNulls;
	}

	// Comparable interface
	public int compareTo(Object o) {
		if (o instanceof ColumnMetaData) {
			ColumnMetaData cmd = (ColumnMetaData) o;
			return getColumnId().compareTo(cmd.getColumnId());
		} else {
			if (o != null) {
				System.out.println("compareTo failed: " + o.getClass());
			}
			assert (false);
			return -1;
		}
	}

	/**
	 * Override equals
	 */
	public boolean equals(Object o) {
		if (o == null)
			return false;
		else
			return (compareTo(o) == 0);
	}

	/**
	 * @return the alias for this column
	 */
	public String getAlias() {
		return m_alias;
	}

	/**
	 * @return the database specific column attributes. The type of object
	 *         depends on the database. For example, in MySQL, we have
	 *         attributes such as unsigned, zero fill, etc;
	 */
	public ColumnAttributes getAttributes() {
		return m_attributes;
	}

	/**
	 * @return the name of this column
	 */
	public String getColumnName() {
		return m_columnname;
	}

	/**
	 * @return the signature for this column
	 */
	public String getColumnSignature(DataTypeInfo typeinfo) {
		if (m_signature == null) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(getColumnName());
			String typename = getTypeName();
			if (typename != null) {
				buffer.append(':');
				buffer.append(typename);
				if (typeinfo != null) {
					if (typeinfo.isPrecisionRequired()) {
						buffer.append("(");
						buffer.append(getColumnSize());
						if (getScale() > 0) {
							buffer.append(',');
							buffer.append(getScale());
						}
						buffer.append(')');
					} else if (DbUtils.isPrecisionAllowed(typeinfo.getType())) {
						if (getColumnSize() > 0) {
							buffer.append("(");
							buffer.append(getColumnSize());
							if (getScale() > 0) {
								buffer.append(',');
								buffer.append(getScale());
							}
							buffer.append(')');
						}
					}
				}
			}
			m_signature = buffer.toString();
		}
		return m_signature;
	}

	/**
	 * @return the size of this column
	 */
	public int getColumnSize() {
		return m_colSize;
	}

	/**
	 * Sets the number of fractional digits for this column
	 */
	public void setScale(int digits) {
		m_scale = digits;
		m_signature = null;
	}

	public int getScale() {
		return m_scale;
	}

	/**
	 * @return the id table that owns this column
	 */
	public TableId getParentTableId() {
		return m_parentid;
	}

	/**
	 * @return the name of this column with its parent table preprended.
	 */
	public String getTableQualifiedName() {
		TableId tableid = getParentTableId();
		if (tableid != null) {
			StringBuffer sbuff = new StringBuffer();
			sbuff.append(tableid.getTableName());
			sbuff.append('.');
			sbuff.append(getColumnName());
			return sbuff.toString();
		} else {
			return getColumnName();
		}
	}

	private boolean tableEquals(TableId id1, TableId id2) {
		if (id1 == null) {
			if (id2 == null)
				return true;
			else
				return false;
		}

		return id1.equals(id2);

	}

	public synchronized ColumnId getColumnId() {
		if (m_columnid == null) {
			m_columnid = new ColumnId(m_columnname, getTableId());
		}
		return m_columnid;
	}

	/**
	 * @return the expression for the default value for this column
	 */
	public String getDefaultValue() {
		return m_defaultvalue;
	}

	public String getFieldName() {
		return m_columnname;
	}

	public String getName() {
		return m_columnname;
	}

	/**
	 * @return the DatabaseMeta flag indicating whether this field is nullable
	 *         or not (or unknown)
	 */
	public int getNullable() {
		return m_nullable;
	}

	/**
	 * Database object implementation
	 */
	public DbObjectId getObjectId() {
		return getColumnId();
	}

	/**
	 * @return the ordinal position for this column
	 */
	public int getOrdinalPosition() {
		return m_ordinalposition;
	}

	/**
	 * @return true if the column is nullable. False if otherwise
	 */
	public boolean isNullable() {
		return (m_nullable == DatabaseMetaData.columnNullable);
	}

	public void setNullable(boolean bNullable) {
		if (bNullable)
			m_nullable = DatabaseMetaData.columnNullable;
		else
			m_nullable = DatabaseMetaData.columnNoNulls;
	}

	/**
	 * @return the fully qualified name for this field (i.e.
	 *         TableSpace.TableName.FieldName);
	 */
	public String getQualifiedName() {
		return getTableId().getFullyQualifiedName() + "." + getFieldName();
	}

	/**
	 * @return the identifier of the table that contains this field. A field
	 *         instance can belong to only one table.
	 */
	public TableId getTableId() {
		return m_parentid;
	}

	public int getType() {
		return m_datatype;
	}

	/**
	 * @return the data source dependent type name (if datatype is not defined,
	 *         this takes precedence)
	 */
	public String getTypeName() {
		return m_typename;
	}

	/**
	 * @return a proper hash code for storing column metadata objects in hash
	 *         tables
	 */
	public int hashCode() {
		return getColumnName().hashCode();
	}

	/**
	 * @return the auto increment flag for this column. This should be used with
	 *         care because you could have a type that does not support auto
	 *         increment. For example, it would not make since for a boolean or
	 *         blob type to be autoincrement
	 */
	public boolean isAutoIncrement() {
		return m_autoincrement;
	}

	/**
	 * Prints this object to the console
	 */
	public void print() {
		System.out.println("    colname = " + m_columnname);
		System.out.println("    datatype = " + m_datatype);
		System.out.println("    typename = " + m_typename);
		System.out.println("    parentid = " + m_parentid);
		System.out.println("    autoinc = " + m_autoincrement);

	}

	/**
	 * Sets the alias for this column
	 */
	public void setAlias(String alias) {
		m_alias = alias;
	}

	/**
	 * Sets the database specific column attributes The type of object depends
	 * on the database. For example, in MySQL, we store attributes such as zero
	 * fill, unsigned, etc.
	 */
	public void setAttributes(ColumnAttributes obj) {
		m_attributes = obj;
	}

	/**
	 * Sets the auto increment flag for this column. This should be used with
	 * care because you could have a type that does not support auto increment.
	 * For example, it would not make since for a boolean or blob type to be
	 * autoincrement
	 */
	public void setAutoIncrement(boolean bAuto) {
		m_autoincrement = bAuto;
	}

	public void setColumnName(String colName) {
		m_columnid = null;
		m_columnname = colName;
		m_signature = null;

	}

	/**
	 * Sets the size for this column
	 */
	public void setColumnSize(int size) {
		m_colSize = size;
		m_signature = null;

	}

	/**
	 * Sets the expression for the default value for this column
	 */
	public void setDefaultValue(String defValue) {
		m_defaultvalue = defValue;
	}

	/**
	 * Sets the ordinal position for this column
	 */
	void setOrdinalPosition(int ordPos) {
		m_ordinalposition = ordPos;
	}

	/**
	 * Sets the parent table for this column
	 */
	public void setParentTableId(TableId tableId) {
		m_columnid = null;
		m_parentid = tableId;
	}

	/**
	 * Sets the data type (JDBC) for this column.
	 */
	public void setType(int type) {
		m_datatype = type;
		m_signature = null;
	}

	/**
	 * Sets the data source dependent type name (if datatype is not defined,
	 * this takes precedence)
	 */
	public void setTypeName(String tName) {
		m_typename = tName;
		m_signature = null;

	}

	public String toString() {
		return m_columnname;
	}

	public DbKey toDbKey() {
		DbKey mykey = new DbKey();
		mykey.setKeyName(getFieldName());
		mykey.addField(getFieldName());
		return mykey;
	}

	public void unitTest() {

	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_columnname = (String) in.readObject();
		m_datatype = in.readInt();
		m_typename = (String) in.readObject();
		m_parentid = (TableId) in.readObject();
		m_colSize = in.readInt();
		m_nullable = in.readInt();
		m_scale = in.readInt();
		m_alias = (String) in.readObject();
		m_autoincrement = in.readBoolean();
		m_defaultvalue = (String) in.readObject();
		m_attributes = (ColumnAttributes) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeObject(m_columnname);
		out.writeInt(m_datatype);
		out.writeObject(m_typename);
		out.writeObject(m_parentid);
		out.writeInt(m_colSize);
		out.writeInt(m_nullable);
		out.writeInt(m_scale);
		out.writeObject(m_alias);
		out.writeBoolean(m_autoincrement);
		out.writeObject(m_defaultvalue);
		out.writeObject(m_attributes);
	}

}
