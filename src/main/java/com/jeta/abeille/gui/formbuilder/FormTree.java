package com.jeta.abeille.gui.formbuilder;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;

import javax.swing.tree.TreePath;

import com.jeta.abeille.gui.model.DbObjectFlavor;
import com.jeta.abeille.gui.model.DbObjectTransfer;
import com.jeta.abeille.gui.model.MultiTransferable;
import com.jeta.abeille.gui.model.ObjectTree;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.TreeFolder;

import com.jeta.abeille.gui.model.common.ProxyTree;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class shows user built forms in a tree view
 * 
 * @author Jeff Tassin
 */
public class FormTree extends ProxyTree {
	/**
	 * ctor
	 */
	public FormTree(ObjectTree tree) {
		super(tree);
		setCellRenderer(new FormTreeRenderer());
		// setName( FormTree.class.getName() + TSUtils.createUID() );
		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("New Form"), FormNames.ID_NEW_FORM,
				null));
		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Edit Form"), FormNames.ID_EDIT_FORM,
				null));
		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Show Form"), FormNames.ID_SHOW_FORM,
				null));
		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Rename Form"),
				FormNames.ID_RENAME_FORM, null));
		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Delete Form"),
				FormNames.ID_DELETE_FORM, null));
		setController(new FormTreeController(this));
		setUIDirector(new FormTreeUIDirector(this));
	}

	/**
	 * Creates a object that can handle simultaneaously transfering objects of
	 * different types Override if support drag and drop.
	 */
	public void createTransferable(MultiTransferable mt, TreePath path) {
		ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
		Object userobj = node.getUserObject();
		if (userobj instanceof FormProxy) {
			FormProxy proxy = (FormProxy) userobj;
			mt.addData(FormFlavor.FORM, proxy.getModel());
			DbObjectTransfer.addTransferable(mt, path);
		} else if (userobj instanceof TreeFolder) {
			mt.addData(DbObjectFlavor.TREE_FOLDER, userobj);
			DbObjectTransfer.addTransferable(mt, path);
		}
	}

	public FormProxy getSelectedForm() {
		ObjectTreeNode node = getSelectedNode();
		if (node != null) {
			Object obj = node.getUserObject();
			if (obj instanceof FormProxy)
				return (FormProxy) obj;
		}
		return null;
	}

	/**
	 * @return true if the transferable has flavors that are supported by this
	 *         QueryTree
	 */
	public boolean isDataFlavorSupported(Transferable transfer) {
		return (transfer.isDataFlavorSupported(FormFlavor.FORM) || transfer
				.isDataFlavorSupported(DbObjectFlavor.TREE_FOLDER));
	}

}
