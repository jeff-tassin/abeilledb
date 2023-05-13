package com.jeta.abeille.gui.utils;

import java.sql.SQLException;

/**
 * This interface defines a listener for the SQLCommandDialog. A caller can set
 * the listener on the dialog. This lister will be called when the user presses
 * Ok. The dialog can then provide handling for the dialog in response to the Ok
 * button. This SQLDialog will automatically provide handling for the exception.
 * 
 * @author Jeff Tassin
 */
public interface SQLDialogListener {
	/**
	 * this method gets called when the user presses Ok. If false is returned,
	 * the dialog remains on the screen. This indicates an error condition due
	 * to some user input values. If true is returned, the dialog is closed
	 * normally.
	 */
	public boolean cmdOk() throws SQLException;
}
