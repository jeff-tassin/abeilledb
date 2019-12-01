package com.jeta.abeille.gui.queryresults;

import java.awt.Color;

import java.awt.Component;

import java.util.Calendar;
import java.util.Date;

import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.foundation.gui.components.TSDateField;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the date cell renderer for a SQL query results table
 * 
 * @author Jeff Tassin
 */
public class DateRenderer extends TSDateField implements TableCellRenderer {
	private Calendar m_cal;

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
	public DateRenderer(QueryResultsView view) {
		// m_view = view;
		m_viewref = new WeakReference(view);
		setFont(UIManager.getFont("Table.font"));
		m_cal = Calendar.getInstance();
		setBorder(null);
	}

	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int column) {
		QueryResultsView view = (QueryResultsView) m_viewref.get();
		if (aValue instanceof Date) {
			Date dt = (Date) aValue;
			m_cal.setTime(dt);
			setMonth(m_cal.get(Calendar.MONTH));
			setDay(m_cal.get(Calendar.DAY_OF_MONTH));
			setYear(m_cal.get(Calendar.YEAR));
		} else {
			if (aValue == null)
				setNull(true);
			else {
				setMonth(0);
				setDay(0);
				setYear(0);
			}
		}

		m_helper.renderComponentColors(this, view, table, aValue, bSelected, row, column);
		return this;
	}
}
