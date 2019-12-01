package com.jeta.abeille.gui.procedures;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;

/**
 * Top level controller for the procedure view/editor
 * 
 * @author Jeff Tassin
 */
public class ProcedureViewController extends TSController {
	/** the view we are controlling */
	private ProcedureView m_view;

	/** the parameters view that is embedded withing the procedure view */
	private ParametersView m_parametersview = null;

	/**
	 * ctor
	 * 
	 * @param view
	 *            the procedure view. The model must already be set here
	 */
	public ProcedureViewController(ProcedureView view) {
		super(view);
		m_view = view;
		// m_parametersview = (ParametersView)m_view.getComponentByName(
		// ProcedureView.ID_PARAMETERS_VIEW );
		// setUIDirector( new ProcedureViewUIDirector() );
	}

	/**
	 * UI updater
	 */
	public class ProcedureViewUIDirector implements UIDirector {
		public void updateComponents(java.util.EventObject evt) {
			if (m_parametersview != null) {
				UIDirector uidirector = m_parametersview.getUIDirector();
				if (uidirector != null)
					uidirector.updateComponents(evt);
			}
		}
	}

}
