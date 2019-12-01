package com.jeta.abeille.gui.model;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.MultiColumnLink;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.model.utils.UserLinkInfoView;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

/**
 * This class provides the logic necessary to handle drag/drop of links in the
 * modeler view. Her we check conditions such as a user cannot drag a link
 * between two saved tables unless the link is a user defined link.
 * 
 * @author Jeff Tassin
 */
public class ModelerLinkUI extends LinkUI {

	/**
	 * The modeler
	 */
	private ModelerModel m_modeler;

	/**
	 * ctor
	 */
	public ModelerLinkUI(ModelerModel modeler) {
		m_modeler = modeler;
	}

	/**
	 * Creates the foreign key in the given table and updates the view
	 */
	private void createForeignKey(TableMetaData desttmd, String destCol, TableId sourceId, String sourceCol) {
		DbForeignKey fkey = new DbForeignKey();
		fkey.setSourceTableId(sourceId);
		fkey.setDestinationTableId(desttmd.getTableId());
		fkey.assignForeignKeyColumn(destCol, sourceCol);
		desttmd.addForeignKey(fkey);
		m_modeler.tableChanged(desttmd);
	}

	/**
	 * @return the foriegn key that is defined by this link
	 */
	private DbForeignKey getForeignKey(Link link) {
		if (link instanceof MultiColumnLink) {
			MultiColumnLink flink = (MultiColumnLink) link;
			return flink.getForeignKey();
		} else {
			DbForeignKey fkey = new DbForeignKey();
			fkey.setSourceTableId(link.getSourceTableId());
			fkey.setDestinationTableId(link.getDestinationTableId());
			fkey.assignForeignKeyColumn(link.getDestinationColumn(), link.getSourceColumn());
			return fkey;
		}
	}

