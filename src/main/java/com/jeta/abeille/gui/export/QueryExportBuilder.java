package com.jeta.abeille.gui.export;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.SQLFormatterFactory;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.table.export.ColumnExportSetting;
import com.jeta.foundation.gui.table.export.ColumnHeadingsDecorator;
import com.jeta.foundation.gui.table.export.DecoratorComposite;
import com.jeta.foundation.gui.table.export.DecoratorBuilder;
import com.jeta.foundation.gui.table.export.ExportDecorator;
import com.jeta.foundation.gui.table.export.ExportModel;
import com.jeta.foundation.gui.table.export.ExportUtils;
import com.jeta.foundation.gui.table.export.LiteralDecorator;
import com.jeta.foundation.gui.table.export.ResultsIterator;
import com.jeta.foundation.gui.table.export.RowDecorator;

/**
 * This class builds a decorator used for exporting query results in a standard
 * grid format
 * 
 * @author Jeff Tassin
 */
public class QueryExportBuilder implements DecoratorBuilder {
	/**
	 * The results iterator object that was created the last time the build was
	 * called
	 */
	private ResultsIterator m_resultsiter;

	/**
	 * this is the maximum number of rows that this decorator should output. set
	 * to -1 to output all rows
	 */
	private int m_maxrows;

	/**
	 * Callers can pass a maxRows that specifies the number of rows to export.
	 * Set to -1 for all rows. The default ResultsIterator class is used for
	 * this case.
	 */
	public QueryExportBuilder(int maxRows) {
		// set the maximumn number of rows to export
		m_maxrows = maxRows;
	}

	/**
	 * Callers can pass their own specialized ResultsIterator object. This is
	 * for cases where the ResultsIterator does some type of status update.
	 */
	public QueryExportBuilder(ResultsIterator iter) {
		m_resultsiter = iter;
		m_maxrows = -1;
	}

	/**
	 * Builds a decorator that you can use to export formatted results in a
	 * standard grid format.
	 * 
	 * @param model
	 *            the export model that contains the settings for the export
	 * @return the decorator object that will export the query results
	 */
	public ExportDecorator build(ExportModel emodel) {
		SQLExportModel sqlmodel = (SQLExportModel) emodel;

		TSConnection connection = sqlmodel.getQueryModel().getTSConnection();
		SQLFormatterFactory ffactory = SQLFormatterFactory.getInstance(connection);
		SQLFormatter sqlformatter = ffactory.createFormatter();

		// ColumnExportModel optionsmodel = model.getColumnOptions();

		DecoratorComposite dc = new DecoratorComposite();
		// the output is not transposed - this is the standard behavior
		if (sqlmodel.isShowColumnNames()) {
			ColumnHeadingsDecorator chdec = new ColumnHeadingsDecorator(sqlmodel);
			dc.add(chdec);
			dc.add(new LiteralDecorator(ExportNames.NEWLINE));
		}

		// now handle the decorations for each row
		// now create each column decorator

		int index = 0;
		Collection outputcols = sqlmodel.getIncludedColumns();
		ExportDecorator[] columndecorators = new ExportDecorator[outputcols.size()];
		Iterator iter = outputcols.iterator();
		while (iter.hasNext()) {
			ColumnExportSetting setting = (ColumnExportSetting) iter.next();
			TableColumnDecorator cdec = new TableColumnDecorator((TableColumnExportSetting) setting, sqlmodel,
					sqlformatter);
			columndecorators[index] = cdec;
			index++;
		}

		RowDecorator rowdec = new RowDecorator(sqlmodel, columndecorators);

		DecoratorComposite rowcomposite = new DecoratorComposite();
		LiteralDecorator preline = new LiteralDecorator(ExportUtils.parseLeft(ExportNames.LINE_EXPRESSION,
				sqlmodel.getLineDecorator()));
		LiteralDecorator postline = new LiteralDecorator(ExportUtils.parseRight(ExportNames.LINE_EXPRESSION,
				sqlmodel.getLineDecorator()));
		rowcomposite.add(preline);
		rowcomposite.add(rowdec);
		rowcomposite.add(postline);

		// the ResultsIterator is responsible for doing the actual iterating
		// over the result set
		// once any column headings decorators are done
		if (m_resultsiter == null)
			m_resultsiter = new ResultsIterator(m_maxrows);

		m_resultsiter.add(rowcomposite);
		dc.add(m_resultsiter);
		return dc;
	}

	/**
	 * @return the ResultsIterator object that this builder created the last
	 *         time build was called. This is used because the ResultsIterator
	 *         has the cancel operation and the caller will require the object.
	 */
	public ResultsIterator getResultsIterator() {
		return m_resultsiter;
	}

}
