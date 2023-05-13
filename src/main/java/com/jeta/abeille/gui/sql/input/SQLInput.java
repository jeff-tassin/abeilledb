package com.jeta.abeille.gui.sql.input;

import com.jeta.abeille.gui.sql.TokenInfo;

public interface SQLInput {
	public String getName();

	public String getValue();

	public void setValue(String value);

	/**
	 * @return the constraint token entered by the user that identifies the type
	 *         of constraint operator (i.e. >, <, ==, <>, <=, LIKE etc )
	 */
	public TokenInfo getOperatorToken();

	/**
	 * @return the token entered by the user that identifies the type of input.
	 *         Either ? or @ is accepted. A ? means that the value will
	 *         automatically be 'delimited' as text. A @ means that the input is
	 *         inserted into the SQL command exactly as typed by the user
	 */
	public TokenInfo getInputToken();
}
