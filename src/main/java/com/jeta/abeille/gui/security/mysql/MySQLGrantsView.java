package com.jeta.abeille.gui.security.mysql;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.security.UsersModel;
import com.jeta.abeille.gui.security.GrantsModel;
import com.jeta.abeille.gui.security.GrantsView;
import com.jeta.abeille.gui.security.GroupsModel;

import com.jeta.plugins.abeille.mysql.MySQLObjectType;

/**
 * View for showing mysql grants
 * 
 * @author Jeff Tassin
 */
public class MySQLGrantsView extends GrantsView {

	/**
	 * ctor
	 */
	public MySQLGrantsView(TSConnection connection, UsersModel usersModel, GroupsModel groupsModel) {
		super(connection, usersModel, groupsModel);

		JComboBox cbox = (JComboBox) getComponentByName(GrantsView.ID_TYPES_COMBO);
		cbox.addItem(MySQLObjectType.GLOBAL);
		cbox.addItem(DbObjectType.TABLE);
		cbox.addItem(DbObjectType.COLUMN);

		// calling this will create the grants table
		setGrantsModelType(MySQLObjectType.GLOBAL);

		/** will be enabled when user selects table or column type */
		JTextField ffield = (JTextField) getComponentByName(GrantsView.ID_FILTER_FIELD);
		ffield.setEnabled(false);

	}

	public GrantsModel createGrantsModel(DbObjectType objType) {
		if (objType == MySQLObjectType.GLOBAL) {
			return new GlobalGrantsModel(getConnection());
		} else if (objType == DbObjectType.TABLE) {
			return new TableGrantsModel(getConnection());
		} else if (objType == DbObjectType.COLUMN) {
			return new ColumnGrantsModel(getConnection());
		} else {
			assert (false);
			return null;
		}
	}
}
