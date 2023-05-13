package com.jeta.abeille.gui.modeler;

import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.AbstractRule;
import com.jeta.open.rules.RuleResult;

/**
 * Validator for ForeignKeyView
 * 
 * @author Jeff Tassin
 */
public class ForeignKeyValidatorRule extends AbstractRule {

	/**
	 * ctor
	 */
	public ForeignKeyValidatorRule() {

	}

	/**
	 * JETARule implementation. Validates the data in the view. Null is returned
	 * if the input is valid.
	 */
	public RuleResult check(Object[] params) {
		assert (params != null);

		RuleResult result = checkName(params);
		if (result.equals(RuleResult.SUCCESS))
			result = checkAssignments(params);

		return result;
	}

	/**
	 * Validation: Validates the data in the view. Null is returned if the input
	 * is valid.
	 */
	public RuleResult checkName(Object[] params) {
		ForeignKeyView view = (ForeignKeyView) params[0];
		String keyname = view.getName();
		if (keyname.length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		} else {
			return RuleResult.SUCCESS;
		}
	}

	/**
	 * Validation: Make sure the column assignments are valid
	 */
	public RuleResult checkAssignments(Object[] params) {

		ForeignKeyView view = (ForeignKeyView) params[0];
		ColumnMetaData[][] assign = view.getAssignments();
		if (assign == null || assign.length == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Column Assignments"));
		}

		for (int index = 0; index < assign.length; index++) {
			if (assign[index][0] == null || assign[index][1] == null)
				return new RuleResult(I18N.getLocalizedMessage("Invalid Column Assignments"));
		}
		return RuleResult.SUCCESS;
	}

}
