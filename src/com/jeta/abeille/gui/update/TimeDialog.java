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
 * This dialog allows the user to edit TIME data types in a database.
 * 
 * @author Jeff Tassin
 */
public class TimeDialog extends TSDialog {
	// private JTimeField m_timeField;
	private TSTimePanel m_timeField;

	public TimeDialog(Frame owner, boolean bmodal) {
		super(owner, bmodal);
		initialize();
	}

	/**
	 * Creates the controls for this dialog
	 * 
	 * @return a panel that contains the controls
	 */
	private JPanel createControlsPanel() {
		m_timeField = new TSTimePanel();

		JComponent[] controls = new JComponent[1];
		controls[0] = m_timeField;

		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedMessage("Time#"));

		ControlsAlignLayout layout = new ControlsAlignLayout();
		JPanel tpanel = TSGuiToolbox.alignLabelTextRows(labels, controls);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(tpanel, BorderLayout.NORTH);
		return panel;
	}

	/**
	 * @return the selected hour Always in 24 hour format
	 */
	public int getHours() {
		return m_timeField.getHours();
	}

	/**
	 * @return the selected minute
	 */
	public int getMinutes() {
		return m_timeField.getMinutes();
	}

	/**
	 * @return the selected seconds
	 */
	public int getSeconds() {
		return m_timeField.getSeconds();
	}

	/**
	 * Creates and initializes the controls on this dialog
	 */
	private void initialize() {
		Container container = this.getDialogContentPanel();
		container.setLayout(new BorderLayout());

		JPanel controls = createControlsPanel();
		container.add(createControlsPanel(), BorderLayout.CENTER);
		Container btnpanel = getButtonPanel().getParent();
		Dimension d = btnpanel.getPreferredSize();
		this.setSize(container.getPreferredSize().width + 30, container.getPreferredSize().height + d.height / 2 + 50);

	}
}
