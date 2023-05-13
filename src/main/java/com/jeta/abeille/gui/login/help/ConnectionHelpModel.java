package com.jeta.abeille.gui.login.help;

import com.jeta.foundation.gui.table.JETATableModel;

import com.jeta.foundation.i18n.I18N;

/**
 * A table model for displaying JDBC connection parameters for supported
 * databases.
 * 
 * @author Jeff Tassin
 */
public class ConnectionHelpModel extends JETATableModel {
	public ConnectionHelpModel() {
		/** database, driver, port, url, JAR, download */
		String[] names = new String[6];
		names[0] = I18N.getLocalizedMessage("Database");
		names[1] = I18N.getLocalizedMessage("Driver");
		names[2] = I18N.getLocalizedMessage("Port");
		names[3] = I18N.getLocalizedMessage("URL");
		names[4] = I18N.getLocalizedMessage("JAR File");
		names[5] = I18N.getLocalizedMessage("Download");
		setColumnNames(names);

		Class[] coltypes = new Class[6];
		coltypes[0] = String.class;
		coltypes[1] = String.class;
		coltypes[2] = String.class;
		coltypes[3] = String.class;
		coltypes[4] = String.class;
		coltypes[5] = String.class;

		setColumnTypes(coltypes);
	}
}
