package com.jeta.abeille.gui.procedures;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.procedures.ProcedureLanguage;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.abeille.gui.model.DbObjectClassModel;
import com.jeta.abeille.gui.model.DbObjectClassRenderer;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.ObjectTreeRenderer;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This is the renderer for objects in the ProcedureTreeView
 * 
 * @author Jeff Tassin
 */
public class ProcedureTreeRenderer implements DbObjectClassRenderer {
	private static ImageIcon m_icon;
	private static ImageIcon m_classicon;

	static {
		m_icon = TSGuiToolbox.loadImage("incors/16x16/gear.png");
		m_classicon = TSGuiToolbox.loadImage("incors/16x16/gears.png");
	}

	public void renderNode(ObjectTreeRenderer renderer, TSConnection conn, JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		ObjectTreeNode node = (ObjectTreeNode) value;
		Object object = node.getUserObject();

		if (object instanceof StoredProcedure) {
			StoredProcedure proc = (StoredProcedure) object;
			renderer.setText(proc.getName());
			renderer.setIcon(m_icon);
		} else if (object instanceof ProcedureLanguage) {
			ProcedureLanguage lang = (ProcedureLanguage) object;
			renderer.setText(lang.toString());
		} else if (object instanceof DbObjectClassModel) {
			renderer.setText("Procedures");
			renderer.setIcon(m_classicon);
		}

	}
}
