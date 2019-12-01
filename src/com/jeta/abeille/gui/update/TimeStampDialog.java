package com.jeta.abeille.gui.update;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import com.jeta.foundation.gui.utils.*;
import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.calendar.*;

import com.jeta.abeille.database.utils.*;
import com.jeta.abeille.database.model.*;

/**
 * This dialog allows the user to edit TIMESTAMP data types in a database. I has
 * a calendar, time element, and nanonseconds field
 * 
 * [ calendar widget ] [ time element][nanosecs field]
 * 
 * @author Jeff Tassin
 */
public class TimeStampDialog extends TSDialog {

	public TimeStampDialog(Frame owner, boolean bmodal) {
		super(owner, bmodal);
	}

	/**
	 * Creates the controls for this dialog
	 * 
	 * @return a panel that contains the controls
	 */
	private JPanel createControlsPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		return panel;
	}

	/**
	 * Creates and initializes the controls on this dialog
	 */
	private void initialize() {
		Container container = this.getDialogContentPanel();
		container.setLayout(new BorderLayout());
		container.add(createControlsPanel(), BorderLayout.CENTER);
		// let's get the button panel height and use it as a basis to pad the
		// overall height of this frame
		Container btnpanel = getButtonPanel().getParent();
		Dimension d = btnpanel.getPreferredSize();
		this.setSize(container.getPreferredSize().width + 24, container.getPreferredSize().height + d.height + 50);
	}

}
