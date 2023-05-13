package com.jeta.abeille.gui.security;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.open.gui.framework.UIDirector;

/**
 * Controller for the SecurityMgrFrame
 * 
 * @author Jeff Tassin
 */
public class SecurityMgrFrameController extends TSController implements UIDirector {
	/** the frame we are controlling events for */
	private SecurityMgrFrame m_frame;

	/**
	 * ctor
	 */
	public SecurityMgrFrameController(SecurityMgrFrame frame) {
		super(frame);
		m_frame = frame;
		frame.setUIDirector(this);
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		TSPanel panel = m_frame.getCurrentView();
		if (panel != null) {
			panel.updateComponents(evt);
		}
	}
}
