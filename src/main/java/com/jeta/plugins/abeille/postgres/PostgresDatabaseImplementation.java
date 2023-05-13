package com.jeta.plugins.abeille.postgres;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbVersion;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.plugins.abeille.standard.DefaultDatabase;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the Postgres implementation for the database interface
 * 
 * @author Jeff Tassin
 */
public class PostgresDatabaseImplementation extends DefaultDatabase {
	{
		registerDataType(new DataTypeInfo("bigint", "[]"));
		registerDataType(new DataTypeInfo("bigserial", "[]", "dtype_autoincrement16.gif"));
		registerDataType(new DataTypeInfo("boolean", "[]"));
		registerDataType(new DataTypeInfo("bytea", "[]"));
		registerDataType(new DataTypeInfo("character", "(P)"));
		registerDataType(new DataTypeInfo("date", "[]"));
		registerDataType(new DataTypeInfo("double precision", "[]"));
		registerDataType(new DataTypeInfo("integer", "[]"));
		registerDataType(new DataTypeInfo("numeric", "[P,S]"));
		registerDataType(new DataTypeInfo("real", "[]"));
		registerDataType(new DataTypeInfo("serial", "[]", "dtype_autoincrement16.gif"));
		registerDataType(new DataTypeInfo("smallint", "[]"));
		registerDataType(new DataTypeInfo("varchar", java.sql.Types.VARCHAR, "(P)"));
		registerDataType(new DataTypeInfo("text", "[]"));
		registerDataType(new DataTypeInfo("time", "[P]"));
		registerDataType(new DataTypeInfo("timestamp", "[P]"));

		registerDataTypeAlias("int8", "bigint");
		registerDataTypeAlias("serial8", "bigserial");
		registerDataTypeAlias("bool", "boolean");
		registerDataTypeAlias("char", "character");
		registerDataTypeAlias("float8", "double precision");
		registerDataTypeAlias("int", "integer");
		registerDataTypeAlias("int4", "integer");
		registerDataTypeAlias("float4", "real");
		registerDataTypeAlias("int2", "smallint");
		registerDataTypeAlias("serial4", "serial");
		registerDataTypeAlias("decimal", "numeric");
		registerDataTypeAlias("bpchar", "character");

		// registerDataTypeAlias( "binary", "bytea" );
	}

	/**
	 * ctor
	 */
	public PostgresDatabaseImplementation() {

	}

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
			return objName.toLowerCase();
	}

	/**
	 * @return the current schema
	 */
	public Schema getCurrentSchema() throws SQLException {
		if (supportsSchemas()) {
			Schema result = null;
			String sql = "select CURRENT_SCHEMA()";
			Connection conn = getConnection().getMetaDataConnection();

			String sname = (String) DbUtils.runSingleResultQuery(conn, sql);
			result = getConnection().getModel(getConnection().getDefaultCatalog()).getSchemaInstance(sname);
//			if ( !getConnection().isAutoCommit() ) {
//				conn.commit();
//			}
			return result;
		} else {
			return Schema.VIRTUAL_SCHEMA;
		}
	}

	/**
	 * @return a collection of Schema objects for the current user
	 */
	public Collection getSchemas() throws SQLException {
		return new java.util.LinkedList();
	}

	/**
	 * @return true if this database requires a rollback after an exception
	 *         occurs
	 */
	public boolean rollbackOnException() {
		// postgres requires a rollback on any exception or connection goes into
		// a funny state
		return true;
	}

	public void setConnection(TSConnection conn) {
		super.setConnection(conn);
		try {
			ResultSet rset = getConnection().getMetaDataConnection().getMetaData().getTypeInfo();
			while (rset.next()) {
				String typename = rset.getString("TYPE_NAME");
				int data_type = rset.getInt("DATA_TYPE");
				assert (typename != null);

				DataTypeInfo ti = (DataTypeInfo) getDataTypeInfo(typename);
				if (ti == null) {
					// TSUtils.printMessage(
					// "PostgresDatabaseImpl encounted unknown type: " +
					// typename );

					// we encountered an unknown type. this should not happen,
					// but if it
					// does, just assume the most liberal settings
					// ti = new DataTypeInfo( typename, "[P,S]" );
					// registerDataType( ti );
				} else {
					ti.setDataType(data_type);
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Sets the current schema for the currrent user
	 */
	public void setCurrentSchema(Schema schema) throws SQLException {
		if (schema == null)
			return;

		if (supportsSchemas()) {
			StringBuffer sql = new StringBuffer();
			sql.append("set SEARCH_PATH to '");
			sql.append(schema.getName());
			sql.append("'");
			SQLCommand.runModalCommand(getConnection(), I18N.getLocalizedMessage("Set Current Schema"), sql.toString());
		}
	}

	/**
	 * @return true if the given database supports more than one catalog opened
	 *         for a connection as well as the ability to query between catalogs
	 *         for this connection. PostgreSQL <= 7.3 does not support this but
	 *         MySQL does.
	 */
	public boolean supportsCatalogs() throws SQLException {
		return false;

	}

	/**
	 * This method checks if a given feature is supported by the database in
	 * Abeille. For example, we currently only allow modeling in PostgreSQL and
	 * MySQL. If the database is not one of these, we need to disable the create
	 * table feature in the ModelViewFrame. IMPORTANT: By convention, you should
	 * begin any feature names with the string: checked.feature. So,
	 * ID_CREAT_TABLE = "checked.feature.model.create.table"; ID_CREAT_INDEX =
	 * "checked.feature.indexes.create.index"; etc.
	 * 
	 * @return true if the database supports the given feature.
	 * 
	 */
	public boolean supportsFeature(String featuresName) {
		assert (featuresName.indexOf("checked.feature.") == 0);
		return true;
	}

	/**
	 * @return false. Postgres does not support schemas
	 */
	public boolean supportsSchemas() throws SQLException {

		DbVersion version = getVersion();
		if (version.getMajor() == 7 && version.getMinor() >= 3) {
			return true;
		} else if (version.getMajor() > 7) {
			return true;
		} else {
			return false;
		}
	}

}
