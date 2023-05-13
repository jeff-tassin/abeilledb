package com.jeta.abeille.gui.modeler.mysql;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.modeler.TableValidationRule;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.mysql.MySQLUtils;
import com.jeta.plugins.abeille.mysql.MySQLTableType;

/**
 * Validator for TableMetaData objects in MySQL
 * 
 * @author Jeff Tassin
 */
public class MySQLTableValidationRule implements JETARule {
	private TableValidationRule m_defaultvalidator = new TableValidationRule();
	private MySQLForeignKeyValidatorRule m_fkvalidator = new MySQLForeignKeyValidatorRule();

	/**
	 * JETARule implementation. Validates the data in the view. Null is returned
	 * if the input is valid.
	 */
	public RuleResult check(Object[] params) {
		RuleResult result = m_defaultvalidator.check(params);
		if (result == RuleResult.SUCCESS) {
			assert (params.length >= 2);
			assert (params[0] instanceof TSConnection);
			assert (params[1] instanceof TableMetaData);
			TSConnection conn = (TSConnection) params[0];
			TableMetaData tmd = (TableMetaData) params[1];
			ModelerModel modeler = (ModelerModel) params[2];

			try {
				result = m_fkvalidator.checkForeignKeys(tmd, modeler, modeler);
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
		return result;
	}

}
