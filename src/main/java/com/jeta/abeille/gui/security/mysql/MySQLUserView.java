package com.jeta.abeille.gui.security.mysql;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.DbVersion;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Privilege;
import com.jeta.abeille.database.security.User;

import com.jeta.abeille.gui.common.MetaDataTableRenderer;
import com.jeta.abeille.gui.security.UserView;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.layouts.ColumnLayout;

import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.JETATableModel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.mysql.MySQLUser;
import com.jeta.plugins.abeille.mysql.MySQLPrivilege;

import com.jeta.forms.components.panel.FormPanel;

/**
 * This class implements a view for a user in a MySQL database
 * 
 * @author Jeff Tassin
 */
public class MySQLUserView extends UserView {

	/** the user */
	private MySQLUser m_user;

	/** the database connection */
	private TSConnection m_connection;

	private FormPanel m_view;

	/**
	 * ctor
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param user
	 *            the user we are displaying properties for
	 * @param assignGroups
	 *            a list of groups (Group objects) that this user belongs to
	 */
	public MySQLUserView(TSConnection connection, MySQLUser user) {
		m_view = new FormPanel("com/jeta/abeille/gui/security/mysql/mysqlUserView.jfrm");
		setLayout(new BorderLayout());
		add(m_view, BorderLayout.CENTER);
		m_connection = connection;
		m_user = user;

		loadData(user);
	}

	/**
	 * Creates a User object based on the information entered into the dialog
	 */
	public User createUser() {
		MySQLUser user = new MySQLUser(TSUtils.fastTrim(m_view.getText(MySQLUserNames.ID_NAME)),
				TSUtils.fastTrim(m_view.getText(MySQLUserNames.ID_HOST)));

		user.setPassword(m_view.getText(MySQLUserNames.ID_PASSWORD).toCharArray());
		return user;
	}

	/**
	 * @return the user name entered by the user
	 */
	public String getUserName() {
		return TSUtils.fastTrim(m_view.getText(MySQLUserNames.ID_NAME));
	}

	/**
	 * @return the password entered by the user
	 */
	public char[] getPassword() {
		JPasswordField pw = (JPasswordField) m_view.getComponentByName(MySQLUserNames.ID_PASSWORD);
		return pw.getPassword();
	}

	/**
	 * @return the confirmed password entered by the user
	 */
	public char[] getConfirmPassword() {
		JPasswordField pw = (JPasswordField) m_view.getComponentByName(MySQLUserNames.ID_CONFIRM);
		return pw.getPassword();
	}

	/**
	 * Sets the password in the password and confirm password boxes
	 */
	protected void setPassword(char[] password) {
		JPasswordField pw = (JPasswordField) m_view.getComponentByName(MySQLUserNames.ID_PASSWORD);
		JPasswordField confirm = (JPasswordField) m_view.getComponentByName(MySQLUserNames.ID_CONFIRM);
		String str = new String(password);
		pw.setText(str);
		confirm.setText(str);
	}

	/**
	 * @return true if we are creating a new user
	 */
	public boolean isNew() {
		return (m_user == null);
	}

	/**
	 * Initializes the GUI state from the user
	 */
	private void loadData(MySQLUser user) {
		if (user == null)
			return;

		try {
			m_view.setText(MySQLUserNames.ID_NAME, user.getName());
			m_view.setText(MySQLUserNames.ID_HOST, user.getHost());
			m_view.setText(MySQLUserNames.ID_PASSWORD, new String(user.getPassword()));
		} catch (Exception e) {

		}
	}

}
