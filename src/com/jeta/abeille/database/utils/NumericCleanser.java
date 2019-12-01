/**
 * Title:        <p>
 * Description:  <p>
 * Copyright:    Copyright (c) <p>
 * Company:      <p>
 * @author
 * @version 1.0
 */
package com.jeta.abeille.database.utils;

public class NumericCleanser implements Cleanser {

	public NumericCleanser() {

	}

	public String cleanse(String value) {
		if (value.charAt(0) == '$')
			value = value.substring(1, value.length());

		return value;
	}

}
