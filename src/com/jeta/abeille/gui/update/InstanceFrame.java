package com.jeta.abeille.gui.update;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;

import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JRootPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSResultSet;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.utils.CommitDialog;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.TSEvent;
import com.jeta.foundation.componentmgr.TSNotifier;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.components.TSStatusBar;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;

/**
 * This frame window shows the fields (columns) for a database table. It allows
 * the user to query the table and view and *edit* the results (1 row at a
 * time). It has enhanced gui capabilities over and above the general query
 * results window
 * 
 * 
 * @author Jeff Tassin
 */
public class InstanceFrame extends TSInternalFrame {
	private InstanceView m_instanceview;
	private TSStatusBar m_statusbar;

	/**
	 * the object that is responsible for building the view. Set in
	 * initializeModel
	 */
	private InstanceViewBuilder m_viewbuilder;

	/**
	 * contains the source that launched this frame (e.g. another InstanceFrame,
	 * ModelViewFrame )
	 */
	private InstanceFrameLauncher m_launcher;

	// status bar cell names
	public static final String INSTANCE_POS = "instance_pos";
	public static final String TABLE_COUNT = "table_count";
	public static final String COMMIT_STATUS = "commit.status";

	/** the frame icon for this frame */
	private static ImageIcon m_frameicon;

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/form_blue.png");
	}

	/**
	 * ctor
	 */
	public InstanceFrame() {
		super("");
		setFrameIcon(m_frameicon);
	}

	/**
	 * Create the menu for this frame window
	 */
	protected void createMenu() {
		MenuTemplate template = this.getMenuTemplate();
		MenuDefinition menu = new MenuDefinition(I18N.getLocalizedMessage("Entry"));
		menu.add(i18n_createMenuItem("Clear", ID_CLEAR_FORM, null));
		menu.add(i18n_createMenuItem("Add Row", ID_ADD_ROW, null));
		//menu.add(i18n_createMenuItem("Modify Row", ID_MODIFY_ROW, null));
		//menu.add(i18n_createMenuItem("Delete Row", ID_DELETE_ROW, null));
		menu.add(i18n_createMenuItem("Commit", ID_COMMIT, null));
		menu.add(i18n_createMenuItem("Rollback", ID_ROLLBACK, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Query"));
		menu.add(i18n_createMenuItem("Run Query", ID_RUN_QUERY, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Options"));
		menu.add(i18n_createMenuItem("Configure", ID_CONFIGURE, null));
		menu.add(i18n_createMenuItem("Preferences", InstanceFrame.ID_PREFERENCES, null));
		template.add(menu);

	}

	/**
	 * Creates the toolbar used for this internal frame
	 */
	void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();

		template.add(i18n_createToolBarButton(ID_CLEAR_FORM, "incors/16x16/clear.png", ID_CLEAR_FORM));
		template.addSeparator();
		template.add(i18n_createToolBarButton(ID_ADD_ROW, "incors/16x16/document_add.png", ID_ADD_ROW));
		//template.add(i18n_createToolBarButton(ID_MODIFY_ROW, "incors/16x16/document_edit.png", ID_MODIFY_ROW));
		//template.add(i18n_createToolBarButton(ID_DELETE_ROW, "incors/16x16/document_delete.png", ID_DELETE_ROW));
		template.add(i18n_createToolBarButton(ID_COMMIT, "incors/16x16/data_into.png", ID_COMMIT));
		template.add(i18n_createToolBarButton(ID_ROLLBACK, "incors/16x16/undo.png", ID_ROLLBACK));
		template.addSeparator();

		template.add(i18n_createToolBarButton(ID_RUN_QUERY, "incors/16x16/view.png", ID_RUN_QUERY));
		template.add(i18n_createToolBarButton("Show Tabular Results", "incors/16x16/table_sql_view.png",
				ID_SHOW_TABULAR_RESULTS));
		template.add(i18n_createToolBarButton(ID_FIRST_ROW, "incors/16x16/media_rewind.png", ID_FIRST_ROW));
		template.add(i18n_createToolBarButton(ID_PREV_ROW, "incors/16x16/media_step_back.png", ID_PREV_ROW));
		template.add(i18n_createToolBarButton(ID_NEXT_ROW, "incors/16x16/media_step_forward.png", ID_NEXT_ROW));
		template.add(i18n_createToolBarButton(ID_LAST_ROW, "incors/16x16/media_fast_forward.png", ID_LAST_ROW));
		template.addSeparator();

		template.add(i18n_createToolBarButton(ID_PASTE_INSTANCE, "incors/16x16/paste_instance.png", "Paste Primary Key"));

	}

	public void dispose() {
		super.dispose();
		InstanceModel model = m_instanceview.getModel();
		model.close();
		m_viewbuilder.save();

		// we need to do a garbage collect here to release connections properly
		// this will probabaly not work very well with some VM's
		// System.gc();
	}

	/**
	 * @return the object that launched this frame (e.g. another InstanceFrame,
	 *         ModelViewFrame, etc)
	 */
	InstanceFrameLauncher getLauncher() {
		return m_launcher;
	}

	/**
	 * @return the underlying instance view
	 */
	public InstanceView getView() {
		return m_instanceview;
	}

	/**
	 * @return the object responsible for building/configuring the instance view
	 */
	InstanceViewBuilder getViewBuilder() {
		return m_viewbuilder;
	}

	/**
	 * Creates the menu, toolbar, and content window for this frame. Called by
	 * the framework when this frame is created.
	 * 
	 * @param params
	 *            The caller should pass in the following parameters: param[0]
	 *            an InstanceViewBuilder object that is used to create the
	 *            instance view and controller
	 * 
	 */
	public void initializeModel(Object[] params) {

		m_viewbuilder = (InstanceViewBuilder) params[0];
		m_instanceview = m_viewbuilder.getView();
		JScrollPane scrollpane = new JScrollPane(m_instanceview);
		scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(scrollpane, BorderLayout.CENTER);

		JFrame jframe = (JFrame) getDelegate();
		jframe.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				m_instanceview.doLayout();
			}
		});

		if (params.length > 1) {
			// the second param must be the object that is launching this frame
			m_launcher = (InstanceFrameLauncher) params[1];
		}

		m_statusbar = new TSStatusBar();
		TSCell cell1 = new TSCell(INSTANCE_POS, "###### of ############ ");
		m_statusbar.addCell(cell1);
		TSCell cell2 = new TSCell(TABLE_COUNT, "Table Count #############");
		m_statusbar.addCell(cell2);
		TSCell cell3 = new TSCell(COMMIT_STATUS, "Commit  ######");
		cell3.setMain(true);
		m_statusbar.addCell(cell3);

		getContentPane().add(m_statusbar, BorderLayout.SOUTH);
		createMenu();
		createToolBar();

		InstanceController controller = (InstanceController) m_viewbuilder.getController(this);
		setController(controller);
		m_viewbuilder.initializeFrame(this);
		controller.updateView();
	}

	/**
	 * Sets the status message for a given cell on the status bar
	 * 
	 * @param cellName
	 *            the name of the cell whose status message to set
	 * @param statusMsg
	 *            the message to display
	 */
	public void setStatus(String cellName, String statusMsg) {
		TSCell cell = (TSCell) m_statusbar.getCell(cellName);
		if (cell != null) {
			cell.setText(statusMsg);
		}
	}

	/**
	 * Override TSInternalFrame. This method is called by the TS framework
	 * before closing the window. Here, we check if we have uncommitted data. If
	 * so, we post a message to the user asking if the data should be commited.
	 */
	public boolean tryCloseFrame() {
		InstanceModel model = m_instanceview.getModel();
		if (model.supportsTransactions() && model.shouldCommit()) {
			String msg = I18N.getLocalizedMessage("window_has_uncomitted_data");
			CommitDialog dlg = (CommitDialog) TSGuiToolbox.createDialog(CommitDialog.class, this, true);
			dlg.initialize(msg);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				try {
					if (dlg.getResult() == CommitDialog.COMMIT) {
						model.commit();
						model.close();
					} else {
						model.rollback();
						model.close();
					}
					return true;
				} catch (SQLException e) {
					SQLErrorDialog sdlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, this, true);
					sdlg.initialize(e, null);
					sdlg.setSize(sdlg.getPreferredSize());
					sdlg.showCenter();
					return false;
				}
			} else
				return false;
		} else
			return true;
	}

	/**
	 * Updates the status bar to show the current row pointer
	 * 
	 * @param pos
	 *            the current position in the result set. This value can be -1
	 *            if you don't want to show anything in the cell
	 * @param rowCount
	 *            the total number of instances in the result set. This can be
	 *            -1 if the total is not known.
	 * @param maxRow
	 *            this the maximum number of instances downloaded so far.
	 */
	public void updateRowStatus(int pos, int rowCount, int maxRow) {
		// System.out.println( "InstanceFrame.updateRowStatus  pos = " + pos +
		// "  rowCount = " + rowCount + "  maxRow = " + maxRow );
		TSCell cell = (TSCell) m_statusbar.getCell(INSTANCE_POS);
		if (pos < 0) {
			cell.setText("");
		} else {
			String msg = "";
			if (rowCount < 0) // the rowCount is unknown, so use the max row
				msg = I18N.format("Row_Pointer_2_plus", new Integer(pos), new Integer(maxRow));
			else if (rowCount == 0) {
				msg = I18N.getLocalizedMessage("No Results");
			} else
				// total row count is known
				msg = I18N.format("Row_Pointer_1", new Integer(pos), new Integer(rowCount));

			cell.setText(msg);
		}

	}

	/**
	 * Update the status bar to show the current count
	 * 
	 * @param count
	 *            the new table count
	 */
	public void updateTableCount(final long count) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TSCell cell = (TSCell) m_statusbar.getCell(TABLE_COUNT);
				if (cell != null) {
					String msg = I18N.format("Table_Count_1", new Long(count));
					cell.setText(msg);
				}

			}
		});
	}

	// ///////////////////////////////////////////////////////////////////////////////////
	public static final String ID_CLEAR_FORM = "Clear";
	public static final String ID_ADD_ROW = "Add Row";
	public static final String ID_DELETE_ROW = "Delete Row";
	public static final String ID_MODIFY_ROW = "Modify Row";
	public static final String ID_RUN_QUERY = "Run Query";
	public static final String ID_SHOW_TABULAR_RESULTS = "Show Tabular Results";
	public static final String ID_STOP = "Stop";
	public static final String ID_FIRST_ROW = "First";
	public static final String ID_NEXT_ROW = "Next";
	public static final String ID_PREV_ROW = "Prev";
	public static final String ID_LAST_ROW = "Last";
	public static final String ID_CONFIGURE = "Configure";
	public static final String ID_COMMIT = "Commit";
	public static final String ID_ROLLBACK = "Rollback";
	public static final String ID_PASTE_INSTANCE = "paste.instance";
	public static final String ID_PREFERENCES = "form.view.preferences";
}
