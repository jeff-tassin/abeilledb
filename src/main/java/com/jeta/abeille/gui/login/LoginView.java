package com.jeta.abeille.gui.login;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComboBox;

import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.gui.main.AboutModel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAPanel;

import com.jeta.forms.components.panel.FormPanel;

/**
 * This dialog allows the user to choose a database connection and login. It
 * also allows the user to create a new connection. We use a JFrame here because
 * Windows has problems showing dialogs
 * 
 * @author Jeff Tassin
 */
public class LoginView extends FormPanel {
	/** the data model for this view */
	private LoginModel m_model;

	/**
	 * ctor
	 */
	public LoginView(LoginModel model) {
		super("com/jeta/abeille/gui/login/loginView.jfrm");
		m_model = model;
		loadModel();

		final JComboBox dbbox = getComboBox(LoginNames.ID_CONNECTIONS);
		dbbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				ConnectionInfo info = (ConnectionInfo) dbbox.getSelectedItem();
				if (info != null) {
					setText(LoginNames.ID_USERNAME, m_model.getLastUser(info));
					setText(LoginNames.ID_URL, info.getUrl());
				} else {
					setText(LoginNames.ID_USERNAME, "");
					setText(LoginNames.ID_URL, "");
				}
			}
		});
	}

	/**
	 * @return the data model for this view
	 */
	public LoginModel getModel() {
		return m_model;
	}

	/**
	 * @return the password entered by the user
	 */
	String getPassword() {
		return getText(LoginNames.ID_PASSWORD);
	}

	/**
	 * @return the connection info selected in the databse combo
	 */
	public ConnectionInfo getSelectedConnection() {
		return (ConnectionInfo) getComboBox(LoginNames.ID_CONNECTIONS).getSelectedItem();
	}

	/**
	 * @return the username entered by the user
	 */
	public String getUsername() {
		return getText(LoginNames.ID_USERNAME);
	}

	/**
	 * Loads the data from the model into the view
	 */
	private void loadModel() {
		JComboBox cbox = getComboBox(LoginNames.ID_CONNECTIONS);
		cbox.removeAllItems();
		if (m_model != null) {
			Collection c = m_model.getConnections();
			Iterator iter = c.iterator();
			while (iter.hasNext()) {
				ConnectionInfo info = (ConnectionInfo) iter.next();
				cbox.addItem(info);
			}

			setText(LoginNames.ID_USERNAME, "");
			setText(LoginNames.ID_PASSWORD, "");
			setText(LoginNames.ID_URL, "");

			ConnectionInfo info = m_model.getLastConnection();
			if (info != null) {
				setText(LoginNames.ID_USERNAME, m_model.getLastUser(info));
				cbox.setSelectedItem(info);
				setText(LoginNames.ID_URL, info.getUrl());
			}

			info = (ConnectionInfo) cbox.getSelectedItem();
			if (info != null)
				setText(LoginNames.ID_URL, info.getUrl());

		}

		// AboutModel about = new AboutModel();
		// setText( LoginNames.ID_LICENSEE, about.getLicensee() );
		// setText( LoginNames.ID_SERIALNO, about.getSerialNumber() );
		// setText( LoginNames.ID_VERSION, about.getVersion() );
		// setText( LoginNames.ID_LICENSE, about.getLicenseType() );
	}

	/**
	 * Sets the model for this view
	 */
	public void setModel(LoginModel model) {
		m_model = model;
		loadModel();
	}

	/**
	 * Sets the connection in the combo box
	 */
	public void setSelectedConnection(ConnectionInfo info) {
		if (info != null)
			getComboBox(LoginNames.ID_CONNECTIONS).setSelectedItem(info);
	}

}
