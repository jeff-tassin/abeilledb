package com.jeta.abeille.gui.downloader;

import java.util.HashMap;

public interface Task {

	public void cancel() throws Exception;

	/**
	 * Invokes the task
	 * 
	 * @param props
	 *            a map of parameters to pass to the task. Each task defines its
	 *            own parameters.
	 */
	public void invoke(HashMap props) throws Exception;
}
