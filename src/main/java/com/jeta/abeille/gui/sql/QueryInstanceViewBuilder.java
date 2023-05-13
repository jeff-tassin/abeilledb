package com.jeta.abeille.gui.sql;

import java.awt.Container;

import java.sql.SQLException;

import javax.swing.JTable;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.abeille.gui.update.DefaultInstanceProxy;
import com.jeta.abeille.gui.update.InstanceController;
import com.jeta.abeille.gui.update.InstanceFrame;
import com.jeta.abeille.gui.update.InstanceFrameUIDirector;
import com.jeta.abeille.gui.update.InstanceMetaData;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceOptionsView;
import com.jeta.abeille.gui.update.InstanceOptionsViewController;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.InstanceViewBuilder;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.table.TableSorter;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is an factory class for creating and saving SQL query results settings
 * for the instance view.
 * 
 * @author Jeff Tassin
 */
public class QueryInstanceViewBuilder implements InstanceViewBuilder {
	/** the database connection */
	private TSConnection m_connection;

	/** the table id */
	private TableId m_tableid;

	/** the frame that contains the instance view */
	private InstanceFrame m_frame;

	/** the controller for the frame/view */
	private InstanceController m_controller;

	private JTable m_table;

	/** the view for the instance frame */
	private InstanceView m_view;

	/**
	 * the sql results. we need this primarily to get the underlying result set
	 * when accessing binary data
	 */
	private SQLResultsModel m_sqlmodel;

	private QueryInstanceMetaData m_metadata;

	/** this is the result set to initialize the view with. can be null */
	private ResultSetReference m_resultref;

	/** this is the current row to set */
	private int m_currentrow;

	/**
	 * ctor
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param id
	 *            the table id that we are creating the metadata for
	 */
	public QueryInstanceViewBuilder(SQLResultsModel sqlmodel, JTable table, TSConnection tsconn,
			ResultSetReference ref, int row) throws SQLException {
		m_sqlmodel = sqlmodel;
		m_table = table;
		m_connection = tsconn;
		m_resultref = ref;
		m_currentrow = row;
		assert (m_resultref != null);
		m_metadata = new QueryInstanceMetaData(m_table, m_connection, m_resultref, m_sqlmodel.getSettings());
	}

	/**
	 * Called when the user selectes the configure menu item
	 */
	public void configure() {
		assert (m_view != null);

		TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
		dlg.setTitle(I18N.getLocalizedMessage("Form Options"));
		InstanceOptionsView panel = new InstanceOptionsView(getMetaData());
		// set the controller
		panel.setController(new InstanceOptionsViewController(panel));

		dlg.setPrimaryPanel(panel);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();
		if (dlg.isOk()) {
			m_view.getModel().setMetaData(panel.getMetaData());
			m_view.reset();
			m_view.doLayout();
		}
	}

	/**
	 * Not used here
	 */
	public String getID() {
		assert (false);
		return null;
	}

	/**
	 * Creates the InstanceMetaData object for the InstanceView/Model
	 */
	public InstanceMetaData getMetaData() {

		return m_metadata;
	}

	/**
	 * Saves the instance meta data to the application state store.
	 */
	public JETAController getController(TSInternalFrame frame) {
		m_frame = (InstanceFrame) frame;
		if (m_controller == null) {
			m_controller = new InstanceController(m_frame, getView(), getView().getModel());
			m_frame.setUIDirector(new InstanceFrameUIDirector(m_frame, getView()) {
				public void updateComponents(java.util.EventObject evt) {
					super.updateComponents(evt);
					m_frame.enableComponent(InstanceFrame.ID_CLEAR_FORM, false);
					m_frame.enableComponent(InstanceFrame.ID_ADD_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_DELETE_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_MODIFY_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_RUN_QUERY, false);
				}
			});
		}

		return m_controller;
	}

	/**
	 * @return the view for this instance data
	 */
	public InstanceView getView() {
		if (m_view == null) {
			InstanceModel model = new InstanceModel(m_connection, getMetaData());
			if (m_resultref != null) {
				try {
					model.setInstanceProxy(new QueryInstanceProxy(m_sqlmodel, model, m_table, m_currentrow));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			m_view = new InstanceView(model);
		}
		return m_view;
	}

	/**
	 * @param frame
	 *            allows the builder to perform any initializations on the frame
	 *            such as set the title
	 */
	public void initializeFrame(TSInternalFrame frame) {
		InstanceFrame iframe = (InstanceFrame) frame;
		iframe.setTitle(m_connection.getShortId());
		// frame.setShortTitle( m_tableid.getSchemaQualifiedTableName() );
	}

	/**
	 * Saves any configuration settings for the current view
	 */
	public void save() {
		if (m_metadata != null) {
			m_metadata.saveSettings(m_sqlmodel.getSettings());
			SQLSettingsMgr smgr = SQLSettingsMgr.getInstance(m_connection);
			if (smgr != null)
				smgr.save();
		}
	}

}
