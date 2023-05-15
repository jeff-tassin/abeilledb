package com.jeta.abeille.gui.queryresults;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.gui.sql.SQLDefaultSettings;
import com.jeta.abeille.gui.sql.SQLSettingsNames;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.TSTableNames;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.utils.TSUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.SQLException;

/**
 * This panel is used to display the results of a SQL query in a JTable. Each
 * column in the table has a ColumnMetaData object set as the identifier.
 * 
 * @author Jeff Tassin
 */
public class QueryResultsView extends TSPanel {
	/** the data model */
	private QueryResultsModel m_model;

	/** the table panel that supports split views and sorting */
	private TSTablePanel m_tablepanel;

	/**
	 * ctor
	 */
	public QueryResultsView(QueryResultsModel model) {
		try {
			m_model = model;
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Invokes the table options dialog for the table panel
	 */
	public void configureTableOptions() {
		TSController controller = (TSController) m_tablepanel.getController();
		if (controller != null) {
			controller.invokeAction(TSTableNames.ID_TABLE_OPTIONS);
		} else {
			assert (false);
		}
	}

	/**
	 * Copies the selected table rows/columns to the clipboard using the default
	 * copy settings.
	 */
	public void copy() {
		TSController controller = (TSController) m_tablepanel.getController();
		if (controller != null)
			controller.invokeAction(TSComponentNames.ID_COPY);
	}

	/**
	 * Provide our own dispose so we can get more effective garbage collection
	 */
	public void dispose() {
		removeAll();
		m_tablepanel.dipose();
		m_tablepanel = null;
		m_model = null;
	}

	public void finalize() throws Throwable {
		super.finalize();
		TSUtils.printMessage("QueryResultsView.finalize");
	}

	public TSConnection getConnection() {
		return m_model.getTSConnection();
	}

	/**
	 * @return the underlying data model
	 */
	public QueryResultsModel getModel() {
		return m_model;
	}

	/**
	 * @return the a set of RowSelection objects that represents the selected
	 *         cells in the table.
	 */
	public TableSelection getSelection() {
		return m_tablepanel.getSelection();
	}

	/**
	 * @return the underlying table panel
	 */
	public TSTablePanel getTablePanel() {
		return m_tablepanel;
	}

	/**
	 * Initializes the view
	 */
	private void initialize() throws SQLException {
		setLayout(new BorderLayout());
		m_tablepanel = new TSTablePanel(m_model) {
			/**
			 * Override so that we can test if the results have fully been
			 * downloaded
			 */
			public boolean canSortColumn(JTable table, int viewcolumn) {
				if (m_model.isRowCountKnown()) {
					/**
					 * show a message on the screen if the row count is large
					 * because if the count is too large, it could take a while
					 */
					if (m_model.getRowCount() > 1000) {
						String msg = I18N.getLocalizedMessage("Sort_large_result_set_question");
						String title = I18N.getLocalizedMessage("Confirm");
						int result = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
						return (result == JOptionPane.YES_OPTION);
					}
					return true;
				} else {
					String msg = I18N.getLocalizedMessage("Cannot Sort Query Results");
					String title = I18N.getLocalizedMessage("Message");
					JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
					return false;
				}
			}
		};
		// set the cell renderer
		TableId tableid = m_model.getTableId();

		// check if the user wants to use the default renderer for the query
		// results
		boolean defaultrenderer = false;
		try {
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			defaultrenderer = Boolean.valueOf(
					userprops.getProperty(SQLSettingsNames.USE_DEFAULT_RENDERER,
							SQLDefaultSettings.USE_DEFAULT_RENDERER)).booleanValue();
		} catch (Exception e) {
			// ignore
		}

		// NOTE: if we add a persistence mechanism to the table to
		// save settings such as column order/widths, you must load
		// the settings after this method or the ColumnMetaData objects
		// won't be set correctly

		// now, let's associate the columnmetadata object with the
		// table column that shows that data. This allows the caller
		// to easily get the column info from the table
		JTable table = m_tablepanel.getTable();
		TableColumnModel colmodel = table.getColumnModel();
		for (int index = 0; index < m_model.getColumnCount(); index++) {
			ColumnMetaData cmd = m_model.getColumnMetaData(index);
			if (tableid != null)
				cmd.setParentTableId(tableid);
			TableColumn col = colmodel.getColumn(index);
			col.setIdentifier(cmd);

			TableCellRenderer delegate = table.getDefaultRenderer(m_model.getColumnClass(index));

			TableCellRenderer renderer = null;
			if (defaultrenderer) {
				renderer = new QueryResultsRenderer(this, new javax.swing.table.DefaultTableCellRenderer());
			} else if (java.sql.Time.class == m_model.getColumnClass(index)) {
				renderer = new TimeRenderer(this);
			} else if (java.sql.Date.class == m_model.getColumnClass(index)) {
				renderer = new DateRenderer(this);
			} else if (java.sql.Timestamp.class == m_model.getColumnClass(index)) {
				renderer = new TimeStampRenderer(this);
			} else {
				renderer = new QueryResultsRenderer(this, delegate);
			}

			m_tablepanel.setCellRenderer(index, renderer);
		}

		m_tablepanel.syncTableColumnCache();
		add(m_tablepanel, BorderLayout.CENTER);
	}

	/**
	 * @return true if the view is currently split horizontally
	 */
	public boolean isSplitHorizontal() {
		if (m_tablepanel == null)
			return false;
		else
			return m_tablepanel.isSplitHorizontal();
	}

	/**
	 * @return true if the view is currently split veritcally
	 */
	public boolean isSplitVertical() {
		if (m_tablepanel == null)
			return false;
		else
			return m_tablepanel.isSplitVertical();
	}

	/**
	 * Shows the query results window with no split
	 */
	public void showNormal() {
		m_tablepanel.showNormal();
	}

	/**
	 * Splits the view horizontally. This is opposite from how it is defined in
	 * the JSplitPane (which makes no sense). The definition should be based on
	 * the orientation of the divider (as it is here )
	 */
	public void splitHorizontal() {
		m_tablepanel.splitHorizontal();
	}

	/**
	 * Splits the view vertically. This is opposite from the definition in
	 * JSplitPane (which makes no sense). The definition should be based on the
	 * orientation of the divider (as it is here )
	 */
	public void splitVertical() {
		m_tablepanel.splitVertical();
	}

}
