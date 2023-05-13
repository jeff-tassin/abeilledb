package com.jeta.abeille.gui.formbuilder;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import com.jeta.abeille.gui.update.InstanceOptionsView;

import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of columns used in a form view
 * 
 * @author Jeff Tassin
 */
public class ColumnsView extends InstanceOptionsView {

	/*
	 * ctor
	 */
	public ColumnsView(FormInstanceMetaData model) {
		super(model);

		JToolBar toolbar = (JToolBar) getComponentByName(InstanceOptionsView.ID_TOOLBAR);
		toolbar.getParent().remove(toolbar);

		/*
		 * JButton btn = i18n_createButton( null, FormNames.ID_ADD_COLUMN,
		 * "general/Add16.gif" ); btn.setToolTipText(
		 * I18N.getLocalizedMessage("Add Column") );
		 * btn.setBorderPainted(false); btn.setFocusPainted(false); toolbar.add(
		 * btn, 0 );
		 * 
		 * btn = i18n_createButton( null, FormNames.ID_REMOVE_COLUMN,
		 * "general/Delete16.gif" ); btn.setToolTipText(
		 * I18N.getLocalizedMessage("Remove Column") );
		 * btn.setBorderPainted(false); btn.setFocusPainted(false); toolbar.add(
		 * btn, 1 );
		 */

		JTable table = getTable();
		TableColumnModel colmodel = table.getColumnModel();
		colmodel.removeColumn(colmodel.getColumn(0));
	}
}
