package com.jeta.abeille.gui.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * ObjectTreeModel is the GUI model for the database objects that are stored in
 * a hierarchial view.
 * 
 * @author Jeff Tassin
 */
public abstract class ObjectTreeModel extends DefaultTreeModel {
	/**
	 * A map of serializer keys to ObjectTreeSerializers
	 */
	private HashMap m_serializers = new HashMap();

	/**
	 * ctor
	 */
	public ObjectTreeModel() {
		super(new ObjectTreeNode());
	}

	public void addConnection(TSConnection connection) {
		loadBaseNodes(connection);
	}

	/**
	 * Adds the given database object to the tree model. It is loaded to any
	 * folder it was moved to in a previous session
	 */
	public ObjectTreeNode addDatabaseObjectNode(TSConnection conn, String classKey, DatabaseObject obj) {
		if (obj == null) {
			assert (false);
			return null;
		}

		DbObjectId dbid = obj.getObjectId();
		if (dbid == null) {
			assert (false);
			return null;
		}

		Schema schema = dbid.getSchema();
		if (schema == null) {
			assert (false);
			return null;
		}

		Catalog catalog = dbid.getCatalog();
		assert (catalog != null);

		// ObjectTreeNode parentnode = addBaseNode( conn, catalog, schema );
		ObjectTreeNode parentnode = addClassNode(conn, catalog, schema, classKey);

		if (m_serializers != null) {
			ObjectTreeSerializer ots = (ObjectTreeSerializer) m_serializers.get(getSerializerKey(conn, catalog, schema,
					classKey));
			if (ots != null) {
				/*
				 * NodeSerializer nodeserializer = ots.get( dbid ); if (
				 * nodeserializer != null ) { String parentuid =
				 * nodeserializer.getParentUID(); NodeSerializer ptnode =
				 * ots.get( parentuid ); if ( ptnode != null &&
				 * ptnode.getTreeNode() != null ) parentnode =
				 * ptnode.getTreeNode(); }
				 */
				ObjectTreeNode foldernode = ots.getParentNode(dbid);
				if (foldernode != null)
					parentnode = foldernode;
			}
		}

		ObjectTreeNode newnode = new ObjectTreeNode(obj);
		insertNodeInto(newnode, parentnode, parentnode.getChildCount());

		return newnode;
	}

	/**
	 * Adds the catalog node to the current view model.
	 * 
	 * @param catalog
	 *            the catalod to add
	 * @return the catalog node that was added or the currently existing node
	 */
	private ObjectTreeNode addCatalogNode(TSConnection conn, Catalog catalog) {
		if (conn.supportsCatalogs()) {
			ObjectTreeNode catnode = getCatalogNode(conn, catalog);
			if (catnode == null) {
				catnode = new ObjectTreeNode();
				CatalogWrapper wrapper = new CatalogWrapper(conn, catalog, catnode);
				catnode.setUserObject(wrapper);

				ObjectTreeNode servernode = getConnectionNode(conn);
				assert (servernode != null);
				insertNodeInto(catnode, servernode, servernode.getChildCount());
				return catnode;
			} else {
				return catnode;
			}
		} else {
			assert (false);
			return null;
		}
	}

	/**
	 * Adds the class node to the model
	 */
	abstract ObjectTreeNode addClassNode(TSConnection conn, Catalog catalog, Schema schema, String classKey);

	/**
	 * Adds the schema to the current view model. This is mainly used when the
	 * user creates a new schema/table with the modeler and we need to update
	 * the tree view.
	 * 
	 * @param schema
	 *            the schema to add
	 * @return the schema node that was added or the currently existing node
	 */
	public ObjectTreeNode addBaseNode(TSConnection conn, Catalog cat, Schema schema) {
		if (conn.supportsSchemas()) {
			if (conn.supportsCatalogs()) {
				addCatalogNode(conn, cat);
			}
			return addSchemaNode(conn, cat, schema);
		} else {
			if (conn.supportsCatalogs()) {
				return addCatalogNode(conn, cat);
			} else {
				return getConnectionNode(conn);
			}
		}
	}

