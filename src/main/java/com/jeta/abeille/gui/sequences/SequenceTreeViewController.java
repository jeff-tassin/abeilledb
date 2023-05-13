package com.jeta.abeille.gui.sequences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.sequences.Sequence;
import com.jeta.abeille.database.sequences.SequenceService;

import com.jeta.abeille.gui.model.ObjectTree;
import com.jeta.abeille.gui.model.ObjectTreeModel;
import com.jeta.abeille.gui.model.ObjectTreeNode;

import com.jeta.abeille.gui.utils.DropDialog;
import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This is the main controller for the SequenceTreeView It responds to user
 * events from the SequenceTreeView.
 * 
 * @author Jeff Tassin
 */
public class SequenceTreeViewController extends TSController {
	private SequenceTree m_view;

	/**
	 * ctor
	 */
	public SequenceTreeViewController(SequenceTree view) {
		super(view);
		m_view = view;
		assignAction(SequenceNames.ID_NEW_SEQUENCE, new AddSequenceAction());
		assignAction(SequenceNames.ID_EDIT_SEQUENCE, new EditSequenceAction());
		assignAction(SequenceNames.ID_DROP_SEQUENCE, new DropSequenceAction());
	}

	/**
	 * Displays the selected sequence in a dialog;
	 */
	private void editSequence(Sequence seq) {
		final TSConnection tsconn = m_view.getSelectedConnection();
		if (tsconn == null)
			return;

		try {
			/** reload the sequence from the database to get the latest values */
			if (seq != null) {
				SequenceService ssrv = (SequenceService) m_view.getSelectedConnection().getImplementation(
						SequenceService.COMPONENT_ID);
				seq = ssrv.getSequence(seq);
			}
		} catch (SQLException sqe) {
			SQLErrorDialog.showErrorDialog(m_view.getTree(), sqe, null);
			return;
		}

		String dlgmsg = null;
		if (seq == null) {
			dlgmsg = I18N.getLocalizedMessage("Create Sequence");
		} else {
			dlgmsg = I18N.getLocalizedMessage("Modify Sequence");
		}

		SQLCommandDialog dlg = SQLCommandDialog.createDialog(m_view.getSelectedConnection(), m_view.getTree(), true);
		dlg.setMessage(dlgmsg);

		Catalog selectedcatalog = null;
		Schema selectedschema = null;
		if (seq == null) {
			ObjectTree tree = m_view.getTree();
			selectedschema = tree.getSchema(tree.getSelectedNode());
			selectedcatalog = tree.getCatalog(tree.getSelectedNode());
			if (selectedcatalog == null)
				selectedcatalog = tsconn.getCurrentCatalog();
		} else {
			selectedschema = seq.getSchema();
			selectedcatalog = seq.getCatalog();
		}

		assert (selectedschema != null);
		assert (selectedcatalog != null);

		final SequenceView view = new SequenceView(tsconn, seq, selectedcatalog, selectedschema);
		if (seq == null)
			dlg.addValidator(view);

		dlg.setPrimaryPanel(view);
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());

		final Sequence oldseq = seq;
		dlg.addDialogListener(new SQLDialogListener() {
			public boolean cmdOk() throws SQLException {
				Sequence newseq = view.createSequence();
				SequenceService ssrv = (SequenceService) tsconn.getImplementation(SequenceService.COMPONENT_ID);
				if (oldseq == null) {
					// we are creating a new sequence
					ssrv.createSequence(newseq);
				} else {
					// we are modifying an existing sequence
					ssrv.modifySequence(newseq, oldseq);
				}
				return true;
			}
		});

		dlg.showCenter();
		if (dlg.isOk()) {
			// update the GUI
			if (oldseq == null) {
				Sequence newseq = view.createSequence();
				ObjectTreeModel model = m_view.getModel();
				ObjectTreeNode parent = model.getClassNode(tsconn, newseq.getCatalog(), newseq.getSchema(),
						SequenceTreeModel.CLASS_KEY);
				if (parent != null) {
					ObjectTreeNode newnode = new ObjectTreeNode(newseq);
					model.insertNodeInto(newnode, parent, 0);
					model.sortNode(parent);
				}
			}
		}
	}

	/**
	 * Action handler for the add sequence command
	 */
	public class AddSequenceAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			editSequence(null);
		}
	}

	/**
	 * Drops the selected sequence from the database
	 */
	public class DropSequenceAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final TSConnection tsconn = m_view.getSelectedConnection();
			final Sequence seq = m_view.getSelectedSequence();
			if (tsconn != null && seq != null) {
				String msg = I18N.format("Drop_1", seq.getName());
				final DropDialog dlg = DropDialog.createDropDialog(tsconn, m_view.getTree(), true);
				dlg.setMessage(msg);

				if (!tsconn.supportsSchemas())
					dlg.setCascadeEnabled(false);

				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						SequenceService ssrv = (SequenceService) tsconn.getImplementation(SequenceService.COMPONENT_ID);
						ssrv.dropSequence(seq, dlg.isCascade());
						return true;
					}
				});

				dlg.showCenter();
				if (dlg.isOk()) {
					// update GUI
					ObjectTreeModel model = m_view.getModel();
					model.removeNodeFromParent(m_view.getSelectedNode());
				}
			}
		}
	}

	/**
	 * Action handler for edit sequence command.
	 */
	public class EditSequenceAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Sequence seq = m_view.getSelectedSequence();
			if (seq != null) {
				editSequence(seq);
			}
		}
	}

}
