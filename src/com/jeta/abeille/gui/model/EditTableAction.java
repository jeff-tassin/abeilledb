package com.jeta.abeille.gui.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;

import javax.swing.AbstractAction;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.modeler.TableEditorDialog;
import com.jeta.abeille.gui.table.TableFrame;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.interfaces.license.LicenseManager;

/**
 * This action is called when user selectes edit table command. Pops up edit
 * table dialog showing the selected table. This action can either show
 * read-only properties of an existing table or allow the user to edit the
 * properties of a modeling table.
 * 
 * @author Jeff Tassin
 */
public class EditTableAction extends AbstractAction {
	/** the modeler model - used for managing new tables and the like */
	private ModelerModel m_modeler;

	/** the view getter - gets the relevent ModelView */
	private ViewGetter m_viewgetter;

	/** the database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public EditTableAction(ModelerModel modeler, TSConnection connection, ViewGetter getter) {
		m_modeler = modeler;
		m_connection = connection;
		m_viewgetter = getter;
	}

	/**
	 * Invokes the table editor dialog
	 */
	public static void editTable(ModelerModel modeler, TableId tableid) {
		TableMetaData tmd = modeler.getTable(tableid);
		if (tableid != null && tmd != null) {
			assert (tmd.getTableId() != null);

			if (modeler.isPrototype(tableid)) {
				TableEditorDialog dlg = new TableEditorDialog(TSWorkspaceFrame.getInstance(),
						(TableMetaData) tmd.clone(), modeler.getConnection(), modeler);
				TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					// to effect new changes, let's just remove the old widget
					// and create a new one
					// the view will automatically create the links (also, we
					// don't allow user defined links
					// on non-saved tables )
					tmd = dlg.createTableMetaData();

					TableId newid = tmd.getTableId();
					TableId oldid = tableid;

					assert (modeler.isPrototype(oldid));

					// modeler will fire table_changed event
					tmd.setTableId(oldid);
					modeler.tableChanged(tmd);

					if (!newid.equals(oldid)) {
						tmd.setTableId(newid);
						// modeler will fire table_renamed event
						modeler.tableRenamed(newid, oldid);
					}
				}
			} else {
				// show table properties frame
				TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
				TableFrame tableframe = (TableFrame) wsframe.show(TableFrame.class, modeler.getConnection());
				if (tableframe != null) {
					tableframe.setTableId(tableid);
					wsframe.show(tableframe);
				}
			}
		}
	}

	/**
	 * Action listener implementation
	 */
	public void actionPerformed(ActionEvent evt) {
		TableWidget oldwidget = null;
		ModelView view = m_viewgetter.getModelView();
		Collection c = view.getSelectedItems();
		// only allow one widget to be selected
		if (c.size() == 1) {
			Object obj = c.iterator().next();
			if (obj instanceof TableWidget)
				oldwidget = (TableWidget) obj;

			editTable(m_modeler, oldwidget.getTableId());
		}
		// else if ( c.size() == 0 )
		// {
		// TablePropertiesAction.showPropertiesDialog( m_connection );
		// }
	}
}
