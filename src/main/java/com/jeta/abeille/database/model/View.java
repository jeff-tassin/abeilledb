package com.jeta.abeille.database.model;

/**
 * This class represents a view in the database.
 * 
 * @author Jeff Tassin
 */
public class View {
	/**
	 * The definition for this view
	 */
	private String m_definition;

	/**
	 * The id for this view
	 */
	private DbObjectId m_tableid;

	/**
	 * ctor
	 */
	public View(DbObjectId tableid) {
		m_tableid = tableid;
	}

	/**
	 * @return the SQL used to define the view
	 */
	public String getDefinition() {
		return m_definition;
	}

	/**
	 * @return the name for this view
	 */
	public String getName() {
		if (m_tableid != null)
			return m_tableid.getObjectName();
		else
			return null;
	}

	/**
	 * @return the schema that contains this view
	 */
	public Schema getSchema() {
		if (m_tableid != null) {
			return m_tableid.getSchema();
		} else
			return null;
	}

	/**
	 * @return the view name with the appropriate schema name prepended
	 */
	public String getFullyQualifiedName() {
		if (m_tableid != null)
			return m_tableid.getFullyQualifiedName();
		else
			return null;
	}

	/**
	 * @return the id for this view
	 */
	public DbObjectId getTableId() {
		return m_tableid;
	}

	/**
	 * Sets the SQL used to define the view
	 */
	public void setDefinition(String def) {
		m_definition = def;
	}

	/**
	 * Sets the id for this view
	 */
	public void setTableId(DbObjectId tableId) {
		m_tableid = tableId;
	}

}
