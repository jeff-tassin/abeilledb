package com.jeta.abeille.gui.login;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.Database;

import com.jeta.foundation.i18n.I18N;
import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

public class AdvancedConnectionViewValidator implements JETARule {

	/**
	 * JETARule implementation. This rule expects a parameter array of at least
	 * 1 element. The first element must be a ConnectionView object.
	 * 
	 */
	public RuleResult check(Object[] params) {
		assert (params != null);
		assert (params.length > 0);
		assert (params[0] instanceof ConnectionView);

		AdvancedConnectionView view = (AdvancedConnectionView) params[0];

		String driver = view.getDriver();
		if (driver == null || driver.length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Driver_name_cannot_be_empty"));
		}

		String url = view.getUrl();
		if (url == null || url.length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Url_cannot_be_empty"));
		}

		Collection jars = view.getJars();
		if (jars.size() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Jar_File_Required"));
		} else {
			Iterator iter = jars.iterator();
			while (iter.hasNext()) {
				String jar = (String) iter.next();
				File f = new File(jar);
				if (!f.isFile()) {
					return new RuleResult(I18N.format("Unable_locate_JDBC_Jar", jar));
				}
			}
		}

		if (Database.DAFFODIL.equals(view.getDatabase()) && Database.POINTBASE.equals(view.getDatabase())) {
			if (view.isEmbedded()) {
				File f = new File(view.getDatabasePath());
				if (!f.isDirectory()) {
					return new RuleResult(I18N.getLocalizedMessage("Invalid Database Directory"));
				}
			}
		}

		return RuleResult.SUCCESS;
	}
}
