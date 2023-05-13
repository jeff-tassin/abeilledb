/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileInputStream;


import javax.swing.JComponent;
import javax.swing.JEditorPane;

import javax.swing.JOptionPane;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;


import javax.swing.text.Caret;
import javax.swing.text.Document;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;


import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;
import com.jeta.foundation.i18n.I18N;


import com.jeta.open.gui.framework.JETAContainer;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

/**
 * This is the controller for the editor window
 * 
 * @author Jeff Tassin
 */
public class EditorController extends TSController {
	/** the container window that contains the buffers, toolbar */
	private JETAContainer m_frame;

	/** the buffer manager */
	private BufferMgr m_buffermgr;

	/** the frequency for updating the menu and toolbar items (in milliseconds) */
	public static final int UPDATE_UI_FREQUENCY = 1000;

	/** the drop target for drag-drop */
	private DropTarget m_droptarget;

	/** the drop listener for drag-drop */
	EditorDropListener m_droplistener = new EditorDropListener();

	public EditorController(JETAContainer frame, BufferMgr bufferMgr) {
		super(frame);
		m_frame = frame;
		m_buffermgr = bufferMgr;

		assignAction(FrameKit.saveFileAction, new SaveAction());
		assignAction(FrameKit.saveAsAction, new SaveAsAction());
		assignAction(TSTextNames.ID_UNDO, new ActionFactory.UndoAction());
		assignAction(TSTextNames.ID_REDO, new ActionFactory.RedoAction());
		assignAction(TSTextNames.ID_CUT, new BaseKit.CutAction());
		assignAction(TSTextNames.ID_COPY, new BaseKit.CopyAction());
		assignAction(TSTextNames.ID_PASTE, new BaseKit.PasteAction());

		assignAction(FrameKit.newBufferAction, new NewBufferAction());
		assignAction(FrameKit.openFileAction, new OpenBufferAction());
		assignAction(FrameKit.closeFileAction, new KillBufferAction());
		assignAction(ExtKit.escapeAction, new CancelAction());
		assignAction(TSTextNames.ID_PREFERENCES, new EditorPreferencesAction(m_frame, m_buffermgr));
		assignAction(TSTextNames.ID_GOTO, new ExtKit.GotoAction());
		assignAction(TSTextNames.ID_FIND, new TSKit.FindAction());
		assignAction(TSTextNames.ID_REPLACE, new TSKit.ReplaceAction());
		assignAction(TSTextNames.ID_SELECT_ALL, new SelectAllAction());

		assignAction(FrameKit.nextBufferAction, new NextBufferAction());
		assignAction(FrameKit.prevBufferAction, new PrevBufferAction());

		m_frame.enableComponent(TSTextNames.ID_CUT, false);
		m_frame.enableComponent(TSTextNames.ID_COPY, false);

		// start a timer that we use to update the toolbar and menubar
		/*
		 * int delay = UPDATE_UI_FREQUENCY; ActionListener updater = new
		 * ActionListener() { public void actionPerformed(ActionEvent evt) {
		 * updateUI(); } }; new Timer( delay, updater ).start();
		 */
	}

	/**
	 * Override from TSController so we can update the UI
	 */
	public boolean actionPerformed(String actionName, ActionEvent evt) {
		boolean bresult = super.actionPerformed(actionName, evt);
		updateUI();
		return bresult;
	}

	/**
	 * Cancels any operation that is in progress
	 */
	public void cancel() {
		// unselect any selected text
		Buffer buff = m_buffermgr.getCurrentBuffer();
		if (buff != null) {
			buff.getEditor().requestFocus();
			Caret caret = buff.getEditor().getCaret();
			((TSCaret) caret).setMarked(false);
			caret.setDot(caret.getDot());
		}
	}

	/**
	 * Internal method that creates a buffer object. Specialized controllers an
	 * override to return specialized buffers
	 */
	protected Buffer createBuffer(Class kitClass) {
		return new Buffer();
	}

