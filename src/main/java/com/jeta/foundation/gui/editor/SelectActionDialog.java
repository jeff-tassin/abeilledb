/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.Collection;
import java.util.Iterator;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import org.netbeans.editor.MultiKeyBinding;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This dialog basically allows the user to select an action name. Once the
 * action is selected, a new entry is created in the KeyBindingsModel for that
 * action. The user can then assign whatever keystrokes to that action. This
 * allows the user to have multiple key bindings for the same action. (e.g.
 * delete-char has VK_DELETE as well as CTRL+D on emacs )
 * 
 * @author Jeff Tassin
 */
public class SelectActionDialog extends TSDialog {
	private TSComboBox m_actionscombo;

	public SelectActionDialog(Dialog owner, boolean bmodal) {
		super(owner, bmodal);
	}

	/**
	 * Override ok button. Make sure that the end user has selected a valid
	 * username.
	 */
	public void cmdOk() {

		if (!m_actionscombo.isValueInModel()) {
			String msg = I18N.getLocalizedMessage("Invalid Action Name");
			String title = I18N.getLocalizedMessage("Error");
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
		} else
			super.cmdOk();
	}

	/**
	 * @return the selected action name
	 */
	public String getActionName() {
		return m_actionscombo.getText();
	}

	/**
	 * Creates the components used on this dialog Currently, it is just a combo
	 * box that has all action names for the given model.
	 * 
	 * @param actionNames
	 *            the collection of actions (String objects) to display in the
	 *            combo
	 */
	public void initialize(Collection actionNames) {
		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedMessage("Action Name"));

		m_actionscombo = new TSComboBox();
		JComponent[] components = new JComponent[1];
		components[0] = m_actionscombo;

		JPanel panel = TSGuiToolbox.alignLabelTextRows(labels, components);

		// set panel preferred size based on JLabel
		Dimension d = labels[0].getPreferredSize();
		d.width *= 3;
		d.height *= 3;
		panel.setPreferredSize(d);

		// now populate the combo list with all users in system
		try {
			PopupList list = m_actionscombo.getPopupList();
			SortedListModel listmodel = new SortedListModel();

			Iterator iter = actionNames.iterator();
			while (iter.hasNext()) {
				String actionname = (String) iter.next();
				listmodel.add(actionname);
			}
			list.setModel(listmodel);

		} catch (Exception se) {
			se.printStackTrace();
		}
		setPrimaryPanel(panel);
		setInitialFocusComponent(m_actionscombo);
	}
}
