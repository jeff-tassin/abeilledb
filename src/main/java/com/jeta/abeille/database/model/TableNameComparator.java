package com.jeta.abeille.database.model;

import java.util.Comparator;

/**
 * This class compares only the table names of two table ids. It does not take
 * into account the schema
 */
public class TableNameComparator implements Comparator {
	public int compare(Object o1, Object o2) {
		if (o1 != null && o2 != null) {
			String tname1 = null;
			String tname2 = null;

			if (o1 instanceof String) {
				tname1 = (String) o1;
			} else if (o1 instanceof TableId) {
				TableId id1 = (TableId) o1;
				tname1 = id1.getTableName();
			}

			if (o2 instanceof String) {
				tname2 = (String) o2;
			} else if (o2 instanceof TableId) {
				TableId id2 = (TableId) o2;
				tname2 = id2.getTableName();
			}

			if (tname1 != null) {
				return tname1.compareToIgnoreCase(tname2);
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
}
