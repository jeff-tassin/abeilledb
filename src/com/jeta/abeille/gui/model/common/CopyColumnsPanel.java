package com.jeta.abeille.gui.model.common;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import com.jeta.foundation.gui.table.CopyListOptionsPanel;
import com.jeta.foundation.gui.table.CopyOptionsNames;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This panel is an extension of the standard CopyListOptionsPanel. It is used
 * specifically when copying ColumnMetaData objects. It includes a checkbox that
 * allows the user to include the table name with the column name when copying
 * to the clipboard.
 * 
 * @author Jeff Tassin
 */
public class CopyColumnsPanel extends CopyListOptionsPanel {
	/**
	 * Private ctor. Use the createPanel instead.
	 */
	private CopyColumnsPanel() {
		super(true);
	}

	public static CopyColumnsPanel createPanel() {
		return new CopyColumnsPanel();
	}

	/**
	 * @return true if the include table check box is selected
	 */
	public boolean isIncludeTable() {
		JCheckBox cbox = (JCheckBox) getComponentByName(CopyOptionsNames.ID_INCLUDE_TABLE_NAME);
		if (cbox != null)
			return cbox.isSelected();
		else
			return false;
	}
}
