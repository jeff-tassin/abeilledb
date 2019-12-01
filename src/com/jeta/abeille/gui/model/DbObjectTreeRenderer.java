package com.jeta.abeille.gui.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.common.DbGuiUtils;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the renderer for objects in the TableTreeView
 * 
 * @author Jeff Tassin
 */
public class DbObjectTreeRenderer extends ObjectTreeRenderer {

	/**
	 * ctor
	 */
	public DbObjectTreeRenderer() {

	}

	DbObjectClassModel getClassModel(JTree tree, ObjectTreeNode node) {
		DbObjectTreeModel model = (DbObjectTreeModel) tree.getModel();
		ObjectTreeNode classnode = model.getClassNode(node);
		if (classnode != null) {
			DbObjectClassModel cmodel = (DbObjectClassModel) classnode.getUserObject();
			return cmodel;
		} else
			return null;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		ObjectTreeNode node = (ObjectTreeNode) value;
		Object object = node.getUserObject();

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		DbObjectClassModel cmodel = getClassModel(tree, node);
		if (cmodel != null) {
			String class_key = cmodel.getClassKey();
			DbObjectTree dbtree = (DbObjectTree) tree;
			DbObjectClassTree classtree = dbtree.getClassTree(class_key);
			DbObjectClassRenderer renderer = classtree.getCellRenderer();
			assert (renderer != null);
			if (renderer != null)
				renderer.renderNode(this, cmodel.getConnection(), tree, value, sel, expanded, leaf, row, hasFocus);
		}
		return this;
	}
}
