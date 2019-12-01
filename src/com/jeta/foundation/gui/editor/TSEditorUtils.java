/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.MissingResourceException;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Event;
import java.awt.Frame;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.text.JTextComponent;

import java.io.File;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.JLabel;
import javax.swing.JEditorPane;
import javax.swing.JToolBar;
import javax.swing.JTextArea;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.components.TSMenuBuilder;
import com.jeta.foundation.gui.components.TSToolBarBuilder;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.StatusBar;
import org.netbeans.editor.ext.ExtKit;

/**
 * 
 * @author Jeff Tassin
 */
public class TSEditorUtils {
	public static final int MENUBAR_EDIT_INDEX = 1;
	public static final int MENUBAR_OPTIONS_INDEX = 2;

	/**
	 * Creates the menu used for this frame window. Adds standard editing
	 * commands (cut, copy, paste, etc.)
	 */
	public static void buildMenu(TSMenuBuilder builder) {
		MenuTemplate template = builder.getMenuTemplate();

		MenuDefinition filemenu = new MenuDefinition(I18N.getLocalizedMessage("File"));
		filemenu.add(createMenuItem(builder, "New", FrameKit.newBufferAction, null, null));
		filemenu.add(createMenuItem(builder, "Open", FrameKit.openFileAction, null, null));
		filemenu.add(createMenuItem(builder, "Save", FrameKit.saveFileAction, null, null));
		filemenu.add(createMenuItem(builder, "Save As", FrameKit.saveAsAction, null, null));
		filemenu.add(createMenuItem(builder, "Close", FrameKit.closeFileAction, null, null));

		template.add(filemenu);

		MenuDefinition editmenu = new MenuDefinition(I18N.getLocalizedMessage("Edit"));
		editmenu.add(createMenuItem(builder, "Cut", TSTextNames.ID_CUT, null, null));
		editmenu.add(createMenuItem(builder, "Copy", TSTextNames.ID_COPY, null, null));
		editmenu.add(createMenuItem(builder, "Paste", TSTextNames.ID_PASTE, null, null));
		editmenu.add(createMenuItem(builder, "Delete", TSTextNames.ID_DELETE, null, null));
		editmenu.add(createMenuItem(builder, "Select All", TSTextNames.ID_SELECT_ALL, null, null));
		editmenu.addSeparator();
		editmenu.add(createMenuItem(builder, "Undo", TSTextNames.ID_UNDO, null, null));
		editmenu.add(createMenuItem(builder, "Redo", TSTextNames.ID_REDO, null, null));
		editmenu.addSeparator();
		editmenu.add(createMenuItem(builder, "Find", TSTextNames.ID_FIND, null, null));
		editmenu.add(createMenuItem(builder, "Replace", TSTextNames.ID_REPLACE, null, null));
		editmenu.add(createMenuItem(builder, "Goto", TSTextNames.ID_GOTO, null, null));
		template.add(editmenu);

		MenuDefinition toolsmenu = new MenuDefinition(I18N.getLocalizedMessage("Tools"));
		// toolsmenu.add( createMenuItem( builder, TSTextNames.ID_RUN_MACRO,
		// KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK), null ) );
		// toolsmenu.add( createMenuItem( builder, TSTextNames.ID_KEY_MAP, null,
		// null ) );
		// toolsmenu.addSeparator();
		// toolsmenu.add( createMenuItem( builder, TSTextNames.ID_PREFERENCES,
		// null, null ) );
		template.add(toolsmenu);
	}

