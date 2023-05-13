package com.jeta.abeille.gui.model.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ViewGetter;

import com.jeta.foundation.gui.print.PrintPreviewDialog;
import com.jeta.foundation.gui.print.PrintUtils;

import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * Invokes the print preview for a ModelView
 * 
 * @author Jeff Tassin
 */
public class PrintPreviewAction implements ActionListener {
	private ViewGetter m_viewgetter;

	/**
	 * ctor
	 */
	public PrintPreviewAction(ViewGetter viewgetter) {
		m_viewgetter = viewgetter;
	}

	public void actionPerformed(ActionEvent evt) {
		ModelView view = m_viewgetter.getModelView();
		view.deselectAll();
		PrintPreviewDialog dlg = (PrintPreviewDialog) TSGuiToolbox.createDialog(PrintPreviewDialog.class,
				m_viewgetter.getModelView(), true);
		dlg.setPrintable(view);
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
		dlg.showCenter();
	}
}
