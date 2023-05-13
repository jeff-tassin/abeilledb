/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class is used to display a exception. It provides a text area to display
 * the exception information
 * 
 * @author Jeff Tassin
 */
public class TSErrorPanel extends TSPanel {
	private JLabel m_error_label;

	/**
	 * ctor
	 */
	public TSErrorPanel() {
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(6, 8);
	}

	/**
	 * Initializes the panel with the given data.
	 * 
	 * @param msg
	 *            a message to include with the exception msg
	 * @param e
	 *            the exception that was thrown. The error message of this
	 *            exception will be displayed.
	 */
	public void initialize(String msg, Throwable e) {
		StringBuffer msgbuff = new StringBuffer();
		msgbuff.append(e.getClass().getName());
		msgbuff.append("\n");

		String errormsg = e.getLocalizedMessage();
		if (errormsg == null || errormsg.length() == 0)
			errormsg = e.getMessage();

		if (errormsg != null) {
			int pos = errormsg.indexOf("Stack Trace");
			if (pos >= 0)
				errormsg = errormsg.substring(0, pos);
		}

		if (msg != null) {
			msgbuff.append(msg);
			msgbuff.append("\n");
		}

		msgbuff.append(errormsg);
		initialize(msgbuff.toString());
	}

	/**
	 * Initializes the panel with the given data.
	 * 
	 * @param msg
	 *            a message to display
	 */
	public void initialize(String msg) {
		setLayout(new BorderLayout());

		JTextArea msgcomp = TSEditorUtils.createTextArea();
		msgcomp.setEditable(false);
		msgcomp.setLineWrap(true);
		msgcomp.setWrapStyleWord(true);
		msgcomp.setText(msg);
		JScrollPane msgpane = new JScrollPane(msgcomp);

		add(msgpane, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		try {
			msgcomp.setCaretPosition(0);
		} catch (Exception e) {

		}
	}

	/**
	 * Shows an error message 'title' with an error icon at top of panel
	 */
	public void showErrorIcon(String msg) {
		if (m_error_label == null) {
			m_error_label = new JLabel(msg);
			m_error_label.setIcon(javax.swing.UIManager.getIcon("OptionPane.errorIcon"));
			m_error_label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));
			add(m_error_label, BorderLayout.NORTH);
		} else {
			m_error_label.setText(msg);
		}
	}

}
