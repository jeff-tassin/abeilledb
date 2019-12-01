package com.jeta.abeille.gui.security.hsqldb;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.User;

import com.jeta.abeille.gui.security.GroupsModel;
import com.jeta.abeille.gui.security.UsersModel;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.hsqldb.HSQLUser;

/**
 * @author Jeff Tassin
 */
public class HSQLUsersModel extends UsersModel {

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int ADMIN_COLUMN = 1;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public HSQLUsersModel(TSConnection connection) {
		super(connection);

		String[] names = { I18N.getLocalizedMessage("User Name"), I18N.getLocalizedMessage("Administrator") };

		Class[] types = { User.class, Boolean.class };
		setColumnNames(names);
		setColumnTypes(types);

		loadData();
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "User Name", "Administrator" */
		HSQLUser user = (HSQLUser) getRow(row);
		if (column == NAME_COLUMN) {
			return user;
		} else if (column == ADMIN_COLUMN) {
			return Boolean.valueOf(user.isAdministrator());
		}
		return "";
	}

}
