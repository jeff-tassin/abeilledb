package com.jeta.abeille.gui.indexes.mysql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.gui.indexes.IndexesView;
import com.jeta.abeille.gui.indexes.IndexesViewUIDirector;
import com.jeta.abeille.gui.indexes.TableIndex;

import com.jeta.abeille.gui.utils.DropDialog;
import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;

import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;

/**
 * @author Jeff Tassin
 */
public class MySQLIndexesViewController extends TSController {
	/** the view we are controlling */
	private IndexesView m_view;

	public MySQLIndexesViewController(IndexesView view) {
		super(view);
		m_view = view;

		assignAction(IndexesView.ID_CREATE_INDEX, new CreateIndex());
		assignAction(IndexesView.ID_EDIT_INDEX, new EditIndex());

		assignAction(IndexesView.ID_DELETE_INDEX, new DropIndex());

		m_view.setVisible(IndexesView.ID_REINDEX_INDEX, false);
		m_view.setVisible(IndexesView.ID_REINDEX_TABLE, false);

		UIDirector uidirector = new IndexesViewUIDirector(view);
		view.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Edits/Creates an index.
	 * 
	 * @param index
	 *            the index to edit. If this is null, then we create an index
	 */
	private void editIndex(TableIndex index) {
		final TableIndex oldindex = index;
		final boolean create = (oldindex == null);
		MySQLIndexesModel model = (MySQLIndexesModel) m_view.getModel();

		SQLCommandDialog dlg = SQLCommandDialog.createDialog(model.getConnection(), m_view, true);

		if (create) {
			dlg.setMessage(I18N.getLocalizedMessage("Create Index"));
		} else {
			dlg.setMessage(I18N.getLocalizedMessage("Modify Index"));
			if (oldindex.isPrimaryKey()) {
				dlg.getOkButton().setEnabled(false);
				dlg.showSQLButton(false);
			}
		}

		final MySQLIndexView iview = new MySQLIndexView(model.getConnection(), model.getTableId(), oldindex);
		dlg.addValidator((JETARule) iview.getController());
		dlg.setPrimaryPanel(iview);
		dlg.setSize(dlg.getPreferredSize());
		dlg.addDialogListener(new SQLDialogListener() {
			public boolean cmdOk() throws SQLException {
				TableIndex newindex = iview.createTableIndex();
				if (create) {
					getModel().createIndex(newindex);
				} else {
					if (!oldindex.isPrimaryKey()) {
						assert (I18N.equals(oldindex.getName(), newindex.getName()));
						getModel().modifyIndex(newindex, oldindex);
					}
				}
				return true;
			}
		});

		dlg.showCenter();
		if (dlg.isOk()) {
			model.reload();
		}

	}

	/**
	 * @return the model for our indexes
	 */
	MySQLIndexesModel getModel() {
		return (MySQLIndexesModel) m_view.getModel();
	}

	/**
	 * Action handler to create a new index
	 */
	public class CreateIndex implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			editIndex(null);
		}
	}

	/**
	 * Action handler to drop the selected index
	 */
	public class DropIndex implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final TableIndex index = m_view.getSelectedIndex();
			if (index != null && !index.isPrimaryKey()) {
				TableId tableid = getModel().getTableId();
				String msg = I18N.format("Drop_2", index.getName(), tableid.getTableName());
				final DropDialog dlg = DropDialog.createDropDialog(getModel().getConnection(), m_view, true);
				dlg.setCascadeEnabled(false);
				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						getModel().dropIndex(index);
						return true;
					}
				});

				dlg.showCenter();
				if (dlg.isOk()) {
					getModel().reload();
				}
			}
		}
	}

	/**
	 * Action handler to edit the selected index
	 */
	public class EditIndex implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableIndex index = m_view.getSelectedIndex();
			if (index != null)
				editIndex(index);
		}
	}

}
