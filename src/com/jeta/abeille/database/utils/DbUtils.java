package com.jeta.abeille.database.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.SchemaComparator;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSConnectionMgr;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSTable;

import com.jeta.abeille.logger.DbLogger;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.i18n.I18NHelper;
import com.jeta.foundation.utils.IntegerHashtable;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is a utility class for common database operations
 * 
 * @author Jeff Tassin
 */
public class DbUtils {
	public static final ImageIcon CATALOG_ICON = TSGuiToolbox.loadImage("incors/16x16/data_gear.png");
	public static final ImageIcon TABLE_ICON = TSGuiToolbox.loadImage("incors/16x16/table_sql.png");
	public static final ImageIcon AUTO_INCREMENT_ICON = TSGuiToolbox.loadImage("dtype_autoincrement16.gif");
	public static final ImageIcon SCHEMA_ICON = TSGuiToolbox.loadImage("incors/16x16/folder_cubes.png");
	public static final ImageIcon PROCEDURE_ICON = TSGuiToolbox.loadImage("incors/16x16/gear.png");
	public static final ImageIcon USER_ICON = TSGuiToolbox.loadImage("incors/16x16/businessman.png");
	public static final ImageIcon GROUP_ICON = TSGuiToolbox.loadImage("incors/16x16/businessmen.png");

	public static IntegerHashtable m_typeicons = new IntegerHashtable();

	public static ImageIcon m_generalicon = TSGuiToolbox.loadImage("dtype_general16.gif");

	/**
	 * some common verbs in SQL (in localized form). This values are used mainly
	 * to display information in a JTable. We cache here so the table renderer
	 * does not have to do a lookup of the resource everytime the table needs
	 * repainting
	 */
	public static final String I18N_AFTER = I18N.getLocalizedMessage("After");
	public static final String I18N_BEFORE = I18N.getLocalizedMessage("Before");
	public static final String I18N_INSTEAD = I18N.getLocalizedMessage("Instead");
	public static final String I18N_INSERT = I18N.getLocalizedMessage("Insert");
	public static final String I18N_DELETE = I18N.getLocalizedMessage("Delete");
	public static final String I18N_UPDATE = I18N.getLocalizedMessage("Update");
	public static final String I18N_STATEMENT = I18N.getLocalizedMessage("Statement");
	public static final String I18N_ROW = I18N.getLocalizedMessage("Row");
	public static final String I18N_CHECK = I18N.getLocalizedMessage("Check");
	public static final String I18N_UNIQUE = I18N.getLocalizedMessage("Unique");

