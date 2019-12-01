/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import javax.swing.Action;
import javax.swing.text.Document;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.ExtKit;

import com.jeta.foundation.gui.editor.macros.Macro;

/**
 * Editor kit used to edit the plain text.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TSPlainKit extends TSKit {
	public static final String PLAIN_MIME_TYPE = "text/plain"; // NOI18N

	public String getContentType() {
		return PLAIN_MIME_TYPE;
	}

	public Syntax createSyntax(Document doc) {
		return new PlainSyntax();
	}

	public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
		return new ExtSyntaxSupport(doc);
	}

	/**
	 * Get the default bindings. We are using a different method for
	 * initializing default bindings than the netbeans callback method.
	 */
	public static MultiKeyBinding[] listDefaultKeyBindings() {
		return new MultiKeyBinding[0];
	}

	/**
	 * List all actions supported by this class.
	 */
	public static Action[] listDefaultActions() {
		return new Action[0];
	}

	/**
	 * No default macros for plain kit
	 */
	public static Macro[] listDefaultMacros() {
		return new Macro[0];
	}

}
