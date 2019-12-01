package com.jeta.abeille.gui.procedures;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Schema;

import com.jeta.abeille.database.procedures.ProcedureLanguage;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.abeille.gui.model.DbObjectClassTree;
import com.jeta.abeille.gui.model.DbObjectFlavor;
import com.jeta.abeille.gui.model.DbObjectTransfer;
import com.jeta.abeille.gui.model.MultiTransferable;
import com.jeta.abeille.gui.model.ObjectTree;
import com.jeta.abeille.gui.model.ObjectTreeNames;
import com.jeta.abeille.gui.model.ObjectTreeModel;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.TreeFolder;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class shows procedures in a tree view
 * 
 * @author Jeff Tassin
 */
public class ProcedureTree extends DbObjectClassTree {
	/**
	 * ctor
	 */
	public ProcedureTree(ObjectTree tree) {
		super(tree);
		setCellRenderer(new ProcedureTreeRenderer());
		// setName( ProcedureTree.class.getName() + TSUtils.createUID() );
		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("View Procedure"),
				ProcedureNames.ID_VIEW_PROCEDURE, null));
		setController(new ProcedureTreeController(this));
		setUIDirector(new ProcedureTreeUIDirector(this));

	}

	/**
	 * Iterates up the node hierarchy looking for a ProcedureLanguage node.
	 * 
	 * @return the procedure language node for the given descedent node. If the
	 *         node is not found, null is returned.
	 */
	protected ObjectTreeNode getLanguageNode(ObjectTreeNode node) {
		if (node == null)
			return null;

		ObjectTreeNode result = null;
		ObjectTreeNode parent = node;
		while (parent != null) {
			Object userobj = parent.getUserObject();
			if (userobj instanceof ProcedureLanguage) {
				result = parent;
				break;
			}
			parent = (ObjectTreeNode) parent.getParent();
		}
		return result;
	}

	/**
	 * @return the selected procedure. If a procedure is not selected, null is
	 *         returned
	 */
	public StoredProcedure getSelectedProcedure() {
		ObjectTreeNode node = getSelectedNode();
		Object obj = node.getUserObject();
		if (obj instanceof StoredProcedure)
			return (StoredProcedure) obj;
		else
			return null;

	}

}
