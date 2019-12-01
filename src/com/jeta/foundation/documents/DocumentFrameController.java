/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.documents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.Serializable;

import javax.swing.JOptionPane;

import com.jeta.foundation.gui.filechooser.FileChooserConfig;
import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;
import com.jeta.foundation.gui.filechooser.TSFileFilter;

import com.jeta.open.gui.framework.JETAController;

/**
 */
public class DocumentFrameController extends JETAController {
	/**
	 * ctor Note that this controller must be created after the views have been
	 * loaded in the frame
	 */
	public DocumentFrameController(DocumentFrame frame) {
		super(frame);

		assignAction(DocumentNames.ID_NEW, new NewAction());
		assignAction(DocumentNames.ID_OPEN, new OpenAction());
		assignAction(DocumentNames.ID_SAVE, new SaveAction(false));
		assignAction(DocumentNames.ID_CLOSE, new CloseAction());
		assignAction(DocumentNames.ID_SAVE_AS, new SaveAction(true));

	}

	public DocumentFrame getDocumentFrame() {
		return (DocumentFrame) getView();
	}

	/**
    */
	public class CloseAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			getDocumentFrame().closeModel();
		}
	}

	/**
	 * Creates a new modeler
	 */
	public class NewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			DocumentFrame frame = getDocumentFrame();
			if (frame.getCurrentFile() == null)
				frame.createNewModel();
			else
				frame.createNewFrame();
		}
	}

	/**
	 * Saves the current frame state
	 */
	public class OpenAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			DocumentFrame frame = getDocumentFrame();
			if (frame.saveModel() == JOptionPane.CANCEL_OPTION)
				return;

			String MODEL_EXT = frame.getDefaultFileExtension();

			FileChooserConfig fcc = new FileChooserConfig("." + MODEL_EXT, new TSFileFilter(MODEL_EXT, "Model Files(*."
					+ MODEL_EXT + ")"));
			File f = TSFileChooserFactory.showOpenDialog(fcc);
			if (f != null) {
				frame.openModel(f);
			}
		}
	}

	/**
	 * Saves the current frame state
	 */
	public class SaveAction implements ActionListener {
		private boolean m_save_as = false;

		public SaveAction(boolean saveAs) {
			m_save_as = saveAs;
		}

		public void actionPerformed(ActionEvent evt) {
			DocumentFrame frame = getDocumentFrame();
			String MODEL_EXT = frame.getDefaultFileExtension();
			File f = frame.getCurrentFile();

			if (m_save_as)
				f = null;

			if (f == null) {
				FileChooserConfig fcc = new FileChooserConfig("." + MODEL_EXT, new TSFileFilter(MODEL_EXT,
						"Model Files(*." + MODEL_EXT + ")"));
				f = TSFileChooserFactory.showSaveDialog(fcc);
			}

			if (f != null) {
				String path = f.getPath();
				int pos = path.lastIndexOf(MODEL_EXT);
				if (pos != path.length() - MODEL_EXT.length()) {
					path = path + "." + MODEL_EXT;
					f = new File(path);
				}
				frame.saveModel(f);
			}
		}
	}

}
