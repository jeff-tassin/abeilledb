package com.jeta.abeille.logger;

import java.io.OutputStream;

import java.util.logging.Logger;

public class SystemConsoleLogger extends OutputStream {
	private Logger m_logger = null;

	public SystemConsoleLogger() {
		m_logger = Logger.getLogger(com.jeta.foundation.componentmgr.ComponentNames.APPLICATION_LOGGER);
	}

	public void write(byte[] b) {
		m_logger.config(new String(b));
	}

	public void write(byte[] b, int off, int len) {
		m_logger.config(new String(b, off, len));
	}

	public void write(int b) {

	}
}
