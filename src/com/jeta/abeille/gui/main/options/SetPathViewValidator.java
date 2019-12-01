package com.jeta.abeille.gui.main.options;

import java.io.File;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.i18n.I18N;
import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

public class SetPathViewValidator implements JETARule {
	/**
	 * JETARule implementation. This rule expects a parameter array of at least
	 * 1 element. The first element must be a SetPathView object.
	 * 
	 */
	public RuleResult check(Object[] params) {
		assert (params != null);
		assert (params.length > 0);

		SetPathView view = (SetPathView) params[0];
		TSConnection tsconn = view.getConnection();
		if (!tsconn.contains(view.getCatalog(), view.getSchema())) {
			return new RuleResult(I18N.getLocalizedMessage("Path does not exist in the current database"));
		}

		return RuleResult.SUCCESS;
	}
}
