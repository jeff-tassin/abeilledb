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
public class TSErrorDialog2 extends TSDialog {
	/** message to be prepended to error */
	private String m_msg;

	/**
	 * ctor
	 */
	public TSErrorDialog2(Dialog owner, boolean bmodal) {
		super(owner, bmodal);
		showCloseLink();
	}

	/**
	 * ctor
	 */
	public TSErrorDialog2(Frame owner, boolean bmodal) {
		super(owner, bmodal);
		showCloseLink();
	}

	public static TSErrorDialog2 createDialog(Component owner, String caption, String errormsg, Throwable e) {
		TSErrorDialog2 dlg = (TSErrorDialog2) TSGuiToolbox.createDialog(TSErrorDialog2.class, owner, true);
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
		setTitle(I18N.getLocalizedMessage("Error"));
		TSErrorPanel2 panel = new TSErrorPanel2();
		panel.initialize(msg, e);
		setPrimaryPanel(panel);
		showErrorIcon(I18N.getLocalizedMessage("Error"));
	}

	/**
	 * Shows an error message 'title' with an error icon at top of panel
	 */
	public void showErrorIcon(String msg) {
		TSErrorPanel2 panel = (TSErrorPanel2) getPrimaryPanel();
		if (panel != null) {
			panel.showErrorIcon(msg);
		}
	}
}
