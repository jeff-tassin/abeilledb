package com.jeta.abeille.gui.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.GrantDefinition;

import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLOptionDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.gui.table.RowSelection;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * The controller class for the GrantsView
 * 
 * @author Jeff Tassin
 */
public class GrantsViewController extends TSController implements UIDirector {
	/** the view we are controlling */
	private GrantsView m_view;

	/** constants */
	private static final int GRANT_ACTION = 1;
	private static final int REVOKE_ACTION = 2;
	private static final int TOGGLE_ACTION = 3;

	/**
	 * ctor
	 */
	public GrantsViewController(GrantsView view) {
		super(view);
		m_view = view;

		// assignAction( GrantsView.ID_RELOAD_GRANTS, new ReloadGrantsAction()
		// );
		// assignAction( GrantsView.ID_COMMIT, new CommitAction() );

		// assignAction( GrantsView.ID_GRANT_SELECTION, new
		// GrantSelectionAction() );
		// assignAction( GrantsView.ID_REVOKE_SELECTION, new
		// RevokeSelectionAction() );
		// assignAction( GrantsView.ID_TOGGLE_SELECTION, new
		// ToggleSelectionAction() );

		// setUIDirector(this);
		// updateComponents();

	}

	/**
	 * Gets the selected cells from the GrantsView table and updates the
	 * privileges based on the given action.
	 * 
	 * @param action
	 *            determines how to update the privileges in the selection.
	 *            either GRANT_ACTION, REVOKE_ACTION, or TOGGLE_ACTION
	 */
	private void modifySelection(int action) {
		GrantsView.GrantsViewDefinition viewdef = m_view.getCurrentViewDefinition();
		GrantsModel model = viewdef.getGrantsModel();
		TSTablePanel tablepanel = (TSTablePanel) viewdef.getViewComponent();
		TableSelection selection = tablepanel.getSelection();
		int rowcount = selection.getRowCount();
		/**
		 * the table selection is already in model coordinates, so we can update
		 * the model directly
		 */
		for (int rowindex = 0; rowindex < rowcount; rowindex++) {
			RowSelection rowsel = selection.fromIndex(rowindex);
			if (rowsel != null) {
				int[] cols = rowsel.getColumns();
				for (int colindex = 0; colindex < cols.length; colindex++) {
					int col = cols[colindex];
					GrantsModel.ColumnGrantDefinition cgd = model.getColumnGrantDefinition(col);
					if (cgd != null && cgd.isEditable()) {
						if (action == GRANT_ACTION) {
							model.setValueAt(Boolean.TRUE, rowsel.getRow(), col);
						} else if (action == REVOKE_ACTION) {
							model.setValueAt(Boolean.FALSE, rowsel.getRow(), col);
						} else if (action == TOGGLE_ACTION) {
							Boolean bval = (Boolean) model.getValueAt(rowsel.getRow(), col);
							model.setValueAt(Boolean.valueOf(!bval.booleanValue()), rowsel.getRow(), col);
						}
					}
				}
			}
		}
	}

	/**
	 * UIManager implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		AbstractUser user = m_view.getSelectedUser();
		if (user == null) {
			m_view.enableComponent(GrantsView.ID_RELOAD_GRANTS, false);
		} else {
			m_view.enableComponent(GrantsView.ID_RELOAD_GRANTS, true);
		}
	}

	/**
	 * Iterates over the grants table and saves any grants changes to the
	 * database
	 */
	public class CommitAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ArrayList grants = null;
			GrantsModel model = m_view.getGrantsModel();
			for (int row = 0; row < model.getRowCount(); row++) {
				GrantDefinitionWrapper wrapper = (GrantDefinitionWrapper) model.getRow(row);
				if (wrapper.isModified()) {
					GrantDefinition original = wrapper.getOriginalGrantDefinition();
					GrantDefinition newdef = wrapper.getGrantDefinition();
					GrantDefinition[] g = new GrantDefinition[2];
					g[0] = newdef;
					g[1] = original;
					if (grants == null) {
						grants = new ArrayList();
					}
					grants.add(g);
				}
			}

			if (grants == null) {
				String msg = I18N.getLocalizedMessage("Nothing to commit");
				TSGuiToolbox.showErrorDialog(msg);
			} else {
				showCommitDialog(grants);
			}
		}

		/**
		 * Displays the commit dialog and commits the grants to the database if
		 * the user confirms the action.
		 * 
		 * @param a
		 *            collection of GrantDefinition objects
		 */
		protected void showCommitDialog(Collection grants) {

			final Collection grants_list = grants;
			String msg = I18N.getLocalizedMessage("Commit the grants in the current view");
			SQLOptionDialog dlg = SQLOptionDialog.createOptionDialog(m_view.getConnection(), true);
			dlg.setMessage(msg);
			dlg.addDialogListener(new SQLDialogListener() {
				public boolean cmdOk() throws SQLException {
					GrantsModel model = m_view.getGrantsModel();
					Iterator iter = grants_list.iterator();
					while (iter.hasNext()) {
						GrantDefinition[] gdef = (GrantDefinition[]) iter.next();
						model.modifyGrant(gdef[0], gdef[1]);
					}
					return true;
				}
			});

			dlg.showCenter();
			if (dlg.isOk())
				m_view.getGrantsModel().reload();
		}
	}

	/**
	 * Grants the selected privilege
	 */
	public class GrantSelectionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			modifySelection(GRANT_ACTION);
		}
	}

	/**
	 * ActionListener for reloading grants
	 */
	public class ReloadGrantsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String filter = m_view.getFilter();
			// replace % in case user thinks filter behaves LIKE %
			filter = filter.replace('%', '*');
			String regex = null;
			if (filter.length() > 0) {
				regex = TSUtils.wildCardToRegex(filter);
			}

			m_view.setGrantsModelType(m_view.getSelectedObjectType());
			AbstractUser user = m_view.getSelectedUser();
			DbObjectType objtype = m_view.getSelectedObjectType();
			Schema schema = m_view.getSelectedSchema();
			if (user != null) {
				GrantsModel gmodel = m_view.getGrantsModel();
				gmodel.reload(objtype, m_view.getCatalog(), schema, user, regex);
				m_view.setGrantsHeading(I18N.format("object_privileges_for_2", objtype.toString(),
						user.getQualifiedName()));
			}
		}
	}

	/**
	 * Revokes the selected privilege
	 */
	public class RevokeSelectionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			modifySelection(REVOKE_ACTION);
		}
	}

	/**
	 * Toggles the selected privilege
	 */
	public class ToggleSelectionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			modifySelection(TOGGLE_ACTION);
		}
	}

}
