package com.jeta.abeille.gui.modeler;

public class ForeignKeyColumn {
	/** the name of the local column */
	private String m_localcolname;

	/**
	 * the name of the column in the reference table the local column is
	 * assigned to
	 */
	private String m_refcolname;

	/**
	 * ctor
	 */
	public ForeignKeyColumn(String localColumnName, String refColumnName) {
		m_localcolname = localColumnName;
		m_refcolname = refColumnName;
	}

	/**
	 * @return the local column name
	 */
	public String getLocalColumnName() {
		return m_localcolname;
	}

	/**
	 * @return the reference column name
	 */
	public String getReferenceColumnName() {
		return m_refcolname;
	}
}
