/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.export.ExportModel;
import com.jeta.foundation.gui.table.export.ColumnExportModel;
import com.jeta.foundation.gui.table.export.ColumnExportSetting;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.foundation.interfaces.app.ObjectStore;

import com.jeta.forms.components.panel.FormPanel;

/**
 * This is the GUI used to specify how values from a single dimensional list are
 * copied. Currently, we support the following:
 * 
 * 
 * @author Jeff Tassin
 */
public class CopyListOptionsPanel extends TSPanel {
	/** set to true if copying a table. */
	private boolean m_table_copy;
	private FormPanel m_form;

	/**
	 * ctor
	 */
	public CopyListOptionsPanel() {
		this(false);
	}

	/**
	 * ctor
	 * 
	 * @param basic
	 *            set to true if you want to display the copy options for a
	 *            table copy. When copying from a table, we include options such
	 *            as column headers and null value support.
	 */
	public CopyListOptionsPanel(boolean tablecopy) {
		m_table_copy = tablecopy;
		setLayout(new BorderLayout());
		if (tablecopy) {
			m_form = new FormPanel("com/jeta/foundation/gui/table/copyColumns.jfrm");
		} else {
			m_form = new FormPanel("com/jeta/foundation/gui/table/copySpecial.jfrm");
			final JCheckBox check = m_form.getCheckBox(CopyOptionsNames.ID_COLUMN_HEADER_SHOW);
			check.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					m_form.enableComponent(CopyOptionsNames.ID_COLUMN_HEADER_DELIMITER, check.isSelected());
				}
			});
		}

		add(m_form, BorderLayout.CENTER);
		initialize();
	}

	/**
	 * @return the line decorator
	 */
	public String getLineDecorator() {
		return m_form.getText(CopyOptionsNames.ID_LINE_DECORATOR);
	}

	/**
	 * @return the line decorator
	 */
	public String getNullsValue() {
		String txt = m_form.getText(CopyOptionsNames.ID_NULLS);
		if (txt == null)
			txt = "";
		return txt;
	}

	/**
	 * Gets the model represented by the gui settings. Note that the column
	 * settings objects are not set here. This is used for BasicSelections where
	 * we only have a single column.
	 */
	public ExportModel getModel() {
		ExportModel model = save();
		// now create the ColumnExportModel
		ColumnExportModel cmodel = new ColumnExportModel();
		ColumnExportSetting setting = new ColumnExportSetting("", 0, true, null);
		cmodel.addSetting(setting);
		model.setColumnExportModel(cmodel);
		model.setLineDecorator(getLineDecorator());
		return model;
	}

	/**
	 * Gets the model represented by the gui settings
	 */
	public ExportModel getModel(TableModel tableModel) {
		ExportModel model = save();
		// now create the ColumnExportModel
		ColumnExportModel cmodel = new ColumnExportModel();
		for (int col = 0; col < tableModel.getColumnCount(); col++) {
			ColumnExportSetting setting = new ColumnExportSetting(tableModel.getColumnName(col), col, true, null);
			cmodel.addSetting(setting);
		}
		model.setColumnExportModel(cmodel);
		model.setLineDecorator(getLineDecorator());
		return model;
	}

	/**
	 * Initializes the GUI components from any saved ExportModel
	 */
	private void initialize() {
		ExportModel model = null;

		try {
			ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
			model = (ExportModel) os.load(ExportModel.COMPONENT_ID);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}

		if (model != null) {
			m_form.setSelected(CopyOptionsNames.ID_TRANSPOSE, model.isTransposed());

			// value delimiter
			TSComboBox combo = (TSComboBox) m_form.getComponentByName(CopyOptionsNames.ID_VALUE_DELIMITER);
			combo.setSelectedItem(model.getValueDelimiter());

			// column headers
			m_form.setSelected(CopyOptionsNames.ID_COLUMN_HEADER_SHOW, model.isShowColumnNames());
			m_form.enableComponent(CopyOptionsNames.ID_COLUMN_HEADER_DELIMITER, model.isShowColumnNames());

			combo = (TSComboBox) m_form.getComponentByName(CopyOptionsNames.ID_COLUMN_HEADER_DELIMITER);
			if (combo != null) {
				combo.setSelectedItem(model.getColumnNameDelimiter());
				combo.setEnabled(model.isShowColumnNames());
			}

			m_form.setText(CopyOptionsNames.ID_NULLS, model.getNullsValue());
		}
	}

	/**
	 * Saves the settings to the application state store
	 */
	private ExportModel save() {
		ExportModel model = new ExportModel();
		model.setTranspose(m_form.isSelected(CopyOptionsNames.ID_TRANSPOSE));

		// value delimiter
		TSComboBox combo = (TSComboBox) m_form.getComponentByName(CopyOptionsNames.ID_VALUE_DELIMITER);
		model.setValueDelimiter(combo.getText());

		// column headers
		model.setShowColumnNames(m_form.isSelected(CopyOptionsNames.ID_COLUMN_HEADER_SHOW));

		combo = (TSComboBox) m_form.getComponentByName(CopyOptionsNames.ID_COLUMN_HEADER_DELIMITER);
		if (combo != null) {
			model.setColumnNameDelimiter(combo.getText());
		}

		model.setNullsValue(getNullsValue());

		try {
			ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
			os.store(ExportModel.COMPONENT_ID, model);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}

		return model;
	}

}
