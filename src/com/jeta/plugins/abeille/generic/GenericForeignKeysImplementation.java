package com.jeta.plugins.abeille.generic;

import java.sql.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.ForeignKeyConstraints;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSForeignKeys;

import com.jeta.abeille.gui.modeler.ForeignKeyView;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.plugins.abeille.standard.AbstractForeignKeysImplementation;

/**
 * This is the implementation for foreign key retrieval for Postgres.
 * 
 * @author Jeff Tassin
 */
public class GenericForeignKeysImplementation extends AbstractForeignKeysImplementation {

	public GenericForeignKeysImplementation() {

	}

	public GenericForeignKeysImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * @param tableId
	 *            the id of the table whose foreign keys we wish to retrieve
	 * @return a collection of DbForeignKey objects for the given table
	 */
	public Collection getForeignKeys(TableId tableid) throws SQLException {
		Calendar c = Calendar.getInstance();
		long mstart = c.getTimeInMillis();

		DbModel model = getConnection().getModel(tableid.getCatalog());
		HashMap fkeys = new HashMap();

		DatabaseMetaData metadata = getConnection().getMetaDataConnection().getMetaData();
		ResultSet rs = null;
		try {

			Schema schema = tableid.getSchema();
			Schema refschema = schema;
			/*
			 * if ( schema == Schema.VIRTUAL_SCHEMA ) rs =
			 * metadata.getImportedKeys( "", "", tableid.getTableName() ); else
			 * rs = metadata.getImportedKeys( "", schema.getName(),
			 * tableid.getTableName() );
			 */

			rs = metadata.getImportedKeys(tableid.getCatalog().getMetaDataSearchParam(), tableid.getSchema()
					.getMetaDataSearchParam(), tableid.getTableName());

			c = Calendar.getInstance();
			float diff = (float) (c.getTimeInMillis() - mstart);
			// System.out.println(
			// "DefaultForeignKeysImplementation  getForeignKeys." + tableid +
			// "   loaded in " + diff/1000.0 + " seconds " );

			TableMetaData localtmd = getConnection().getTable(tableid);
			while (rs != null && rs.next()) {
				// ref table
				String ref_tablename = rs.getString("PKTABLE_NAME");
				String ref_colname = rs.getString("PKCOLUMN_NAME");

				if (getConnection().supportsSchemas()) {
					try {
						String refschemaname = rs.getString("PKTABLE_SCHEM");
						refschema = getConnection().getSchema(tableid.getCatalog(), refschemaname);
						if (refschema == null)
							refschema = schema;
					} catch (Exception e) {
						TSUtils.printException(e);
					}
				}

				// local table
				String local_colname = rs.getString("FKCOLUMN_NAME");
				String local_tablename = rs.getString("FKTABLE_NAME");
				String local_keyname = rs.getString("FK_NAME");

				short update_rule = rs.getShort("UPDATE_RULE");
				short delete_rule = rs.getShort("DELETE_RULE");
				short defer = rs.getShort("DEFERRABILITY");

				assert (local_keyname != null);
				assert (local_keyname.length() > 0);

				DbForeignKey fkey = (DbForeignKey) fkeys.get(local_keyname);
				if (fkey == null) {
					fkey = new DbForeignKey();
					fkeys.put(local_keyname, fkey);
					fkey.setKeyName(local_keyname);
					fkey.setLocalTableId(new TableId(tableid.getCatalog(), schema, local_tablename));

					TableId refid = new TableId(tableid.getCatalog(), refschema, ref_tablename);
					fkey.setReferenceTableId(refid);

					TableMetaData reftmd = model.getTable(refid);
					if (reftmd != null) {
						DbKey pk = reftmd.getPrimaryKey();
						assert (pk != null);
						if (pk != null) {
							fkey.setReferenceKeyName(pk.getKeyName());
						}
					}

					ForeignKeyConstraints fc = new ForeignKeyConstraints();
					fc.setDeleteAction(delete_rule);
					fc.setUpdateAction(update_rule);
					if (defer == DatabaseMetaData.importedKeyNotDeferrable) {
						fc.setDeferrable(false);
					} else if (defer == DatabaseMetaData.importedKeyInitiallyImmediate) {
						fc.setDeferrable(true);
						fc.setInitiallyDeferred(false);
					} else if (defer == DatabaseMetaData.importedKeyInitiallyDeferred) {
						fc.setDeferrable(true);
						fc.setInitiallyDeferred(true);
					}

					fkey.setConstraints(fc);
				}
				fkey.assignForeignKeyColumn(local_colname, ref_colname);
			}
		} finally {
			if (rs != null)
				rs.close();
		}

		return fkeys.values();
	}

