package com.jeta.abeille.gui.importer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.PreparedStatementWriter;
import com.jeta.abeille.database.utils.SQLFormatter;
import com.jeta.abeille.database.utils.SQLFormatterFactory;

import com.jeta.abeille.gui.formbuilder.SubQuery;
import com.jeta.abeille.gui.formbuilder.SubQueryBuilder;
import com.jeta.abeille.gui.formbuilder.SubQueryProxy;
import com.jeta.abeille.gui.formbuilder.ValueProxy;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewModel;

import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;

import com.jeta.foundation.i18n.I18N;

/**
 * The main controller for the ImportBuilderFrame
 * 
 * @author Jeff Tassin
 */
public class ImportBuilderFrameController extends TSController {

	/** the frame we are controlling */
	private ImportBuilderFrame m_frame;

	/**
	 * ctor
	 */
	public ImportBuilderFrameController(ImportBuilderFrame iframe) {
		super(iframe);
		m_frame = iframe;
		assignAction(ImportBuilderNames.ID_START_IMPORT, new ImportAction());
		assignAction(ImportBuilderNames.ID_TARGET, new SetTargetAction());

		assignAction(ImportBuilderNames.ID_OPEN_IMPORT, new OpenImportFileAction());
		assignAction(ImportBuilderNames.ID_SAVE_IMPORT, new SaveImportFileAction());

		iframe.setUIDirector(new ImportBuilderFrameUIDirector(iframe));
	}

	private HashMap buildQueries(TSConnection connection, TableId targetid, ModelViewModel modelviewmodel) {
		TableMetaData targettmd = connection.getTable(targetid);

		HashMap queries = new HashMap();

		LinkModel linkmodel = modelviewmodel.getLinkModel();
		DefaultLinkModel defmodel = (DefaultLinkModel) linkmodel;

		// @todo we probabaly need to analze the out links as well to handle the
		// custom out links case
		LinkedList c = new LinkedList();
		c.addAll(linkmodel.getInLinks(targetid));
		c.addAll(linkmodel.getOutLinks(targetid));
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Link l = (Link) iter.next();

			String columname = null;
			/** this is the column in the target table that has a link */
			ColumnMetaData target_cmd = null;
			TableId refid = l.getSourceTableId();

			// for each link (in or out) find the target column
			if (refid.equals(targetid)) {
				refid = l.getDestinationTableId();
				columname = l.getSourceColumn();
				target_cmd = targettmd.getColumn(columname);
			} else {
				columname = l.getDestinationColumn();
				target_cmd = targettmd.getColumn(columname);
				refid = l.getSourceTableId();
			}

			// now, we have a start table for a sub query
			// we need to determine the contraints/reportables for that sub
			// query
			// first, build the sub link model
			SubQuery subquery = (SubQuery) queries.get(refid);
			if (subquery == null) {
				subquery = SubQueryBuilder.build(refid, linkmodel, targetid);
				// subquery.print();
				queries.put(refid, subquery);
			}

			TableMetaData reftmd = connection.getTable(refid);
			ColumnMetaData refcmd = reftmd.getColumn(l.getSourceColumn());
			subquery.addReportable(refcmd);
		}

