package com.jeta.abeille.gui.indexes.mysql;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Class that defines available types of indexes for postgresql
 */
public class MySQLIndexTypes {
	/** the list of types (String objects ) */
	private static LinkedList m_types = new LinkedList();

	static {
		m_types.add("BTREE");
		m_types.add("FULLTEXT");
	}

	/**
	 * @return true if the given index type is valid
	 */
	public static boolean isIndexType(String iType) {
		Iterator iter = m_types.iterator();
		while (iter.hasNext()) {
			String validtype = (String) iter.next();
			if (validtype.equalsIgnoreCase(iType))
				return true;
		}
		return false;
	}

	/**
	 * @return a list of available index types (String objects) in Postgres
	 */
	public static Collection getTypes() {
		return m_types;
	}

}
