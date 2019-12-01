package com.jeta.abeille.gui.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.foundation.gui.dnd.DnDSupport;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class shows tables in a tree view
 * 
 * @author Jeff Tassin
 */
public class TableTree extends DbObjectClassTree {

	/**
	 * ctor
	 */
	public TableTree(ObjectTree tree) {
		super(tree);
		setCellRenderer(new TableTreeRenderer());
		// setName( TableTree.class.getName() + TSUtils.createUID() );

		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Table Properties"),
				TableTreeNames.ID_TABLE_PROPERTIES, null));
		addContextMenuItem(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Data Browser"),
				TableTreeNames.ID_INSTANCE_VIEW, null));
		addContextMenuItem(TSGuiToolbox.createMenuItem("select *", TableTreeNames.ID_QUERY_TABLE, null));

		setController(new TableTreeController(this));
		setUIDirector(new TableTreeUIDirector(this));
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
		if (!super.canDrop(dropNode, transfer)) {
			return false;
		}

		try {
			boolean candrop = true;
			// we don't support dropping schema or column meta data here
			if (transfer.isDataFlavorSupported(DbObjectFlavor.COLUMN_METADATA)
					|| transfer.isDataFlavorSupported(DbObjectFlavor.SCHEMA)) {
				candrop = false;
			}

			return candrop;
		} catch (Exception e) {
			if (TSUtils.isDebug()) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @return true if the given node can be reloaded in the view. If the node
	 *         is a base node such as schema or catalog, then we can reload all
	 *         tables. If the node is a table id, then we can reload the table.
	 *         Otherwise, return false;
	 */
	public boolean canReload(ObjectTreeNode onode) {
		if (!super.canReload(onode)) {
			return false;
		} else {
			if (onode != null) {
				return (onode.getUserObject() instanceof TableId);
			} else {
				return false;
			}
		}
	}

	/**
	 * Creates a object that can handle simultaneaously transfering objects of
	 * different types Override if support drag and drop.
	 */
	public void createTransferable(MultiTransferable mt, TreePath path) {
		ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
		Object userobj = node.getUserObject();
		if (userobj instanceof TableId) {
			DbObjectTransfer.addTransferable(mt, getConnection(node), (TableId) userobj);
		} else if (userobj instanceof ColumnMetaData) {
			DbObjectTransfer.addTransferable(mt, (ColumnMetaData) userobj);
		} else if (userobj instanceof TreeFolder) {
			mt.addData(DbObjectFlavor.TREE_FOLDER, userobj);
		} else if (userobj instanceof Schema) {
			DbObjectTransfer.addTransferable(mt, (Schema) userobj);
		} else {
			// this object cannot be dragged
			return;
		}
		DbObjectTransfer.addTransferable(mt, path);
	}

	public TableId getSelectedTable() {
		ObjectTreeNode node = getSelectedNode();
		if (node != null) {
			Object obj = node.getUserObject();
			if (obj instanceof TableId)
				return (TableId) obj;
		}
		return null;
	}

}
