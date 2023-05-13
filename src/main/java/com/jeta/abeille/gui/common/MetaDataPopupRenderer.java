package com.jeta.abeille.gui.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import java.util.HashMap;

import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.User;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This renderer is used to render database objects such as schemas, tables, and
 * columns in a JList. It shows a icon and text in the list corresponding to the
 * type of object. It also supports two display modes: STANDARD and LONG. In the
 * standard mode, just the name of the object is displayed. In the LONG mode,
 * the name of the object *plus* the object's owner is displayed. For example,
 * if the object is a column, then the column.tablename is displayed.
 * 
 * @author Jeff Tassin
 */
public class MetaDataPopupRenderer extends JLabel implements ListCellRenderer {
	/** the database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	protected MetaDataPopupRenderer(TSConnection conn) {
		m_connection = conn;
		setOpaque(true);
		setVerticalAlignment(CENTER);
	}

	/**
	 * Creates a renderer for the given connection
	 */
	public static MetaDataPopupRenderer createInstance(TSConnection conn) {
		return createRenderer(conn);
	}

	/**
	 * Creates a renderer for the given connection
	 */
	public static MetaDataPopupRenderer createRenderer(TSConnection conn) {
		return new MetaDataPopupRenderer(conn);
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * ListCellRenderer implementation.
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		setFont(list.getFont());
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		// @TODO cache icons
		if (value instanceof TableId) {
			TableId id = (TableId) value;
			setText(id.getTableName());
			setIcon(DbUtils.TABLE_ICON);
		} else if (value instanceof Catalog) {
			Catalog cat = (Catalog) value;
			setText(cat.getName());
			setIcon(DbUtils.CATALOG_ICON);
		} else if (value instanceof TableMetaData) {
			TableMetaData tmd = (TableMetaData) value;
			setText(tmd.getTableName());
			setIcon(DbUtils.TABLE_ICON);
		} else if (value instanceof ColumnMetaData) {
			ColumnMetaData cmd = (ColumnMetaData) value;
			if (cmd.isAutoIncrement()) {
				setIcon(DbUtils.AUTO_INCREMENT_ICON);
			} else {
				DataTypeInfo typeinfo = DbUtils.getDataTypeInfo(m_connection, cmd.getTypeName(), false);
				if (typeinfo == null) {
					int datatype = cmd.getType();
					setIcon(DbUtils.getIcon(datatype));
				} else {
					setIcon(typeinfo.getIcon());
				}
			}
			setText(cmd.getColumnName());
		} else if (value instanceof DataTypeInfo) {
			DataTypeInfo info = (DataTypeInfo) value;
			setText(info.getTypeName());
			setIcon(info.getIcon());
		} else if (value instanceof Schema) {
			setText(value.toString());
			setIcon(DbUtils.SCHEMA_ICON);
		} else if (value instanceof StoredProcedure) {
			setText(value.toString());
			setIcon(DbUtils.PROCEDURE_ICON);
		} else if (value instanceof User) {
			setText(value.toString());
			setIcon(DbUtils.USER_ICON);
		} else if (value instanceof Group) {
			setText(value.toString());
			setIcon(DbUtils.GROUP_ICON);
		} else {
			if (value == null) {
				if (TSUtils.isDebug()) {
					setText("null");
				} else {
					setText("");
				}
			} else
				setText(value.toString());
		}
		return this;
	}

}
