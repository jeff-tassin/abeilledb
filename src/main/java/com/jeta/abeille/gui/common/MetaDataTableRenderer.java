package com.jeta.abeille.gui.common;

import java.awt.Component;

import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.User;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class is used to render database objects in a table cell
 * 
 * @author Jeff Tassin
 */
public class MetaDataTableRenderer extends JLabel implements TableCellRenderer {

	/** the database connection */
	private TSConnection m_connection;

	/** allows you to override the given icon */
	private ImageIcon m_icon;

	private MetaDataTableRenderer(TSConnection conn) {
		m_connection = conn;
		// must set or the background color won't show
		setOpaque(true);
		setFont(UIManager.getFont("Table.font"));
	}

	private MetaDataTableRenderer(ImageIcon icon) {
		this((TSConnection) null);
		m_icon = icon;
	}

	/**
	 * Creates a renderer for the given connection
	 */
	public static MetaDataTableRenderer createInstance(TSConnection conn) {
		return createRenderer(conn);
	}

	/**
	 * Creates a renderer for the given connection
	 */
	public static MetaDataTableRenderer createRenderer(TSConnection conn) {
		return new MetaDataTableRenderer(conn);
	}

	/**
	 * Allows you to create a renderer with a custom icon
	 */
	public static MetaDataTableRenderer createRenderer(ImageIcon icon) {
		return new MetaDataTableRenderer(icon);
	}

	public java.awt.Font getFont() {
		return javax.swing.UIManager.getFont("Table.font");
	}

	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int column) {

		if (aValue instanceof ColumnMetaData) {
			ColumnMetaData cmd = (ColumnMetaData) aValue;
			setText(cmd.getColumnName());

			DataTypeInfo typeinfo = DbUtils.getDataTypeInfo(m_connection, cmd.getTypeName(), false);
			if (typeinfo == null) {
				int datatype = cmd.getType();
				setIcon(DbUtils.getIcon(datatype));
			} else {
				setIcon(typeinfo.getIcon());
			}
		} else if (aValue instanceof TableMetaData) {
			TableMetaData tmd = (TableMetaData) aValue;
			setText(tmd.getTableName());
			setIcon(DbUtils.TABLE_ICON);
		} else if (aValue instanceof TableId) {
			TableId id = (TableId) aValue;
			setText(id.getTableName());
			setIcon(DbUtils.TABLE_ICON);
		} else if (aValue instanceof User) {
			setText(aValue.toString());
			setIcon(DbUtils.USER_ICON);
		} else if (aValue instanceof Group) {
			setText(aValue.toString());
			setIcon(DbUtils.GROUP_ICON);
		} else {
			setIcon(m_icon);
			if (aValue == null)
				setText("");
			else
				setText(aValue.toString());
		}

		if (bSelected) {
			setBackground(UIManager.getColor("Table.selectionBackground"));
			setForeground(UIManager.getColor("Table.selectionForeground"));
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}

		return this;
	}
}
