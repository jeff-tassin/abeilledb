package com.jeta.abeille.gui.queryresults;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.LinkedList;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.ForwardOnlyRowCache;
import com.jeta.abeille.database.utils.ResultSetReference;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class manages a set of ResultSet objects. This is used for handling SQL
 * statements that return multiple resultsets (e.g. calling stored procedures).
 * The problem is that if you move to the next resultset, the previous resultset
 * is automatically closed. So, we employ a forward row cache to get the data
 * for all resultsets BUT the last one. This is for performance because the
 * majority of queries only return a single resultset and we don't want to
 * download all the data at once for these cases.
 * 
 * @author Jeff Tassin
 */
public class ResultsManager {
	/** the underlying database connection */
	private TSConnection m_connection;

	/** a collection of QueryResultSet objects */
	private LinkedList m_results = new LinkedList();

	/** this is the last resultset that was added to this set */
	private ResultSetReference m_lastresults;

	/**
	 * Flag that indicates if this manager can accept new resultset objects.
	 * Once a caller invokes the getResults method, the manager is locked and
	 * cannot accept more sets.
	 */
	private boolean m_locked;

	/**
	 * ctor
	 */
	public ResultsManager(TSConnection tsconn) {
		m_connection = tsconn;
	}

	/**
	 * Adds a resultset to this manager. If there was a previous last resultset,
	 * then that set is cached and added to the current list.
	 */
	public synchronized void addResults(ResultSetReference rset) throws SQLException {
		assert (!m_locked);
		if (!m_locked) {
			cacheCurrentResults();
			m_lastresults = rset;
		}
	}

	/**
	 * Removes all resultsets from this manager but does not close them.
	 */
	public synchronized void clear() {
		m_results.clear();
		m_lastresults = null;
	}

	/**
	 * Caches the last result set.
	 */
	public synchronized void cacheCurrentResults() throws SQLException {
		if (m_lastresults != null) {
			/**
			 * create a forward only result that reads in everything at once. We
			 * do this because some databases don't support mutiple resultsets
			 * opened at one time
			 */
			m_lastresults.cacheMetaData();
			ForwardOnlyRowCache rowcache = new ForwardOnlyRowCache(null, m_lastresults, true);
			QueryResultSet qset = new QueryResultSet(rowcache);
			m_results.add(qset);
			/** force the model to download everything */
			qset.last();
			m_lastresults = null;
		}
	}

	/**
	 * @return the current collection of results (QueryResultSet objects)
	 */
	public synchronized Collection getResults() {
		assert (m_locked);
		assert (m_lastresults == null);
		return m_results;
	}

	/**
	 * Locks down this object so that it cannot accept more results. The reason
	 * is because we cache the last resultset potentially using a scrollable row
	 * cache. Since resulsets are closed if you move to the next set (from a
	 * statement that returns multiple sets), you must always finish adding
	 * resultsets before accessing ResultsManager.getResults.
	 */
	public synchronized void lock() throws SQLException {
		if (m_lastresults != null) {
			QueryResultSet qset = new QueryResultSet(null, m_lastresults);
			m_results.add(qset);
			m_lastresults = null;
			m_locked = true;
		}
	}

	/**
	 * @return the number of QueryResultSet objects in this manager
	 */
	public int size() {
		int count = 0;
		if (m_lastresults != null)
			count = 1;

		return m_results.size() + count;
	}

}
