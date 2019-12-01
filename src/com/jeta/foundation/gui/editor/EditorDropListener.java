/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.Point;
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

/**
 * The drop listener for the editor We need a drop listener instead of using the
 * Transferable because we need to determine the mouse position at the time of
 * the drop so we can insert text at the correct place
 */
public class EditorDropListener implements DropTargetListener {

	/**
	 * ctor
	 */
	public EditorDropListener() {
	}

	/**
	 * Invoked when you are dragging over the DropSite
	 * 
	 */
	public void dragEnter(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			event.acceptDrag(DnDConstants.ACTION_COPY);
		} else
			event.rejectDrag();
	}

	/**
	 * Invoked when you are exit the DropSite without dropping
	 * 
	 */
	public void dragExit(DropTargetEvent event) {

	}

	/**
	 * Invoked when a drag operation is going on
	 * 
	 */
	public void dragOver(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			event.acceptDrag(DnDConstants.ACTION_COPY);
			DropTargetContext context = event.getDropTargetContext();
			java.awt.Component comp = context.getComponent();
			if (comp instanceof JEditorPane) {
				JEditorPane editor = (JEditorPane) comp;
				int docpos = editor.viewToModel(event.getLocation());
				editor.requestFocus();
				editor.setCaretPosition(docpos);
			}
		} else {
			event.rejectDrag();
		}
	}

	/**
	 * a drop has occurred
	 */
	public void drop(DropTargetDropEvent event) {
		Transferable transferable = event.getTransferable();
		if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				event.acceptDrop(DnDConstants.ACTION_COPY);
				DropTargetContext context = event.getDropTargetContext();
				java.awt.Component comp = context.getComponent();
				assert (comp instanceof JEditorPane);
				if (comp instanceof JEditorPane) {
					JEditorPane editor = (JEditorPane) comp;
					String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
					int docpos = editor.viewToModel(event.getLocation());
					Document doc = editor.getDocument();
					doc.insertString(docpos, data, null);
				}
				event.dropComplete(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event.rejectDrop();
		}
	}

	/**
	 * Invoked if the use modifies the current drop gesture
	 */
	public void dropActionChanged(DropTargetDragEvent event) {
	}

}