	/**
	 * Creates the toolbar used for this frame window Adds standard editing
	 * commands (cut, copy, paste, etc.)
	 */
	public static void buildToolBar(TSToolBarBuilder builder) {
		// System.out.println( "TSEditorUtils.buildToolBar: " +
		// builder.getClass() );
		TSToolBarTemplate template = builder.getToolBarTemplate();

		template.add(builder.createToolBarButton(FrameKit.openFileAction, "incors/16x16/folder.png",
				I18N.getLocalizedMessage("Open")));
		template.add(builder.createToolBarButton(FrameKit.saveFileAction, "incors/16x16/disk_blue.png",
				I18N.getLocalizedMessage("Save")));
		// template.addSeparator();
		template.add(builder.createToolBarButton(TSTextNames.ID_CUT, "incors/16x16/cut.png",
				I18N.getLocalizedMessage("Cut")));
		template.add(builder.createToolBarButton(TSTextNames.ID_COPY, "incors/16x16/copy.png",
				I18N.getLocalizedMessage("Copy")));
		template.add(builder.createToolBarButton(TSTextNames.ID_PASTE, "incors/16x16/paste.png",
				I18N.getLocalizedMessage("Paste")));
		template.add(builder.createToolBarButton(TSTextNames.ID_UNDO, "incors/16/x16/undo.png",
				I18N.getLocalizedMessage("Undo")));
		// template.addSeparator();
		template.add(builder.createToolBarButton(TSTextNames.ID_FIND, "incors/16x16/find_text.png",
				I18N.getLocalizedMessage("Find")));
	}

	/**
	 * Creates a editor and sets the editor kit to the passed in object. This is
	 * useful if you want to specialize a kit without having to go through the
	 * song and dance of registering kit info.
	 * 
	 * @param kit
	 *            the kit to set
	 */
	public static JEditorPane createEditor(TSKit kit) {
		return TSEditorMgr.createEditor(kit);
	}

	/**
	 * Factory method that creates an editor pane
	 * 
	 * @param kitClass
	 *            the name of the class that implements the EditorKit for this
	 *            editor
	 * @param frameKit
	 *            the frame kit class that will contain the editor ( can be null
	 *            )
	 */
	static JEditorPane createEditor(Class kitClass, Class frameKit) throws MissingResourceException {
		TSEditorMgr editormgr = TSEditorMgr.createEditor(kitClass, frameKit);
		return editormgr.getEditorPane();
	}

	/**
	 * An internal helper method for creating menu items.
	 */
	private static JMenuItem createMenuItem(TSMenuBuilder builder, String txt, String Id, KeyStroke keyStroke,
			String imageName) {
		JMenuItem item = builder.createMenuItem(I18N.getLocalizedMessage(txt), Id, keyStroke);
		return item;
	}

	/**
	 * Creates a standard multiline text area component for dialogs and panels.
	 * This component though has the key bindings set for the selected editor
	 * type.
	 * 
	 * @return the created text area component
	 */
	public static JTextArea createTextArea() {
		// @todo add key bindings
		return new JTextArea();
	}

	/**
	 * Creates a standard singleline text field component for dialogs and
	 * panels. This component though has the key bindings set for the selected
	 * editor type.
	 * 
	 * @return the created text area component
	 */
	public static JTextField createTextField() {
		// @todo add key bindings
		return new JTextField();
	}

	/**
	 * The editor pane is actually contained within a JPanel. This panel
	 * contains the editor, scroll pane, and status bar. It is this 'Extended'
	 * component that we are returning and will add to some other container for
	 * display to the user.
	 * 
	 * @return the JPanel that contains the JEditorPane
	 */
	public static JComponent getExtComponent(JEditorPane editorPane) {
		return Utilities.getEditorUI(editorPane).getExtComponent();
	}

	/**
	 * @return the icon associated with a given kit class
	 */
	public static Icon getIcon(Class kitClass) {
		if (kitClass == TSKit.class)
			return TSGuiToolbox.loadImage("incors/16x16/document_edit.png");
		else if (kitClass == FrameKit.class)
			return TSGuiToolbox.loadImage("incors/16x16/window_earth.png");
		else {
			KitInfo kitinfo = TSEditorMgr.getKitInfo(kitClass);
			if (kitinfo != null)
				return kitinfo.getIcon();
		}

		return null;
	}

