/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.net.URL;

import java.awt.Color;
import java.awt.Event;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.InputEvent;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import javax.swing.text.Keymap;
import javax.swing.JEditorPane;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import com.jeta.foundation.gui.utils.*;
import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class TSEditorMgr {
	private JEditorPane m_editor = null; // this is the sql editor window
	private KitInfo m_kitinfo;
	private static HashMap m_kits; // the collection of registered kits
	private Class m_kitclass;

	public static final String STATUS_BAR_VISIBLE_PROP = "statusBarVisible"; // NOI18N

	static {
		m_kits = new HashMap();

		// System.setProperty( "netbeans.debug.editor.draw", "true" );
		// System.setProperty( "netbeans.debug.editor.draw.fragment", "true" );

		LocaleSupport.addLocalizer(new Localizer("org.netbeans.editor.Bundle"));
		// turn off netbeans popup

		// Feed our kits with their default Settings
		Settings.addInitializer(new BaseSettingsInitializer(), Settings.CORE_LEVEL);
		Settings.addInitializer(new ExtSettingsInitializer(), Settings.CORE_LEVEL);
		// Settings.addInitializer(new TSKitSettingsInitializer() );
		// Settings.addInitializer(new EditorFrameSettingsInitializer() );
		Settings.reset();

		// temporary
		// register the kit info for this kit
		// KitInfo info = new KitInfo( "text/plain2", TSPlainKit.class );
		// TSEditorMgr.registerEditorKit( info );

		// temporary for now
		try {
			LinkedList kits = new LinkedList();
			kits.add("com.jeta.foundation.gui.editor.TSPlainKit");
			// kits.add( "com.jeta.abeille.gui.sql.SQLKit" );

			Iterator iter = kits.iterator();
			while (iter.hasNext()) {
				String kitname = (String) iter.next();
				Class kitclass = Class.forName(kitname);
				kitclass.newInstance();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public TSEditorMgr() {
		// initialize( this, "text/plain2",
		// "org.netbeans.editor.ext.plain.PlainKit" );
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
		// System.out.println( "TSEditorManager.createEditor: " + kit );
		JEditorPane editor = new TSEditorPane();
		editor.setEditorKit(kit);
		initializeDocument(editor.getDocument());
		KeyBindingMgr.setKeyBindings(editor, new KitSet(kit.getClass(), null));
		return editor;
	}

	/**
	 * Factory method that creates an editor mgr
	 * 
	 * @param kitClass
	 *            the editor kit class to create the editor for
	 * @param frameKit
	 *            the frame kit class that will contain the editor (can be null)
	 */
	public static TSEditorMgr createEditor(Class kitClass, Class frameKit) throws MissingResourceException {
		return createEditor(getKitInfo(kitClass), frameKit);
	}

	/**
	 * Creates an editor from a given kit info object
	 * 
	 * @param kitInfo
	 *            the kit info that describes the type of editor
	 * @param frameKit
	 *            the frame kit that will contain the editor ( can be null )
	 * @return a new TSEditorMgr for the given kit
	 */
	public static TSEditorMgr createEditor(KitInfo kitInfo, Class frameKit) throws MissingResourceException {
		TSEditorMgr editormgr = new TSEditorMgr();
		initialize(editormgr, kitInfo.getMimeType(), kitInfo.getKitClass(), kitInfo.getClassLoader());
		KeyBindingMgr.setKeyBindings(editormgr.m_editor, new KitSet(kitInfo.getKitClass(), frameKit));
		// SettingsUtil.setColoring( BaseKit.class,
		// SettingsNames.DEFAULT_COLORING,
		// Coloring.changeBackColor( SettingsDefaults.defaultColoring, new
		// Color(230,222,205) ) );

		return editormgr;
	}

	/**
	 * @return the editor pane
	 */
	public JEditorPane getEditorPane() {
		return m_editor;
	}

	/**
	 * The editor pane is actually contained within a JPanel. This panel
	 * contains the editor, scroll pane, and status bar. It is this 'Extended'
	 * component that we are returning and will add to some other container for
	 * display to the user.
	 * 
	 * @return the JPanel that contains the JEditorPane
	 */
	public JComponent getExtComponent() {
		return org.netbeans.editor.Utilities.getEditorUI(getEditorPane()).getExtComponent();
	}

	/**
	 * @return the kit info for a given editor kit
	 */
	public static KitInfo getKitInfo(Class kitclass) {
		return (KitInfo) m_kits.get(kitclass);
	}

	/**
	 * @return the kit info that handles files with the given extension. If no
	 *         editor can be found, then the TSPlainKit is returned
	 */
	public static KitInfo getKitInfo(String ext) {
		KitInfo result = null;
		Iterator iter = m_kits.keySet().iterator();
		while (iter.hasNext()) {
			KitInfo info = (KitInfo) m_kits.get(iter.next());
			if (info.containsExtension(ext)) {
				result = info;
				break;
			}
		}

		if (result == null)
			result = getKitInfo(TSPlainKit.class);

		return result;
	}

	/**
	 * Initializes an editor mgr
	 * 
	 * @param editorMgr
	 *            the editor mgr to initialize
	 * @param contentType
	 *            the mime type for the content used for this editor. (e.g.
	 *            text/x-java )
	 * @param kit
	 *            the name of the class that implements the EditorKit for this
	 *            editor
	 */
	private static void initialize(TSEditorMgr editorMgr, String contentType, Class kitclass, ClassLoader loader) {
		// probably don't need to do this every time
		JEditorPane.registerEditorKitForContentType(contentType, kitclass.getName(), loader);

		JEditorPane editor = new TSEditorPane(contentType, "");
		initializeDocument(editor.getDocument());

		editorMgr.m_editor = editor;
		editorMgr.m_kitclass = kitclass;
	}

	/**
	 * Initializes a document for the application
	 */
	public static void initializeDocument(Document doc) {
		doc.putProperty(TSTextNames.CREATED, Boolean.TRUE);
		TSUndoManager um = new TSUndoManager();
		doc.addUndoableEditListener(um);
		doc.putProperty(BaseDocument.UNDO_MANAGER_PROP, um);
	}

	/**
	 * Initializes the editor
	 */
	/*
	 * private void readSettings() throws MissingResourceException { String
	 * contenttype = "text/x-java"; String kit =
	 * "com.jeta.abeille.gui.sql.SQLKit"; Class kitclass; try { kitclass =
	 * Class.forName( kit ); } catch( ClassNotFoundException exc ) { throw new
	 * MissingResourceException( "Missing class", kit, "KitClass" ); }
	 * 
	 * ClassLoader loader = kitclass.getClassLoader();
	 * 
	 * JEditorPane.registerEditorKitForContentType( contenttype, kit, loader );
	 * m_editor = new JEditorPane( contenttype, "" );
	 * 
	 * Document doc = m_editor.getDocument();
	 * 
	 * doc.putProperty( TSTextNames.CREATED, Boolean.TRUE );
	 * 
	 * UndoManager um = new UndoManager(); doc.addUndoableEditListener( um );
	 * doc.putProperty( BaseDocument.UNDO_MANAGER_PROP, um ); }
	 */

	/**
	 * Registers an editor kit with the editor mgr
	 */
	public static void registerEditorKit(KitInfo info) {
		JEditorPane.registerEditorKitForContentType(info.getMimeType(), info.getKitClass().getName(),
				info.getClassLoader());
		m_kits.put(info.getKitClass(), info);
	}

	/**
	 * Sets the text for the sql window. Any existing text is replaced
	 * 
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		try {
			Document doc = getEditorPane().getDocument();
			doc.remove(0, doc.getLength());
			doc.insertString(0, text, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void dumpActions(BaseKit kit) {
		Keymap keymap = kit.getKeymap();
		Action[] actions = kit.getActions();
		for (int index = 0; index < actions.length; index++) {
			Action action = actions[index];
			System.out.print("    action: " + action.getValue(Action.NAME));

			KeyStroke[] strokes = keymap.getKeyStrokesForAction(action);
			if (strokes != null) {
				for (int j = 0; j < strokes.length; j++) {
					KeyStroke stroke = strokes[j];
					if (stroke != null)
						System.out.print(", " + stroke.toString());
				}
			}
			System.out.println("");
		}
	}

	public void foo() {
		// an array list of MultiKeyBindings
		MultiKeyBinding switchcase = new MultiKeyBinding(
				new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), }, BaseKit.switchCaseAction);

		ArrayList bindings = (ArrayList) Settings.getValue(BaseKit.class, SettingsNames.KEY_BINDING_LIST);
		for (int index = 0; index < bindings.size(); index++) {
			MultiKeyBinding binding = (MultiKeyBinding) bindings.get(index);
			if (BaseKit.switchCaseAction.equals(binding.actionName)) {
				System.out.println("ok, got switch case action... changing");
				ArrayList newbindings = (ArrayList) bindings.clone();
				newbindings.set(index, switchcase);

				Settings.setValue(BaseKit.class, SettingsNames.KEY_BINDING_LIST, newbindings);
				m_editor.setKeymap(Utilities.getKit(m_editor).getKeymap());

				System.out.println("done changing mapping ");
				break;
			}
		}
	}

	/**
	 * Sets the current editorKit and action->Vector(KeyStroke[]) mapping. Note:
	 * first item points to proper EditorKit class.
	 */
	public void dumpActions2() {

		BaseKit kit = (BaseKit) m_editor.getEditorKit();
		Keymap keymap = kit.getKeymap();
		Action[] actions = kit.getActions();

		// Create our sorter, ActionDescriptors knows themselves how to sort
		TreeMap treeMap = new TreeMap();
		// Fill it with new ActionDescriptors for actions, they'll be in-sorted
		for (int i = 0; i < actions.length; i++) {
			Action action = actions[i];
			treeMap.put(action.getValue(Action.NAME), action);
		}

		// add all inherited bindings
		// Class parent = kit.getClass().getSuperclass();
		Class parent = kit.getClass();
		System.out.println("TSEditorMgr.dumpActions2  kitclass " + parent.getName());

		Settings.KitAndValue[] kv = Settings.getValueHierarchy(parent, SettingsNames.KEY_BINDING_LIST);
		// go through all levels and add inherited bindings
		for (int i = kv.length - 1; i >= 0; i--)
			addKeyBindingList(treeMap, ((List) kv[i].value).iterator(), true);

	}

	private void addKeyBindingList(Map target, Iterator source, boolean inherited) {
		while (source.hasNext()) {
			MultiKeyBinding b = (MultiKeyBinding) source.next();
			Action a = (Action) target.get(b.actionName);

			if (a != null) {
				// we've found proper action
				KeySequence sequence = getKeySequenceForBinding(inherited, b);
				if (sequence != null) {
					System.out.println("TSEditorMgr.addKeyBinding.   action = " + a.getValue(Action.NAME) + "  keys: "
							+ sequence);
				}
			} else {
				// complain for weird mapping
				// System.err.println( "Weird mapping" );
			}
		}
	}

	private KeySequence getKeySequenceForBinding(boolean inherited, MultiKeyBinding binding) {
		KeyStroke[] sequence = binding.keys;
		if (sequence == null) { // convert simple KeyStroke to KeyStroke[1]
			if (binding.key == null)
				return null;
			sequence = new KeyStroke[1];
			sequence[0] = binding.key;
		}
		return new KeySequence(inherited, sequence);
	}

	/**
	 * Shows/hides the status bar on the editor
	 * 
	 * @param bshow
	 *            set to true/false to show/hide the status bar
	 */
	public void setStatusBarVisible(boolean bshow) {
		Settings.setValue(m_kitclass, SettingsNames.STATUS_BAR_VISIBLE, bshow ? Boolean.TRUE : Boolean.FALSE);
	}

	private final static class KeySequence {
		private boolean inherited;
		private KeyStroke[] sequence;

		KeySequence(boolean inherited, KeyStroke[] sequence) {
			this.inherited = inherited;
			this.sequence = sequence;
		}

		KeyStroke[] getKeyStrokes() {
			return sequence;
		}

		boolean isInherited() {
			return inherited;
		}

		public String toString() {
			return Utilities.keySequenceToString(sequence);
		}
	}

}
