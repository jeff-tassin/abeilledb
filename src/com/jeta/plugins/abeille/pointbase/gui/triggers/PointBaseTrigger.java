package com.jeta.plugins.abeille.pointbase.gui.triggers;

import java.sql.Timestamp;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a trigger in PointBase
 * 
 * @author Jeff Tassin
 */
public class PointBaseTrigger {
	/**
	 * The name for this trigger
	 */
	private String m_name;

	/**
	 * ctor
	 */
	public PointBaseTrigger(String triggerName) {
		m_name = triggerName;
	}

	public String getName() {
		return m_name;
	}

}
