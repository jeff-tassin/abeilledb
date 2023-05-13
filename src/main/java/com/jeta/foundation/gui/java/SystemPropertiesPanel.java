/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.awt.BorderLayout;

import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JComponent;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.JETATableModel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * This class displays Java System properties in a table.
 * 
 * @author Jeff Tassin
 */
public class SystemPropertiesPanel extends TSPanel {
	/** the model for holding the Java system properties */
	private JETATableModel m_model;

	/**
	 * ctor
	 */
	public SystemPropertiesPanel() {
		initialize();
	}

	/**
	 * Creates the JTable that displays the JDBC driver properties
	 * 
	 * @returns the table component
	 */
	private JComponent createInfoTable() {
		m_model = new JETATableModel();

		String[] names = new String[2];
		names[0] = I18N.getLocalizedMessage("Property");
		names[1] = I18N.getLocalizedMessage("Value");
		m_model.setColumnNames(names);

		Class[] coltypes = new Class[2];
		coltypes[0] = String.class;
		coltypes[1] = String.class;
		m_model.setColumnTypes(coltypes);

		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_model, true);
		return tpanel;
	}

	/**
	 * Creates the components on this panel
	 */
	private void initialize() {
		setLayout(new BorderLayout());
		add(createInfoTable(), BorderLayout.CENTER);

		// load the data
		try {
			Properties props = System.getProperties();
			Enumeration names = props.propertyNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				String value = props.getProperty(name);
				Object[] row = new Object[2];
				row[0] = name;
				row[1] = value;
				m_model.addRow(row);
			}
		} catch (Exception e) {

		}
	}

}
