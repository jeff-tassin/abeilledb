package com.jeta.abeille.gui.sql;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;

import java.io.File;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.Document;

import com.jeta.foundation.gui.editor.Buffer;
import com.jeta.foundation.gui.editor.EditorDropListener;

/**
 * The drop listener for the SQL editor. We override here because we want
 * specialized handling for dropping files. If a single file is being dropped
 * and the target is a SQLBuffer, then we load the file into the buffer. If
 * multiple files are being dropped or the target is not a SQLBuffer, then we
 * open the files into new buffers.
 * 
 * @author Jeff Tassin
 */
public class SQLEditorDropListener extends EditorDropListener {
	/** the controller for the frame/buffers we are handling drop events for */
	private SQLController m_controller;

	/**
	 * ctor
	 */
	public SQLEditorDropListener(SQLController controller) {
		m_controller = controller;
	}

	/**
	 * Invoked when you are dragging over the DropSite
	 * 
	 */
	public void dragEnter(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			event.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			super.dragEnter(event);
		}
	}

	/**
	 * Invoked when a drag operation is going on
	 * 
	 */
	public void dragOver(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			event.acceptDrag(DnDConstants.ACTION_COPY);
		} else
			super.dragOver(event);
	}

	/**
	 * a drop has occurred
	 */
	public void drop(DropTargetDropEvent event) {
		Transferable transferable = event.getTransferable();
		if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_COPY);
				List files = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				if (files.size() == 1) {
					Buffer buff = m_controller.getCurrentBuffer();
					if (buff instanceof SQLBuffer) {
						m_controller.openFileIntoBuffer((File) files.get(0), buff);
						m_controller.selectBuffer(buff);
					} else {
						m_controller.openFile((File) files.get(0));
					}
				} else {
					for (int i = 0; i < files.size(); i++) {
						File f = (File) files.get(i);
						m_controller.openFile(f);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			super.drop(event);
		}
	}

}
