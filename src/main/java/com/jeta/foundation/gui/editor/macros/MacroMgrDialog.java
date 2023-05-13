package com.jeta.foundation.gui.editor.macros;

import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.JButton;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.editor.KitSet;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This dialog allows a user to assign a keystroke to an action
 * 
 * @author Jeff Tassin
 */
public class MacroMgrDialog extends TSDialog {
	private MacroMgrPanel m_macropanel;
	private MacroMgrController m_controller;

	/**
	 * MacroMgrDialog constructor
	 */
	public MacroMgrDialog(Dialog owner, boolean bModal) {
		super(owner, bModal);

	}

	/**
	 * @return the main panel for this dialog
	 */
	public MacroMgrPanel getPanel() {
		return m_macropanel;
	}

	public Dimension getPreferredSize() {
		return new Dimension(600, 400);
	}

	/**
	 * Creates and initializes the controls for this dialog
	 * 
	 * @param kitClass
	 *            the editor kit whose macros we want to edit
	 */
	public void initialize(MacroMgrModel model) {
		m_macropanel = new MacroMgrPanel(model);
		m_controller = new MacroMgrController(m_macropanel);
		m_macropanel.setController(m_controller);
		setPrimaryPanel(m_macropanel);
		setTitle(I18N.getLocalizedMessage("Macro Manager"));
	}
}