	/**
	 * Adds the schema node to the given catalog/connection
	 */
	private ObjectTreeNode addSchemaNode(TSConnection conn, Catalog cat, Schema schema) {
		ObjectTreeNode schemanode = getSchemaNode(conn, cat, schema);
		if (schemanode == null) {
			assert (schema != null);
			schemanode = new ObjectTreeNode(schema);
			ObjectTreeNode parentnode = null;
			if (conn.supportsCatalogs()) {
				parentnode = addCatalogNode(conn, cat);
			} else {
				parentnode = getConnectionNode(conn);
			}
			assert (parentnode != null);
			SchemaWrapper wrapper = new SchemaWrapper(conn, cat, schema, schemanode);
			schemanode.setUserObject(wrapper);
			insertNodeInto(schemanode, parentnode, parentnode.getChildCount());
		}
		return schemanode;
	}

	/**
	 * Adds the server node to the current view model.
	 * 
	 * @param server
	 *            the server name to add
	 * @return the server node that was added or the currently existing node
	 */
	public ObjectTreeNode addServer(TSConnection conn) {
		ObjectTreeNode servernode = getConnectionNode(conn);
		if (servernode == null) {
			servernode = new ObjectTreeNode(conn);
			insertNodeInto(servernode, getRootNode(), getRootNode().getChildCount());
		}
		return servernode;
	}

	/**
	 * Clears the serializers from the cache because they are only needed at
	 * startup.
	 */
	protected void clearModelState(TSConnection conn, Catalog catalog, Schema schema, String classKey) {
		String schemakey = getSerializerKey(conn, catalog, schema, classKey);
		m_serializers.put(schemakey, null);
	}

	/**
	 * Creates a folder node in this model
	 * 
	 * @return the newly created folder node
	 */
	public ObjectTreeNode createFolder(ObjectTreeNode parentNode, String folderName) {
		TreeFolder treefolder = new TreeFolder(folderName);
		treefolder.setUID(TSUtils.createUID());

		int pos = -1;
		ObjectTreeNode foldernode = new ObjectTreeNode(treefolder);
		for (int index = 0; index < parentNode.getChildCount(); index++) {
			ObjectTreeNode node = (ObjectTreeNode) parentNode.getChildAt(index);
			Object userobj = node.getUserObject();
			if (!(userobj instanceof TreeFolder)) {
				pos = index;
				break;
			}
		}

		if (pos < 0)
			pos = parentNode.getChildCount();

		insertNodeInto(foldernode, parentNode, pos);
		return foldernode;
	}

	private void flattenTree(LinkedList results, ObjectTreeNode node, Class nodeClass) {
		if (node == null)
			return;

		Object userobj = node.getUserObject();
		if (nodeClass.isInstance(userobj))
			results.add(userobj);

		for (int index = 0; index < node.getChildCount(); index++) {
			flattenTree(results, (ObjectTreeNode) node.getChildAt(index), nodeClass);
		}
	}

	/**
	 * @returns all objects in the tree in a single linked list
	 */
	public Collection flattenTree(ObjectTreeNode classNode, Class nodeClass) {
		assert (classNode != null);
		assert (classNode.getUserObject() instanceof DbObjectClassModel);
		LinkedList results = new LinkedList();
		flattenTree(results, classNode, nodeClass);
		return results;
	}

	/**
	 * @return the catalog that is the ancestor of the given node. Null is
	 *         returned if this tree does not display schema nodes
	 */
	public Catalog getCatalog(ObjectTreeNode dropNode) {
		if (dropNode == null)
			return null;

		Object obj = dropNode.getUserObject();
		if (obj instanceof CatalogWrapper) {
			CatalogWrapper wrapper = (CatalogWrapper) obj;
			return wrapper.getCatalog();
		} else if (obj instanceof SchemaWrapper) {
			TSConnection conn = getConnection(dropNode);
			if (conn.supportsCatalogs()) {
				ObjectTreeNode parent = (ObjectTreeNode) dropNode.getParent();
				Object wrapper = parent.getUserObject();
				if (wrapper instanceof CatalogWrapper) {
					CatalogWrapper cw = (CatalogWrapper) wrapper;
					return cw.getCatalog();
				} else {
					assert (false);
					return null;
				}
			} else {
				ObjectTreeNode parent = (ObjectTreeNode) dropNode.getParent();
				assert (parent.getUserObject() instanceof TSConnection);
				return Catalog.VIRTUAL_CATALOG;
			}
		} else if (obj instanceof TSConnection) {
			TSConnection tsconn = (TSConnection) obj;
			assert (!tsconn.supportsCatalogs());
			assert (!tsconn.supportsSchemas());
			return Catalog.VIRTUAL_CATALOG;
		} else
			return getCatalog((ObjectTreeNode) dropNode.getParent());
	}

