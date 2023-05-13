package com.jeta.abeille.gui.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * This class provides support for loading/storing an ObjectTree hierarchy. This
 * includes TreeFolders and their parent/child relationships.
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeSerializer {
	/** the persitence store */
	private ObjectStore m_os;

	/** a hash of nodeUID keys (java.lang.Object) to NodeSerializers (values) */
	private HashMap m_nodes = new HashMap();

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

	/**
	 * ctor Initializes this object for storage/retrieval of an ObjectTreeModel
	 * 
	 * @param os
	 *            the object store instance
	 */
	public ObjectTreeSerializer(ObjectStore os) {
		m_os = os;
	}

	/**
	 * ctor Initializes this object for storage/retrieval of an ObjectTreeModel
	 * 
	 * @param os
	 *            the object store instance
	 */
	public ObjectTreeSerializer(ObjectStore os, boolean storeAll) {
		this(os);
		m_storeallnodes = storeAll;
	}

	/**
	 * ONLY used for debugging
	 */
	public HashMap getNodeMap() {
		return m_nodes;
	}

	/**
	 * @return a node serializer object that has the given UID
	 */
	public NodeSerializer get(Object nodeUID) {
		return (NodeSerializer) m_nodes.get(nodeUID);
	}

	/**
	 * @return a flattened list of NodeSerializer objects that make up the
	 *         ObjectTree
	 */
	public Collection getNodes() {
		return m_nodes.values();
	}

	/**
	 * Loads a previously stored model into the given model
	 */
	public void load(String storeKey, ObjectTreeModel model, ObjectTreeNode baseNode, String baseUID) {
		load(storeKey, model, baseNode, baseUID, null);
	}

	/**
	 * Loads a previously stored model into the given model
	 */
	public void load(String storeKey, ObjectTreeModel model, ObjectTreeNode baseNode, String baseUID, Class userClass) {
		try {
			// System.out.println( "ObjectTreeSerializer load: " + storeKey +
			// "   userclas: " + userClass );
			m_nodes = new HashMap();
			LinkedList list = null;
			try {
				list = (LinkedList) m_os.load(storeKey);
			} catch (java.io.IOException ioe) {
				ioe.printStackTrace();
			}

			if (list != null) {
				Iterator iter = list.iterator();
				while (iter.hasNext()) {
					NodeSerializer otnode = (NodeSerializer) iter.next();
					Object userobj = otnode.getUserObject();

					// System.out.println( "Object tree serializer.load: " +
					// storeKey + "   userobj: " + userobj );
					if (userobj instanceof TreeFolder) {
						// System.out.println(
						// "ObjectTreeSerializer.load  key = " + storeKey +
						// "  got tree folder: " + userobj + "   uid: " +
						// otnode.getUID() );
						ObjectTreeNode parentnode = baseNode;
						if (baseUID.equals(otnode.getParentUID())) {
							parentnode = baseNode;
						} else {
							NodeSerializer parentns = (NodeSerializer) m_nodes.get(otnode.getParentUID());
							if (parentns != null)
								parentnode = parentns.getTreeNode();
						}

						if (parentnode != null) {
							ObjectTreeNode newfoldernode = new ObjectTreeNode(userobj);
							model.insertNodeInto(newfoldernode, parentnode, parentnode.getChildCount());
							otnode.setTreeNode(newfoldernode);
							m_nodes.put(otnode.getUID(), otnode);
						}
					} else if (userobj != null) {
						// System.out.println(
						// "ObjectTreeSerializer.load user obj key = " +
						// storeKey + " userobj: " + userobj + "  uid: " +
						// otnode.getUID() + "   parentuid = " +
						// otnode.getParentUID() );

						m_nodes.put(userobj, otnode);

						// ObjectTreeNode parentnode = baseNode;
						// String parentuid = otnode.getParentUID();
						// NodeSerializer parentns = get( parentuid );
						// if ( parentns != null && parentns.getTreeNode() !=
						// null )
						// parentnode = parentns.getTreeNode();
						// ObjectTreeNode newnode = new ObjectTreeNode( userobj
						// );
						// model.insertNodeInto( newnode, parentnode,
						// parentnode.getChildCount());
					}
				}
			}
		} catch (Exception e) {
			// let's catch all exceptions here so that even if we have an error,
			// the
			// tree can still be loaded in its default hierarhcy
			e.printStackTrace();
		}
	}

	public ObjectTreeNode getParentNode(Object nodeKey) {
		if (nodeKey == null)
			return null;

		NodeSerializer otnode = (NodeSerializer) m_nodes.get(nodeKey);
		// System.out.println( "ObjectTreeSeralizer.getParentNode   nodeKey: " +
		// nodeKey + "   nodeserailizer: " + otnode );
		if (otnode != null) {
			String parentuid = otnode.getParentUID();
			NodeSerializer parentns = get(parentuid);
			if (parentns != null && parentns.getTreeNode() != null)
				return parentns.getTreeNode();
		}
		return null;
	}

	/**
	 * Writes out the given node to the object store
	 */
	public void write(String storeKey, ObjectTreeNode node, String nodeUID) {
		write(storeKey, node, nodeUID, null);
	}

	/**
	 * Writes out the given node to the object store. Only folders and objects
	 * of the given node type are written out.
	 */
	public void write(String storeKey, ObjectTreeNode node, String nodeUID, Class userClass) {
		LinkedList data = new LinkedList();
		writeState(data, node, node, nodeUID, userClass);
		try {
			m_os.store(storeKey, data);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Writes out the given node to the object store
	 */
	public void writeBasic(String storeKey, ObjectTreeNode node, String nodeUID) {
		assert (node != null);
		LinkedList data = new LinkedList();
		writeState(data, null, node, nodeUID, null);
		try {
			m_os.store(storeKey, data);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Saves the model state for a node. This flattens all nodes in the tree
	 * into a linked list. We then store the list to the object store.
	 * 
	 * @param data
	 *            this is the list of nodes in the tree flattened out into a
	 *            list. The parent is written first and then the children so
	 *            that when we reconstitute the tree, the parent will already be
	 *            known when a child is read.
	 * @param startNode
	 *            this is the node that the defines the starting point. We use
	 *            this because we don't store nodes (other than folders) that
	 *            are the direct children of this node since that is the default
	 *            location anyway.
	 * @param parent
	 *            this the parent node whose children we wish to write. (This
	 *            call is recursive). On the first call, the startNode = parent
	 * @param parentUID
	 *            this is a unique identifier for the parent node.
	 * @param userClass
	 *            this is an optional class (can be null) that the caller can
	 *            pass to tell this method that only folders and userobjects of
	 *            this type should be stored. Objects of other types are
	 *            ignored.
	 */
	private void writeState(LinkedList data, ObjectTreeNode startNode, ObjectTreeNode parent, String parentUID,
			Class userClass) {
		// System.out.println( "ObjectTreeSerializer.writeState  storall = " +
		// m_storeallnodes + "  childcount  " + parent.getChildCount() );
		for (int index = 0; index < parent.getChildCount(); index++) {
			ObjectTreeNode node = (ObjectTreeNode) parent.getChildAt(index);
			Object obj = node.getUserObject();
			if (obj == null && !(node instanceof EmptyTreeNode)) {
				// System.out.println(" ObjectTreeSerializer.writeState  userobj NULL"
				// );
			}

			if (obj instanceof TreeFolder) {
				TreeFolder folder = (TreeFolder) obj;
				NodeSerializer nodestore = new NodeSerializer(folder, folder.getUID());
				nodestore.setParentUID(parentUID);
				// System.out.println(" ObjectTreeSerializer.writeState:  storing folder: "
				// + folder.getUID() );

				data.add(nodestore);
				writeState(data, null, node, folder.getUID(), userClass);
			} else if (obj != null && (m_storeallnodes || (startNode != parent))) {
				if (obj instanceof DatabaseObject) {
					DatabaseObject dbobj = (DatabaseObject) obj;
					NodeSerializer nodestore = new NodeSerializer(dbobj.getObjectId(), null);
					// System.out.println(" ObjectTreeSerializer.writeState  userobj: "
					// + obj.getClass() + "  parentUID: " + parentUID );

					nodestore.setParentUID(parentUID);
					data.add(nodestore);
				} else if (userClass == null || userClass.isInstance(obj)) {
					// we assume that the user obj is serializable
					NodeSerializer nodestore = new NodeSerializer(obj, null);
					nodestore.setParentUID(parentUID);
					data.add(nodestore);
				}
			}
		}
	}

}
