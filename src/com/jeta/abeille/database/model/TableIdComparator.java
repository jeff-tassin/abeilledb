package com.jeta.abeille.database.model;

import java.util.Comparator;

/**
 * Comparator for table ids. Used mostly in the table combo boxes.
 * 
 * @author Jeff Tassin
 */
public class TableIdComparator implements Comparator {
	private TSConnection m_connection;
	private boolean m_case_sensitive;

	public TableIdComparator(TSConnection conn) {
		m_connection = conn;
		m_case_sensitive = m_connection.isCaseSensitive();
	}

	public TableIdComparator(TSConnection conn, boolean case_sensitive) {
		m_case_sensitive = case_sensitive;
		m_connection = conn;
	}

	/**
	 * TableId Comparator
	 */
	public int compare(Object o1, Object o2) {
		// @todo this is probably language specific code
		if (isCaseSensitive()) {
			TableId f1 = (TableId) o1;
			TableId f2 = (TableId) o2;
			String s1 = f1.getTableName();
			String s2 = f2.getTableName();

			Catalog ct1 = f1.getCatalog();
			Catalog ct2 = f2.getCatalog();
			if (ct1 != null) {
				int result = ct1.compareTo(ct2);
				if (result != 0)
					return result;
			}

			Schema sc1 = f1.getSchema();
			Schema sc2 = f2.getSchema();
			if (sc1 != null) {
				int result = sc1.compareTo(sc2);
				if (result != 0)
					return result;
			}

			int n1 = s1.length(), n2 = s2.length();
			for (int i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
				char c1 = s1.charAt(i1);
				char c2 = s2.charAt(i2);
				if (c1 != c2) {
					char cc1 = Character.toUpperCase(c1);
					char cc2 = Character.toUpperCase(c2);
					if (cc1 == cc2) {
						return c1 - c2;
					} else {
						cc1 = Character.toLowerCase(cc1);
						cc2 = Character.toLowerCase(cc2);
						if (cc1 == cc2) {
							return c1 - c2;
						} else {
							return cc1 - cc2;
						}
					}
				}
			}
			return n1 - n2;
		} else {
			TableId f1 = (TableId) o1;
			TableId f2 = (TableId) o2;

			Catalog ct1 = f1.getCatalog();
			Catalog ct2 = f2.getCatalog();
			if (ct1 != null) {
				int result = ct1.compareTo(ct2);
				if (result != 0)
					return result;
			}

			Schema sc1 = f1.getSchema();
			Schema sc2 = f2.getSchema();
			if (sc1 != null) {
				int result = sc1.compareTo(sc2);
				if (result != 0)
					return result;
			}

			String s1 = f1.getTableName();
			String s2 = f2.getTableName();
			int n1 = s1.length(), n2 = s2.length();
			for (int i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
				char c1 = s1.charAt(i1);
				char c2 = s2.charAt(i2);
				if (c1 != c2) {
					c1 = Character.toUpperCase(c1);
					c2 = Character.toUpperCase(c2);
					if (c1 != c2) {
						c1 = Character.toLowerCase(c1);
						c2 = Character.toLowerCase(c2);
						if (c1 != c2) {
							return c1 - c2;
						}
					}
				}
			}
			return n1 - n2;
		}
	}

	public boolean isCaseSensitive() {
		return m_case_sensitive;
	}

}
