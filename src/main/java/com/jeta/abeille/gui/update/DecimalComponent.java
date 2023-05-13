package com.jeta.abeille.gui.update;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JTextField;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a decimal component in the update frame. (i.e. DECIMAL
 * or NUMERIC )
 * 
 * @author Jeff Tassin
 */
public class DecimalComponent extends TextComponentBase {
	/** the java.sql.Type - should be either DECIMAL or NUMERIC */
	private int m_sqltype;

	/**
	 * ctor
	 */
	public DecimalComponent(String fieldName, int sqltype, int size) {
		super(fieldName, sqltype, size);
		m_sqltype = sqltype;

		if (m_sqltype != java.sql.Types.DECIMAL && m_sqltype != java.sql.Types.NUMERIC) {
			assert (false);
		}

		JTextField field = new InstanceTextField(this);
		setComponent(field);
	}

	/**
	 * Sets the value represented by this component into the prepared statement
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		if (isNull()) {
			pstmt.setNull(count, m_sqltype);
		} else {
			String sval = toString();
			if (sval == null)
				sval = "";

			sval = sval.trim();

			// just try to set as string here and let the database handle the
			// conversion
			if (sval.length() == 0)
				pstmt.setString(count, "");
			else
				pstmt.setString(count, sval);
		}
	}

	/**
	 * Gets the value that is displayed in the component and calls setValue on
	 * this object.
	 */
	public void syncValue() {
		String sval = TSUtils.fastTrim(toString());
		if (sval == null || sval.length() == 0)
			setValue(null);
		else
			setValue(sval);
	}

}
