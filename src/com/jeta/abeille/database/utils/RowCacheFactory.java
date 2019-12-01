package com.jeta.abeille.database.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.foundation.utils.TSUtils;

/**
 * Factory class for creating row cache instances. We current support two types
 * of RowCache objects ScrollableRowCache and ForwardOnlyRowCache. This is
 * because some databases only support forward-only result set in some
 * situations (e.g. LOB objects or resutls returned from a procedure )
 * 
 * @author Jeff Tassin
 */
public class RowCacheFactory {

	/**
	 * Tests the type of result set. If the type is scrollable, then we return a
	 * ScrollableRowCache. Otherwise, we return a ForwardOnlyRowCache
	 */
	public static RowCache createInstance(Catalog catalog, ResultSetReference ref) throws SQLException {
		if (ref == null) {
			return new ScrollableRowCache(catalog, null);
		} else {
			ResultSet rset = ref.getResultSet();
			if (rset.getType() == ResultSet.TYPE_FORWARD_ONLY) {
				return new ForwardOnlyRowCache(catalog, ref);
			} else {
				return new ScrollableRowCache(catalog, ref);
			}
		}
	}
}
