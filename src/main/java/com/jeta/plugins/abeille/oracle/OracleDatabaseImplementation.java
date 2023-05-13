package com.jeta.plugins.abeille.oracle;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.plugins.abeille.generic.GenericDatabaseImplementation;

/**
 * This is the Oracle implementation for the database interface
 * 
 * @author Jeff Tassin
 */
public class OracleDatabaseImplementation extends GenericDatabaseImplementation {

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because some databases require exact case for
	 * table names (HSQLDB). So, if the user types in a table name, but the case
	 * does not match, we still want to be able to get the table from the db.
	 * This call converts to the case needed.
	 */
	public String convertCase(String objName) {
		if (objName == null)
			return null;
		else
			return objName.toUpperCase();
	}

	/**
	 * @return the current schema
	 */
	public Schema getCurrentSchema() throws SQLException {
		Schema schema = super.getCurrentSchema();
		if (schema == null) {
			TSConnection conn = getConnection();
			ConnectionInfo info = conn.getConnectionInfo();
			/**
			 * oracle binds a schema to a user, so if the current user is a
			 * schema owner, then return that schema
			 */
			String uname = info.getUserName();
			schema = conn.getSchema(Catalog.VIRTUAL_CATALOG, uname);

			if (schema == null) {
				Collection schemas = conn.getModel(Catalog.VIRTUAL_CATALOG).getSchemas();
				Iterator iter = schemas.iterator();
				while (iter.hasNext()) {
					schema = (Schema) iter.next();
					break;
				}
			}
		}
		return schema;
	}

	public void loadDataTypes() {
		super.loadDataTypes();
		registerDataTypeAlias("VARCHAR", "VARCHAR2");
	}

	/**
	 * Sets the current schema for the currrent user
	 */
	public void setCurrentSchema(Schema schema) throws SQLException {
		if (schema == null)
			return;

		super.setCurrentSchema(schema);
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER SESSION SET current_schema = ");
		sql.append(schema.getName());
		SQLCommand.runModalCommand(getConnection(), I18N.getLocalizedMessage("Set Current Schema"), sql.toString());

	}

}
