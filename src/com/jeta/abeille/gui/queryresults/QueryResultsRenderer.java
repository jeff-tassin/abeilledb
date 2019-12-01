package com.jeta.abeille.gui.queryresults;

import java.awt.Color;
import java.awt.Component;

import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the main cell renderer for a SQL query results table
 * 
 * @author Jeff Tassin
 */
public class QueryResultsRenderer implements TableCellRenderer {
	/** this is the default Java renderers that we delegate rendereing to */
	private TableCellRenderer m_delegate;

	/**
	 * we need the results view in order to determine if a given row has been
	 * deleted
	 */
	private WeakReference m_viewref;

	/** object that handles the foreground and background colors */
	private RendererHelper m_helper = new RendererHelper();

	/**
	 * ctor
	 */
	public QueryResultsRenderer(QueryResultsView view, TableCellRenderer delegate) {
		m_viewref = new WeakReference(view);
		m_delegate = delegate;
	}

	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int column) {
		QueryResultsView view = (QueryResultsView) m_viewref.get();
		if (view != null) {
			Component comp = m_delegate.getTableCellRendererComponent(table, aValue, bSelected, bFocus, row, column);
			if (aValue instanceof java.sql.Clob) {
				if (comp instanceof JLabel) {
					((JLabel) comp).setText(renderLOB(aValue));
				}
			} else if (aValue instanceof java.sql.Blob) {
				if (comp instanceof JLabel) {
					((JLabel) comp).setText(renderLOB(aValue));
				}
			} else if (aValue instanceof byte[]) {
				if (comp instanceof JLabel) {
					((JLabel) comp).setText(renderLOB(aValue));
				}
			}
			m_helper.renderComponentColors(comp, view, table, aValue, bSelected, row, column);
			return comp;
		}
		return null;
	}

	public static String renderLOB(Object aValue) {
		try {
			if (aValue instanceof java.sql.Clob) {
				StringBuffer sbuff = new StringBuffer();
				java.sql.Clob clob = (java.sql.Clob) aValue;
				sbuff.append(clob.toString());
				sbuff.append("(");
				sbuff.append(I18N.format("characters_1", String.valueOf(clob.length())));
				sbuff.append(")");
				return sbuff.toString();
			} else if (aValue instanceof java.sql.Blob) {
				StringBuffer sbuff = new StringBuffer();
				java.sql.Blob blob = (java.sql.Blob) aValue;
				sbuff.append(blob.toString());
				sbuff.append("(");
				sbuff.append(I18N.format("bytes_1", String.valueOf(blob.length())));
				sbuff.append(")");
				return sbuff.toString();
			} else if (aValue instanceof byte[]) {
				StringBuffer sbuff = new StringBuffer();
				sbuff.append("byte[] ");
				sbuff.append("(");
				sbuff.append(I18N.format("bytes_1", String.valueOf(((byte[]) aValue).length)));
				sbuff.append(")");
				return sbuff.toString();
			}
			return "";
		} catch (java.sql.SQLException e) {
			return "#ERROR#";
		}
	}

}
