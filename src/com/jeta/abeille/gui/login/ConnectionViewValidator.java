package com.jeta.abeille.gui.login;

import java.io.File;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.Database;

import com.jeta.foundation.i18n.I18N;
import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

public class ConnectionViewValidator implements JETARule {

	/**
	 * JETARule implementation. This rule expects a parameter array of at least
	 * 1 element. The first element must be a ConnectionView object.
	 * 
	 */
	public RuleResult check(Object[] params) {
		assert (params != null);
		assert (params.length > 0);
		assert (params[0] instanceof ConnectionView);

		BasicConnectionView view = (BasicConnectionView) params[0];

		String name = view.getName();
		if ((view.getDatabase() != Database.HSQLDB) && (name == null || name.trim().length() == 0)) {
			return new RuleResult(I18N.getLocalizedMessage("Connection_name_cannot_be_empty"));
		}

		String jar = view.getJDBCJar();
		File f = new File(jar);
		if (!f.isFile()) {
			return new RuleResult(I18N.format("Unable_locate_JDBC_Jar", jar));
		}

		if (view.getPort() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Port number must be greater than zero"));
		}

		return RuleResult.SUCCESS;
	}
}
