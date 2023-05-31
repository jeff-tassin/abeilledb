package com.jeta.abeille.gui.sql;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.queryresults.QueryUtils;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This class manages the view for a single resultset. This frame window can
 * display multiple resultsets, so we manage each view with this type of object.
 */
public class TransposableResultsView extends TSPanel {

    private ResultsView m_resultsView;
    private AbstractTablePanel m_transposedView;
    private boolean m_isTransposed = false;


    /**
     * ctor
     */
    public TransposableResultsView(Object launcher, TSConnection tsconn, SQLResultsModel model) {
        setLayout(new BorderLayout());
        m_resultsView = new ResultsView(launcher, tsconn, model);
        SQLResultsController controller = new SQLResultsController(m_resultsView);
        controller.assignAction(SQLResultsNames.ID_TRANSPOSE, new TransposeViewAction());

        add(BorderLayout.CENTER, m_resultsView);

        SQLResultsUIDirector uidirector = new SQLResultsUIDirector(m_resultsView);
        setUIDirector(uidirector);
        uidirector.updateComponents(null);

        try {
            m_transposedView = QueryUtils.createTransposedResultView("transposed", tsconn, model.getQueryResultSet().getResultSet());
        } catch( Exception e ) {
            System.out.println(e);
        }

    }

    public ResultsView getResultsView() {
        return m_resultsView;
    }

    /**
     * Cleans up this view to assist in garbage collection.
     */
    public void dispose() {
        m_resultsView.dispose();
        removeAll();
    }

    public void saveFrame() {
        m_resultsView.saveFrame();
    }

    class TransposeViewAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            System.out.println("transpose view");
            remove(m_resultsView);
            remove(m_transposedView);
            if ( m_isTransposed ) {
                add( m_resultsView, BorderLayout.CENTER );
            } else {
                add( m_transposedView, BorderLayout.CENTER );
            }
            m_isTransposed = !m_isTransposed;
        }
    }

}