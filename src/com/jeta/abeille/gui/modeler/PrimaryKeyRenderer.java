package com.jeta.abeille.gui.modeler;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This renderer displays a primary key in a table cell
 * 
 * @author Jeff Tassin
 */
public class PrimaryKeyRenderer extends JLabel implements TableCellRenderer {
	private static ImageIcon m_pkheaderimage = TSGuiToolbox.loadImage("primarykeyheader16.gif");
	private static ImageIcon m_pkimage = TSGuiToolbox.loadImage("primarykey16.gif");

	/**
	 * flag that indicates if this renderer is for a table header or a standard
	 * table cell
	 */
	private boolean m_header;

	public PrimaryKeyRenderer(boolean bheader) {
		super();
		setOpaque(true);
		m_header = bheader;
		this.setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (m_header) {
			// Try to set default fore- and background colors
			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}

			// set normal border
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setIcon(m_pkheaderimage);
		} else {
			if (value instanceof ColumnInfo) {
				ColumnInfo info = (ColumnInfo) value;
				if (info.isPrimaryKey())
					setIcon(m_pkimage);
				else
					setIcon(null);
			}

			if (isSelected) {
				setBackground(UIManager.getColor("Table.selectionBackground"));
				setForeground(UIManager.getColor("Table.selectionForeground"));
			} else {
				setBackground(UIManager.getColor("Table.background"));
				setForeground(UIManager.getColor("Table.foreground"));
			}
		}
		return this;
	}
}
