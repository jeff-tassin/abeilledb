package com.jeta.abeille.gui.sql;

import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;

/**
 * SQL indentation services are located here
 * 
 * @author Jeff Tassin
 */

public class SQLFormatSupport extends ExtFormatSupport {

	private TokenContextPath tokenContextPath;

	public SQLFormatSupport(FormatWriter formatWriter) {
		this(formatWriter, SQLTokenContext.contextPath);
	}

	public SQLFormatSupport(FormatWriter formatWriter, TokenContextPath tokenContextPath) {
		super(formatWriter);
		this.tokenContextPath = tokenContextPath;
	}
}
