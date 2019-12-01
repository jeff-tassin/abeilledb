package com.jeta.abeille.gui.export;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.io.File;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.QueryResultsModel;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSComponentUtils;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.components.TSStatusBar;
import com.jeta.foundation.gui.components.TSCell;

import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.gui.table.export.ExportModel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class allows the user to export the results of a query to a file. The
 * user can configure the format of the file. For example, the column names can
 * be included and the type of delimiters can be specified.
 * 
 * @author Jeff Tassin
 */
public class ExportDialog extends TSDialog {
	/**
	 * The content panel for this frame
	 */
	private ExportPanel m_exportpanel;

	/**
	 * The datamodel for this frame and panel
	 */
	private SQLExportModel m_model;

	/**
	 * The status bar
	 */
	private TSStatusBar m_statusbar;

	public static final String STATUS_CELL = "status_cell";

	/** the frame icon for this frame */
	private static ImageIcon m_frameicon;

	static {
		m_frameicon = TSGuiToolbox.loadImage("general/Export16.gif");
	}

	/**
	 * ctor
	 */
	public ExportDialog(java.awt.Frame parent, boolean bmodal) {
		super(parent, bmodal);

		setTitle(I18N.getLocalizedMessage("Export"));

		createMenu();
		createToolBar();

		m_statusbar = new TSStatusBar();
		TSCell cell1 = new TSCell(STATUS_CELL, "############################# ");
		cell1.setHorizontalAlignment(SwingConstants.LEFT);
		cell1.setMain(true);
		m_statusbar.addCell(cell1);
		// getContentPane().add( m_statusbar, BorderLayout.SOUTH );

	}

	/**
	 * Create the menu for this frame window
	 */
	protected void createMenu() {
		/*
		 * MenuTemplate template = this.getMenuTemplate(); JMenu menu = new
		 * JMenu( I18N.getLocalizedMessage( "File" ) ); menu.add(
		 * i18n_createMenuItem( "Select", ExportNames.ID_SELECT_FILE, null,
		 * "openfile16.gif" ) ); menu.add( i18n_createMenuItem( "Close",
		 * ExportNames.ID_CLOSE, null, "close16.gif" ) ); template.add( menu );
		 * 
		 * menu = new JMenu( I18N.getLocalizedMessage( "Export" ) ); menu.add(
		 * i18n_createMenuItem( "Preview", ExportNames.ID_SAMPLE_OUTPUT, null,
		 * "empty16.gif" ) ); menu.add( i18n_createMenuItem( "Start",
		 * ExportNames.ID_START_EXPORT, null, "start16.gif" ) ); template.add(
		 * menu );
		 */
	}

	/**
	 * Creates the toolbar for this frame
	 */
	public void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();
		template.add(i18n_createToolBarButton(ExportNames.ID_RESET, "clear16.gif", null));
		template.add(i18n_createToolBarButton(ExportNames.ID_SQL_FORMAT, "query16.gif", null));
		template.addSeparator();
		template.add(i18n_createToolBarButton(ExportNames.ID_START_EXPORT, "general/Export16.gif", null));
		template.add(i18n_createToolBarButton(ExportNames.ID_STOP_EXPORT, "general/Stop16.gif", null));
		template.add(i18n_createToolBarButton(ExportNames.ID_SAMPLE_OUTPUT, "table16.gif", null));
	}

	/**
	 * @return the underlying content panel
	 */
	public ExportPanel getExportPanel() {
		return m_exportpanel;
	}

	/**
	 * Creates and initializes the components on this dialog
	 * 
	 * @param params
	 *            the list of parameters need to initialize this frame
	 *            Currently, we expect an array (size >= 1) where the first
	 *            parameter must be a QueryResultsModel object or a TableId. If
	 *            the first parameter is a tableid, then we assume the caller
	 *            wants to export the entire table. In this case we prefill with
	 *            INSERT sql syntax.
	 */
	public void initialize(Object[] params) {
		QueryResultsModel querymodel = (QueryResultsModel) params[0];

		TableSelection selection = null;
		if (params.length == 2) {
			selection = (TableSelection) params[1];
			int[] cols = selection.getColumnSpan();
			for (int index = 0; index < cols.length; index++) {
				System.out.println("col = " + cols[index]);
			}
		}

		m_model = new SQLExportModel(querymodel, selection);
		m_exportpanel = new ExportPanel(m_model);

		// getContentPane().add( m_exportpanel, BorderLayout.CENTER );
		setPrimaryPanel(m_exportpanel);
		Dimension d = m_exportpanel.getPreferredSize();
		d.height += TSComponentUtils.getTitleBarHeight();
		d.width += 10;
		// setPreferredSize( d );
		updateComponents();
		// setController( new ExportFrameController(this) );
	}

	/**
	 * Sets the message in the status bar
	 */
	void setStatusMessage(String msg) {
		TSCell cell = (TSCell) m_statusbar.getCell(STATUS_CELL);
		cell.setText(msg);
	}

	/**
	 * Updates the GUI components on this frame
	 */
	void updateComponents() {
		if (m_model.getTableId() == null)
			enableComponent(ExportNames.ID_SQL_FORMAT, false);
		else
			enableComponent(ExportNames.ID_SQL_FORMAT, true);

		if (m_model.isExporting()) {
			enableComponent(ExportNames.ID_STOP_EXPORT, true);
			enableComponent(ExportNames.ID_START_EXPORT, false);
		} else {
			enableComponent(ExportNames.ID_STOP_EXPORT, false);
			enableComponent(ExportNames.ID_START_EXPORT, true);
		}

	}
}
