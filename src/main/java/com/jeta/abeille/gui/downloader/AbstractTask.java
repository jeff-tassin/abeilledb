package com.jeta.abeille.gui.downloader;

import java.awt.event.ActionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

public abstract class AbstractTask implements Task {
	private String m_input;
	private String m_output;
	private String m_name;
	private String m_message;

	public LinkedList m_listeners = new LinkedList();

	private boolean m_canceled = false;

	public AbstractTask() {

	}

	public AbstractTask(ActionListener listener) {
		addListener(listener);
	}

	public void addListener(ActionListener listener) {
		if (listener != null) {
			m_listeners.add(listener);
		}
	}

	public void addListeners(Collection listeners) {
		m_listeners.addAll(listeners);
	}

	public void cancel() {
		m_canceled = true;
	}

	public String getName() {
		return m_name;
	}

	public String getInput() {
		return m_input;
	}

	public String getOutput() {
		return m_output;
	}

	public Collection getListeners() {
		return m_listeners;
	}

	public String getMessage() {
		return m_message;
	}

	/**
	 * Task implementation
	 */
	public abstract void invoke(HashMap props) throws Exception;

	public boolean isCanceled() {
		return m_canceled;
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setInput(String input) {
		m_input = input;
	}

	public void setOutput(String output) {
		m_output = output;
	}

	public void setMessage(String msg) {
		m_message = msg;
	}

	public void updateListeners(DownloadEvent evt) {

		if (m_listeners.size() == 0)
			return;

		final DownloadEvent fevt = evt;
		Runnable gui_update = new Runnable() {
			public void run() {
				Iterator iter = m_listeners.iterator();
				while (iter.hasNext()) {
					ActionListener listener = (ActionListener) iter.next();
					if (listener != null) {
						listener.actionPerformed(fevt);
					}
				}
			}
		};

		SwingUtilities.invokeLater(gui_update);
	}

	public void updateListeners(Exception e) {
		updateListeners(new DownloadEvent(AbstractTask.this, e));
	}

}
