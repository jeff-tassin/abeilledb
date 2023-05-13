package com.jeta.abeille.gui.procedures;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.SQLException;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.procedures.StoredProcedureService;

import com.jeta.abeille.gui.model.ObjectTreeViewController;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.utils.DropDialog;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.abeille.gui.store.ConnectionContext;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETADialogListener;

/**
 * This is the main controller for the ProcedureTreeView It responds to user
 * events from the ProcedureTreeView.
 * 
 * @author Jeff Tassin
 */
public class ProcedureTreeController extends TSController {
	private ProcedureTree m_view;

	public ProcedureTreeController(ProcedureTree view) {
		super(view);
		m_view = view;

		assignAction(ProcedureNames.ID_DROP_PROCEDURE, new DropProcedureAction());
		assignAction(ProcedureNames.ID_VIEW_PROCEDURE, new ShowProcedureAction());

		// setUIDirector( new ProcedureTreeUIDirector( m_view ) );

		m_view.getTree().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				if (evt.getClickCount() == 2) {

				}
			}
		});
	}

	/**
	 * Action handler for drop procedure command.
	 */
	public class DropProcedureAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final ObjectTreeNode otnode = m_view.getSelectedNode();
			StoredProcedure proc = null;
			if (otnode != null) {
				Object userobj = otnode.getUserObject();
				if (userobj instanceof StoredProcedure)
					proc = (StoredProcedure) userobj;
			}

			final StoredProcedure oldproc = proc;
			if (oldproc != null) {
				String msg = I18N.format("Drop_1", proc.getName());
				final DropDialog dlg = DropDialog
						.createDropDialog(m_view.getConnection(otnode), m_view.getTree(), true);
				dlg.setMessage(msg);
				dlg.addDialogListener(new JETADialogListener() {
					public boolean cmdOk() {
						try {
							StoredProcedureService tsprocs = (StoredProcedureService) m_view.getConnection(otnode)
									.getImplementation(StoredProcedureService.COMPONENT_ID);

							tsprocs.dropProcedure(oldproc, dlg.isCascade());
							return true;
						} catch (SQLException e) {
							SQLErrorDialog.showErrorDialog(m_view.getTree(), e, null);
							return false;
						}
					}
				});

				dlg.showCenter();
				if (dlg.isOk()) {
					m_view.getModel().removeNodeFromParent(otnode);
				}
			}
		}
	}

	/**
	 * Action handler for edit procedure command. Invokes the procedure
	 * editor/viewer dialog for the selected procedure
	 */
	public class EditProcedureAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

		}
	}

	/**
	 * Action handler that allows the user to invoke a procedure
	 */
	public class InvokeProcedure implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			StoredProcedure proc = m_view.getSelectedProcedure();
			if (proc != null) {
				TSConnection tsconn = m_view.getSelectedConnection();
				if (tsconn != null) {
					ProcedureModel procmodel = new ProcedureModel(tsconn, proc);
					ProcedureRunner runner = new ProcedureRunner(tsconn, procmodel);
					runner.invokeProcedure();
				}
			}
		}
	}

	/**
	 * Reloads the procedures from the database
	 */
	public class ReloadProceduresAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// ProcedureTreeModel model =
			// (ProcedureTreeModel)m_view.getClassModel();
			// save folder hierarchy
			// model.saveState( m_view.getConnection() );

			// StoredProcedureService tsprocs =
			// (StoredProcedureService)m_view.getConnection().getImplementation(
			// StoredProcedureService.COMPONENT_ID );
			// tsprocs.reload();
			// m_view.initialize();
		}
	}

	/**
	 * Action handler that allows the user to edit the invoke options for a
	 * procedure.
	 */
	public class ShowInvokeOptions implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			StoredProcedure proc = m_view.getSelectedProcedure();
			if (proc != null) {
				ProcedureModel procmodel = new ProcedureModel(m_view.getSelectedConnection(), proc);
				// ParametersModel model = new ParametersModel(
				// procmodel.getReturnType(), procmodel.getParameters() );
				ParametersView view = new ParametersView();
				// view.setModel( procmodel );
				ParametersController controller = new ParametersController(view);
				view.setController(controller);

				InvokeOptionsDialog dlg = (InvokeOptionsDialog) TSGuiToolbox.createDialog(InvokeOptionsDialog.class,
						m_view.getTree(), true);
				dlg.initialize(m_view.getSelectedConnection(), procmodel, view);
				TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					// view.saveToModel();
					// procmodel.setParameters( model );
				}
			}
		}
	}

	/**
	 * Action handler for show procedure command. Invokes the procedure
	 * editor/viewer frame for the selected procedure
	 */
	public class ShowProcedureAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			ProcedureFrame pframe = (ProcedureFrame) wsframe.show(ProcedureFrame.class, m_view.getSelectedConnection());
			if (pframe != null) {
				StoredProcedure proc = m_view.getSelectedProcedure();
				if (proc != null) {
					pframe.showProcedure(proc);
				}
			}
		}
	}

}
