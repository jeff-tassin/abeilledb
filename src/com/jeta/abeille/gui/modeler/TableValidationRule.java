package com.jeta.abeille.gui.modeler;

import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * Validator for standard TableMetaData objects
 * 
 * @author Jeff Tassin
 */
public class TableValidationRule implements JETARule {

	/**
	 * JETARule implementation. Validates the data in the view. Null is returned
	 * if the input is valid.
	 */
	public RuleResult check(Object[] params) {
		assert (params.length >= 2);
		assert (params[0] instanceof TSConnection);
		assert (params[1] instanceof TableMetaData);
		TSConnection conn = (TSConnection) params[0];
		TableMetaData tmd = (TableMetaData) params[1];

		if (tmd.getColumnCount() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Table requires one or more columns"));
		}
		return RuleResult.SUCCESS;
	}

}
