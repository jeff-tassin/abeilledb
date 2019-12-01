/**
 * Title:        <p>
 * Description:  <p>
 * Copyright:    Copyright (c) <p>
 * Company:      <p>
 * @author
 * @version 1.0
 */
package com.jeta.abeille.database.utils;

public class VarCharCleanser implements Cleanser {

	public VarCharCleanser() {

	}

	public String cleanse(String value) {
		if (value != null) {
			if (value.charAt(0) != '\'')
				value = '\'' + value;

			if (value.charAt(value.length() - 1) != '\'')
				value += '\'';
		}
		return value;
	}

}
