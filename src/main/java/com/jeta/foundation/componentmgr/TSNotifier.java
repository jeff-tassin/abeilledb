/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.componentmgr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * This is the main notifier class for the VM. Objects can register with an
 * instance of a notifier. Other objects can send messages to the notifier. The
 * notifier will then forward the messages to those objects that registered an
 * interest. There can be multiple notifiers in the system. You distinguish
 * between them by a notifier ID string. These IDs should be well known to the
 * application. For example: TSNotifer notifer = TSNotifer.getInstance(
 * currentdatabasename ); notifier.fireTSEvent(...); (this would fire an event
 * to all listeners who are interested in messages that pertain to the given
 * database connection. the application might have multiple database connections
 * opened at once, so we need a way to differentiate)
 * 
 * @author Jeff Tassin
 */
public class TSNotifier {
	// @todo keep references to listeners as WeakReferences
	private HashMap<String,LinkedList<TSListener>> m_listeners = new HashMap();

	/**
	 * The identifer for this notifier instance
	 */
	private String m_notifierId;

	/**
	 * Map of notifier ids(key) to notifier instances(values)
	 */
	private static HashMap m_notifiers = new HashMap();

	private static final String COMPONENT_ID = "tsnotifier.component.id";

	/**
	 * ctor
	 */
	private TSNotifier(String notifierId) {

	}

	/**
	 * Gets the application notifier instances for a given id. If the instance
	 * is not found, a new one is instatiated
	 */
	public static TSNotifier getInstance() {
		return getInstance(COMPONENT_ID);
	}

	/**
	 * Gets the notifier instances for a given id. If the instance is not found,
	 * a new one is instatiated
	 */
	synchronized public static TSNotifier getInstance(String notifierId) {
		TSNotifier notifier = (TSNotifier) m_notifiers.get(notifierId);
		if (notifier == null) {
			notifier = new TSNotifier(notifierId);
			m_notifiers.put(notifierId, notifier);
		}
		return notifier;
	}

	/**
	 * Sends a TSEvent to all objects that are interested in the given
	 * messageGroup.
	 * 
	 * @param evt
	 *            the evt to send
	 */
	synchronized public void fireEvent(TSEvent evt) {
		LinkedList<TSListener> list = m_listeners.get(evt.getGroup());
		if (list != null) {
			Iterator<TSListener> iter = list.iterator();
			while (iter.hasNext()) {
				TSListener listener = iter.next();
				if (listener == null) // listener was garbage collected
				{
					// System.out.println(
					// "TSNotify.fireEvent  listener was garbage collected: " +
					// evt.getValue() );
					iter.remove();
				} else {
					// System.out.println( "TSNotify.fireEvent  listener ok: " +
					// listener.getClass() );
					listener.tsNotify(evt);
				}
			}
		}
	}

	/**
	 * Sends a TSEvent to all objects that are interested in the given
	 * messageGroup.
	 * 
	 * @param sender
	 *            the sender of the event
	 * @param messageGroup
	 *            the message group for the event. This is the main filter we
	 *            use for routing events.
	 * @param msgType
	 *            the message type. Listeners use this to further filter the
	 *            message.
	 * @param msgValue
	 *            the actual message value.
	 * 
	 */
	synchronized public void fireEvent(Object sender, String msgGroup, String msgType, Object msgValue) {
		TSEvent evt = new TSEvent(sender, msgGroup, msgType, msgValue);
		fireEvent(evt);
	}

	/**
	 * Registers a listener to receive events for a given messageGroup
	 * 
	 * @param listener the object to receive the events
	 * @param messageGroup the message group
	 */
	synchronized public void registerInterest(TSListener listener, String messageGroup) {
		// System.out.println( "TSNotifier.registerInterest: " + listener  + " messageGroup: " + messageGroup);
		LinkedList<TSListener> list = m_listeners.computeIfAbsent(messageGroup, k -> new LinkedList<>());
		list.add(listener);
	}

	/**
	 * Removes a listener from this notifier (for all messages)
	 * 
	 * @param listener
	 *            the object to remove
	 */
	synchronized public void removeListener(TSListener listener) {
		Iterator<String> iter = m_listeners.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			LinkedList<TSListener> list = m_listeners.get(key);
			if (list != null) {
				Iterator<TSListener> liter = list.iterator();
				while (liter.hasNext()) {
					if ( listener == liter.next() ) {
						iter.remove();
					}
				}
			}
		}
	}

}
