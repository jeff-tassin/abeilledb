package com.jeta.abeille.gui.importer;

import java.awt.Component;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewUIDirector;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is used for updating the frame toolbar/menus based on the state of
 * the model
 * 
 * @author Jeff Tassin
 */
public class ImportBuilderFrameUIDirector extends ModelViewUIDirector {
	/** the frame we are updating */
	private ImportBuilderFrame m_frame;

	/** the model that the frame is based on */
	private ImportBuilderModel m_model;

	/** the form view (that is enclosed by the m_frame) */
	private ImportBuilderView m_view;

	/**
	 * ctor
	 */
	public ImportBuilderFrameUIDirector(ImportBuilderFrame frame) {
		super(frame, frame);
		m_frame = frame;
		m_model = frame.getModel();
		m_view = frame.getImportBuilderView();
	}

	/**
	 * Updates the UI
	 */
	public void updateComponents(java.util.EventObject evt) {
		super.updateComponents(evt);

		TSCell targetcell = (TSCell) m_frame.getComponentByName(ImportBuilderNames.ID_TARGET_CELL);
		assert (targetcell != null);
		TableId id = m_model.getTargetTable();
		if (id == null) {
			targetcell.setText("");
		} else {
			targetcell.setText(id.getFullyQualifiedName());
		}

		m_frame.enableComponent(ImportBuilderNames.ID_START_IMPORT, (m_model.getTargetTable() != null));

	}
}
