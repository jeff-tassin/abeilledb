package com.jeta.abeille.gui.model;

import javax.swing.ImageIcon;
import javax.swing.JTree;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.common.DbGuiUtils;

import com.jeta.abeille.gui.views.ViewTreeModel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the renderer for objects in the TableTreeView
 * 
 * @author Jeff Tassin
 */
public class TableTreeRenderer implements DbObjectClassRenderer {
	private static ImageIcon m_tableIcon;
	private static ImageIcon m_schemaIcon;
	private static ImageIcon m_classIcon;
	private static ImageIcon m_viewsIcon;

	static {
		m_classIcon = TSGuiToolbox.loadImage("incors/16x16/table_sql.png");
		m_viewsIcon = TSGuiToolbox.loadImage("incors/16x16/tables.png");
		m_tableIcon = TSGuiToolbox.loadImage("incors/16x16/table_sql.png");
		m_schemaIcon = TSGuiToolbox.loadImage("incors/16x16/folder_cubes.png");
	}

	public void renderNode(ObjectTreeRenderer renderer, TSConnection conn, JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		ObjectTreeNode node = (ObjectTreeNode) value;
		Object object = node.getUserObject();

		if (object instanceof TableId) {
			TableId id = (TableId) object;
			if (TSUtils.isTest()) {
				renderer.setText(id.getFullyQualifiedName());
			} else {
				renderer.setText(id.getTableName());
			}
			renderer.setIcon(m_tableIcon);
		} else if (object instanceof ColumnMetaData) {
			ColumnMetaData cd = (ColumnMetaData) object;
			renderer.setIcon(DbGuiUtils.getIcon(cd, conn));
			renderer.setText(cd.getColumnName());
		} else if (object instanceof DbObjectClassModel) {
			if (object instanceof SynonymTreeModel) {
				renderer.setText("Synonyms");
				renderer.setIcon(m_classIcon);
			} else if (object instanceof TableTreeModel) {
				renderer.setText("Tables");
				renderer.setIcon(m_classIcon);
			} else if (object instanceof ViewTreeModel) {
				renderer.setText("Views");
				renderer.setIcon(m_viewsIcon);
			}
		}
	}
}
