package com.jeta.abeille.database.model;

import java.sql.*;

/**
 * This interface represents a result set in the system. It defines are few more
 * methods that are useful for the application. Various database vendors will
 * have probably have implementations of this interface
 * 
 * @author Jeff Tassin
 */
public interface TSResultSet extends ResultSet {
	/**
	 * @return the current position in the result set
	 */
	public int getPosition();

	/**
	 * @return the size of the result set. If the entire result set has not been
	 *         retrieved, -1 will be returned because the size is unknown at
	 *         that point
	 */
	public int getSize();

	/**
	 * @return true if this result set is empty
	 */
	public boolean isEmpty() throws SQLException;

}
