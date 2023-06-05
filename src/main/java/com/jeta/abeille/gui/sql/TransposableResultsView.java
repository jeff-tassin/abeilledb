package com.jeta.abeille.gui.sql;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.database.utils.TransposedResultSet;
import com.jeta.foundation.gui.components.TSPanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This class manages the view for a single resultset. This frame window can
 * display multiple resultsets, so we manage each view with this type of object.
 */
public class TransposableResultsView extends TSPanel {

    private ResultsView m_resultsView;
    private ResultsView m_transposedView;
    private boolean m_isTransposed = false;


    /**
     * ctor
     */
    public TransposableResultsView(Object launcher, TSConnection tsconn, SQLResultsModel model) {
        setLayout(new BorderLayout());
        m_resultsView = new ResultsView(launcher, tsconn, model);

        add(BorderLayout.CENTER, m_resultsView);

        SQLResultsUIDirector uidirector = new SQLResultsUIDirector(m_resultsView);
        setUIDirector(uidirector);
        uidirector.updateComponents(null);

        try {
            TransposedResultSet tset = new TransposedResultSet(model.getQueryResultSet());
            ResultSetReference rref = new ResultSetReference(new ConnectionReference(tsconn, tsconn.getWriteConnection()), null, tset, model.getUnprocessedSQL() + " --transposed");
            SQLResultsModel tmodel = new SQLResultsModel(tsconn, rref);
            m_transposedView = new ResultsView(launcher, tsconn, tmodel);
            tmodel.last();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        SQLResultsController controller1 = new SQLResultsController(m_resultsView);
        controller1.assignAction(SQLResultsNames.ID_TRANSPOSE, new TransposeViewAction());
        SQLResultsController controller2 = new SQLResultsController(m_transposedView);
        controller2.assignAction(SQLResultsNames.ID_TRANSPOSE, new TransposeViewAction());
    }

    public ResultsView getResultsView() {
        return m_resultsView;
    }

    public ResultsView getTransposedView() {
        return m_transposedView;
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
        m_transposedView.saveFrame();
    }

    public void transpose() {
        remove(m_resultsView);
        remove(m_transposedView);
        if ( m_isTransposed ) {
            add( m_resultsView, BorderLayout.CENTER );
        } else {
            add( m_transposedView, BorderLayout.CENTER );
        }
        m_isTransposed = !m_isTransposed;
        validate();
    }

    class TransposeViewAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            transpose();
        }
    }

}