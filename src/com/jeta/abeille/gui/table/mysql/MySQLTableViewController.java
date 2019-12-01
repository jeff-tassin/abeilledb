package com.jeta.abeille.gui.table.mysql;

import com.jeta.foundation.gui.components.TSController;

/**
 * The controller class for the TableView class
 * 
 * @author Jeff Tassin
 */
public class MySQLTableViewController extends TSController {
	/**
	 * ctor
	 */
	public MySQLTableViewController(MySQLTableView view) {
		super(view);
		// for now just set the UIDirector for the view
		view.setUIDirector(new MySQLTableViewUIDirector(view));
	}
}
