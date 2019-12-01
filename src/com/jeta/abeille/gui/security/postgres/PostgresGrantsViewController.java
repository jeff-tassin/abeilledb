package com.jeta.abeille.gui.security.postgres;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.GrantDefinition;

import com.jeta.abeille.gui.common.DbGuiUtils;

import com.jeta.abeille.gui.security.GrantsModel;
import com.jeta.abeille.gui.security.GrantsView;
import com.jeta.abeille.gui.security.GrantsViewController;

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

/**
 * The controller class for the PostgrseGrantsView
 * 
 * @author Jeff Tassin
 */
public class PostgresGrantsViewController extends GrantsViewController {

	/**
	 * ctor
	 */
	public PostgresGrantsViewController(GrantsView view) {
		super(view);

		assignAction(GrantsView.ID_RELOAD_GRANTS, new ReloadGrantsAction());
		assignAction(GrantsView.ID_COMMIT, new CommitAction());

		assignAction(GrantsView.ID_GRANT_SELECTION, new GrantSelectionAction());
		assignAction(GrantsView.ID_REVOKE_SELECTION, new RevokeSelectionAction());
		assignAction(GrantsView.ID_TOGGLE_SELECTION, new ToggleSelectionAction());

		view.setUIDirector(this);
		updateComponents(null);
	}

}
