package com.jeta.abeille.gui.security;

import java.util.Collection;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.security.User;

import com.jeta.abeille.gui.security.postgres.PostgresUserView;
import com.jeta.abeille.gui.security.postgres.PostgresUserViewController;
import com.jeta.abeille.gui.security.postgres.PostgresViewBuilder;

import com.jeta.abeille.gui.security.mysql.MySQLUserView;
import com.jeta.abeille.gui.security.mysql.MySQLViewBuilder;

import com.jeta.abeille.gui.security.hsqldb.HSQLUserView;
import com.jeta.abeille.gui.security.hsqldb.HSQLViewBuilder;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.open.rules.RuleGroup;

import com.jeta.plugins.abeille.mysql.MySQLUser;

/**
 * Builder class for various views/objects in this package
 * 
 * @author Jeff Tassin
 */
public class SecurityBuilder {
	/** the database connection */
	private TSConnection m_connection;

	private SecurityBuilder() {
	}

	private SecurityBuilder(TSConnection conn) {
		m_connection = conn;
	}

	/**
	 * factory
	 */
	public static SecurityBuilder createInstance(TSConnection conn) {
		return new SecurityBuilder(conn);
	}

	/**
	 * Creates the view for creating/editing a user
	 */
	public UserView createUserView(TSDialog dlg, User currentUser, Collection groups) {
		Database db = m_connection.getDatabase();
		if (db == Database.POSTGRESQL) {
			PostgresUserView view = new PostgresUserView(m_connection, currentUser, groups);
			PostgresUserViewController controller = new PostgresUserViewController(view);
			view.setController(controller);
			RuleGroup rule = new RuleGroup(view);
			rule.add(new UserViewValidator());
			rule.add(controller);
			dlg.addValidator(controller);
			dlg.addValidator(view, rule);

			return view;
		} else if (db == Database.MYSQL) {
			MySQLUserView view = new MySQLUserView(m_connection, (MySQLUser) currentUser);
			RuleGroup rule = new RuleGroup(view);
			rule.add(new UserViewValidator());
			dlg.addValidator(view, rule);
			return view;
		} else {
			assert (false);
			return null;
		}
	}

	/**
	 * Creates the views for the main frame window
	 */
	public void buildFrame(SecurityMgrFrame frame) {
		Database db = m_connection.getDatabase();
		if (db == Database.POSTGRESQL) {
			PostgresViewBuilder.buildViews(frame);
		} else if (db == Database.MYSQL) {
			MySQLViewBuilder.buildViews(frame);
		} else if (db == Database.HSQLDB) {
			HSQLViewBuilder.buildViews(frame);
		}
	}

}
