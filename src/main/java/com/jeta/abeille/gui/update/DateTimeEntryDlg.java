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
 * This dialog allows the user to edit TIMESTAMP data types in a database.
 * 
 * @author Jeff Tassin
 */
public class DateTimeEntryDlg extends TSDialog {
	// private JTimeField m_timeField;

	public DateTimeEntryDlg(Frame owner) {
		super(owner, true);
		initialize();
	}

	public void cmdOk() {
		if (validateOk())
			setVisible(false);
	}

	/**
	 * Creates the controls for this dialog
	 * 
	 * @return a panel that contains the controls
	 */
	private JPanel createControlsPanel() {
		// m_timeField = new JTimeField();

		JComponent[] controls = new JComponent[1];
		// controls[0] = m_timeField;

		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedMessage("Time"));

		ControlsAlignLayout layout = new ControlsAlignLayout();
		JPanel tpanel = TSGuiToolbox.alignLabelTextRows(labels, controls);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new CalendarWidget(), BorderLayout.CENTER);
		panel.add(tpanel, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * Creates and initializes the controls on this dialog
	 */
	private void initialize() {
		Container container = this.getDialogContentPanel();
		container.setLayout(new BorderLayout());
		container.add(createControlsPanel(), BorderLayout.CENTER);
	}

	/**
	 * Checks input values.
	 * 
	 * @return false if the input values are invalid.
	 */
	private boolean validateOk() {
		return true;
	}
}
