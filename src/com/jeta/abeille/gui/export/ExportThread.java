package com.jeta.abeille.gui.export;

import java.io.Writer;

import com.jeta.abeille.gui.queryresults.QueryResultsModel;

import com.jeta.foundation.gui.table.export.ExportDecorator;
import com.jeta.foundation.gui.table.export.ExportModel;
import com.jeta.foundation.gui.table.export.ResultsIterator;

/**
 * This class writes exported data to a file in a background thread
 * 
 * @author Jeff Tassin
 */
public class ExportThread implements Runnable {
	/**
	 * This thread sends status information to the mediator. The mediator sends
	 * control information to this thread
	 */
	private ExportMediator m_mediator;

	/**
	 * The model that defines the parameters and data for the export
	 */
	private SQLExportModel m_exportmodel;

	/**
	 * The export decorator that loops over each row. This decorator has a
	 * cancel operation that we can invoke to stop the export
	 */
	private ResultsIterator m_resultsiter;

	/**
	 * The writer object we will write the exported data to.
	 */
	private Writer m_writer;

	/**
	 * ctor
	 */
	public ExportThread(Writer writer, SQLExportModel model, ExportMediator mediator) {
		m_writer = writer;
		m_exportmodel = model;
		m_mediator = mediator;
		m_resultsiter = new ResultsIterator() {
			protected void wroteRow(int row) {
				m_mediator.exportStatus(row);
			}
		};
	}

	/**
	 * Implementation of Runnable
	 */
	public void run() {
		try {
			QueryResultsModel querymodel = m_exportmodel.getQueryModel();
			QueryExportBuilder decbuilder = new QueryExportBuilder(m_resultsiter);
			ExportDecorator decorator = decbuilder.build(m_exportmodel);
			decorator.write(m_writer, querymodel, 0, 0);
			m_writer.flush();
			m_writer.close();
			if (m_resultsiter.isCanceled())
				m_mediator.exportStopped();
			else
				m_mediator.exportCompleted();
		} catch (Exception e) {
			handleError(e);
		}
	}

	/**
	 * Gets called if an exception occurs during the export. This currently
	 * stops the export.
	 */
	private void handleError(Exception e) {
		// @todo we probably need to provide the capability to ignore errors
		// and continue with the export
		m_mediator.exportError(e);
	}

	/**
	 * Stops a currently running export. Invoked by the client.
	 */
	public void stop() {
		m_resultsiter.setCanceled(true);
	}
}
