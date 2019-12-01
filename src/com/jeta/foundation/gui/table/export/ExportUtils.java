package com.jeta.foundation.gui.table.export;

/**
 * Utilities used by some of the export classes
 * 
 * @author Jeff Tassin
 */
public class ExportUtils {

	/**
	 * Searches the tokenExpression for the location of the token string. If the
	 * string is found, then the substring to the left of the token is returned.
	 * If nothing is found, then an emtpy string is returned.
	 * 
	 * @param token
	 *            the string to search for
	 * @param tokenExpression
	 *            the expression to search
	 * @return the substring to the left of token if it is found
	 */
	public static String parseLeft(String token, String tokenExpression) {
		String result = "";
		int pos = tokenExpression.indexOf(token);
		if (pos >= 0)
			result = tokenExpression.substring(0, pos);

		return result;
	}

	/**
	 * Searches the tokenExpression for the location of the token string. If the
	 * string is found, then the substring to the right of the token is
	 * returned. If nothing is found, then an emtpy string is returned.
	 * 
	 * @param token
	 *            the string to search for
	 * @param tokenExpression
	 *            the expression to search
	 * @return the substring to the right of token if it is found
	 */
	public static String parseRight(String token, String tokenExpression) {
		String result = "";
		int pos = tokenExpression.indexOf(token);
		if (pos >= 0)
			result = tokenExpression.substring(pos + token.length(), tokenExpression.length());

		return result;
	}
}
