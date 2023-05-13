package com.jeta.abeille.gui.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the renderer for objects in the ObjectTreeView
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeRenderer extends JLabel implements TreeCellRenderer {
	protected static DefaultTreeCellRenderer m_defaultrenderer;
	public static ImageIcon m_schemaIcon;
	public static ImageIcon m_servericon;
	public static ImageIcon m_catalogicon;
	/**
	 * the user should never see this icon, but we render it anyway to faciliate
	 * QA
	 */
	public static ImageIcon m_emptynodeicon;

	/** the database connection */
	private TSConnection m_connection;

	static {
		m_schemaIcon = TSGuiToolbox.loadImage("incors/16x16/folder_cubes.png");
		m_servericon = TSGuiToolbox.loadImage("incors/16x16/server.png");
		m_catalogicon = TSGuiToolbox.loadImage("incors/16x16/data_gear.png");
		m_emptynodeicon = TSGuiToolbox.loadImage("dtype_general16.gif");
	}

	/**
	 * ctor
	 */
	public ObjectTreeRenderer() {
		super("");
		initialize();
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		ObjectTreeNode node = (ObjectTreeNode) value;
		Object object = node.getUserObject();

		if (sel) {
			setForeground(UIManager.getColor("Tree.selectionForeground"));
			setBackground(UIManager.getColor("Tree.selectionBackground"));
			setOpaque(true);
		} else {
			setBackground(UIManager.getColor("Tree.background"));
			setForeground(UIManager.getColor("Tree.foreground"));
			setOpaque(false);
		}

		if (node.getMoveFlag()) {
			setForeground(Color.gray);
		}

		if (node instanceof EmptyTreeNode) {
			setIcon(m_emptynodeicon);
			setText("");
		} else if (object instanceof TreeFolder) {
			TreeFolder folder = (TreeFolder) object;
			if (expanded)
				setIcon(m_defaultrenderer.getDefaultOpenIcon());
			else
				setIcon(m_defaultrenderer.getClosedIcon());

			setText(folder.toString());
		} else if (object instanceof Schema) {
			Schema s = (Schema) object;
			setText(s.getName());
			setIcon(m_schemaIcon);
		} else if (object instanceof TSConnection) {
			TSConnection conn = (TSConnection) object;
			setText(conn.getDescription());
			setIcon(m_servericon);
		} else if (object instanceof CatalogWrapper) {
			CatalogWrapper wrapper = (CatalogWrapper) object;
			setText(wrapper.getDisplayName());
			setIcon(m_catalogicon);
		} else if (object instanceof SchemaWrapper) {
			SchemaWrapper wrapper = (SchemaWrapper) object;
			setText(wrapper.getDisplayName());
			setIcon(m_schemaIcon);
		} else if (node == tree.getModel().getRoot()) {
			setIcon(m_defaultrenderer.getClosedIcon());
		}

		return this;
	}

	public void initialize() {
		m_defaultrenderer = new DefaultTreeCellRenderer();

		setLayout(new FlowLayout());
		setFont(UIManager.getFont("Tree.font"));
	}

	public java.awt.Font getFont() {
		return UIManager.getFont("Tree.font");
	}

}
