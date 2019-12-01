package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorPanel;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * This is the controller for the TableEditorDialog. It handles tasks that are
 * global to the dialog such as import table. Each panel on the dialog has its
 * own controller that is specific for that panel.
 * 
 * @author Jeff Tassin
 */
public class TableEditorDialogController extends TSController implements JETARule {
	private TableSelectorModel m_tableselector;
	private TableEditorDialog m_dialog; // the dialog we are handling events for
	private TSConnection m_connection;

	/**
	 * Constructor
	 */
	public TableEditorDialogController(TSConnection conn, TableSelectorModel tableSelector, TableEditorDialog dialog) {
		super(dialog);
		m_connection = conn;
		m_dialog = dialog;
		m_tableselector = tableSelector;
		dialog.setUIDirector(new TableDialogDirector());
	}

	/**
	 * Sets the dialog for this controller
	 */
	void setDialog(TableEditorDialog dlg) {
		m_dialog = dlg;
	}

	/**
	 * Validate that the table definition is consistent
	 * 
	 * @return a string message if the validation fails. Otherwise, return null
	 *         on success
	 */
	public RuleResult check(Object[] params) {
		return m_dialog.check(params);
	}

	/**
	 * Command Handler that gets called when user clicks import button on the
	 * columns panel This pops up the ImportDialog to allow the user to select a
	 * table whose columns and optionally primary/foreign keys to import into
	 * this definition
	 */
	public class ImportAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ImportDialog dlg = (ImportDialog) TSGuiToolbox.createDialog(ImportDialog.class, m_dialog, true);
			dlg.initialize(m_tableselector);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				TableId id = dlg.createTableId(m_connection);
				if (id != null) {
					TableMetaData tmd = m_tableselector.getTable(id);
					if (tmd != null) {

						// clone it
						tmd = (TableMetaData) tmd.clone();
						ModelerUtils.validateColumnSizes(tmd);

						// okay, first import all defined columsn into the model
						// @todo strip off sizes from those fields that don't
						// really make sense (like time and date)
						// some databases will return sizes for these types
						ColumnsPanel columnspanel = (ColumnsPanel) m_dialog
								.getComponentByName(ModelerNames.ID_COLUMNS_PANEL);
						columnspanel.getModel().loadTable(tmd, dlg.importPrimaryKey());
						columnspanel.notifyModelChanged();

						// now, load the foreign keys
						ForeignKeysView fkpanel = (ForeignKeysView) m_dialog
								.getComponentByName(ModelerNames.ID_FOREIGN_KEYS_PANEL);
						// fkpanel.getModel().loadForeignKeys( tmd );
						// fkpanel.expandAll();
					} else {
						assert (false);
					}
				}
			}
		} // actionPerformed
	}

	/**
	 * Class for updating buttons/menus on dialog
	 */
	public class TableDialogDirector implements UIDirector {
		public void updateComponents(java.util.EventObject evt) {
			TableEditorPanel editorview = m_dialog.getTableEditorPanel();

			javax.swing.JPanel subview = editorview.getActiveView();
			if (subview instanceof TSPanel) {
				TSPanel tpanel = (TSPanel) subview;
				tpanel.updateComponents(null);
			}
		}
	}

}
