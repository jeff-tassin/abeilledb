/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import org.netbeans.editor.BaseAction;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtKit;

import com.jeta.foundation.gui.components.TSComponentUtils;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.WindowDelegate;

import com.jeta.foundation.gui.editor.macros.Macro;

import com.jeta.open.gui.framework.JETAController;

/**
 * This class defines actions and macros for the frame container for any editor
 * kit. This container should implement the TSWindow interface (e.g. TSFrame,
 * TSDialog, and TSInternalFrame)
 * 
 * @author Jeff Tassin
 */
public class FrameKit {
	public FrameKit() {

	}

	/**
	 * Get the default bindings. We are using a different method for
	 * initializing default bindings than the netbeans callback method.
	 */
	public static MultiKeyBinding[] listDefaultKeyBindings() {
		MultiKeyBinding[] framebindings = new MultiKeyBinding[] {
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK), openFileAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK), newBufferAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), saveFileAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK), closeFileAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK), nextBufferAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), prevBufferAction)

		};

		return framebindings;
	}

	/**
	 * List all actions supported by this class.
	 */
	public static Action[] listDefaultActions() {
		Action[] frameactions = new Action[] { new FrameAction(newBufferAction), new FrameAction(openFileAction),
				new FrameAction(closeFileAction), new FrameAction(saveFileAction), new FrameAction(saveAsAction),
				new FrameAction(nextBufferAction), new FrameAction(prevBufferAction) };
		return frameactions;
	}

	/**
	 * @return the list default macros supported by this kit
	 */
	public static Macro[] listDefaultMacros() {
		Macro[] macros = new Macro[0];
		return macros;
	}

	public static class FrameAction extends BaseAction {
		public FrameAction(String actionName) {
			super(actionName);
		}

		public void actionPerformed(ActionEvent e, JTextComponent textComp) {
			Component parent = TSComponentUtils.getParentFrame(textComp);
			if (parent != null) {
				WindowDelegate container = null;
				if (parent instanceof WindowDelegate) {
					WindowDelegate window = (WindowDelegate) parent;
					TSInternalFrame iframe = window.getTSInternalFrame();
					JETAController controller = iframe.getController();
					if (controller != null) {
						controller.actionPerformed((String) getValue(Action.NAME), e);
					}
				} else if (parent instanceof TSDialog) {
					TSDialog dlg = (TSDialog) parent;
					JETAController controller = dlg.getController();
					if (controller != null) {
						controller.actionPerformed((String) getValue(Action.NAME), e);
					}
				} else {
					assert (false);
				}
			}
		}
	}

	public static final String listBuffersAction = "list-buffers";
	public static final String newBufferAction = "new-buffer";
	public static final String openFileAction = "open-file";
	public static final String closeFileAction = "close-file";
	public static final String saveFileAction = "save-file";
	public static final String saveAsAction = "save-as";
	public static final String nextBufferAction = "next-buffer";
	public static final String prevBufferAction = "prev-buffer";

	public static final String toolbarSwitchBufferAction = "toolbar-switch-to-buffer";
	public static final String toolbarSearchAction = "toolbar-search";
	public static final String toolbarReplaceAction = "toolbar-replace";
	public static final String toolbarGotoAction = "toolbar-goto-line";
	public static final String toolbarRegexReplaceAction = "toolbar-regexreplace";

}
