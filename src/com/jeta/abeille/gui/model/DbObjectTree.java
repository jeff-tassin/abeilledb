package com.jeta.abeille.gui.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Iterator;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.foundation.gui.dnd.DnDSupport;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class shows database objects in a tree view
 * 
 * @author Jeff Tassin
 */
public class DbObjectTree extends ObjectTree {
	/**
	 * A map of class_keys (String) to DbObjectClassTree instances
	 */
	private TreeMap m_class_trees = new TreeMap();

	/**
	 * ctor
	 */
	public DbObjectTree(DbObjectTreeModel model) {
		super(model);
		setCellRenderer(new DbObjectTreeRenderer());
		setName(DbObjectTree.class.getName() + TSUtils.createUID());
	}

	/**
	 * This method is used to determine whether objects that we are dragging in
	 * this tree can be dropped on a given node in this tree. Classes are meant
	 * to override this method to provide specific drop checking for that
	 * particular tree. For example, if we are dragging two tables and a folder,
	 * we are only allowed to drop if the dropNode is a folder or a schema. We
	 * cannot drop a table on another table.
	 * 
	 * @param dropNode
	 *            the node we want to drop on.
	 * @param transferable
	 *            the objects we are dragging in the tree
	 */
	public boolean canDrop(ObjectTreeNode dropNode, Transferable transfer) {
		ObjectTreeModel model = (ObjectTreeModel) getModel();
		String class_key = model.getClassKey(dropNode);
		if (class_key != null) {
			DbObjectClassTree tree = getClassTree(class_key);
			if (tree != null) {
				return tree.canDrop(dropNode, transfer);
			}
		}
		return false;

	}

	/**
	 * @return true if the given node can be reloaded in the view. For nodes
	 *         such as schema and connection, this is generally true if the node
	 *         is a base node
	 */
	public boolean canReload(ObjectTreeNode onode) {
		if (onode == null)
			return false;
		else {
			ObjectTreeModel model = (ObjectTreeModel) getModel();
			if (model.isClassNode(onode))
				return true;
			else {
				String class_key = model.getClassKey(onode);
				if (class_key != null) {
					DbObjectClassTree tree = getClassTree(class_key);
					if (tree != null) {
						return tree.canReload(onode);
					}
				}
			}
		}
		return false;
	}

	/**
	 * Creates a object that can handle simultaneaously transfering objects of
	 * different types Override if support drag and drop.
	 */
	public Transferable createTransferable() {
		MultiTransferable mt = null;
		TreePath[] selections = getSelectionPaths();
		if (selections.length > 0) {
			ObjectTreeModel model = (ObjectTreeModel) getModel();

			mt = new MultiTransferable();
			for (int index = 0; index < selections.length; index++) {
				TreePath path = selections[index];
				ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
				String class_key = model.getClassKey(node);
				if (class_key != null) {
					DbObjectClassTree tree = getClassTree(class_key);
					if (tree != null) {
						tree.createTransferable(mt, path);
					}
				}
			}
		}
		return mt;
	}

	/**
	 * Drops the given transferable onto the tree's given node
	 */
	public void drop(ObjectTreeNode parentnode, Transferable mt) {
		ObjectTreeModel model = (ObjectTreeModel) getModel();
		String class_key = model.getClassKey(parentnode);
		if (class_key != null) {
			DbObjectClassTree tree = getClassTree(class_key);
			if (tree != null)
				tree.drop(parentnode, mt);
		}
	}

	DbObjectClassTree getClassTree(String classKey) {
		if (classKey != null) {
			DbObjectClassTree tree = (DbObjectClassTree) m_class_trees.get(classKey);
			if (tree == null) {
				DbObjectClassFactory factory = DbObjectClassFactory.getFactory(classKey);
				tree = factory.createClassTree(this);
				m_class_trees.put(classKey, tree);
			}
			return tree;
		}
		return null;
	}

	public boolean nodeAllowsFolders(ObjectTreeNode node) {
		ObjectTreeModel model = (ObjectTreeModel) getModel();
		DbObjectClassTree tree = getClassTree(model.getClassKey(node));
		if (tree != null)
			return tree.nodeAllowsFolders(node);
		else
			return false;
	}

}
