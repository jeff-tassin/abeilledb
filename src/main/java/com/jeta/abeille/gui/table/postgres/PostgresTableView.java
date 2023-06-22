package com.jeta.abeille.gui.table.postgres;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdGetter;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.checks.postgres.ChecksModel;
import com.jeta.abeille.gui.checks.postgres.ChecksView;
import com.jeta.abeille.gui.help.SQLHelpPanel;
import com.jeta.abeille.gui.help.SQLReferenceType;
import com.jeta.abeille.gui.indexes.IndexesView;
import com.jeta.abeille.gui.indexes.generic.GenericIndexesModel;
import com.jeta.abeille.gui.jdbc.ColumnInfoPanel;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.modeler.*;
import com.jeta.abeille.gui.rules.postgres.RulesModel;
import com.jeta.abeille.gui.rules.postgres.RulesView;
import com.jeta.abeille.gui.table.TableView;
import com.jeta.abeille.gui.triggers.TriggersModel;
import com.jeta.abeille.gui.triggers.TriggersView;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import javax.swing.*;

/**
 * This view displays all the properties for a table in a JTabbedPane
 * 
 * @author Jeff Tassin
 */
public class PostgresTableView extends TableView implements TableIdGetter {

	/** the model for the table columns */
	private ColumnsGuiModel m_colsmodel;

	/** the view for the primary key */
	private PrimaryKeyView m_pkview;

	/** the foreign key model */
	private ForeignKeysModel m_foreignkeymodel;

	/** the model for the table indices */
	private GenericIndexesModel m_indicesmodel;

	/** the model for the table indices */
	private ChecksModel m_checksmodel;

	/** the model for the table rules */
	private RulesModel m_rulesmodel;

	/** the model for the table triggers */
	private TriggersModel m_triggersmodel;

	/** JDBC driver info */
	private ColumnInfoPanel m_jdbcview; // this pane shows raw jdbc information
										// about a table in the database

	/** Postgres specific view */
	/** later, this needs to be moved into the plugins area */
	private PostgresView m_postgresview;

	// //////////////////////////////////////////////////////
	// sql pane
	private SQLPanel m_sqlview;

	/**
	 * ctor
	 */
	public PostgresTableView(TSConnection conn) {
		super(conn);
		initialize();
		setController(new TableViewController(this));
	}

	/**
	 * Creates the view that displays the columns
	 */
	private TSPanel createColumnsView() {
		m_colsmodel = new ColumnsGuiModel(getConnection(), false, null, false);
		ColumnsPanel view = new ColumnsPanel(m_colsmodel, false);
		view.setController( new TableColumnsPanelController(view));
		return view;
	}

	/**
	 * Creates the check constraints view
	 */
	private TSPanel createChecksView() {
		m_checksmodel = new ChecksModel(getConnection(), null);
		ChecksView view = new ChecksView(m_checksmodel);
		return new SQLHelpPanel(getConnection(), view, SQLReferenceType.CHECK_CONSTRAINTS);
	}

	/**
	 * Creates the foreign keys view
	 */
	private TSPanel createForeignKeysView() {
		ForeignKeysView fkview = new ForeignKeysView(m_colsmodel, getConnection(),
				ModelerModel.getDefaultInstance(getConnection()), this, false);

		m_foreignkeymodel = fkview.getModel();
		return new SQLHelpPanel(getConnection(), fkview, SQLReferenceType.FOREIGN_KEYS);
	}

	/**
	 * Creates the primary key view
	 */
	private TSPanel createPrimaryKeyView() {
		m_pkview = new PrimaryKeyView(getConnection(), m_colsmodel, null, false);
		return m_pkview;
	}

	/**
	 * Creates the indices view for the selected table
	 */
	private TSPanel createIndexesView() {
		m_indicesmodel = new GenericIndexesModel(getConnection(), null);
		IndexesView view = new IndexesView(m_indicesmodel);
		// view.setController( new IndexesViewController( view ) );
		return view;
	}

	/**
	 * Creates the JDBC Panel. Called by initialize. This panel allows the user
	 * to raw information about the table from the JDBC driver
	 */
	private TSPanel createJDBCView() {
		m_jdbcview = new ColumnInfoPanel();
		m_jdbcview.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return m_jdbcview;
	}

	/**
	 * Creates the view that shows postgres specific options for this table
	 */
	private TSPanel createPostgresView() {
		m_postgresview = new PostgresView(getConnection(), getTableId());
		return m_postgresview;
	}

	/**
	 * Creates the rules view for the selected table
	 */
	private TSPanel createRulesView() {
		m_rulesmodel = new RulesModel(getConnection(), null);
		RulesView view = new RulesView(m_rulesmodel);
		return view;
	}

	/**
	 * Creates the SQL Panel. Called by initialize. This panel allows the user
	 * to view/tweak the generated SQL
	 */
	private SQLPanel createSQLView() {
		m_sqlview = new SQLPanel(getConnection());
		return m_sqlview;
	}

	/**
	 * Creates the view that displays the triggers in the system
	 */
	private TSPanel createTriggersView() {
		m_triggersmodel = new TriggersModel(getConnection(), null);
		return new SQLHelpPanel(getConnection(), new TriggersView(m_triggersmodel), SQLReferenceType.TRIGGERS);
	}

	/**
	 * Initializes and creates the sub-views for this view
	 */
	private void initialize() {
		addView(I18N.getLocalizedMessage("Columns"), createColumnsView(), null);
		addView(I18N.getLocalizedMessage("Primary Key"), createPrimaryKeyView(), null);
		addView(I18N.getLocalizedMessage("Foreign Keys"), createForeignKeysView(), null);
		addView(I18N.getLocalizedMessage("Indexes"), createIndexesView(), null);
		addView(I18N.getLocalizedMessage("Checks"), createChecksView(), null);
		addView(I18N.getLocalizedMessage("Rules"), createRulesView(), null);
		addView(I18N.getLocalizedMessage("Triggers"), createTriggersView(), null);
		addView("PostgreSQL", createPostgresView(), null);
		addView(I18N.getLocalizedMessage("SQL"), createSQLView(), null);
		addView("JDBC", createJDBCView(), null);
	}

	/**
	 * Sets the current table id for the view. All tabs are updated to display
	 * the properties for the given table
	 */
	public void setTableId(TableId tableId) {
		try {
			super.setTableId(tableId);

			// make sure the table is fully loaded
			TableMetaData tmd = null;
			if (tableId != null) {
				tmd = getConnection().getModel(tableId.getCatalog()).getTableEx(tableId,
						TableMetaData.LOAD_FOREIGN_KEYS | TableMetaData.LOAD_COLUMNS_EX);
			}

			m_colsmodel.setTableId(tableId);
			m_pkview.loadData(m_colsmodel);
			m_foreignkeymodel.setTableId(tableId);
			m_indicesmodel.setTableId(tableId);
			m_checksmodel.setTableId(tableId);
			m_rulesmodel.setTableId(tableId);
			m_triggersmodel.setTableId(tableId);
			m_postgresview.setTableId(tableId);
			m_jdbcview.refresh(getConnection().getMetaDataConnection(), tableId);

			if (tmd == null) {
				m_sqlview.setText("");
			} else {
				assert (tmd.getCatalog() != null);
				assert (tmd.getSchema() != null);
				m_sqlview.setText(DbUtils.createTableSQL(getConnection(), tmd));
			}
		} catch (Exception e) {
			TSUtils.printStackTrace(e);
		}

	}
}
