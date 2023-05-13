package com.jeta.abeille.gui.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.query.Reportable;

import com.jeta.open.rules.AbstractRule;
import com.jeta.open.rules.RuleResult;

import com.jeta.foundation.i18n.I18N;

/**
 * Rule that checks the query builder model to make sure it is valid.
 * 
 * @author Jeff Tassin
 */
public class QueryValidatorRule extends AbstractRule {

	/**
	 * JETARule implementation. This rule requires that you pass parameter array
	 * with the zero element being a QueryModel instance
	 */
	public RuleResult check(Object[] params) {
		QueryModel model = (QueryModel) params[0];
		Collection tables = model.getSpecifiedTables();

		TreeSet table_hash = new TreeSet(tables);

		Collection constraints = model.getConstraints();
		Collection reportables = model.getReportables();

		if (tables.size() == 0 || reportables.size() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Query needs at least one table and one reportable"));
		}

		// first, lets make sure that all tables in the reportables are includes
		// in the specified tables
		Iterator iter = reportables.iterator();
		while (iter.hasNext()) {
			Reportable rep = (Reportable) iter.next();
			if (!table_hash.contains(rep.getTableId())) {
				return new RuleResult(I18N.format("query_reportable_invalid_table_1", rep.getTableId()));
			}
		}

		// second, make sure that all tables in the contraints are specified
		iter = constraints.iterator();
		while (iter.hasNext()) {
			QueryConstraint qc = (QueryConstraint) iter.next();
			Collection ctables = qc.getTables();
			Iterator citer = ctables.iterator();
			while (citer.hasNext()) {
				TableId id = (TableId) citer.next();
				assert (id != null);
				if (!table_hash.contains(id)) {
					qc.print();
					return new RuleResult(I18N.format("query_constraint_invalid_table_1", id));
				}
			}
		}

		return RuleResult.SUCCESS;
	}

}
