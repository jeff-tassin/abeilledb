package com.jeta.abeille.database.model;

import java.util.LinkedList;

/**
 * This class defines the privileges for a given user/group for a given database
 * object. Note that the actual privilege definitions are vendor specific and
 * provided by the TSDatabase implementation class
 * 
 * @author Jeff Tassin
 */
public class TSPrivileges {
	public static final String COMPONENT_ID = "jeta.TSUserPrivileges";

	public String m_username; // the name of the user/group
	public LinkedList m_privileges; // a list of privileges (as defined by the
									// the TSUserPrivileges implementation class
									// - vendor specific )
	public TSDatabaseObject m_type; // the type of the object the privileges
									// refer to
	public String m_objectname; // the name of the object the privileges refer
								// to

	public TSPrivileges() {
		m_username = "";
		m_privileges = new LinkedList();
		m_objectname = "";
		m_type = null;
	}
}
