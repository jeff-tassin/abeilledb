package com.jeta.abeille.query;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Represents a query constraint within parentheses node = ( constraint1 AND
 * constraint2 ) The simple case is a node with a single constraint
 * 
 * @author Jeff Tassin
 */
public class ConstraintNode implements JETAExternalizable {
	static final long serialVersionUID = -8849599536778767208L;

	public static int VERSION = 1;

	/**
	 * A linked list of constraint nodes and conjunctions i.e. node1 AND node2
	 * OR node3 where nodeN can be broken down into further nodes therefore, the
	 * list must be as follows ConstraintNode, LogicalConnective,
	 * ConstraintNode, LogicalConnective, ConstraintNode, etc
	 */
	private LinkedList m_expressionList = new LinkedList();

	/**
	 * ctor
	 */
	public ConstraintNode() {

	}

	/**
	 * ctor Simple constraint with one expression
	 */
	public ConstraintNode(Expression expr) {
		addNode(expr, null);
	}

	/**
	 * ctor Simple constraint with one expression
	 */
	public ConstraintNode(TableId tableId, String colName, Operator op, String value) {
		Expression e = new Expression(tableId, colName, op, value);
		addNode(e, null);
	}

	/**
	 * Adds a linked list of expressions Expression, LogicalConnective,
	 * Expression,...
	 */
	public void addExpressions(LinkedList expressions) {
		m_expressionList.addAll(expressions);
		m_expressionList.add(null);
	}

	/**
	 * Adds a node to the expression list. The nextConnective (i.e. AND, OR ) is
	 * relative to the next node added. If this is the last node, then
	 * nextConnective should be null. It is ignored anyway. Node should either
	 * be a ConstraintNode or a JConstraint
	 */
	private void addNode(Object node, LogicalConnective nextConnective) {
		if (node instanceof ConstraintNode) {
			m_expressionList.add(node);
			if (nextConnective != null)
				m_expressionList.add(nextConnective);
		} else if (node instanceof Expression) {
			m_expressionList.add(node);
			if (nextConnective != null)
				m_expressionList.add(nextConnective);
		} else
			assert (false);
	}

	/**
	 * Adds a node to the expression list. The nextConnective (i.e. AND, OR ) is
	 * relative to the previous node added. If this is the first node, then
	 * nextConnective should be null. Node should either be a ConstraintNode or
	 * a JConstraint
	 */
	public void addNode(LogicalConnective prevConnective, Object node) {
		if (node instanceof ConstraintNode) {
			if (prevConnective != null)
				m_expressionList.add(prevConnective);
			m_expressionList.add(node);
		} else if (node instanceof Expression) {
			if (prevConnective != null)
				m_expressionList.add(prevConnective);
			m_expressionList.add(node);
		} else
			assert (false);
	}

	/**
	 * @return true if this node contains an expression that has a column in the
	 *         given table
	 */
	public boolean contains(TableId tableid) {
		boolean bresult = false;
		Iterator iter = m_expressionList.listIterator();
		while (iter.hasNext()) {
			Object node = iter.next();
			if (node instanceof ConstraintNode) {
				ConstraintNode cnode = (ConstraintNode) node;
				if (cnode.contains(tableid)) {
					bresult = true;
					break;
				}
			} else if (node instanceof Expression) {
				Expression expression = (Expression) node;
				if (tableid.equals(expression.getTableId())) {
					bresult = true;
					break;
				}
			}
		}
		return bresult;
	}

	/**
	 * @return a collection of expression (Expression objects) in the same order
	 *         as produced by the toSQL call.
	 */
	public Collection getExpressions() {
		LinkedList result = new LinkedList();

		Iterator iter = m_expressionList.listIterator();
		while (iter.hasNext()) {
			Object node = iter.next();
			if (node instanceof ConstraintNode) {
				ConstraintNode cnode = (ConstraintNode) node;
				result.addAll(cnode.getExpressions());
			} else if (node instanceof Expression) {
				result.add((Expression) node);
			}
		}
		return result;
	}

	/**
	 * @return all tables (TableId) objects that make up this node
	 */
	public Collection getTables() {
		LinkedList ll = new LinkedList();
		// StringBuffer sql = new StringBuffer();
		// sql.append( "( " );
		Iterator iter = m_expressionList.listIterator();
		while (iter.hasNext()) {
			Object node = iter.next();
			if (node instanceof ConstraintNode) {
				ConstraintNode cnode = (ConstraintNode) node;
				ll.addAll(cnode.getTables());
			} else if (node instanceof Expression) {
				Expression expression = (Expression) node;
				ll.add(expression.getTableId());
			}
		}
		return ll;
	}

	/**
	 * This is a helper method that iterates over all expressions in the
	 * constraint and sets the value to ? so the constraint can be used in a
	 * prepared statement.
	 */
	void prepareConstraint() {
		Iterator iter = m_expressionList.listIterator();
		while (iter.hasNext()) {
			Object node = iter.next();
			if (node instanceof ConstraintNode) {
				ConstraintNode cnode = (ConstraintNode) node;
				cnode.prepareConstraint();
			} else if (node instanceof Expression) {
				Expression expr = (Expression) node;
				expr.setValue("?");
			}
		}
	}

	/**
	 * Prints out contents to System.out
	 */
	public void print() {
		Iterator iter = m_expressionList.listIterator();
		while (iter.hasNext()) {
			Object node = iter.next();
			if (node instanceof ConstraintNode) {
				ConstraintNode cnode = (ConstraintNode) node;
				cnode.print();
			} else if (node instanceof Expression) {
				Expression expr = (Expression) node;
				expr.print();
			} else if (node instanceof LogicalConnective) {
				LogicalConnective lc = (LogicalConnective) node;
				System.out.println("Connective = " + lc.toString());
			}
		}
	}

	/**
	 * @return the number of expressions and subexpressions in this node
	 */
	public int size() {
		return m_expressionList.size();
	}

	/**
	 * @param currentCatalog
	 *            the current catalog. If a table name is not in this catalog,
	 *            then the table is fully qualified.
	 * @param currentSchema
	 *            the current catalog. If a table name is not in this schema,
	 *            then the table is fully qualified.
	 * @return this constraint in SQL form
	 */
	public String toSQL(Catalog currentCatalog, Schema currentSchema) {
		if (m_expressionList.size() > 0) {
			StringBuffer sql = new StringBuffer();

			Iterator iter = m_expressionList.listIterator();
			while (iter.hasNext()) {
				Object node = iter.next();
				if (node instanceof ConstraintNode) {
					ConstraintNode cnode = (ConstraintNode) node;
					sql.append(" (");
					sql.append(cnode.toSQL(currentCatalog, currentSchema));
					sql.append(" ) ");
				} else if (node instanceof Expression) {
					Expression constraint = (Expression) node;
					sql.append(constraint.toSQL(currentCatalog, currentSchema));
				} else if (node instanceof LogicalConnective) {
					LogicalConnective lc = (LogicalConnective) node;
					sql.append(" " + lc.toString() + " ");
				}
			}
			return sql.toString();
		} else
			return null;
	}

	/**
	 * @return the string representation of this node
	 */
	public String toString() {
		return toSQL(null, null);
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_expressionList = (LinkedList) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_expressionList);
	}

}
