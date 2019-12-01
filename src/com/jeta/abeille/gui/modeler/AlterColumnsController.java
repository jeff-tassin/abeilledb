package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.SQLException;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSTable;
import com.jeta.abeille.gui.store.ColumnInfo;
import com.jeta.abeille.gui.utils.DropDialog;
import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the ColumnsPanel GUI. It handles all button/gui
 * events. This controll is specifically for existing tables.
 * 
 * @author Jeff Tassin
 */
public class AlterColumnsController extends TSController {
	/** this is the panel that this controller controls */
	private ColumnsPanel m_view;

	/**
	 * Constructor
	 */
	public AlterColumnsController(ColumnsPanel panel) {
		super(panel);
		m_view = panel;

		panel.setUIDirector(new ColumnsViewDirector());

		assignAction(ColumnsPanel.ID_ADD_COLUMN, new AddColumnAction());
		assignAction(ColumnsPanel.ID_EDIT_COLUMN, new EditColumnAction());
		assignAction(ColumnsPanel.ID_REMOVE_COLUMN, new DropColumnAction());

		// goes in the controller
		JComponent comp = (JComponent) m_view.getComponentByName(ColumnsPanel.ID_COLUMNS_TABLE);
		comp.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					editSelectedItem();
				}
			}
		});

	}

	/**
	 * Edits the selected column data
	 */
	public void editSelectedItem() {
		final ColumnInfo oldinfo = m_view.getSelectedItem();
		if (oldinfo != null) {
			final TableId tableid = m_view.getModel().getTableId();
			final TSConnection tsconn = m_view.getModel().getConnection();
			SQLCommandDialog dlg = SQLCommandDialog.createDialog(tsconn, true);
			dlg.setMessage(I18N.getLocalizedMessage("Modify Column"));

			ModelerFactory factory = ModelerFactory.getFactory(tsconn);
			final ColumnPanel panel = factory.createColumnPanel(tsconn, tableid, oldinfo, false);
			dlg.setPrimaryPanel(panel);
			dlg.addValidator(panel);
			dlg.setSize(dlg.getPreferredSize());
			dlg.addDialogListener(new SQLDialogListener() {
				public boolean cmdOk() throws SQLException {
					TSTable tablesrv = (TSTable) tsconn.getImplementation(TSTable.COMPONENT_ID);
					ColumnInfo newinfo = panel.getColumnInfo();
					tablesrv.modifyColumn(tableid, newinfo, oldinfo);
					return true;
				}
			});
			dlg.showCenter();
			if (dlg.isOk()) {
				System.out.println("AlterColumnsController.editSelected item: " + tableid);
				tsconn.getModel(tableid.getCatalog()).reloadTable(tableid);
				m_view.getModel().setTableId(tableid);
			}
		}
	}

	/**
	 * Brings up the new column dialog box and allows the user to add a column
	 * to this table
	 */
	public class AddColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final TableId tableid = m_view.getModel().getTableId();
			final TSConnection tsconn = m_view.getModel().getConnection();
			SQLCommandDialog dlg = SQLCommandDialog.createDialog(tsconn, true);
			dlg.setMessage(I18N.getLocalizedMessage("Add Column"));
			ModelerFactory factory = ModelerFactory.getFactory(tsconn);
			final ColumnPanel panel = factory.createColumnPanel(tsconn, tableid, null, true);
			panel.enableComponent(ColumnNames.ID_PRIMARY_KEY, false);
			dlg.setPrimaryPanel(panel);
			dlg.addValidator(panel);
			dlg.setSize(dlg.getPreferredSize());
			dlg.addDialogListener(new SQLDialogListener() {
				public boolean cmdOk() throws SQLException {
					TSTable tablesrv = (TSTable) tsconn.getImplementation(TSTable.COMPONENT_ID);
					ColumnInfo newinfo = panel.getColumnInfo();
					tablesrv.createColumn(tableid, newinfo);
					return true;
				}
			});
			dlg.showCenter();
			if (dlg.isOk()) {
				tsconn.getModel(tableid.getCatalog()).reloadTable(tableid);
				m_view.getModel().setTableId(tableid);
			}
		}
	}

	/**
	 * Removes the selected column from the model
	 */
	public class DropColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final ColumnInfo oldinfo = m_view.getSelectedItem();
			if (oldinfo != null) {
				final TableId tableid = m_view.getModel().getTableId();
				final TSConnection tsconn = m_view.getModel().getConnection();
				final DropDialog dlg = DropDialog.createDropDialog(tsconn, m_view, true);
				dlg.setConnection(tsconn);
				dlg.setMessage(I18N.getLocalizedMessage("Drop Column"));
				dlg.setSize(dlg.getPreferredSize());
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						TSTable tablesrv = (TSTable) tsconn.getImplementation(TSTable.COMPONENT_ID);
						tablesrv.dropColumn(tableid, oldinfo, dlg.isCascade());
						return true;
					}
				});
				dlg.showCenter();
				if (dlg.isOk()) {
					tsconn.getModel(tableid.getCatalog()).reloadTable(tableid);
					m_view.getModel().setTableId(tableid);
				}
			}
		}
	}

	/**
	 * Edits the selected item
	 */
	public class EditColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			editSelectedItem();
		}
	}

	/**
	 * Responsible for enabling/disabling components on the ColumnsView based on
	 * the state of the model
	 */
	public class ColumnsViewDirector implements UIDirector {
		public void updateComponents(java.util.EventObject evt) {
			ColumnsGuiModel model = m_view.getModel();
			assert (!model.isPrototype());
			if (model.getTableId() == null) {
				m_view.enableComponent(ColumnsPanel.ID_ADD_COLUMN, false);
				m_view.enableComponent(ColumnsPanel.ID_EDIT_COLUMN, false);
				m_view.enableComponent(ColumnsPanel.ID_REMOVE_COLUMN, false);
			} else {
				ColumnInfo info = m_view.getSelectedItem();
				int rowcount = model.getRowCount();
				m_view.enableComponent(ColumnsPanel.ID_ADD_COLUMN, true);

				if (info == null) {
					m_view.enableComponent(ColumnsPanel.ID_EDIT_COLUMN, false);
					m_view.enableComponent(ColumnsPanel.ID_REMOVE_COLUMN, false);
				} else {

					m_view.enableComponent(ColumnsPanel.ID_EDIT_COLUMN, true);
					if (info.isPrimaryKey())
						m_view.enableComponent(ColumnsPanel.ID_REMOVE_COLUMN, false);
					else
						m_view.enableComponent(ColumnsPanel.ID_REMOVE_COLUMN, true);

				}
			}

		}
	}

}
