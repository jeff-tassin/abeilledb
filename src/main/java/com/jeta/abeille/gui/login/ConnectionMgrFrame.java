package com.jeta.abeille.gui.login;

import java.awt.BorderLayout;
import javax.swing.ImageIcon;

import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.Database;

import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This dialog shows the connections that have been defined by the user. The
 * user can edit, create, and delete connections.
 * 
 * @author Jeff Tassin
 */
public class ConnectionMgrFrame extends TSInternalFrame {

	/** the connection mgr */
	private ConnectionMgr m_connectionmgr = new ConnectionMgr();

	/** the primary view for this dialog */
	private ConnectionMgrView m_view;

	/** the frame icon for this frame */
	public static ImageIcon FRAME_ICON;

	static {
		FRAME_ICON = TSGuiToolbox.loadImage("incors/16x16/server_client.png");
	}

	/**
	 * ctor
	 */
	public ConnectionMgrFrame() {
		super(I18N.getLocalizedMessage("Connection Manager"));
		setTitle(I18N.getLocalizedMessage("Connection Manager"));
		setShortTitle(I18N.getLocalizedMessage("Connection Manager"));

		setFrameIcon(FRAME_ICON);

		initialize(m_connectionmgr, null);
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

		setController(new ConnectionMgrViewController(m_view));
		UIDirector uidirector = new ConnectionMgrUIDirector(m_view);
		setUIDirector(uidirector);

		uidirector.updateComponents(null);

		getContentPane().add(m_view, BorderLayout.CENTER);

		if (model != null) {
			ConnectionInfo info = m_connectionmgr.getConnection(model.getUID());
			m_view.setConnection(info);
		}
		// else
		// m_view.setConnection( null );

	}

}
