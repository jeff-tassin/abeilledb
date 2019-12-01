package com.jeta.abeille.gui.importer;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.model.TableViewController;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.split.CustomSplitPane;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the view window that contains a graphical view of tables and joins
 * used for building imports from arbitrary data
 * 
 * @author Jeff Tassin
 */
public class ImportBuilderView extends TSPanel {
	/** the view of tables used to build the form */
	private ModelView m_tableview;

	private ImportBuilderModel m_importermodel;

	private ModelerModel m_modeler;

	private TargetColumnsView m_targetview;
	private SourceColumnsView m_sourceview;

	/** the text field that displays the selected result set */
	private JTextField m_rsetfield;

	public static final String ID_MAIN_SPLIT = "main.splitter";

	/**
	 * ctor
	 */
	public ImportBuilderView(ImportBuilderModel model, ModelerModel modeler) {
		assert (model != null);
		m_modeler = modeler;
		m_importermodel = model;
		createComponents();
	}

	/**
	 * Creates the columns panel at the bottom pane in this frame
	 * 
	 */
	JPanel createBottomPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 10));

		m_targetview = new TargetColumnsView(new TargetColumnsModel(m_importermodel.getTargetColumns()));
		m_targetview.setController(new TargetColumnsViewController(m_targetview));

		m_sourceview = new SourceColumnsView(new SourceColumnsModel());

		CustomSplitPane split = new CustomSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		// split.getThumb().setVisible( false );
		split.setDividerLocation(0.7f);
		split.add(m_targetview);
		split.add(m_sourceview);

		panel.add(split, BorderLayout.CENTER);

		m_rsetfield = new JTextField();
		m_rsetfield.setEnabled(false);

		JPanel btnpanel = new JPanel();
		btnpanel.setLayout(new BorderLayout());
		JButton rsetbtn = new JButton(TSGuiToolbox.loadImage("ellipsis16.gif"));
		rsetbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				selectResultSet();
			}
		});

		rsetbtn.setFocusPainted(false);
		Dimension d = new Dimension(32, 16);
		rsetbtn.setSize(d);
		rsetbtn.setMaximumSize(d);
		rsetbtn.setPreferredSize(d);

		btnpanel.add(m_rsetfield, BorderLayout.CENTER);
		btnpanel.add(rsetbtn, BorderLayout.EAST);

		JComponent[] controls = new JComponent[1];
		controls[0] = btnpanel;

		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Result Set"));
		JPanel cpanel = TSGuiToolbox.alignLabelTextRows(labels, controls);

		panel.add(cpanel, BorderLayout.NORTH);

		return panel;
	}

	/**
	 * Initializes the components on this frame
	 */
	protected void createComponents() {
		m_tableview = new ModelView(m_importermodel);
		m_tableview.setController(new TableViewController(m_tableview, m_modeler));

		JScrollPane scroller = new JScrollPane(m_tableview);

		CustomSplitPane split = new CustomSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setName(ID_MAIN_SPLIT);
		split.add(scroller);
		split.add(createBottomPanel());
		split.setDividerLocation(0.7f);
		split.setOneTouchExpandable(false);

		setLayout(new BorderLayout());
		// you must add the split's layered pane instead of the split itself
		// add( split.getLayeredPane(), BorderLayout.CENTER );
		add(split, BorderLayout.CENTER);
	}

	/**
	 * @return the form model
	 */
	public ImportBuilderModel getModel() {
		return m_importermodel;
	}

	/**
	 * @return the one and only tables view component for this frame
	 */
	public ModelView getModelView() {
		return m_tableview;
	}

	public void reloadView() {
		QueryResultSet rset = m_importermodel.getQueryResults();
		m_sourceview.setModel(new SourceColumnsModel(rset));
		m_rsetfield.setText(rset.getSQL());
	}

	/**
	 * Saves the state (and all views) to the data model
	 */
	public void saveState() {
		TargetColumnsModel tmodel = m_targetview.getModel();
		m_importermodel.setTargetColumns(tmodel.getTargetColumns());
	}

	/**
	 * Iterates over all opened result set frames in the workspace. Displays a
	 * dialog that allows the user to select one of the result sets.
	 */
	private void selectResultSet() {
		ArrayList results = new ArrayList();
		Collection frames = TSWorkspaceFrame.getInstance().getAllFrames(null);
		Iterator iter = frames.iterator();
		while (iter.hasNext()) {
			TSInternalFrame iframe = (TSInternalFrame) iter.next();
			if (iframe instanceof com.jeta.abeille.gui.sql.SQLResultsFrame) {
				com.jeta.abeille.gui.sql.SQLResultsFrame sqlframe = (com.jeta.abeille.gui.sql.SQLResultsFrame) iframe;
				// results.add( sqlframe.getModel().getQueryResultSet() );
				assert (false);
			}
		}

		if (results.size() == 0) {
			// error
		} else {
			GetResultSetDialog dlg = (GetResultSetDialog) TSGuiToolbox.createDialog(GetResultSetDialog.class, this,
					true);
			dlg.initialize(results);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				QueryResultSet rset = dlg.getSelectedResultSet();
				m_importermodel.setQueryResults(rset);
				reloadView();
			}
		}
	}
}
