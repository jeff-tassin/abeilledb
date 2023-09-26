package com.jeta.abeille.gui.sql;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.database.utils.TransposedResultSet;
import com.jeta.abeille.gui.command.ModalCommandRunner;
import com.jeta.abeille.gui.command.QueryCommand;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;


/**
 * This class manages the view for a single resultset. This frame window can
 * display multiple resultsets, so we manage each view with this type of object.
 */
public class TransposableResultsView extends TSPanel {

    private TSConnection m_tsconn;
    private ResultsView m_resultsView;
    private ResultsView m_transposedView;
    private boolean m_isTransposed = false;


    /**
     * ctor
     */
    public TransposableResultsView(Object launcher, TSConnection tsconn, SQLResultsModel model) {
        m_tsconn = tsconn;
        m_resultsView = new ResultsView(launcher, tsconn, null);
        m_transposedView = new ResultsView(launcher, tsconn, null);

        setResults( model );

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, m_resultsView);

        SQLResultsUIDirector uidirector = new SQLResultsUIDirector(m_resultsView);
        setUIDirector(uidirector);
        uidirector.updateComponents(null);

        SQLResultsController controller1 = new TransposableController(m_resultsView);
        controller1.assignAction(SQLResultsNames.ID_TRANSPOSE, new TransposeViewAction());
        SQLResultsController controller2 = new TransposableController(m_transposedView);
        controller2.assignAction(SQLResultsNames.ID_TRANSPOSE, new TransposeViewAction());
    }

    public void setResults(SQLResultsModel model) {
        m_resultsView.setResults(model);
        try {
            TransposedResultSet tset = new TransposedResultSet(model.getQueryResultSet());
            ResultSetReference rref = new ResultSetReference(new ConnectionReference(m_tsconn, m_tsconn.getWriteConnection()), null, tset, model.getSQL() + " --transposed");
            SQLResultsModel tmodel = new SQLResultsModel(m_tsconn, rref);
            tmodel.last();
            m_transposedView.setResults(tmodel);
        } catch( Exception e ) {
            e.printStackTrace();;
        }
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

    class TransposeRedoQueryAction implements ActionListener {
        ResultsView currentView = m_isTransposed ? m_transposedView : m_resultsView;
        public void actionPerformed(ActionEvent evt) {
            try {
                m_transposedView.saveFrame();
                m_resultsView.saveFrame();

                SQLResultsModel model = (SQLResultsModel) m_resultsView.getModel();
                ConnectionReference cref = model.getConnectionReference();
                ResultSetReference ref = model.getResultSetReference();
                String sql = ref.getSQL();

                TSConnection tsconn = cref.getTSConnection();
                int rtype = tsconn.getResultSetScrollType();
                int concurrency = tsconn.getResultSetConcurrency();
                Statement stmt = cref.getConnection().createStatement(rtype, concurrency);

                QueryCommand cmd = new QueryCommand(cref, stmt, sql);
                ModalCommandRunner crunner = new ModalCommandRunner(cref.getTSConnection(), currentView, cmd);
                if (crunner.invoke() == ModalCommandRunner.COMPLETED) {
                    ref = cmd.getResultSetReference();
                    ref.setSQL(sql);
                    ref.setUnprocessedSQL(model.getUnprocessedSQL());
                    SQLResultsModel smodel = new SQLResultsModel(tsconn, ref, model.getTableId());
                    Timer timer = new Timer(1000, e -> setResults(smodel));
                    timer.setRepeats(false);
                    timer.start();
                }
            } catch (SQLException e) {
                ((TransposableController) currentView.getController()).showError(e);
            }
        }
    }

    class TransposableController extends SQLResultsController {
        public TransposableController(ResultsView view) {
            super(view);
            super.assignAction( SQLResultsNames.ID_REDO_QUERY, new TransposeRedoQueryAction() );
        }

        /** to eliminate missing component messages */
        public void assignAction(String compName, ActionListener action) {
            String[] excluded = {
                    TSComponentNames.ID_PRINT_PREVIEW,
                    SQLResultsNames.ID_PREFERENCES,
                    SQLResultsNames.ID_REDO_QUERY
            };
            if (!Arrays.asList(excluded).contains(compName)) {
                super.assignAction(compName, action);
            }
        }
    }
}