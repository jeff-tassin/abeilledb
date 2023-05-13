package com.jeta.abeille.gui.formbuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.DefaultTreeModel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.DbObjectClassModel;
import com.jeta.abeille.gui.model.DbObjectTreeModel;
import com.jeta.abeille.gui.model.ObjectTreeNode;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

/**
 * FormTreeModel is the GUI model for the tree view of queries that the user has
 * built and stored
 * 
 * @author Jeff Tassin
 */
public class FormTreeModel extends DbObjectClassModel {
	public static final String CLASS_KEY = "forms.formtreemodel.";
	public static final String FORMS_MODEL = "formbuilder.forms";

	/**
	 * ctor
	 */
	public FormTreeModel(TSConnection connection, Catalog catalog, Schema schema, DbObjectTreeModel treeModel) {
		super(CLASS_KEY, connection, catalog, schema, treeModel, true);
	}

	/**
	 * Adds a query to the model
	 * 
	 * @param parentNode
	 *            the parent node to add the query to
	 * @param formproxy
	 *            the query proxy object to add to the model
	 */
	ObjectTreeNode addForm(ObjectTreeNode parentNode, FormProxy formproxy) {
		ObjectTreeNode querynode = new ObjectTreeNode(formproxy);
		formproxy.setFormTreeModel(this);
		insertNodeInto(querynode, parentNode, parentNode.getChildCount());
		fireTreeStructureChanged(parentNode);
		return querynode;
	}

	/**
	 * @return the main type of object we are displaying in the tree
	 */
	public Class getObjectClass() {
		return FormProxy.class;
	}

	/**
	 * @return the key that identifies where we store the queries for a given
	 *         schema/catalog
	 */
	private String getFormsStoreKey(Catalog cat, Schema schema) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(cat.getName());
		buffer.append(".");
		buffer.append(schema.getName());
		buffer.append(".");
		buffer.append(FORMS_MODEL);
		return buffer.toString();
	}

	/**
	 * Loads the model state from the object store
	 */
	public void refreshModel(TSConnection tsconn, Catalog catalog, Schema schema) {
		try {
			removeAllChildren(tsconn, catalog, schema);
			loadModelState(tsconn, catalog, schema, FormProxy.class);

			ObjectStore os = tsconn.getObjectStore();
			Collection forms = (Collection) os.load(getFormsStoreKey(catalog, schema));
			if (forms != null) {
				Iterator iter = forms.iterator();
				while (iter.hasNext()) {
					FormProxy proxy = (FormProxy) iter.next();
					proxy.setFormTreeModel(this);
					addDatabaseObjectNode(tsconn, proxy);
				}
			}

			fireTreeStructureChanged(getRootNode());
		} catch (Exception e) {
			TSUtils.printException(e);
		} finally {
			clearModelState(tsconn, catalog, schema);
		}
	}

	/**
	 * Notify the tree that the form data has changed so that the tree can
	 * update itself
	 */
	public void notifyFormChanged(FormProxy proxy) {
		ObjectTreeNode node = getNode(getRootNode(), proxy);
		if (node != null) {
			fireTreeNodesChanged(getRootNode());
		}
	}

	public void saveState(ObjectTreeNode classNode) {
		try {
			super.saveState(classNode);
			Collection forms = flattenTree(classNode, FormProxy.class);
			TSConnection tsconn = getConnection(classNode);
			assert (tsconn == getConnection());
			ObjectStore os = tsconn.getObjectStore();
			Catalog cat = getCatalog(classNode);
			Schema schema = getSchema(classNode);
			os.store(getFormsStoreKey(cat, schema), forms);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

}
