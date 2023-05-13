package com.jeta.abeille.gui.security.mysql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Privilege;

import com.jeta.abeille.gui.common.DbGuiUtils;

import com.jeta.abeille.gui.security.GrantsModel;
import com.jeta.abeille.gui.security.GrantsView;
import com.jeta.abeille.gui.security.GrantsViewController;
import com.jeta.abeille.gui.security.OrthoGrantDefinitionWrapper;

import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLOptionDialog;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.gui.table.RowSelection;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.mysql.MySQLObjectType;

/**
 * The controller class for the GrantsView
 * 
 * @author Jeff Tassin
 */
public class MySQLGrantsViewController extends GrantsViewController {

	/**
	 * ctor
	 */
	public MySQLGrantsViewController(GrantsView view) {
		super(view);

		assignAction(GrantsView.ID_RELOAD_GRANTS, new MySQLReloadGrantsAction());
		assignAction(GrantsView.ID_COMMIT, new MySQLCommitAction());
		assignAction(GrantsView.ID_GRANT_SELECTION, new GrantSelectionAction());
		assignAction(GrantsView.ID_REVOKE_SELECTION, new RevokeSelectionAction());
		assignAction(GrantsView.ID_TOGGLE_SELECTION, new ToggleSelectionAction());

		final JComboBox cbox = (JComboBox) view.getComponentByName(GrantsView.ID_TYPES_COMBO);
		cbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				GrantsView view = (GrantsView) getView();
				JTextField ffield = (JTextField) view.getComponentByName(GrantsView.ID_FILTER_FIELD);
				TSComboBox tablescombo = (TSComboBox) view.getComponentByName(GrantsView.ID_TABLES_COMBO);
				DbObjectType objtype = (DbObjectType) cbox.getSelectedItem();
				if (objtype == DbObjectType.COLUMN) {
					tablescombo.setEnabled(true);
					DbGuiUtils.loadTablesCombo(view.getConnection(), view.getCatalog(), view.getSelectedSchema(),
							tablescombo);
				} else {
					tablescombo.setEnabled(false);
				}

				if (objtype == MySQLObjectType.GLOBAL) {
					ffield.setEnabled(false);
				} else {
					ffield.setEnabled(true);
				}
			}
		});

		final JComboBox catbox = (JComboBox) view.getComponentByName(GrantsView.ID_CATALOGS_COMBO);
		catbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				GrantsView view = (GrantsView) getView();
				TSComboBox tablescombo = (TSComboBox) view.getComponentByName(GrantsView.ID_TABLES_COMBO);
				DbObjectType objtype = (DbObjectType) cbox.getSelectedItem();
				if (objtype == DbObjectType.COLUMN) {
					tablescombo.setEnabled(true);
					DbGuiUtils.loadTablesCombo(view.getConnection(), view.getCatalog(), view.getSelectedSchema(),
							tablescombo);
				} else {
					tablescombo.setEnabled(false);
				}
			}
		});

		view.setUIDirector(this);
		updateComponents(null);
	}

	public class MySQLCommitAction extends CommitAction {
		public void actionPerformed(ActionEvent evt) {
			GrantsModel model = ((GrantsView) getView()).getGrantsModel();
			if (model.getObjectType() == MySQLObjectType.GLOBAL) {
				boolean bmodified = false;
				// create a psuedo object id for global grants
				DbObjectId objid = new DbObjectId(MySQLObjectType.GLOBAL, model.getCatalog(), model.getSchema(), "");
				GrantDefinition newdef = new GrantDefinition(objid);
				newdef.setUser(model.getUser());
				GrantDefinition olddef = (GrantDefinition) newdef.clone();

				ArrayList grants = null;
				for (int row = 0; row < model.getRowCount(); row++) {
					OrthoGrantDefinitionWrapper wrapper = (OrthoGrantDefinitionWrapper) model.getRow(row);
					Privilege priv = wrapper.getPrivilege();
					if (wrapper.isModified()) {
						/** the new grant */
						if (wrapper.isGranted(Privilege.GRANT)) {
							newdef.addPrivilege(priv);
						} else {
							olddef.addPrivilege(priv);
						}

						if (grants == null) {
							grants = new ArrayList();
							GrantDefinition[] gdefs = new GrantDefinition[2];
							gdefs[0] = newdef;
							gdefs[1] = olddef;
							grants.add(gdefs);
						}
					}
				}

				if (grants == null) {
					String msg = I18N.getLocalizedMessage("Nothing to commit");
					TSGuiToolbox.showErrorDialog(msg);
				} else {
					showCommitDialog(grants);
				}
			} else {
				super.actionPerformed(evt);
			}
		}
	}

	/**
	 * ActionListener for reloading grants
	 */
	public class MySQLReloadGrantsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			GrantsView view = (GrantsView) getView();
			String filter = view.getFilter();
			// replace % in case user thinks filter behaves LIKE %
			filter = filter.replace('%', '*');
			String regex = null;
			if (filter.length() > 0) {
				regex = TSUtils.wildCardToRegex(filter);
			}

			view.setGrantsModelType(view.getSelectedObjectType());

			AbstractUser user = view.getSelectedUser();
			DbObjectType objtype = view.getSelectedObjectType();
			if (user != null) {
				view.setGrantsHeading(I18N.format("object_privileges_for_2", objtype.toString(),
						user.getQualifiedName()));
				if (objtype == MySQLObjectType.GLOBAL) {
					GlobalGrantsModel gmodel = (GlobalGrantsModel) view.getGrantsModel();
					gmodel.reload(objtype, view.getCatalog(), Schema.VIRTUAL_SCHEMA, user, regex);
				} else if (objtype == DbObjectType.TABLE) {
					TableGrantsModel gmodel = (TableGrantsModel) view.getGrantsModel();
					gmodel.reload(objtype, view.getCatalog(), user, regex);
				} else if (objtype == DbObjectType.COLUMN) {
					TSComboBox tablescombo = (TSComboBox) view.getComponentByName(GrantsView.ID_TABLES_COMBO);
					TableId tableid = (TableId) tablescombo.getSelectedItem();
					if (tableid != null) {
						ColumnGrantsModel cmodel = (ColumnGrantsModel) view.getGrantsModel();
						cmodel.reload(tableid, user, regex);
					}
				}
			}
		}
	}

}
