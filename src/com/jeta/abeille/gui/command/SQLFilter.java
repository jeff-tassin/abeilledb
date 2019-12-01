package com.jeta.abeille.gui.command;

/**
 * This interface is used to trap SQL commands in the SQLCommand class. Its main
 * purpose is to allow the user to preview SQL instead of sending it to the
 * database.
 * 
 * @author Jeff Tassin
 */
public interface SQLFilter {
	public void sqlCommand(String sql);
}
