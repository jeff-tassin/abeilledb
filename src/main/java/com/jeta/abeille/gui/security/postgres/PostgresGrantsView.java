package com.jeta.abeille.gui.security.postgres;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.security.UsersModel;
import com.jeta.abeille.gui.security.GrantsModel;
import com.jeta.abeille.gui.security.GrantsView;
import com.jeta.abeille.gui.security.GroupsModel;

/**
 * View for showing postgres grants
 * 
 * @author Jeff Tassin
 */
public class PostgresGrantsView extends GrantsView {

	/**
	 * ctor
	 */
	public PostgresGrantsView(TSConnection connection, UsersModel usersModel, GroupsModel groupsModel) {
		super(connection, usersModel, groupsModel);
		// calling this will create the grants table
		setGrantsModelType(DbObjectType.TABLE);
	}

	public GrantsModel createGrantsModel(DbObjectType objType) {
		if (objType == DbObjectType.FUNCTION) {
			return new FunctionGrantsModel(getConnection());
		} else {
			return new StandardGrantsModel(getConnection());
		}
	}
}
