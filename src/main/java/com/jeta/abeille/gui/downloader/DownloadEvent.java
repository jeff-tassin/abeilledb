package com.jeta.abeille.gui.downloader;

import java.awt.event.ActionEvent;

public class DownloadEvent extends ActionEvent {

	/** set if an exception occurs */
	private Exception m_error;

	/** the number of bytes downloaded so far */
	private int m_current_total;

	/** the total length of the download */
	private int m_content_len;

	public static final int COMPLETED = 1;
	public static final int STATUS = 2;
	public static final int ERROR = 3;

	public DownloadEvent(DownloadEvent evt) {
		super(evt.getSource(), evt.getID(), "");
		m_error = evt.m_error;
		m_current_total = evt.m_current_total;
		m_content_len = evt.m_content_len;
	}

	public DownloadEvent(DownloadEvent evt, Task source) {
		this(evt);
		setSource(source);
	}

	public DownloadEvent(Task source, int id) {
		super(source, id, "");
		setSource(source);
	}

	public DownloadEvent(Task source, Exception e) {
		super(source, ERROR, "");
		m_error = e;
	}

	public DownloadEvent(Task source, int total, int content_len) {
		super(source, STATUS, "");
		m_current_total = total;
		m_content_len = content_len;
	}

	public int getCurrentTotal() {
		return m_current_total;
	}

	public int getContentLength() {
		return m_content_len;
	}

	public String getMessage() {
		String msg = "";
		Object src = getSource();
		if (src instanceof AbstractTask) {
			AbstractTask task = (AbstractTask) src;
			msg = task.getMessage();
		}
		return msg;
	}

	public Exception getException() {
		return m_error;
	}

	public void print() {

		if (getID() == COMPLETED) {
			System.out.println("id: completed");
		} else if (getID() == ERROR) {
			System.out.println("id: error");
			System.out.println("  " + m_error.getMessage());
		} else if (getID() == STATUS) {
			System.out.println("id: status");
			System.out.println("  current_total = " + m_current_total + "   content_len = " + m_content_len);
		} else {
			System.out.println("Uknown download event code!");
		}

		if (getSource() == null)
			System.out.println("src: null");
		else
			System.out.println("src: " + getSource().getClass());

	}

	public void setStatus(int current_total, int content_len) {
		m_current_total = current_total;
		m_content_len = content_len;
	}

}
