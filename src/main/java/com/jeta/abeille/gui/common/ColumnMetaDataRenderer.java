package com.jeta.abeille.gui.common;

import java.awt.Color;
import java.awt.Component;

import java.util.HashMap;

import javax.swing.JList;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

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
public class ColumnMetaDataRenderer extends MetaDataPopupRenderer {
	/** the parent table for the columns in this renderer */
	private TableMetaData m_tmd;

	private boolean m_show_icon = true;
	private boolean m_show_type = true;

	private Color m_primary_key_color;
	private Color m_foreign_key_color;

	/**
	 * ctor
	 */
	private ColumnMetaDataRenderer(TSConnection conn, TableMetaData tmd) {
		super(conn);
		m_tmd = tmd;
	}

	/**
	 * Creator
	 */
	public static ColumnMetaDataRenderer createInstance(TSConnection conn, TableMetaData tmd) {
		return new ColumnMetaDataRenderer(conn, tmd);
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

		if (value instanceof ColumnMetaData) {
			ColumnMetaData cmd = (ColumnMetaData) value;
			DataTypeInfo typeinfo = DbUtils.getDataTypeInfo(getConnection(), cmd.getTypeName(), false);
			if (isShowIcon()) {
				setIcon(DbGuiUtils.getIcon(cmd, getConnection()));
			} else {
				setIcon(null);
			}

			if (isShowType()) {
				setText(cmd.getColumnSignature(typeinfo));
			} else {
				setText(cmd.getColumnName());
			}

			if (m_tmd != null) {
				boolean bprimary = m_tmd.isPrimaryKey(cmd.getColumnName());
				boolean bforeign = m_tmd.isForeignKey(cmd.getColumnName());
				if (!isSelected) {
					if (bprimary) {
						if (m_primary_key_color == null) {
							setForeground(Color.blue);
						} else {
							setForeground(m_primary_key_color);
						}
					} else if (bforeign) {
						if (m_foreign_key_color == null) {
							setForeground(Color.red);
						} else {
							setForeground(m_foreign_key_color);
						}
					}
				}
			}
		}
		return this;
	}

	/**
	 * @return the flag that indicates if the column type icon should be
	 *         displayed
	 */
	public boolean isShowIcon() {
		return m_show_icon;
	}

	/**
	 * Sets the flag that indicates if the column type should be displayed
	 */
	public boolean isShowType() {
		return m_show_type;
	}

	public void setPrimaryKeyColor(Color c) {
		m_primary_key_color = c;
	}

	public void setForeignKeyColor(Color c) {
		m_foreign_key_color = c;
	}

	/**
	 * Sets the flag that indicates if the column type icon should be displayed
	 */
	public void setShowIcon(boolean showicon) {
		m_show_icon = showicon;
	}

	/**
	 * Sets the flag that indicates if the column type should be displayed
	 */
	public void setShowType(boolean showtype) {
		m_show_type = showtype;
	}
}
