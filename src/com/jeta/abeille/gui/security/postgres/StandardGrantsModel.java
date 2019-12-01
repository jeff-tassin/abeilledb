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
 * This table model displays the grants for a list of tables, sequences, and
 * views in a given schema for a given user.
 * 
 * @author Jeff Tassin
 */
public class StandardGrantsModel extends GrantsModel {

	/** column definitions */
	static final int OWN_COLUMN = 1;
	static final int SELECT_COLUMN = 2;
	static final int INSERT_COLUMN = 3;
	static final int UPDATE_COLUMN = 4;
	static final int DELETE_COLUMN = 5;
	static final int RULE_COLUMN = 6;
	static final int REF_COLUMN = 7;
	static final int TRIGGER_COLUMN = 8;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public StandardGrantsModel(TSConnection connection) {
		super(connection);

		String[] names = { I18N.getLocalizedMessage("Object Name"), I18N.getLocalizedMessage("Own"),
				I18N.getLocalizedMessage("Select"), I18N.getLocalizedMessage("Insert"),
				I18N.getLocalizedMessage("Update"), I18N.getLocalizedMessage("Delete"),
				I18N.getLocalizedMessage("Rule"), I18N.getLocalizedMessage("Ref"), I18N.getLocalizedMessage("Trigger") };

		Class[] types = { String.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class,
				Boolean.class, Boolean.class, Boolean.class };

		setColumnGrantDefinition(OWN_COLUMN, new ColumnGrantDefinition(Privilege.OWN, false));
		setColumnGrantDefinition(SELECT_COLUMN, new ColumnGrantDefinition(Privilege.SELECT));
		setColumnGrantDefinition(INSERT_COLUMN, new ColumnGrantDefinition(Privilege.INSERT));
		setColumnGrantDefinition(UPDATE_COLUMN, new ColumnGrantDefinition(Privilege.UPDATE));
		setColumnGrantDefinition(DELETE_COLUMN, new ColumnGrantDefinition(Privilege.DELETE));
		setColumnGrantDefinition(RULE_COLUMN, new ColumnGrantDefinition(Privilege.RULE));
		setColumnGrantDefinition(REF_COLUMN, new ColumnGrantDefinition(Privilege.REFERENCES));
		setColumnGrantDefinition(TRIGGER_COLUMN, new ColumnGrantDefinition(Privilege.TRIGGER));

		setColumnNames(names);
		setColumnTypes(types);
	}

}
