package com.jeta.abeille.gui.security.mysql;

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

import com.jeta.abeille.gui.security.UsersModel;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.mysql.MySQLUser;

/**
 * Data model for Users in the database
 * 
 * @author Jeff Tassin
 */
public class MySQLUsersModel extends UsersModel {

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int HOST_COLUMN = 1;
	static final int PASSWORD_COLUMN = 2;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public MySQLUsersModel(TSConnection connection) {
		super(connection);

		String[] names = { I18N.getLocalizedMessage("User Name"), I18N.getLocalizedMessage("Host"),
				I18N.getLocalizedMessage("Password") };

		Class[] types = { User.class, String.class, String.class };
		setColumnNames(names);
		setColumnTypes(types);
		loadData();
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "User Name", "Host Name", "Password" */
		MySQLUser user = (MySQLUser) getRow(row);
		if (column == NAME_COLUMN) {
			return user;
		} else if (column == HOST_COLUMN) {
			return user.getHost();
		} else if (column == PASSWORD_COLUMN) {
			return user.getPassword();
		} else
			return null;
	}

	/**
	 * private void loadData() { try { SecurityService srv =
	 * (SecurityService)getConnection().getImplementation(
	 * SecurityService.COMPONENT_ID ); assert( srv != null ); Collection users =
	 * srv.getUsers(); Iterator iter = users.iterator(); while( iter.hasNext() )
	 * { User user = (User)iter.next(); addRow( user ); } } catch( Exception e )
	 * { TSUtils.printException( e ); } }
	 * 
	 * public void reload() { removeAll(); loadData(); fireTableDataChanged(); }
	 */
}
