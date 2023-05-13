package com.jeta.plugins.abeille.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import com.jeta.foundation.i18n.I18N;

import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.ForeignKeyConstraints;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSTable;
import com.jeta.abeille.database.model.TSForeignKeys;

import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.standard.AbstractForeignKeysImplementation;
import com.jeta.plugins.abeille.standard.DefaultTableImplementation;
import com.jeta.plugins.abeille.generic.GenericForeignKeysImplementation;


/**
 * This is the implementation for foreign key retrieval for Postgres.
 * 
 * @author Jeff Tassin
 */
public class ForeignKeysImplementation extends GenericForeignKeysImplementation {

	/** the cache of TableIds (keys) to Oids (Long values ) */
	private HashMap m_tableoids = null;

	/**
	 * a map of Long oids (keys) to a linkedlist (values) of DbForeignKeys for a
	 * given table
	 */
	private HashMap m_keycache = null;

	private Boolean m_supportsSchemas;

	/**
	 * Ctor
	 */
	public ForeignKeysImplementation() {

	}

	/**
	 * ctor
	 */
	public ForeignKeysImplementation(TSConnection conn) {
		super(conn);
	}

	/**
	 * Builds a foreign key based on the SQL query result to the Postgres system
	 * catalogs. This method assumes the result set is from the following SQL:
	 * select tgargs, tgrelid, tgfoid, tgconstrrelid, tgname, tgconstrname from
	 * pg_trigger where tgname like 'RI%' and tgtype = 21 and tgrelid = oid (the
	 * last constraint, tgrelid = oid, is optional here )
	 * 
	 * @return the newly constructed foreign key based on the information found
	 *         in the current result set instance. If the current row in the
	 *         result set does not form a valid foreign key, then null is
	 *         returned.
	 */
	private DbForeignKey buildForeignKey(ResultSet rset, TableId tableId) throws SQLException {
		DbModel model = getConnection().getModel(tableId.getCatalog());
		String args = rset.getString("tgargs");
		long refoid = rset.getLong("tgconstrrelid"); // this is the oid of the
														// table this foreign
														// key depends on
		String fkname = rset.getString("tgconstrname");
		long oid = rset.getLong("tgrelid");

		if (isForeignKeyTrigger(oid, refoid, args)) {
			Collection localcolumns = getLocalColumns(args);

			TableId refid = getTableId(refoid);
			if (refid != null && localcolumns.size() > 0) {
				// we have a foreign key
				// bingo
				Iterator iter = localcolumns.iterator();

				iter.next(); // first item should be <unnamed>

				String arg_localtable = (String) iter.next(); // next is local
																// table name:
																// twofkexample
				String arg_reftable = (String) iter.next(); // next is reference
															// table name:
															// twopkexample

				// if ( I18N.equals( localtable, arg_localtable ) &&
				// I18N.equals( reftable, arg_reftable ) )
				if (I18N.equals(refid.getTableName(), arg_reftable)) {
					DbForeignKey fkey = new DbForeignKey();
					fkey.setKeyName(fkname);
					fkey.setLocalTableId(new TableId(getConnection().getDefaultCatalog(), tableId.getSchema(),
							arg_localtable));
					fkey.setReferenceTableId(refid);

					TableMetaData reftmd = model.getTable(refid);

					if (TSUtils.isDebug()) {
						assert (reftmd != null);
						assert (reftmd.getPrimaryKey() != null);
					}

					fkey.setReferenceKeyName(reftmd.getPrimaryKey().getKeyName());

					iter.next(); // next is UNSPECIFIED
					// then we get the columns that define foreign key
					while (iter.hasNext()) {
						String fkcol = (String) iter.next(); // local col1:
																// entryid_fk
						String pkcol = (String) iter.next(); // ref col1:
																// entry_id
						fkey.assignForeignKeyColumn(fkcol, pkcol);
					}

					return fkey;
				} else {
					System.out
							.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>***ERROR  Postgres foreign key tables are not consistent***");
				}
			}
		}

		return null;
	}

	/**
	 * Creates a new foreign key in the database
	 */
	public void createForeignKey(DbForeignKey newkey) throws SQLException {
		DefaultTableImplementation impl = (DefaultTableImplementation) getConnection().getImplementation(
				TSTable.COMPONENT_ID);
		String sql = impl.getCreateForeignKeySQL(newkey);
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Create Foreign Key"), sql);
	}