	/**
	 * Get the catalog node for the given connection root | -connection |
	 * -catalog
	 * 
	 * @return the catalog node associated with the given connection
	 * 
	 */
	private ObjectTreeNode getCatalogNode(TSConnection conn, Catalog cat) {
		ObjectTreeNode servernode = getConnectionNode(conn);
		assert (servernode != null);
		if (servernode != null) {
			for (int index = 0; index < servernode.getChildCount(); index++) {
				ObjectTreeNode node = (ObjectTreeNode) servernode.getChildAt(index);
				CatalogWrapper wrapper = (CatalogWrapper) node.getUserObject();
				if (wrapper.getCatalog().equals(cat))
					return node;
			}
			return null;
		} else {
			assert (false);
			return null;
		}
	}

	/**
	 * Finds the first folder node or class node.
	 */
	public ObjectTreeNode getValidParent(ObjectTreeNode node) {
		while (node != null) {
			Object userobj = node.getUserObject();
			if (userobj instanceof DbObjectClassModel)
				return node;
			else if (userobj instanceof TreeFolder)
				return node;

			node = (ObjectTreeNode) node.getParent();
		}
		return null;
	}

	/**
	 * @return the node that is the default parent for this node (either Schema
	 *         or Catalog)
	 */
	public ObjectTreeNode getClassNode(ObjectTreeNode node) {
		while (node != null) {
			Object userobj = node.getUserObject();
			if (userobj instanceof DbObjectClassModel)
				return node;

			node = (ObjectTreeNode) node.getParent();
		}
		return null;
	}

	public DbObjectClassModel getClassModel(ObjectTreeNode node) {
		ObjectTreeNode onode = getClassNode(node);
		if (onode != null)
			return (DbObjectClassModel) onode.getUserObject();
		else
			return null;
	}

	public String getClassKey(ObjectTreeNode node) {
		ObjectTreeNode cnode = getClassNode(node);
		if (cnode != null) {
			DbObjectClassModel cmodel = (DbObjectClassModel) cnode.getUserObject();
			return cmodel.getClassKey();
		}
		return null;
	}

	public ObjectTreeNode getClassNode(TSConnection conn, Catalog cat, Schema schema, String classKey) {
		ObjectTreeNode basenode = getBaseNode(conn, cat, schema);
		for (int index = 0; index < basenode.getChildCount(); index++) {
			ObjectTreeNode node = (ObjectTreeNode) basenode.getChildAt(index);
			DbObjectClassModel model = (DbObjectClassModel) node.getUserObject();
			if (classKey.equals(model.getClassKey())) {
				return node;
			}
		}
		return null;
	}

	public DbObjectClassModel getClassModel(TSConnection conn, Catalog cat, Schema schema, String classKey) {
		ObjectTreeNode onode = getClassNode(conn, cat, schema, classKey);
		if (onode != null)
			return (DbObjectClassModel) onode.getUserObject();
		else
			return null;
	}

	/**
	 * @return the connection that contains the given object tree node
	 */
	public TSConnection getConnection(ObjectTreeNode onode) {
		if (onode == null || onode == getRootNode())
			return null;

		Object userobj = onode.getUserObject();
		if (userobj instanceof TSConnection)
			return (TSConnection) userobj;
		else
			return getConnection((ObjectTreeNode) onode.getParent());
	}

	/**
	 * Return either the schema node or the catalog node for a given
	 * catalog/schema. If a database does not support schemas, then we return a
	 * catalog node If a database does not support catalogs(it therefore must
	 * support schemas), then we return a schema node. If a database supports
	 * both catalogs and schemas, we return a schema node.
	 * 
	 * @return the default base node for a given table. Most of the
	 */
	public ObjectTreeNode getBaseNode(TSConnection conn, Catalog catalog, Schema schema) {
		assert (conn != null);
		ObjectTreeNode result = null;
		if (conn.supportsSchemas()) {
			assert (schema != Schema.VIRTUAL_SCHEMA);
			result = getSchemaNode(conn, catalog, schema);
		} else {
			assert (schema == Schema.VIRTUAL_SCHEMA);
			if (conn.supportsCatalogs()) {
				result = getCatalogNode(conn, catalog);
			} else {
				assert (catalog == Catalog.VIRTUAL_CATALOG);
				result = getConnectionNode(conn);
			}
		}
		assert (result != null);
		return result;
	}

