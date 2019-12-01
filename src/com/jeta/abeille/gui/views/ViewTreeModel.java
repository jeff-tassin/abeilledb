package com.jeta.abeille.gui.views;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.DbModelListener;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.ViewService;

import com.jeta.abeille.gui.model.EmptyTreeNode;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.DbObjectTreeModel;
import com.jeta.abeille.gui.model.TableTreeBaseModel;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

/**
 * ViewTreeModel is the GUI model for the tree view of the database views. This
 * view shows all views and schemas in a hierarchial view.
 * 
 * @author Jeff Tassin
 */
public class ViewTreeModel extends TableTreeBaseModel implements DbModelListener {
	public static final String CLASS_KEY = "view.viewtreemodel.";

	/**
	 * ctor
	 */
	public ViewTreeModel(TSConnection connection, Catalog catalog, Schema schema, DbObjectTreeModel treeModel) {
		super(CLASS_KEY, connection, catalog, schema, treeModel);
	}

	/**
	 * Called when we get an event from the DbModel
	 */
	public void eventFired(DbModelEvent evt) {
		TableId tableid = evt.getTableId();

		if (evt.getID() == DbModelEvent.VIEW_CHANGED || evt.getID() == DbModelEvent.TABLE_CHANGED) {
			tableChanged(evt.getConnection(), tableid);
		}
	}

	/**
	 * Override TableTreeBaseModel so we can get the table from the connection
	 */
	public TableMetaData getTable(TSConnection conn, TableId tableid) {
		return conn.getTable(tableid);
	}

	/**
	 * Initializes the tree view. Reads all schemas/tables from the model and
	 * inserts them into the tree.
	 */
	public void refreshModel(TSConnection conn, Catalog catalog, Schema schema) {
		try {
			removeAllChildren(conn, catalog, schema);
			loadModelState(conn, catalog, schema, TableId.class);

			ObjectTreeNode classnode = getClassNode(conn, catalog, schema);

			ViewService viewsrv = (ViewService) conn.getImplementation(ViewService.COMPONENT_ID);
			assert (viewsrv != null);

			DbModel model = conn.getModel(catalog);
			Collection views = viewsrv.getViews(catalog, schema);
			Iterator viter = views.iterator();
			while (viter.hasNext()) {
				TableId tableid = (TableId) viter.next();
				ObjectTreeNode newtablenode = addDatabaseObjectNode(conn, tableid);
				newtablenode.setLoaded(false);
				EmptyTreeNode emptynode = new EmptyTreeNode();
				insertNodeInto(emptynode, newtablenode, 0);
			}

			reload(classnode);
		} catch (Exception e) {
			TSUtils.printException(e);
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

	/**
	 * Called to refresh a view nodes children
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

}
