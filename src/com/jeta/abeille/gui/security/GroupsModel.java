package com.jeta.abeille.gui.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.database.security.User;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * @author Jeff Tassin
 */
public class GroupsModel extends AbstractTableModel {
	/** an array of Group objects */
	private ArrayList m_data;

	/**
	 * we use this to generate/cache the string list of users to display in the
	 * table. It is used for peformance so we don't need to regenerate the list
	 * every time
	 */
	private HashMap m_usersdisplay = new HashMap();

	/** an array of column names for the table */
	private String[] m_colnames;

	/** an array of column types for the table */
	private Class[] m_coltypes;

	/** The database connection */
	private TSConnection m_connection;

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int GROUP_ID_COLUMN = 1;
	static final int USERS_COLUMN = 2;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public GroupsModel(TSConnection connection) {
		super();

		m_connection = connection;
		m_data = new ArrayList();

		String[] values = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Group Id"),
				I18N.getLocalizedMessage("Users") };

		m_colnames = values;
		Class[] types = { Group.class, Object.class, String.class };
		m_coltypes = types;

		loadData();
	}

	/**
	 * Adds the given group object to the table
	 */
	public void addRow(Group group) {
		if (m_data == null)
			m_data = new ArrayList();

		m_data.add(group);
		fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
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
	 * This method returns a command separated list of users that belong to the
	 * group. We cache this so we don't need to generate every time.
	 */
	String getUsers(Group group) {
		String users = (String) m_usersdisplay.get(group.getKey());
		if (users == null) {
			StringBuffer sbuff = new StringBuffer();
			try {
				SecurityService srv = (SecurityService) m_connection.getImplementation(SecurityService.COMPONENT_ID);
				Collection ulist = group.getUsers();
				int count = 0;
				Iterator iter = ulist.iterator();
				while (iter.hasNext()) {
					Object userkey = iter.next();
					User user = srv.getUser(userkey);
					if (user != null) {
						if (count > 10) {
							sbuff.append("...");
							break;
						}
						if (count > 0) {
							sbuff.append(", ");
						}
						sbuff.append(user.getName());
						count++;
					}
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
			users = sbuff.toString();
			m_usersdisplay.put(group.getKey(), users);
		}
		return users;
	}

	/**
	 * @return the object at the given row in the model
	 */
	public Group getRow(int row) {
		return (Group) m_data.get(row);
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "Group Id", "Group Name", "Users" */
		Group group = getRow(row);
		if (column == NAME_COLUMN) {
			return group;
		} else if (column == GROUP_ID_COLUMN) {
			return group.getKey();
		} else if (column == USERS_COLUMN) {
			return getUsers(group);
		} else
			return "";
	}

	/**
	 * Tries to load the data from the security service
	 */
	private void loadData() {
		try {
			SecurityService srv = (SecurityService) m_connection.getImplementation(SecurityService.COMPONENT_ID);
			assert (srv != null);
			Collection groups = srv.getGroups();
			Iterator iter = groups.iterator();
			while (iter.hasNext()) {
				Group group = (Group) iter.next();
				addRow(group);
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Reloads the model
	 */
	public void reload() {
		removeAll();
		loadData();
		fireTableDataChanged();
	}

	/**
	 * Remove all data items from this model
	 */
	public void removeAll() {
		m_data.clear();
		m_usersdisplay.clear();
	}

}
