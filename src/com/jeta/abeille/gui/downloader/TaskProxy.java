package com.jeta.abeille.gui.downloader;

import java.awt.event.ActionListener;

import java.util.HashMap;

public class TaskProxy extends AbstractTask {
	private String m_prop_name;

	public TaskProxy(String prop_name, ActionListener listener) {
		super(listener);
		m_prop_name = prop_name;
	}

	/**
	 * Task impl
	 */
	public void invoke(HashMap props) throws Exception {
		Object obj = props.get(m_prop_name);
		if (obj instanceof byte[]) {
			AbstractTask task = TaskFactory.parse((byte[]) obj);
			if (!isCanceled()) {

				task.addListeners(getListeners());
				task.invoke(props);
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
}
