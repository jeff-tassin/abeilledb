package com.jeta.abeille.gui.update;

import java.sql.SQLException;
import com.jeta.abeille.database.utils.DbUtils;

/**
 * Helper methods
 */
public class InstanceUtils {

	public static Object getBinaryData(Object value, InstanceProxy proxy, String fieldName) throws SQLException {
	    System.out.println("get ibinay data " + proxy.getClass());
	    if ((value instanceof byte[]) || (value instanceof String)) {
			return value;
		} else if (value instanceof java.sql.Clob) {
			/**
			 * it is possible to get a clob type here since we classify it has a
			 * 'binary' type. So, let's do the check.
			 */
			return DbUtils.getCharacterData((java.sql.Clob) value);
		} else if (value instanceof java.sql.Blob) {
			/**
			 * it is possible to get a clob type here since we classify it has a
			 * 'binary' type. So, let's do the check.
			 */
			return DbUtils.getBinaryData((java.sql.Blob) value);
		} else {
			if (proxy != null) {
				return proxy.getBinaryData(fieldName);
			} else {
				return value;
			}
		}
	}

}
