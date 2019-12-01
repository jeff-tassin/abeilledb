package com.jeta.abeille.gui.sql;

import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
 * Various extensions to the displaying of the sql tokens is defined here. The
 * tokens defined here are used by the sql-drawing-layer.
 * 
 * @author Jeff Tassin
 */

public class SQLLayerTokenContext extends TokenContext {

	// Token category-ids

	// Numeric-ids for token-ids
	public static final int METHOD_ID = 1;

	// Token-categories
	// public static final BaseTokenCategory KEYWORDS
	// = new BaseTokenCategory("keywords", KEYWORDS_ID);

	// Token-ids
	public static final BaseTokenID METHOD = new BaseTokenID("method", METHOD_ID);

	// Context instance declaration
	public static final SQLLayerTokenContext context = new SQLLayerTokenContext();

	public static final TokenContextPath contextPath = context.getContextPath();

	private SQLLayerTokenContext() {
		super("sql-layer-");

		try {
			addDeclaredTokenIDs();
		} catch (Exception e) {
			if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
				e.printStackTrace();
			}
		}

	}

}
