package com.jeta.abeille.gui.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.TableId;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is the main controller for the ModelerView
 * 
 * @author Jeff Tassin
 */
public class ModelerViewController extends JETAController {
	/** the view we are controlling */
	private ModelerView m_view;

	/**
	 * ctor
	 */
	public ModelerViewController(ModelerView view) {
		super(view);
		m_view = view;
		assignAction(TableTreeNames.ID_TABLE_PROPERTIES, new MyEditTable());
		assignAction(ModelerNames.ID_DELETE_PROTOTYPE, new DeletePrototypeAction());

		ModelerViewUIDirector uidirector = new ModelerViewUIDirector(m_view);
		m_view.setUIDirector(uidirector);

	}

	TableId getSelectedTable() {
		return m_view.getSelectedTable();
	}

	ModelerModel getModeler() {
		return m_view.getModeler();
	}

	/**
	 * Deletes the selected prototype
	 */
	class DeletePrototypeAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableId tableid = getSelectedTable();
			if (tableid != null) {
				String msg = I18N.getLocalizedMessage("Delete the selected prototypes");
				int result = JOptionPane.showConfirmDialog(null, msg, I18N.getLocalizedMessage("Confirm"),
						JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION) {
					Object[] selections = m_view.getSelectedValues();
					for (int index = 0; index < selections.length; index++) {
						Object userobj = selections[index];
						if (userobj instanceof TableId) {
							getModeler().removePrototype((TableId) userobj);
						}
					}
				}
			}
		}
	}

	/**
	 * Edits the selected table
	 */
	class MyEditTable implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableId tableid = getSelectedTable();
			if (tableid != null) {
				EditTableAction.editTable(getModeler(), tableid);
			}
		}
	}

}
