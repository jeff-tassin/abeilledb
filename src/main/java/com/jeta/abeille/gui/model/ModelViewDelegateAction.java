package com.jeta.abeille.gui.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This action is used to map frame commands to model view handlers. The reason
 * is that the frame and view have shared actions (such as cut, copy and paste).
 * Recall that the view has a popup menu with these items and the frame has a
 * standard menu. We also wish to share the view actions with other frames, so
 * we need this delegate action to forwared frame menu commands to the view
 * handlers.
 * 
 * @author Jeff Tassin
 */
public class ModelViewDelegateAction implements ActionListener {
	private ViewGetter m_getter;

	/**
	 * The name of the action to forward
	 */
	private String m_actioncmd;

	/**
	 * ctor
	 */
	public ModelViewDelegateAction(ViewGetter getter, String action) {
		m_getter = getter;
		m_actioncmd = action;
	}

	public void actionPerformed(ActionEvent evt) {
		ModelView view = m_getter.getModelView();
		if (view != null) {
			ModelViewController controller = (ModelViewController) view.getController();
			controller.invokeAction(m_actioncmd);
		}
	}
}
