/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.componentmgr;

/**
 * This is the event object used to encapsulate an event fired by the
 * TSNotifier.
 * 
 * @author Jeff Tassin
 */
public class TSEvent {

	public Object m_sender; // this is the message sender
	public String m_group; // this is the group that the message belongs to
							// (i.e. model, userproperties, etc)
	public String m_msg; // this is the message (e.g. delete table)
	public Object m_value; // this is the value of the message

	public TSEvent(Object sender, String mgroup, String msg, Object mvalue) {
		m_sender = sender;
		m_group = mgroup;
		m_msg = msg;
		m_value = mvalue;
	}

	public Object getSender() {
		return m_sender;
	}

	public String getGroup() {
		return m_group;
	}

	public String getMessage() {
		return m_msg;
	}

	public Object getValue() {
		return m_value;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("TSEvent: ");
		buff.append(m_group);
		buff.append("   ");
		buff.append(m_msg);
		buff.append("   ");
		buff.append(m_value);
		return buff.toString();
	}

}
