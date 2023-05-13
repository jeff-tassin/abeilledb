package com.jeta.abeille.gui.export;

import com.jeta.open.gui.framework.UIDirector;

/**
 * The UIDirector for the export frame
 * 
 * @author Jeff Tassin
 */
public class ExportFrameUIDirector implements UIDirector {
	/** the frame we are updating */
	private ExportPanel m_view;

	/** the data model for the view */
	private SQLExportModel m_model;

	/**
	 * ctor
	 */
	public ExportFrameUIDirector(ExportPanel view) {
		m_view = view;
		m_model = view.getModel();
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		if (m_model.isExporting()) {
			m_view.enableComponent(ExportNames.ID_STOP_EXPORT, true);
			m_view.enableComponent(ExportNames.ID_START_EXPORT, false);
			m_view.enableComponent(ExportNames.ID_RESET, false);
			m_view.enableComponent(ExportNames.ID_SAMPLE_OUTPUT, false);
		} else {
			m_view.enableComponent(ExportNames.ID_STOP_EXPORT, false);
			m_view.enableComponent(ExportNames.ID_START_EXPORT, true);
			m_view.enableComponent(ExportNames.ID_RESET, true);
			m_view.enableComponent(ExportNames.ID_SAMPLE_OUTPUT, true);
		}
	}
}
