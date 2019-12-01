package com.jeta.abeille.gui.indexes.postgres;

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
public class IndexesViewController extends TSController {
	/** the view we are controlling */
	private IndexesView m_view;

	public IndexesViewController(IndexesView view) {
		super(view);
		m_view = view;
		assignAction(IndexesView.ID_CREATE_INDEX, new CreateIndex());
		assignAction(IndexesView.ID_EDIT_INDEX, new EditIndex());
		assignAction(IndexesView.ID_DELETE_INDEX, new DropIndex());
		assignAction(IndexesView.ID_REINDEX_INDEX, new RebuildIndex());
		assignAction(IndexesView.ID_REINDEX_TABLE, new RebuildAllIndexes());

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

		IndexesModel model = (IndexesModel) getModel();

		SQLCommandDialog dlg = SQLCommandDialog.createDialog(model.getConnection(), m_view, true);

		if (create) {
			dlg.setMessage(I18N.getLocalizedMessage("Create Index"));
		} else {
			dlg.setMessage(I18N.getLocalizedMessage("Modify Index"));
			if (oldindex.isPrimaryKey()) {
				dlg.getOkButton().setEnabled(false);
			}
		}

		final IndexView iview = new IndexView(model.getConnection(), model.getTableId(), oldindex);
		dlg.addValidator((JETARule) iview.getController());
		dlg.setPrimaryPanel(iview);
		dlg.setSize(dlg.getPreferredSize());
		dlg.addDialogListener(new SQLDialogListener() {
			public boolean cmdOk() throws SQLException {
				TableIndex newindex = iview.getTableIndex();
				if (create) {
					getModel().createIndex(newindex);
				} else {
					if (!oldindex.isPrimaryKey()) {
						assert (I18N.equals(oldindex.getName(), newindex.getName()));
						getModel().modifyIndex(newindex);
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
	IndexesModel getModel() {
		return (IndexesModel) m_view.getModel();
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
				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						getModel().dropIndex(index, dlg.isCascade());
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

	/**
	 * Action handler to rebuild the selected index
	 */
	public class RebuildIndex implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableIndex index = m_view.getSelectedIndex();
			if (index != null) {
				String msg = I18N.format("Rebuild_1", index.getName());
				int result = JOptionPane.showConfirmDialog(null, msg, I18N.getLocalizedMessage("Confirm"),
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					// try
					// {
					// getModel().rebuildIndex( index );
					// }
					// catch( SQLException e )
					// {
					// SQLErrorDialog.showErrorDialog( e, null );
					// }

				}
			}

		}
	}

	/**
	 * Action handler to rebuild all the indexes for the selected table
	 */
	public class RebuildAllIndexes implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			IndexesModel model = getModel();
			String msg = I18N.format("Rebuild_all_indexes_for_1", model.getTableId().getTableName());
			int result = JOptionPane.showConfirmDialog(null, msg, I18N.getLocalizedMessage("Confirm"),
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				// try
				// {
				// model.rebuildAllIndexes();
				// }
				// catch( SQLException e )
				// {
				// SQLErrorDialog.showErrorDialog( e, null );
				// }
			}
		}
	}

}
