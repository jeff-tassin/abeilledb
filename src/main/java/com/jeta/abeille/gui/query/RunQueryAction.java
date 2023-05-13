package com.jeta.abeille.gui.query;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ConnectionReference;

import com.jeta.abeille.gui.command.ModalCommandRunner;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.sql.SQLMediator;
import com.jeta.abeille.gui.sql.SQLMediatorListener;
import com.jeta.abeille.gui.sql.SQLResultsFrame;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.JETAFrameEvent;
import com.jeta.foundation.gui.components.JETAFrameListener;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.open.rules.RuleResult;
import com.jeta.foundation.utils.TSUtils;

/**
 * Runs the current query
 */
public class RunQueryAction implements ActionListener, JETAFrameListener {
	/** the querybuilder controller assocated with this action */
	private QueryBuilderController m_controller;

	/**
	 * the results frame. we keep this reference in case the user wants to
	 * re-use the frame
	 */
	private SQLResultsFrame m_resultsframe = null;

	/**
	 * ctor
	 */
	public RunQueryAction(QueryBuilderController controller) {
		m_controller = controller;
	}

	/**
	 * ActionListener implementation
	 */
	public void actionPerformed(ActionEvent evt) {
		Component comp = (Component) evt.getSource();

		boolean new_window = QueryNames.ID_RUN_QUERY_NEW_WINDOW.equals(comp.getName());

		QueryBuilderFrame frame = m_controller.getFrame();
		frame.saveViewToModel();
		ModelView view = frame.getModelView();
		QueryModel querymodel = (QueryModel) view.getModel();
		String sql = querymodel.getSQL();

		RuleResult result = m_controller.check(querymodel);
		if (result == RuleResult.SUCCESS) {
			try {
				TSConnection tsconn = m_controller.getConnection();
				ConnectionReference cref = new ConnectionReference(tsconn, tsconn.getWriteConnection());

				AdvancedQueryCommand a_cmd = new AdvancedQueryCommand(I18N.getLocalizedMessage("Running Query"), cref,
						querymodel.getSQL());

				SQLMediator mediator = a_cmd.getMediator();

				ModalCommandRunner crunner = new ModalCommandRunner(tsconn, (java.awt.Component) null, a_cmd);
				if (crunner.invoke() == ModalCommandRunner.COMPLETED) {
					// the user may have canceled the query if there were inputs
					// and the input dialog was canceled,
					// so we need to check for success here.
					if (mediator.isSuccess()) {
						prepareResultsFrame(mediator, new_window);
					}
				} else {
					// show error window
					try {
						cref.rollback();
					} catch (SQLException e) {
						TSUtils.printException(e);
					}
					SQLErrorDialog.logErrorDialog(a_cmd.getMediator().getException(), a_cmd.getMediator().getLastSQL(),
							true);
				}
			} catch (SQLException se) {
				// no need to log here because the error occured before the
				// query
				SQLErrorDialog.showErrorDialog(frame, se, sql);
			}
		} else {
			if (result.getCode() == RuleResult.FAIL_MESSAGE_ID) {
				String title = I18N.getLocalizedMessage("Error");
				JOptionPane.showMessageDialog(null, result.getMessage(), title, JOptionPane.ERROR_MESSAGE);
			} else {
				assert (false);
			}
		}
	}

	TSConnection getConnection() {
		return m_controller.getConnection();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////
	// internal frame listener implementation
	// we implement to support sending results to the same query window, if the
	// user closes the window
	// then we need to know about it

	public void jetaFrameActivated(JETAFrameEvent e) {
	}

	public void jetaFrameClosed(JETAFrameEvent e) {
		System.out.println("RunQueryAction.jetaFrameClose....");
		if (e.getSource() == m_resultsframe) {
			m_resultsframe = null;
		} else {
			assert (false);
		}
	}

	public void jetaFrameClosing(JETAFrameEvent e) {
		System.out.println("RunQueryAction.jetaFrameClosing....");

		if (e.getSource() == m_resultsframe) {
			m_resultsframe = null;
		}
	}

	public void jetaFrameDeactivated(JETAFrameEvent e) {
	}

	public void jetaFrameDeiconified(JETAFrameEvent e) {
	}

	public void jetaFrameIconified(JETAFrameEvent e) {
	}

	public void jetaFrameOpened(JETAFrameEvent e) {
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This is an internal helper routine that prepares the sql results window
	 * to display a query. This routine checks if we should reuse the results
	 * window and handles this case accordingly.
	 */
	protected void prepareResultsFrame(SQLMediator mediator, boolean new_window) {
		System.out.println("RunQueryAction.prepareResultsFrame.....  m_resultsframe = " + m_resultsframe
				+ "  new_window = " + new_window);
		TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();

		if (new_window) {
			m_resultsframe = (SQLResultsFrame) TSWorkspaceFrame.getInstance().createInternalFrame(
					com.jeta.abeille.gui.sql.SQLResultsFrame.class, false, getConnection().getId());
			m_resultsframe.setSize(m_resultsframe.getPreferredSize());
			wsframe.addWindow(m_resultsframe, false);
		} else {
			if (m_resultsframe == null) {
				m_resultsframe = (SQLResultsFrame) TSWorkspaceFrame.getInstance().createInternalFrame(
						com.jeta.abeille.gui.sql.SQLResultsFrame.class, false, getConnection().getId());
				m_resultsframe.setSize(m_resultsframe.getPreferredSize());
				wsframe.addWindow(m_resultsframe, false);
			}
		}

		Object[] params = new Object[3];
		params[0] = getConnection();
		params[1] = mediator.getResultsManager();
		params[2] = null;
		m_resultsframe.initializeModel(params);

		m_resultsframe.removeFrameListener(this);
		m_resultsframe.addFrameListener(this);
		wsframe.centerWindow(m_resultsframe);
		wsframe.show(m_resultsframe);
	}

}
