/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.Collection;

import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;

import org.netbeans.editor.BaseDocument;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;

/**
 * Updates the editor frame menus and toolbar buttons
 * 
 * @author Jeff Tassin
 */
public class EditorFrameUIDirector implements UIDirector {
	/** the frame we are updating */
	private JETAContainer m_frame;

	/** the buffer manager that is associated with the frame */
	private BufferMgr m_buffermgr;

	/**
	 * ctor
	 */
	public EditorFrameUIDirector(JETAContainer frame, BufferMgr buffmgr) {
		m_frame = frame;
		m_buffermgr = buffmgr;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		Collection buffers = m_buffermgr.getBuffers();
		if (buffers.size() == 0) {
			// m_frame.enableAll( false );
			m_frame.enableComponent(FrameKit.newBufferAction, true);
			m_frame.enableComponent(FrameKit.openFileAction, true);
			return;
		}

		m_frame.enableComponent(TSTextNames.ID_FIND, true);
		m_frame.enableComponent(TSTextNames.ID_REPLACE, true);
		m_frame.enableComponent(TSTextNames.ID_SELECT_ALL, true);
		m_frame.enableComponent(TSTextNames.ID_GOTO, true);
		m_frame.enableComponent(FrameKit.saveFileAction, true);
		m_frame.enableComponent(FrameKit.saveAsAction, true);

		Buffer buff = m_buffermgr.getCurrentBuffer();
		if (buff != null) {
			Document doc = buff.getEditor().getDocument();
			UndoableEdit undomgr = (UndoableEdit) doc.getProperty(BaseDocument.UNDO_MANAGER_PROP);
			if (undomgr != null) {
				if (undomgr.canRedo())
					m_frame.enableComponent(TSTextNames.ID_REDO, true);
				else
					m_frame.enableComponent(TSTextNames.ID_REDO, false);

				if (undomgr.canUndo())
					m_frame.enableComponent(TSTextNames.ID_UNDO, true);
				else
					m_frame.enableComponent(TSTextNames.ID_UNDO, false);
			}

			m_frame.enableComponent(TSTextNames.ID_PASTE, true);

			Caret caret = buff.getEditor().getCaret();
			if (caret.getDot() != caret.getMark()) {
				m_frame.enableComponent(TSTextNames.ID_CUT, true);
				m_frame.enableComponent(TSTextNames.ID_COPY, true);
			} else {
				m_frame.enableComponent(TSTextNames.ID_CUT, false);
				m_frame.enableComponent(TSTextNames.ID_COPY, false);
			}
		}
	}

}
