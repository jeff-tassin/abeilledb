package com.jeta.abeille.gui.formbuilder;

import javax.swing.ImageIcon;
import javax.swing.JTree;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.DbObjectClassRenderer;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.ObjectTreeRenderer;
import com.jeta.abeille.gui.model.DbObjectClassModel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This is the renderer for objects in the FormTreeView
 * 
 * @author Jeff Tassin
 */
public class FormTreeRenderer implements DbObjectClassRenderer {
	private static ImageIcon m_icon;
	private static ImageIcon m_classicon;

	static {
		m_icon = TSGuiToolbox.loadImage("incors/16x16/form_blue.png");
		m_classicon = TSGuiToolbox.loadImage("incors/16x16/form_green.png");
	}

	public void renderNode(ObjectTreeRenderer renderer, TSConnection conn, JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		ObjectTreeNode node = (ObjectTreeNode) value;
		Object object = node.getUserObject();
		if (object instanceof FormProxy) {
			FormProxy proxy = (FormProxy) object;
			renderer.setText(proxy.getName());
			renderer.setIcon(m_icon);
		} else if (object instanceof DbObjectClassModel) {
			renderer.setText("Forms");
			renderer.setIcon(m_classicon);
		}

	}
}
