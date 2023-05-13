package com.jeta.abeille.gui.update;

import java.awt.Container;

import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is an factory class for creating and saving TableInstanceMetaData
 * settings.
 * 
 * @author Jeff Tassin
 */
public class TableInstanceViewBuilder implements InstanceViewBuilder {
	/** the database connection */
	protected TSConnection m_connection;

	/** the table id */
	protected TableId m_tableid;

	/** the frame that contains the instance view */
	private InstanceFrame m_frame;

	/** the controller for the frame/view */
	private InstanceController m_controller;

	/** the view for the instance frame */
	protected InstanceView m_view;

	protected TableInstanceMetaData m_metadata;

	/** this is the result set to initialize the view with. can be null */
	private ResultSetReference m_resultref;

	/**
	 * this is the instance proxy to initialize the view with. This value can be
	 * null and takes precedence of m_resultref if both are set
	 */
	private InstanceProxy m_instanceproxy;

	/** this is the current row to set */
	private int m_initialrow;

	/**
	 * ctor
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param id
	 *            the table id that we are creating the metadata for
	 */
	public TableInstanceViewBuilder(TSConnection connection, TableId id) {
		m_connection = connection;
		m_tableid = id;
	}

	/**
	 * Called when the user selectes the configure menu item
	 */
	public void configure() {
		assert (m_view != null);

		final TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
		InstanceOptionsView panel = new InstanceOptionsView(getMetaData());
		// set the controller
		panel.setController(new InstanceOptionsViewController(panel));

		dlg.setTitle(I18N.getLocalizedMessage("Column Settings"));
		dlg.setPrimaryPanel(panel);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();
		if (dlg.isOk()) {
		    m_metadata = (TableInstanceMetaData)panel.getMetaData();
			m_view.getModel().setMetaData(m_metadata);
			m_view.reset();
			m_view.doLayout();
			m_metadata.saveSettings();
		}
	}

	/**
	 * @return the underlying connection manager
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return an identifier that identifies the frame so that if the user wants
	 *         to launch the same frame again, we can locate it
	 */
	public String getID() {
		return m_tableid.getFullyQualifiedName();
	}

	/**
	 * @return the initial row in the result set
	 */
	public int getInitialRow() {
		return m_initialrow;
	}

	/**
	 * Creates the InstanceMetaData object for the InstanceView/Model
	 */
	public InstanceMetaData getMetaData() {
		if (m_metadata == null) {
			m_metadata = new TableInstanceMetaData(m_connection, m_tableid);
		}

		return m_metadata;
	}

	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * Saves the instance meta data to the application state store.
	 */
	public JETAController getController(TSInternalFrame frame) {
		m_frame = (InstanceFrame) frame;
		if (m_controller == null) {
			m_controller = new InstanceController(m_frame, getView(), getView().getModel());
			frame.setUIDirector(new InstanceFrameUIDirector(m_frame, m_view));
		}
		return m_controller;
	}

	/**
	 * Internal method that simply returns the m_view reference
	 */
	protected InstanceView _getView() {
		return m_view;
	}

	/**
	 * @return the view for this instance data
	 */
	public InstanceView getView() {
		if (m_view == null) {
			InstanceMetaData metadata = getMetaData();
			InstanceModel model = new InstanceModel(m_connection, metadata);
			if (metadata instanceof TableInstanceMetaData) {
				TableInstanceMetaData timd = (TableInstanceMetaData) metadata;
				TableMetaData tmd = timd.getTableMetaData();
				// if ( tmd.isView() )
				// {
				// model.setAllowsUpdates( false );
				// }
			}

			if (m_instanceproxy != null) {
				model.setInstanceProxy(m_instanceproxy);
			} else if (m_resultref != null) {
				try {
					QueryResultSet qset = new QueryResultSet(m_tableid.getCatalog(), m_resultref);
					qset.setRow(m_initialrow);
					model.setInstanceProxy(new DefaultInstanceProxy(model, qset));
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
		iframe.setTitle(m_tableid.getFullyQualifiedName() + " - " + m_connection.getShortId());
		iframe.setShortTitle(m_tableid.getFullyQualifiedName());
	}

	/**
	 * Saves any configuration settings for the current view
	 */
	public void save() {
		if (m_metadata != null)
			m_metadata.saveSettings();
	}

	/**
	 * Sets the initial row in the result set
	 */
	public void setInitialRow(int row) {
		m_initialrow = row;
	}

	/**
	 * This allows the caller to set an initial result set and current instance.
	 * This is currently used by the SQL results window for a single table. It
	 * allows the user to launch the instance view for a different look at the
	 * data.
	 * 
	 * @param ref
	 *            the underlying result set reference
	 * @param currentRow
	 *            the row to make active in the view
	 */
	public void setResults(ResultSetReference ref, int currentRow) {
		m_resultref = ref;
		m_initialrow = currentRow;
	}

	/**
	 * This allows the caller to set an initial instance proxy for the view.
	 * This allows the caller to pass data that the view should display. Note
	 * that this call will override any calls to setResults(ResultSetRef, int).
	 */
	public void setInstanceProxy(InstanceProxy iproxy) {
		m_instanceproxy = iproxy;
	}

	/**
	 * Sets the instance view
	 */
	protected void setView(InstanceView view) {
		m_view = view;
	}
}
