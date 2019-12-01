/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ListIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.JFileChooser;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;

import javax.swing.text.Caret;
import javax.swing.text.Document;

import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.userprops.*;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.JETAContainer;

/**
 * This is the controller for the text editor window
 * 
 * @author Jeff Tassin
 */
public class TSTextController extends TSController {
	private JETAContainer m_view; // the frame window we are controlling
	private Buffer m_buffer; // the buffer (editor) we are controlling
	private JEditorPane m_editor;

	private UndoManager m_undo = new UndoManager();
	private UndoableEditListener m_undoeditlistener;
	private TSUserProperties m_settings; // user settings that affect various
											// operations

	public TSTextController(JETAContainer view, Buffer buffer) {
		super(view);
		m_view = view;
		m_buffer = buffer;
		m_editor = m_buffer.getEditor();

		assignAction(FrameKit.saveFileAction, new SaveAction());

		assignAction(TSTextNames.ID_UNDO, new UndoAction());
		assignAction(TSTextNames.ID_REDO, new RedoAction());
		assignAction(TSTextNames.ID_CUT, new CutAction());
		assignAction(TSTextNames.ID_COPY, new CopyAction());
		assignAction(TSTextNames.ID_PASTE, new PasteAction());

		setUndoableEditListener();

		m_view.enableComponent(TSTextNames.ID_CUT, false);
		m_view.enableComponent(TSTextNames.ID_COPY, false);

		// update the cut/copy menu items
		m_editor.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				if (e.getDot() != e.getMark()) {
					m_view.enableComponent(TSTextNames.ID_CUT, true);
					m_view.enableComponent(TSTextNames.ID_COPY, true);
				} else {
					m_view.enableComponent(TSTextNames.ID_CUT, false);
					m_view.enableComponent(TSTextNames.ID_COPY, false);
				}
			}
		});

		updateUIComponents();
	}

	/**
	 * Cuts the selected text from the sql editor window to the system clipboard
	 */
	public class CutAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_editor.cut();
		}
	}

	/**
	 * Copies the selected text from the sql editor window to the system
	 * clipboard
	 */
	public class CopyAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_editor.copy();
		}
	}

	/**
	 * Pastes the text from the system clipboard into the sql editor window
	 */
	public class PasteAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_editor.paste();
		}
	}

	/**
	 * Redo's the last undo
	 */
	public class RedoAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				m_undo.redo();
			} catch (CannotRedoException ex) {
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			updateUIComponents();
		}
	}

	/**
	 * Saves the buffer
	 */
	public class SaveAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			saveFile();
		}
	}

	/**
	 * Undo's the last change
	 */
	public class UndoAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				m_undo.undo();
			} catch (CannotUndoException ex) {
				System.out.println("Unable to undo: " + ex);
				ex.printStackTrace();
			}
			updateUIComponents();
		}
	}

	/**
	 * Loads the given file into the editor. It is assumed that the file is a
	 * text file.
	 * 
	 * @param file
	 *            the file to load
	 */
	public void loadFile(File file) {
		_loadFile(file);
		m_undo.discardAllEdits();
		setUndoableEditListener();
		updateUIComponents();
	}

	/**
	 * Loads the given file into the editor. It is assumed that the file is a
	 * text file.
	 * 
	 * @param file
	 *            the file to load
	 */
	public void _loadFile(File file) {
		try {
			// this causes a new Document object to be created
			m_editor.read(new FileInputStream(file), file.getCanonicalPath());
			TSEditorMgr.initializeDocument(m_editor.getDocument());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Can't read file", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Prompts the user for a file name, opens the selected file, and loads it
	 * into the editor
	 */
	public static void openStandard() {
		JFileChooser chooser = new JFileChooser();
		GenericFileFilter filter = new GenericFileFilter();
		filter.addExtension("sql");
		filter.addExtension("txt");
		filter.setDescription("SQL & Text Files");
		chooser.setFileFilter(filter);

		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		if (userprops != null) {
			String lastdir = userprops.getProperty("javax.swing.JFileChooser.filechooser.lastdir");
			if (lastdir != null) {
				File homedir = new File(lastdir);
				chooser.setCurrentDirectory(homedir);
			}
		}

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				File f = chooser.getSelectedFile();
				if (f.isFile()) {
					// loadFile( f.getAbsolutePath() );
				}

				String dir = chooser.getCurrentDirectory().getPath();
				if (userprops != null)
					userprops.setProperty("javax.swing.JFileChooser.filechooser.lastdir", dir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves the file
	 */
	public void saveFile() {
		// saves the file to a temporary file name
		try {
			System.out.println("saving file: " + m_buffer.getName());
			TSUtils.safeSaveFile(m_buffer.getFile().getParent(), m_buffer.getFile().getName(), m_editor);
			org.netbeans.editor.Utilities.setStatusText(m_editor, "Saved " + m_buffer.getFile().getPath());
			// doc.putProperty( TSTextNames.MODIFIED, new Boolean( false ) );
			// doc.putProperty( TSTextNames.CREATED, new Boolean( false ) );
			// doc.putProperty( TSTextNames.BUFFER, m_buffer );
			// doc.addDocumentListener( new MarkingDocumentListener() );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUndoableEditListener() {
		if (m_undoeditlistener != null)
			m_editor.getDocument().removeUndoableEditListener(m_undoeditlistener);

		m_undoeditlistener = new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent e) {
				m_undo.addEdit(e.getEdit());
				updateUIComponents();
			}
		};

		m_editor.getDocument().addUndoableEditListener(m_undoeditlistener);
	}

	/**
	 * Enables/disables menu and toolbar items depending on the program state
	 */
	public void updateUIComponents() {
		// m_view.enableAll( true );

		if (m_undo.canRedo())
			m_view.enableComponent(TSTextNames.ID_REDO, true);
		else
			m_view.enableComponent(TSTextNames.ID_REDO, false);

		if (m_undo.canUndo())
			m_view.enableComponent(TSTextNames.ID_UNDO, true);
		else
			m_view.enableComponent(TSTextNames.ID_UNDO, false);

		Caret caret = m_editor.getCaret();
		if (caret.getDot() != caret.getMark()) {
			m_view.enableComponent(TSTextNames.ID_CUT, true);
			m_view.enableComponent(TSTextNames.ID_COPY, true);
		} else {
			m_view.enableComponent(TSTextNames.ID_CUT, false);
			m_view.enableComponent(TSTextNames.ID_COPY, false);
		}

	}

	/**
	 * Listener listening for document changes on opened documents. There is
	 * initially one instance per opened document, but this listener is one-fire
	 * only - as soon as it gets fired, markes changes and removes itself from
	 * document. On save, new Listener is hooked again.
	 */
	private class MarkingDocumentListener implements DocumentListener {
		public MarkingDocumentListener() {

		}

		private void markChanged(DocumentEvent evt) {
			Document doc = evt.getDocument();
			doc.putProperty(TSTextNames.MODIFIED, new Boolean(true));

			Buffer buff = (Buffer) doc.getProperty(TSTextNames.BUFFER);
			// tabPane.setTitleAt( index, file.getName() + '*' );
			doc.removeDocumentListener(this);
		}

		public void changedUpdate(DocumentEvent e) {

		}

		public void insertUpdate(DocumentEvent evt) {
			markChanged(evt);
		}

		public void removeUpdate(DocumentEvent evt) {
			markChanged(evt);
		}
	}
}
