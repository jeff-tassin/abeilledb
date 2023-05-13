package com.jeta.abeille.database.model;

import java.util.*;
import java.lang.*;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.utils.EmptyCollection;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a TABLE object in a RDBMS.
 * 
 * @author Jeff Tassin
 */
public class TableMetaData implements Cloneable, Comparable, JETAExternalizable, DatabaseObject {
	/** serialization */
	static final long serialVersionUID = 5965264290041419756L;

	public static int VERSION = 2;

	/**
	 * an array of columns (ColumnMetaData objects) in this table sorted by
	 * ordinal position
	 */
	private ArrayList m_columns = new ArrayList();

	/**
	 * a map of column names (Strings) to ColumnMetaData objects. Used for
	 * performance
	 */
	private TreeMap m_fieldsByName = new TreeMap(String.CASE_INSENSITIVE_ORDER); // map
																					// of
																					// ColumnMetaData
																					// by
																					// name

	/** the primary key for this table */
	private DbKey m_primaryKey;

	/**
	 * an array of DbForeignKey objects that define the foreign keys for this
	 * table
	 */
	private ArrayList m_foreignKeys = new ArrayList();

	private TableId m_tableid;

	/**
	 * String that indicates the table type: TABLE, VIEW, SYNONYM, etc.
	 * (corresponds to table type in DatabaseMetaData.getTables
	 */
	private String m_table_type;
	/** flag that indicates if table_type is a view. This is needed for legacy */
	private boolean m_view;

	/**
	 * an array of DbForeignKeys that are declared in other tables that
	 * reference the primary key in this table
	 */
	private transient LinkedList m_exportedKeys;

	/**
	 * bitwise flag (for loadmask) that indicates if foreign keys are loaded for
	 * a table
	 */
	public static final int LOAD_FOREIGN_KEYS = 1;
	/**
	 * bitwise flag that indicates if the full column properties are loaded for
	 * a table. This goes beyond simply the column names and types which are
	 * already loaded. This loads additional information such as constraints and
	 * default value information
	 */
	public static final int LOAD_COLUMNS_EX = 2;

	/**
	 * bitwise flag that loads all foreign keys that are declared in other
	 * tables that reference the primary key in this table. NOTE: This flag will
	 * do a getExportedKeys lookup on this table. It will then do a
	 * LOAD_FOREIGN_KEYS on all the tables returned by getExportedKeys.
	 * Therefore, you can get into a cycle where most of the database is loaded
	 * (for LOAD_ALL). So, LOAD_ALL flag never calls LOAD_EXPORTED_KEYS.
	 */
	public static final int LOAD_EXPORTED_KEYS = 4;

	/**
	 * bitwise flag (for loadmask) that indicates if the table columns/primary
	 * key have been loaded for this table table
	 */
	public static final int LOAD_COLUMNS = 8;

	/** bitwise flag to laod all extra attributes */
	public static final int LOAD_ALL = LOAD_FOREIGN_KEYS | LOAD_COLUMNS_EX;

	/**
	 * this mask determines which part of table has been loaded. we don't load
	 * everthing at once
	 */
	private transient int m_loadmask = 0;

	/** database specific table information */
	private TableAttributes m_attributes;

	/**
	 * ctor for serialization
	 */
	public TableMetaData() {

	}

	/**
	 * Copy ctro
	 */
	public TableMetaData(TableMetaData tmd) {
		Iterator iter = tmd.getColumns().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			addColumn(new ColumnMetaData(cmd));
		}

		DbKey key = tmd.getPrimaryKey();
		if (key != null) {
			setPrimaryKey((DbKey) key.clone());
		}

		iter = tmd.m_foreignKeys.iterator();
		while (iter.hasNext()) {
			DbForeignKey fkey = (DbForeignKey) iter.next();
			addForeignKey((DbForeignKey) fkey.clone());
		}

		m_loadmask = tmd.m_loadmask;
		m_view = tmd.m_view;
		if (tmd.m_attributes != null) {
			m_attributes = (TableAttributes) tmd.m_attributes.clone();
		}

