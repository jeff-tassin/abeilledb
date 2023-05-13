package com.jeta.abeille.gui.update;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JTextField;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a real component in the update frame. (i.e. FLOAT, REAL
 * or DOUBLE )
 * 
 * @author Jeff Tassin
 */
public class RealComponent extends TextComponentBase {
	/** the java.sql.Type - should be either FLOAT, REAL, or DOUBLE */
	private int m_sqltype;

	public RealComponent(String fieldName, int sqltype, int size) {
		super(fieldName, sqltype, size);
		m_sqltype = sqltype;

		if (m_sqltype != java.sql.Types.FLOAT && m_sqltype != java.sql.Types.REAL && m_sqltype != java.sql.Types.DOUBLE) {
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

			try {
				if (m_sqltype == java.sql.Types.DOUBLE) {
					if (sval.length() == 0)
						pstmt.setDouble(count, 0.0);
					else
						pstmt.setDouble(count, Double.parseDouble(sval));
				} else // assume FLOAT
				{
					if (sval.length() == 0)
						pstmt.setFloat(count, 0.0f);
					else
						pstmt.setFloat(count, Float.parseFloat(sval));
				}

			} catch (NumberFormatException nfe) {
				String param2 = null;
				if (m_sqltype == java.sql.Types.DOUBLE)
					param2 = I18N.getLocalizedMessage("double");
				else
					param2 = I18N.getLocalizedMessage("float");

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