		return queries;
	}

	private void doImport() throws SQLException {
		m_frame.saveState();

		if (validateInputs2()) {
			TSConnection connection = m_frame.getConnection();
			ConnectionReference cref = new ConnectionReference(connection, connection.getWriteConnection());

			ImportBuilderModel importmodel = m_frame.getModel();
			TableId targetid = importmodel.getTargetTable();
			if (targetid != null) {
				HashMap queries = buildQueries(m_frame.getConnection(), targetid, importmodel);
				Iterator iter = queries.values().iterator();
				while (iter.hasNext()) {
					SubQuery subquery = (SubQuery) iter.next();
					subquery.print();
				}

				LinkedList proxies = new LinkedList();
				Collection targets = importmodel.getTargetColumns();
				iter = targets.iterator();
				while (iter.hasNext()) {
					TargetColumnInfo info = (TargetColumnInfo) iter.next();
					TableId id = info.getTableId();
					// for now, just handle single table import
					if (targetid.equals(id)) {
						ImportValueProxy proxy = new ImportValueProxy(importmodel.getQueryResults(), info);
						proxies.add(proxy);
					} else {
						// this is a table that is not the target, so we need to
						// add it as a constraint
						// in a subquery
						// now find all subqueries that contain the target
						// column
						Iterator qiter = queries.values().iterator();
						while (qiter.hasNext()) {
							SubQuery subquery = (SubQuery) qiter.next();
							if (subquery.contains(id)) {
								// the value is constrained, so we can add it
								subquery.addConstraint(new ImportValueProxy(importmodel.getQueryResults(), info));
							}
						}
					}
				}

				TableMetaData targettmd = connection.getTable(targetid);

				LinkModel linkmodel = importmodel.getLinkModel();
				Collection inlinks = linkmodel.getInLinks(targetid);
				iter = inlinks.iterator();
				while (iter.hasNext()) {
					Link l = (Link) iter.next();
					String columname = l.getDestinationColumn();
					ColumnMetaData cmd = targettmd.getColumn(columname);
					TableId refid = l.getSourceTableId();
					TableMetaData reftmd = connection.getTable(refid);
					ColumnMetaData refcmd = reftmd.getColumn(l.getSourceColumn());

					SubQuery subquery = (SubQuery) queries.get(refid);
					assert (subquery != null);
					ImporterSubQueryProxy vproxy = new ImporterSubQueryProxy(cref, subquery, cmd, refcmd);
					proxies.add(vproxy);
				}
				startImport(cref, importmodel, proxies);
			}
		}
	}

	/**
	 * Starts the import process
	 */
	private void startImport(ConnectionReference cref, ImportBuilderModel model, Collection valueproxies) {
		TSConnection connection = null;
		Connection conn = null;

		try {
			TableId targetid = model.getTargetTable();

			connection = m_frame.getConnection();
			SQLFormatter formatter = SQLFormatterFactory.getInstance(connection).createFormatter();
			conn = cref.getConnection();

			QueryResultSet rset = model.getQueryResults();

			ColumnMetaData[] rsetcols = rset.getColumnMetaData();
			int colcount = rsetcols.length;

			StringBuffer sqlbuff = new StringBuffer();
			StringBuffer values = new StringBuffer();
			values.append(" VALUES (");

			sqlbuff.append("INSERT INTO ");
			sqlbuff.append(targetid.getFullyQualifiedName());
			sqlbuff.append(" (");
			Iterator iter = valueproxies.iterator();
			while (iter.hasNext()) {
				ValueProxy proxy = (ValueProxy) iter.next();
				ColumnMetaData cmd = proxy.getColumnMetaData();
				sqlbuff.append(cmd.getColumnName());
				values.append("?");
				if (iter.hasNext()) {
					sqlbuff.append(", ");
					values.append(", ");
				}
			}
			sqlbuff.append(") ");
			values.append(") ");
			sqlbuff.append(values.toString());
			sqlbuff.append(";");
			String sql = sqlbuff.toString();

			System.out.println(sql);
			rset.first();
			while (true) {
				PreparedStatement pstmt = cref.prepareStatement(sql);
				PreparedStatementWriter pwriter = new PreparedStatementWriter(sql);

				int count = 1;
				iter = valueproxies.iterator();
				while (iter.hasNext()) {
					ValueProxy proxy = (ValueProxy) iter.next();
					proxy.prepareStatement(count, pwriter, formatter);
					proxy.prepareStatement(count, pstmt, formatter);
					count++;
				}

				System.out.println(pwriter.getPreparedSQL());
				pstmt.executeUpdate();
				pstmt.close();

				if (!rset.next())
					break;
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				TSDatabase tsdb = (TSDatabase) connection.getImplementation(TSDatabase.COMPONENT_ID);
				if (tsdb.supportsTransactions())
					conn.rollback();
			} catch (Exception noe) {
				// no op
			}
		}

	}

	/**
	 * Checks that current model and makes sure the inputs are valid before
	 * starting an input
	 */
	public boolean validateInputs2() {
		ImportBuilderModel importmodel = m_frame.getModel();
		Collection cols = importmodel.getTargetColumns();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			TargetColumnInfo info = (TargetColumnInfo) iter.next();
			if (info.getSourceColumn() == null) {
				String msg = I18N.format("Import_column_not_set_1", info.getTarget().getName());
				String title = I18N.getLocalizedMessage("Error");
				JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	/**
	 * Starts the import process
	 */
	public class ImportAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				doImport();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Opens a previously saved import definition
	 */
	public class OpenImportFileAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				File f = com.jeta.foundation.gui.filechooser.TSFileChooserFactory.showOpenDialog();
				if (f != null) {
					FileInputStream fis = new FileInputStream(f);
					ObjectInputStream ois = new ObjectInputStream(fis);
					ImportBuilderModel importmodel = (ImportBuilderModel) ois.readObject();
					// importmodel.setConnection( m_frame.getConnection() );
					// importmodel.initialize();
					// m_frame.setImportBuilderModel( importmodel );
					// ois.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves the current import definition to a file
	 */
	public class SaveImportFileAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			m_frame.saveState();
			ImportBuilderModel importmodel = m_frame.getModel();
			File f = com.jeta.foundation.gui.filechooser.TSFileChooserFactory.showSaveDialog();
			if (f != null) {
				try {
					FileOutputStream fos = new FileOutputStream(f);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(importmodel);
					oos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * Sets the selected table as the target for the import
	 */
	public class SetTargetAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView iview = m_frame.getModelView();
			TableMetaData tmd = iview.getSelectedTable();
			if (tmd != null) {
				ImportBuilderModel imodel = m_frame.getModel();
				imodel.setTargetTable(tmd.getTableId());
				UIDirector director = m_frame.getUIDirector();
				director.updateComponents(null);
			}
		}
	}

}
