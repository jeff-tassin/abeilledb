/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * We override UndoManager so we can get undo/redo events. This allows us to
 * notify the frame windows to update the title/status if we undo to a point
 * where the document becomes 'unchanged'.
 * 
 * @author Jeff Tassin
 */
public class TSUndoManager extends UndoManager {
	/** the buffer we are managing undos for */
	private Buffer m_buffer;

	/** the buffer manager that contains the given buffer */
	private BufferMgr m_buffermgr;

	private long m_lastsavetime;

	/**
	 * ctor
	 */
	public TSUndoManager() {
		reset();
	}

	/**
	 * Invoked when the document has changed
	 */
	public void notifyChanged() {
		if (m_buffer != null && m_buffer.getEditor() != null) {
			Document doc = m_buffer.getEditor().getDocument();
			doc.putProperty(TSTextNames.MODIFIED, new Boolean(true));
			m_buffermgr.fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_CHANGED, m_buffer));
		}
	}

	/**
	 * Called by BeforeSave edit when we have undone the last edit and the
	 * document is now unmodified
	 */
	public void notifyUnmodified() {
		if (m_buffer != null && m_buffer.getEditor() != null) {
			Document doc = m_buffer.getEditor().getDocument();
			doc.putProperty(TSTextNames.MODIFIED, new Boolean(false));
			m_buffermgr.fireBufferEvent(new BufferEvent(BufferEvent.BUFFER_CHANGED, m_buffer));
		}
	}

	public void reset() {
		discardAllEdits(); // reset undo manager
		// Insert before-save undo event to enable unmodifying undo
		m_lastsavetime = System.currentTimeMillis();

		undoableEditHappened(new UndoableEditEvent(this, new BeforeSaveEdit(m_lastsavetime)));
	}

	/**
	 * Sets the buffer that we are managing undos for.
	 * 
	 * @param buffer
	 *            the buffer to set
	 */
	public void setBuffer(Buffer buffer) {
		m_buffer = buffer;
		Document doc = m_buffer.getEditor().getDocument();
		doc.addDocumentListener(new DocListener());
	}

	/**
	 * Sets the buffer manager. We tell the manager to fire buffer change events
	 * when we get undo/redo
	 * 
	 * @param buffermgr
	 *            the buffer manager to set
	 */
	public void setBufferMgr(BufferMgr buffermgr) {
		m_buffermgr = buffermgr;
	}

	/** Generic undoable edit that delegates to the given undoable edit. */
	private class FilterUndoableEdit implements UndoableEdit {

		protected UndoableEdit delegate;

		FilterUndoableEdit() {

		}

		public void undo() throws CannotUndoException {
			if (delegate != null)
				delegate.undo();
		}

		public boolean canUndo() {
			if (delegate != null) {
				return delegate.canUndo();
			} else {
				return false;
			}
		}

		public void redo() throws CannotRedoException {
			if (delegate != null) {
				delegate.redo();
			}
		}

		public boolean canRedo() {
			if (delegate != null) {
				return delegate.canRedo();
			} else {
				return false;
			}
		}

		public void die() {
			if (delegate != null) {
				delegate.die();
			}
		}

		public boolean addEdit(UndoableEdit anEdit) {
			if (delegate != null) {
				return delegate.addEdit(anEdit);
			} else {
				return false;
			}
		}

		public boolean replaceEdit(UndoableEdit anEdit) {
			if (delegate != null) {
				return delegate.replaceEdit(anEdit);
			} else {
				return false;
			}
		}

		public boolean isSignificant() {
			if (delegate != null) {
				return delegate.isSignificant();
			} else {
				return true;
			}
		}

		public String getPresentationName() {
			if (delegate != null) {
				return delegate.getPresentationName();
			} else {
				return ""; // NOI18N
			}
		}

		public String getUndoPresentationName() {
			if (delegate != null) {
				return delegate.getUndoPresentationName();
			} else {
				return ""; // NOI18N
			}
		}

		public String getRedoPresentationName() {
			if (delegate != null) {
				return delegate.getRedoPresentationName();
			} else {
				return ""; // NOI18N
			}
		}

	}

	/**
	 * Undoable edit that is put before the savepoint. Its replaceEdit() method
	 * will consume and wrap the edit that precedes the save. If the edit is
	 * added to the begining of the queue then the isSignificant()
	 * implementation guarantees that the edit will not be removed from the
	 * queue. When redone it marks the document as not modified.
	 */
	private class BeforeSaveEdit extends FilterUndoableEdit {

		private long saveTime;

		BeforeSaveEdit(long saveTime) {
			this.saveTime = saveTime;
		}

		public boolean replaceEdit(UndoableEdit anEdit) {
			if (delegate == null) {
				delegate = anEdit;
				return true; // signal consumed
			}

			return false;
		}

		public boolean addEdit(UndoableEdit anEdit) {
			if (!(anEdit instanceof BeforeModificationEdit)) {
				/*
				 * UndoRedo.addEdit() must not be done lazily because the edit
				 * must be "inserted" before the current one.
				 */
				TSUndoManager.this.addEdit(new BeforeModificationEdit(saveTime, anEdit));
				return true;
			}
			return false;
		}

		public void redo() {
			super.redo();

			if (saveTime == m_lastsavetime) {
				notifyUnmodified();
			}
		}

		public boolean isSignificant() {
			return (delegate != null);
		}

	}

	/**
	 * Edit that is created by wrapping the given edit. When undone it marks the
	 * document as not modified.
	 */
	private class BeforeModificationEdit extends FilterUndoableEdit {

		private long saveTime;

		BeforeModificationEdit(long saveTime, UndoableEdit delegate) {
			this.saveTime = saveTime;
			this.delegate = delegate;
		}

		public boolean addEdit(UndoableEdit anEdit) {
			if (delegate == null) {
				delegate = anEdit;
				return true;
			}

			return false;
		}

		public void undo() {
			super.undo();

			if (saveTime == m_lastsavetime) {
				notifyUnmodified();
			}
		}

	}

	/**
	 * Listener listening for document changes on opened documents.
	 */
	private class DocListener implements DocumentListener {
		public DocListener() {

		}

		public void changedUpdate(DocumentEvent e) {

		}

		public void insertUpdate(DocumentEvent evt) {
			notifyChanged();
		}

		public void removeUpdate(DocumentEvent evt) {
			notifyChanged();
		}
	}

}
