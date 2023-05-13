package com.jeta.plugins.abeille.mysql;

import java.sql.Connection;
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
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.plugins.abeille.standard.DefaultDatabase;

import com.jeta.foundation.utils.TSUtils;

/**
 * This is the MySQL implementation for the database interface
 * 
 * @author Jeff Tassin
 */
public class MySQLDatabaseImplementation extends DefaultDatabase {
	/** a list of supported table types for this connection */
	private LinkedList m_tabletypes;

	/**
	 * this flag is set if MySQL is case-insensitive - the default when running
	 * on windows
	 */
	private boolean m_lower_case_table_names = false;

	{
		registerMySQLDataType(new MySQLDataTypeInfo("TINYINT", "[P]", java.sql.Types.SMALLINT, true, true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("BIT", "[]", true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("BOOL", "[]", true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("SMALLINT", "[P]", true, true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("MEDIUMINT", "[P]", true, true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("INTEGER", "[P]", true, true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("BIGINT", "[P]", true, true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("FLOAT", "[P,S]", true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("DOUBLE", "[P,S]", true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("DOUBLE PRECISION", "[P,S]", true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("REAL", "[P,S]", true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("DECIMAL", "[P,S]", true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("NUMERIC", "[P,S]", true, true));
		registerMySQLDataType(new MySQLDataTypeInfo("DATE", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("DATETIME", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("TIMESTAMP", "[P]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("TIME", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("YEAR", "[P]", java.sql.Types.SMALLINT));
		registerMySQLDataType(new MySQLDataTypeInfo("CHAR", "[P]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("CHAR BINARY", "[P]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("VARCHAR", "(P)", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("VARCHAR BINARY", "(P)", java.sql.Types.VARCHAR));
		registerMySQLDataType(new MySQLDataTypeInfo("TINYBLOB", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("TINYTEXT", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("BLOB", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("TEXT", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("MEDIUMBLOB", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("MEDIUMTEXT", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("LONGBLOB", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("LONGTEXT", "[]", false, false));
		registerMySQLDataType(new MySQLDataTypeInfo("ENUM", "[]", false, false, false, true));
		registerMySQLDataType(new MySQLDataTypeInfo("SET", "[]", false, false, false, true));

		registerDataTypeAlias("BINARY", "CHAR BINARY");
		registerDataTypeAlias("CHAR VARYING", "VARCHAR");
		registerDataTypeAlias("FLOAT4", "FLOAT");
		registerDataTypeAlias("FLOAT8", "DOUBLE");
		registerDataTypeAlias("INT", "INTEGER");
		registerDataTypeAlias("INT1", "TINYINT");
		registerDataTypeAlias("INT2", "SMALLINT");
		registerDataTypeAlias("INT3", "MEDIUMINT");
		registerDataTypeAlias("INT4", "INTEGER");
		registerDataTypeAlias("INT8", "BIGINT");
		registerDataTypeAlias("LONG VARBINARY", "MEDIUMBLOB");
		registerDataTypeAlias("LONG VARCHAR", "MEDIUMTEXT");
		registerDataTypeAlias("MIDDLEINT", "MEDIUMINT");
		registerDataTypeAlias("VARBINARY", "VARCHAR BINARY");

		if (TSUtils.isDebug()) {
			assert (getDataTypeInfo("VARchar") != null);
			assert (getDataTypeInfo("varchar") != null);
			assert (getDataTypeInfo("VARCHAR") != null);
			assert (getDataTypeInfo("ARCHAR") == null);

		}
	}

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because MySQL can support either case-sensitive
	 * or insensitive modes. For case-insensitive, everything is converted to
	 * lower case
	 */
	public String convertCase(String objName) {
		if (isCaseSensitive()) {
			return objName;
		} else {
			if (objName == null)
				return null;
			else
				return objName.toLowerCase();
		}
	}

	private void registerMySQLDataType(MySQLDataTypeInfo dtype) {
		registerDataType(dtype);
		if (dtype.supportsUnsigned()) {
			if (dtype.supportsZeroFill()) {
				StringBuffer typename = new StringBuffer();
				typename.append(dtype.getTypeName());
				typename.append(" UNSIGNED");
				typename.append(" ZEROFILL");
				registerDataTypeAlias(typename.toString(), dtype.getTypeName());
			} else {
				StringBuffer typename = new StringBuffer();
				typename.append(dtype.getTypeName());
				typename.append(" UNSIGNED");
				registerDataTypeAlias(typename.toString(), dtype.getTypeName());
			}
		} else if (dtype.supportsZeroFill()) {
			StringBuffer typename = new StringBuffer();
			typename.append(dtype.getTypeName());
			typename.append(" ZEROFILL");
			registerDataTypeAlias(typename.toString(), dtype.getTypeName());
		}
	}

	/**
	 * ctor
	 */
	public MySQLDatabaseImplementation() {

	}

	/**
	 * Creates a default data type. This is used when the user creates a column
	 * with a type that is not registered. So, we simply create a new type with
	 * the most liberal settings.
	 */
	public DataTypeInfo createDefaultDataType(String typeName) {
		TSUtils.printMessage("MySQLDatabaseImpl.createDefaultDataType  found unregistered type: " + typeName);

		DataTypeInfo info = getDataTypeInfo(typeName);
		if (info == null) {
			if (TSUtils.isDebug()) {
				Exception e = new Exception();
				e.printStackTrace();
			}

			// just create a type with the most liberal settings
			info = new MySQLDataTypeInfo(typeName, "[P,S]", true, true);
		}

		return info;
	}

	/**
	 * @return the table types supported by this database
	 */
	public synchronized Collection getSupportedTableTypes() {
		if (m_tabletypes == null) {
			TSConnection tsconn = getConnection();

			m_tabletypes = new LinkedList();
			Connection conn = null;
			Statement stmt = null;

			try {
				m_tabletypes.add(MySQLTableType.MyISAM);
				m_tabletypes.add(MySQLTableType.HEAP);
				m_tabletypes.add(MySQLTableType.MERGE);

				String sql = "show variables";

				conn = tsconn.getMetaDataConnection();
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sql.toString());
				while (rset.next()) {
					String vname = rset.getString("Variable_name");
					String value = rset.getString("Value");
					if (vname.equals("have_bdb") && value.equalsIgnoreCase("YES")) {
						m_tabletypes.add(MySQLTableType.BDB);
					} else if (vname.equals("have_innodb") && value.equalsIgnoreCase("YES")) {
						m_tabletypes.add(MySQLTableType.InnoDB);
					} else if (vname.equals("have_isam") && value.equalsIgnoreCase("YES")) {
						m_tabletypes.add(MySQLTableType.ISAM);
					} else if (vname.equals("lower_case_table_names")
							&& (value.equalsIgnoreCase("ON") || value.equals("1"))) {
						m_lower_case_table_names = true;
					}
				}

				java.util.Collections.sort(m_tabletypes);
			} catch (Exception e) {
				m_tabletypes.clear();
				TSUtils.printException(e);
				m_tabletypes.addAll(MySQLTableType.getTypes());
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}
		}
		return m_tabletypes;
	}

	/**
	 * @return true if table names are case-sensitive for this database.
	 */
	public boolean isCaseSensitive() {
		// let's call getSupportTableTypes because it picks up the variable,
		// lower_case_table_names
		getSupportedTableTypes();
		return !m_lower_case_table_names;
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
				int precision = rset.getInt("PRECISION");
				short min_scale = rset.getShort("MINIMUM_SCALE");
				short max_scale = rset.getShort("MAXIMUM_SCALE");

				MySQLDataTypeInfo ti = (MySQLDataTypeInfo) getDataTypeInfo(typename);
				if (ti == null) {
					TSUtils.printMessage("MySQLDatabaseImpl encounted unknown type: " + typename);
					assert (false);

					// we encountered an unknown type. this should not happen,
					// but if it
					// does, just assume the most liberal settings
					ti = new MySQLDataTypeInfo(typename, "[P,S]", true, true);
					registerDataType(ti);
				}

				ti.setDataType(data_type);
				ti.setPrecision(precision);
				ti.setMinimumScale(min_scale);
				ti.setMaximumScale(max_scale);
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * @return true if the given database supports more than one catalog opened
	 *         for a connection as well as the ability to query between catalogs
	 *         for this connection. PostgreSQL <= 7.3 does not support this by
	 *         MySQL does.
	 */
	public boolean supportsCatalogs() throws SQLException {
		return true;
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
	public boolean supportsSchemas(Connection c) throws SQLException {
		return false;
	}

	/**
	 * @return true if this database supports transactions
	 */
	public boolean supportsTransactions() {
		return false;
	}

}
