package com.jeta.abeille.gui.formbuilder;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;

import com.jeta.abeille.gui.update.FieldElement;
import com.jeta.abeille.gui.update.InstanceView;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * Specialization of InstanceView. Here the main change is the popup menu for
 * selecting a column
 * 
 * @author Jeff Tassin
 */
public class FormInstanceView extends InstanceView {
	/**
	 * the content menu that pops up when user right clicks on a column in the
	 * form. Allows the user to constrain the query only to that table or that
	 * subquery
	 */
	private JPopupMenu m_popupmenu;

	/**
	 * the selected column that constrains a form query to a subquery that
	 * contains this column
	 */
	private ColumnMetaData m_querylock;

	/**
	 * ctor
	 */
	public FormInstanceView(FormInstanceModel model) {
		super(model);

		m_popupmenu = new JPopupMenu();
		m_popupmenu.add(i18n_createMenuItem("Query Lock", FormNames.ID_QUERY_LOCK, null));
		m_popupmenu.add(i18n_createMenuItem("Query Unlock", FormNames.ID_QUERY_UNLOCK, null));
	}

	/**
	 * Sets the label color and adds any lable icons to the field element
	 * depending on the column We override this to set the lable icons for the
	 * anchor table to black
	 */
	public void decorate(FieldElement fe) {
		super.decorate(fe);
		FormInstanceMetaData fmd = (FormInstanceMetaData) getModel().getMetaData();
		TableId anchortable = fmd.getAnchorTable();
		ColumnMetaData cmd = fe.getColumnMetaData();
		if (anchortable != null) {
			if (anchortable.equals(cmd.getParentTableId())) {
				javax.swing.JLabel label = fe.getLabel();
				label.setForeground(java.awt.Color.black);
				JButton iconbtn = (JButton) fe.getIconButton();
				iconbtn.setVisible(false);
			} else {
				JButton iconbtn = (JButton) fe.getIconButton();
				// iconbtn.setIcon( TSGuiToolbox.loadImage( "form_link16.gif" )
				// );
				iconbtn.setVisible(true);
			}
		} else {
			System.out.println("-------------- FormInstanceView error - anchortable = null --------------- ");
		}
	}

	/**
	 * @return the popup menu for this view
	 */
	public JPopupMenu getPopupMenu() {
		return m_popupmenu;
	}

	/**
	 * @return the column used to lock a query to a subquery
	 */
	public ColumnMetaData getQueryLock() {
		return m_querylock;
	}

	/**
	 * Sets the column used to lock a query to a subquery
	 */
	public void setQueryLock(ColumnMetaData cmd) {
		m_querylock = cmd;
	}

}