	/**
	 * @return the line number for a given caret/document position in an editor.
	 *         Note: The editor must have been created with this class. (i.e. be
	 *         part of the editor framework for this application)
	 */
	public static int getLine(JEditorPane editor, int docPos) {
		try {
			return Utilities.getLineOffset((org.netbeans.editor.BaseDocument) editor.getDocument(), docPos);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Moves the caret to the given line
	 * 
	 * @param editor
	 *            the editor whose caret we wish to move
	 * @param line
	 *            the line number to move to
	 */
	public static void gotoLine(JEditorPane editor, int line) {
		BaseDocument doc = Utilities.getDocument(editor);
		if (doc != null) {
			// Obtain the offset where to jump
			int pos = Utilities.getRowStartFromLineOffset(doc, line - 1);
			if (pos != -1)
				editor.getCaret().setDot(pos);
			else
				editor.getToolkit().beep();
		}
	}

	/**
	 * Helper method that invokes a static method on a editor kit. This is
	 * mainly used to load default settings for the various kits. The
	 * application requires each editor kit to define various methods to load
	 * default settings (e.g. loadDefaultKeyBindings, loadDefaultMacros,
	 * loadDefaultAbbreviations ). It calls these methods using reflection when
	 * it needs to get default settings.
	 * 
	 * @param kitClass
	 *            the class whose static method we will be invoking
	 * @param methodName
	 *            the name of the method to invoke.
	 */
	public static Object invokeClassMethod(Class kitClass, String methodName) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		// System.out.println( "TSEditorUtils.invokClassMethod " + methodName +
		// "  on " + kitClass.getName() );
		try {
			Class[] paramtypes = new Class[0];
			Method m = kitClass.getDeclaredMethod(methodName, paramtypes);
			Object[] params = new Object[0];
			// we are calling a static method, so the obj=null in this case
			return m.invoke(null, params);
		} catch (NoSuchMethodException nsme) {
			// ignore
			nsme.printStackTrace();
		}
		return null;
	}

	/**
	 * Launches the file open dialog and returns the selected file object.
	 * 
	 * @return the selected file object. Null is returned if the user cancels
	 *         the operation
	 */
	public static File openFile() {
		return TSFileChooserFactory.showOpenDialog();
	}

	/**
	 * Launches the file save dialog and returns the selected file object.
	 * 
	 * @return the selected file object. Null is returned if the user cancels
	 *         the operation
	 */
	public static File saveFile() {
		return TSFileChooserFactory.showSaveDialog();
	}

	/**
	 * Enables/Disables line numbering for the given editor
	 */
	public static void setLineNumberEnabled(JEditorPane editor, boolean lineNumberEnabled) {
		EditorUI editorui = Utilities.getEditorUI(editor);
		editorui.setLineNumberEnabled(lineNumberEnabled);
	}

	/**
	 * Shows the statusbar on the given editor
	 */
	public static void showStatusBar(JEditorPane editor, boolean bShow) {
		StatusBar sb = Utilities.getEditorUI(editor).getStatusBar();
		if (sb != null)
			sb.setVisible(false);
	}

	public static class ToolBarLayout extends BoxLayout {

		private JComponent m_minibuffer;
		private int m_maxheight;

		ToolBarLayout(JToolBar toolBar, JComponent minibuffer, int maxheight) {
			super(toolBar, BoxLayout.X_AXIS);
			m_minibuffer = minibuffer;
			m_maxheight = maxheight;
		}

		public void layoutContainer(Container target) {
			super.layoutContainer(target);

			Dimension d = m_minibuffer.getSize();
			if (d.height > m_maxheight) {
				d.height = m_maxheight;
				int y = (target.getHeight() - m_maxheight) / 2;
				m_minibuffer.setLocation(m_minibuffer.getX(), y);
			}
			d.width = d.width - 32;
			m_minibuffer.setSize(d);
		}
	}

}
