package com.jeta.abeille.gui.model;

import javax.swing.JTree;

import com.jeta.abeille.database.model.TSConnection;

/**
 * DbObjectTree renderer
 * 
 * @author Jeff Tassin
 */
public interface DbObjectClassRenderer {
	public void renderNode(ObjectTreeRenderer renderer, TSConnection conn, JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus);
}
