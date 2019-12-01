package com.jeta.abeille.gui.model;

import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSComponentNames;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is responsible for updating (enable/disable) GUI controls on the
 * ObjectTreeView.
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeUIDirector implements UIDirector {
	/** the view we are updating */
	private ObjectTreeView m_view;

	/**
	 * ctor
	 */
	public ObjectTreeUIDirector(ObjectTreeView view) {
		m_view = view;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		boolean new_folder = false;
		boolean folder_selected = false;
		boolean can_copy = true;
		boolean can_cut = true;

		ObjectTree otree = m_view.getTree();

		ObjectTreeNode node = null;

		TreePath[] selections = otree.getSelectionPaths();
		if (selections == null || selections.length == 0) {
			node = m_view.getModel().getRootNode();
			can_cut = false;
			can_copy = false;
		} else if (selections != null && selections.length == 1) {
			TreePath path = selections[0];
			node = (ObjectTreeNode) path.getLastPathComponent();
			Object userobj = node.getUserObject();
			if (userobj instanceof TreeFolder)
				folder_selected = true;

			can_cut = canCut(node);
		} else if (selections != null && selections.length > 1) {
			for (int index = 0; index < selections.length; index++) {
				TreePath path = selections[index];
				node = (ObjectTreeNode) path.getLastPathComponent();
				Object userobj = node.getUserObject();
				can_cut = canCut(node);
				if (can_cut == false)
					break;
			}
		}

		if (node != null && otree.nodeAllowsFolders(node))
			new_folder = true;

		m_view.enableComponent(ObjectTreeNames.ID_NEW_FOLDER, new_folder);

		m_view.enableComponent(ObjectTreeNames.ID_RENAME_FOLDER, folder_selected);
		m_view.enableComponent(ObjectTreeNames.ID_REMOVE_FOLDER, folder_selected);

		m_view.enableComponent(TSComponentNames.ID_CUT, can_cut);
		m_view.enableComponent(TSComponentNames.ID_COPY, can_copy);
		m_view.enableComponent(ObjectTreeNames.ID_RELOAD, otree.canReload(node));

		boolean paste = false;
		// now check if we can paste
		TreePath path = otree.getSelectionPath();
		if (path != null) {
			ObjectTreeNode selnode = (ObjectTreeNode) path.getLastPathComponent();
			if (selnode != null) {
				Toolkit kit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = kit.getSystemClipboard();
				Transferable transferable = (Transferable) clipboard.getContents(null);
				if (otree.canDrop(selnode, transferable)) {
					paste = true;
				}
			}
		}

		m_view.enableComponent(TSComponentNames.ID_PASTE, paste);
	}

	private boolean canCut(ObjectTreeNode onode) {
		if (onode == null)
			return false;

		Object userobj = onode.getUserObject();
		return !(userobj instanceof DbObjectClassModel || userobj instanceof TSConnection
				|| userobj instanceof CatalogWrapper || userobj instanceof SchemaWrapper);
	}
}
