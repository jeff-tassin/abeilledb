package com.jeta.abeille.gui.table;

import java.lang.ref.WeakReference;

import com.jeta.abeille.gui.views.ViewInfoView;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;

/**
 * The UIDirector for the table frame class. This class is responsible for
 * enabling/disabling the controls on the TableFrame
 * 
 * @author Jeff Tassin
 */
public class TableFrameUIDirector implements UIDirector {
	/**
	 * the weak reference to the frame. We don't need a hard ref here If the
	 * frame wants to go away, then fine.
	 */
	private WeakReference m_frameref;

	/**
	 * ctor
	 */
	public TableFrameUIDirector(TableFrame frame) {
		m_frameref = new WeakReference(frame);
	}

	/**
	 * @return the frame window we are updating components for
	 */
	private TableFrame getFrame() {
		return (TableFrame) m_frameref.get();
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		TableFrame frame = getFrame();
		if (frame != null) {
			if (frame.getTableId() == null) {
				frame.enableComponent(TableFrame.ID_SHOW_TABLE_FORM, false);
				frame.enableComponent(TableFrame.ID_QUERY_TABLE, false);
			} else {
				frame.enableComponent(TableFrame.ID_SHOW_TABLE_FORM, true);
				frame.enableComponent(TableFrame.ID_QUERY_TABLE, true);
			}

			TableView view = frame.getTableView();
			if (view != null) {
				view.updateComponents(evt);
			}

			ViewInfoView viv = frame.getViewInfoView();
			if (viv != null) {
				viv.updateComponents(evt);
			}
		}
	}
}
