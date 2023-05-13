package com.jeta.abeille.gui.triggers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import com.jeta.abeille.database.triggers.Trigger;
import com.jeta.abeille.database.triggers.TriggerService;

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
public class TriggersViewController extends TSController implements UIDirector {
	/** the view we are controlling */
	private TriggersView m_view;

	public TriggersViewController(TriggersView view) {
		super(view);
		m_view = view;
		assignAction(TriggersView.ID_CREATE_TRIGGER, new CreateTriggerAction());
		assignAction(TriggersView.ID_EDIT_TRIGGER, new EditTriggerAction());
		assignAction(TriggersView.ID_DELETE_TRIGGER, new DropTriggerAction());

		view.setUIDirector(this);
		updateComponents(null);
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		if (m_view.getModel().getTableId() == null) {
			m_view.enableComponent(TriggersView.ID_CREATE_TRIGGER, false);
			m_view.enableComponent(TriggersView.ID_EDIT_TRIGGER, false);
			m_view.enableComponent(TriggersView.ID_DELETE_TRIGGER, false);
		} else {
			m_view.enableComponent(TriggersView.ID_CREATE_TRIGGER, true);
			TriggerWrapper wrapper = m_view.getSelectedTrigger();
			if (wrapper == null) {
				m_view.enableComponent(TriggersView.ID_EDIT_TRIGGER, false);
				m_view.enableComponent(TriggersView.ID_DELETE_TRIGGER, false);
			} else {
				m_view.enableComponent(TriggersView.ID_EDIT_TRIGGER, true);
				m_view.enableComponent(TriggersView.ID_DELETE_TRIGGER, true);
			}
		}
	}

	/**
	 * Action handler for creating a new trigger
	 */
	public class CreateTriggerAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			SQLCommandDialog dlg = SQLCommandDialog.createDialog(m_view.getModel().getConnection(), m_view, true);
			final TriggerView view = new TriggerView(m_view.getModel().getConnection(), m_view.getModel().getTableId());
			dlg.setMessage(I18N.getLocalizedMessage("Create Trigger"));
			dlg.setPrimaryPanel(view);
			dlg.addValidator((JETARule) view.getController());
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.addDialogListener(new SQLDialogListener() {
				public boolean cmdOk() throws SQLException {
					TriggerService triggersrv = (TriggerService) m_view.getModel().getConnection()
							.getImplementation(TriggerService.COMPONENT_ID);
					Trigger newtrigger = view.createTrigger();
					triggersrv.createTrigger(newtrigger);
					return true;
				}
			});
			dlg.showCenter();
			if (dlg.isOk()) {
				m_view.getModel().reload();
			}
		}
	}

	/**
	 * Action handler to delete the selected trigger
	 */
	public class DropTriggerAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final TriggerWrapper wrapper = m_view.getSelectedTrigger();
			if (wrapper != null) {
				String msg = I18N.format("Drop_1", wrapper.getName());
				final DropDialog dlg = DropDialog.createDropDialog(m_view.getModel().getConnection(), m_view, true);
				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						TriggerService tsrv = (TriggerService) m_view.getModel().getConnection()
								.getImplementation(TriggerService.COMPONENT_ID);
						tsrv.dropTrigger(wrapper.getTrigger(), dlg.isCascade());

						return true;
					}
				});

				dlg.showCenter();
				if (dlg.isOk()) {
					m_view.getModel().reload();
				}
			}
		}
	}

	/**
	 * Action handler for editing an existing trigger
	 */
	public class EditTriggerAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final TriggerWrapper wrapper = m_view.getSelectedTrigger();
			if (wrapper != null) {
				Trigger oldtrigger = wrapper.getTrigger();
				SQLCommandDialog dlg = SQLCommandDialog.createDialog(m_view.getModel().getConnection(), m_view, true);
				final TriggerView view = new TriggerView(m_view.getModel().getConnection(), wrapper);
				dlg.setMessage(I18N.getLocalizedMessage("Modify Trigger"));
				dlg.setPrimaryPanel(view);
				dlg.addValidator((JETARule) view.getController());
				TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						TriggerService triggersrv = (TriggerService) m_view.getModel().getConnection()
								.getImplementation(TriggerService.COMPONENT_ID);
						triggersrv.modifyTrigger(view.createTrigger(), wrapper.getTrigger());
						return true;
					}
				});
				dlg.showCenter();
				if (dlg.isOk()) {
					m_view.getModel().reload();
				}
			}
		}
	}

}
