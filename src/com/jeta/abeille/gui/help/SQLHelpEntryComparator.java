package com.jeta.abeille.gui.help;

import java.util.Comparator;

/**
 * This class compares only the table names of two table ids. It does not take
 * into account the schema
 */
public class SQLHelpEntryComparator implements Comparator {
	public int compare(Object o1, Object o2) {
		if (o1 instanceof SQLHelpEntry && o2 instanceof SQLHelpEntry) {
			SQLHelpEntry entry1 = (SQLHelpEntry) o1;
			SQLHelpEntry entry2 = (SQLHelpEntry) o2;
			return entry1.compareTo(entry2);
		} else {
			return -1;
		}
	}
}
