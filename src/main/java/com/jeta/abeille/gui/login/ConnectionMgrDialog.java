package com.jeta.abeille.gui.login;

import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.Database;

import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.i18n.I18N;

/**
 * This dialog shows the connections that have been defined by the user. The
 * user can edit, create, and delete connections.
 * 
 * @author Jeff Tassin
 */
public class ConnectionMgrDialog extends TSDialog {

	/** the connection mgr */
	private ConnectionMgr m_connectionmgr;

	/** the primary view for this dialog */
	private ConnectionMgrView m_view;

	/**
	 * ctor
	 */
	public ConnectionMgrDialog(java.awt.Frame frame, boolean bmodal) {
		super(frame, bmodal);
		setCloseText(I18N.getLocalizedMessage("Cancel"));
	}

	/**
	 * Close the dialog and set the ok flag
	 * 
	 * @return true if the controllers pass validation
	 */
	public void cmdOk() {
		try {
			m_connectionmgr.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.cmdOk();
	}

	/**
	 * @return the currently selected connection in the list
	 */
	public ConnectionInfo getSelectedConnection() {
		return m_view.getSelectedConnection();
	}

	/**
	 * Initializes the dialog
	 */
	public void initialize(ConnectionMgr mgr, ConnectionInfo model) {
		m_connectionmgr = mgr;
		m_view = new ConnectionMgrView(mgr);
		m_view.setController(new ConnectionMgrViewController(m_view));
		setPrimaryPanel(m_view);

		if (model != null) {
			ConnectionInfo info = m_connectionmgr.getConnection(model.getUID());
			m_view.setConnection(info);
		} else
			m_view.setConnection(null);

		setTitle(I18N.getLocalizedMessage("Manage Connections"));
	}

}
