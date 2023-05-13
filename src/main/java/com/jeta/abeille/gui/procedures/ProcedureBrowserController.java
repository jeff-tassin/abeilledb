package com.jeta.abeille.gui.procedures;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.gui.common.TableSelectorPanel;

import com.jeta.foundation.gui.components.TSController;

/**
 * The controller class for the ProcedureBrowser window
 * 
 * @author Jeff Tassin
 */
public class ProcedureBrowserController extends TSController {
	/** the frame we are controlling */
	private ProcedureBrowser m_view;

	/**
	 * ctor
	 */
	public ProcedureBrowserController(ProcedureBrowser frame) {
		super(frame);
		m_view = frame;

		JButton btn = (JButton) m_view.getComponentByName(ProcedureBrowser.ID_RELOAD);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				reloadProcedure();
			}
		});
	}

	/**
	 * Reloads the frame based on the currently selected table
	 */
	public void reloadProcedure() {
		ProcedureSelectorPanel pspanel = (ProcedureSelectorPanel) m_view
				.getComponentByName(ProcedureBrowser.ID_PROCEDURE_SELECTOR);
		Collection procs = pspanel.getProcedures();
		m_view.reloadProcedures(procs);
	}

}
