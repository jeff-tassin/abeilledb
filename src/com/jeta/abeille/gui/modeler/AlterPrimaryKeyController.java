package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSTable;

import com.jeta.abeille.gui.utils.DropDialog;
import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This class is for altering a primary key for an existing table
 * 
 * @author Jeff Tassin
 */
public class AlterPrimaryKeyController extends TSController {
	/**
	 * The view we are controlling events for
	 */
	private PrimaryKeyView m_view;

	/**
	 * Listener that wants events when we change the table by
	 * modifying/deleting/creating the primary key
	 */
	private TableChangedListener m_listener;

	public AlterPrimaryKeyController(PrimaryKeyView view, TableChangedListener listener) {
		super(view);
		m_view = view;
		m_listener = listener;
		assignAction(PrimaryKeyView.ID_EDIT_PRIMARY_KEY, new EditPrimaryKeyAction());
		assignAction(PrimaryKeyView.ID_DELETE_PRIMARY_KEY, new DeletePrimaryKeyAction());

		PrimaryKeyViewUIDirector uidirector = new PrimaryKeyViewUIDirector(m_view);
		m_view.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Deletes the primary key
	 */
	public class DeletePrimaryKeyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (m_view.getPrimaryKeyColumnsCount() > 0) {
				final TableId tableid = m_view.getTableId();
				final TSConnection tsconn = m_view.getConnection();
				final DropDialog dlg = DropDialog.createDropDialog(tsconn, m_view, true);
				dlg.setConnection(tsconn);
				dlg.setMessage(I18N.getLocalizedMessage("Drop Primary Key"));
				dlg.setSize(dlg.getPreferredSize());
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						TSTable tablesrv = (TSTable) tsconn.getImplementation(TSTable.COMPONENT_ID);
						tablesrv.dropPrimaryKey(tableid, dlg.isCascade());
						return true;
					}
				});
				dlg.showCenter();
				if (dlg.isOk()) {
					// tsconn.getModel( tableid.getCatalog() ).reloadTable(
					// tableid );
					if (m_listener != null)
						m_listener.tableChanged(tableid);
				}
			}
		}
	}

	/**
	 * Action handler for editing a primary key
	 */
	public class EditPrimaryKeyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			SQLCommandDialog dlg = SQLCommandDialog.createDialog(m_view.getConnection(), m_view, true);
			dlg.setMessage(I18N.getLocalizedMessage("Modify Primary Key"));
			final PrimaryKeyAssignmentView assignview = new PrimaryKeyAssignmentView(m_view.getConnection(),
					m_view.getPrimaryKeyColumns(), m_view.getColumnsModel());

			final TableId tableid = m_view.getTableId();

			dlg.setPrimaryPanel(assignview);
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.addDialogListener(new SQLDialogListener() {
				public boolean cmdOk() throws SQLException {
					TSConnection tsconn = m_view.getConnection();
					TableMetaData tmd = tsconn.getTable(tableid);
					if (tmd != null) {
						DbKey newpk = assignview.createPrimaryKey();

						newpk.print();
						TSTable tablesrv = (TSTable) tsconn.getImplementation(TSTable.COMPONENT_ID);
						tablesrv.modifyPrimaryKey(tableid, newpk, tmd.getPrimaryKey());
					}
					return true;
				}
			});
			dlg.showCenter();
			if (dlg.isOk()) {
				if (m_listener != null)
					m_listener.tableChanged(tableid);
			}
		}
	}

}
