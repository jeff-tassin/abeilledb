package com.jeta.abeille.gui.sql;

/**
 * This class is used for getting SQL completion events from the SQLMediator
 * 
 * @author Jeff Tassin
 */
public interface SQLMediatorListener {
	/**
	 * Called when an event occurs such as a new line processed, elapsed time
	 * interval, error, or command completed. You typically will get an elapsed
	 * time interval event every 500 millisecs. For multi-line SQL commands, you
	 * can a status message everytime a line is processed
	 */
	public void notifyEvent(SQLMediatorEvent mediatorEvent);
}
