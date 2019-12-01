package com.jeta.abeille.gui.security;

import java.awt.Color;
import java.awt.Component;

import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the main cell renderer for the Grants table
 * 
 * @author Jeff Tassin
 */
public class GrantsRenderer implements TableCellRenderer {
	/** this is the default Java renderers that we delegate rendereing to */
	private TableCellRenderer m_delegate;

	/**
	 * the weak ref to the GrantsView. This is so this renderer does not keep a
	 * reference to the view
	 */
	private WeakReference m_viewref;

	private Color m_selected;
	private Color m_selectedback;

	private Color m_standard;
	private Color m_standardback;

	/**
	 * ctor
	 */
	public GrantsRenderer(GrantsView view, TableCellRenderer delegate) {
		m_delegate = delegate;
		m_viewref = new WeakReference(view);

		m_selected = UIManager.getColor("Table.selectionForeground");
		m_selectedback = UIManager.getColor("Table.selectionBackground");

		m_standard = UIManager.getColor("Table.foreground");
		m_standardback = UIManager.getColor("Table.background");
	}

	/**
	 * The grants renderer
	 */
	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int column) {
		GrantsView view = (GrantsView) m_viewref.get();
		if (view != null) {
			Component comp = m_delegate.getTableCellRendererComponent(table, aValue, bSelected, bFocus, row, column);
			row = TableUtils.convertTableToModelIndex(view.getGrantsTable(), row);
			GrantDefinitionWrapper wrapper = view.getGrantDefinitionWrapper(row);
			if (wrapper != null && wrapper.isModified()) {
				if (bSelected) {
					comp.setForeground(Color.red);
					comp.setBackground(m_selectedback);
				} else {
					comp.setForeground(Color.red);
					comp.setBackground(m_standardback);
				}
			} else {
				if (bSelected) {
					comp.setForeground(m_selected);
					comp.setBackground(m_selectedback);
				} else {
					comp.setForeground(m_standard);
					comp.setBackground(m_standardback);
				}
			}
			return comp;
		} else {
			return null;
		}
	}
}
