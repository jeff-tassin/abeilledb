package com.jeta.plugins.abeille.generic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.gui.command.SQLCommand;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.plugins.abeille.standard.DefaultDatabase;

/**
 * This is the HSQLDB implementation for the database interface
 * 
 * @author Jeff Tassin
 */
public class GenericDatabaseImplementation extends DefaultDatabase {
	/** the current schema. Set by the user */
	private Schema m_schema;

	/**
	 * Flag that indicates if this database supports catalogs
	 */
	private Boolean m_supportsCatalogs;

	/**
	 * Flag that indicates if this database supports schemas
	 */
	private Boolean m_supportsSchemas;

	/**
	 * Flag that indicates if this database supports transactions
	 */
	private Boolean m_supportsTransactions;

	/**
	 * Used in the convertCase method
	 */
	private int m_identifier_case_code = 0;
	private final static int SUPPORTS_MIXED_CASE = 1;
	private final static int SUPPORTS_UPPER_CASE = 2;
	private final static int SUPPORTS_LOWER_CASE = 3;

	/**
	 * ctor
	 */
	public GenericDatabaseImplementation() {

	}

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because some databases require exact case for
	 * table names (HSQLDB). So, if the user types in a table name, but the case
	 * does not match, we still want to be able to get the table from the db.
	 * This call converts to the case needed.
	 */
	public String convertCase(String objName) {
		if (Database.DB2.equals(getConnection().getDatabase())) {
			return toUpper(objName);
		} else {
			if (m_identifier_case_code == 0) {
				try {
					DatabaseMetaData metadata = getConnection().getMetaDataConnection().getMetaData();
					if (metadata.supportsMixedCaseIdentifiers())
						m_identifier_case_code = SUPPORTS_MIXED_CASE;
					else if (metadata.storesUpperCaseIdentifiers())
						m_identifier_case_code = SUPPORTS_UPPER_CASE;
					else if (metadata.storesLowerCaseIdentifiers())
						m_identifier_case_code = SUPPORTS_LOWER_CASE;
					else
						m_identifier_case_code = SUPPORTS_MIXED_CASE;

				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}

			switch (m_identifier_case_code) {
			case SUPPORTS_MIXED_CASE:
				return objName;

			case SUPPORTS_UPPER_CASE:
				return TSUtils.toUpperCase(objName);

			case SUPPORTS_LOWER_CASE:
				return TSUtils.toLowerCase(objName);

			default:
				return objName;
			}
		}
	}

	/**
	 * Converts the case for the given object name to upperCase
	 */
	public static String toUpper(String objName) {
		if (objName == null)
			return null;
		else
			return objName.toUpperCase();
	}

	/**
	 * @return the type name used for auto increment types
	 */
	private String getAutoIncrementType() {
		return "INTEGER IDENTITY";

	}

	/**
	 * @return the current schema
	 */
	public Schema getCurrentSchema() throws SQLException {
		if (Database.DB2.equals(getDatabase())) {
			if (m_schema == null) {
				Schema result = null;
				String sql = "values( current schema )";
				Connection conn = getConnection().getMetaDataConnection();
				String sname = (String) DbUtils.runSingleResultQuery(conn, sql);
				m_schema = getConnection().getModel(getConnection().getDefaultCatalog()).getSchemaInstance(sname);
			}
		}

		return m_schema;
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
		loadDataTypes();
	}

	protected void loadDataTypes() {
		try {
			TSConnection tsconn = getConnection();
			ResultSet rset = tsconn.getMetaData().getTypeInfo();
			while (rset.next()) {
				String typename = rset.getString("TYPE_NAME");
				int data_type = rset.getInt("DATA_TYPE");
				assert (typename != null);
				DataTypeInfo ti = (DataTypeInfo) getDataTypeInfo(typename);
				if (ti == null) {
					// we encountered an unknown type. this should not happen,
					// but if it
					// does, just assume the most liberal settings
					ti = new DataTypeInfo(typename, data_type, "[P,S]");
					registerDataType(ti);
				}

				if (tsconn.getDatabase().equals(Database.ORACLE)) {
					if ("NUMBER".equals(typename)) {
						data_type = java.sql.Types.DECIMAL;
					}
				}
				ti.setDataType(data_type);
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

		if (getConnection().getDatabase() == Database.DB2) {
			StringBuffer sql = new StringBuffer();
			sql.append("SET CURRENT SCHEMA='");
			sql.append(schema.getName());
			sql.append("'");
			SQLCommand.runModalCommand(getConnection(), I18N.getLocalizedMessage("Set Current Schema"), sql.toString());
		}
		m_schema = schema;
	}

	/**
	 * @return true if the given database supports more than one catalog opened
	 *         for a connection as well as the ability to query between catalogs
	 *         for this connection.
	 */
	public boolean supportsCatalogs() throws SQLException {
		if (m_supportsCatalogs == null) {
			try {
				DatabaseMetaData metadata = getConnection().getMetaDataConnection().getMetaData();
				// String dbname = TSUtils.toLowerCase(
				// metadata.getDatabaseProductName() );
				// System.out.println( "GenericDatabaseImpl.supportsCatalog: " +
				// dbname );
				// if ( dbname.indexOf( "daffo" ) >= 0 )
				// {
				// m_supportsCatalogs = Boolean.FALSE;
				// }
				// else
				// {
				m_supportsCatalogs = Boolean.valueOf(metadata.supportsCatalogsInTableDefinitions());
				// }
			} catch (Exception e) {
				TSUtils.printException(e);
				m_supportsCatalogs = Boolean.FALSE;
			}
		}
		return m_supportsCatalogs.booleanValue();
	}

	/**
	 * @return true if the database supports schemas
	 */
	public boolean supportsSchemas() throws SQLException {
		if (m_supportsSchemas == null) {
			try {
				DatabaseMetaData metadata = getConnection().getMetaDataConnection().getMetaData();
				m_supportsSchemas = Boolean.valueOf(metadata.supportsSchemasInTableDefinitions());
			} catch (Exception e) {
				TSUtils.printException(e);
				m_supportsSchemas = Boolean.FALSE;
			}
		}
		return m_supportsSchemas.booleanValue();
	}

	/**
	 * @return true if this database supports transactions
	 */
	public boolean supportsTransactions() {
		if (m_supportsTransactions == null) {
			try {
				DatabaseMetaData metadata = getConnection().getMetaDataConnection().getMetaData();
				m_supportsTransactions = Boolean.valueOf(metadata.supportsTransactions());
			} catch (Exception e) {
				TSUtils.printException(e);
				m_supportsTransactions = Boolean.FALSE;
			}
		}
		return m_supportsTransactions.booleanValue();
	}


	
}
