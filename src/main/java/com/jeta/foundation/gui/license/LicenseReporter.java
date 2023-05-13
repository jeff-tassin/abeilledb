/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.license;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import java.lang.reflect.Method;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.TSEvent;
import com.jeta.foundation.componentmgr.TSListener;

import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class LicenseReporter implements TSListener {
	public LicenseReporter() {

	}

	/**
	 * TSListener implementation
	 */
	public void tsNotify(TSEvent evt) {
		if (LicenseManager.MSG_GROUP.equals(evt.getGroup())) {
			Component owner = getOwner(evt.getSender());
			if (LicenseManager.LICENSE_ERROR.equals(evt.getMessage())) {
				String title = I18N.getLocalizedMessage("Evaluation");
				String msg = (String) evt.getValue();
				javax.swing.JOptionPane.showMessageDialog(owner, msg, title,
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
				ComponentMgr.shutdown();
			} else if (LicenseManager.LICENSE_TIMEOUT_MESSAGE.equals(evt.getMessage())) {
				String title = I18N.getLocalizedMessage("Evaluation_time_limit");
				String msg = I18N.getLocalizedMessage("Evaluation_time_has_expired_for_this_session");
				javax.swing.JOptionPane.showMessageDialog(owner, msg, title,
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
			} else if (LicenseManager.OPEN_WINDOW_LIMIT_MESSAGE.equals(evt.getMessage())) {
				String title = I18N.getLocalizedMessage("Evaluation");
				String msg = I18N.getLocalizedMessage("Open_windows_limited_for_eval_version");
				javax.swing.JOptionPane.showMessageDialog(owner, msg, title,
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
			} else if (LicenseManager.EVALUATION_MESSAGE.equals(evt.getMessage())) {
				String title = I18N.getLocalizedMessage("Evaluation");
				String msg = (String) evt.getValue();
				javax.swing.JOptionPane.showMessageDialog(owner, msg, title,
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
			} else if (LicenseManager.LICENSE_ERROR_VERBOSE.equals(evt.getMessage())) {
				String title = I18N.getLocalizedMessage("Evaluation");
				String msg = (String) evt.getValue();

				TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, owner, true);
				dlg.initialize(msg);
				dlg.setTitle(title);
				dlg.setSize(dlg.getPreferredSize());
				dlg.getOkButton().setVisible(false);
				dlg.showCenter();
				ComponentMgr.shutdown();
			}
		}
	}

	private Component getWindow(Component owner) {
		if (owner instanceof Dialog) {
			return owner;
		} else if (owner instanceof Frame) {
			return owner;
		} else {
			if (owner != null) {
				owner = javax.swing.SwingUtilities.getWindowAncestor(owner);
			}

			if (owner instanceof Dialog || owner instanceof Frame)
				return owner;
		}
		return TSWorkspaceFrame.getInstance();
	}

	private Component getOwner(Object sender) {
		if (sender instanceof TSInternalFrame)
			return getWindow(((TSInternalFrame) sender).getDelegate());
		else if (sender instanceof Component)
			return getWindow((Component) sender);
		else
			return TSWorkspaceFrame.getInstance();
	}

}
