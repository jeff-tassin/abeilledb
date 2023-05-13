package com.jeta.abeille.database.model;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.Collection;

/**
 * This is the interface definition for the database system. Each database
 * vendor will have an implementation of this interface
 * 
 * @author Jeff Tassin
 */
public interface TSDatabase {
	public static final String COMPONENT_ID = "database.TSDatabase";

	/** some common database properties */
	public static final String RESULT_SET_TYPE = "result.set.type";
	public static final String RESULT_SET_CONCURRENCY = "result.set.concurrency";
	public static final String QUERY_TABLE_COUNTS = "query.table.counts";

	/**
	 * Converts the case for the given object name to the case supported by the
	 * database. This is needed because some databases require exact case for
	 * table names (HSQLDB). So, if the user types in a table name, but the case
	 * does not match, we still want to be able to get the table from the db.
	 * This call converts to the case needed.
	 */
	public String convertCase(String objName);

	/**
	 * Creates a default data type. This is used when the user creates a column
	 * with a type that is not registered. So, we simply create a new type with
	 * the most liberal settings.
	 */
	public DataTypeInfo createDefaultDataType(String typeName);

	/**
	 * @return the current schema for the currrent user
	 */
	public Schema getCurrentSchema() throws SQLException;

	/**
	 * @return the data type info object for the given type name
	 */
	public DataTypeInfo getDataTypeInfo(String typename);

	/**
	 * @return a collection of Schema objects for the current user
	 */
	public Collection getSchemas() throws SQLException;

	/**
	 * @return a collection of data types (DataTypeInfo objects) that this
	 *         database supports
	 */
	public Collection getSupportedTypes();

	/**
	 * @return the major/minor/sub version for this database
	 */
	public DbVersion getVersion();

	/**
	 * @return true if table names are case-sensitive for this database.
	 */
	public boolean isCaseSensitive();

	/**
	 * @return true if this database requires a rollback after an exception
	 *         occurs
	 */
	public boolean rollbackOnException();
	

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn);

	/**
	 * Sets the current schema for the currrent user
	 */
	public void setCurrentSchema(Schema schema) throws SQLException;

	/**
	 * @return true if the given database supports more than one catalog opened
	 *         for a connection as well as the ability to query between catalogs
	 *         for this connection. PostgreSQL <= 7.3 does not support this by
	 *         MySQL does.
	 */
	public boolean supportsCatalogs() throws SQLException;

	/**
	 * @return true if this database supports schemas
	 */
	public boolean supportsSchemas() throws SQLException;

	/**
	 * @return true if this database supports transactions
	 */
	public boolean supportsTransactions();

	/**
	 * @return true if this database supports canceling a SQL statement
	 */
	public boolean supportsCancelStatement();

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
	public boolean supportsFeature(String featuresName);
	
	
	public String getFullyQualifiedName( DbObjectId objId );

}
