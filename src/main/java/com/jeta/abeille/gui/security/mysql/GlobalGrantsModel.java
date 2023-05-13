package com.jeta.abeille.gui.security.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Privilege;
import com.jeta.abeille.database.security.SecurityService;

import com.jeta.abeille.gui.security.GrantDefinitionWrapper;
import com.jeta.abeille.gui.security.GrantsModel;
import com.jeta.abeille.gui.security.OrthoGrantDefinitionWrapper;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.mysql.MySQLObjectType;
import com.jeta.plugins.abeille.mysql.MySQLPrivilege;
import com.jeta.plugins.abeille.mysql.MySQLSecurityServiceImplementation;
import com.jeta.plugins.abeille.mysql.UserId;

/**
 * This table model displays the global grants for given user in MySQL
 * 
 * @author Jeff Tassin
 */
public class GlobalGrantsModel extends GrantsModel {

	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int GRANT_COLUMN = 1;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public GlobalGrantsModel(TSConnection connection) {
		super(connection);

		String[] names = { I18N.getLocalizedMessage("Privilege"), I18N.getLocalizedMessage("Grant") };

		Class[] types = { String.class, Boolean.class };

		setColumnGrantDefinition(GRANT_COLUMN, new ColumnGrantDefinition(MySQLPrivilege.GRANT));
		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * Tries to load the data from the security service
	 * 
	 * @param objType
	 *            the type of object to load (e.g. TABLE, VIEW, FUNCTION, etc )
	 * @param schema
	 *            the schema that contains the objects to load
	 * @param user
	 *            the user or group that we wish to load permissions for
	 * @param regex
	 *            a regular expression to match the object name for a given
	 *            object type
	 */
	public void loadData(DbObjectType objType, Catalog catalog, Schema schema, AbstractUser user, String regex) {
		try {
			if (objType == MySQLObjectType.GLOBAL) {

				MySQLSecurityServiceImplementation srv = (MySQLSecurityServiceImplementation) getConnection()
						.getImplementation(MySQLSecurityServiceImplementation.COMPONENT_ID);
				assert (srv != null);

				GrantDefinition gdef = srv.getGlobalPrivileges(user);
				if (gdef != null) {
					Collection grants = srv.getSupportedPrivileges(MySQLObjectType.GLOBAL);
					Iterator giter = grants.iterator();
					while (giter.hasNext()) {
						Privilege p = (Privilege) giter.next();
						boolean isgranted = gdef.isGranted(p);
						addRow(new OrthoGrantDefinitionWrapper(p, isgranted));
					}
				}
			}
			setCatalog(catalog);
			setSchema(schema);
			setUser(user);
			setObjectType(objType);
			setFilter(regex);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}
}
