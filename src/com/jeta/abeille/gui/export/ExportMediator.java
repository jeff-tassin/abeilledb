package com.jeta.abeille.gui.export;

import java.io.Writer;

import com.jeta.foundation.gui.table.export.ExportModel;

/**
 * This class is a mediator between the export thread and the GUI. It decouples
 * the thread and the GUI.
 * 
 * @author Jeff Tassin.
 */
public class ExportMediator {
	/**
	 * The gui controller for the export window
	 */
	private ExportFrameController m_controller;

	/**
	 * The model that defines the export parameters and data
	 */
	private SQLExportModel m_model;

	/**
	 * The thread that runs the export
	 */
	private ExportThread m_exportthread;

	/**
	 * The thread object that is the actual thread
	 */
	private Thread m_thread;

	/**
	 * ctor
	 */
	public ExportMediator(ExportFrameController controller, Writer writer, SQLExportModel model) {
		m_controller = controller;
		m_model = model;
		m_exportthread = new ExportThread(writer, model, this);
		m_thread = new Thread(m_exportthread);
		m_thread.start();
		m_model.setExporting(true);
	}

	/**
	 * Called when the export has been completed.
	 */
	void exportCompleted() {
		m_model.setExporting(false);
		m_controller.exportCompleted();
	}

	/**
	 * Called if an exception is thrown during the export. Sends the message
	 * back to the gui.
	 */
	void exportError(Exception e) {
		m_model.setExporting(false);
		m_controller.exportError(e);
	}

	/**
	 * Stops an active export
	 */
	void stopExport() {
		m_exportthread.stop();
	}

	/**
	 * Updates the status of the export.
	 */
	void exportStatus(int row) {
		m_controller.exportStatus(row);
	}

	/**
	 * Export was stopped by the user
	 */
	void exportStopped() {
		m_model.setExporting(false);
		m_controller.exportStopped();
	}
}
