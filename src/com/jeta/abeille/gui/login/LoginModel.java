package com.jeta.abeille.gui.login;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.gui.store.LoginInfo;
import com.jeta.abeille.gui.store.UserInfo;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * The data model for the LoginView
 * 
 * @author Jeff Tassin
 */
public class LoginModel {

	/** the connection mgr */
	private transient ConnectionMgr m_connectionmgr;

	/** user entered data that we store for the next login */
	private UserInfo m_userdata = new UserInfo();

	private static final String USER_DATA_ID = "jeta.abeille.gui.login.loginmodel.userdata";

	/**
	 * ctor
	 */
	public LoginModel(ConnectionMgr mgr) {
		m_connectionmgr = mgr;

		ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
		try {
			UserInfo userdata = (UserInfo) os.load(USER_DATA_ID);
			if (userdata != null)
				m_userdata = userdata;
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}

	}

	/**
	 * @return the set of defined connections (ConnectionInfo) objects
	 */
	public Collection getConnections() {
		return m_connectionmgr.getDefinedConnections();
	}

	/**
	 * @return the last connection that was used
	 */
	public ConnectionInfo getLastConnection() {
		if (m_userdata.size() > 0) {
			LoginInfo info = (LoginInfo) m_userdata.getFirst();
			return m_connectionmgr.getConnection(info.getUID());
		} else
			return null;
	}

	/**
	 * @return the last user logged into the given connection
	 */
	public String getLastUser(ConnectionInfo info) {
		String username = null;
		LoginInfo logininfo = m_userdata.get(info.getUID());
		if (logininfo != null) {
			username = logininfo.getUser();
		} else {

		}
		return username;
	}

	/**
	 * Saves the model preferences to the object store
	 */
	public void save() {
		ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
		try {
			os.store(USER_DATA_ID, m_userdata);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Sets the last connection
	 */
	public void setLastConnection(ConnectionInfo info) {
		/*
		 * if ( !m_userdata.remove( info.getUID() ) ) { System.out.println(
		 * ">>>>>>>>>>>>>>>> LoginModel.setLastConnection  info not found in user prefs"
		 * ); } if ( m_userdata.size() > MAX_USER_INFO_SIZE )
		 * m_userdata.removeLast();
		 */

		if (info != null) {
			m_userdata.add(new LoginInfo(info.getUID(), null));
		}
	}

	/**
	 * Sets the last user for the given connection
	 */
	public void setLastUser(ConnectionInfo info, String userName) {
		LoginInfo logininfo = m_userdata.get(info.getUID());
		logininfo.setUser(userName);

	}

}
