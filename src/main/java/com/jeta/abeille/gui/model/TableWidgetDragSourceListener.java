package com.jeta.abeille.gui.model;

import java.awt.Component;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceEvent;

import com.jeta.foundation.gui.dnd.DnDSupport;

public class TableWidgetDragSourceListener implements DragSourceListener {
	private TableWidget m_widget;

	/**
	 * ctor
	 */
	public TableWidgetDragSourceListener(TableWidget widget) {
		m_widget = widget;
	}

	/**
	 * DragSourceListener. The dragging has ended. We don't need to do anything
	 * to the tree model here. The ObjectTreeDropListener will handle moving the
	 * tree nodes if that is the resulting action.
	 */
	public void dragDropEnd(DragSourceDropEvent event) {

	}

	/**
	 * DragSourceListener The dragging has entered the DropSite
	 * 
	 */
	public void dragEnter(DragSourceDragEvent event) {

	}

	/**
	 * DragSourceListener The dragging has exited the DropSite
	 */
	public void dragExit(DragSourceEvent event) {

	}

	/**
	 * DragSourceListener The dragging is currently over the DropSite. We
	 * override this method mainly to set the cursor since it seems to be broken
	 * in the current jdk1.4
	 */
	public void dragOver(DragSourceDragEvent event) {
		DragSourceContext ctx = event.getDragSourceContext();
		int targetactions = DnDSupport.getTargetActions(event);

		// we don't allow dragging into the tree from outside of the tree just
		// yet
		Component target = DnDSupport.getTarget();
		if ((targetactions & DnDConstants.ACTION_MOVE) > 0)
			ctx.setCursor(DragSource.DefaultMoveDrop);
		else
			ctx.setCursor(DragSource.DefaultMoveNoDrop);
	}

	/**
	 * DragSourceListener Invoked when the user changes the dropAction
	 * 
	 */
	public void dropActionChanged(DragSourceDragEvent event) {

	}

}
