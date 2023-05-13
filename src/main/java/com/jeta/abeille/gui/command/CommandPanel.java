package com.jeta.abeille.gui.command;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.ColumnLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class CommandPanel extends TSPanel {
	// checkbox id
	public static final String ID_CANCEL_BOX = "commandpanel.cancel.component";

	/**
	 * ctor
	 * 
	 * @param msg
	 *            the message to display in the panel
	 */
	public CommandPanel(String msg) {
		initialize(msg);
	}

	/**
	 * Initializes this panel
	 * 
	 * @param msg
	 *            the message to set in the panel
	 */
	private void initialize(String msg) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JCheckBox cancelbox = TSGuiToolbox.createCheckBox(I18N.getLocalizedMessage("cancel_this_request"));
		cancelbox.setName(ID_CANCEL_BOX);

		JPanel mainpanel = new JPanel();
		mainpanel.setLayout(new ColumnLayout());
		mainpanel.add(new JLabel(msg));
		mainpanel.add(javax.swing.Box.createVerticalStrut(10));
		mainpanel.add(cancelbox);

		add(mainpanel, BorderLayout.NORTH);
	}
}
