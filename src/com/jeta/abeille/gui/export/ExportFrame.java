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

import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSComponentUtils;
import com.jeta.foundation.gui.components.MenuDefinition;
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
public class ExportFrame extends TSInternalFrame {
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
	public ExportFrame() {
		super(I18N.getLocalizedMessage("Export"));
		setFrameIcon(m_frameicon);
		// createMenu();
		// createToolBar();

		m_statusbar = new TSStatusBar();
		TSCell cell1 = new TSCell(STATUS_CELL, "############################# ");
		cell1.setHorizontalAlignment(SwingConstants.LEFT);
		cell1.setMain(true);
		m_statusbar.addCell(cell1);
		getContentPane().add(m_statusbar, BorderLayout.SOUTH);
	}

	/**
	 * Create the menu for this frame window
	 */
	protected void createMenu() {
		MenuTemplate template = this.getMenuTemplate();
		MenuDefinition menu = new MenuDefinition(I18N.getLocalizedMessage("File"));
		menu.add(i18n_createMenuItem("Clear", ExportNames.ID_RESET, null, null));
		menu.add(i18n_createMenuItem("Close", ExportNames.ID_CLOSE, null, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Export"));
		menu.add(i18n_createMenuItem("Preview", ExportNames.ID_SAMPLE_OUTPUT, null, null));
		menu.add(i18n_createMenuItem("Start", ExportNames.ID_START_EXPORT, null, null));
		menu.add(i18n_createMenuItem("Stop", ExportNames.ID_STOP_EXPORT, null, null));

		template.add(menu);
	}

	/**
	 * Creates the toolbar for this frame
	 */
	public void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();
		template.add(i18n_createToolBarButton(ExportNames.ID_RESET, "incors/16x16/clear.png", "Clear"));
		template.addSeparator();
		template.add(i18n_createToolBarButton(ExportNames.ID_START_EXPORT, "incors/16x16/element_run.png",
				"Start Export"));
		template.add(i18n_createToolBarButton(ExportNames.ID_STOP_EXPORT, "incors/16x16/stop.png", "Stop Export"));
		template.add(i18n_createToolBarButton(ExportNames.ID_SAMPLE_OUTPUT, "incors/16x16/document_view.png", "Preview"));

		/*
		template.add(javax.swing.Box.createHorizontalStrut(10));
		javax.swing.JButton hbtn = i18n_createToolBarButton(ExportNames.ID_HELP, "incors/16x16/help2.png", "Help");
		com.jeta.foundation.help.HelpUtils.enableHelpOnButton(hbtn, ExportNames.ID_HELP);
		template.add(hbtn);
		 */

	}

	/**
	 * @return the underlying content panel
	 */
	public ExportPanel getExportPanel() {
		return m_exportpanel;
	}

	/**
	 * @return the underlying data model
	 */
	public SQLExportModel getModel() {
		return m_model;
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
	public void initializeModel(Object[] params) {
		QueryResultsModel querymodel = (QueryResultsModel) params[0];

		TableSelection selection = null;
		if (params.length == 2) {
			selection = (TableSelection) params[1];
			// System.out.println( "ExportFrame.initializeModel gotSelection: "
			// );
			// int[] cols = selection.getColumnSpan();
			// for( int index=0; index < cols.length; index++ )
			// {
			// System.out.println( "col = " + cols[index] );
			// }
		}

		m_model = new SQLExportModel(querymodel, selection);
		m_exportpanel = new ExportPanel(m_model);

		getContentPane().add(m_exportpanel, BorderLayout.CENTER);
		Dimension d = m_exportpanel.getPreferredSize();
		d.height += TSComponentUtils.getTitleBarHeight();
		d.width += 10;
		setPreferredSize(d);
		setController(new ExportFrameController(this));
	}

	/**
	 * Sets the message in the status bar
	 */
	void setStatusMessage(String msg) {
		TSCell cell = (TSCell) m_statusbar.getCell(STATUS_CELL);
		cell.setText(msg);
	}

}