	/**
	 * Creates a new buffer based on the given editor kit
	 * 
	 * @param kit
	 *            the kit instance to base the new buffer on
	 * @param bInit
	 *            set to true if you want to initialize the buffer. In some
	 *            cases, you don't want to initialize here (e.g. when loading a
	 *            file )
	 * 
	 */
	public Buffer createNew(TSKit kit, boolean bInit) {
		JEditorPane editor = TSEditorUtils.createEditor(kit);
		Buffer buff = createBuffer(kit.getClass());
		buff.setEditor(editor);

		if (bInit)
			initializeBuffer(buff, kit.getClass(), null);
		return buff;
	}

	/**
	 * Creates a new buffer based on the given editor kit class
	 * 
	 * @param kitClass
	 *            the kit class to base the new buffer on
	 * @param bInit
	 *            set to true if you want to initalize the buffer
	 */
	public Buffer createNew(Class kitClass, boolean bInit) {
		JEditorPane editor = TSEditorUtils.createEditor(kitClass, FrameKit.class);
		Buffer buff = createBuffer(kitClass);
		buff.setEditor(editor);
		if (bInit)
			initializeBuffer(buff, kitClass, null);
		return buff;
	}

	/**
	 * @return the underlying buffer mgr
	 */
	public BufferMgr getBufferMgr() {
		return m_buffermgr;
	}

	/**
	 * @return the current buffer from the buffer manager
	 */
	public Buffer getCurrentBuffer() {
		return m_buffermgr.getCurrentBuffer();
	}

	/**
	 * @return the drop listener for drag-drop
	 */
	protected EditorDropListener getDropListener() {
		return m_droplistener;
	}

	/**
	 * @return the frame window that contains the buffers
	 */
	public JETAContainer getFrameWindow() {
		return m_frame;
	}

