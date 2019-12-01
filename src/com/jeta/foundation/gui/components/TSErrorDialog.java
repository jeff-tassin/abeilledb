/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

/**
 * This is a utility dialog that can be used to display an Exception.
 * 
 * @author Jeff Tassin
 */
public class TSErrorDialog extends TSDialog {
	/** message to be prepended to error */
	private String m_msg;

	/**
	 * ctor
	 */
	public TSErrorDialog(Dialog owner, boolean bmodal) {
		super(owner, bmodal);
		showCloseLink();
	}

	/**
	 * ctor
	 */
	public TSErrorDialog(Frame owner, boolean bmodal) {
		super(owner, bmodal);
		showCloseLink();
	}

	public static TSErrorDialog createDialog(String msg) {
		TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class,
				TSWorkspaceFrame.getInstance(), true);
		dlg.setTitle(I18N.getLocalizedMessage("Error"));
		dlg.initialize(msg);
		dlg.setSize(dlg.getPreferredSize());
		return dlg;
	}

	public static TSErrorDialog createDialog(Component owner, String msg) {
		TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, owner, true);
		dlg.setTitle(I18N.getLocalizedMessage("Error"));
		dlg.initialize(msg);
		dlg.setSize(dlg.getPreferredSize());
		return dlg;
	}

	public static TSErrorDialog createDialog(String msg, Throwable e) {
		TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class,
				TSWorkspaceFrame.getInstance(), true);
		dlg.setTitle(I18N.getLocalizedMessage("Error"));
		dlg.initialize(msg, e);
		dlg.setSize(dlg.getPreferredSize());
		return dlg;
	}

	public static TSErrorDialog createDialog(Component owner, String caption, String errormsg, Throwable e) {
		TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, owner, true);
		dlg.setTitle(I18N.getLocalizedMessage("Error"));
		dlg.initialize(errormsg, e);
		if (caption != null)
			dlg.showErrorIcon(caption);

		dlg.setSize(dlg.getPreferredSize());
		return dlg;
	}

	/**
	 * Initializes the dialog with the exception
	 */
	public void initialize(String msg, Throwable e) {
		if (TSUtils.isDebug() && e != null) {
			// e.printStackTrace();
		}
		setTitle(I18N.getLocalizedMessage("Error"));

		TSErrorPanel panel = new TSErrorPanel();
		panel.initialize(msg, e);
		setPrimaryPanel(panel);
		showErrorIcon(I18N.getLocalizedMessage("Error"));

	}

	/**
	 * Initializes the dialog with the exception
	 */
	public void initialize(String msg) {
		if (TSUtils.isDebug()) {
			// Exception e = new Exception();
			// e.printStackTrace();
		}
		setTitle(I18N.getLocalizedMessage("Error"));
		TSErrorPanel panel = new TSErrorPanel();
		panel.initialize(msg);
		setPrimaryPanel(panel);
		showErrorIcon(I18N.getLocalizedMessage("Error"));
	}

	/**
	 * Shows an error message 'title' with an error icon at top of panel
	 */
	public void showErrorIcon(String msg) {
		TSErrorPanel panel = (TSErrorPanel) getPrimaryPanel();
		if (panel != null) {
			panel.showErrorIcon(msg);
		}
	}
}
