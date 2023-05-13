/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

/**
 * This class defines string constants used by the editor and editor clients. It
 * defines command ids for toolbar and menus as well as other common strings.
 * 
 * @author Jeff Tassin
 */
public class TSTextNames {
	static final String SETTINGS = "settings";
	/** Document property holding String name of associated file */
	static final String FILE = "file";
	/** Document property holding Boolean if document was created or opened */
	static final String CREATED = "created";
	/** Document property holding Boolean modified information */
	static final String MODIFIED = "modified";
	/** Document property holding buffer object */
	static final String BUFFER = "buffer";

	public static final String ID_MINIBUFFER = "minibuffer"; // text field id on
																// toolbar

	// //////////////////////////////////////////////////////////////
	// command ids

	public static final String ID_CUT = "Cut";
	public static final String ID_COPY = "Copy";
	public static final String ID_PASTE = "Paste";
	public static final String ID_DELETE = "Delete";
	public static final String ID_SELECT_ALL = "Select All";

	public static final String ID_UNDO = "Undo";
	public static final String ID_REDO = "Redo";

	public static final String ID_FIND = "Find";
	public static final String ID_REPLACE = "Replace";
	public static final String ID_GOTO = "TSGoto.Action";

	public static final String ID_RUN_MACRO = "Run Macro";
	public static final String ID_KEY_MAP = "Key Map Manager";
	public static final String ID_PREFERENCES = "editro.Preferences";
}
