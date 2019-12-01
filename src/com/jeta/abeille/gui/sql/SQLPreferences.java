package com.jeta.abeille.gui.sql;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.preferences.Preferences;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;
import com.jeta.open.rules.JETARule;
import com.jeta.foundation.utils.TSUtils;

public class SQLPreferences implements Preferences {
	private TSConnection m_connection;

	private SQLPreferencesAction m_editorprefs;

	public SQLPreferences(TSConnection conn) {
		m_connection = conn;
	}

	public void apply() {
		if (m_editorprefs != null) {
			m_editorprefs.save();
			/** now update the SQLFrames for all other opened connections */
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			SQLFrame curr_sqlframe = (SQLFrame) wsframe.getSingletonFrame(SQLFrame.class, m_connection.getId());
			Collection sqlframes = wsframe.getSingletonFrames(SQLFrame.class);
			Iterator iter = sqlframes.iterator();
			while (iter.hasNext()) {
				SQLFrame sqlframe = (SQLFrame) iter.next();
				if (sqlframe != curr_sqlframe) {
					SQLPreferencesAction ea = new SQLPreferencesAction(sqlframe, sqlframe.getBufferMgr());
					ea.save(m_editorprefs.getView());
				}
			}

		}
	}

	/**
	 * @return the delimiter used to separate SQL commands
	 */
	public static char getDelimiter() {
		char delim = TSUtils.getChar(TSUserPropertiesUtils.getString(SQLNames.ID_SQL_DELIMITER, ";"));
		if (delim == 0)
			delim = ';';
		return delim;
	}

	public JETARule getValidator() {
		return null;
	}

	public TSPanel getView() {
		if (m_editorprefs == null) {
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			SQLFrame sqlframe = (SQLFrame) wsframe.getSingletonFrame(SQLFrame.class, m_connection.getId());
			m_editorprefs = new SQLPreferencesAction(sqlframe, sqlframe.getBufferMgr());
		}
		return m_editorprefs.getView();
	}

	public String getTitle() {
		return I18N.getLocalizedMessage("SQL");
	}

}
