package com.jeta.abeille.gui.procedures;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.abeille.database.procedures.ProcedureParameter;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the ParametersView. The ParametersView shows a
 * list of all parameters for a given stored procedure. It also allows the user
 * to add/edit/delete parameters.
 * 
 * @author Jeff Tassin
 */
public class ParametersController extends TSController {
	/** the view we are controlling */
	private ParametersView m_view;

	/**
	 * ctor
	 * 
	 * @param view
	 *            the view we are controlling
	 */
	public ParametersController(ParametersView view) {
		super(view);
		m_view = view;
		assignAction(ParametersView.ID_ADD_PARAMETER, new AddAction());
		assignAction(ParametersView.ID_EDIT_PARAMETER, new EditAction());
		assignAction(ParametersView.ID_DELETE_PARAMETER, new DeleteAction());
		assignAction(ParametersView.ID_MOVE_UP, new MoveUpAction());
		assignAction(ParametersView.ID_MOVE_DOWN, new MoveDownAction());
		UIDirector uidirector = new ParametersUIDirector();
		view.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Invokes the ParameterView dialog and allows the user to add a new
	 * parameter.
	 */
	public class AddAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ParameterView view = new ParameterView();
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
			dlg.setTitle(I18N.getLocalizedMessage("Add Parameter"));
			dlg.setPrimaryPanel(view);
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				ProcedureParameter param = view.getParameter();
				m_view.getModel().addParameter(param);
			}
		}
	}

	/**
	 * Deletes the selected parameter
	 */
	public class DeleteAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_view.deleteSelectedParameter();
		}
	}

	/**
	 * Invokes the ParameterView dialog and allows the user to edit the
	 * selected.
	 */
	public class EditAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			int row = m_view.getSelectedRow();
			ProcedureParameter oldparam = m_view.getSelectedParameter();
			if (oldparam != null) {
				ParameterView view = new ParameterView(oldparam);
				TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
				dlg.setTitle(I18N.getLocalizedMessage("Edit Parameter"));
				dlg.setPrimaryPanel(view);
				TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					ProcedureParameter newparam = view.getParameter();
					m_view.getModel().replaceParameter(newparam, oldparam);
				}
			}
		}
	}

	/**
	 * Moves the selected parameter up one
	 */
	public class MoveUpAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

		}
	}

	/**
	 * Moves the selected parameter down one
	 */
	public class MoveDownAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

		}
	}

	/**
	 * UI director for parameters view
	 */
	public class ParametersUIDirector implements UIDirector {
		public void updateComponents(java.util.EventObject evt) {
			ProcedureParameter param = m_view.getSelectedParameter();
			if (param == null) {
				m_view.enableComponent(ParametersView.ID_EDIT_PARAMETER, false);
				m_view.enableComponent(ParametersView.ID_DELETE_PARAMETER, false);
				m_view.enableComponent(ParametersView.ID_MOVE_UP, false);
				m_view.enableComponent(ParametersView.ID_MOVE_DOWN, false);
			} else {
				m_view.enableComponent(ParametersView.ID_EDIT_PARAMETER, true);
				m_view.enableComponent(ParametersView.ID_DELETE_PARAMETER, true);
				m_view.enableComponent(ParametersView.ID_MOVE_UP, true);
				m_view.enableComponent(ParametersView.ID_MOVE_DOWN, true);
			}
		}
	}

}
