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
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This dialog allows the user to enter a username/password It is basically a
 * text field and a checkbox.
 * 
 * @author Jeff Tassin
 */
public class TSUsernameDialog extends TSDialog {
	private JTextField m_usernameField;
	private JPasswordField m_passwordField;

	/**
	 * ctor
	 */
	public TSUsernameDialog(java.awt.Dialog owner, boolean bModal) {
		super(owner, bModal);
		setTitle(I18N.getLocalizedMessage("Authorization"));
		initialize();
	}

	/**
	 * ctor
	 */
	public TSUsernameDialog(java.awt.Frame owner, boolean bModal) {
		super(owner, bModal);
		setTitle(I18N.getLocalizedMessage("Authorization"));
		initialize();
	}

	/**
	 * Create the main content panel for the dialog
	 */
	protected JPanel createControlsPanel() {
		m_usernameField = new JTextField();
		m_passwordField = new JPasswordField();

		JComponent[] controls = new JComponent[2];
		controls[0] = m_usernameField;
		controls[1] = m_passwordField;

		JLabel[] labels = new JLabel[2];
		labels[0] = new JLabel(I18N.getLocalizedMessage("Name"));
		labels[1] = new JLabel(I18N.getLocalizedMessage("Password"));

		return TSGuiToolbox.alignLabelTextRows(labels, controls);
	}

	/**
	 * @return the name entered by the user
	 */
	public char[] getPassword() {
		return m_passwordField.getPassword();
	}

	public String getUsername() {
		return m_usernameField.getText();
	}

	/**
	 * @return the preferred size for this dialog so that the caller can size
	 *         the window properly
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(4, 6);
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

}
