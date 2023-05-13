package com.jeta.abeille.gui.modeler.mysql;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;

import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.modeler.ForeignKeyView;
import com.jeta.abeille.gui.modeler.ForeignKeyValidatorRule;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.RuleResult;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.mysql.MySQLUtils;
import com.jeta.plugins.abeille.mysql.MySQLTableType;

/**
 * Validator for the MySQLForeignKeyView
 * 
 * @author Jeff Tassin
 */
public class MySQLForeignKeyValidatorRule extends ForeignKeyValidatorRule {

	/**
	 * ctor
	 */
	public MySQLForeignKeyValidatorRule() {

	}

	/**
	 * JETARule implementation
	 */
	public RuleResult check(Object[] params) {
		/** ignore the params */
		assert (params != null);

		RuleResult result = checkAssignments(params);
		if (result == null || result.equals(RuleResult.SUCCESS))
			result = checkTable(params);

		return result;
	}

	/**
	 * Checks the given TableMetaData and verifies that it and all tables that
	 * it refers to are of type, InnoDB.
	 */
	public RuleResult checkForeignKeys(TableMetaData tmd, TableSelectorModel tableselector, ModelerModel modeler) {
		/** now check all reference tables */
		Collection fkeys = tmd.getForeignKeys();

		if (fkeys.size() > 0) {
			if (!MySQLUtils.getTableType(tmd).equals(MySQLTableType.InnoDB)) {
				return new RuleResult(I18N.getLocalizedMessage("Only_InnoDb_supports_foreign_keys"));
			}
		}

		Iterator iter = fkeys.iterator();
		while (iter.hasNext()) {
			DbForeignKey fkey = (DbForeignKey) iter.next();

			TableMetaData reftmd = tableselector.getTableEx(fkey.getReferenceTableId(), TableMetaData.LOAD_ALL);
			if (reftmd != null) {
				MySQLTableType ttype = MySQLUtils.getTableType(reftmd);

				if (!ttype.equals(MySQLTableType.InnoDB)) {
					return new RuleResult(I18N.format("Only_InnoDb_supports_foreign_keys_1", reftmd.getTableName()));
				}
			}
		}

		try {
			/**
			 * now check that if any prototypes define a foreign key to this
			 * table those prototypes must be declared as InnoDb
			 */
			TableId refid = tmd.getTableId();
			Collection protos = modeler.getPrototypes();
			iter = protos.iterator();
			while (iter.hasNext()) {
				TableId tableid = (TableId) iter.next();
				if (!tableid.equals(refid)) {
					TableMetaData localtmd = modeler.getTable(tableid);
					if (localtmd != null) {
						fkeys = localtmd.getForeignKeys();
						Iterator fiter = fkeys.iterator();
						while (fiter.hasNext()) {
							DbForeignKey fkey = (DbForeignKey) fiter.next();
							if (refid.equals(fkey.getReferenceTableId())) {
								MySQLTableType ttype = MySQLUtils.getTableType(localtmd);
								if (!ttype.equals(MySQLTableType.InnoDB)) {
									return new RuleResult(I18N.format("Only_InnoDb_supports_foreign_keys_1",
											tableid.getTableName()));
								}

								if (!MySQLUtils.getTableType(tmd).equals(MySQLTableType.InnoDB)) {
									return new RuleResult(I18N.format("Only_InnoDb_supports_foreign_keys2_1",
											tableid.getTableName()));
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		return RuleResult.SUCCESS;
	}

	/**
	 * Validates the data in the view. Null is returned if the input is valid.
	 */
	public RuleResult checkTable(Object[] params) {
		RuleResult result = RuleResult.SUCCESS;
		ForeignKeyView view = (ForeignKeyView) params[0];
		TableId localid = view.getLocalTableId();
		ModelerModel modeler = view.getTableSelectorModel();
		TableMetaData localtmd = modeler.getTable(localid);
		if (localtmd != null) {
			result = checkForeignKeys(localtmd, modeler, modeler);
		} else {
			// this can be null if you are creating a table for the first time
			// and creating
			// foreign keys without saving the table to the modeler.
		}

		return result;
	}
}
