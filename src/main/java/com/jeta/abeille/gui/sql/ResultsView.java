package com.jeta.abeille.gui.sql;

import java.awt.BorderLayout;

import java.lang.ref.WeakReference;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.queryresults.QueryResultsView;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSStatusBar;

import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.i18n.I18N;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class manages the view for a single resultset. This frame window can
 * display multiple resultsets, so we manage each view with this type of object.
 */
public class ResultsView extends TSPanel {
	/** the underlying database connection */
	private TSConnection m_connection;

	/** the status bar */
	private TSStatusBar m_statusbar;

	/** the underlying data model that manages the result set */
	private SQLResultsModel m_model;

	/** the underlying view for the query */
	private QueryResultsView m_view;

	/**
	 * used to hold the instance frame if the user presses the show instances
	 * action. If the query results window was launched by an instance frame,
	 * then the lanucher reference will be maintained by the SQLResultsFrame
	 */
	private WeakReference m_instance_frame = null;

	/** status bar cell ids */
	public static final String ROW_COUNT_CELL = "row.count.cell";
	public static final String MAX_ROWS_CELL = "max.rows.cell";
	public static final String SQL_CELL = "sql.cell";

	/**
	 * ctor
	 */
	public ResultsView(TSConnection tsconn, SQLResultsModel model) {
		m_connection = tsconn;
		setLayout(new BorderLayout());

		m_statusbar = new TSStatusBar();
		TSCell cell1 = new TSCell(ROW_COUNT_CELL, "###100000 rows####");
		m_statusbar.addCell(cell1);

		TSCell cell2 = new TSCell(MAX_ROWS_CELL, "Max Rows: #####");
		m_statusbar.addCell(cell2);
		try {
			String max_rows = String.valueOf(TSConnection.getMaxQueryRows());
			max_rows = I18N.format("Max_query_rows_1", max_rows);
			cell2.setText(max_rows);
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		TSCell cell3 = new TSCell(SQL_CELL, "#####");
		cell3.setMain(true);
		m_statusbar.addCell(cell3);

		setResults(model);
	}

	public void setResults(SQLResultsModel model) {
		removeAll();

		m_model = model;
		m_view = new QueryResultsView(m_model);
		add(m_view, BorderLayout.CENTER);
		add(m_statusbar, BorderLayout.SOUTH);

		// restore any table settings that we associated with the sql statement
		// in a previous
		// instance of this frame
		String sql = m_model.getUnprocessedSQL();

		if (sql == null)
			sql = m_model.getSQL();

		if (sql != null && sql.length() > 0) {
			TSTablePanel tspanel = m_view.getTablePanel();

			SQLSettingsMgr smgr = SQLSettingsMgr.getInstance(m_connection);
			String trimsql = smgr.trim(sql);

			SQLSettings settings = smgr.get(trimsql);
			if (settings == null) {
				settings = new SQLSettings(trimsql);
				smgr.add(settings);
			} else {
				if (!TableUtils.restoreTableSettings(tspanel, settings.getTableSettings())) {
					// the stored table settings are not consistent with the
					// columns from the sql statement
					// so, we remove the stored settings.
					settings.setTableSettings(null);
				} else {
					// settings.getTableSettings().print();
				}
			}
			m_model.setSettings(settings);

			TSCell cell = (TSCell) m_statusbar.getCell(SQL_CELL);
			if (sql.length() > 256)
				cell.setText(sql.substring(0, 255));
			else
				cell.setText(sql);
		}

		revalidate();
		repaint();
	}

	/**
	 * Cleans up this view to assist in garbage collection.
	 */
	public void dispose() {
		removeAll();
		m_statusbar = null;
		m_model = null;
		m_view.dispose();
		m_view = null;
	}

	/**
	 * @return the reference used to hold the instance frame if the user presses
	 *         the show instances action. If the query results window launched
	 *         by an instance frame, then the lanucher reference will be
	 *         maintained by the SQLResultsFrame
	 */
	public WeakReference getInstanceFrameReference() {
		return m_instance_frame;
	}

	public SQLResultsModel getModel() {
		return m_model;
	}

	public QueryResultsView getView() {
		return m_view;
	}

	/**
	 * Sets the reference used to hold the instance frame if the user presses
	 * the show instances action.
	 */
	public void setInstanceFrameReference(WeakReference ref) {
		m_instance_frame = ref;
	}

	/**
	 * Sets the status bar message
	 */
	public void setStatus(String msg) {
		if (m_statusbar != null) {
			TSCell cell = (TSCell) m_statusbar.getCell(ROW_COUNT_CELL);
			cell.setText(msg);
		}
	}

}
