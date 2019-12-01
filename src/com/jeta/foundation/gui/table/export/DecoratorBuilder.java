package com.jeta.foundation.gui.table.export;

/**
 * This class defines the interface for a decorator builder. This type of
 * builder is responsible for constructing decorator classes that know how to
 * output query results.
 * 
 * @author Jeff Tassin
 */
public interface DecoratorBuilder {
	/**
	 * Builds a decorator that you can use to export formatted results.
	 * Specialized classes need to override this to provide differnt formatters.
	 * 
	 * @param model
	 *            the export model that contains the settings for the export
	 * @return the decorator object that will export the query results
	 */
	public ExportDecorator build(ExportModel model);
}
