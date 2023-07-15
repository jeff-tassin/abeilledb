package com.jeta.abeille.gui.model;

import com.jeta.abeille.database.model.*;
import com.jeta.foundation.utils.TSUtils;

import java.util.Arrays;

/**
 * Base TableTreeModel is the GUI model for the tree view of the database This
 * view shows table and fields in a hierarchial view.
 * 
 * @author Jeff Tassin
 */
public abstract class TableTreeBaseModel extends DbObjectClassModel {
	public static final String TABLE_TREE_BASE = "TableTreeBaseModel";

	/**
	 * ctor
	 */
	public TableTreeBaseModel(String key, TSConnection tsconn, Catalog catalog, Schema schema,
			DbObjectTreeModel baseModel) {
		super(key, tsconn, catalog, schema, baseModel);
	}

	/**
	 * Adds the fields that make up the given table to the tree node object that
	 * is associated with the table.
	 * 
	 * @param tableNode the tree node object that is associated with the table
	 * @param tmd the table whose fields we want to add
	 */
	protected void addFields(ObjectTreeNode tableNode, TableMetaData tmd) {
		if (tmd == null || tableNode == null)
			return;


		ColumnMetaData[] cols = tmd.getColumnsArray().clone();
		Arrays.sort(cols, (a, b) -> a.getColumnName().compareToIgnoreCase(b.getColumnName()));
		for( int index=0; index < cols.length; index++ ) {
			ColumnMetaData cmd = cols[index];
			ObjectTreeNode newcolnode = new ObjectTreeNode(cmd);
			insertNodeInto(newcolnode, tableNode, tableNode.getChildCount());
		}
	}

	/**
	 * TableTrees display tableids
	 */
	public Class getObjectClass() {
		return TableId.class;
	}

	/**
	 * SPECIALIZED CLASSES MUST OVERRIDE to provide specific logic to get the
	 * table meta data
	 */
	public abstract TableMetaData getTable(TSConnection conn, TableId tableid);

	/**
	 * Recursively searches the model for the given table node.
	 */
	ObjectTreeNode getTableNode(ObjectTreeNode parent, TableId tableId) {
		if (parent != null) {
			// first check parent
			Object userobj = parent.getUserObject();
			if (userobj instanceof TableId) {
				TableId id = (TableId) userobj;
				if (tableId.equals(id)) {
					return parent;
				}
			}

			int count = parent.getChildCount();
			for (int index = 0; index < count; index++) {
				ObjectTreeNode node = (ObjectTreeNode) parent.getChildAt(index);
				userobj = node.getUserObject();
				if (userobj instanceof TableId) {
					TableId id = (TableId) userobj;
					if (tableId.equals(id)) {
						return node;
					}
				}

				// now search this node's children
				node = getTableNode(node, tableId);
				if (node != null)
					return node;
			}
		}

		// node not found, return null
		return null;
	}

	/**
	 * Called when a table has been created in the underlying data. Tells this
	 * model to update itself
	 */
	public void tableCreated(TSConnection conn, TableId tableid) {
		assert (conn != null);
		ObjectTreeNode parent = getClassNode(conn, tableid.getCatalog(), tableid.getSchema());
		if (parent == null) {
			TSUtils.printMessage("TableTreeBaseModel.tableCreated failed.  Catalog: " + tableid.getCatalog()
					+ "  schema: " + tableid.getSchema());
			assert (parent != null);
		}

		int count = parent.getChildCount();
		if (count == 1) {
			ObjectTreeNode node = (ObjectTreeNode) parent.getChildAt(0);
			if (node instanceof EmptyTreeNode) {
				refreshModel(conn, tableid.getCatalog(), tableid.getSchema());
			}
		}

		for (int index = 0; index < count; index++) {
			ObjectTreeNode node = (ObjectTreeNode) parent.getChildAt(index);
			Object userobj = node.getUserObject();
			if (userobj instanceof TableId) {
				TableId siblingid = (TableId) userobj;
				if (tableid.compareTo(siblingid) < 0) {
					ObjectTreeNode newnode = new ObjectTreeNode(tableid);
					insertNodeInto(newnode, parent, index);
					addFields(newnode, getTable(conn, tableid));
					return;
				}
			}
		}
		ObjectTreeNode newnode = new ObjectTreeNode(tableid);
		insertNodeInto(newnode, parent, parent.getChildCount());
		addFields(newnode, getTable(conn, tableid));
	}

	/**
	 * Called when a table has changed in the underlying data. Tells this model
	 * to update itself
	 */
	public void tableChanged(TSConnection conn, TableId tableid) {
		// System.out.println( "TableTreeBaseModel.tableChanged: " + tableid );
		assert (conn != null);
		ObjectTreeNode node = getTableNode(getClassNode(conn, tableid.getCatalog(), tableid.getSchema()), tableid);
		if (node != null) {
			TableMetaData tmd = getTable(conn, tableid);

			int[] indices = new int[node.getChildCount()];
			Object[] children = new Object[node.getChildCount()];
			for (int index = 0; index < node.getChildCount(); index++) {
				ObjectTreeNode child = (ObjectTreeNode) node.getChildAt(index);
				indices[index] = index;
				children[index] = child;
			}

			node.removeAllChildren();
			nodesWereRemoved(node, indices, children);
			addFields(node, getTable(conn, tableid));
			fireTreeNodesChanged(node);
		}
	}

	/**
	 * Called when a table has been renamed in the underlying data. Tells this
	 * model to update itself
	 */
	public void tableRenamed(TSConnection conn, TableId newId, TableId oldId) {
		assert (newId != null);
		assert (oldId != null);

		if (newId.equals(oldId))
			return;

		ObjectTreeNode node = getTableNode(getClassNode(conn, oldId.getCatalog(), oldId.getSchema()), oldId);
		if (node != null) {
			if (newId.getSchema().equals(oldId.getSchema()) && newId.getCatalog().equals(oldId.getCatalog())) {
				node.setUserObject(newId);
				fireTreeNodesChanged(node);
			} else {
				// schema and/or catalog changed
				tableDeleted(conn, oldId);
				tableCreated(conn, newId);
			}
		}
	}

	/**
	 * Called when a table has been deleted in the underlying data. Tells this
	 * model to update itself
	 */
	public void tableDeleted(TSConnection conn, TableId tableId) {
		ObjectTreeNode node = getTableNode(getClassNode(conn, tableId.getCatalog(), tableId.getSchema()), tableId);
		if (node != null) {
			removeNodeFromParent(node);
		}
	}

}
