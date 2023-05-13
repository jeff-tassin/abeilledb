package com.jeta.abeille.gui.sql.input;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSStatusBar;

import com.jeta.foundation.i18n.I18N;

/**
 * Dialog that allows the user to enter constraints for a 'prepared' SQL
 * command.
 * 
 * @author Jeff Tassin
 */
public class SQLInputDialog extends TSDialog {

	public SQLInputDialog(java.awt.Frame frame, boolean modal) {
		super(frame, modal);
	}

	public SQLInputDialog(java.awt.Dialog dlg, boolean modal) {
		super(dlg, modal);
	}

	/**
	 * Initializes this dialog with the given data model
	 */
	public void initialize(SQLInputModel inputmodel) {
		setTitle(I18N.getLocalizedMessage("Enter Constraints"));
		final SQLInputView view = new SQLInputView(inputmodel);
		JScrollPane scroll = new JScrollPane(view);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		TSStatusBar statusbar = new TSStatusBar();
		TSCell cell1 = new TSCell("main.cell", "#####");
		cell1.setMain(true);
		statusbar.addCell(cell1);
		// cell1.setHorizontalAlignment( javax.swing.SwingConstants.LEFT );
		// cell1.setText( I18N.getLocalizedMessage("Text_delimiters_required")
		// );

		TSPanel main = new TSPanel();
		main.setLayout(new BorderLayout());
		main.add(scroll, BorderLayout.CENTER);
		// main.add( statusbar, BorderLayout.SOUTH );
		setPrimaryPanel(main);

		setInitialFocusComponent(view.getFirstEntryComponent());

		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				view.doLayout();
			}
		});

	}
}
