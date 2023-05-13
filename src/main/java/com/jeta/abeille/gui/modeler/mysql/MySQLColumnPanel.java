package com.jeta.abeille.gui.modeler.mysql;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.common.DbGuiUtils;
import com.jeta.abeille.gui.modeler.ColumnPanel;
import com.jeta.abeille.gui.modeler.ColumnNames;
import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TextFieldwButtonPanel;
import com.jeta.foundation.gui.layouts.TableLayout;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.rules.RuleResult;

import com.jeta.plugins.abeille.mysql.MySQLColumnAttributes;

/**
 * This is a popup dialog that allows the user to directly edit the attributes
 * of a column
 * 
 * @author Jeff Tassin
 */
public class MySQLColumnPanel extends ColumnPanel {

	/**
	 * ctor
	 * 
	 * @param connection
	 *            the database connection
	 * @param parentId
	 *            if the table is not a prototype, then this is the table that
	 *            contains this column. Otherwise, this value will be null.
	 * @param info
	 *            the column we are editing
	 */
	public MySQLColumnPanel(TSConnection connection, TableId parentId, ColumnInfo info, boolean bprototype) {
		super(connection, info, bprototype);
	}

	/**
	 * @return the field info object specified in this dialog
	 */
	public ColumnInfo getColumnInfo() {
		ColumnInfo info = super.getColumnInfo();
		info.setAttributes(getColumnAttributes());
		return info;
	}

	/**
	 * @return the column attributes shown in the current view
	 */
	public MySQLColumnAttributes getColumnAttributes() {
		MySQLColumnAttributes attr = new MySQLColumnAttributes();
		attr.setUnsigned(isSelected(ColumnNames.ID_UNSIGNED));
		attr.setZeroFill(isSelected(ColumnNames.ID_ZERO_FILL));
		attr.setParameters(getText(ColumnNames.ID_PARAMETERS));
		return attr;
	}

	/**
	 * Sets the field information for this dialog
	 * 
	 * @param fi
	 *            the ColumnInfo object used to initalize the components in the
	 *            dialog
	 */
	public void setColumnInfo(ColumnInfo colinfo) {
		super.setColumnInfo(colinfo);
		if (colinfo != null) {
			MySQLColumnAttributes attr = (MySQLColumnAttributes) colinfo.getAttributes();
			if (attr != null) {
				setSelected(ColumnNames.ID_UNSIGNED, attr.isUnsigned());
				setSelected(ColumnNames.ID_ZERO_FILL, attr.isZeroFill());
				setText(ColumnNames.ID_PARAMETERS, attr.getParameters());
			}
		}
	}
}
