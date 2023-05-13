package com.jeta.abeille.gui.formbuilder;

import com.jeta.abeille.gui.update.InstanceFrame;
import com.jeta.abeille.gui.update.InstanceFrameUIDirector;
import com.jeta.abeille.gui.update.InstanceView;

/**
 * This is the UIDirector for the FormInstanceFrame
 * 
 * @author Jeff Tassin
 */
public class FormInstanceFrameUIDirector extends InstanceFrameUIDirector {

	/**
	 * ctor
	 */
	public FormInstanceFrameUIDirector(InstanceFrame frame, InstanceView view) {
		super(frame, view);
	}

	public void updateComponents(java.util.EventObject evt) {
		super.updateComponents(evt);

		InstanceFrame iframe = getFrame();
		iframe.enableComponent(FormNames.ID_SHOW_QUERY_PLAN, isEnabled(InstanceFrame.ID_RUN_QUERY));
		iframe.enableComponent(FormNames.ID_SHOW_INSERT_PLAN, isEnabled(InstanceFrame.ID_ADD_ROW));
		iframe.enableComponent(FormNames.ID_SHOW_UPDATE_PLAN, isEnabled(InstanceFrame.ID_MODIFY_ROW));
		iframe.enableComponent(FormNames.ID_SHOW_DELETE_PLAN, isEnabled(InstanceFrame.ID_DELETE_ROW));
		iframe.enableComponent(InstanceFrame.ID_CONFIGURE, false);
	}

	private boolean isEnabled(String cmdname) {
		java.awt.Component comp = getFrame().getComponentByName(cmdname);
		if (comp == null)
			return true;
		else
			return comp.isEnabled();
	}
}
