package com.jeta.abeille.gui.update;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JTextField;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents an integral component in the update frame. An integral
 * component shows a number with no decimal digits
 * 
 * @author Jeff Tassin
 */
public class IntegralComponent extends TextComponentBase {
	/** the java.sql.Type - should be either BIGINT,INTEGER,SMALLINT, or TINYINT */
	private int m_sqltype;

	public IntegralComponent(String fieldName, int sqltype, int size) {
		super(fieldName, sqltype, size);
		m_sqltype = sqltype;

		if (m_sqltype != java.sql.Types.BIGINT && m_sqltype != java.sql.Types.INTEGER
				&& m_sqltype != java.sql.Types.SMALLINT && m_sqltype != java.sql.Types.TINYINT) {
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
		String sval = toString();
		if (sval == null)
			sval = "";

		sval = sval.trim();
		if (sval.length() == 0)
			setValue(null);

		if (isNull()) {
			pstmt.setNull(count, m_sqltype);
		} else {
			try {
				if (m_sqltype == java.sql.Types.BIGINT) {
					if (sval.length() == 0)
						pstmt.setLong(count, 0);
					else
						pstmt.setLong(count, Long.parseLong(sval));
				} else {
					if (sval.length() == 0)
						pstmt.setInt(count, 0);
					else
						pstmt.setInt(count, Integer.parseInt(sval));
				}
			} catch (NumberFormatException nfe) {
				String param2 = null;
				if (m_sqltype == java.sql.Types.BIGINT)
					param2 = I18N.getLocalizedMessage("long");
				else
					param2 = I18N.getLocalizedMessage("integer");

				String msg = I18N.format("Unable_to_parse_to_2", toString(), param2);
				throw new SQLException(msg);
			}
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
