package com.jeta.abeille.database.model;

/**
 * This interface is used to get table meta data for a given table id. Its main
 * purpose is to hide the source of the data model. This is important during
 * modeling where a newly modeled table (that has not been saved) is not part of
 * the DbModel but we still need to see it.
 * 
 * @author Jeff Tassin
 */
public interface DbTableModel {
	/**
	 * @return a table metadata object for the given table name
	 */
	public TableMetaData getTable(TableId tableid);

}
