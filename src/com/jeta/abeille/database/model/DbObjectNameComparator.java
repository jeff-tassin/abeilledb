package com.jeta.abeille.database.model;

import java.util.Comparator;

/**
 * This class compares only the names of two database objects. It does not take
 * into account the schema
 */
public class DbObjectNameComparator implements Comparator {
	private static DbObjectNameComparator m_singleton = new DbObjectNameComparator();

	public int compare(Object o1, Object o2) {
		if (o1 != null && o2 != null) {
			String oname1 = null;
			String oname2 = null;

			if (o1 instanceof String) {
				oname1 = (String) o1;
			} else if (o1 instanceof DbObjectId) {
				DbObjectId id1 = (DbObjectId) o1;
				oname1 = id1.getObjectName();
			} else if (o1 instanceof DatabaseObject) {
				DatabaseObject dobj = (DatabaseObject) o1;
				DbObjectId id1 = dobj.getObjectId();
				oname1 = id1.getObjectName();
			} else if (o1 instanceof Schema) {
				Schema schema = (Schema) o1;
				oname1 = schema.getName();
			} else if (o1 instanceof Catalog) {
				Catalog cat = (Catalog) o1;
				oname1 = cat.getName();
			} else {
				assert (false);
			}

			if (o2 instanceof String) {
				oname2 = (String) o2;
			} else if (o2 instanceof DbObjectId) {
				DbObjectId id2 = (DbObjectId) o2;
				oname2 = id2.getObjectName();
			} else if (o2 instanceof DatabaseObject) {
				DatabaseObject dobj = (DatabaseObject) o2;
				DbObjectId id2 = dobj.getObjectId();
				oname2 = id2.getObjectName();
			} else if (o2 instanceof Schema) {
				Schema schema = (Schema) o2;
				oname2 = schema.getName();
			} else if (o2 instanceof Catalog) {
				Catalog cat = (Catalog) o2;
				oname2 = cat.getName();
			} else {
				assert (false);
			}

			if (oname1 != null) {
				return oname1.compareToIgnoreCase(oname2);
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	public static DbObjectNameComparator getInstance() {
		return m_singleton;
	}
}
