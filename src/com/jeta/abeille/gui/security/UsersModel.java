package com.jeta.abeille.gui.security;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.database.security.User;

import com.jeta.abeille.gui.common.MetaDataTableModel;

import com.jeta.foundation.utils.TSUtils;

public abstract class UsersModel extends MetaDataTableModel {
	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public UsersModel(TSConnection connection) {
		super(connection, null);
	}

	/**
	 * @return the set of groups (Group objects) that this user belongs to
	 */
	public Collection getGroups(User user) {
		return com.jeta.foundation.utils.EmptyCollection.getInstance();
	}

	/**
	 * Tries to load the data from the security service
	 */
	protected void loadData() {
		try {
			SecurityService srv = (SecurityService) getConnection().getImplementation(SecurityService.COMPONENT_ID);
			assert (srv != null);
			Collection users = srv.getUsers();
			Iterator iter = users.iterator();
			while (iter.hasNext()) {
				User user = (User) iter.next();
				addRow(user);
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

}
