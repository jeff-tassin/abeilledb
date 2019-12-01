package com.jeta.abeille.gui.keys;

import java.sql.DatabaseMetaData;

public class Key {
	private String m_pkcatalog;
	private String m_pkschema;
	private String m_pktable;
	private String m_pkcolumn;
	private String m_pkname;

	private String m_fkcatalog;
	private String m_fkschema;
	private String m_fktable;
	private String m_fkcolumn;
	private String m_fkname;

	private Integer m_key_seq;
	private Integer m_deferability;
	private Integer m_update_rule;
	private Integer m_delete_rule;

	public String getPKCatalog() {
		return m_pkcatalog;
	}

	public String getPKSchema() {
		return m_pkschema;
	}

	public String getPKTable() {
		return m_pktable;
	}

	public String getPKColumn() {
		return m_pkcolumn;
	}

	public String getFKCatalog() {
		return m_fkcatalog;
	}

	public String getFKSchema() {
		return m_fkschema;
	}

	public String getFKTable() {
		return m_fktable;
	}

	public String getFKColumn() {
		return m_fkcolumn;
	}

	public Integer getKeySequence() {
		return m_key_seq;
	}

	public void setPKCatalog(String pk_catalog) {
		m_pkcatalog = pk_catalog;
	}

	public void setPKSchema(String pk_schema) {
		m_pkschema = pk_schema;
	}

	public void setPKTable(String pk_table) {
		m_pktable = pk_table;
	}

	public void setPKColumn(String pk_column) {
		m_pkcolumn = pk_column;
	}

	public void setFKCatalog(String fk_catalog) {
		m_fkcatalog = fk_catalog;
	}

	public void setFKSchema(String fk_schema) {
		m_fkschema = fk_schema;
	}

	public void setFKTable(String fk_table) {
		m_fktable = fk_table;
	}

	public void setFKColumn(String fk_column) {
		m_fkcolumn = fk_column;
	}

	public void setKeySequence(Integer keyseq) {
		m_key_seq = keyseq;
	}

	public void setUpdateRule(Integer updateRule) {
		m_update_rule = updateRule;
	}

	public void setDeleteRule(Integer deleteRule) {
		m_delete_rule = deleteRule;
	}

	public void setPKName(String pkname) {
		m_pkname = pkname;
	}

	public void setFKName(String fkname) {
		m_fkname = fkname;
	}

	public void setDeferrability(Integer deferability) {
		m_deferability = deferability;
	}

	public Integer getUpdateRule() {
		return m_update_rule;
	}

	public String getUpdateRuleDescription() {
		if (m_update_rule == null)
			return "";
		else
			return getRuleString(m_update_rule.intValue());
	}

	public static String getRuleString(int rulevalue) {
		String result = "";
		switch (rulevalue) {
		case DatabaseMetaData.importedKeyNoAction:
			result = "NO ACTION";
			break;

		case DatabaseMetaData.importedKeyCascade:
			result = "CASCADE";
			break;

		case DatabaseMetaData.importedKeySetNull:
			result = "SET NULL";
			break;

		case DatabaseMetaData.importedKeySetDefault:
			result = "SET DEFAULT";
			break;

		case DatabaseMetaData.importedKeyRestrict:
			result = "RESTRICT";
			break;

		default:
			break;
		}
		return result;
	}

	public Integer getDeleteRule() {
		return m_delete_rule;
	}

	public String getDeleteRuleDescription() {
		if (m_delete_rule == null)
			return "";
		else
			return getRuleString(m_delete_rule.intValue());
	}

	public String getPKName() {
		return m_pkname;
	}

	public String getFKName() {
		return m_fkname;
	}

	public Integer getDeferrability() {
		return m_deferability;
	}

	public String getDeferrabilityDescription() {
		String result = "";
		if (m_deferability != null) {
			int dval = m_deferability.intValue();
			switch (dval) {
			case DatabaseMetaData.importedKeyInitiallyDeferred:
				result = "Initially Deferred";
				break;

			case DatabaseMetaData.importedKeyInitiallyImmediate:
				result = "Initially Immediate";
				break;

			case DatabaseMetaData.importedKeyNotDeferrable:
				result = "Not Deferrable";
				break;
			}
		}
		return result;
	}

}
