package com.jeta.abeille.gui.downloader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class TaskGroup extends AbstractTask implements ActionListener {
	private LinkedList m_actions = new LinkedList();

	/**
	 * ctor
	 */
	public TaskGroup() {
	}

	/**
	 * ctor
	 */
	public TaskGroup(ActionListener listener) {
		super(listener);
	}

	public void actionPerformed(ActionEvent evt) {
		Collection listeners = getListeners();
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(evt);
		}
	}

	public void add(Task action) {
		if (action != null) {
			m_actions.add(action);
			if (action instanceof AbstractTask) {
				AbstractTask at = (AbstractTask) action;
				at.addListener(this);
			}
		}
	}

	public void cancel() {
		Iterator iter = m_actions.iterator();
		while (iter.hasNext()) {
			try {
				Task task = (Task) iter.next();
				task.cancel();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * DownloaderAction implementation
	 */
	public void invoke(HashMap props) throws Exception {
		Iterator iter = m_actions.iterator();
		while (iter.hasNext()) {
			Task action = (Task) iter.next();
			if (!isCanceled()) {
				action.invoke(props);
			}
		}
	}

	public int size() {
		return m_actions.size();
	}

}
