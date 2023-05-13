package com.jeta.abeille.gui.sql;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.componentmgr.ComponentMgr;

/**
 * This dialog allows a user to assign a keystroke to a macro
 * 
 * @author Jeff Tassin
 */
public class UserPropertiesDialog extends TSDialog {
	private SQLPropertiesPanel m_controlspanel; // the panel that contains the
												// controls for this dialog

	/**
	 * UserPropertiesDialog constructor
	 */
	public UserPropertiesDialog(Frame owner, boolean bModal) {
		super(owner, bModal);
		initialize();
	}

	/**
	 * Create the main content panel for the dialog
	 */
	protected SQLPropertiesPanel createControlsPanel() {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);

		m_controlspanel = new SQLPropertiesPanel(userprops);
		m_controlspanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
		return m_controlspanel;
	}

	/**
	 * @return the content panel for this dialog
	 */
	public SQLPropertiesPanel getPropertiesPanel() {
		return m_controlspanel;
	}

	/**
	 * Creates and initializes the controls for this dialog
	 */
	private void initialize() {
		m_controlspanel = createControlsPanel();
		setPrimaryPanel(m_controlspanel);
	}

}