	/**
	 * Drops a foreign key in the database
	 */
	public void dropForeignKey(DbForeignKey key, boolean cascade) throws SQLException {
		String sql = getDropForeignKeySQL(key, cascade);
		if (supportsSchemas()) {
			SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Drop Foreign Key"), sql);
		} else {
			String msg = I18N.format("operation_not_supported_in_postgres_version_1", sql);
			throw new SQLException(msg);
		}
	}

	/**
	 * Load the constraint objects for each foreign key
	 * 
	 * @param oid
	 *            the oid of the table the foreign keys are defined in
	 * @param tableId
	 *            the id of the same table as the oid table
	 * @param keys
	 *            a collection of DbForeignKey objects for that table
	 */
	private void getConstraints(long oid, TableId tableId, Collection keys) throws SQLException {
		TSConnection tsconn = getConnection();
		if (tsconn.supportsSchemas()) {
			StringBuffer sql = new StringBuffer();
			sql.append("select * from pg_catalog.pg_constraint where contype = 'f' and conrelid = ");
			sql.append(oid);

			Connection conn = null;
			Statement stmt = null;
			try {
				conn = tsconn.getMetaDataConnection();
				stmt = conn.createStatement();

				ResultSet rset = stmt.executeQuery(sql.toString());
				while (rset.next()) {
					String fkname = rset.getString("conname");

					// now get the foreign key from the set of foreign keys
					// passed to this method
					Iterator iter = keys.iterator();
					while (iter.hasNext()) {
						DbForeignKey fkey = (DbForeignKey) iter.next();
						if (fkname.equals(fkey.getName())) {
							ForeignKeyConstraints fc = new ForeignKeyConstraints();

							boolean deferrable = rset.getBoolean("condeferrable");
							boolean initially = rset.getBoolean("condeferred");
							String updatestr = rset.getString("confupdtype");

							char updatea = 'a';
							if (updatestr.length() > 0)
								updatea = updatestr.charAt(0);

							if (updatea == 'r' || updatea == 'a') {
								fc.setUpdateAction(ForeignKeyConstraints.NO_ACTION);
							} else if (updatea == 'c') {
								fc.setUpdateAction(ForeignKeyConstraints.CASCADE);
							} else if (updatea == 'n') {
								fc.setUpdateAction(ForeignKeyConstraints.SET_NULL);
							} else if (updatea == 'd') {
								fc.setUpdateAction(ForeignKeyConstraints.SET_DEFAULT);
							}

							String deletestr = rset.getString("confdeltype");
							char deletea = 'a';
							if (deletestr.length() > 0)
								deletea = deletestr.charAt(0);

							if (deletea == 'r' || deletea == 'a') {
								fc.setDeleteAction(ForeignKeyConstraints.NO_ACTION);
							} else if (deletea == 'c') {
								fc.setDeleteAction(ForeignKeyConstraints.CASCADE);
							} else if (deletea == 'n') {
								fc.setDeleteAction(ForeignKeyConstraints.SET_NULL);
							} else if (deletea == 'd') {
								fc.setDeleteAction(ForeignKeyConstraints.SET_DEFAULT);
							}

							fc.setDeferrable(deferrable);
							fc.setInitiallyDeferred(initially);
							fkey.setConstraints(fc);
							break;
						}
					}
				}
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (Exception e) {
					TSUtils.printException(e);
				}

				tsconn.release(conn);
			}

		}
	}

	/**
	 * @return the SQL used to drop the given foreign key
	 */
	String getDropForeignKeySQL(DbForeignKey key, boolean cascade) {
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(key.getLocalTableId().getFullyQualifiedName());
		sql.append(" DROP CONSTRAINT \"");
		sql.append(key.getName());
		sql.append("\"");
		if (cascade)
			sql.append(" CASCADE");

		sql.append(';');

		return sql.toString();
	}

	/**
	 * @return a collection of DbForeignKey objects for the given table
	 */
	/*
	public synchronized Collection getForeignKeys(TableId tableId) throws SQLException {
		Collection results = getKeys(getOid(tableId), tableId);
		if (results == null)
			results = new LinkedList();
		return results;
	}
	*/


