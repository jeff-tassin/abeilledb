package com.jeta.abeille.gui.security.hsqldb;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.User;
import com.jeta.abeille.gui.security.UserView;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.plugins.abeille.hsqldb.HSQLUser;

/**
 * This class implements a view for a user in the database
 * 
 * @author Jeff Tassin
 */
public class HSQLUserView extends UserView {

	/** the user */
	private HSQLUser m_user;

	/** the database connection */
	private TSConnection m_connection;

	private JTextField m_namefield = new JTextField();
	private JPasswordField m_password = new JPasswordField();
	private JPasswordField m_confirmpassword = new JPasswordField();

	private JCheckBox m_admincheck = new JCheckBox(I18N.getLocalizedMessage("Administrator"));

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
	public HSQLUserView(TSConnection connection, User user) {
		m_connection = connection;
		m_user = (HSQLUser) user;

		setLayout(new BorderLayout());
		add(createTextFieldsPanel(), BorderLayout.NORTH);
		loadData();
	}

	/**
	 * Creates the topmost panel
	 */
	private JComponent createTextFieldsPanel() {
		Component[] left = new Component[5];
		left[0] = new JLabel(I18N.getLocalizedMessage("Name"));
		left[1] = new JLabel(I18N.getLocalizedMessage("Password"));
		left[2] = new JLabel(I18N.getLocalizedMessage("Confirm"));
		left[3] = Box.createVerticalStrut(10);
		left[4] = Box.createVerticalStrut(10);

		Component[] right = new Component[5];
		right[0] = getNameField();
		right[1] = getPasswordField();
		right[2] = getConfirmPasswordField();
		right[3] = Box.createVerticalStrut(10);
		right[4] = m_admincheck;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(getNameField(), 30);
		layout.setMaxTextFieldWidth(getPasswordField(), 30);
		layout.setMaxTextFieldWidth(getConfirmPasswordField(), 30);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, left, right, new java.awt.Insets(10, 10, 0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * Creates a User object based on the information entered into the dialog
	 */
	public User createUser() {
		HSQLUser user = new HSQLUser(getUserName());
		user.setPassword(getPassword());
		user.setAdministrator(m_admincheck.isSelected());
		return user;
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 10);
	}

	public JTextField getNameField() {
		return m_namefield;
	}

	public JPasswordField getConfirmPasswordField() {
		return m_confirmpassword;
	}

	public JPasswordField getPasswordField() {
		return m_password;
	}

	/**
	 * @return the user name entered by the user
	 */
	public String getUserName() {
		return m_namefield.getText();
	}

	/**
	 * @return the password entered by the user
	 */
	public char[] getPassword() {
		return m_password.getPassword();
	}

	/**
	 * @return the confirmed password entered by the user
	 */
	public char[] getConfirmPassword() {
		return m_confirmpassword.getPassword();
	}

	/**
	 * Sets the password in the password and confirm password boxes
	 */
	protected void setPassword(char[] password) {
		if (password == null) {
			m_password.setText("");
			m_confirmpassword.setText("");
		} else {
			m_password.setText(String.valueOf(password));
			m_confirmpassword.setText(String.valueOf(password));
		}
	}

	/**
	 * @return true if we are creating a new user
	 */
	public boolean isNew() {
		return (m_user == null);
	}

	/**
	 * Initializes the GUI state from the user and groups data
	 */
	private void loadData() {
		if (m_user != null) {
			getNameField().setEnabled(false);
			getNameField().setText(m_user.getName());

			setPassword(m_user.getPassword());

			if (m_user.isAdministrator()) {
				m_admincheck.setSelected(true);
			}

			getPasswordField().setEnabled(false);
			getConfirmPasswordField().setEnabled(false);
		}
	}

}
