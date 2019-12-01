package com.jeta.plugins.abeille.pointbase.gui.table;

import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.table.generic.GenericTableView;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.pointbase.gui.constraints.PointBaseConstraintsModel;
import com.jeta.plugins.abeille.pointbase.gui.constraints.PointBaseConstraintsView;

import com.jeta.plugins.abeille.pointbase.gui.triggers.PointBaseTriggersModel;
import com.jeta.plugins.abeille.pointbase.gui.triggers.PointBaseTriggersView;

/**
 * This view displays all the properties for a PointBase table in a JTabbedPane
 * 
 * @author Jeff Tassin
 */
public class PointBaseTableView extends GenericTableView {
	/** model for check constraints for the selected table */
	private PointBaseConstraintsModel m_checksmodel;

	/** model for triggers for the selected table */
	private PointBaseTriggersModel m_triggersmodel;

	/**
	 * ctor
	 */
	public PointBaseTableView(TSConnection conn) {
		super(conn);
		insertView(I18N.getLocalizedMessage("Constraints"), null, createConstraintsView(), 5);
		// insertView( I18N.getLocalizedMessage( "Triggers" ), null,
		// createTriggersView(), 6 );
	}

	/**
	 * Creates the view that displays the checks for this table
	 */
	private TSPanel createConstraintsView() {
		m_checksmodel = new PointBaseConstraintsModel(getConnection(), null);
		PointBaseConstraintsView view = new PointBaseConstraintsView(m_checksmodel);
		return view;
	}

	/**
	 * Creates the view that displays the triggers for this table
	 */
	private TSPanel createTriggersView() {
		m_triggersmodel = new PointBaseTriggersModel(getConnection(), null);
		PointBaseTriggersView view = new PointBaseTriggersView(m_triggersmodel);
		return view;
	}

	/**
	 * Sets the current table id for the view. All tabs are updated to display
	 * the properties for the given table
	 */
	public void setTableId(TableId tableId) {
		super.setTableId(tableId);
		try {
			m_checksmodel.setTableId(tableId);
			// m_triggersmodel.setTableId( tableId );
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

}
