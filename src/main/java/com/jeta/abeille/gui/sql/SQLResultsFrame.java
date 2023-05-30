package com.jeta.abeille.gui.sql;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.gui.queryresults.ResultsManager;
import com.jeta.abeille.gui.utils.SQLErrorDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This is the Frame window for the sql results. It contains the JTables that
 * display the results of a query
 * 
 * @author Jeff Tassin
 */
public class SQLResultsFrame extends TSInternalFrame {

	/** the underlying connection manager */
	private TSConnection m_tsconnection;

	/**
	 * A weakreference to the window that launched this frame. If this results
	 * frame was launched from an instanceframe, then this member will be
	 * non-null
	 */
	private Object m_launcher;

	/** the frame icon for this frame */
	private static ImageIcon m_frameicon;

	/**
	 * The main container that holds the query views/tab pane
	 */
	private JTabbedPane m_view_container;

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/table_sql_view.png");
	}

	/** ctor */
	public SQLResultsFrame() {
		super(I18N.getLocalizedMessage("SQL Results"));
		setShortTitle(I18N.getLocalizedMessage("SQL Results"));

		initialize();
		setFrameIcon(m_frameicon);
	}




	/**
	 * Override dispose so we can store the table settings for the given query.
	 * We do this so that the next time the user runs the same query, we can
	 * configure the view as it was the last time the user used it.
	 */
	public void dispose() {

		Collection views = getViews();
		Iterator iter = views.iterator();
		while (iter.hasNext()) {
			TransposableResultsView view = (TransposableResultsView) iter.next();
			view.saveFrame();
			view.dispose();
		}

		getContentPane().removeAll();
		super.dispose();

		m_tsconnection = null;
		m_launcher = null;
		setController(null);
	}

	/**
	 * @return the connection manager
	 */
	public TSConnection getConnection() {
		return m_tsconnection;
	}

	/**
	 * @return the object that launched this frame ( can be null).
	 */
	public Object getLauncher() {
		return m_launcher;
	}

	/**
	 * @return the preferred size for this frame
	 */
	public Dimension getPreferredSize() {
		return new Dimension(750, 500);
	}


	/**
	 * @return a collection of all ResultsView objects in the frame.
	 */
	public Collection<TransposableResultsView> getViews() {
		LinkedList<TransposableResultsView> results = new LinkedList<TransposableResultsView>();
		for (int index = 0; index < m_view_container.getTabCount(); index++) {
			TransposableResultsView view = (TransposableResultsView) m_view_container.getComponentAt(index);
			results.add(view);
		}
		return results;
	}

	/**
	 * Creates the menu, toolbar, and child components for this frame window
	 */
	void initialize() {
		m_view_container = new JTabbedPane(JTabbedPane.BOTTOM);
		getContentPane().add(m_view_container, BorderLayout.CENTER);
	}

	/**
	 * Creates the menu, toolbar, and content window for this frame
	 * 
	 * @param params
	 *            a 2 length array that contains a TSConnection and
	 *            ResultSetReference object
	 */
	public void initializeModel(Object[] params) {
		try {
			m_view_container.removeAll();

			m_tsconnection = (TSConnection) params[0];
			Object results = params[1];
			m_launcher = params[2];
			TableId tableid = null;
			if (params.length == 4)
				tableid = (TableId) params[3];

			if (results instanceof ResultSetReference) {
				ResultSetReference ref = (ResultSetReference) results;
				SQLResultsModel model = new SQLResultsModel(m_tsconnection, ref, tableid);
				TransposableResultsView view = new TransposableResultsView(m_launcher, m_tsconnection, model);
				m_view_container.addTab("Results", m_frameicon, view );
			} else if (results instanceof QueryResultSet) {
				/**
				 * we allow passing QueryResultSet for cases where the resultset
				 * if forward only
				 */
				SQLResultsModel model = new SQLResultsModel(m_tsconnection, (QueryResultSet) results, tableid);
				TransposableResultsView view = new TransposableResultsView(m_launcher, m_tsconnection, model);
				m_view_container.addTab("Results", m_frameicon, view );
			} else if (results instanceof ResultsManager) {
				/**
				 * this supports the case where we have multiple results from a
				 * query/stored procedure
				 */
				ResultsManager rmgr = (ResultsManager) results;
				if (rmgr.size() <= 1) {
					Collection query_results = rmgr.getResults();
					Iterator iter = query_results.iterator();
					if (iter.hasNext()) {
						QueryResultSet qset = (QueryResultSet) iter.next();
						SQLResultsModel model = new SQLResultsModel(m_tsconnection, qset, null);
						TransposableResultsView view = new TransposableResultsView(m_launcher, m_tsconnection, model);
						m_view_container.addTab("Results", m_frameicon, view );
					}
				} else {
					Collection query_results = rmgr.getResults();
					Iterator iter = query_results.iterator();
					int count = 1;
					while (iter.hasNext()) {
						QueryResultSet qset = (QueryResultSet) iter.next();
						SQLResultsModel model = new SQLResultsModel(m_tsconnection, qset, null);
						TransposableResultsView view = new TransposableResultsView(m_launcher, m_tsconnection, model);
						m_view_container.addTab(I18N.format("ResultSet_1", TSUtils.getInteger(count)), m_frameicon, view);
						count++;
					}
				}
			}
		} catch (Exception e) {
			SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, this, true);
			dlg.initialize(e);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
		}
	}

	/**
	 * This is only needed to support docking frames
	 */
	public void setDelegate(com.jeta.foundation.gui.components.WindowDelegate delegate) {
		java.awt.Container cc = getContentPane();
		if (cc != null)
			cc.remove(m_view_container);

		super.setDelegate(delegate);
		cc = getContentPane();
		if (cc != null && m_view_container != null) {
			cc.add(m_view_container, java.awt.BorderLayout.CENTER);
		}
	}



}