	/**
	 * This method returns an object for a given supported feature relating to
	 * foreign keys. For example, databases support different foreign key
	 * constraints for ON_UPDATE and ON_DELETE. So, the client application can
	 * query this service to see what features are supported and update the GUI
	 * accordingly IMPORTANT: By convention, you should begin any feature names
	 * with the string: checked.feature. FEATURE_FOREIGN_KEY_ON_UPDATE =
	 * "checked.feature.foreign.key.on.update"; FEATURE_FOREIGN_KEY_ON_DELETE =
	 * "checked.feature.foreign.key.on.delete"; etc.
	 * 
	 * @return an object that is specific for the requested feature. Some
	 *         features require a collection, and others might require a
	 *         Boolean. It depends.
	 */
	public Object getSupportedFeature(String featuresName) {
		return getSupportedFeature(featuresName, null);
	}

	/**
	 * Convenience method that allows user to send a default value if the
	 * feature is not supported.
	 * 
	 * @return an object that is specific for the requested feature. Some
	 *         features require a collection, and others might require a
	 *         Boolean. It depends.
	 */
	public Object getSupportedFeature(String featureName, Object defaultValue) {
		assert (featureName != null);
		assert (featureName.indexOf("checked.") == 0);
		TSConnection tsconn = getConnection();
		Database db = tsconn.getDatabase();
		if (ForeignKeyView.FEATURE_FOREIGN_KEY_ON_UPDATE.equals(featureName)) {
			if (db.equals(Database.ORACLE) || db.equals(Database.HSQLDB)) {
				return null;
			} else if (db.equals(Database.DB2)) {
				Object[] actions = { ForeignKeyView.NO_ACTION, ForeignKeyView.RESTRICT };
				return actions;
			} else if (db.equals(Database.POINTBASE)) {
				Object[] actions = { ForeignKeyView.NO_ACTION, ForeignKeyView.RESTRICT, ForeignKeyView.CASCADE,
						ForeignKeyView.SET_DEFAULT, ForeignKeyView.SET_NULL };
				return actions;
			} else {
				return super.getSupportedFeature(featureName, defaultValue);
			}
		} else if (ForeignKeyView.FEATURE_FOREIGN_KEY_ON_DELETE.equals(featureName)) {
			if (db.equals(Database.ORACLE)) {
				/** oracle only supports ON DELETE - cascade, set null */
				Object[] actions = { ForeignKeyView.CASCADE, ForeignKeyView.SET_NULL };
				return actions;
			} else if (db.equals(Database.HSQLDB)) {
				Object[] actions = { ForeignKeyView.CASCADE };
				return actions;
			} else if (db.equals(Database.DB2)) {
				Object[] actions = { ForeignKeyView.NO_ACTION, ForeignKeyView.RESTRICT, ForeignKeyView.CASCADE,
						ForeignKeyView.SET_NULL };
				return actions;
			} else if (db.equals(Database.POINTBASE)) {
				Object[] actions = { ForeignKeyView.NO_ACTION, ForeignKeyView.RESTRICT, ForeignKeyView.CASCADE,
						ForeignKeyView.SET_DEFAULT, ForeignKeyView.SET_NULL };
				return actions;
			} else {
				return super.getSupportedFeature(featureName, defaultValue);
			}
		} else if (ForeignKeyView.FEATURE_FOREIGN_KEY_DEFERRABLE.equals(featureName)) {
			if (db.equals(Database.HSQLDB) || db.equals(Database.DB2) || db.equals(Database.POINTBASE)) {
				return Boolean.FALSE;
			} else {
				return super.getSupportedFeature(featureName, defaultValue);
			}
		} else {
			return super.getSupportedFeature(featureName, defaultValue);
		}
	}

}
