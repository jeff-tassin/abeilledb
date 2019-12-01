package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.utils.TSUtils;

/**
 * Controller class for the ColumnPanel.
 * 
 * @author Jeff Tassin
 */
public class ColumnPanelController extends TSController implements UIDirector {

	/** the view we are controlling */
	private ColumnPanel m_view;

	/**
	 * ctor
	 */
	public ColumnPanelController(ColumnPanel view) {
		super(view);
		m_view = view;

		final JCheckBox pkbox = (JCheckBox) view.getComponentByName(ColumnNames.ID_PRIMARY_KEY);
		pkbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JCheckBox nullbox = (JCheckBox) m_view.getComponentByName(ColumnNames.ID_ALLOW_NULLS);
				if (pkbox.isSelected()) {
					nullbox.setSelected(false);
					nullbox.setEnabled(false);
				} else {
					nullbox.setEnabled(true);
				}
			}
		});

		view.setUIDirector(this);
		updateComponents(null);
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		ColumnPanel view = (ColumnPanel) getView();
		if (view != null) {
			JTextField defvalue = (JTextField) view.getComponentByName(ColumnNames.ID_DEFAULT_VALUE);
			defvalue.setEditable(!view.isAutoIncrement());

			DataTypeInfo dinfo = view.getSelectedDataType();
			JTextField pfield = (JTextField) view.getComponentByName(ColumnNames.ID_SIZE);
			JTextField sfield = (JTextField) view.getComponentByName(ColumnNames.ID_SCALE);
			JCheckBox autobox = (JCheckBox) view.getComponentByName(ColumnNames.ID_AUTO_INCREMENT);
			JCheckBox nullbox = (JCheckBox) m_view.getComponentByName(ColumnNames.ID_ALLOW_NULLS);

			if (dinfo == null) {
				pfield.setEnabled(true);
				sfield.setEnabled(true);
			} else {
				pfield.setEnabled(dinfo.supportsCustomPrecision());
				sfield.setEnabled(dinfo.supportsCustomScale());
				autobox.setEnabled(dinfo.supportsAutoIncrement());
			}

			if (view.isPrimaryKey()) {
				nullbox.setEnabled(false);
			} else {
				nullbox.setEnabled(true);
			}
		} else {
			assert (false);
		}
	}
}
