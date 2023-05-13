package com.jeta.abeille.gui.formbuilder;

import java.awt.Component;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JTextField;

import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewNames;
import com.jeta.abeille.gui.model.ModelViewUIDirector;
import com.jeta.abeille.gui.model.TableWidget;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is used for updating the frame toolbar/menus based on the state of
 * the model
 * 
 * @author Jeff Tassin
 */
public class FormBuilderFrameUIDirector extends ModelViewUIDirector {
	/** the frame we are updating */
	private FormBuilderFrame m_frame;

	/** the model that the frame is based on */
	private FormModel m_model;

	/** the form view (that is enclosed by the m_frame) */
	private FormView m_formview;

	/**
	 * ctor
	 */
	public FormBuilderFrameUIDirector(FormBuilderFrame frame) {
		super(frame, frame);
		m_frame = frame;
		m_model = frame.getModel();
		m_formview = frame.getFormView();
	}

	/**
	 * Updates the UI
	 */
	public void updateComponents(java.util.EventObject evt) {
		super.updateComponents(evt);

		/** allow any sub-views to update here */
		ColumnsView cv = m_formview.getColumnsView();
		UIDirector uidirector = cv.getUIDirector();
		if (uidirector != null)
			uidirector.updateComponents(evt);

		JTextField anchorcell = (JTextField) m_frame.getComponentByName(FormBuilderFrame.ID_ANCHOR_CELL);
		assert (anchorcell != null);
		TableId id = m_model.getAnchorTable();
		if (id == null) {
			anchorcell.setText("");
			m_frame.enableComponent(FormNames.ID_SHOW_FORM, false);
		} else {
			anchorcell.setText(id.getFullyQualifiedName());
			if (m_model.getColumnCount() == 0) {
				m_frame.enableComponent(FormNames.ID_SHOW_FORM, false);
			} else {
				m_frame.enableComponent(FormNames.ID_SHOW_FORM, true);
			}
		}

		ModelView modelview = m_formview.getModelView();
		Collection items = modelview.getSelectedItems();
		TableWidget widget = null;
		Iterator iter = items.iterator();
		if (iter.hasNext()) {
			Component comp = (Component) iter.next();
			if (comp instanceof TableWidget)
				widget = (TableWidget) comp;
		}

		if (items.size() == 0) {
			m_frame.enableComponent(FormNames.ID_REMOVE_TABLE, false);
		} else {
			m_frame.enableComponent(FormNames.ID_REMOVE_TABLE, true);
		}

		if (m_model.getTableWidgetCount() == 0) {
			m_frame.enableComponent(FormNames.ID_ADD_COLUMN, false);

		} else {
			m_frame.enableComponent(FormNames.ID_ADD_COLUMN, true);
		}

		if (widget == null) {
			m_frame.enableComponent(FormNames.ID_UPDATE_TABLE, false);
			m_frame.enableComponent(FormNames.ID_QUERY_TABLE, false);
			m_frame.enableComponent(FormNames.ID_ANCHOR, false);
			m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS, false);
			m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS_QUALIFIED, false);
		} else {
			m_frame.enableComponent(FormNames.ID_UPDATE_TABLE, true);
			m_frame.enableComponent(FormNames.ID_QUERY_TABLE, true);
			m_frame.enableComponent(FormNames.ID_ANCHOR, true);
			m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS, true);
			m_frame.enableComponent(ModelViewNames.ID_COPY_JOINS_QUALIFIED, true);
		}
	}
}
