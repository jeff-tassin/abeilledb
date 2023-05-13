package com.jeta.abeille.gui.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.database.security.User;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This table model displays a list of users and groups in a table
 * 
 * @author Jeff Tassin
 */
public class GroupsAndUsersModel extends AbstractTableModel {
	/** an array of Group objects */
	private ArrayList m_data;

	/** an array of column names for the table */
	private String[] m_colnames;

	/** an array of column types for the table */
	private Class[] m_coltypes;

	/** the underlying data models for this model */
	private GroupsModel m_groupsmodel;
	private UsersModel m_usersmodel;

	private static ImageIcon m_groupicon = TSGuiToolbox.loadImage("incors/16x16/businessmen.png");
	private static ImageIcon m_usericon = TSGuiToolbox.loadImage("incors/16x16/businessman.png");

	/** column definitions */
	static final int ICON_COLUMN = 0;
	static final int NAME_COLUMN = 1;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public GroupsAndUsersModel() {
		super();
		m_data = new ArrayList();

		String[] values = { "", I18N.getLocalizedMessage("Name") };

		m_colnames = values;
		Class[] types = { ImageIcon.class, String.class };
		m_coltypes = types;

		loadData();
	}

	/**
	 * Adds the given group object to the table
	 */
	public void addRow(Object obj) {
		if (m_data == null)
			m_data = new ArrayList();

		m_data.add(obj);
		fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
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
	 * @return the user object at the given row. If the row is invalid, null is
	 *         returned
	 */
	public AbstractUser getUser(int row) {
		if (row >= 0 && row < m_data.size()) {
			return (AbstractUser) m_data.get(row);
		} else {
			return null;
		}
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "Icon", "Name" */
		AbstractUser user = getUser(row);
		if (column == ICON_COLUMN) {
			if (user instanceof Group)
				return m_groupicon;
			else
				return m_usericon;
		} else if (column == NAME_COLUMN) {
			return user.getQualifiedName();
		} else
			return "";
	}

	/**
	 * Tries to load the data from the security service
	 */
	private void loadData() {
		// add the public user
		if (m_groupsmodel != null)
			addRow(User.PUBLIC);

		if (m_usersmodel != null) {
			for (int index = 0; index < m_usersmodel.getRowCount(); index++) {
				User user = (User) m_usersmodel.getRow(index);
				addRow(user);
			}
		}

		if (m_groupsmodel != null) {
			for (int index = 0; index < m_groupsmodel.getRowCount(); index++) {
				Group group = m_groupsmodel.getRow(index);
				addRow(group);
			}
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
	}

	/**
	 * Sets the groups and users model that this model uses for its underlying
	 * data
	 */
	void setGroupsAndUsers(GroupsModel groupsModel, UsersModel usersModel) {
		m_groupsmodel = groupsModel;
		m_usersmodel = usersModel;
	}

}
