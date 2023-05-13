package com.jeta.abeille.gui.queryresults;

import java.awt.Color;

import java.awt.Component;

import java.util.Calendar;
import java.sql.Timestamp;
import java.util.Date;

import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import com.jeta.abeille.database.utils.RowInstance;

import com.jeta.foundation.gui.components.TSTimeStampField;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the time cell renderer for a SQL query results table
 * 
 * @author Jeff Tassin
 */
public class TimeStampRenderer extends TSTimeStampField implements TableCellRenderer {
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
	public TimeStampRenderer(QueryResultsView view) {
		if (view != null) {
			m_viewref = new WeakReference(view);
		}
		setFont(UIManager.getFont("Table.font"));
		m_cal = Calendar.getInstance();
		setBorder(null);
	}

	public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
			int row, int column) {
		if (aValue instanceof Timestamp) {
			Timestamp dt = (Timestamp) aValue;
			m_cal.setTime(dt);
			set(m_cal.get(Calendar.YEAR), m_cal.get(Calendar.MONTH), m_cal.get(Calendar.DAY_OF_MONTH),
					m_cal.get(Calendar.HOUR_OF_DAY), m_cal.get(Calendar.MINUTE), m_cal.get(Calendar.SECOND),
					dt.getNanos());
		} else if (aValue instanceof Calendar) {
			Calendar cal = (Calendar) aValue;
			m_cal.setTime(cal.getTime());
			set(m_cal.get(Calendar.YEAR), m_cal.get(Calendar.MONTH), m_cal.get(Calendar.DAY_OF_MONTH),
					m_cal.get(Calendar.HOUR_OF_DAY), m_cal.get(Calendar.MINUTE), m_cal.get(Calendar.SECOND), 0);
		} else {
			if (aValue == null)
				setNull(true);
			else {
				set(0, 0, 0, 0, 0, 0, 0);
			}
		}

		QueryResultsView view = null;
		if (m_viewref != null)
			view = (QueryResultsView) m_viewref.get();

		m_helper.renderComponentColors(this, view, table, aValue, bSelected, row, column);

		return this;
	}
}
