package com.jeta.abeille.gui.sql;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import com.jeta.foundation.gui.utils.*;
import com.jeta.foundation.gui.components.*;

import com.jeta.foundation.i18n.I18N;

import com.jeta.foundation.interfaces.userprops.*;

/**
 * This is a panel that allows the user to modify properties for the SQL window
 * 
 * @author Jeff Tassin
 */
public class SQLPropertiesPanel extends TSPanel {
	private JTextField m_sqlhistory; // the number of sql queries we store in
										// the history buffer
	private JCheckBox m_reusewindow; // true if we want the sql results to be in
										// the same window for every query
	private TSUserProperties m_model; // the model for our dialog
	private JLabel m_historylabel; // use this to size the panel

	public SQLPropertiesPanel(TSUserProperties model) {
		m_model = model;
		initialize();
	}

	/**
	 * Creates the controls for the panel and gets the initial values from the
	 * model
	 */
	private void initialize() {
		setLayout(new BorderLayout());
		m_sqlhistory = new TSNumericTextField();
		m_reusewindow = new JCheckBox();

		JComponent[] controls = new JComponent[2];
		controls[0] = m_sqlhistory;
		controls[1] = m_reusewindow;
		// m_keybindingpanel = new KeyBindingComboPanel();
		// controls[2] = m_keybindingpanel;

		JLabel[] labels = new JLabel[3];
		labels[0] = new JLabel(I18N.getLocalizedMessage("SQL History"));
		labels[1] = new JLabel(I18N.getLocalizedMessage("Key Bindings"));
		m_historylabel = labels[0];

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_sqlhistory, 5);
		add(TSGuiToolbox.alignLabelTextRows(layout, labels, controls), BorderLayout.CENTER);

		// set the initial values
		m_sqlhistory.setText(m_model.getProperty(SQLSettingsNames.SQLHISTORY, SQLDefaultSettings.SQLHISTORY));
		// m_reusewindow.setSelected( Boolean.valueOf(m_model.getProperty(
		// SQLSettingsNames.REUSERESULTS, SQLDefaultSettings.REUSERESULTS )
		// ).booleanValue() );

	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		Dimension d = m_historylabel.getPreferredSize();
		d.height *= 7;
		d.width *= 4;
		return d;
	}

	/**
	 * @return the name of the key binding selected by the user
	 */
	public String getSelectedBinding() {
		// return m_keybindingpanel.getSelectedBinding();
		return null;
	}

	/**
	 * Saves the control values to the model
	 */
	public void saveSettings() {
		m_model.setProperty(SQLSettingsNames.SQLHISTORY, m_sqlhistory.getText());
		// m_model.setProperty( SQLSettingsNames.REUSERESULTS, String.valueOf(
		// m_reusewindow.isSelected() ) );
	}

}
