package com.jeta.abeille.gui.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import com.jeta.abeille.gui.main.MainFrame;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This is the main controller for the ObjectTreeFrame
 * 
 * @author Jeff Tassin
 */
public class ObjectTreeFrameController extends TSController implements UIDirector {
	private ObjectTreeFrame m_frame;

	public ObjectTreeFrameController(ObjectTreeFrame frame) {
		super(frame);
		m_frame = frame;
		frame.setUIDirector(this);
		m_frame.getComponentByName(ObjectTreeFrameNames.ID_CLOSE_FRAME).addMouseListener(new CloseFrameAction());
		m_frame.getComponentByName(ObjectTreeFrameNames.ID_DOCK_FRAME).addMouseListener(new DockFrameAction());
	}

	public class CloseFrameAction extends MouseAdapter {
		public void mouseClicked(MouseEvent evt) {
			MainFrame mframe = (MainFrame) TSWorkspaceFrame.getInstance();
			mframe.showObjectTree(false);
		}
	}

	public class DockFrameAction extends MouseAdapter {
		public void mouseClicked(MouseEvent evt) {
			MainFrame mframe = (MainFrame) TSWorkspaceFrame.getInstance();
			mframe.dockObjectTree();
		}
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		TSPanel view = m_frame.getCurrentView();

		if (view != null) {
			if (view instanceof ObjectTreeView) {
				ObjectTreeView otview = (ObjectTreeView) view;
				// System.out.println(
				// "ObjectTreeFrameController updateComponents object tree view isDragging: "
				// + otview.getTree().isDragging() );
				if (otview != null) {
					ObjectTree otree = otview.getTree();
					if (otree != null && !otree.isDragging())
						otview.updateComponents();
				}
			} else {
				assert (false);
				view.updateComponents();
			}
		}
	}
}
