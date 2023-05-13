package com.jeta.abeille.gui.model;

import java.awt.datatransfer.DataFlavor;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

/**
 * Database object transfer utilties
 * 
 * @author Jeff Tassin
 */
public class DbObjectTransfer {

	/**
	 * Adds a prototype table to the multi transferable. This also adds the
	 * string name
	 */
	public static void addPrototype(MultiTransferable mt, TSConnection tsconn, TableMetaData tmd) {
		assert (tmd != null);
		assert (tsconn != null);
		mt.addData(DbObjectFlavor.TABLE_PROTOTYPE_REFERENCE, new TableMetaDataReference(tsconn, tmd));
		TableId tableid = tmd.getTableId();
		if (tableid != null) {
			mt.addData(DataFlavor.stringFlavor, tableid.getFullyQualifiedName());
		}
	}

	/**
	 * Adds a standard dabase object to the transfer
	 */
	public static void addDatabaseObject(MultiTransferable mt, DatabaseObject dbobj) {
		mt.addData(DbObjectFlavor.getFlavor(dbobj.getObjectId().getObjectType()), dbobj);
		mt.addData(DataFlavor.stringFlavor, dbobj.getObjectId().getObjectName());
	}

	/**
	 * Adds a table widget to the multi transferable. This also adds the table
	 * metadata as well as a String (for table name)
	 */
	public static void addTransferable(MultiTransferable mt, TableWidget tw) {
		mt.addData(DbObjectFlavor.TABLE_WIDGET_REFERENCE, new TableWidgetReference(tw.getConnection(), tw));
		addTransferable(mt, tw.getConnection(), tw.getTableMetaData());
	}

	/**
	 * Adds a table widget to the multi transferable. This also adds the table
	 * metadata as well as a String (for table name)
	 */
	public static void addTransferable(MultiTransferable mt, Link link) {
		mt.addData(DbObjectFlavor.LINK, link);
	}

	/**
	 * Adds a column metadata object to the multi transferable. This also adds
	 * the column metadata metadata as well as a String (for table name)
	 */
	public static void addTransferable(MultiTransferable mt, ColumnMetaData cmd) {
		mt.addData(DbObjectFlavor.COLUMN_METADATA, cmd);
		mt.addData(DataFlavor.stringFlavor, cmd.getColumnName());
	}

	/**
	 * Adds a column metadata object multi transferable. This also adds the
	 * table metadata as well as a String (for table name).
	 * 
	 * @param qualified
	 *            if true, the table name is prepended to the column name for
	 *            the string value
	 */
	public static void addTransferable(MultiTransferable mt, ColumnMetaData cmd, boolean qualified) {
		mt.addData(DbObjectFlavor.COLUMN_METADATA, cmd);
		if (qualified) {
			mt.addData(DataFlavor.stringFlavor, cmd.getTableQualifiedName());
		} else {
			mt.addData(DataFlavor.stringFlavor, cmd.getColumnName());
		}
	}

	/**
	 * Adds a table metadata to the multi transferable. This also adds the
	 * tableid.
	 */
	public static void addTransferable(MultiTransferable mt, TSConnection tsconn, TableMetaData tmd) {
		assert (tsconn != null);
		mt.addData(DbObjectFlavor.TABLE_METADATA_REFERENCE, new TableMetaDataReference(tsconn, tmd));
		addTransferable(mt, tsconn, tmd.getTableId());
	}

	/**
	 * Adds a table id to the multi transferable. This also adds a String (for
	 * table name)
	 */
	public static void addTransferable(MultiTransferable mt, TSConnection tsconn, TableId tableid) {
		assert (tsconn != null);

		addTransferable(mt, new TableReference(tsconn, tableid));
	}

	/**
	 * Adds a table id to the multi transferable. This also adds a String (for
	 * table name)
	 */
	public static void addTransferable(MultiTransferable mt, TableReference tableref) {
		mt.addData(DbObjectFlavor.TABLE_REFERENCE, tableref);
		mt.addData(DataFlavor.stringFlavor, tableref.getFullyQualifiedName());
	}

	/**
	 * Adds a schema to the multi transferable. This also adds a String (for
	 * schema name)
	 */
	public static void addTransferable(MultiTransferable mt, Schema schema) {
		mt.addData(DbObjectFlavor.SCHEMA, schema);
		mt.addData(DataFlavor.stringFlavor, schema.getName());
	}

	/**
	 * Adds the path to the nodes that are being dragged in a given object tree
	 */
	public static void addTransferable(MultiTransferable mt, TreePath path) {
		mt.addData(DbObjectFlavor.TREE_PATH, path);
	}

}
