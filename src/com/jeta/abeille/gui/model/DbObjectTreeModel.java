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
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

/**
 * 
 * @author Jeff Tassin
 */
public class DbObjectTreeModel extends ObjectTreeModel {

	/**
	 * ctor
	 */
	public DbObjectTreeModel() {
	}

	public void addConnection(TSConnection tsconn) {
		Collection class_keys = DbObjectClassFactory.getFactories(tsconn);
		Iterator iter = class_keys.iterator();
		while (iter.hasNext()) {
			DbObjectClassFactory factory = (DbObjectClassFactory) iter.next();
			addConnection(tsconn, factory.getClassKey());
		}
	}

	public void addConnection(TSConnection tsconn, String class_key) {
		super.addConnection(tsconn);

		Collection catalogs = tsconn.getCatalogs();
		Iterator citer = catalogs.iterator();
		while (citer.hasNext()) {
			Catalog catalog = (Catalog) citer.next();
			System.out.println("DBObjectTree addconnn catalog: " + catalog);
			Collection schemas = tsconn.getSchemas(catalog);
			Iterator siter = schemas.iterator();
			while (siter.hasNext()) {
				Schema schema = (Schema) siter.next();
				System.out.println("          DBObjectTree addconnn schema: " + schema);
				addClassNode(tsconn, catalog, schema, class_key);
			}
		}
	}

	/**
	 * Adds the class node to the model
	 */
	ObjectTreeNode addClassNode(TSConnection conn, Catalog catalog, Schema schema, String classKey) {
		ObjectTreeNode classnode = getClassNode(conn, catalog, schema, classKey);
		if (classnode == null) {
			ObjectTreeNode basenode = addBaseNode(conn, catalog, schema);
			DbObjectClassFactory fac = DbObjectClassFactory.getFactory(classKey);
			if (fac != null) {
				DbObjectClassModel model = fac.createClassModel(conn, catalog, schema, this);
				classnode = new ObjectTreeNode(model);
				insertNodeInto(classnode, basenode, basenode.getChildCount());
				EmptyTreeNode emptynode = new EmptyTreeNode();
				insertNodeInto(emptynode, classnode, 0);
			} else {
				assert (false);
			}
		}
		return classnode;
	}

	void _fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		super.fireTreeNodesChanged(source, path, childIndices, children);
	}

	void _fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
		super.fireTreeNodesInserted(source, path, childIndices, children);
	}

	void _fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		super.fireTreeNodesInserted(source, path, childIndices, children);
	}

	/**
	 * Called when a tree node is being expanded. Allows specialized classes to
	 * load child elements for the node if the node has not been loaded yet
	 */
	public void refreshNode(ObjectTreeNode node) {
		ObjectTreeNode classnode = getClassNode(node);
		if (classnode != null) {
			DbObjectClassModel model = (DbObjectClassModel) classnode.getUserObject();
			model.refreshNode(node);
		}
	}

	/**
	 * Causes the database or object store to reload itself. Then this model
	 * will refresh with the latest data.
	 */
	public void reloadModel(TSConnection conn, Catalog catalog, Schema schema, String classKey) {

	}

	/**
	 * Causes the database or object store to reload itself. Then this model
	 * will refresh with the latest data.
	 */
	public void refreshModel(TSConnection conn, Catalog catalog, Schema schema, String classKey) {
		ObjectTreeNode classnode = getClassNode(conn, catalog, schema, classKey);
		if (classnode != null) {
			DbObjectClassModel model = (DbObjectClassModel) classnode.getUserObject();
			model.refreshModel(conn, catalog, schema);
		}
	}

}