	static {
		m_typeicons.put(java.sql.Types.BIGINT, new JDBCTypeInfo("BIGINT", "dtype_bigint16.gif"));
		m_typeicons.put(java.sql.Types.BINARY, new JDBCTypeInfo("BINARY", "dtype_blob16.gif"));
		m_typeicons.put(java.sql.Types.BIT, new JDBCTypeInfo("BIT", "dtype_bool16.gif"));
		m_typeicons.put(java.sql.Types.BOOLEAN, new JDBCTypeInfo("BOOLEAN", "dtype_bool16.gif"));
		m_typeicons.put(java.sql.Types.BLOB, new JDBCTypeInfo("BLOB", "dtype_blob16.gif"));
		m_typeicons.put(java.sql.Types.CHAR, new JDBCTypeInfo("CHAR", "dtype_char16.gif"));
		m_typeicons.put(java.sql.Types.CLOB, new JDBCTypeInfo("CLOB", "dtype_clob16.gif"));
		// m_typeicons.put( java.sql.Types.DATE, new JDBCTypeInfo( "DATE",
		// "calendar16.gif" ) );
		m_typeicons.put(java.sql.Types.DATE, new JDBCTypeInfo("DATE", "incors/16x16/calendar.png"));
		m_typeicons.put(java.sql.Types.DECIMAL, new JDBCTypeInfo("DECIMAL", "dtype_decimal16.gif"));
		m_typeicons.put(java.sql.Types.DOUBLE, new JDBCTypeInfo("DOUBLE", "dtype_double16.gif"));
		m_typeicons.put(java.sql.Types.FLOAT, new JDBCTypeInfo("FLOAT", "dtype_real16.gif"));
		m_typeicons.put(java.sql.Types.INTEGER, new JDBCTypeInfo("INTEGER", "dtype_int16.gif"));
		m_typeicons.put(java.sql.Types.JAVA_OBJECT, new JDBCTypeInfo("JAVA_OBJECT", "dtype_blob16.gif"));
		m_typeicons.put(java.sql.Types.LONGVARBINARY, new JDBCTypeInfo("LONGVARBINARY", "dtype_blob16.gif"));
		m_typeicons.put(java.sql.Types.LONGVARCHAR, new JDBCTypeInfo("LONGVARCHAR", "dtype_longvarchar16.gif"));
		m_typeicons.put(java.sql.Types.NUMERIC, new JDBCTypeInfo("NUMERIC", "dtype_numeric16.gif"));
		m_typeicons.put(java.sql.Types.REAL, new JDBCTypeInfo("REAL", "dtype_real16.gif"));
		m_typeicons.put(java.sql.Types.SMALLINT, new JDBCTypeInfo("SMALLINT", "dtype_smallint16.gif"));
		// m_typeicons.put( java.sql.Types.TIME, new JDBCTypeInfo( "TIME",
		// "dtype_time16.gif" ) );
		// m_typeicons.put( java.sql.Types.TIMESTAMP, new JDBCTypeInfo(
		// "TIMESTAMP", "dtype_timestamp16.gif" ) );
		m_typeicons.put(java.sql.Types.TIME, new JDBCTypeInfo("TIME", "incors/16x16/clock.png"));
		m_typeicons.put(java.sql.Types.TIMESTAMP, new JDBCTypeInfo("TIMESTAMP", "incors/16x16/clock_run.png"));
		m_typeicons.put(java.sql.Types.VARCHAR, new JDBCTypeInfo("VARCHAR", "dtype_varchar16.gif"));
	}

