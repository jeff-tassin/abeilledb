package com.jeta.abeille.gui.login;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import com.jeta.abeille.database.model.ConnectionInfo;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.gui.components.TSComponentUtils;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETADialogListener;

/**
 * This dialog allows the user to choose a database connection and login. It
 * also allows the user to create a new connection.
 * 
 * @author Jeff Tassin
 */
public class LoginDialog extends TSDialog {
	/**
	 * The main view for this frame
	 */
	private LoginView m_view;

	/** the login model */
	private ConnectionMgr m_model;

	/**
	 * ctor
	 */
	public LoginDialog(java.awt.Frame frame, boolean bmodal) {
		super(frame, bmodal);
	}

	/**
	 * Initializes the dialog
	 */
	public void initialize(ConnectionMgr model) {
		m_model = model;
		m_view = new LoginView(new LoginModel(m_model));
		setOkText(I18N.getLocalizedMessage("Login"));

		final LoginController controller = new LoginController(m_view);
		addDialogListener(new JETADialogListener() {
			public boolean cmdOk() {
				return controller.login();
			}
		});

		setPrimaryPanel(m_view);
	}

	/**
	 * @return the main view for the application
	 */
	public LoginView getView() {
		return m_view;
	}

	public void setSelectedConnection(ConnectionInfo info) {
		m_view.setSelectedConnection(info);
	}
}