	/**
	 * @return the node that is the default parent for this node (either Schema
	 *         or Catalog)
	 */
	public ObjectTreeNode getBaseNode(TSConnection conn, ObjectTreeNode node) {
		if (node == null)
			return null;

		if (conn.supportsSchemas()) {
			Object userobj = node.getUserObject();
			if (userobj instanceof SchemaWrapper)
				return node;
			else
				return getBaseNode(conn, (ObjectTreeNode) node.getParent());
		} else {
			Object userobj = node.getUserObject();
			if (conn.supportsCatalogs()) {
				if (userobj instanceof CatalogWrapper)
					return node;
			} else {
				if (userobj instanceof TSConnection)
					return node;
			}
			return getBaseNode(conn, (ObjectTreeNode) node.getParent());
		}
	}

	/**
	 * Retrieves the default parent for the given node. If the node is null or
	 * it's default parent cannot be found, then we retreive the default parent
	 * for the current catalog or schema.
	 */
	public ObjectTreeNode getBaseNode(TSConnection conn, ObjectTreeNode node, boolean useDefault) {
		ObjectTreeNode parent = getBaseNode(conn, node);
		if (parent == null && useDefault) {
			if (conn.supportsSchemas()) {
				Catalog catalog = conn.getCurrentCatalog();
				Schema schema = conn.getCurrentSchema(catalog);
				parent = getSchemaNode(conn, catalog, schema);
			} else {
				Catalog catalog = conn.getCurrentCatalog();
				parent = getCatalogNode(conn, catalog);
			}
		}
		return parent;
	}

	/**
	 * @return the node that is the default parent for this node (either Schema
	 *         or Catalog)
	 */
	public ObjectTreeNode getBaseNode(ObjectTreeNode node) {
		TSConnection conn = getConnection(node);
		return getBaseNode(conn, node);
	}

	/**
	 * Recursively searches the model for the node that contains the given user
	 * object
	 * 
	 * @param bequals
	 *            call equals method instead of checking for the same object
	 */
	public ObjectTreeNode getNode(ObjectTreeNode parent, Object userObject) {
		if (parent != null) {
			// first check parent
			Object userobj = parent.getUserObject();
			if (userObject.equals(userobj)) {
				return parent;
			}

			int count = parent.getChildCount();
			for (int index = 0; index < count; index++) {
				ObjectTreeNode node = (ObjectTreeNode) parent.getChildAt(index);
				userobj = node.getUserObject();
				if (userObject.equals(userobj)) {
					return node;
				}

				// now search this node's children
				node = getNode(node, userObject);
				if (node != null)
					return node;
			}
		}

		// node not found, return null
		return null;
	}

	/**
	 * @returns a flattened list of all nodes contained by the given node. This
	 *          method is recursive.
	 */
	public void getNodes(ObjectTreeNode node, LinkedList nodes) {
		nodes.add(node);
		for (int index = 0; index < node.getChildCount(); index++) {
			ObjectTreeNode child = (ObjectTreeNode) node.getChildAt(index);
			getNodes(child, nodes);
		}
	}

