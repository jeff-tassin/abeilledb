package com.jeta.abeille.gui.model;

import java.util.Collection;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

public abstract class DbObjectClassModel {
	private String m_class_key;

	private DbObjectTreeModel m_obj_model;

	private TSConnection m_connection;
	private Catalog m_catalog;
	private Schema m_schema;

	/**
	 * This is a flag that indicates if this serializer should store the
	 * heirarhcy for every node in the model. For some models, we want this
	 * behavior (e.g. FormTreeModel and QueryTreeModel). The reason is because
	 * those models actually maintain their data in the tree. Other models such
	 * as the TableTreeModel and SequenceTreeModel don't maintain their data in
	 * the tree. They get their data from the database. So, in this case, we
	 * only store the nodes that are in folders.
	 */
	private boolean m_storeallnodes = false;

	public DbObjectClassModel(String classKey, TSConnection tsconn, Catalog catalog, Schema schema,
			DbObjectTreeModel model) {
		this(classKey, tsconn, catalog, schema, model, false);
	}

	public DbObjectClassModel(String classKey, TSConnection tsconn, Catalog catalog, Schema schema,
			DbObjectTreeModel model, boolean storeAllNodes) {
		m_class_key = classKey;
		m_connection = tsconn;
		m_catalog = catalog;
		m_schema = schema;
		m_obj_model = model;
		m_storeallnodes = storeAllNodes;
	}

	protected ObjectTreeNode addDatabaseObjectNode(TSConnection conn, DatabaseObject obj) {
		return m_obj_model.addDatabaseObjectNode(conn, m_class_key, obj);
	}

	public void clearModelState(TSConnection conn, Catalog catalog, Schema schema) {
		m_obj_model.clearModelState(conn, catalog, schema, m_class_key);
	}

	public void loadModelState(TSConnection conn, Catalog catalog, Schema schema, Class userClass) {
		m_obj_model.loadModelState(conn, catalog, schema, m_class_key, userClass);
	}

	public ObjectTreeNode getClassNode(ObjectTreeNode node) {
		return m_obj_model.getClassNode(node);
	}

	public ObjectTreeNode getClassNode(TSConnection conn, Catalog cat, Schema schema) {
		return m_obj_model.getClassNode(conn, cat, schema, m_class_key);
	}

	/**
	 * Finds the first folder node or class node.
	 */
	public ObjectTreeNode getValidParent(ObjectTreeNode node) {
		return m_obj_model.getValidParent(node);
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	public TSConnection getConnection(ObjectTreeNode node) {
		return m_obj_model.getConnection(node);
	}

	public Catalog getCatalog() {
		return m_catalog;
	}

	public Catalog getCatalog(ObjectTreeNode node) {
		return m_obj_model.getCatalog(node);
	}

	public Schema getSchema() {
		return m_schema;
	}

	public Schema getSchema(ObjectTreeNode node) {
		return m_obj_model.getSchema(node);
	}

	public String getClassKey() {
		return m_class_key;
	}

	/**
	 * @return the main type of object we are displaying in the tree
	 */
	public abstract Class getObjectClass();

	public ObjectTreeNode getRootNode() {
		return (ObjectTreeNode) m_obj_model.getRootNode();
	}

	protected void fireTreeNodesInserted(ObjectTreeNode node) {
		m_obj_model._fireTreeNodesInserted(node, node.getPath(), null, null);
	}

	protected void fireTreeNodesChanged(ObjectTreeNode node) {
		m_obj_model._fireTreeNodesChanged(node, node.getPath(), null, null);
	}

	protected void fireTreeStructureChanged(ObjectTreeNode node) {
		m_obj_model._fireTreeStructureChanged(m_obj_model, node.getPath(), null, null);
	}

	public void nodesWereInserted(ObjectTreeNode node, int[] childIndices) {
		m_obj_model.nodesWereInserted(node, childIndices);
	}

	public void nodesWereRemoved(ObjectTreeNode node, int[] childIndices, Object[] children) {
		m_obj_model.nodesWereRemoved(node, childIndices, children);
	}

	protected Collection flattenTree(ObjectTreeNode classNode, Class nodeClass) {
		return m_obj_model.flattenTree(classNode, nodeClass);
	}

	/**
	 * Recursively searches the model for the node that contains the given user
	 * object
	 * 
	 * @param bequals
	 *            call equals method instead of checking for the same object
	 */
	public ObjectTreeNode getNode(ObjectTreeNode parent, Object userObject) {
		return m_obj_model.getNode(parent, userObject);
	}

	public boolean isStoreAllNodes() {
		return m_storeallnodes;
	}

	public void insertNodeInto(ObjectTreeNode newnode, ObjectTreeNode parent, int childIndex) {
		m_obj_model.insertNodeInto(newnode, parent, childIndex);
	}

	/**
	 * Causes the database or object store to reload itself. Then this model
	 * will refresh with the latest data.
	 */
	public void reloadModel(TSConnection conn, Catalog catalog, Schema schema) {
		// no op.
	}

	/**
	 * Loads the model state from the object store or database
	 */
	public void refreshModel(TSConnection conn, Catalog catalog, Schema schema) {

	}

	/**
	 * Refreshes the user object associated with the given node. The only reads
	 * the current object state from the database (cache) and does not reload
	 * the object.
	 */
	public void refreshNode(ObjectTreeNode node) {
		// no op
	}

	/**
	 * Reloads the user object associated with the given node. The should reload
	 * the object from the database if the object is a database object.
	 */
	public void reloadNode(ObjectTreeNode node) {
		// no op
	}

	public void reload(ObjectTreeNode node) {
		m_obj_model.reload(node);
	}

	public void removeAllChildren(TSConnection tsconn, Catalog catalog, Schema schema) {
		m_obj_model.removeAllChildren(tsconn, catalog, schema, m_class_key);
	}

	public void removeNodeFromParent(ObjectTreeNode node) {
		m_obj_model.removeNodeFromParent(node);
	}

	public void saveState(ObjectTreeNode classNode) {
		m_obj_model.saveState(classNode);
	}

	/**
	 * Sorts the childe nodes for a parent
	 */
	public void sortNode(ObjectTreeNode parent) {
		m_obj_model.sortNode(parent);
	}
}
