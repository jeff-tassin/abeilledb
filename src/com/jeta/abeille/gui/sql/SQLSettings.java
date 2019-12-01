package com.jeta.abeille.gui.sql;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.table.TableSettings;

/**
 * This class is responsible for serializing SQL settings that were set by the
 * user. Keep in mind that these settings are assoicated with a specific SQL
 * command, not for SQL settings in general. An example is the table layout in
 * the sql results window for a given sql command (each SQL command can have a
 * different table layout)
 * 
 * @author Jeff Tassin
 */
public class SQLSettings implements JETAExternalizable {
	static final long serialVersionUID = 8410123017731164401L;

	public static int VERSION = 1;

	/** the settings for the SQL results table */
	private TableSettings m_tablesettings;

	/** the sql associated with these settings */
	private String m_sql;

	/** settings for the instance view */
	private Object m_instanceviewsettings;

	/**
	 * ctor
	 */
	public SQLSettings() {

	}

	/**
	 * ctor
	 * 
	 * @param sql
	 *            the sql
	 */
	public SQLSettings(String sql) {
		m_sql = sql;
	}

	/**
	 * ctor
	 * 
	 * @param tablesettings
	 *            the object that keeps track of the settings for the sql
	 *            results table
	 */
	public SQLSettings(String sql, TableSettings tablesettings) {
		m_sql = sql;
		m_tablesettings = tablesettings;
	}

	/**
	 * Sets the instance view settings
	 */
	public Object getInstanceViewSettings() {
		return m_instanceviewsettings;
	}

	/**
	 * @return the sql associated with these settings
	 */
	public String getSQL() {
		return m_sql;
	}

	/**
	 * @return the table settings for the sql results window
	 */
	public TableSettings getTableSettings() {
		return m_tablesettings;
	}

	/**
	 * Sets the instance view settings
	 */
	public void setInstanceViewSettings(Object tdata) {
		m_instanceviewsettings = tdata;
	}

	/**
	 * Sets the table settings for the sql
	 */
	public void setTableSettings(TableSettings tablesettings) {
		m_tablesettings = tablesettings;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_tablesettings = (TableSettings) in.readObject();
		m_sql = (String) in.readObject();
		m_instanceviewsettings = in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_tablesettings);
		out.writeObject(m_sql);
		out.writeObject(m_instanceviewsettings);
	}

}
