package com.jeta.plugins.abeille.postgres;

import java.net.URL;
import java.util.HashMap;
import java.util.Collection;

import com.jeta.abeille.gui.help.SQLHelpEntry;
import com.jeta.abeille.gui.help.SQLReference;
import com.jeta.abeille.gui.help.SQLReferenceService;
import com.jeta.abeille.gui.help.SQLReferenceType;

public class PostgresSQLReferenceService implements SQLReferenceService {
	private PostgresSQLReference m_sqlref = new PostgresSQLReference();

	public SQLReference getReference() {
		return m_sqlref;
	}

	private static class PostgresSQLReference implements SQLReference {
		private static HashMap m_help_files = new HashMap();

		static {
			/*
			 * m_help_files.put( SQLReferenceType.ALTER_TABLE_COLUMNS, new
			 * SQLHelpEntry( SQLReferenceType.ALTER_TABLE_COLUMNS,
			 * "com/jeta/plugins/abeille/postgres/help/alter_table_columns.html"
			 * ) );
			 * 
			 * m_help_files.put( SQLReferenceType.CHECK_CONSTRAINTS, new
			 * SQLHelpEntry( SQLReferenceType.CHECK_CONSTRAINTS,
			 * "com/jeta/plugins/abeille/postgres/help/checks.html" ) );
			 * 
			 * m_help_files.put( SQLReferenceType.FUNCTIONS, new SQLHelpEntry(
			 * SQLReferenceType.FUNCTIONS,
			 * "com/jeta/plugins/abeille/postgres/help/functions.html" ) );
			 * 
			 * m_help_files.put( SQLReferenceType.TRIGGERS, new SQLHelpEntry(
			 * SQLReferenceType.TRIGGERS,
			 * "com/jeta/plugins/abeille/postgres/help/triggers.html" ) );
			 */
		}

		public boolean supportsContent(SQLReferenceType objtype) {
			return (getContent(objtype) != null);
		}

		public URL getContent(SQLReferenceType objtype) {
			SQLHelpEntry entry = (SQLHelpEntry) m_help_files.get(objtype);
			if (entry != null) {
				return getContent(entry.getUrl());
			}
			return null;
		}

		public URL getContent(String url) {
			try {
				return ClassLoader.getSystemResource(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Returns a collection of SQLHelpEntry objects.
		 */
		public Collection getHelpEntries() {
			return m_help_files.values();
		}
	}

}
