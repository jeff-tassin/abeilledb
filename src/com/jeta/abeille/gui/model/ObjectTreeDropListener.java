package com.jeta.abeille.gui.model;

import java.awt.Container;
import java.awt.Point;
import java.awt.datatransfer.Transferable;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;

import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.TableMetaData;

import com.jeta.foundation.gui.dnd.DnDSupport;

/**
 * The drop listener interface for the object tree view Basically, this is for
 * moving items within the tree. Currently, we don't allow dragging objects into
 * the tree from other components
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeDropListener implements DropTargetListener {
	/** the tree we are providing drop services for */
	private ObjectTree m_tree;

	/**
	 * ctor
	 * 
	 * @param tree
	 *            the tree we are listening to events
	 */
	public ObjectTreeDropListener(ObjectTree tree) {
		m_tree = tree;

	}

	/**
	 * Invoked when you are dragging over the DropSite
	 * 
	 */
	public void dragEnter(DropTargetDragEvent event) {
		// if ( m_autoscroller == null )
		// m_autoscroller = new ObjectTreeAutoScroller(m_tree);
		event.rejectDrag();
	}

	/**
	 * Invoked when you are exit the DropSite without dropping
	 * 
	 */
	public void dragExit(DropTargetEvent event) {

	}

	/**
	 * Invoked when a drag operation is going on
	 * 
	 */
	public void dragOver(DropTargetDragEvent event) {
		// System.out.println( "drag over" );
		boolean acceptdrag = false;
		// the point is in JTree coordinates, so let's get the tree node
		// the point is over and select it
		Point pt = event.getLocation();

		// TreePath path = m_tree.getClosestPathForLocation( pt.x, pt.y );
		// if ( path == null )
		// {
		// m_tree.clearSelection();
		// }

		// if ( m_autoscroller != null )
		// pt = m_autoscroller.dragOver( event );

		int row = m_tree.getRowForLocation(pt.x, pt.y);
		if (row >= 0) {
			// we only accept drops from ourself
			ObjectTreeModel model = (ObjectTreeModel) m_tree.getModel();
			m_tree.setSelectionRow(row);
			TreePath path = m_tree.getSelectionPath();
			if (path != null) {
				ObjectTreeNode dropnode = (ObjectTreeNode) path.getLastPathComponent();
				if (dropnode != null) {
					MultiTransferable transferable = m_tree.getTransferable();
					if (transferable != null) {
						if (m_tree.canDrop(dropnode, transferable))
							acceptdrag = true;
					}
				} else {
					System.out.println("drop node = null");
				}
			} else {
				System.out.println("selection path = null");
			}
		}

		if (acceptdrag) {
			event.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			event.rejectDrag();
		}

	}

	/**
	 * a drop has occurred
	 */
	public void drop(DropTargetDropEvent event) {
		boolean rejectdrop = true;

		MultiTransferable mt = m_tree.getTransferable();
		if (mt != null) {
			Point pt = event.getLocation();
			ObjectTreeModel model = (ObjectTreeModel) m_tree.getModel();

			ObjectTreeNode parentnode = model.getRootNode();
			TreePath droppath = m_tree.getPathForLocation(pt.x, pt.y);
			if (droppath != null)
				parentnode = (ObjectTreeNode) droppath.getLastPathComponent();

			if (m_tree.canDrop(parentnode, mt)) {
				m_tree.drop(parentnode, mt);
				rejectdrop = false;
				event.acceptDrop(DnDConstants.ACTION_COPY);
				event.dropComplete(true);
			}
		}

		if (rejectdrop)
			event.rejectDrop();

	}

	/**
	 * Invoked if the user modifies the current drop gesture
	 */
	public void dropActionChanged(DropTargetDragEvent event) {
		System.out.println("ObjectTreeDropListener.dropActionChanged");
	}

}
