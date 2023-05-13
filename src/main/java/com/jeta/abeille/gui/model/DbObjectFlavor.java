package com.jeta.abeille.gui.model;

import java.awt.datatransfer.DataFlavor;

import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.DbObjectType;

public class DbObjectFlavor {

	/**
	 * @return the object flavor associated with a given database object type
	 */
	public static DataFlavor getFlavor(DbObjectType objtype) {
		if (objtype == DbObjectType.SCHEMA)
			return DbObjectFlavor.SCHEMA;
		else if (objtype == DbObjectType.SEQUENCE)
			return DbObjectFlavor.SEQUENCE;
		else if (objtype == DbObjectType.FUNCTION)
			return DbObjectFlavor.PROCEDURE;
		else {
			System.out.println("Invalid object type: " + objtype);
			assert (false);
			return null;
		}
	}

	/**
	 * this is used to indentify a hetergeneous set of database objects that are
	 * being transfered
	 */
	/**
	 * transfer uid - this is used when moving tree nodes within the same tree -
	 * a very special case
	 */
	public static final DataFlavor TRANSFER_UID = new DataFlavor(com.jeta.abeille.gui.model.ObjectTree.class,
			"jeta.abeille.gui.model.transferuid");

	/** dragging table(s) */
	public static final DataFlavor TABLE_METADATA_REFERENCE = new DataFlavor(
			com.jeta.abeille.database.model.TableMetaData.class, "table.metadata");

	/**
	 * The user is dragging/copying the tableid of a table prototype
	 */
	public static final DataFlavor TABLE_PROTOTYPE_REFERENCE = new DataFlavor(
			com.jeta.abeille.gui.model.PrototypeId.class, "table.prototype");

	/** dragging table(s) */
	public static final DataFlavor TABLE_REFERENCE = new DataFlavor(TableReference.class, "table.reference");

	/**
	 * dragging tablewidget(s) - this includes widget size and bounds. Note that
	 * table widgets cannot be dragged using DnD. The ModelCanvas class handles
	 * dragging. However, they can be copied to the clipboard
	 */
	public static final DataFlavor TABLE_WIDGET_REFERENCE = new DataFlavor(
			com.jeta.abeille.gui.model.TableWidget.class, "table.widget");

	/** this is only used for drag and drop within the object tree */
	public static final DataFlavor TREE_PATH = new DataFlavor(ObjectTreePath.class, "object.tree.path");

	/** dragging column(s) */
	public static final DataFlavor COLUMN_METADATA = new DataFlavor(
			com.jeta.abeille.database.model.ColumnMetaData.class, "column");

	/** dragging a schema */
	public static final DataFlavor SCHEMA = new DataFlavor(com.jeta.abeille.database.model.Schema.class, "schema");

	/** dragging a tree folder */
	public static final DataFlavor TREE_FOLDER = new DataFlavor(TreeFolder.class, "tree.folder");

	/** dragging a link */
	public static final DataFlavor LINK = new DataFlavor(com.jeta.abeille.database.model.Link.class, "link");

	/** dragging a sequence */
	public static final DataFlavor SEQUENCE = new DataFlavor(com.jeta.abeille.database.sequences.Sequence.class,
			"sequence");

	/** dragging a procedure */
	public static final DataFlavor PROCEDURE = new DataFlavor(
			com.jeta.abeille.database.procedures.StoredProcedure.class, "procedure");

	public static final DataFlavor MULTI_OBJECT = new DataFlavor(MultiTransferable.class, "multitransferable");

	// public static final DataFlavor UNHAMULTI_OBJECT = new DataFlavor(
	// MultiTransferable.class, "multitransferable" );

}
