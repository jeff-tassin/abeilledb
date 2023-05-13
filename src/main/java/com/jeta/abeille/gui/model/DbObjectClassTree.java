package com.jeta.abeille.gui.model;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.dnd.DnDSupport;

import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.JETAController;
import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.open.support.EmptyCollection;

public abstract class DbObjectClassTree implements JETAContainer {
	private ObjectTree m_tree;

	private DbObjectClassRenderer m_renderer;
	private UIDirector m_uidirector;
	private JETAController m_controller;

	/**
	 * The menu items for the popup menu on this tree (JMenuItem objects).
	 */
	private ArrayList m_menu_items = new ArrayList();

	/**
	 * Creates an object class tree with the specified object tree owner.
	 */
	public DbObjectClassTree(ObjectTree tree) {
		m_tree = tree;
	}

	protected void addContextMenuItem(JMenuItem item) {
		m_menu_items.add(item);
	}

	/**
	 * This method is used to determine whether objects that we are dragging or
	 * pasting in this tree can be dropped on a given node in this tree. Classes
	 * are meant to override this method to provide specific drop checking for
	 * that particular tree. We only allow a paste or drop operation if the
	 * source of the data is this same tree. To indentify the source, the
	 * transfer object stores a transfer UID that we can compare to the one
	 * created by the tree when the the cut/drag operation started.
	 * 
	 * @param dropNode
	 *            the node we want to drop on.
	 * @param transferable
	 *            the objects we are dragging in the tree
	 */
	public boolean canDrop(ObjectTreeNode dropNode, Transferable transfer) {
		// first make sure the drop node is a TreeFolder or Schema
		ObjectTreeModel model = (ObjectTreeModel) getModel();
		Object dropobj = dropNode.getUserObject();
		if (!(dropobj instanceof TreeFolder) && !model.isClassNode(dropNode)) {
			return false;
		}

		Object[] paths = null;
		try {
			if (transfer.isDataFlavorSupported(DbObjectFlavor.TREE_PATH)) {
				ObjectTreeNode classnode = model.getClassNode(dropNode);
				assert (classnode != null);
				// now make sure that we are dropping to the same schema for
				// every node
				paths = (Object[]) transfer.getTransferData(DbObjectFlavor.TREE_PATH);
				if (paths != null) {
					for (int index = 0; index < paths.length; index++) {
						TreePath path = (TreePath) paths[index];
						ObjectTreeNode dragnode = (ObjectTreeNode) path.getLastPathComponent();
						// don't allow drop if basenode is different
						if (!dragnode.isNodeAncestor(classnode)) {
							return false;
						}
					}
				}
			}
		} catch (Exception e) {
			TSUtils.printStackTrace(e);
			return false;
		}

		boolean bresult = true;
		if (transfer.isDataFlavorSupported(DbObjectFlavor.TRANSFER_UID)) {
			try {
				String transfer_id = (String) transfer.getTransferData(DbObjectFlavor.TRANSFER_UID);
				MultiTransferable mt = getTransferable();
				if (mt == null) {
					bresult = false;
				} else {
					String tt_uid = (String) mt.getTransferData(DbObjectFlavor.TRANSFER_UID);
					if (tt_uid == null || !tt_uid.equals(transfer_id)) {
						bresult = false;
					}

					// now make sure the drop is to a valid folder or schema
					// parent
					if (bresult) {
						// Object[] paths = (Object[])mt.getTransferData(
						// DbObjectFlavor.TREE_PATH );
						if (paths != null) {
							for (int index = 0; index < paths.length; index++) {
								TreePath path = (TreePath) paths[index];
								ObjectTreeNode dragnode = (ObjectTreeNode) path.getLastPathComponent();
								// ignore drops on the same parent nor can we
								// drop an ancestor on a descendant
								if (dragnode.getParent() == dropNode || dragnode.isNodeDescendant(dropNode)
										|| dragnode == dropNode) {
									bresult = false;
									break;
								}
							}
						}
					}
				}
			} catch (Exception e) {
				TSUtils.printException(e);
				bresult = false;
			}
		} else {
			bresult = false;
		}

		return bresult;
	}

	public boolean canReload(ObjectTreeNode onode) {
		if (onode == null)
			return false;

		Object obj = onode.getUserObject();
		return !(obj instanceof TSConnection || obj instanceof SchemaWrapper || obj instanceof CatalogWrapper);
	}

	/**
	 * Creates a object that can handle simultaneaously transfering objects of
	 * different types Override if support drag and drop.
	 */
	public void createTransferable(MultiTransferable mt, TreePath path) {
	}

