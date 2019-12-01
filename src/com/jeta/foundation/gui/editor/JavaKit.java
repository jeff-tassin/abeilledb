/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;

import java.util.Map;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.Action;

import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.java.*;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.editor.macros.*;

/**
 * Java editor kit with appropriate document
 * 
 * @author Jeff Tassin
 */

public class JavaKit extends TSKit {

	static {
		try {
			KitInfo info = new KitInfo("text/x-java", JavaKit.class);
			TSEditorMgr.registerEditorKit(info);

			Settings.addInitializer(new JavaSettingsInitializer(JavaKit.class));
			Settings.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ctor
	 */
	public JavaKit() {

	}

	public String getContentType() {
		return "text/x-java";
	}

	/**
	 * Create new instance of syntax coloring scanner
	 * 
	 * @param doc
	 *            document to operate on. It can be null in the cases the syntax
	 *            creation is not related to the particular document
	 */
	public Syntax createSyntax(Document doc) {
		return new JavaSyntax();
	}

	/** Create syntax support */
	public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
		return new JavaSyntaxSupport(doc);
	}

	public Completion createCompletion(ExtEditorUI extEditorUI) {
		return new JavaCompletion(extEditorUI);
	}

	/** Create the formatter appropriate for this kit */
	public Formatter createFormatter() {
		return new JavaFormatter(this.getClass());
	}

	protected EditorUI createEditorUI() {
		return new ExtEditorUI();
	}

	/**
	 * List all actions supported by this class.
	 */
	public static Action[] listDefaultActions() {
		return new Action[0];
	}

	/**
	 * @return the list default macros supported by this kit
	 */
	public static Macro[] listDefaultMacros() {
		return new Macro[0];
	}

	/**
	 * Get the default bindings. We are using a different method for
	 * initializing default bindings than the netbeans callback method.
	 */
	public static MultiKeyBinding[] listDefaultKeyBindings() {
		MultiKeyBinding[] bindings = new MultiKeyBinding[0];
		return bindings;
	}
}
