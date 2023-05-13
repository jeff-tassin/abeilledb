package com.jeta.abeille.gui.main;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.login.LoginController;
import com.jeta.abeille.gui.login.LoginDialog;
import com.jeta.abeille.gui.login.LoginView;

import com.jeta.abeille.gui.store.ConnectionContext;

import com.jeta.foundation.i18n.I18N;

public class MainFrameLoginController extends LoginController {
	/** the main frame window */
	private MainFrame m_frame;

	public MainFrameLoginController(MainFrame frame, LoginView view) {
		super(view);
		m_frame = frame;
	}

	/**
	 * Called by the LoginAction. Check that the given connection is not
	 * currently opened in the main frame
	 */
	protected boolean validate(ConnectionInfo info) {
		if (info == null) {
			return false;
		} else {
			// now make sure the user has not selected a connection that is
			// already opened
			Collection conns = m_frame.getOpenedConnections();
			Iterator iter = conns.iterator();
			while (iter.hasNext()) {
				ConnectionContext cc = (ConnectionContext) iter.next();
				TSConnection con = cc.getConnection();
				if (info.equals(con.getConnectionInfo())) {
					String msg = I18N.format("Connection_already_opened_1", con.getName());
					String error = I18N.getLocalizedMessage("Error");
					JOptionPane.showMessageDialog(null, msg, error, JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		return true;
	}

}
