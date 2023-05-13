/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.documents;

import java.io.File;
import java.io.Serializable;

import javax.swing.JOptionPane;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.i18n.I18N;

/**
 * @author Jeff Tassin
 */
public abstract class DocumentFrame extends TSInternalFrame implements DocumentOwner {
	/**
	 * The file where the model state is stored.
	 */
	private File m_model_file;

	/**
	 * The default file extension
	 */
	private String m_default_ext;

	/**
	 * ctor
	 */
	public DocumentFrame(String caption, String defaultExt) {
		super(caption);
		m_default_ext = defaultExt;
	}

	/**
	 * Called when this frame has been activated. If a current model is opened,
	 * check if another window has the same document as well. If so, we need to
	 * reload the latest version in case the document was changed in the other
	 * window
	 */
	protected void activated() {
		if (m_model_file != null) {
			DocumentManager doc_mgr = getDocumentManager();
			DocumentOwner doc_owner = doc_mgr.getCurrentOwner(m_model_file);
			if (doc_owner != null && doc_owner != this) {
				if (doc_owner.isDocumentModified()) {
					try {
						Object doc_copy = doc_mgr.cloneDocument(doc_owner.getCurrentDocument());
						// System.out.println(
						// "$$$$$$$$$$$$$$$$$DocumentFrame.activated  with modified model setting latest model for: "
						// + m_model_file.getName() );
						doc_mgr.setDocumentModified(m_model_file, true);

						/**
						 * we do this because the current owner now has the
						 * latest copy and the docmanager still has the doc
						 * marked as modified. If we did not do this, we would
						 * clone every time we switched back from an originally
						 * modified document
						 */
						doc_owner.setDocumentModified(false);
						setModel(doc_copy);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// System.out.println(
					// "___________________DocumentFrame.activated  with UNModified model "
					// );
				}
			}
			doc_mgr.setCurrentOwner(m_model_file, this);
		}
	}

	public abstract Serializable createNewModel();

	public abstract TSInternalFrame createNewFrame();

	/**
	 * 
	 * Returns the default file extension for models
	 */
	public String getDefaultFileExtension() {
		return m_default_ext;
	}

	/**
	 * Returns the main document manager for the application
	 */
	public DocumentManager getDocumentManager() {
		return (DocumentManager) ComponentMgr.lookup(DocumentManager.COMPONENT_ID);
	}

	/**
	 * Removes the model from the current frame without saving.
	 */
	protected abstract void clearModel();

	/**
	 * Closes and removes the model from the frame
	 */
	public boolean closeModel() {
		int result = saveModel();
		if (result == JOptionPane.CANCEL_OPTION) {
			return false;
		}
		DocumentManager doc_mgr = getDocumentManager();
		doc_mgr.removeReference(m_model_file, this);

		if (TSWorkspaceFrame.getInstance().isExiting()) {
			doc_mgr.removeAllReferences(m_model_file);
		}
		m_model_file = null;
		clearModel();
		return true;
	}

	protected boolean isAutoSave() {
		return false;
	}

	public int saveModel() {
		int result = JOptionPane.YES_OPTION;
		DocumentManager doc_mgr = getDocumentManager();
		if (isDocumentModified() || doc_mgr.isDocumentModified(m_model_file)) {
			if (m_model_file != null && isAutoSave()) {
				getController().invokeAction(DocumentNames.ID_SAVE);
			} else {
				String fname = "New Model";
				if (m_model_file != null)
					fname = m_model_file.getName();

				String msg = I18N.format("Save_1", fname);
				result = JOptionPane.showConfirmDialog(null, msg, I18N.getLocalizedMessage("Confirm"),
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					getController().invokeAction(DocumentNames.ID_SAVE);
				}
			}
		}
		return result;
	}

	/**
	 * DocumentOwner Implementation
	 */
	public File getCurrentFile() {
		return m_model_file;
	}

	public Serializable loadModel(File f) throws Exception {
		java.io.FileInputStream fis = new java.io.FileInputStream(f);
		java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.BufferedInputStream(fis));
		return (Serializable) ois.readObject();
	}

	/**
	 * Opens the model. First attemps to load the model from the document
	 * manager
	 */
	public boolean openModel(File f) {
		try {
			/** trying to open save file */
			if (f.equals(getCurrentFile()))
				return false;

			if (!closeModel())
				return false;

			Serializable model = null;
			DocumentManager doc_mgr = getDocumentManager();
			DocumentOwner doc_owner = doc_mgr.getCurrentOwner(f);
			if (doc_owner != null) {
				model = doc_owner.getCurrentDocument();
				assert (model != null);
				model = doc_mgr.cloneDocument(model);
			} else {
				try {
					model = loadModel(f);
				} catch (Exception e) {
					model = null;
					TSErrorDialog dlg = TSErrorDialog.createDialog("Unable to load file", e);
					dlg.showCenter();
				}
			}

			if (model != null) {
				m_model_file = f;
				setModel(model);
				setTitle(f.getName());
				/** register sets the owner as well */
				doc_mgr.registerDocument(f, this);
				return true;
			} else {
				m_model_file = null;
				clearModel();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Serializable saveModel(File f) {
		if (f != null) {
			DocumentManager doc_mgr = getDocumentManager();
			if (!f.equals(m_model_file)) {
				if (doc_mgr.isDocumentRegistered(f)) {
					String msg = I18N.format("file_is_already_opened_1", f.getName());
					JOptionPane.showMessageDialog(getDelegate(), msg, I18N.getLocalizedMessage("Error"),
							JOptionPane.ERROR_MESSAGE);
					return null;
				}

				doc_mgr.removeReference(m_model_file, this);
				doc_mgr.registerDocument(f, this);
			}

			m_model_file = f;

			try {
				Serializable obj = getCurrentDocument();
				java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
				java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.BufferedOutputStream(fos));
				oos.writeObject(obj);
				oos.close();
				doc_mgr.setDocumentModified(f, false);
				setTitle(f.getName());
				return obj;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected abstract void setModel(Object model);

	/**
	 * Called by the TS framework before closing the window. Specialized frames
	 * should override if they wish to perform special processing. This method
	 * should not call setVisible(false) or dispose. That is handled by the
	 * frame work.
	 */
	public boolean tryCloseFrame() {
		return closeModel();
	}

}
