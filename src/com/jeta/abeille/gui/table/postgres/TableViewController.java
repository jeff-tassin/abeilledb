package com.jeta.abeille.gui.table.postgres;

import com.jeta.foundation.gui.components.TSController;

/**
 * The controller class for the TableView class
 * 
 * @author Jeff Tassin
 */
public class TableViewController extends TSController {
	/**
	 * ctor
	 */
	public TableViewController(PostgresTableView view) {
		super(view);
		// for now just set the UIDirector for the view
		view.setUIDirector(new TableViewUIDirector(view));
	}
}
