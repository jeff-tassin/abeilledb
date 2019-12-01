package com.jeta.abeille.gui.security;

import java.util.Collection;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jeta.abeille.database.security.User;

import com.jeta.foundation.gui.components.TSPanel;

public abstract class UserView extends TSPanel {

	public UserView() {

	}

	/**
	 * @return the user name entered by the user
	 */
	public abstract String getUserName();

	/**
	 * @return the password entered by the user
	 */
	public abstract char[] getPassword();

	/**
	 * @return the confirmed password entered by the user
	 */
	public abstract char[] getConfirmPassword();

	public abstract User createUser();

	/**
	 * @return the groups that this user belongs to. We allow the user to be
	 *         assigned to groups in the UserView. Not all databases support
	 *         groups, so null is returned for those databases.
	 */
	public Collection getGroups() {
		return null;
	}

	/**
	 * Sets the password in the password and confirm password boxes
	 */
	protected abstract void setPassword(char[] password);

}