	/**
	 * @param oid
	 *            the oid of the table whose foreign keys we wish to retreive
	 * @param tableId
	 *            the id of the table (same table whose oid is specified)
	 * @return all foreign keys for a given table.
	 */
	private Collection getKeys(long oid, TableId tableId) throws SQLException {
		Statement stmt = null;
		try {
			stmt = getConnection().getMetaDataConnection().createStatement();

			LinkedList results = new LinkedList();
			StringBuffer sql = new StringBuffer();
			// //////////////////////////////////////////////////////////////////////
			// @todo this probably won't work in the general case, may need to
			// refactor
			if (supportsSchemas()) {
				sql.append("select * from pg_catalog.pg_trigger where tgname like 'RI%' and tgtype = 21 and tgrelid = ");
			} else {
				sql.append("select * from pg_trigger where tgname like 'RI%' and tgtype = 21 and tgrelid = ");
			}
			sql.append(oid);

			ResultSet rset = stmt.executeQuery(sql.toString());
			if (rset == null || rset.isAfterLast()) // no foreign keys for this
													// table
			{
				return results;
			}

			while (rset.next()) {
				DbForeignKey fkey = buildForeignKey(rset, tableId);
				if (fkey != null)
					results.add(fkey);
			}

			// load the constraints for each key
			getConstraints(oid, tableId, results);

			return results;
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}

	/**
	 * Parses the tgargs column from the pg_trigger table to get the local
	 * columns assigned to a foreign key.
	 * 
	 * @param tgargs
	 *            the tgargs column to parse. This is a bytea type in postgres,
	 *            so we need to tokenize on \000. However, we probably need to
	 *            change to support postgres type decoding
	 * @return a collection of String objects identifing the local columns that
	 *         make up the foreign key
	 */
	private Collection getLocalColumns(String tgargs) {
		// <unnamed>
		// local table name: twofkexample
		// reference table name: twopkexample
		// UNSPECIFIED
		// local col1: entryid_fk
		// ref col1: entry_id
		// local col2: entrytag_fk
		// ref col2: entry_tag
		// ...
		// local colN:
		// ref colN

		// @todo this is probably not the correct way to handle this
		LinkedList results = new LinkedList();
		StringTokenizer tz = new StringTokenizer(tgargs, "\\000");
		while (tz.hasMoreTokens()) {
			results.add(tz.nextToken());
		}
		return results;
	}

	/**
	 * @return the oid for the given table
	 */
	public long getOid(TableId tableId) throws SQLException {
		long oid = 0;
		String localtable = tableId.getTableName();
		DbModel model = getConnection().getModel(tableId.getCatalog());
		Collection results = null;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = getConnection().getMetaDataConnection().createStatement();
			StringBuffer sql = new StringBuffer();
			if (supportsSchemas()) {
				sql.append("select oid, relname from pg_catalog.pg_class where pg_catalog.pg_namespace.oid = pg_catalog.pg_class.relnamespace and ");
				sql.append("pg_catalog.pg_namespace.nspname = '");
				sql.append(tableId.getSchema().getName());
				sql.append("' and ");
			} else {
				sql.append("select oid, relname from pg_class where ");
			}
			sql.append("relname = '");
			sql.append(tableId.getTableName());
			sql.append("' and ");
			sql.append("relkind = 'r'");

			rset = stmt.executeQuery(sql.toString());
			if (rset.next()) {
				oid = rset.getLong("oid");
			}
			rset.close();
		} finally {
			if (stmt != null)
				stmt.close();
		}
		return oid;
	}

	/**
	 * Gets the name of a table from the pg_class system catalog given an oid
	 * 
	 * @param oid
	 *            the oid of the table to get
	 * @param stmt
	 *            the statement to use to execute the query
	 * @return the name of the table. If no table is found, null is returned
	 */
	private TableId getTableId(long oid) throws SQLException {
		// get referring table
		Statement stmt = getConnection().createStatement();
		ResultSet rset = null;
		try {
			StringBuffer sql = new StringBuffer();
			if (supportsSchemas()) {
				sql.append("select relname, nspname from pg_catalog.pg_class, pg_catalog.pg_namespace where pg_catalog.pg_namespace.oid = pg_catalog.pg_class.relnamespace and pg_catalog.pg_class.oid = ");
			} else {
				sql.append("select relname from pg_class where oid = ");
			}
			sql.append(oid);
			rset = stmt.executeQuery(sql.toString());
			if (rset != null) {
				rset.next();
				if (supportsSchemas()) {
					return new TableId(getConnection().getDefaultCatalog(), new Schema(rset.getString("nspname")),
							rset.getString("relname"));
				} else {
					return new TableId(getConnection().getDefaultCatalog(), Schema.VIRTUAL_SCHEMA,
							rset.getString("relname"));
				}
			}
		} finally {
			if (stmt != null)
				stmt.close();
		}

		return null;
	}