	/**
	 * Initializes a newly created buffer
	 */
	private void initializeBuffer(Buffer buff, Class kitClass, File file) {
		JEditorPane editor = buff.getEditor();
		JComponent comp = TSEditorUtils.getExtComponent(editor);
		buff.setContentPanel(comp);

		KeyBindingMgr.setKeyBindings(editor, new KitSet(kitClass, FrameKit.class));
		buff.setFile(file);
		editor.addCaretListener(new BufferCaretListener());

		if (file == null) {
			Document doc = editor.getDocument();
			doc.putProperty(TSTextNames.MODIFIED, new Boolean(false));
			doc.putProperty(TSTextNames.CREATED, new Boolean(true));
			doc.putProperty(TSTextNames.BUFFER, buff);

			TSUndoManager undomgr = (TSUndoManager) doc.getProperty(BaseDocument.UNDO_MANAGER_PROP);
			assert (undomgr != null);
			undomgr.setBuffer(buff);
			undomgr.setBufferMgr(m_buffermgr);
		}

		m_buffermgr.addBuffer(buff);
		m_buffermgr.fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_CREATED, buff));
		selectBuffer(buff);

		// create the drop target for the editor
		DropTarget droptarget = new DropTarget(editor, getDropListener());

		/**
		 * we do this in order to have the editor receive focus when it appears
		 * on the screen
		 */
		/** without this, the editor does not get focus */
		TSGuiToolbox.simulateMouseClick(editor, 1, 1);
	}

	/**
	 * Closes the given buffer. If the buffer has been modified, we ask the user
	 * if the file should be saved.
	 * 
	 * @param buff
	 *            the buffer to delete(close)
	 */
	public void killBuffer(Buffer buff) {
		if (buff == null)
			return;

		if (buff.isModified()) {
			int nresult = TSGuiToolbox.showConfirmDialog(I18N.getLocalizedMessage("Buffer_modified_kill"), "Confirm",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (nresult == JOptionPane.CANCEL_OPTION)
				return;
			else if (nresult == JOptionPane.YES_OPTION) {
				if (saveFile(buff, buff.getFile()) == null) {
					return;
				}
			}
		}

		m_buffermgr.deleteBuffer(buff);
	}

	/**
	 * Loads the given file into the editor. It is assumed that the file is a
	 * text file.
	 * 
	 * @param file
	 *            the file to load
	 */
	public void _loadFile(JEditorPane editor, File file) {
		try {
			// this causes a new Document object to be created
			// editor.read( new FileInputStream(file), file.getCanonicalPath()
			// );
			javax.swing.text.EditorKit kit = editor.getUI().getEditorKit(editor);
			Document doc = editor.getDocument();
			if (doc.getLength() > 0)
				doc.remove(0, doc.getLength());

			java.io.InputStreamReader file_reader = new java.io.InputStreamReader(new FileInputStream(file));
			java.io.BufferedReader reader = new java.io.BufferedReader(file_reader);
			kit.read(reader, doc, 0);
			editor.setCaretPosition(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Can't read file", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Loads the given file into the editor. It is assumed that the file is a
	 * text file.
	 * 
	 * @param file
	 *            the file to load
	 */
	public void loadFile(Buffer buffer, File file) {
		_loadFile(buffer.getEditor(), file);

		String ext = TSUtils.getFileExtension(file);
		KitInfo info = getKitInfo(ext);

		Document doc = buffer.getEditor().getDocument();
		doc.putProperty(TSTextNames.MODIFIED, new Boolean(false));
		doc.putProperty(TSTextNames.CREATED, new Boolean(true));
		doc.putProperty(TSTextNames.BUFFER, buffer);
		TSUndoManager undomgr = (TSUndoManager) doc.getProperty(BaseDocument.UNDO_MANAGER_PROP);
		assert (undomgr != null);
		undomgr.setBuffer(buffer);
		undomgr.setBufferMgr(getBufferMgr());

		// m_undo.discardAllEdits();
		// setUndoableEditListener();
		KeyBindingMgr.setKeyBindings(buffer.getEditor(), new KitSet(info.getKitClass(), FrameKit.class));

		updateUI();
	}

	/**
	 * @return the kit info for the given extension. We allow overrides from
	 *         EditorController to allow controllers the dynamically provide for
	 *         kit infos
	 */
	protected KitInfo getKitInfo(String ext) {
		return TSEditorMgr.getKitInfo(ext);
	}

	/**
	 * Opens the given file in the buffer
	 */
	public void openFile(File f) {
		// first we need to make sure the file is not already opened
		Buffer buff = m_buffermgr.findBuffer(f);
		if (buff == null) {
			String ext = TSUtils.getFileExtension(f);
			KitInfo info = getKitInfo(ext);
			buff = createNew(info.getKitClass(), true);
			loadFile(buff, f);
			buff.setFile(f);
			selectBuffer(buff);
			m_buffermgr.fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_NAME_CHANGED, buff));
		} else {
			if (buff.isModified()) {
				String title = I18N.getLocalizedMessage("Confirm");
				String msg = I18N.getLocalizedMessage("Buffer_is_modified_reload");
				int result = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					openFileIntoBuffer(f, buff);
					selectBuffer(buff);
				}
			} else {
				selectBuffer(buff);
			}
		}
	}

	/**
	 * Opens the file into the given buffer
	 */
	public void openFileIntoBuffer(File f, Buffer buff) {
		if (f != null) {
			buff.clear();
			invokeAction(ExtKit.escapeAction);
			loadFile(buff, f);
			buff.getContentPanel().validate();
			buff.getContentPanel().repaint();
			org.netbeans.editor.Utilities.requestFocus(buff.getEditor());
			m_buffermgr.fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_CHANGED, buff));
		}
	}

	/**
	 * Saves the buffer to the given file. If the file is null, then the user is
	 * prompted with the save file dialog.
	 * 
	 * @return the file that the buffer was saved to. Null is returned if the
	 *         user cancels the operation.
	 */
	public File saveFile(Buffer buffer, File file) {
		// saves the file to a temporary file name
		try {
			if (file == null) {
				// the buffer has never been saved to a file, so we need to
				// query the user for a file name
				file = TSFileChooserFactory.showSaveDialog();
				if (file == null)
					return null;
			}

			JEditorPane editor = buffer.getEditor();
			TSUtils.safeSaveFile(file.getParent(), file.getName(), editor);
			org.netbeans.editor.Utilities.setStatusText(editor, "Saved " + file.getPath());

			Document doc = editor.getDocument();
			doc.putProperty(TSTextNames.MODIFIED, new Boolean(false));
			doc.putProperty(TSTextNames.CREATED, new Boolean(false));
			doc.putProperty(TSTextNames.BUFFER, buffer);

			TSUndoManager undomgr = (TSUndoManager) doc.getProperty(BaseDocument.UNDO_MANAGER_PROP);
			undomgr.reset();

			// this get's rid of the modified * indicator in the title bar
			m_buffermgr.fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_CHANGED, buffer));
			return file;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Selectes the given buffer
	 */
	public void selectBuffer(Buffer buffer) {
		m_buffermgr.selectBuffer(buffer);
		updateUI();
	}

	/**
	 * Updates the toolbar and menus on the JEmacsFrame
	 */
	public void updateUI() {
		super.updateComponents(null);
	}

	/**
	 * Cancels and selected text or search
	 */
	public class CancelAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			cancel();
		}
	}

	/**
	 * Closes the current buffer
	 */
	public class KillBufferAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			killBuffer(m_buffermgr.getCurrentBuffer());
		}
	}

	/**
	 * Handle the new command. Creates a new, empty buffer
	 */
	public class NewBufferAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			createNew(TSPlainKit.class, true);
		}
	}

	/**
	 * Make the next buffer active
	 */
	public class NextBufferAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_buffermgr.selectNextBuffer();
		}
	}

	/**
	 * Handle the open command
	 */
	public class OpenBufferAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			File f = TSFileChooserFactory.showOpenDialog();
			if (f != null) {
                openFileIntoBuffer(f, m_buffermgr.getCurrentBuffer());
			}
		}
	}

	/**
	 * Make the previous buffer active
	 */
	public class PrevBufferAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_buffermgr.selectPrevBuffer();
		}
	}

	/**
	 * Saves the buffer
	 */
	public class SaveAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Buffer buffer = m_buffermgr.getCurrentBuffer();
			File f = saveFile(buffer, buffer.getFile());
			if (f != null) {
				buffer.setFile(f);
			}
		}
	}

	/**
	 * Saves the buffer as a new file
	 */
	public class SaveAsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Buffer buffer = m_buffermgr.getCurrentBuffer();
			String oldname = buffer.getName();
			File f = saveFile(buffer, null);
			if (f != null) {
				buffer.setFile(f);
				// m_buffermgr.fireBufferEvent( new BufferEvent(
				// BufferEvent.BUFFER_CHANGED, buffer ) );
			}
		}
	}

	/**
	 * Select All
	 */
	public class SelectAllAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Buffer buffer = m_buffermgr.getCurrentBuffer();
			if (buffer != null) {
				JEditorPane editor = buffer.getEditor();
				editor.selectAll();
			}
		}
	}

	public class BufferCaretListener implements CaretListener {
		public void caretUpdate(CaretEvent e) {
			if (e.getDot() != e.getMark()) {
				m_frame.enableComponent(TSTextNames.ID_CUT, true);
				m_frame.enableComponent(TSTextNames.ID_COPY, true);
			} else {
				m_frame.enableComponent(TSTextNames.ID_CUT, false);
				m_frame.enableComponent(TSTextNames.ID_COPY, false);
			}
		}
	}

}
