package com.jeta.foundation.gui.table.export;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class builds a decorator used for exporting selections from a
 * JTable/TSTablePanel
 * 
 * @author Jeff Tassin
 */
public class StandardExportBuilder implements DecoratorBuilder {
	/**
	 * this is the maximum number of rows that this decorator should output. set
	 * to -1 to output all rows
	 */
	private int m_maxrows;

	public StandardExportBuilder(int maxRows) {
		// set the maximumn number of rows to export
		m_maxrows = maxRows;
	}

	/**
	 * Builds a decorator that you can use to export formatted results in a
	 * standard grid format
	 * 
	 * @param model
	 *            the export model that contains the settings for the export
	 * @return the decorator object that will export the query results
	 */
	public ExportDecorator build(ExportModel model) {
		if (model.isTransposed())
			return buildTransposed(model);
		else {
			DecoratorComposite dc = new DecoratorComposite();
			// the output is not transposed - this is the standard behavior
			if (model.isShowColumnNames()) {
				ColumnHeadingsDecorator chdec = new ColumnHeadingsDecorator(model);
				dc.add(chdec);
				dc.add(new LiteralDecorator(ExportNames.NEWLINE));
			}

			// now handle the decorations for each row
			// now create each column decorator

			int index = 0;
			Collection outputcols = model.getIncludedColumns();
			ExportDecorator[] columndecorators = new ExportDecorator[outputcols.size()];
			Iterator iter = outputcols.iterator();
			while (iter.hasNext()) {
				ColumnExportSetting setting = (ColumnExportSetting) iter.next();
				ColumnDecorator cdec = new ColumnDecorator(setting, model);
				columndecorators[index] = cdec;
				index++;
			}

			RowDecorator rowdec = new RowDecorator(model, columndecorators);

			DecoratorComposite rowcomposite = new DecoratorComposite();

			LiteralDecorator preline = new LiteralDecorator(ExportUtils.parseLeft(ExportNames.LINE_EXPRESSION,
					model.getLineDecorator()));
			LiteralDecorator postline = new LiteralDecorator(ExportUtils.parseRight(ExportNames.LINE_EXPRESSION,
					model.getLineDecorator()));
			rowcomposite.add(preline);
			rowcomposite.add(rowdec);
			rowcomposite.add(postline);

			// the ResultsIterator is responsible for doing the actual iterating
			// over the result set
			// once any column headings decorators are done
			ResultsIterator resultsIter = new ResultsIterator(m_maxrows);
			resultsIter.add(rowcomposite);
			dc.add(resultsIter);
			return dc;
		}
	}

	/**
	 * Builds a decorator that you can use to export formatted results in a
	 * standard grid format
	 * 
	 * @param model
	 *            the export model that contains the settings for the export
	 * @return the decorator object that will export the query results
	 */
	public ExportDecorator buildTransposed(ExportModel model) {
		// now handle the decorations for each row
		// now create each column decorator
		ExportDecorator[] columndecorators = new ExportDecorator[model.getColumnCount()];
		for (int index = 0; index < model.getColumnCount(); index++) {
			ColumnExportSetting setting = model.getColumnExportSetting(index);
			ColumnDecorator cdec = new ColumnDecorator(setting, model);
			columndecorators[index] = cdec;
		}

		TransposeIterator titer = new TransposeIterator(m_maxrows, new ColumnHeadingsDecorator(model),
				columndecorators, model);
		return titer;
	}
}
