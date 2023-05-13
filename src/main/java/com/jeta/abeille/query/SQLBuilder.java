package com.jeta.abeille.query;

import java.sql.SQLException;

import java.util.Collection;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;

/**
 * This class provides the logic to build SQL from a query definition.
 * 
 * @author Jeff Tassin
 */
public interface SQLBuilder {
	/**
	 * Builds the sql command from the given query builder objects
	 * 
	 * @param tables
	 *            the collection of tables that are used for making the joins
	 * @param linkmodel
	 *            the collection of links that join the set of given tables
	 * @param constraints
	 *            a collection of QueryConstraint objects
	 * @param reportbles
	 *            the columns that we wish to report
	 * @param qualified
	 *            if true provide the fully qualified
	 *            catalog/schemat/table/column names, otherwise just provide the
	 *            table names and column names
	 */
	public String build(boolean distinct, Collection qtables, LinkModel qlinkmodel, Collection constraints,
			Collection reportables, Catalog currentCatalog, Schema currentSchema) throws SQLException;

	/**
	 * Builds the where portion of a sql command. Automatically creates the
	 * joins and constraints from the given parameters
	 * 
	 * @param tables
	 *            the collection of tables that are used for making the joins
	 * @param linkmodel
	 *            the collection of links that join the set of given tables
	 * @param constraints
	 *            a collection of QueryConstraint objects
	 * @param reportbles
	 *            the columns that we wish to report
	 * @param qualified
	 *            if true provide the fully qualified
	 *            catalog/schemat/table/column names, otherwise just provide the
	 *            table names and column names
	 */
	public String buildWhere(Collection qtables, LinkModel qlinkmodel, Collection constraints, Collection reportables,
			Catalog currentCatalog, Schema currentSchema);

	/**
	 * @return a collection of Expression objects that correspond to the
	 *         constraints in the build SQL. The order of the expressions is the
	 *         same as the order of the SQL so you can use this are part of a
	 *         prepared statement sequence.
	 */
	public Collection getConstraintExpressions();

}
