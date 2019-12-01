package com.jeta.abeille.gui.sequences;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.sequences.Sequence;

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
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class shows sequences in a tree view
 * 
 * @author Jeff Tassin
 */
public class SequenceTree extends DbObjectClassTree {
	/**
	 * ctor
	 */
	public SequenceTree(ObjectTree tree) {
		super(tree);
		setCellRenderer(new SequenceTreeRenderer());
		// setName( SequenceTree.class.getName() + TSUtils.createUID() );

		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Create Sequence"),
				SequenceNames.ID_NEW_SEQUENCE, null));

		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Modify Sequence"),
				SequenceNames.ID_EDIT_SEQUENCE, null));

		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Drop Sequence"),
				SequenceNames.ID_DROP_SEQUENCE, null));
		setController(new SequenceTreeViewController(this));

		SequenceTreeUIDirector uidirector = new SequenceTreeUIDirector(this);
		setUIDirector(uidirector);
	}

	/**
	 * @return the selected procedure. If a procedure is not selected, null is
	 *         returned
	 */
	public Sequence getSelectedSequence() {
		ObjectTreeNode node = getSelectedNode();
		if (node != null) {
			Object obj = node.getUserObject();
			if (obj instanceof Sequence)
				return (Sequence) obj;
		}
		return null;
	}

}
