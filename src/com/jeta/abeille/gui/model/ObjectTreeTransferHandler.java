package com.jeta.abeille.gui.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Base transfer handler for object tree views
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeTransferHandler extends TransferHandler {
	private ObjectTree m_tree;

	public ObjectTreeTransferHandler(ObjectTree tree) {
		m_tree = tree;
	}

	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		return false;
	}

	/**
    *
    */
	protected Transferable createTransferable(JComponent comp) {
		if (comp instanceof ObjectTree) {
			ObjectTree otree = (ObjectTree) comp;
			// we have to to this to support a more sophisticated drag-drop in
			// the tree
			MultiTransferable mt = (MultiTransferable) otree.createTransferable();
			m_tree.setTransferable(mt);
			return mt;
		}
		return null;
	}

	/**
	 * Start of a drag operation
	 */
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		// clear any object nodes that may have cut(move) status still set
		m_tree.setDragging(true);

		if (comp instanceof ObjectTree) {
			ObjectTree otree = (ObjectTree) comp;
			// we have to to this to support a more sophisticated drag-drop in
			// the tree
			MultiTransferable mt = otree.getTransferable();
			if (mt != null) {
				ObjectTree.setMoveFlag(mt, false);
			}
		}
		super.exportAsDrag(comp, e, action);
	}

	protected void exportDone(JComponent source, Transferable data, int action) {
		super.exportDone(source, data, action);
		// System.out.println( "ObjectTreeTransferHandler.exportDone" );
		m_tree.setDragging(false);
		m_tree.setTransferable(null);
	}

	public int getSourceActions(JComponent comp) {
		return TransferHandler.COPY;
	}

	public boolean importData(JComponent comp, Transferable t) {
		return false;
	}
}
