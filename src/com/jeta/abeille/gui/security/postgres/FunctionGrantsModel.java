package com.jeta.abeille.gui.security.postgres;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Privilege;
import com.jeta.abeille.database.security.SecurityService;

import com.jeta.abeille.gui.security.GrantsModel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This table model displays the grants for a list of functions in a given
 * schema for a given user.
 * 
 * @author Jeff Tassin
 */
public class FunctionGrantsModel extends GrantsModel {
	/** column definitions */
	static final int OWN_COLUMN = 1;
	static final int EXECUTE_COLUMN = 2;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public FunctionGrantsModel(TSConnection connection) {
		super(connection);

		String[] names = { I18N.getLocalizedMessage("Object Name"), I18N.getLocalizedMessage("Own"),
				I18N.getLocalizedMessage("Execute") };

		Class[] types = { String.class, Boolean.class, Boolean.class };

		setColumnGrantDefinition(OWN_COLUMN, new ColumnGrantDefinition(Privilege.OWN, false));
		setColumnGrantDefinition(EXECUTE_COLUMN, new ColumnGrantDefinition(Privilege.EXECUTE));
		setColumnNames(names);
		setColumnTypes(types);
	}

}
