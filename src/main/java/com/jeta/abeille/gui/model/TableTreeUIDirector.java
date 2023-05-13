package com.jeta.abeille.gui.model;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is responsible for updating (enable/disable) GUI controls on the
 * TableTreeView.
 * 
 * @author Jeff Tassin
 */
public class TableTreeUIDirector implements UIDirector {
	private DbObjectClassTree m_tree;

	/**
	 * ctor
	 */
	public TableTreeUIDirector(DbObjectClassTree view) {
		m_tree = view;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {

		boolean seltable = false;
		boolean reload = false;

		ObjectTreeNode node = m_tree.getTree().getSelectedNode();
		if (node != null) {
			Object userobj = node.getUserObject();
			if (userobj instanceof CatalogWrapper) {
				reload = true;
			} else if (userobj instanceof TableId) {
				seltable = true;
				reload = true;
			}
		}

		m_tree.enableComponent(TableTreeNames.ID_TABLE_PROPERTIES, seltable);
		m_tree.enableComponent(TableTreeNames.ID_INSTANCE_VIEW, seltable);
		m_tree.enableComponent(TableTreeNames.ID_QUERY_TABLE, seltable);
	}
}
