/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This dialog allows the user to enter a new password It is basically a text
 * field and a checkbox.
 * 
 * @author Jeff Tassin
 */
public class TSPasswordDialog extends TSDialog {
	private JPasswordField m_passwordField;
	private JPasswordField m_confirmField;

	/**
	 * ctor
	 */
	public TSPasswordDialog(java.awt.Dialog owner, boolean bModal) {
		super(owner, bModal);
		setTitle(I18N.getLocalizedMessage("Change Password"));
		initialize();
	}

	/**
	 * @return true if the passwords in the password and confirm fields match
	 */
	public boolean checkPasswords() {
		return I18N.equals(m_passwordField.getText(), m_confirmField.getText());
	}

	/**
	 * ctor
	 */
	public TSPasswordDialog(java.awt.Frame owner, boolean bModal) {
		super(owner, bModal);
		setTitle(I18N.getLocalizedMessage("Change Password"));
		initialize();
	}

	/**
	 * Called when user pressed ok button. Make sure the user has provided a
	 * valid name
	 */
	public void cmdOk() {
		if (checkPasswords())
			super.cmdOk();
		else {
			String msg = I18N.getLocalizedMessage("Passwords Do Not Match");
			String title = I18N.getLocalizedMessage("Error");
			JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Create the main content panel for the dialog
	 */
	protected JPanel createControlsPanel() {
		m_passwordField = new JPasswordField();
		m_confirmField = new JPasswordField();

		JComponent[] controls = new JComponent[2];
		controls[0] = m_passwordField;
		controls[1] = m_confirmField;

		JLabel[] labels = new JLabel[2];
		labels[0] = new JLabel(I18N.getLocalizedMessage("Password"));
		labels[1] = new JLabel(I18N.getLocalizedMessage("Confirm"));

		return TSGuiToolbox.alignLabelTextRows(labels, controls);
	}

	/**
	 * @return the name entered by the user
	 */
	public String getPassword() {
		return m_passwordField.getText();
	}

	/**
	 * @return the preferred size for this dialog so that the caller can size
	 *         the window properly
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(4, 7);
	}

	/**
	 * Creates and initializes the controls for this dialog
	 */
	public void initialize() {
		Container container = this.getDialogContentPanel();
		container.setLayout(new BorderLayout());

		JPanel controlspanel = createControlsPanel();
		controlspanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		container.add(controlspanel, BorderLayout.NORTH);
	}

	/**
	 * Sets the password for the dialog.
	 * 
	 * @param passwd
	 *            the password
	 */
	public void setPassword(String passwd) {
		m_passwordField.setText(passwd);
		m_confirmField.setText(passwd);
	}

}
