package com.jeta.abeille.database.model;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Collection;

/**
 * This is the interface definition for some table operations in the database
 * system. Each database vendor will have an implementation of this interface
 * 
 * @author Jeff Tassin
 */
public interface TSTable {
	public static final String COMPONENT_ID = "database.TSTable";

	/**
	 * Creates the given column in the given table
	 */
	public void createColumn(TableId tableId, ColumnMetaData newColumn) throws SQLException;

	/**
	 * @return the SQL command to create the table represented by the table
	 *         metadata object. This is database dependent
	 */
	public String createTableSQL(TableMetaData tmd) throws SQLException;

	/**
	 * Drops the given column from the given table
	 */
	public void dropColumn(TableId tableId, ColumnMetaData dropColumn, boolean cascade) throws SQLException;

	/**
	 * Drops the primary key for the given table
	 */
	public void dropPrimaryKey(TableId tableid, boolean cascade) throws SQLException;

	/**
	 * Loads columns information such as constraints and default values.
	 */
	public void loadColumnsEx(TableMetaData tmd) throws SQLException;

	/**
	 * Find the foreign keys in other tables that reference the primary key in
	 * this table.
	 */
	public Collection loadExportedKeys(TableMetaData tmd) throws SQLException;

	/**
	 * Modifies the given column to take on the attributes of the new column for
	 * the given table.
	 */
	public void modifyColumn(TableId tableId, ColumnMetaData newColumn, ColumnMetaData oldColumn) throws SQLException;

	/**
	 * Modifies the given primary key.
	 */
	public void modifyPrimaryKey(TableId tableid, DbKey newpk, DbKey oldpk) throws SQLException;

	/**
	 * Renames a table to a new table name. This is database dependent
	 */
	public void renameTable(TableId newName, TableId oldName) throws SQLException;

	/**
	 * Sets the connection which this interface will operate on
	 * 
	 * @param conn
	 *            the connection to set
	 */
	public void setConnection(TSConnection conn);

}
