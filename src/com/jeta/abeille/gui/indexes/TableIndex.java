package com.jeta.abeille.gui.indexes;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class defines an index for a table in a database.
 * 
 * @author Jeff Tassin
 */
public class TableIndex {
	/** the table this index is assigned to */
	private TableId m_tableid;

	/** the name of this index */
	private String m_name;

	/** the columns ( as a comma delimited string ) that make up this index */
	private String m_columnstring = null;

	/**
	 * the names of the columns (String or IndexColumn objects ) that make up
	 * this index
	 */
	private ArrayList m_columns;

	/** flag that indicates if this index is for the primary key */
	private boolean m_primarykey = false;

	/** flag that indicates if this index is unique */
	private boolean m_unique = false;

	/** the type of index (BTREE,RTREE,HASH,GIST) - postgres */
	private String m_type;

	/** flag that indicates if this index is a functional index */
	private boolean m_functional = false;

	/** the function if this index is a functional index */
	private String m_function;

	/**
	 * ctor
	 */
	public TableIndex(TableId tableid) {
		m_tableid = tableid;
	}

	/**
	 * ctor
	 */
	public TableIndex(TableId tableId, String name) {
		this(tableId);
		m_name = name;
	}

	/**
	 * @return the name for this index
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the set of columns names (String objects or IndexColumn objects)
	 *         that make up the columns in this index
	 */
	public Collection getIndexColumns() {
		return m_columns;
	}

	/**
	 * @return a string representation of the columns that make up this index
	 *         This is basically the column names separated by a space
	 */
	public String getColumnsString() {
		if (m_columnstring == null) {
			if (m_columns != null) {
				StringBuffer sbuff = new StringBuffer();
				Iterator iter = m_columns.iterator();
				while (iter.hasNext()) {
					sbuff.append(iter.next());
					if (iter.hasNext())
						sbuff.append(",");
				}
				m_columnstring = sbuff.toString();
			}
		}
		return m_columnstring;
	}

	/**
	 * @return the fully qualified index name
	 */
	public String getSchemaQualifiedIndexName() {
		Schema schema = m_tableid.getSchema();
		if (schema == Schema.VIRTUAL_SCHEMA) {
			return getName();
		} else {
			return schema.getName() + "." + getName();
		}
	}

	/**
	 * @return the table id this index is assigned to
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return the type of index: btree, rtree, hash, gist
	 */
	public String getType() {
		return m_type;
	}

	/**
	 * @return the name of the function for this index if it is functional
	 */
	public String getFunction() {
		return m_function;
	}

	/**
	 * @return true if this index is functional
	 */
	public boolean isFunctional() {
		return m_functional;
	}

	public boolean isPrimaryKey() {
		return m_primarykey;
	}

	public boolean isUnique() {
		return m_unique;
	}

	/**
	 * Prints this index to the console
	 */
	public void print() {
		System.out.println("Name: " + m_name);
		System.out.println("Cols: " + m_columnstring);
		System.out.println("isPrimary: " + m_primarykey);
		System.out.println("isUnique: " + m_unique);
		System.out.println("type: " + m_type);
		System.out.println("isFunctional: " + m_functional);
		System.out.println("function: " + m_function);
	}

	/**
	 * Sets the column name at the given position in the index. This is for
	 * multi-column indexes.
	 * 
	 * @param pos
	 *            the Zero-based index
	 * @param icol
	 *            the column
	 */
	public void setColumn(int pos, IndexColumn icol) {
		if (m_columns == null) {
			m_columns = new ArrayList();
		}

		if (m_columns.size() < (pos + 1)) {
			TSUtils.ensureSize(m_columns, pos + 1);
		}
		m_columns.set(pos, icol);
		m_columnstring = null;
	}

	/**
	 * @return the name of the function for this index if it is functional
	 */
	public void setFunction(String function) {
		m_function = function;
	}

	/**
	 * Sets this index as functional
	 */
	public void setFunctional(boolean functional) {
		m_functional = functional;
	}

	/**
	 * Sets the columns that make up the index
	 * 
	 * @param cols
	 *            a collection of column names (String objects or IndexColumn
	 *            objects)
	 */
	public void setIndexColumns(Collection cols) {
		if (m_columns == null)
			m_columns = new ArrayList();

		m_columns.clear();
		m_columns.addAll(cols);
		m_columnstring = null;
	}

	/**
	 * Sets the name for this index
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Sets whether this index is for the primary key
	 */
	public void setPrimary(boolean isPrimary) {
		m_primarykey = isPrimary;
	}

	/**
	 * Sets the index type
	 */
	public void setType(String iType) {
		m_type = iType;
	}

	/**
	 * Sets whether this index has a unique constraint
	 */
	public void setUnique(boolean isUnique) {
		m_unique = isUnique;
	}
}
