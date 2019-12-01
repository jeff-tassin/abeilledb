package com.jeta.abeille.gui.modeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.modeler.mysql.MySQLForeignKeyView;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the ForeignKeysView GUI. It handles all button/gui
 * events.
 * 
 * @author Jeff Tassin
 */
public class ForeignKeysController extends TSController {
	/** this is the view that this class handles events for */
	private ForeignKeysView m_view;

	/** the data model */
	private ForeignKeysModel m_model;

	/**
	 * Constructor
	 */
	public ForeignKeysController(ForeignKeysView view) {
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
		ForeignKeyView view = null;

		TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
		String dlgmsg = null;
		if (oldKey == null) {
			dlgmsg = I18N.getLocalizedMessage("Create Foreign Key");
		} else {
			dlgmsg = I18N.getLocalizedMessage("Modify Foreign Key");
		}

		TSConnection tsconn = m_view.getConnection();

		ModelerFactory factory = ModelerFactory.getFactory(tsconn);
		view = factory.createForeignKeyView(m_view, (oldKey == null ? null : oldKey.getForeignKey()));
		dlg.addValidator(view, view.getValidatorRule());

		dlg.setTitle(dlgmsg);
		dlg.setPrimaryPanel(view);
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
		dlg.showCenter();
		if (dlg.isOk()) {
			DbForeignKey newkey = view.createForeignKey();
			if (oldKey == null)
				m_view.getModel().addKey(newkey);
			else
				m_view.getModel().modifyKey(newkey, oldKey.getForeignKey());
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
	 * Command handler for deleting the selected foreign key
	 */
	public class DeleteForeignKeyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ForeignKeyWrapper wrapper = m_view.getSelectedForeignKey();
			if (wrapper != null) {
				m_view.getModel().deleteKey(wrapper);
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

	/**
	 * Helper method to get the foreign key model for the panel.
	 * 
	 * @return the foreignkey model for the associated panel
	 */
	private ForeignKeysModel getModel() {
		return m_model;
	}

}
