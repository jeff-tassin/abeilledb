package com.jeta.abeille.gui.rules.postgres;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

import com.jeta.foundation.utils.TSUtils;

/**
 * @author Jeff Tassin
 */
public class RuleViewFactory implements JETARule {

	private TSPanel m_delegate;

	/**
	 * ctor for creating a new view
	 */
	public RuleViewFactory(TSConnection conn, Rule rule, TableId tableId) {
		if (!parseRule(conn, rule, tableId))
			m_delegate = new BasicRuleView(conn, rule);
	}

	public Rule createRule() {
		if (m_delegate instanceof BasicRuleView) {
			return ((BasicRuleView) m_delegate).createRule();
		} else {
			return ((AdvancedRuleView) m_delegate).createRule();
		}
	}

	public TSPanel getView() {
		return m_delegate;
	}

	private boolean parseRule(TSConnection conn, Rule rule, TableId tableId) {
		boolean bresult = false;
		try {
			String expr = rule.getExpression();
			/**
			 * CREATE [ OR REPLACE ] RULE name AS ON event TO table [ WHERE
			 * condition ] DO [ INSTEAD ] action
			 */

			int evtpos1 = expr.indexOf(" AS ON ");
			int evtpos2 = expr.indexOf(" TO ");

			String event = null;
			String where = null;
			String doaction = null;
			boolean instead = false;

			if (evtpos1 > 0 && expr.lastIndexOf(" AS ON ") == evtpos1 && evtpos1 < evtpos2) {
				// ok so far
				event = expr.substring(evtpos1 + " AS ON ".length(), evtpos2);

				int dopos1 = expr.indexOf(" DO ", evtpos2);
				if (dopos1 > 0 && expr.lastIndexOf(" DO ") == dopos1) {
					// WHERE is optional
					int wpos1 = expr.indexOf(" WHERE ", evtpos2);
					if (wpos1 > 0 && wpos1 < dopos1) {
						where = expr.substring(wpos1 + " WHERE ".length(), dopos1);
					}

					if (wpos1 < 0 || where != null) {
						int instd1 = expr.indexOf("INSTEAD ", dopos1);
						if (instd1 > 0 && expr.lastIndexOf("INSTEAD ") == instd1) {
							instead = true;
							doaction = expr.substring(instd1 + "INSTEAD ".length(), expr.length());
						} else {
							doaction = expr.substring(dopos1 + " DO ".length(), expr.length());
						}

						AdvancedRuleView advview = new AdvancedRuleView(conn, tableId);
						advview.setName(rule.getName());
						advview.setEvent(event);
						advview.setWhere(where);
						advview.setInstead(instead);
						advview.setDoAction(doaction);

						m_delegate = advview;
						bresult = true;
					}
				} else {
					TSUtils.printDebugMessage("RuleViewFactory.parseRule failed  DO index found twice");
				}
			} else {
				TSUtils.printDebugMessage("RuleViewFactory.parseRule failed  AS ON index found twice");
			}

		} catch (Exception e) {
			TSUtils.printException(e);
			bresult = false;
		}
		return bresult;
	}

	/**
	 * Validate the information in the view
	 */
	public RuleResult check(Object[] params) {
		if (m_delegate instanceof BasicRuleView) {
			return ((BasicRuleView) m_delegate).check(params);
		} else {
			return ((AdvancedRuleView) m_delegate).check(params);
		}
	}
}
