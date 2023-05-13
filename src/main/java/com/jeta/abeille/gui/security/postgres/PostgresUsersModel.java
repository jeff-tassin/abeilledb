package com.jeta.abeille.gui.security.postgres;

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

/**
 * @author Jeff Tassin
 */
public class PostgresUsersModel extends UsersModel {

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int USER_ID_COLUMN = 1;
	static final int CREATE_DB_COLUMN = 2;
	static final int CREATE_USER_COLUMN = 3;
	static final int VALID_UNTIL_COLUMN = 4;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public PostgresUsersModel(TSConnection connection) {
		super(connection);

		String[] names = { I18N.getLocalizedMessage("User Name"), I18N.getLocalizedMessage("User Id"),
				I18N.getLocalizedMessage("Create DB"), I18N.getLocalizedMessage("Create User"),
				I18N.getLocalizedMessage("Valid Until") };

		Class[] types = { User.class, Object.class, Boolean.class, Boolean.class, Calendar.class };
		setColumnNames(names);
		setColumnTypes(types);

		loadData();
	}

	/**
	 * @return the set of groups (Group objects) that this user belongs to
	 */
	public Collection getGroups(User user) {
		LinkedList results = new LinkedList();
		try {
			if (user != null) {
				GroupsModel groups = new GroupsModel(getConnection());
				for (int index = 0; index < groups.getRowCount(); index++) {
					Group group = groups.getRow(index);
					if (group.containsUser(user.getKey())) {
						results.add(group);
					}
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
		return results;
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/**
		 * "User Name", "User Id", "Create DB", "Create User", "Encrypted",
		 * "Valid Until"
		 */
		User user = (User) getRow(row);
		if (column == NAME_COLUMN) {
			return user;
		} else if (column == USER_ID_COLUMN) {
			return user.getKey();
		} else if (column == CREATE_DB_COLUMN) {
			return Boolean.valueOf(user.canCreateDB());
		} else if (column == CREATE_USER_COLUMN) {
			return Boolean.valueOf(user.canCreateUser());
		} else if (column == VALID_UNTIL_COLUMN) {
			return user.getExpireDate();
		}
		return "";
	}

	/**
	 * private void loadData() { try { SecurityService srv =
	 * (SecurityService)getConnection().getImplementation(
	 * SecurityService.COMPONENT_ID ); assert( srv != null ); Collection users =
	 * srv.getUsers(); Iterator iter = users.iterator(); while( iter.hasNext() )
	 * { User user = (User)iter.next(); addRow( user ); } } catch( Exception e )
	 * { TSUtils.printException( e ); } }
	 * 
	 * 
	 * public void reload() { removeAll(); loadData(); fireTableDataChanged(); }
	 */

}
