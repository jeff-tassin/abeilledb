package com.jeta.abeille.gui.views;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * Controller for ViewView class
 * 
 * @author Jeff Tassin
 */
public class ViewViewController extends TSController implements JETARule {
	/** the view we are handling events for */
	private ViewView m_view;

	/**
	 * ctor
	 */
	public ViewViewController(ViewView view) {
		super(view);
		m_view = view;
	}

	/**
	 * Overriden from TSController to provide a validation
	 * 
	 * @return an error message if the validation failed. Return null if the
	 *         validation succeeded
	 */
	public RuleResult check(Object[] params) {
		String name = m_view.getName();
		if (name.length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		}

		if (m_view.getSchema() == null) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Schema"));
		}

		String query = m_view.getDefinition();
		if (query.length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Definition"));
		}

		return RuleResult.SUCCESS;
	}
}
