package com.jeta.abeille.gui.sql;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.queryresults.QueryResultsView;
import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.gui.table.TableSettings;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;

/**
 * This class manages the view for a single result set. This frame window can
 * display multiple result sets, so we manage each view with this type of object.
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
	private WeakReference<TSInternalFrame> m_instance_frame = null;

	private Object m_launcher;

	/** status bar cell ids */
	public static final String ROW_COUNT_CELL = "row.count.cell";
	public static final String MAX_ROWS_CELL = "max.rows.cell";
	public static final String SQL_CELL = "sql.cell";

	/**
	 * ctor
	 */
	public ResultsView(Object launcher, TSConnection tsconn, SQLResultsModel model) {
		m_launcher = launcher;
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

	/**
	 * Creates the toolbar for this frame
	 */
	protected JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(i18n_createToolBarButton("incors/16x16/copy.png", TSComponentNames.ID_COPY,  "Copy"));
		toolbar.addSeparator();

		toolbar.add(i18n_createToolBarButton("incors/16x16/form_blue.png", SQLResultsNames.ID_SHOW_INSTANCE, "Row View"));

		toolbar.addSeparator();
		toolbar.add(i18n_createToolBarButton( "incors/16x16/transpose_table.png", SQLResultsNames.ID_TRANSPOSE, "Transpose"));

		toolbar.addSeparator();
		toolbar.add(i18n_createToolBarButton( "incors/16x16/table.png", SQLResultsNames.ID_NO_SPLIT, "No Split"));
		toolbar.add(i18n_createToolBarButton( "incors/16x16/column.png", SQLResultsNames.ID_SPLIT_VERTICAL, "Split Vertical"));
		toolbar.add(i18n_createToolBarButton( "incors/16x16/row.png", SQLResultsNames.ID_SPLIT_HORIZONTAL, "Split Horizontal"));
		toolbar.add(i18n_createToolBarButton( "incors/16x16/preferences.png", SQLResultsNames.ID_TABLE_OPTIONS, "Table Options"));
		toolbar.addSeparator();
		toolbar.add(i18n_createToolBarButton( "incors/16x16/media_step_back.png", SQLResultsNames.ID_FIRST, "First"));
		toolbar.add(i18n_createToolBarButton( "incors/16x16/media_step_forward.png", SQLResultsNames.ID_LAST, "Last"));
		toolbar.addSeparator();
		toolbar.add(i18n_createToolBarButton( "incors/16x16/redo.png", SQLResultsNames.ID_REDO_QUERY, "Redo Query"));

		toolbar.addSeparator();
		toolbar.add(i18n_createToolBarButton( "incors/16x16/information.png", SQLResultsNames.ID_QUERY_INFO, "Query Information"));

		toolbar.add(javax.swing.Box.createHorizontalStrut(16));
		toolbar.add(i18n_createToolBarButton( "incors/16x16/windows.png", SQLResultsNames.ID_SHOW_IN_FRAME_WINDOW, "Detach Window"));
		return toolbar;

	}

	public void setResults(SQLResultsModel model) {
		removeAll();

		m_model = model;
		m_view = new QueryResultsView(m_model);
		m_view.getTablePanel()
				.getTable()
				.getTableHeader()
				.addMouseListener( new MouseAdapter() {
					public void mouseReleased(MouseEvent arg0) {
						// saveFrame on column resize events
						saveFrame();
					}
				});

		add(m_view, BorderLayout.CENTER);
		add(m_statusbar, BorderLayout.SOUTH);
		add(createToolBar(), BorderLayout.NORTH);

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
		m_launcher = null;
	}

	public void copy() {
		m_view.copy();
	}

	public TableSelection getSelection() {
		return m_view.getSelection();
	}

	public SQLResultsModel getModel() {
		return m_model;
	}

	public QueryResultsView getView() {
		return m_view;
	}

	/**
	 * @return the reference used to hold the instance frame if the user presses
	 *         the show instances action. If the query results window launched
	 *         by an instance frame, then the lanucher reference will be
	 *         maintained by the SQLResultsFrame
	 */
	public WeakReference<TSInternalFrame> getInstanceFrameReference() {
		return m_instance_frame;
	}

	/**
	 * Sets the reference used to hold the instance frame if the user presses
	 * the show instances action.
	 */
	public void setInstanceFrameReference(WeakReference<TSInternalFrame> ref) {
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

	public TSConnection getConnection() {
		return m_connection;
	}

	public TSTablePanel getTablePanel() {
		return m_view.getTablePanel();
	}

	public Object getLauncher() {
		return m_launcher;
	}

	public void splitVertical() {
		m_view.splitVertical();
	}
	public void splitHorizontal() {
		m_view.splitHorizontal();
	}

	public void showNormal() {
		m_view.showNormal();
	}

	public boolean isSplitHorizontal() {
		return m_view.isSplitHorizontal();
	}

	public boolean isSplitVertical() {
		return m_view.isSplitVertical();
	}

	/**
	 * Saves any frame settings such as column widths for a given SQL
	 */
	public void saveFrame() {
		SQLResultsModel model = (SQLResultsModel) getModel();

		// now, let's store the table settings (e.g. the table column
		// widths) so that the next
		// time the user runs this query, we can restore the table to the
		// way the user prefers it
		String sql = model.getUnprocessedSQL();
		if (sql == null)
			sql = model.getSQL();

		if (sql != null && sql.length() > 0) {
			TSTablePanel tspanel = m_view.getTablePanel();
			TableSettings tablesettings = TableUtils.getTableSettings(tspanel);
			SQLSettingsMgr smgr = SQLSettingsMgr.getInstance(m_connection);
			String trimsql = smgr.trim(sql);

			SQLSettings sqlsettings = smgr.get(trimsql);
			if (sqlsettings == null) {
				sqlsettings = new SQLSettings(trimsql);
			} else {
				// @todo trim spaces and semicolon from sql as well as
				// convert to lowercase since
				// we want to handle from sql command window as well
				smgr.remove(trimsql);
			}

			sqlsettings.setTableSettings(tablesettings);
			smgr.add(sqlsettings);
			smgr.save();
		}
	}

	public void configureTableOptions() {
		m_view.configureTableOptions();
	}



}
