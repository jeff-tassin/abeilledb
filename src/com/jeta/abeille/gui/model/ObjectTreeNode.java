package com.jeta.abeille.gui.model;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class represents a TreeNode use in the ObjectTree/Models. We override to
 * provide efficient processing of the node children. Specifically, when sorting
 * the children, we need quick access to the children as an array.
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeNode extends DefaultMutableTreeNode implements Comparable {
	static final long serialVersionUID = -2375134349299142328L;

	/**
	 * flag that indicates if this node has been loaded. This is used in
	 * situations like database table nodes. We load the table node with the
	 * table columns only when the user clicks to expand the node
	 */
	private boolean m_loaded = false;

	/**
	 * Sets a flag that indicates this node is in the process of being
	 * cut/pasted
	 */
	private transient boolean m_moveflag;

	/**
	 * ctor
	 */
	public ObjectTreeNode() {
		m_moveflag = false;
	}

	/**
	 * ctor
	 */
	public ObjectTreeNode(Object userobj) {
		super(userobj);
	}

	/**
	 * Comparable interface. For sorting in the tree
	 */
	public int compareTo(Object obj) {
		if (obj instanceof ObjectTreeNode) {
			ObjectTreeNode node = (ObjectTreeNode) obj;

			Object nodeuserobj = node.getUserObject();
			Object myuserobj = getUserObject();

			if (myuserobj == null)
				return -1;
			else if (myuserobj instanceof TreeFolder) {
				if (nodeuserobj instanceof TreeFolder) {
					TreeFolder folder = (TreeFolder) myuserobj;
					return folder.compareTo(nodeuserobj);
				} else
					return -1;
			} else if (myuserobj instanceof Comparable) {
				Comparable mycomp = (Comparable) myuserobj;
				return mycomp.compareTo(nodeuserobj);
			} else
				return -1;
		} else
			return -1;
	}

	public boolean getMoveFlag() {
		return m_moveflag;
	}

	/**
	 * @return the flag that indicates if this node has been loaded. This is
	 *         used in situations like database table nodes. We load the table
	 *         node with the table columns only when the user clicks to expand
	 *         the node
	 */
	public boolean isLoaded() {
		return m_loaded;
	}

	/**
	 * Sets the flag that indicates if this node has been loaded. This is used
	 * in situations like database table nodes. We load the table node with the
	 * table columns only when the user clicks to expand the node
	 */
	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}

	public void setMoveFlag(boolean move) {
		m_moveflag = move;
	}

	/**
	 * Sorts the children of this node to their natural ordering
	 */
	public void sortChildren() {
		if (children != null) {
			java.util.Collections.sort(children);

			/*
			 * System.out.println( "sorted node: " ); java.util.Iterator iter =
			 * children.iterator(); while( iter.hasNext() ) { ObjectTreeNode
			 * node = (ObjectTreeNode)iter.next(); System.out.println( " ... " +
			 * node.getUserObject() ); }
			 */
		}
	}

}