		setTableId((TableId) tmd.m_tableid.clone());
	}

	public TableMetaData(TableId tableId) {
		m_tableid = tableId;
	}

	/**
	 * Adds a column to the end of this table's list of columns
	 */
	public void addColumn(ColumnMetaData cd) {
		cd.setParentTableId(getTableId());
		m_columns.add(cd);
		m_fieldsByName.put(cd.getColumnName(), cd);
	}

	/**
	 * Adds a foreign key that references this table
	 */
	public void addExportedKey(DbForeignKey fkey) {
		if (m_exportedKeys == null)
			m_exportedKeys = new LinkedList();

		m_exportedKeys.add(fkey);
	}

	/**
	 * Adds the given foreign key to this table.
	 */
	public void addForeignKey(DbForeignKey fKey) {
		if (!m_foreignKeys.contains(fKey)) {
			m_foreignKeys.add(fKey);
		} else {
			assert (false);
		}
	}

	public void clearExportedKeys() {
		if (m_exportedKeys != null) {
			m_exportedKeys.clear();
		}
	}

	/**
	 * Creates a clone of this object
	 */
	public Object clone() {
		TableMetaData tmd = new TableMetaData(this);
		return tmd;
	}

	/**
	 * @return true if this table contains the givne foreign key definition
	 */
	public boolean contains(DbForeignKey fkey) {
		return m_foreignKeys.contains(fkey);
	}

	/**
	 * Comparable interface
	 */
	public int compareTo(Object o) {
		String strob = o.toString();
		return m_tableid.toString().compareToIgnoreCase(strob);
	}

	/**
	 * @return the database specific table attributes. The type of object
	 *         depends on the database. For example, in MySQL, we store the
	 *         table type (MyISAM, InnoDB, etc) as an attribute.
	 */
	public TableAttributes getAttributes() {
		return m_attributes;
	}

	/**
	 * @param keyName
	 *            the name of the key to return
	 * @return the candidate key that has the given name.
	 */
	public DbKey getCandidateKey(String keyName) {
		if (m_primaryKey != null && m_primaryKey.getKeyName().equals(keyName))
			return m_primaryKey;
		else {
			ColumnMetaData fmd = (ColumnMetaData) m_fieldsByName.get(keyName);
			if (fmd != null) {
				return fmd.toDbKey();
			} else {

				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * @return the catalog that owns this table
	 */
	public Catalog getCatalog() {
		return m_tableid.getCatalog();
	}

	/**
	 * @return the name of the catalog that owns this table
	 */
	public String getCatalogName() {
		return m_tableid.getCatalog().getName();
	}

	/**
	 * @return the column meta data object that has the given column name
	 */
	public ColumnMetaData getColumn(String colName) {
		if (colName == null)
			return null;

		return (ColumnMetaData) m_fieldsByName.get(colName);
	}

	/**
	 * @return the column meta data object found at the given index (
	 *         zero-based)
	 */
	public ColumnMetaData getColumn(int index) {
		if (index >= 0 && index < m_columns.size()) {
			return (ColumnMetaData) m_columns.get(index);
		} else {
			if (TSUtils.isDebug()) {
				TSUtils.printMessage("TableMetaData.getColumn failed.  Invalid column index: " + index + "  table: "
						+ m_tableid.getFullyQualifiedName());
				assert (false);
			}
			return null;
		}
	}

	/**
	 * @return the column meta data object that has the given column id
	 */
	private ColumnMetaData getColumn(ColumnId cid) {
		return (ColumnMetaData) m_fieldsByName.get(cid.getColumnName());
	}

	/**
	 * @return the number of columns in this table
	 */
	public int getColumnCount() {
		return m_columns.size();
	}

	/**
	 * @return the collection of columns (ColumnMetaData objects) that make up
	 *         this table
	 */
	public Collection getColumns() {
		return m_columns;
	}

	/**
	 * @return the array of columns (ColumnMetaData objects) that make up this
	 *         table
	 */
	public ColumnMetaData[] getColumnsArray() {
		return (ColumnMetaData[]) m_columns.toArray(new ColumnMetaData[0]);
	}

	/**
	 * @return a collection of DbForeignKey objects that are defined in other
	 *         tables that reference this table.
	 */
	public Collection getExportedKeys() {
		if (m_exportedKeys == null)
			return EmptyCollection.getInstance();
		else
			return m_exportedKeys;
	}

	/**
	 * @return the number of foreign keys defined in this table
	 */
	public int getForeignKeyCount() {
		return m_foreignKeys.size();
	}

	public DbForeignKey getForeignKey(int index) {
		return (DbForeignKey) m_foreignKeys.get(index);
	}

	/**
	 * @return an interator to the foreign keys in this table
	 */
	public Collection getForeignKeys() {
		return m_foreignKeys;
	}

	/**
	 * @return the load mask for this table
	 */
	int getLoadMask() {
		return m_loadmask;
	}

	/**
	 * DatabaseObject implementation
	 */
	public DbObjectId getObjectId() {
		return getTableId();
	}

	/**
	 * @return the primary key for this table
	 */
	public DbKey getPrimaryKey() {
		return m_primaryKey;
	}

	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return the schema that owns this table
	 */
	public Schema getSchema() {
		return m_tableid.getSchema();
	}

	/**
	 * @return the name of schema that owns this table
	 */
	public String getSchemaName() {
		return m_tableid.getSchema().getName();
	}

	/**
	 * @return the table name with its schema (i.e. schema.tablename)
	 */
	public String getFullyQualifiedName() {
		return m_tableid.getFullyQualifiedName();
	}

	public String getTableName() {
		return m_tableid.getTableName();
	}

	/**
	 * @return the table type
	 */
	public String getTableType() {
		return m_table_type;
	}

	/**
	 * @return true if this table has a defined primary key
	 */
	public boolean hasPrimaryKey() {
		DbKey pk = getPrimaryKey();
		return (pk != null && pk.getColumnCount() > 0);
	}

	/**
	 * This routine tells if a given field name (in this table) is part of a
	 * foreign key definition in this table.
	 * 
	 * @param fieldName
	 *            the name of the field
	 * @return true if the fieldName is (part of) a foriegn key
	 */
	public boolean isForeignKey(String fieldName) {
		if (fieldName == null)
			return false;

		boolean bresult = false;
		Iterator iter = getForeignKeys().iterator();
		while (iter.hasNext()) {
			DbForeignKey fk = (DbForeignKey) iter.next();
			DbKey key = fk.getLocalKey();
			if (key.containsField(fieldName)) {
				bresult = true;
				break;
			}

		}
		return bresult;
	}

	/**
	 * @return true if the given flag is set in the load mask
	 */
	public boolean isLoadMaskSet(int flag) {
		int currentmask = getLoadMask();
		return ((currentmask & flag) != 0);
	}

	/**
	 * Utility routine that tells if the given field meta data object is part of
	 * the primary key.
	 * 
	 * @param fieldName
	 *            the field name to check
	 * @return true if the fieldName is part of the primary key.
	 */
	public boolean isPrimaryKey(String fieldName) {
		ColumnMetaData fmd = getColumn(fieldName);
		if (fmd == null)
			return false;

		DbKey key = getPrimaryKey();
		if (key != null) {
			if (key.containsField(fmd.getColumnName()))
				return true;
		}
		return false;
	}

	/**
	 * @return true if this table is a view in the database
	 */
	public boolean isView() {
		return "VIEW".equals(m_table_type);
	}

	/**
	 * performs an OR operation with the load mask
	 */
	void ORLoadMask(int mask) {
		m_loadmask |= mask;
	}

	/**
	 * Prepends a column to the beginning of this table's list of columns
	 */
	public void prependColumn(ColumnMetaData cd) {
		cd.setParentTableId(getTableId());
		m_fieldsByName.put(cd.getColumnName(), cd);

		m_columns.add(0, cd);
	}

	/**
	 * Print this object to console
	 */
	public void print() {
		System.out.println("--------  printing table: " + getTableName() + "  ---------- ");
		Collection c = getColumns();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			cmd.print();
		}
	}

	/**
	 * Removes the given foreign key from this table meta data. The fkey is
	 * tested using the equals method against all foreign keys in this meta
	 * data.
	 */
	public void removeForeignKey(DbForeignKey fkey) {
		if (fkey == null)
			return;

		Iterator iter = m_foreignKeys.iterator();
		while (iter.hasNext()) {
			DbForeignKey fk = (DbForeignKey) iter.next();
			if (fkey.equals(fk))
				iter.remove();
		}
	}

	/**
	 * Removes all foreign keys that reference the given tableid
	 */
	public void removeForeignKey(TableId refid) {
		if (refid == null) {
			assert (false);
			return;
		}

		Iterator iter = m_foreignKeys.iterator();
		while (iter.hasNext()) {
			DbForeignKey fk = (DbForeignKey) iter.next();
			if (refid.equals(fk.getReferenceTableId()))
				iter.remove();
		}
	}

	/**
	 * Sets the database specific table attributes The type of object depends on
	 * the database. For example, in MySQL, we store the table type (MyISAM,
	 * InnoDB, etc) as an attribute.
	 */
	public void setAttributes(TableAttributes obj) {
		m_attributes = obj;
	}

	/**
	 * Sets the column at the given ordinal position If a column is already
	 * present at the given index, it is overwritten.
	 * 
	 * @param cmd
	 *            the column meta data to set
	 * @param ordinal_position
	 *            the 1-based position in the table that the column occupies
	 */
	void setColumn(ColumnMetaData cmd, int ordinal_position) {
		assert (cmd != null);
		if (TSUtils.isDebug()) {
			if (ordinal_position < 1) {
				System.out.println("Error. TableMetaData.setColumn  ordinal_position = " + ordinal_position + " for: "
						+ cmd);
			}
		}

		if (m_columns.size() < ordinal_position) {
			int delta = ordinal_position - m_columns.size();
			for (int index = 0; index < delta; index++) {
				m_columns.add(null);
			}
		}

		if (TSUtils.isDebug()) {
			if (m_columns.get(ordinal_position - 1) != null) {
				System.out.println("setColumn failed for " + m_tableid.getFullyQualifiedName());
				System.out.println("column: " + cmd.getColumnName() + "  ordinal position: " + ordinal_position);
				assert (false);
			}
		}
		m_columns.set(ordinal_position - 1, cmd);
		cmd.setParentTableId(getTableId());
		m_fieldsByName.put(cmd.getColumnName(), cmd);
	}

	/**
	 * Sets the table id for this object
	 */
	public void setTableId(TableId tid) {
		m_tableid = tid;
		Iterator iter = m_foreignKeys.iterator();
		while (iter.hasNext()) {
			DbForeignKey fkey = (DbForeignKey) iter.next();
			fkey.setLocalTableId(tid);
		}
	}

	public void setTableName(String tableName) {
		m_tableid = (TableId) m_tableid.changeName(tableName);
	}

	/**
	 * Sets the primary key for this table
	 */
	public void setPrimaryKey(DbKey pKey) {
		m_primaryKey = pKey;
	}

	/**
	 * Sets the table type: TABLE, VIEW, SYSTEM TABLE, GLOBAL TEMPORARY, LOCAL
	 * TEMPORARY, ALIAS, SYNONYM
	 */
	public void setTableType(String ttype) {
		m_table_type = ttype;
		m_view = isView();
	}

	public String toString() {
		return getTableName();
	}

	/**
	 * Used for debugging
	 */
	void validate() {
		ArrayList valid = null;
		for (int index = 0; index < m_columns.size(); index++) {
			ColumnMetaData cmd = (ColumnMetaData) m_columns.get(index);
			if (cmd == null) {
				// this would be a very bad error condition. There is no really
				// good way to recover here
				// This can happen in PostgreSQL if you delete a primary key
				// column
				TSUtils.printDebugMessage("Table failed validation. " + toString() + "  null column at index: " + index);
				if (valid == null)
					valid = new ArrayList();
			}
		}

		if (valid != null) {
			for (int index = 0; index < m_columns.size(); index++) {
				ColumnMetaData cmd = (ColumnMetaData) m_columns.get(index);
				if (cmd != null)
					valid.add(cmd);
			}
			m_columns = valid;
		}
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_columns = (ArrayList) in.readObject();
		m_fieldsByName = (TreeMap) in.readObject();
		m_primaryKey = (DbKey) in.readObject();
		m_foreignKeys = (ArrayList) in.readObject();
		m_tableid = (TableId) in.readObject();
		m_view = in.readBoolean();
		m_attributes = (TableAttributes) in.readObject();
		if (version >= 2)
			m_table_type = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_columns);
		out.writeObject(m_fieldsByName);
		out.writeObject(m_primaryKey);
		out.writeObject(m_foreignKeys);
		out.writeObject(m_tableid);
		out.writeBoolean(m_view);
		out.writeObject(m_attributes);
		out.writeObject(m_table_type);
	}

}
