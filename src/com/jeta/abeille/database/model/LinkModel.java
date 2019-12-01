package com.jeta.abeille.database.model;

import java.util.Collection;

/**
 * This class is responsible for managing links between database tables. A link
 * is defined by a primary key - foreign key relationship. These links are
 * created automatically by the database model and are managed here.
 * Furthermore, the user can create 'user-defined' links used for the query
 * tools. This allows the user to automatically join tables using these
 * artifical links.
 * 
 * @author Jeff Tassin
 */
public interface LinkModel extends Cloneable {

	public Object clone();

	/**
	 * @return all links for the given table
	 */
	public Collection getLinks(TableId tableId);

	/**
	 * @return the collection of incoming links (Link objects) for the given
	 *         table. A link is defined by a foreign key for the given table
	 */
	public Collection getInLinks(TableId tableId);

	/**
	 * @return the collection of outgoing links (Link objects) for the given
	 *         table. A link is defined by a table who has a foreign key
	 *         referenced to the given table's primary key
	 */
	public Collection getOutLinks(TableId tableId);

	/**
	 * @return a collection of table ids in this model
	 */
	public Collection getTables();

	/**
	 * Removes all links that directly reference the given table from the model
	 * 
	 * @param tableId
	 *            the id of the table whose in/out links we wish to remove
	 */
	public void removeTable(TableId tableId);

}