	/**
	 * Posts a dialog on the screen that explains what a User-defined link is.
	 */
	protected void postUserLinkInfoMessage() {

		if (TSUserPropertiesUtils.getBoolean(UserLinkInfoView.ID_SHOW_USER_INFO_VIEW, true)) {
			Runnable gui_update = new Runnable() {
				public void run() {
					UserLinkInfoView view = new UserLinkInfoView();
					TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, TSWorkspaceFrame.getInstance(),
							true);
					dlg.setTitle(I18N.getLocalizedMessage("Added User Defined Link"));
					dlg.setPrimaryPanel(view);
					dlg.setSize(dlg.getPreferredSize());
					dlg.showOkButton(false);
					dlg.showCenter();
					if (!view.isShowMessage()) {
						TSUserPropertiesUtils.setBoolean(UserLinkInfoView.ID_SHOW_USER_INFO_VIEW, false);
					}
				}
			};
			javax.swing.SwingUtilities.invokeLater(gui_update);
		}
	}

	/**
	 * Creates a link and sets its user defined attribute to true. Specialized
	 * UI classes can override this method to provide their own creation logic.
	 * This method can return null, in which case, the link will not be created.
	 * Also, we only allow links to be created between single columns. If a user
	 * wants to create a multi column foreign key, he must do it through the
	 * TableEditorDialog.
	 * 
	 * @param moveLink
	 *            if this value is non-null, then the user is moving a link from
	 *            one location to another. Otherwise, the user is a attempting
	 *            to create a new link.
	 * @param sourceId
	 *            the id of the source table for the link
	 * @param sourceCol
	 *            the name of the source column.
	 * @param destId
	 *            the id of the destination table for the link
	 * @param destCol
	 *            the name of the destination column.
	 * @return true if the link was successfully created.
	 */
	protected boolean tryCreateLink(LinkWidget moveLink, TableId sourceId, String sourceCol, TableId destId,
			String destCol) {
		boolean bresult = false;
		if (m_modeler.isPrototype(destId)) // case 1: create a new in-link to a
											// prototype table
		{
			TableMetaData desttmd = m_modeler.getTable(destId);
			// can only create links from primary keys
			if (validatePrimaryKey(sourceId, sourceCol)) {
				if (moveLink == null) {
					// make sure that a foreign key does not already exist in
					// the destination table with the same parameters
					if (validateExistingLink(desttmd, destCol, sourceId)) {
						createForeignKey(desttmd, destCol, sourceId, sourceCol);
						bresult = true;
					}
				} else {

					if (validateExistingLink(desttmd, destCol, sourceId)) {
						// move the link by first removing it and them adding it
						if (tryRemoveLink(moveLink)) {
							createForeignKey(desttmd, destCol, sourceId, sourceCol);
							bresult = true;
						}
					}
				}
			}
		} else {
			if (m_modeler.isPrototype(sourceId)) {
				// we don't allow links from prototype source to existing
				// destination
				showErrorMessage(I18N.getLocalizedMessage("Cannot create link from prototype table to existing table"));
			} else if (moveLink != null && !moveLink.isUserDefined()) {
				// don't allow moving a system link on a saved destination table
				showErrorMessage(I18N.getLocalizedMessage("Saved tables cannot be altered in the modeler"));
			} else {
				TableMetaData desttmd = m_modeler.getTable(destId);
				TableMetaData srctmd = m_modeler.getTable(sourceId);
				if (validateExistingLink(desttmd, destCol, sourceId) && validateExistingLink(srctmd, sourceCol, destId)) {
					// then we are dragging a user defined link between to
					// existing tables
					// remove the selected link from the modeler
					if (moveLink != null && moveLink.isUserDefined())
						m_modeler.removeUserLink(moveLink.getLink());

					Link link = Link.createUserDefinedLink(sourceId, sourceCol, destId, destCol);
					m_modeler.addUserLink(link);
					bresult = true;
					if (moveLink == null) {
						postUserLinkInfoMessage();
					}
				}
			}
		}
		return bresult;
		// the destination table is an existing table, so we create a user
		// defined link

		// case 2: create a new in-link to a saved table ( this is will be a
		// user defined link) from a saved table
		// case 3: create a new in-link to a saved table from a prototype (not
		// allowed)
		// case 4: move a link in a prototype table (either direction)
		// case 5: move a user defined link
		// case 6: move a defined link from a saved table ( not-allowed)
	}

	/**
	 * Called when the user attempts to remove a link by dragging it off of a
	 * table. Specialized UI classes should override to do their own checks to
	 * see if a link can be removed
	 * 
	 * @return true if the link was successfully removed
	 */
	protected boolean tryRemoveLink(LinkWidget linkw) {
		boolean bresult = false;
		// remove the selected link from the canvas
		if (linkw != null) {
			Link link = linkw.getLink();
			if (link != null) {
				TableId destid = link.getDestinationTableId();
				if (!link.isUserDefined() && m_modeler.isPrototype(destid)) {
					DbForeignKey fkey = getForeignKey(link);
					TableMetaData desttmd = m_modeler.getTable(destid);
					if (desttmd != null)
						desttmd.removeForeignKey(fkey);

					getView().removeLinkWidget(linkw);
					m_modeler.tableChanged(desttmd);
					bresult = true;
				} else if (link.isUserDefined()) {
					m_modeler.removeUserLink(link);
				} else {
					showErrorMessage(I18N.getLocalizedMessage("Saved tables cannot be altered in the modeler"));
				}
			}
		}
		return bresult;
	}

	/**
	 * Searches the given table meta data for the existance of a foreign key
	 * that has the destination column as well as the source table and column.
	 * Basically, this method is meant to prevent adding repeated definitions of
	 * the same key
	 */
	private boolean validateExistingLink(TableMetaData desttmd, String destCol, TableId sourceId) {
		if (desttmd == null)
			return true;

		// System.out.println( "validateExistingLink: destid = " +
		// desttmd.getTableId() + "  sourceId = " + sourceId );
		Collection fkeys = desttmd.getForeignKeys();
		Iterator iter = fkeys.iterator();
		while (iter.hasNext()) {
			DbForeignKey fkey = (DbForeignKey) iter.next();
			if (sourceId.equals(fkey.getSourceTableId())) {
				if (fkey.getAssignedPrimaryKeyColumnName(destCol) != null) {
					// fkey.print();
					showErrorMessage(I18N.getLocalizedMessage("Foreign key already defined"));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Validates that the given column is indeed the sole column in the primary
	 * key of the table, sourceId
	 */
	private boolean validatePrimaryKey(TableId sourceId, String sourceCol) {
		boolean bresult = false;
		TableMetaData tmd = m_modeler.getTable(sourceId);
		if (tmd == null) {
			// this should never happen
			assert (false);
		} else {
			DbKey pkey = tmd.getPrimaryKey();
			if (pkey == null) {
				showErrorMessage(I18N.getLocalizedMessage("Can only link to primary keys"));
			} else {
				if (pkey.getColumnCount() == 1) {
					if (sourceCol.equals(pkey.getColumnName(0))) {
						bresult = true;
					} else {
						showErrorMessage(I18N.getLocalizedMessage("Can only link to primary keys"));
					}
				} else {
					showErrorMessage(I18N.getLocalizedMessage("Manually_link_to_table_with_multiple_primary_keys"));
				}
			}
		}
		return bresult;
	}

}