	/**
	 * Drops the given transferable onto the tree's given node
	 */
	public void drop(ObjectTreeNode parentnode, Transferable mt) {
		// this should be an array of tree paths
		try {
			Object[] paths = (Object[]) mt.getTransferData(DbObjectFlavor.TREE_PATH);
			if (paths != null) {
				ObjectTreeModel model = (ObjectTreeModel) getModel();
				for (int index = 0; index < paths.length; index++) {
					TreePath path = (TreePath) paths[index];
					ObjectTreeNode dragnode = (ObjectTreeNode) path.getLastPathComponent();

					// ignore drops on the same parent nor can we drop an
					// ancestor on a descendant
					if (dragnode.getParent() != parentnode && !dragnode.isNodeDescendant(parentnode)
							|| (dragnode == parentnode)) {
						// System.out.println( "object tree dropping node" );
						model.removeNodeFromParent(dragnode);

						Object userobj = dragnode.getUserObject();
						int nodeindex = parentnode.getChildCount();
						if (userobj instanceof TreeFolder) {
							TreeFolder folder = (TreeFolder) userobj;
							nodeindex = m_tree.getFolderInsertIndex(parentnode, folder);
						}
						model.insertNodeInto(dragnode, parentnode, nodeindex);
						dragnode.setMoveFlag(false);
					} else {
						System.out.println("dragnode/dropnode invalid drop - anscestor/descendant");
					}
				}

				model.sortNode(parentnode);
			} else {
				// System.out.println( "object tree.drop  paths = null" );

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Collection getContextMenuItems() {
		return m_menu_items;
	}

	public ObjectTreeModel getModel() {
		return (ObjectTreeModel) m_tree.getModel();
	}

	public ObjectTree getTree() {
		return m_tree;
	}

	public TSConnection getSelectedConnection() {
		return getConnection(m_tree.getSelectedNode());
	}

	public TSConnection getConnection(ObjectTreeNode node) {
		if (node == null)
			return null;
		return m_tree.getConnection(node);
	}

	public Catalog getCatalog(ObjectTreeNode node) {
		return m_tree.getCatalog(node);
	}

	public Schema getSchema(ObjectTreeNode node) {
		return m_tree.getSchema(node);
	}

	public DbObjectClassModel getClassModel(ObjectTreeNode node) {
		return getModel().getClassModel(node);
	}

	public ObjectTreeNode getSelectedNode() {
		return m_tree.getSelectedNode();
	}

	/**
	 * @return the transferable object for the tree. This is to support moving
	 *         of nodes within the same tree. We need this because some nodes
	 *         cannot be moved to other nodes and we need to determine exactly
	 *         what we are dragging and where to arrive at a accept/reject drag.
	 */
	MultiTransferable getTransferable() {
		return m_tree.getTransferable();
	}

	/**
	 * @return true if the transferable has flavors that are supported by this
	 *         QueryTree
	 */
	public boolean isDataFlavorSupported(Transferable transfer) {
		return false;
	}

	/**
	 * @return true if the given node is allowed to have subfolders or not
	 */
	public boolean nodeAllowsFolders(ObjectTreeNode node) {
		if (node == null)
			return false;

		// root node never allows child folders
		ObjectTreeModel model = (ObjectTreeModel) getModel();
		if (node == model.getRootNode())
			return false;

		Object userobj = node.getUserObject();
		if (userobj instanceof TreeFolder) {
			return true;
		} else {
			if (model.isClassNode(node)) {
				int childcount = node.getChildCount();
				if (childcount == 0)
					return false;
				else if (childcount == 1) {
					return (!(node.getChildAt(0) instanceof EmptyTreeNode));
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
	}

	public DbObjectClassRenderer getCellRenderer() {
		return m_renderer;
	}

	public void setCellRenderer(DbObjectClassRenderer renderer) {
		m_renderer = renderer;
	}

	/**
	 * JETAContainer Enables/Disables the menu/toolbar button associated with
	 * the commandid
	 * 
	 * @param commandId
	 *            the id of the command whose button to enable/disable
	 * @param bEnable
	 *            true/false to enable/disable
	 */
	public void enableComponent(String commandId, boolean bEnable) {
		for (int index = 0; index < m_menu_items.size(); index++) {
			JMenuItem item = (JMenuItem) m_menu_items.get(index);
			if (commandId.equals(item.getName())) {
				boolean en = item.isEnabled();
				if (en != bEnable)
					item.setEnabled(bEnable);
			}
		}
	}

	/**
	 * JETAContainer Locates the first component found in this container
	 * hierarchy that has the given name. This will recursively search into
	 * child containers as well. If no component is found with the given name,
	 * null is returned.
	 * 
	 * @param compName
	 *            the name of the component to search for
	 * @return the named component
	 */
	public Component getComponentByName(String compName) {
		for (int index = 0; index < m_menu_items.size(); index++) {
			JMenuItem item = (JMenuItem) m_menu_items.get(index);
			if (compName.equals(item.getName())) {
				return item;
			}
		}
		return null;
	}

	/**
	 * JETAContainer Locates all components found in this container hierarchy
	 * that has the given name. This will recursively search into child
	 * containers as well. This method is useful for frame windows that can have
	 * multiple components with the same name. For example, a menu item and
	 * toolbar button for the same command would have the same name.
	 * 
	 * @param compName
	 *            the name of the components to search for
	 * @return a collection of @see Component objects that have the given name.
	 */
	public Collection getComponentsByName(String compName) {
		LinkedList result = null;
		for (int index = 0; index < m_menu_items.size(); index++) {
			JMenuItem item = (JMenuItem) m_menu_items.get(index);
			if (compName.equals(item.getName())) {
				if (result == null)
					result = new LinkedList();

				result.add(item);
			}
		}

		if (result == null)
			return EmptyCollection.getInstance();
		else
			return result;
	}

	/**
	 * Returns the UIDirector for this container. UIDirectors are part of this
	 * framework and are responsible for enabling/disabling components based on
	 * the program state. For example, menu items and toolbar buttons must be
	 * enabled or disabled depending on the current state of the frame window.
	 * UIDirectors handle this logic.
	 * 
	 * @return the UIDirector
	 */
	public UIDirector getUIDirector() {
		return m_uidirector;
	}

	public JETAController getController() {
		return m_controller;
	}

	protected void setController(JETAController controller) {
		m_controller = controller;
	}

	protected void setUIDirector(UIDirector uidirector) {
		m_uidirector = uidirector;
	}

}
