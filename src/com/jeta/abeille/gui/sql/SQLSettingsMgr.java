package com.jeta.abeille.gui.sql;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.netbeans.editor.TokenID;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is responsible for serializing and managing preferences for the
 * SQL results window.
 * 
 * @author Jeff Tassin
 */
public class SQLSettingsMgr implements JETAExternalizable {
	static final long serialVersionUID = 3503830603522452310L;

	public static int VERSION = 1;

	/** the underlying database connection */
	private TSConnection m_connection;

	/** a list of setting objects */
	private LinkedList m_settings = new LinkedList();

	/** the total number of settings that we store. */
	private static final int SETTINGS_CACHE_SIZE = 200;

	/**
	 * Internal cache of SQLSettingsMgr objects so we don't have to hit the
	 * object store every time. The map is comprised of TSConnection (keys) to
	 * SQLSettingsMgr (values)
	 */
	private static HashMap m_mgrs = new HashMap();

	public static final String COMPONENT_ID = "jeta.abeille.gui.sql.sqlsettingsmgr";

	/**
	 * ctor for Serialization
	 */
	public SQLSettingsMgr() {

	}

	/**
	 * ctor - use getInstance instead
	 */
	private SQLSettingsMgr(TSConnection connection) {
		m_connection = connection;
	}

	/**
	 * Adds a SQLSettings object for the given sql command. If a settings exists
	 * for the same sql statement, it is overwritten.
	 */
	public synchronized void add(SQLSettings settings) {
		String sql = settings.getSQL();
		if (sql == null)
			return;

		remove(sql);

		if (m_settings.size() >= SETTINGS_CACHE_SIZE)
			m_settings.removeLast();

		m_settings.addFirst(settings);
	}

	/**
	 * @return a settings object that is associated with the given sql. If the
	 *         object is not found, null is returned.
	 */
	public synchronized SQLSettings get(String sql) {

		SQLSettings result = null;
		Iterator iter = m_settings.iterator();
		while (iter.hasNext()) {
			SQLSettings ss = (SQLSettings) iter.next();
			if (sql.equals(ss.getSQL())) {
				result = ss;
				break;
			}
		}

		return result;
	}

	/**
	 * @return the settings mananager associated with the given connection
	 */
	public static synchronized SQLSettingsMgr getInstance(TSConnection connection) {
		SQLSettingsMgr result = (SQLSettingsMgr) m_mgrs.get(connection);
		if (result == null) {
			ObjectStore ostore = connection.getObjectStore();
			try {
				result = (SQLSettingsMgr) ostore.load(COMPONENT_ID);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		if (result == null) {
			result = new SQLSettingsMgr(connection);
		}

		m_mgrs.put(connection, result);

		return result;
	}

	/**
	 * Removes and settings associated with the sql.
	 */
	public synchronized void remove(String sql) {
		if (sql == null)
			return;

		Iterator iter = m_settings.iterator();
		while (iter.hasNext()) {
			SQLSettings ss = (SQLSettings) iter.next();
			if (sql.equals(ss.getSQL()))
				iter.remove();
		}
	}

	/**
	 * Saves this instance to the object store
	 */
	public synchronized void save() {
		ObjectStore ostore = m_connection.getObjectStore();
		try {
			ostore.store(COMPONENT_ID, this);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Removes all whitespace and semicolons from sql. We also convert the
	 * keywords in the sql to lower case because select * from notes is the same
	 * as SELECT * FROM NOTES;
	 * 
	 */
	public String trim(String sql) {
		StringBuffer buff = new StringBuffer();
		try {
			SQLDocumentParser parser = new SQLDocumentParser(m_connection, sql, SQLPreferences.getDelimiter());
			while (parser.hasMoreTokens()) {
				Collection tokens = parser.getTokens();
				Iterator iter = tokens.iterator();
				while (iter.hasNext()) {
					TokenInfo info = (TokenInfo) iter.next();
					String sval = info.getValue();
					TokenID token = info.getToken();
					if (token == SQLTokenContext.STRING_LITERAL) {
						buff.append(sval);
					} else if (token == SQLTokenContext.WHITESPACE) {
						buff.append(' ');
					} else {
						if (TSUtils.getChar(sval) != SQLPreferences.getDelimiter()) {
							buff.append(sval.toLowerCase());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (buff.length() == 0)
			return sql;
		else
			return buff.toString();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_connection = (TSConnection) in.readObject();
		m_settings = (LinkedList) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_connection);
		out.writeObject(m_settings);
	}

}
