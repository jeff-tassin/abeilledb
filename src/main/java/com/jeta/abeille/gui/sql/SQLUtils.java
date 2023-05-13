package com.jeta.abeille.gui.sql;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.foundation.gui.editor.TSEditorUtils;

/**
 * This is a utility class that creates a SQL editor component. This is common
 * in the application, so we encapsulate it here to decouple the procedure
 * somewhat.
 * 
 * @author Jeff Tassin
 */
public class SQLUtils {
	public static SQLComponent createSQLComponent(TSConnection conn) {
		SQLKit kit = new SQLKit(conn);
		JEditorPane editor = TSEditorUtils.createEditor(kit);
		JComponent comp = TSEditorUtils.getExtComponent(editor);

		return new SQLComponent(kit, editor, comp);
	}
}
