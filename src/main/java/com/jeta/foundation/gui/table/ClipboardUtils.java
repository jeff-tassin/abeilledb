/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.TableLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This is a utility class for the clipboard package
 * 
 * @author Jeff Tassin
 */
public class ClipboardUtils {

	/**
	 * @return the components for the show headers options
	 */
	public static JComponent createShowHeadersComponents(boolean bshow) {
		// final TSPanel panel = new TSPanel( new FlowLayout( FlowLayout.LEFT,
		// 0, 0 ) );
		final TSPanel panel = new TSPanel();
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.X_AXIS));

		final JCheckBox showheaders = TSGuiToolbox.createCheckBox(I18N.getLocalizedMessage("Show"));
		showheaders.setSelected(bshow);
		showheaders.setName(ClipboardNames.ID_SHOWHEADERS_CHECKBOX);

		final JLabel delimlabel = new JLabel(I18N.getLocalizedDialogLabel("Delimiter"));
		delimlabel.setName(ClipboardNames.ID_DELIMITER_LABEL);

		JComponent comp = ClipboardUtils.createDelimiterComponent(ClipboardNames.ID_COLUMN_HEADER_DELIMITER);

		panel.add(showheaders);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(delimlabel);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(comp);

		showheaders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				TSComboBox combo = (TSComboBox) panel.getComponentByName(ClipboardNames.ID_COLUMN_HEADER_DELIMITER);
				combo.setVisible(showheaders.isSelected());
				delimlabel.setVisible(showheaders.isSelected());
			}
		});

		if (!bshow) {
			TSComboBox combo = (TSComboBox) panel.getComponentByName(ClipboardNames.ID_COLUMN_HEADER_DELIMITER);
			combo.setVisible(false);
			delimlabel.setVisible(false);
		}
		return panel;
	}

	/**
	 * Creates a set of radio buttons that the user can select how values in a
	 * result set are stored. Currently, we only support commas and tabs
	 * 
	 * @return the resulting container that has the radio buttons
	 */
	public static JComponent createDelimiterComponent(String compName) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		TSComboBox cbox = new DelimComboBox();

		// Dimension d = cbox.getPreferredSize();
		// d.width = 100;
		// cbox.setMinimumSize( d );
		// cbox.setPreferredSize(d );

		cbox.addItem(DelimComboBox.COMMA);
		cbox.addItem(DelimComboBox.TAB);
		cbox.addItem(DelimComboBox.SPACE);

		cbox.setSelectedItem(DelimComboBox.COMMA);
		cbox.setName(compName);

		panel.add(cbox);
		return panel;
	}

	/**
	 * Creates a set of option components that the user can select how values in
	 * a result set are stored.
	 * 
	 * @return the resulting options container
	 */
	public static JComponent createValueComponent(String delimName, String nullsName) {

		/** 3 rows x 6 columns */
		double size[][] = { { TableLayout.PREFERRED, 30, TableLayout.PREFERRED, 5, TableLayout.PREFERRED },
				{ TableLayout.PREFERRED } };

		JPanel panel = new JPanel();
		panel.setLayout(new TableLayout(size));

		TSComboBox cbox = new DelimComboBox();

		cbox.addItem(DelimComboBox.COMMA);
		cbox.addItem(DelimComboBox.TAB);
		cbox.addItem(DelimComboBox.SPACE);
		cbox.setSelectedItem(DelimComboBox.COMMA);
		cbox.setName(delimName);

		panel.add(cbox, "0,0");
		panel.add(new JLabel(I18N.getLocalizedDialogLabel("Nulls")), "2,0");
		JTextField nulls_field = new JTextField(8);
		nulls_field.setName(nullsName);
		nulls_field.setText("NULL");
		panel.add(nulls_field, "4,0");
		return panel;
	}

	/**
	 * Creates a set of option components that the user can select how values in
	 * a result set are stored.
	 * 
	 * @return the resulting options container
	 */
	private static JComponent createValueComponent2(String delimName, String nullsName) {

		// JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
		JPanel panel = new JPanel();
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.X_AXIS));
		TSComboBox cbox = new DelimComboBox();

		// Dimension d = cbox.getPreferredSize();

		cbox.addItem(DelimComboBox.COMMA);
		cbox.addItem(DelimComboBox.TAB);
		cbox.addItem(DelimComboBox.SPACE);

		cbox.setSelectedItem(DelimComboBox.COMMA);
		cbox.setName(delimName);
		panel.add(cbox);

		panel.add(javax.swing.Box.createHorizontalStrut(30));

		panel.add(new JLabel(I18N.getLocalizedDialogLabel("Nulls")));
		JTextField nulls_field = new JTextField(8);
		nulls_field.setName(nullsName);

		panel.add(nulls_field);
		return panel;
	}

}
