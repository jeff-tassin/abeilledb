package com.jeta.plugins.abeille.standard;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.DbVersion;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.utils.EmptyCollection;
import com.jeta.foundation.utils.TSUtils;

/**
 * DefaultDatabase implementation
 * 
 * @author Jeff Tassin
 */
public abstract class DefaultDatabase implements TSDatabase {

	/**
	 * Hash of type names (key) to DataTypeInfo objects for a given database
	 */
	private TreeMap m_datatypes = new TreeMap(String.CASE_INSENSITIVE_ORDER);

	/**
	 * A map of alias types names (string objects) key to registered data type
	 * names (string objects) values
	 */
	private TreeMap m_datatypeAlias = new TreeMap(String.CASE_INSENSITIVE_ORDER);

	/** the connection */
	private TSConnection m_connection;

	/** the version of this database */
	private DbVersion m_version;

	/**
	 * ctor
	 */
	public DefaultDatabase() {
	}

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because some databases require exact case for
	 * table names (HSQLDB). So, if the user types in a table name, but the case
	 * does not match, we still want to be able to get the table from the db.
	 * This call converts to the case needed.
	 */
	public String convertCase(String objName) {
		return objName;
	}

	/**
	 * Creates a default data type. This is used when the user creates a column
	 * with a type that is not registered. So, we simply create a new type with
	 * the most liberal settings.
	 */
	public DataTypeInfo createDefaultDataType(String typeName) {
		DataTypeInfo info = getDataTypeInfo(typeName);
		assert (info == null);

		// just create a type with the most liberal settings
		return new DataTypeInfo(typeName, 0, "[P,S]");
	}

	/**
	 * @return the underlying database connnection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the current schema for the currrent user
	 */
	public Schema getCurrentSchema() throws SQLException {
		return null;
	}

	/**
	 * @return the database for this connection
	 */
	public Database getDatabase() {
		return getConnection().getDatabase();
	}

	/**
	 * @return the data type info object for the given type name
	 */
	public DataTypeInfo getDataTypeInfo(String typename) {
		if (typename == null)
			return null;

		typename = typename.toLowerCase();
		DataTypeInfo result = (DataTypeInfo) m_datatypes.get(typename);
		if (result == null)// if null, then try an alias
		{

			String alias = (String) m_datatypeAlias.get(typename);
			if (alias != null) {
				result = (DataTypeInfo) m_datatypes.get(alias);
			}
		}

		return result;
	}

	/**
	 * @return a collection of Schema objects for the current user
	 */
	public Collection getSchemas() throws SQLException {
		return new java.util.LinkedList();
	}

	/**
	 * @return a collection of data types (DataTypeInfo objects) that this
	 *         database supports
	 */
	public Collection getSupportedTypes() {
		return m_datatypes.values();
	}

	/**
	 * @return the major/minor/sub version for this database
	 */
	public DbVersion getVersion() {
		if (m_version == null) {
			try {
				DatabaseMetaData metadata = getConnection().getMetaDataConnection().getMetaData();
				String ver = metadata.getDatabaseProductVersion();
				if (ver == null)
					m_version = new DbVersion("", 0, 0, 0);
				else {
					ver = ver.trim();

					int major = 0;
					int minor = 0;
					int sub = 0;
					// strip off any text information from the version string.
					// Some databases
					// will put a trailing message on the version such as 4.0.2
					// (beta)
					StringBuffer digits = new StringBuffer();
					for (int index = 0; index < ver.length(); index++) {
						char c = ver.charAt(index);
						if (Character.isDigit(c) || c == '.') {
							digits.append(c);
						}
					}

					int pcount = 0;
					StringTokenizer st = new StringTokenizer(digits.toString(), ".");
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (pcount == 0) {
							major = Integer.parseInt(token);
						} else if (pcount == 1) {
							minor = Integer.parseInt(token);
						} else if (pcount == 2) {
							sub = Integer.parseInt(token);
							break;
						}
						pcount++;
					}

					m_version = new DbVersion(ver, major, minor, sub);
					m_version.print();
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
		return m_version;
	}

	/**
	 * @return true if table names are case-sensitive for this database.
	 */
	public boolean isCaseSensitive() {
		return false;
	}

	/**
	 * Registers a known data type for the given database.
	 * 
	 */
	public void registerDataType(DataTypeInfo info) {
		assert (info != null);
		String typename = info.getTypeName().toLowerCase();
		assert (typename != null);
		assert (!m_datatypes.containsKey(typename));
		m_datatypes.put(typename, info);
	}

	/**
	 * Registers a datatype that can be called by a different name. Note that
	 * you must have previously registered the registeredTypeName
	 */
	public void registerDataTypeAlias(String aliasName, String registeredTypeName) {
		if (getDataTypeInfo(registeredTypeName) == null) {
			TSUtils.printMessage("registerDataTypeAlias failed for alias: " + aliasName + "  registered: "
					+ registeredTypeName);
			assert (false);
		}
		m_datatypeAlias.put(aliasName.toLowerCase(), registeredTypeName);
	}

	/**
	 * @return true if this database requires a rollback after an exception
	 *         occurs
	 */
	public boolean rollbackOnException() {
		return true;
	}

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn) {
		m_connection = conn;
	}

	/**
	 * Sets the current schema for the currrent user
	 */
	public void setCurrentSchema(Schema schema) throws SQLException {
		// no op
	}

	/**
	 * @return true if this database supports canceling a SQL statement
	 */
	public boolean supportsCancelStatement() {
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
		return false;
	}

	/**
	 * @return true if this database supports schemas
	 */
	public boolean supportsSchemas() throws SQLException {
		return false;
	}

	/**
	 * @return true if this database supports transactions
	 */
	public boolean supportsTransactions() {
		return true;
	}

	public String getFullyQualifiedName( DbObjectId objId ) {
		return objId.getFullyQualifiedName();
	}

}