	/**
	 * Creates a hashmap of column names (String objects, keys) to
	 * ColumnMetaData objects (values).
	 * 
	 * @param localColumns
	 *            a collection of ColumnMetaData objects to map
	 */
	public static HashMap createColumnMap(Collection cols) {
		HashMap result = new HashMap();
		if (cols != null) {
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				result.put(cmd.getColumnName(), cmd);
			}
		}
		return result;
	}

	/**
	 * Creates an array of columnmetadata objects based on a given result set
	 * metadata.
	 * 
	 * @param metadata
	 *            the result set metadata we will use to create the column
	 *            metadata array
	 * @return the column metadata objects array
	 * @throws SQLException
	 */
	public static ColumnMetaData[] createColumnMetaData(TSConnection conn, Catalog catalog, ResultSetMetaData metadata)
			throws SQLException {

		ColumnMetaData[] result = new ColumnMetaData[metadata.getColumnCount()];
		for (int index = 1; index <= metadata.getColumnCount(); index++) {

			String colname = TSUtils.fastTrim(metadata.getColumnName(index));
			int coltype = metadata.getColumnType(index);

			String typename = "";

			if (!conn.getDatabase().equals(Database.SYBASE)) {
				typename = metadata.getColumnTypeName(index);
			}

			int colsize = 0;
			int decimal_digits = 0;
			boolean nullable = true;

			try {
				// oracle has problems with get size for some data types
				if (!conn.getDatabase().equals(Database.ORACLE)) {
					colsize = metadata.getPrecision(index);
					decimal_digits = metadata.getScale(index);
					nullable = (metadata.isNullable(index) == ResultSetMetaData.columnNullable);
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}

			String schema = null;
			String table = null;
			Schema sch = null;

			if (!Database.SYBASE.equals(conn.getDatabase())) {
				try {
					schema = TSUtils.fastTrim(metadata.getSchemaName(index));
					table = TSUtils.fastTrim(metadata.getTableName(index));

					if (conn.supportsCatalogs()) {
						if (catalog == null) {
							String catname = TSUtils.fastTrim(metadata.getCatalogName(index));
							if (catname != null && catname.length() > 0)
								catalog = Catalog.createInstance(catname);
							else
								catalog = Catalog.EMPTY_CATALOG;
						}

						if (conn.supportsSchemas()) {
							DbModel model = conn.getModel(catalog);
							sch = model.getSchemaInstance(schema);
						} else {
							sch = Schema.VIRTUAL_SCHEMA;
						}
					} else {
						catalog = Catalog.VIRTUAL_CATALOG;
						if (conn.supportsSchemas()) {
							DbModel model = conn.getModel(catalog);
							sch = model.getSchemaInstance(schema);
						} else {
							sch = Schema.VIRTUAL_SCHEMA;
						}
					}
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}

			if (catalog == null)
				catalog = Catalog.EMPTY_CATALOG;

			if (sch == null)
				sch = Schema.EMPTY_SCHEMA;

			TableId parentid = new TableId(catalog, sch, table);
			ColumnMetaData cmd = new ColumnMetaData(colname, coltype, typename, colsize, parentid, nullable);
			cmd.setAlias(TSUtils.fastTrim(metadata.getColumnLabel(index)));

			/**
			 * postgres JDBC driver sends back a null table here. So we need to
			 * create a distinct columnid for the columnmetadata or columns with
			 * the same name from different tables will cause problems
			 */
			if (table == null || table.trim().length() == 0) {
				TableId tag = new TableId(catalog, sch, String.valueOf(index));
				cmd.setParentTableId(tag);
			}

			result[index - 1] = cmd;
		}
		return result;
	}

	/**
	 * Removes the table from the database using the DROP TABLE sql command
	 * 
	 * @param tableId
	 *            the id of the table to drop
	 * @throws SQLException
	 *             is an error occurs
	 */
	public static void dropTable(TSConnection connection, TableId tableId) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("DROP TABLE ");
		sql.append(tableId.getFullyQualifiedName());
		com.jeta.abeille.gui.command.SQLCommand.runMetaDataCommand(connection, I18N.getLocalizedMessage("Drop Table"),
				sql.toString());
		// now notify all components that table has been dropped
	}

	public static void deleteAllRows(TableId tableId) throws SQLException {
		// DELETE * FROM TABLE

		I18NHelper i18n = I18NHelper.getInstance();

		StringBuffer sql = new StringBuffer();
		sql.append(i18n.getLocalizedMessage("DELETE"));
		sql.append(i18n.space());
		sql.append(i18n.getLocalizedMessage("FROM"));
		sql.append(i18n.space());
		sql.append(tableId.getTableName());
	}

	/**
	 * Creates the SQL that corresponds to the given table metadata
	 */
	public static String createTableSQL(TSConnection tsconn, TableMetaData tmd) throws SQLException {
		TSTable td = (TSTable) tsconn.getImplementation(TSTable.COMPONENT_ID);
		return td.createTableSQL(tmd);
	}

	public static void executeMetaDataSQL(TSConnection tsconn, String sql) throws SQLException {
		// here we run directly because we don't want to log the actual password
		Connection conn = null;
		Statement stmt = null;
		try {
			if (TSUtils.isDebug()) {
				TSUtils.printMessage(sql);
			}
			conn = tsconn.getMetaDataConnection();
			stmt = conn.createStatement();
			stmt.execute(sql);
			stmt.close();
			// conn.commit();
			tsconn.jetaCommit(conn);
		} catch (SQLException e) {
			if (tsconn.supportsTransactions()) {
				conn.rollback();
			}
			throw e;
		} finally {
			tsconn.release(conn);
		}
	}

	/**
	 * Converts the binary data from an inputstream to a byte array
	 */
	public static byte[] getBinaryData(InputStream istream) throws java.io.IOException {
		// rest the stream pointer to the first byte
		if (istream == null)
			return null;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int numread = istream.read(buff);
		while (numread > 0) {
			bos.write(buff, 0, numread);
			numread = istream.read(buff);
		}
		return bos.toByteArray();
	}

	/**
	 * Converts the binary data for a given column in a result set to a byte
	 * array.
	 */
	public static byte[] getBinaryData(ResultSet rset, int colIndex) throws SQLException {
		try {
			if (rset == null)
				return null;

			InputStream istream = null;

			Object obj = rset.getObject(colIndex);
			if (obj instanceof Blob) {
				Blob blob = (Blob) obj;
				istream = blob.getBinaryStream();
			} else if (obj == null) {
				return null;
			} else {
				istream = rset.getBinaryStream(colIndex);
			}

			if (istream != null) {
				return DbUtils.getBinaryData(istream);
			} else {
				throw new SQLException(I18N.format("getBinaryStream_failed_1", TSUtils.getInteger(colIndex)));
			}
		} catch (java.io.IOException ioe) {
			throw new SQLException(ioe.getMessage());
		}
	}

	/**
	 * Converts the binary data for a given column in a result set to a byte
	 * array.
	 */
	public static byte[] getBinaryData(ResultSet rset, String colName) throws SQLException {
		try {
			if (rset == null)
				return null;

			InputStream istream = null;

			Object obj = rset.getObject(colName);
			if (obj instanceof Blob) {
				Blob blob = (Blob) obj;
				istream = blob.getBinaryStream();
			} else if (obj == null) {
				return null;
			} else {
				istream = rset.getBinaryStream(colName);
			}

			if (istream != null) {
				return DbUtils.getBinaryData(istream);
			} else {
				throw new SQLException(I18N.format("getBinaryStream_failed_1", colName));
			}
		} catch (java.io.IOException ioe) {
			throw new SQLException(ioe.getMessage());
		}
	}

	/**
	 * Converts the binary data for a given column in a result set to a byte
	 * array.
	 */
	public static byte[] getBinaryData(Blob blob) throws SQLException {
		if (blob == null)
			return null;

		try {
			InputStream istream = blob.getBinaryStream();
			return DbUtils.getBinaryData(istream);
		} catch (java.io.IOException ioe) {
			throw new SQLException(ioe.getMessage());
		}

	}

	/**
	 * Converts the character data for a given column in a result set to a
	 * String.
	 */
	public static String getCharacterData(java.io.Reader reader) throws java.io.IOException {
		if (reader == null)
			return null;

		try {
			reader.reset();
		} catch (Exception e) {
			if (TSUtils.isDebug()) {
				TSUtils.printException(e);
			}
		}

		java.io.StringWriter writer = new java.io.StringWriter();
		char[] buff = new char[1024];
		int numread = reader.read(buff);
		while (numread > 0) {
			writer.write(buff, 0, numread);
			numread = reader.read(buff);
		}
		return writer.toString();
	}

	/**
	 * Converts the character data for a given column in a result set to a
	 * String.
	 */
	public static String getCharacterData(ResultSet rset, int index) throws SQLException {
		try {
			if (rset == null)
				return null;

			Object obj = rset.getObject(index);
			if (obj instanceof Clob) {
				Clob clob = (Clob) obj;
				return DbUtils.getCharacterData(clob);
			} else if (obj == null) {
				return null;
			} else {
				return DbUtils.getCharacterData(rset.getCharacterStream(index));
			}
		} catch (java.io.IOException ioe) {
			throw new SQLException(ioe.getMessage());
		}
	}

	/**
	 * Converts the character data for a given column in a result set to a
	 * String.
	 */
	public static String getCharacterData(java.sql.Clob clob) throws SQLException {
		try {
			if (clob == null)
				return null;

			return DbUtils.getCharacterData(clob.getCharacterStream());
		} catch (java.io.IOException ioe) {
			throw new SQLException(ioe.getMessage());
		}
	}

	/**
	 * Converts the character data for a given column in a result set to a
	 * String.
	 */
	public static String getCharacterData(ResultSet rset, String colName) throws SQLException {
		try {
			if (rset == null)
				return null;

			return DbUtils.getCharacterData(rset.getCharacterStream(colName));
		} catch (java.io.IOException ioe) {
			throw new SQLException(ioe.getMessage());
		}
	}

	/**
	 * @return the current schema used by the application. Currently, this
	 *         returns the schema owned by the current user (if the user owns
	 *         one).
	 * @todo - if the current user does not own a schema, then we should return
	 *       the schema that the user selected last
	 */
	public static Schema getCurrentSchema() {
		return null;
	}

	/**
	 * Returns the number of rows in a table
	 * 
	 * @param tableId
	 *            the id of the table
	 * @return the number of rows in the given table.
	 */
	public static long getTableCount(TSConnection connection, TableId tableId) throws SQLException {
		if (!connection.isQueryTableCounts())
			return -1;

		long count = -1;
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from ");
		sql.append(tableId.getFullyQualifiedName());

		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = connection.createStatement();
			rset = stmt.executeQuery(sql.toString());
			rset.next();
			count = rset.getLong(1);
		} finally {
			try {
				if (rset != null)
					rset.close();
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	/**
	 * @return the data types registered for the given connection
	 */
	public static Collection getDataTypes(TSConnection conn) {
		TSDatabase dbimpl = (TSDatabase) conn.getImplementation(TSDatabase.COMPONENT_ID);
		return dbimpl.getSupportedTypes();
	}

	/**
	 * @return the data type info structure for the given type name Null is
	 *         returned if the type name can't be found
	 */
	public static DataTypeInfo getDataTypeInfo(TSConnection conn, String typeName, boolean bcreate) {
		TSDatabase dbimpl = (TSDatabase) conn.getImplementation(TSDatabase.COMPONENT_ID);
		DataTypeInfo result = dbimpl.getDataTypeInfo(typeName);
		if (result == null && bcreate) {
			TSUtils.printMessage("********** DbUtils.getDataTypeInfo found unknown type:  typeName " + typeName);
			result = dbimpl.createDefaultDataType(typeName);
		}

		return result;
	}

	public static String getIconName(int dataType) {
		JDBCTypeInfo jinfo = (JDBCTypeInfo) m_typeicons.get(dataType);
		if (jinfo == null) {
			return "dtype_general16.gif";
		} else {
			return jinfo.getIconName();
		}
	}

	public static ImageIcon getIcon(int dataType) {
		JDBCTypeInfo jinfo = (JDBCTypeInfo) m_typeicons.get(dataType);
		if (jinfo == null) {
			return m_generalicon;
		} else {
			return jinfo.getIcon();
		}
	}

	/**
	 * @return the string label for a given transaction isolation value
	 */
	public static String getConcurrencyString(int c) {
		if (c == ResultSet.CONCUR_READ_ONLY)
			return "CONCUR_READ_ONLY";
		else if (c == ResultSet.CONCUR_UPDATABLE)
			return "CONCUR_UPDATABLE";
		else
			return "";
	}

	/**
	 * @return the string label for a given result set holdability
	 */
	public static String getHoldabilityString(int h) {
		if (h == ResultSet.HOLD_CURSORS_OVER_COMMIT)
			return "HOLD_CURSORS_OVER_COMMIT";
		else if (h == ResultSet.CLOSE_CURSORS_AT_COMMIT)
			return "CLOSE_CURSORS_AT_COMMIT";
		else
			return "";
	}

	/**
	 * @return the string label for a given transaction isolation value
	 */
	public static String getIsolationString(int isolation) {
		return TSConnection.getIsolationString(isolation);
	}

	public static String getJDBCTypeName(int jdbcType) {
		JDBCTypeInfo jinfo = (JDBCTypeInfo) m_typeicons.get(jdbcType);
		if (jinfo != null)
			return jinfo.getJDBCTypeName();
		else
			return "UNKNOWN";
	}

	public static String getQualifiedName(Catalog currentCatalog, Schema currentSchema) {
		boolean catalog = false;
		StringBuffer sname = new StringBuffer();
		if (currentCatalog != Catalog.VIRTUAL_CATALOG && currentCatalog != null) {
			sname.append(currentCatalog.getName());
			catalog = true;
		}

		if (currentSchema != Schema.VIRTUAL_SCHEMA && currentSchema != null) {
			if (catalog)
				sname.append(".");

			sname.append(currentSchema.getName());
		}
		return sname.toString();
	}

	public static String getQualifiedName(Catalog currentCatalog, Schema currentSchema, String objName) {
		StringBuffer sname = new StringBuffer();
		if (currentCatalog != Catalog.VIRTUAL_CATALOG && currentCatalog != null) {
			sname.append(currentCatalog.getName());
			sname.append(".");
		}

		if (currentSchema != Schema.VIRTUAL_SCHEMA && currentSchema != null) {
			sname.append(currentSchema.getName());
			sname.append(".");
		}

		sname.append(objName);
		return sname.toString();
	}

	public static String getQualifiedName(Catalog currentCatalog, Schema currentSchema, TableId tableId) {
		if (tableId == null)
			return "";

		if (currentCatalog == null || currentSchema == null)
			return tableId.getFullyQualifiedName();

		StringBuffer sname = new StringBuffer();
		if (!currentCatalog.equals(tableId.getCatalog()) || !currentSchema.equals(tableId.getSchema())) {
			if (tableId.getCatalog() != Catalog.VIRTUAL_CATALOG && tableId.getCatalog() != null) {
				sname.append(tableId.getCatalog().getName());
				sname.append(".");
			}

			if (tableId.getSchema() != Schema.VIRTUAL_SCHEMA && tableId.getSchema() != null) {
				sname.append(tableId.getSchema().getName());
				sname.append(".");
			}
		}

		sname.append(tableId.getTableName());
		return sname.toString();
	}

	public static String getQualifiedName(Catalog currentCatalog, Schema currentSchema, ColumnMetaData cmd) {
		StringBuffer sbuff = new StringBuffer();
		sbuff.append(getQualifiedName(currentCatalog, currentSchema, cmd.getParentTableId()));
		if (sbuff.length() > 0) {
			sbuff.append(".");
			sbuff.append(cmd.getColumnName());
		}
		return sbuff.toString();
	}

	/**
	 * @return the string label for a given result set scroll type
	 */
	public static String getResultSetScrollTypeString(int rtype) {
		if (rtype == ResultSet.TYPE_FORWARD_ONLY)
			return "TYPE_FORWARD_ONLY";
		else if (rtype == ResultSet.TYPE_SCROLL_INSENSITIVE)
			return "TYPE_SCROLL_INSENSITIVE";
		else if (rtype == ResultSet.TYPE_SCROLL_SENSITIVE)
			return "TYPE_SCROLL_SENSITIVE";
		else
			return "";
	}

	/**
	 * Searches the available shemas in the connection and compares them to the
	 * given schema name (ignoring case). If the schema is found, it is
	 * returned. Otherwise, null is returned.
	 */
	public static Schema getSchema(TSConnection conn, Catalog catalog, String schemaName) {
		return conn.getSchema(catalog, schemaName);
	}

	/**
	 * @return true of the given column metadata type is a text type
	 */
	public static boolean isAlpha(ColumnMetaData cmd) {
		return isAlpha(cmd.getType());
	}

	/**
	 * Checks the jdbc data type to determine if it is an Alpha type
	 * 
	 * @param dataType
	 *            the jdbc type
	 * @return true of the given column metadata type is a text type
	 */
	public static boolean isAlpha(int dataType) {
		return (dataType == java.sql.Types.LONGVARCHAR || dataType == java.sql.Types.VARCHAR || dataType == java.sql.Types.CHAR);
	}

	/**
	 * @return true if the given data type is a binary object of some sort
	 */
	public static boolean isBinary(int dataType) {
		boolean bresult = false;
		switch (dataType) {
		case java.sql.Types.BINARY:
		case java.sql.Types.BLOB:
		case java.sql.Types.CLOB:
		case java.sql.Types.JAVA_OBJECT:
		case java.sql.Types.LONGVARBINARY:
		case java.sql.Types.VARBINARY:
			bresult = true;
			break;
		}
		return bresult;
	}

	/**
	 * @return true if the given data type is a binary object of some sort
	 */
	public static boolean isBinary(ColumnMetaData cmd) {
		return DbUtils.isBinary(cmd.getType());
	}

	public static boolean isDateTime(int dataType) {
		return (dataType == java.sql.Types.TIME || dataType == java.sql.Types.DATE || dataType == java.sql.Types.TIMESTAMP);
	}

	/**
	 * @return true if the given field meta data object is a numeric type and if
	 *         it has no decimal digits
	 * @param the
	 *            field meta data object to check
	 */
	public static boolean isIntegral(int dataType) {
		boolean bresult = false;
		switch (dataType) {
		case java.sql.Types.BIGINT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.TINYINT:
			// if ( cmd.getPrecision() == 0 )
			bresult = true;
			break;
		}
		return bresult;
	}

	/**
	 * @return true if the given field meta data object is a numeric type and if
	 *         it has no decimal digits
	 * @param the
	 *            field meta data object to check
	 */
	public static boolean isIntegral(ColumnMetaData cmd) {
		return DbUtils.isIntegral(cmd.getType());
	}

	/**
	 * @return true if the given type is a recognized JDBC type
	 */
	public static boolean isJDBCType(int dataType) {
		return m_typeicons.containsKey(dataType);
	}

	/**
	 * @return true if the given data type is a float, real, or double
	 */
	public static boolean isReal(int dataType) {
		boolean bresult = false;
		switch (dataType) {
		case java.sql.Types.REAL:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:
			bresult = true;
			break;
		}
		return bresult;
	}

	/**
	 * @return true if the given field meta data object can contain decimal
	 *         digits
	 * @param the
	 *            field meta data object to check
	 */
	public static boolean hasPrecision(ColumnMetaData cmd) {
		boolean bresult = false;
		switch (cmd.getType()) {
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
			bresult = true;
			break;
		}
		return bresult;
	}

	/**
	 * @return true if the given data type generally allows a precision (i.e. a
	 *         size)
	 */
	public static boolean isPrecisionAllowed(int dataType) {
		boolean bresult = false;
		switch (dataType) {
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.CHAR:
			bresult = true;
			break;
		}
		return bresult;
	}

	/**
	 * Runs a SQL command against the given connection. This method assumes the
	 * command will return a single row, and it returns the value of the first
	 * column in that row
	 * 
	 * @return the value of the first column in the first row of a query. Null
	 *         is returned if no results are found.
	 */
	public static Object runSingleResultQuery(Connection conn, String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			TSUtils.printDebugMessage(sql);
			ResultSet rset = stmt.executeQuery(sql);
			rset.next();
			return rset.getObject(1);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Converts a string value to a SQL value. Basically, this method converts
	 * any quote characters to double quote characters and also quotes the
	 * entire string. For example: value = bill's result = 'bill''s'
	 * 
	 * @param value
	 *            the string value to convert to SQL
	 * @return the resulting SQL representation
	 */
	public static String toSQL(String value, char delimChar) {
		if (value == null)
			return "";

		StringBuffer buff = new StringBuffer();
		buff.append(delimChar);
		for (int index = 0; index < value.length(); index++) {
			char c = value.charAt(index);
			if (c == delimChar) {
				buff.append(delimChar);
				buff.append(delimChar);
			} else
				buff.append(c);
		}
		buff.append(delimChar);
		return buff.toString();
	}

	/*
	 * public static String toString( ColumnId cid, Object objectValue ) { if (
	 * objectValue != null ) { return objectValue.toString(); } else return "";
	 * }
	 */

	static class JDBCTypeInfo {
		private String m_typename;
		private String m_iconname;
		private ImageIcon m_icon;

		JDBCTypeInfo(String jdbcName, String iconName) {
			m_typename = jdbcName;
			m_iconname = iconName;
			m_icon = TSGuiToolbox.loadImage(iconName);
		}

		public ImageIcon getIcon() {
			return m_icon;
		}

		public String getIconName() {
			return m_iconname;
		}

		public String getJDBCTypeName() {
			return m_typename;
		}
	}

}
