/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.jeta.open.gui.framework.JETAController;
import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This is the base class for all controllers in the application. We follow the
 * model-view-controller pattern extensively in the UI. All controllers should
 * derive from this class.
 * 
 * 
 * @@@@@@@@@@@@@@@@@@ Note: If we ever move to a framework with this controller,
 *                    we need to solve the problem where a user wants to
 *                    register multiple listeners for the same control.
 *                    Additionally, we need to handle the case where a caller
 *                    wants to replace a given listener with another one.
 * 
 *                    Also, need a way to handle menus and toolbars.
 * 
 * 
 * @author Jeff Tassin
 */
public abstract class TSController extends JETAController {
	public TSController(JETAContainer view) {
		super(view);
	}

	public class DelegateAction extends AbstractAction {
		private String m_cmd;

		public DelegateAction(String commandId) {
			m_cmd = commandId;
		}

		public void actionPerformed(ActionEvent evt) {
			invokeAction(m_cmd);
		}
	}
}
