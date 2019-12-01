package com.jeta.abeille.gui.security.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
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
 * This table model displays the column grants for given table/ user in MySQL
 * 
 * @author Jeff Tassin
 */
public class ColumnGrantsModel extends GrantsModel {
	/** the id of the table we are showing grants for */
	private TableId m_tableid;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public ColumnGrantsModel(TSConnection connection) {
		super(connection);

		ArrayList names = new ArrayList();
		ArrayList types = new ArrayList();

		names.add(I18N.getLocalizedMessage("Table"));
		types.add(String.class);

		int col_index = 1;
		SecurityService srv = (SecurityService) getConnection().getImplementation(SecurityService.COMPONENT_ID);
		Collection grants = srv.getSupportedPrivileges(DbObjectType.COLUMN);
		Iterator iter = grants.iterator();
		while (iter.hasNext()) {
			Privilege priv = (Privilege) iter.next();
			names.add(priv.getName());
			types.add(Boolean.class);
			setColumnGrantDefinition(col_index, new ColumnGrantDefinition(priv));
			col_index++;
		}

		setColumnNames((String[]) names.toArray(new String[0]));
		setColumnTypes((Class[]) types.toArray(new Class[0]));
	}

	/**
	 * @return the id of the table for these column grants
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * Reloads the model with the current settings
	 * 
	 */
	public void reload() {
		reload(getTableId(), getUser(), getFilter());
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
	public void reload(TableId tableId, AbstractUser user, String regex) {
		try {
			removeAll();
			MySQLSecurityServiceImplementation srv = (MySQLSecurityServiceImplementation) getConnection()
					.getImplementation(MySQLSecurityServiceImplementation.COMPONENT_ID);

			Collection gdefs = srv.getColumnGrants(tableId, tableId.getCatalog(), (UserId) user.getKey());
			addGrants(gdefs, regex);
			setCatalog(tableId.getCatalog());
			setTableId(tableId);
			setUser(user);
			setObjectType(DbObjectType.COLUMN);
			setFilter(regex);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Sets the id of the table for these column grants
	 */
	private void setTableId(TableId tableid) {
		m_tableid = tableid;
	}

}