	/**
	 * @return the key used to identify this model in the object store
	 */
	protected String getObjectStoreKey(Catalog cat, Schema schema, String classKey) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(classKey);
		buffer.append(".");
		buffer.append(cat.getName());
		buffer.append(".");
		buffer.append(schema.getName());
		return buffer.toString();
	}

	/**
	 * @return the root node for this model
	 */
	public ObjectTreeNode getRootNode() {
		return (ObjectTreeNode) getRoot();
	}

	/**
	 * @return a key used to identify a schema
	 */
	protected String getSerializerKey(TSConnection conn, Catalog cat, Schema schema, String classKey) {
		StringBuffer sbuff = new StringBuffer();
		sbuff.append(conn.getId().toString());
		sbuff.append(".");
		sbuff.append(cat.getName());
		sbuff.append(".");
		sbuff.append(schema.getName());
		sbuff.append(".");
		sbuff.append(classKey);
		return sbuff.toString();
	}

	/**
	 * @return the schema that is the ancestor of the given node. Null is
	 *         returned if this tree does not display schema nodes
	 */
	public Schema getSchema(ObjectTreeNode dropNode) {
		if (dropNode == null)
			return null;

		ObjectTreeNode basenode = getBaseNode(dropNode);
		assert (basenode != null);
		Object userobj = basenode.getUserObject();
		if (userobj instanceof SchemaWrapper) {
			SchemaWrapper wrapper = (SchemaWrapper) userobj;
			return wrapper.getSchema();
		} else {
			assert (userobj instanceof CatalogWrapper || userobj instanceof TSConnection);
			return Schema.VIRTUAL_SCHEMA;
		}
	}

	/**
	 * Searches the catalog node for a child node that is a schema
	 * (non-recursive)
	 * 
	 * @return the schema node in the tree
	 */
	private ObjectTreeNode getSchemaNode(TSConnection conn, Catalog cat, Schema schema) {
		if (schema == null) {
			assert (false);
			return null;
		}

		ObjectTreeNode parentnode = getConnectionNode(conn);
		if (conn.supportsCatalogs()) {
			parentnode = getCatalogNode(conn, cat);
		}

		ObjectTreeNode result = null;
		for (int index = 0; index < parentnode.getChildCount(); index++) {
			ObjectTreeNode node = (ObjectTreeNode) parentnode.getChildAt(index);
			Object obj = node.getUserObject();
			assert (obj instanceof SchemaWrapper);
			SchemaWrapper wrapper = (SchemaWrapper) obj;
			if (schema.equals(wrapper.getSchema()))
				return node;
		}
		return result;
	}

	/**
	 * @return the server node associated with the given connection. Server
	 *         nodes are found just below the root node
	 */
	public ObjectTreeNode getConnectionNode(TSConnection conn) {
		ObjectTreeNode root = getRootNode();
		for (int index = 0; index < root.getChildCount(); index++) {
			ObjectTreeNode node = (ObjectTreeNode) root.getChildAt(index);
			if (conn.equals(node.getUserObject()))
				return node;
		}
		return null;
	}

	/**
	 * Override just for testing
	 */
	public void insertNodeInto(MutableTreeNode newnode, MutableTreeNode parentnode, int index) {
		super.insertNodeInto(newnode, parentnode, index);
	}

	/**
	 * @return true if the given object node is a default parent node. That is,
	 *         if the connection supports schemas, then the onode must contain a
	 *         Schema user object. Otherwise, the onode must contain a Catalog
	 *         user object
	 */
	private boolean isBaseNode(ObjectTreeNode onode) {
		if (onode == null)
			return false;

		if (onode == getRootNode())
			return false;

		TSConnection conn = getConnection(onode);
		if (conn == null) {
			assert (false);
			return false;
		} else {
			if (conn.supportsSchemas()) {
				return (onode.getUserObject() instanceof SchemaWrapper);
			} else if (conn.supportsCatalogs()) {
				return (onode.getUserObject() instanceof CatalogWrapper);
			} else {
				return (onode.getUserObject() instanceof TSConnection);
			}
		}
	}

	/**
	 * @return true if the given object node is a default parent node. That is,
	 *         if the connection supports schemas, then the onode must contain a
	 *         Schema user object. Otherwise, the onode must contain a Catalog
	 *         user object
	 */
	public boolean isClassNode(ObjectTreeNode onode) {
		if (onode == null)
			return false;
		return (onode.getUserObject() instanceof DbObjectClassModel);
	}

	/**
	 * @return true if the given catalog has been fully loaded in this model.
	 */
	public boolean isLoaded(TSConnection conn, Catalog catalog, Schema schema, String classKey) {
		ObjectTreeNode classnode = getClassNode(conn, catalog, schema, classKey);
		assert (classnode != null);
		if (classnode.getChildCount() == 1) {
			ObjectTreeNode childnode = (ObjectTreeNode) classnode.getChildAt(0);
			return !(childnode instanceof EmptyTreeNode);
		} else {
			return true;
		}
	}

	/**
	 * Loads the connection and catalog nodes
	 */
	protected void loadBaseNodes(TSConnection tsconn) {
		addServer(tsconn);
		Collection catalogs = tsconn.getCatalogs();

		Iterator citer = catalogs.iterator();
		while (citer.hasNext()) {
			Catalog catalog = (Catalog) citer.next();
			DbModel model = tsconn.getModel(catalog);
			Collection schemas = model.getSchemas();
			Iterator siter = schemas.iterator();
			while (siter.hasNext()) {
				Schema schema = (Schema) siter.next();
				addBaseNode(tsconn, catalog, schema);
			}
		}
	}

	/**
	 * Loads the model state from the persitent store. This pre-loads the folder
	 * hierarchy for the tree. After this step, each specialized model loads its
	 * nodes.
	 * 
	 * @return the set of serializers (there is a serializer per schema)
	 */
	protected void loadModelState(TSConnection conn, Catalog catalog, Schema schema, String classKey, Class userClass) {
		ObjectStore os = conn.getObjectStore();
		ObjectTreeNode classnode = addClassNode(conn, catalog, schema, classKey);
		ObjectTreeSerializer ots = new ObjectTreeSerializer(os);
		ots.load(getObjectStoreKey(catalog, schema, classKey), this, classnode, classKey, userClass);
		String schemakey = getSerializerKey(conn, catalog, schema, classKey);
		m_serializers.put(schemakey, ots);
	}

	/**
	 * Causes the database or object store to reload itself. Then this model
	 * will refresh with the latest data.
	 */
	public void reloadModel(TSConnection conn, Catalog catalog, Schema schema, String classKey) {
		// no op.
	}

	/**
	 * Loads the model state from the object store or database
	 */
	public void refreshModel(TSConnection conn, Catalog catalog, Schema schema, String classKey) {

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

	/**
	 * Removes all child nodes from the given catalog node
	 */
	public void removeAllChildren(TSConnection conn, Catalog catalog, Schema schema, String classKey) {
		ObjectTreeNode classnode = getClassNode(conn, catalog, schema, classKey);
		if (classnode != null) {
			classnode.removeAllChildren();
		}
	}

	/**
	 * Saves the model state to the object store. It stores the entire tree
	 * starting at each base node.
	 * 
	 * @return a Collection of ObjectTreeSerializer objects (one per base node)
	 */
	public void saveState(ObjectTreeNode classNode) {
		assert (classNode != null);
		Object userobj = classNode.getUserObject();
		if (userobj instanceof DbObjectClassModel) {

			DbObjectClassModel classmodel = (DbObjectClassModel) userobj;
			TSConnection tsconn = getConnection(classNode);
			Catalog cat = getCatalog(classNode);
			Schema schema = getSchema(classNode);

			ObjectStore os = tsconn.getObjectStore();
			assert (isLoaded(tsconn, cat, schema, classmodel.getClassKey()));
			ObjectTreeSerializer ots = new ObjectTreeSerializer(os, classmodel.isStoreAllNodes());
			ots.write(getObjectStoreKey(cat, schema, classmodel.getClassKey()), classNode, classmodel.getClassKey(),
					classmodel.getObjectClass());

		} else {
			assert (false);
		}
	}

	/**
	 * This saves the tree heirachy for the given connection
	 */
	public void saveHeirarchy(TSConnection tsconn) {
		ObjectTreeNode node = getRootNode();
		for (int index = 0; index < node.getChildCount(); index++) {
			ObjectTreeNode childnode = (ObjectTreeNode) node.getChildAt(index);
			if (tsconn.equals(childnode.getUserObject())) {
				saveNodeState(childnode);
			}
		}
	}

	/**
	 * Saves the frame state. This saves the tree heirachy.
	 */
	private void saveNodeState(ObjectTreeNode node) {
		if (node == null)
			return;

		Object userobj = node.getUserObject();
		if (userobj instanceof DbObjectClassModel) {
			DbObjectClassModel classmodel = (DbObjectClassModel) userobj;

			TSConnection tsconn = getConnection(node);
			Catalog cat = getCatalog(node);
			Schema schema = getSchema(node);
			if (isLoaded(tsconn, cat, schema, classmodel.getClassKey())) {
				classmodel.saveState(node);
			}
		} else {
			for (int index = 0; index < node.getChildCount(); index++) {
				ObjectTreeNode childnode = (ObjectTreeNode) node.getChildAt(index);
				saveNodeState(childnode);
			}
		}
	}

	/**
	 * Sorts the childe nodes for a parent
	 */
	public void sortNode(ObjectTreeNode parent) {
		parent.sortChildren();
		nodeStructureChanged(parent);
	}

	/**
	 * Subclassed to message setString() to the changed path item.
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		// System.out.println( "node changed" );
		/* Update the user object. */
		ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();

		Object userobj = node.getUserObject();
		if (userobj instanceof TreeFolder) {
			((TreeFolder) userobj).setName((String) newValue);
		}
		/*
		 * Since we've changed how the data is to be displayed, message
		 * nodeChanged.
		 */
		nodeChanged(node);
	}

}
