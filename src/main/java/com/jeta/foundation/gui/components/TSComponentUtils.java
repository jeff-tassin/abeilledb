/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JDialog;

import com.jeta.foundation.i18n.I18N;

/**
 * This class contains utility methods for our foundation framework. Currently,
 * it is used as a factory to create common TS components.
 * 
 * @author Jeff Tassin
 */
public class TSComponentUtils {

	/**
	 * @return the thickness (in pixels) of the border on a frame or dialog
	 *         window Currently, this is hard coded until I figure out a way to
	 *         get this value.
	 */
	public static int getFrameBorderThickness() {
		return 4; // temporary
	}

	/**
	 * @return the parent frame window of the given component. If the component
	 *         is contained by a JInternalFrame, then that internal frame is
	 *         returned, not the top level frame window
	 */
	public static Component getParentFrame(JComponent comp) {
		Component parent = comp.getParent();
		while (parent != null) {
			if (parent instanceof JInternalFrame || parent instanceof JFrame || parent instanceof JDialog
					|| parent instanceof JPanelFrame)
				return parent;

			parent = parent.getParent();
		}

		return null;
	}

	/**
	 * @return the parent frame for this component. If this component is a
	 *         JFrame, JDialog or JInternalFrame, then the component is returned
	 */
	public static Component getFrame(JComponent jcomp) {
		Component comp = jcomp;
		if ((comp instanceof JInternalFrame) || (comp instanceof JFrame) || (comp instanceof JDialog))
			return comp;
		else
			return getParentFrame(jcomp);
	}

	/**
	 * @return the height (in pixels) of the titlebar on a frame or dialog
	 *         window Currently, this is hard coded until I figure out a way to
	 *         get this value.
	 */
	public static int getTitleBarHeight() {
		return 20;
	}

	/**
	 * Helper method that shows a dialog on the string with the exception error
	 * message
	 */
	public static void showErrorMessage(Exception e) {
		String msg = e.getLocalizedMessage();
		String title = I18N.getLocalizedMessage("Error");
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
	}
}
