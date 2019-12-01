package com.jeta.abeille.gui.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.DbModelListener;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;

import com.jeta.foundation.utils.TSUtils;

/**
 * TableTreeModel is the GUI model for the tree view of the database This view
 * shows all schemas, tables, and fields in a hierarchial view.
 * 
 * @author Jeff Tassin
 */
public class TableTreeModel extends TableTreeBaseModel {
	public static final String CLASS_KEY = "table.tabletreemodel.";

	protected TableTreeRenderer m_renderer;

	/**
	 * ctor
	 */
	public TableTreeModel(TSConnection connection, Catalog catalog, Schema schema, DbObjectTreeModel parentModel) {
		super(CLASS_KEY, connection, catalog, schema, parentModel);
		m_renderer = new TableTreeRenderer();
	}

	/**
	 * ctor
	 */
	public TableTreeModel(String classKey, TSConnection connection, Catalog catalog, Schema schema,
			DbObjectTreeModel parentModel) {
		super(classKey, connection, catalog, schema, parentModel);
		m_renderer = new TableTreeRenderer();
	}

	/**
	 * Returns then renderer for the nodes in the class of objects
	 */
	public DbObjectClassRenderer getRenderer() {
		return m_renderer;
	}

	/**
	 * Override TableTreeBaseModel so we can get the table from the connection
	 */
	public TableMetaData getTable(TSConnection conn, TableId tableid) {
		return conn.getTable(tableid);
	}

	protected String getTableType() {
		return "TABLE";
	}

	/**
	 * Called when a tree node is being expanded. Allows specialized classes to
	 * load child elements for the node if the node has not been loaded yet
	 */
	public void refreshNode(ObjectTreeNode node) {
		if (node != null) {
			TSConnection tsconn = getConnection();
			Object userobj = node.getUserObject();
			if (userobj instanceof TableId) {
				node.removeAllChildren();
				addFields(node, getTable(tsconn, (TableId) userobj));
				fireTreeNodesInserted(node);
			}
		}
	}

	/**
	 * Called when a tree node is being expanded. Allows specialized classes to
	 * load child elements for the node if the node has not been loaded yet
	 */
	public void reloadNode(ObjectTreeNode node) {
		if (node != null) {
			TSConnection tsconn = getConnection();
			Object userobj = node.getUserObject();
			if (userobj instanceof TableId) {
				TableId tableid = (TableId) userobj;
				tsconn.getModel(tableid.getCatalog()).reloadTable(tableid);
			} else if (userobj == this) {
				Catalog catalog = getCatalog(node);
				Schema schema = getSchema(node);
				if (catalog != null && schema != null) {
					reloadModel(tsconn, catalog, schema);
				}
			}
		}
	}

	/**
	 * Initializes the tree view. Reads all schemas/tables from the model and
	 * inserts them into the tree.
	 * 
	 * @param catalog
	 *            the catalog we are reloading
	 * @param schema
	 *            the scheme to reload. If the database does not support
	 *            schemas, then this valus is null
	 */
	public void refreshModel(TSConnection conn, Catalog catalog, Schema schema) {
		try {
			removeAllChildren(conn, catalog, schema);
			ObjectTreeNode classnode = getClassNode(conn, catalog, schema);
			assert (classnode.getChildCount() == 0);

			loadModelState(conn, catalog, schema, TableId.class);
			DbModel model = conn.getModel(catalog);
			Collection tables = model.getTables(schema, getTableType());
			Iterator tableiter = tables.iterator();
			while (tableiter.hasNext()) {
				TableId tableid = (TableId) tableiter.next();
				TableMetaData tmd = model.getTableFast(tableid);
				if (!tmd.isView()) {
					ObjectTreeNode newtablenode = addDatabaseObjectNode(conn, tableid);
					newtablenode.setLoaded(false);
					EmptyTreeNode emptynode = new EmptyTreeNode();
					insertNodeInto(emptynode, newtablenode, 0);
				}
			}
			reload(classnode);
		} finally {
			clearModelState(conn, catalog, schema);
		}
	}

	/**
	 * Causes the database or object store to reload itself. Then this model
	 * will refresh with the latest data.
	 */
	public void reloadModel(TSConnection conn, Catalog catalog, Schema schema) {
		conn.getModel(catalog).reload(schema);
		refreshModel(conn, catalog, schema);
	}

}
