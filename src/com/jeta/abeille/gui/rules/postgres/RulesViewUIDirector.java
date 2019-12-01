package com.jeta.abeille.gui.rules.postgres;

import java.lang.ref.WeakReference;

import com.jeta.open.gui.framework.UIDirector;

/**
 * The UIDirector for the RulesView. This class is responsible for
 * enabling/disabling the controls on the RulesView
 * 
 * @author Jeff Tassin
 */
public class RulesViewUIDirector implements UIDirector {
	/**
	 * the weak reference to the frame. We don't need a hard ref here If the
	 * frame wants to go away, then fine.
	 */
	private WeakReference m_viewref;

	/**
	 * ctor
	 */
	public RulesViewUIDirector(RulesView view) {
		m_viewref = new WeakReference(view);
	}

	/**
	 * @return the frame window we are updating components for
	 */
	private RulesView getView() {
		return (RulesView) m_viewref.get();
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		RulesView view = getView();
		if (view != null) {
			if (view.getModel().getTableId() == null) {
				view.enableComponent(RulesView.ID_CREATE_RULE, false);
				view.enableComponent(RulesView.ID_EDIT_RULE, false);
				view.enableComponent(RulesView.ID_DROP_RULE, false);
			} else {
				view.enableComponent(RulesView.ID_CREATE_RULE, true);
				Rule rule = view.getSelectedRule();
				boolean enable = (rule != null);
				view.enableComponent(RulesView.ID_EDIT_RULE, enable);
				view.enableComponent(RulesView.ID_DROP_RULE, enable);
			}
		}
	}
}
