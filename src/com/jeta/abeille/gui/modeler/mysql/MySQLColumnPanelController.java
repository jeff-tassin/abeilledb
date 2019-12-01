package com.jeta.abeille.gui.modeler.mysql;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jeta.abeille.gui.modeler.ColumnPanel;
import com.jeta.abeille.gui.modeler.ColumnNames;
import com.jeta.abeille.gui.modeler.ColumnPanelController;

import com.jeta.plugins.abeille.mysql.MySQLDataTypeInfo;

/**
 * Controller class for the MySQLColumnPanel.
 * 
 * @author Jeff Tassin
 */
public class MySQLColumnPanelController extends ColumnPanelController {

	/**
	 * ctor
	 */
	public MySQLColumnPanelController(MySQLColumnPanel view) {
		super(view);
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		super.updateComponents(evt);
		ColumnPanel view = (ColumnPanel) getView();
		if (view != null) {
			MySQLDataTypeInfo dinfo = (MySQLDataTypeInfo) view.getSelectedDataType();
			if (dinfo != null) {
				JCheckBox cbox = (JCheckBox) view.getComponentByName(ColumnNames.ID_UNSIGNED);
				cbox.setEnabled(dinfo.supportsUnsigned());
				if (!cbox.isEnabled())
					cbox.setSelected(false);

				cbox = (JCheckBox) view.getComponentByName(ColumnNames.ID_ZERO_FILL);
				cbox.setEnabled(dinfo.supportsZeroFill());
				if (!cbox.isEnabled())
					cbox.setSelected(false);

				JComponent params = (JComponent) view.getComponentByName(ColumnNames.ID_PARAMETERS);
				params.setEnabled(dinfo.supportsAttributes());
			} else {
				JComponent params = (JComponent) view.getComponentByName(ColumnNames.ID_PARAMETERS);
				params.setEnabled(false);
			}
		}
	}
}
