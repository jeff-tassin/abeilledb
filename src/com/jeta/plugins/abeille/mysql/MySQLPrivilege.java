package com.jeta.plugins.abeille.mysql;

import java.util.Collection;
import java.util.LinkedList;

import com.jeta.abeille.database.security.Privilege;

/**
 * Defines a privilege in the database. This is used in GRANT/REVOKE statements
 * 
 * @author Jeff Tassin
 */
public class MySQLPrivilege extends Privilege {
	public static final Privilege FILE = new MySQLPrivilege("FILE", 0x8000);
	public static final Privilege INDEX = new MySQLPrivilege("INDEX", 0x10000);
	public static final Privilege LOCK_TABLES = new MySQLPrivilege("LOCK TABLES", 0x20000);
	public static final Privilege PROCESS = new MySQLPrivilege("PROCESS", 0x80000);
	public static final Privilege RELOAD = new MySQLPrivilege("RELOAD", 0x100000);
	public static final Privilege REPLICATION_CLIENT = new MySQLPrivilege("REPLICATION CLIENT", 0x200000);
	public static final Privilege REPLICATION_SLAVE = new MySQLPrivilege("REPLICATION SLAVE", 0x400000);
	public static final Privilege SHOW_DATABASES = new MySQLPrivilege("SHOW DATABASES", 0x800000);
	public static final Privilege SHUTDOWN = new MySQLPrivilege("SHUTDOWN", 0x1000000);
	public static final Privilege SUPER = new MySQLPrivilege("SUPER", 0x2000000);
	public static final Privilege CREATE_TEMPORARY_TABLES = new MySQLPrivilege("CREATE TEMPORARY TABLES", 0x4000000);
	public static final Privilege GLOBAL = new MySQLPrivilege("GLOBAL", 0x8000000);

	/**
	 * ctor
	 */
	private MySQLPrivilege(String name, int mask) {
		super(name, mask);
	}
}
