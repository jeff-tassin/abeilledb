package com.jeta.plugins.abeille.mysql;

import com.jeta.abeille.database.model.DbObjectType;

import com.jeta.foundation.i18n.I18N;

/**
 * Defines some object types that are specific to MySQL
 * 
 * @author Jeff Tassin
 */
public class MySQLObjectType extends DbObjectType {
	/**
	 * HOST and GLOBAL are not real objects. But, we define them because we need
	 * to get privileges for these items
	 */
	public static MySQLObjectType HOST = new MySQLObjectType("HOST", I18N.getLocalizedMessage("Host"));
	public static MySQLObjectType GLOBAL = new MySQLObjectType("GLOBAL", I18N.getLocalizedMessage("Global"));

	/**
	 * ctor
	 */
	protected MySQLObjectType(String name, String display) {
		super(name, display);
	}

}
