package com.jeta.abeille.gui.model.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.foundation.gui.print.PrintUtils;

/**
 * Invokes the page setup dialog
 */
public class PageSetupAction implements ActionListener {
	public void actionPerformed(ActionEvent evt) {
		PrintUtils.showSetupDialog();
	}
}
