package com.jeta.abeille.gui.procedures;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.procedures.ProcedureLanguage;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.procedures.StoredProcedureService;

import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLOptionDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * The controller class for the SourceView
 * 
 * @author Jeff Tassin
 */
public class SourceViewController extends TSController implements UIDirector {
	/** the source view */
	private SourceView m_view;

	/**
	 * ctor
	 */
	public SourceViewController(SourceView view) {
		super(view);
		m_view = view;
		assignAction(SourceView.ID_COMPILE_BTN, new CompileAction());
		view.setUIDirector(this);
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		StoredProcedure proc = m_view.getModel().getProcedure();
		m_view.enableComponent(SourceView.ID_COMPILE_BTN, proc != null);
	}

	/**
	 * Compiles the procedure
	 */
	public class CompileAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ProcedureModel model = m_view.getModel();
			final StoredProcedure oldproc = model.getProcedure();
			if (oldproc != null) {
				ProcedureLanguage plang = model.getLanguage();
				String lang = plang.getLanguage();

				if (!"sql".equalsIgnoreCase(lang) && !"plpgsql".equalsIgnoreCase(lang)) {
					TSGuiToolbox.showErrorDialog(I18N.getLocalizedMessage("Invalid_procedure_language_compile"));
					return;
				}

				String msg = I18N.format("Compile_1", oldproc.getName());

				SQLOptionDialog dlg = SQLOptionDialog.createOptionDialog(m_view.getModel().getConnection(), m_view,
						true);
				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						StoredProcedure newproc = (StoredProcedure) oldproc.clone();
						newproc.setSource(m_view.getSource());
						StoredProcedureService procsrv = (StoredProcedureService) m_view.getModel().getConnection()
								.getImplementation(StoredProcedureService.COMPONENT_ID);

						procsrv.modifyProcedure(newproc, oldproc);
						return true;
					}
				});
				dlg.showCenter();
			}
		}
	}

}