	/**
	 * @return true if all key requests are forwarded to the cache rather than
	 *         the database
	 */
	private boolean isCached() {
		return (m_tableoids != null);
	}

	/**
	 * Determines whether a given trigger is a foreign key or not
	 * 
	 * @param tgrelid
	 *            the oid of the table that the trigger is attached to
	 * @param tgconstrrelid
	 *            the oid of the table that the trigger is related to
	 * @param args
	 *            the tgargs param for the tgrelid table
	 * @param stmt
	 *            the statement to use to create the query
	 */
	private boolean isForeignKeyTrigger(long tgrelid, long tgconstrrelid, String args) throws SQLException {

		Statement stmt = getConnection().createStatement();
		ResultSet rset = null;
		try {
			// here we swap tgrelid and tgconstrrelid because thats how we find
			// the corresponding triggers
			// on the rerference table
			StringBuffer sql = new StringBuffer();
			if (supportsSchemas()) {
				sql.append("select * from pg_catalog.pg_trigger where (tgtype = 9 or tgtype = 17) and tgconstrrelid = ");
			} else {
				sql.append("select * from pg_trigger where (tgtype = 9 or tgtype = 17) and tgconstrrelid = ");
			}
			sql.append(tgrelid);
			sql.append(" and tgrelid = ");
			sql.append(tgconstrrelid);

			rset = stmt.executeQuery(sql.toString());
			int count = 0;
			while (rset != null && rset.next()) {
				String relargs = rset.getString("tgargs");
				int tgtype = rset.getInt("tgtype");
				// //////////////////////////////////////////////////////////////////////
				// @todo this probably won't work in the general case, may need
				// to refactor
				if (args.equals(relargs))
					count++;
			}
			return (count == 2);
		} finally {
			if (stmt != null)
				stmt.close();
		}

	}

	/**
	 * Find the foreign keys in other tables that reference the primary key in
	 * this table.
	 * 
	 * @return a collection of TableIds that reference the given table
	 */
	public Collection loadExportedTables(TableMetaData tmd) throws SQLException {
		LinkedList results = new LinkedList();
		long oid = getOid(tmd.getTableId());
		if (oid > 0) {
			Statement stmt = null;
			ResultSet rset = null;
			Connection conn = getConnection().getMetaDataConnection();

			StringBuffer sql = new StringBuffer();
			if (supportsSchemas()) {
				sql.append("select conrelid from pg_catalog.pg_constraint where contype = 'f' and confrelid = ");
			} else {
				sql.append("select * from pg_trigger where tgname like 'RI%' and tgtype = 21 and tgconstrrelid = ");
			}
			sql.append(oid);

			try {
				stmt = conn.createStatement();
				rset = stmt.executeQuery(sql.toString());
				while (rset != null && rset.next()) {
					long pkoid = 0;
					if (supportsSchemas()) {
						pkoid = rset.getLong("conrelid");
					} else {
						pkoid = rset.getLong("tgrelid");
					}
					TableId tableid = getTableId(pkoid);
					if (tableid != null) {
						results.add(tableid);
					}
				}
			} finally {
				if (stmt != null)
					stmt.close();

				try {
					conn.commit();
				} catch (Exception e) {

				}
			}
		}
		return results;
	}

	/**
	 * Modifies an existing foreign key
	 */
	public void modifyForeignKey(DbForeignKey newkey, DbForeignKey oldKey) throws SQLException {
		String sql1 = getDropForeignKeySQL(oldKey, false);
		DefaultTableImplementation impl = (DefaultTableImplementation) getConnection().getImplementation(
				TSTable.COMPONENT_ID);
		String sql2 = impl.getCreateForeignKeySQL(newkey);

		if (supportsSchemas()) {
			SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Modify Foreign Key"), sql1, sql2);
		} else {
			String msg = I18N.format("operation_not_supported_in_postgres_version_2", sql1, sql2);
			throw new SQLException(msg);
		}

	}

	private boolean supportsSchemas() throws SQLException {
		if (m_supportsSchemas == null) {
			TSDatabase db = (TSDatabase) getConnection().getImplementation(TSDatabase.COMPONENT_ID);
			m_supportsSchemas = Boolean.valueOf(db.supportsSchemas());
		}
		return m_supportsSchemas.booleanValue();
	}
}
