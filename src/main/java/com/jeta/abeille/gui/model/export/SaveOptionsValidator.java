package com.jeta.abeille.gui.model.export;

import java.io.File;
import javax.swing.JOptionPane;

import com.jeta.foundation.i18n.I18N;
import com.jeta.open.rules.AbstractRule;
import com.jeta.open.rules.RuleResult;

/**
 * Validator for the SaveOptionsView
 * 
 * @author Jeff Tassin
 */
public class SaveOptionsValidator extends AbstractRule {

	/**
	 * JETARule implementation. Validates the data in the view.
	 * 
	 * @param params
	 *            requires the first element to be the SaveOptionsView we are
	 *            validating.
	 */
	public RuleResult check(Object[] params) {
		SaveOptionsView view = (SaveOptionsView) params[0];
		String filename = view.getPath();
		try {
			if (filename.length() == 0) {
				return new RuleResult(I18N.getLocalizedMessage("A valid file name is required"));
			}

			File f = new File(filename);
			if (f.isDirectory()) {
				return new RuleResult(I18N.getLocalizedMessage("A valid file name is required"));
			}

			String dir = f.getParent();
			if (dir == null || dir.length() == 0) {
				return new RuleResult(I18N.getLocalizedMessage("Invalid directory"));
			}

			File parent = new File(dir);
			if (parent.exists() && parent.isDirectory()) {
				// path to file is valid
				if (f.exists()) {
					// show message dialog if we are overwriting an existing
					// file
					String title = I18N.getLocalizedMessage("Warning");
					String msg = I18N.getLocalizedMessage("File_exists_overwrite?");
					int result = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
					if (result != JOptionPane.YES_OPTION) {
						return RuleResult.FAIL;
					}
				}
				return RuleResult.SUCCESS;
			} else {
				return new RuleResult(I18N.getLocalizedMessage("Invalid directory"));
			}

		} catch (Exception e) {
			return new RuleResult(e.getMessage());
		}
	}
}
