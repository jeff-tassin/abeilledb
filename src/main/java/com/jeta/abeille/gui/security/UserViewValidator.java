package com.jeta.abeille.gui.security;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;
import com.jeta.foundation.utils.TSUtils;

/**
 * Validation rule for UserView class
 * 
 * @author Jeff Tassin
 */
public class UserViewValidator implements JETARule {

	/**
	 * ctor
	 */
	public UserViewValidator() {

	}

	/**
	 * Overriden from TSController to provide a validation
	 * 
	 * @return an error message if the validation failed. Return null if the
	 *         validation succeeded
	 */
	public RuleResult check(Object[] params) {

		UserView view = (UserView) params[0];

		String username = view.getUserName();
		if (username.trim().length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid User Name"));
		}

		// check password matches confirmed password if both are not empty
		char[] password = view.getPassword();
		char[] confirmpw = view.getConfirmPassword();
		if (password.length > 0 || confirmpw.length > 0) {
			if (!I18N.equals(password, confirmpw)) {
				return new RuleResult(I18N.getLocalizedMessage("Passwords do not match"));
			}
		}

		return RuleResult.SUCCESS;
	}

}
