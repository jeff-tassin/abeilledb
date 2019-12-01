package com.jeta.abeille.gui.model.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ViewGetter;

import com.jeta.foundation.gui.print.PrintUtils;
import com.jeta.foundation.utils.TSUtils;

/**
 * Invokes the print dialog and prints a ModelView
 * 
 * @author Jeff Tassin
 */
public class PrintAction implements ActionListener {
	private ViewGetter m_viewgetter;

	/**
	 * ctor
	 */
	public PrintAction(ViewGetter viewgetter) {
		m_viewgetter = viewgetter;
	}

	public void actionPerformed(ActionEvent evt) {
		try {
			ModelView view = m_viewgetter.getModelView();
			view.deselectAll();
			java.awt.print.PrinterJob prnJob = PrintUtils.showPrintDialog(view);
			if (prnJob != null) {
				prnJob.print(PrintUtils.getPrintSettings().getAttributes());
			}
		} catch (java.awt.print.PrinterException ex) {
			TSUtils.printException(ex);
		}
	}
}
