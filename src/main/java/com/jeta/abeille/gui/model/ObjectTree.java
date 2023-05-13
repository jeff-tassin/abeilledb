package com.jeta.abeille.gui.model;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;

import java.util.EventObject;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.utils.TSUtils;

/**
 * 
 * @author Jeff Tassin
 */
// public class ObjectTree extends JTree implements DragGestureListenere
public abstract class ObjectTree extends JTree {
	/**
	 * enables this component to be a drop target
	 */
	private DropTarget m_droptarget;

	/** for local dragging of nodes in the tree */
	private MultiTransferable m_transferable;

	/** flag that indicates if we are currently dragging or not */
	private boolean m_dragging = false;

	public static final String MOVE_NODE = "objectree.move.node";

	/**
	 * ctor
	 */
	public ObjectTree(ObjectTreeModel model) {
		super(model);

		setEditable(true);

		setDragEnabled(true);
		setEnabled(true);

		setTransferHandler(new ObjectTreeTransferHandler(this));

		setShowsRootHandles(true);
		putClientProperty("JTree.lineStyle", "Angled");

		setRootVisible(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		m_droptarget = new DropTarget(this, new ObjectTreeDropListener(this));

		// m_dragsource = new DragSource();
		// m_dragsource.createDefaultDragGestureRecognizer( this,
		// DnDConstants.ACTION_LINK, this );
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
	public abstract boolean canDrop(ObjectTreeNode dropNode, Transferable transfer);

	/**
	 * @return true if the given node can be reloaded in the view. For nodes
	 *         such as schema and connection, this is generally true if the node
	 *         is a base node
	 */
	public abstract boolean canReload(ObjectTreeNode onode);

	/**
	 * Creates a object that can handle simultaneaously transfering objects of
	 * different types Override if support drag and drop.
	 */
	public abstract Transferable createTransferable();

	/**
	 * Drops the given transferable onto the tree's given node
	 */
	public abstract void drop(ObjectTreeNode parentnode, Transferable mt);

	public abstract boolean nodeAllowsFolders(ObjectTreeNode node);

	/**
	 * Expands a given node.
	 * 
	 * @param parentNode
	 *            the node to expand
	 * @param bRecursive
	 *            set to true if you want to expand all descendent nodes as well
	 */
	public void expandNode(ObjectTreeNode parentNode, boolean bRecursive) {
		if (parentNode != null) {
			expandPath(new TreePath(parentNode.getPath()));
			if (bRecursive) {
				for (Enumeration e = parentNode.children(); e.hasMoreElements();) {
					ObjectTreeNode childnode = (ObjectTreeNode) e.nextElement();
					expandNode(childnode, bRecursive);
				}
			}
		}
	}

	/**
	 * Iterates through the parent node children looking for the correct place
	 * to insert the tree folder. Folders appear before all other elements and
	 * are in alphabetical order
	 */
	public int getFolderInsertIndex(ObjectTreeNode parentnode, TreeFolder treefolder) {
		int index = 0;
		for (index = 0; index < parentnode.getChildCount(); index++) {
			ObjectTreeNode child = (ObjectTreeNode) parentnode.getChildAt(index);
			Object userobj = child.getUserObject();
			if (userobj instanceof TreeFolder) {
				TreeFolder folder = (TreeFolder) userobj;
				String dropname = treefolder.getName();
				if (dropname != null && dropname.compareTo(folder.getName()) < 0) {
					break;
				}
			} else {
				index--;
				break;
			}
		}

		if (index < 0)
			index = 0;

		if (index > parentnode.getChildCount())
			index = parentnode.getChildCount();

		return index;
	}

	/**
	 * @return the catalog that is the ancestor of the given node.
	 */
	public Catalog getCatalog(ObjectTreeNode dropNode) {
		ObjectTreeModel model = (ObjectTreeModel) getModel();
		return model.getCatalog(dropNode);
	}

	/**
	 * @return the connection that is the ancestor of the given node.
	 */
	public TSConnection getConnection(ObjectTreeNode node) {
		ObjectTreeModel model = (ObjectTreeModel) getModel();
		return model.getConnection(node);
	}

	/**
	 * @return the schema that is the ancestor of the given node. Null is
	 *         returned if this tree does not display schema nodes
	 */
	public Schema getSchema(ObjectTreeNode dropNode) {
		ObjectTreeModel model = (ObjectTreeModel) getModel();
		return model.getSchema(dropNode);
	}

	/**
	 * @return the selected tree node. Null is returned if no node is selected
	 */
	public ObjectTreeNode getSelectedNode() {
		ObjectTreeNode result = null;
		TreePath path = getSelectionPath();
		if (path != null)
			result = (ObjectTreeNode) path.getLastPathComponent();

		return result;
	}

	/**
	 * @return the transferable object for the tree. This is to support moving
	 *         of nodes within the same tree. We need this because some nodes
	 *         cannot be moved to other nodes and we need to determine exactly
	 *         what we are dragging and where to arrive at a accept/reject drag.
	 */
	MultiTransferable getTransferable() {
		return m_transferable;
	}

	/**
	 * @return true if this tree is currently a drag source
	 */
	public boolean isDragging() {
		return m_dragging;
	}

	/**
	 * Provide our own cell editor so we can prevent editing of node names for
	 * all nodes but Folder nodes
	 */
	public boolean isPathEditable(TreePath path) {
		boolean bresult = false;
		ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
		if (node != null) {
			Object obj = node.getUserObject();
			bresult = (obj instanceof TreeFolder);
		}
		return bresult;
	}

	/**
	 * Sets the flag that indicates if we are dragging from this tree
	 */
	protected void setDragging(boolean bdrag) {
		m_dragging = bdrag;
	}

	/**
	 * Sets the move flag. This is used when cutting(moving) a tree node to
	 * another location in the tree. When the user does the cut operation, we
	 * want to update the nodes (set text to gray color) so the user has some
	 * feedback that the nodes are set to move. This method also allows us to
	 * set the node status back to the default value after a move.
	 */
	static void setMoveFlag(MultiTransferable mt, boolean bmove) {
		try {
			Object[] paths = (Object[]) mt.getTransferData(DbObjectFlavor.TREE_PATH);
			if (paths != null) {
				for (int index = 0; index < paths.length; index++) {
					TreePath path = (TreePath) paths[index];
					ObjectTreeNode movenode = (ObjectTreeNode) path.getLastPathComponent();
					movenode.setMoveFlag(bmove);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the transferable object for the tree. This is to support moving of
	 * nodes within the same tree. We need this because some nodes cannot be
	 * moved to other nodes and we need to determine exactly what we are
	 * dragging and where to arrive at a accept/reject drag.
	 */
	protected void setTransferable(MultiTransferable mt) {
		m_transferable = mt;
	}

	/**
	 * Override for look and feel changes
	 */
	public void updateUI() {
		super.updateUI();
		ObjectTreeModel model = (ObjectTreeModel) getModel();

		Object obj = getCellRenderer();
		if (obj instanceof ObjectTreeRenderer) {

			ObjectTreeRenderer otr = (ObjectTreeRenderer) obj;
			otr.updateUI();
			otr.initialize();
		}

	}

}
