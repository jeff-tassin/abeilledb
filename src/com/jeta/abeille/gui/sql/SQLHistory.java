package com.jeta.abeille.gui.sql;

import java.util.ArrayList;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.i18n.I18N;

/**
 * This class is used to manage the list of sql commands that the user has
 * invoked It has support for storing the commands to a file so that future
 * application instances can use past sql commands
 * 
 * @author Jeff Tassin
 */
public class SQLHistory {
	public static final String SQLHISTORYNAME = "jeta.abeille.gui.sql.sqlhistory";
	private ArrayList m_list = new ArrayList(); // the list
	private int m_pos; // the current position
	private int m_historySize; // the number of items to keep in the history
								// buffer

	public SQLHistory(int historySize) {
		m_historySize = historySize;
		m_pos = -1;
	}

	/**
	 * Adds a command to the history list
	 * 
	 * @param cmd
	 *            the sql string to add to the list
	 */
	public void add(String cmd) {
		cmd = cmd.trim();
		if (m_list.size() > 0) {
			String lastcmd = (String) m_list.get(0);
			lastcmd = lastcmd.trim();
			if (I18N.equals(lastcmd, cmd)) {
				return; // don't re-add a command if it was just run
			}
		}
		m_list.add(0, cmd);

		if (m_list.size() > m_historySize)
			m_list.remove(m_list.size() - 1);

		m_pos = 0; // reset the iterator when adding
	}

	/**
	 * Loads the saved history from the application object set
	 */
	void loadHistory(TSConnection connection) {
		ObjectStore os = (ObjectStore) connection.getObjectStore();

		try {
			m_list = (ArrayList) os.load(SQLHISTORYNAME);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}

		if (m_list == null)
			m_list = new ArrayList();

	}

	/**
	 * @return the next item in the history buffer ( going forward in time )
	 *         Null is returned if there are no next items
	 */
	public String next() {
		if (m_list.size() == 0)
			return null;

		if (m_pos > 0) {
			m_pos--;
			return (String) m_list.get(m_pos);
		} else
			return null;
	}

	/**
	 * @return the previous item in the history buffer ( going back in time )
	 *         Null is returned if there are no previous items
	 */
	public String previous() {
		if (m_list.size() == 0)
			return null;

		if (m_pos < (m_list.size() - 1)) {
			m_pos++;
			return (String) m_list.get(m_pos);
		} else
			return null;
	}

	/**
	 * Saves the history into the application object set
	 */
	void saveHistory(TSConnection connection) {
		ObjectStore os = (ObjectStore) connection.getObjectStore();
		try {
			os.store(SQLHISTORYNAME, m_list);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
