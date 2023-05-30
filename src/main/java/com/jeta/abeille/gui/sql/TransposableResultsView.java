package com.jeta.abeille.gui.sql;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.foundation.gui.components.TSPanel;

import java.awt.*;


/**
 * This class manages the view for a single resultset. This frame window can
 * display multiple resultsets, so we manage each view with this type of object.
 */
public class TransposableResultsView extends TSPanel {

    private ResultsView m_resultsView;
    

    /**
     * ctor
     */
    public TransposableResultsView(Object launcher, TSConnection tsconn, SQLResultsModel model) {
        setLayout(new BorderLayout());
        ResultsView view = new ResultsView(launcher, tsconn, model);
        SQLResultsController controller = new SQLResultsController(view);
        add(BorderLayout.CENTER, view);


        SQLResultsUIDirector uidirector = new SQLResultsUIDirector(view);
        setUIDirector(uidirector);
        uidirector.updateComponents(null);
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
}