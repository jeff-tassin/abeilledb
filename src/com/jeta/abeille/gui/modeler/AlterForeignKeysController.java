package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSForeignKeys;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.utils.DropDialog;
import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the ForeignKeysView GUI for an existing table.
 * Changes are immediately effected against the database.
 * 
 * @author Jeff Tassin
 */
public class AlterForeignKeysController extends TSController {
	/** this is the view that this class handles events for */
	private ForeignKeysView m_view;

	/** the data model */
	private ForeignKeysModel m_model;

	/** temporary hack */
	private DbForeignKey m_newkey;

	/**
	 * Constructor
	 */
	public AlterForeignKeysController(ForeignKeysView view) {
		super(view);
		m_view = view;
		m_model = m_view.getModel();

		assignAction(ForeignKeysView.ID_ADD_FOREIGN_KEY, new AddForeignKeyAction());
		assignAction(ForeignKeysView.ID_DROP_FOREIGN_KEY, new DeleteForeignKeyAction());
		assignAction(ForeignKeysView.ID_EDIT_FOREIGN_KEY, new ModifyForeignKeyAction());

		JComponent comp = (JComponent) m_view.getComponentByName(ForeignKeysView.ID_FOREIGN_KEYS_TABLE);
		comp.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				if (evt.getClickCount() == 2 && m_view.isEditable()) {
					invokeAction(ForeignKeysView.ID_EDIT_FOREIGN_KEY);
				}
			}
		});

		ForeignKeysUIDirector uidirector = new ForeignKeysUIDirector(m_view);
		m_view.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Modifies the given foreign key. If the key is null, we assume we are
	 * adding a new key
	 * 
	 */
	private void modifyKey(ForeignKeyWrapper oldKey) {
		m_newkey = null;

		SQLCommandDialog dlg = SQLCommandDialog.createDialog(m_view.getConnection(), true);
		String dlgmsg = null;
		if (oldKey == null) {
			dlgmsg = I18N.getLocalizedMessage("Create Foreign Key");
		} else {
			dlgmsg = I18N.getLocalizedMessage("Modify Foreign Key");
		}

		TSConnection tsconn = m_view.getConnection();
		ModelerFactory factory = ModelerFactory.getFactory(tsconn);
		final ForeignKeyView view = factory.createForeignKeyView(m_view,
				(oldKey == null ? null : oldKey.getForeignKey()));

		dlg.setMessage(dlgmsg);
		dlg.setPrimaryPanel(view);
		dlg.addValidator(view, view.getValidatorRule());
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());

		if (oldKey != null) {
			if (m_view.getConnection().getDatabase().equals(Database.MYSQL)) {
				dlg.enableComponent(SQLCommandDialog.ID_OK, false);
				dlg.enableComponent(SQLCommandDialog.ID_SQL_BUTTON, false);
			}
		}

		final ForeignKeyWrapper oldfkey = oldKey;
		dlg.addDialogListener(new SQLDialogListener() {
			public boolean cmdOk() throws SQLException {
				try {
					m_newkey = view.createForeignKey();
					TSForeignKeys fksrv = (TSForeignKeys) m_model.getConnection().getImplementation(
							TSForeignKeys.COMPONENT_ID);
					if (oldfkey == null) {
						fksrv.createForeignKey(m_newkey);
					} else {
						DbForeignKey old = oldfkey.getForeignKey();
						assert (old.getLocalTableId().equals(m_newkey.getLocalTableId()));
						fksrv.modifyForeignKey(m_newkey, old);
					}
					return true;
				} catch (SQLException se) {
					/**
					 * MySQL allows foreign keys under very limited conditions.
					 * If we detect this situation, then post a MySQL message
					 * describing the foreign key requirements when creating a
					 * table
					 */
					if (m_model.getConnection().getDatabase().equals(Database.MYSQL)) {
						String errmsg = se.getMessage();
						if (errmsg != null && (errmsg.indexOf("errno: 150") >= 0)) {
							StringBuffer msg = new StringBuffer();
							msg.append(errmsg);
							msg.append('\n');
							msg.append(I18N.getLocalizedMessage("mysql_create_table_error"));
							se = new SQLException(msg.toString());
						}
					}
					throw se;
				}
			}
		});
		dlg.showCenter();
		if (dlg.isOk()) {
			if (oldfkey == null) {
				m_view.getModel().addKey(m_newkey);
			} else {
				DbForeignKey old = oldfkey.getForeignKey();
				m_view.getModel().modifyKey(m_newkey, old);
			}

			TableId localid = m_newkey.getLocalTableId();
			m_model.getConnection().getModel(localid.getCatalog()).reloadTable(localid);
		}
	}

	/**
	 * Command handler for adding foreign key
	 */
	public class AddForeignKeyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			modifyKey(null);
		}
	}

	/**
	 * 
	 * Command handler for deleting the selected foreign key
	 */
	public class DeleteForeignKeyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ForeignKeyWrapper wrapper = m_view.getSelectedForeignKey();
			if (wrapper != null) {
				final DbForeignKey fkey = wrapper.getForeignKey();
				String msg = I18N.format("Drop_1", fkey.getName());
				final DropDialog dlg = DropDialog.createDropDialog(m_model.getConnection(), m_view, true);
				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						TSForeignKeys fksrv = (TSForeignKeys) m_model.getConnection().getImplementation(
								TSForeignKeys.COMPONENT_ID);
						fksrv.dropForeignKey(fkey, dlg.isCascade());
						return true;
					}
				});

				dlg.showCenter();
				if (dlg.isOk()) {
					m_view.getModel().deleteKey(wrapper);
					TableId localid = fkey.getLocalTableId();
					m_model.getConnection().getModel(localid.getCatalog()).reloadTable(localid);
				}
			}
		}
	}

	/**
	 * Edits a foriegn key. Only allows the user to change the name
	 */
	public class ModifyForeignKeyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ForeignKeyWrapper wrapper = m_view.getSelectedForeignKey();
			if (wrapper != null) {
				modifyKey(wrapper);
			}
		}
	}
}
