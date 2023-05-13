package com.jeta.plugins.foundation.locale;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.lang.*;

import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.components.maskedtextfield.*;

/**
 * This is a plugin used to build a date field for a specified locale
 * 
 */
public class TSDateFieldHandler {
	public TSDateFieldHandler() {

	}

	/**
	 * Builds a time field. This takes only one parameter (TSDateField)
	 */
	public void build(TSMaskedTextField df) {
		StaticStringMask datedelimiter = new StaticStringMask("-");
		StaticStringMask margin = new StaticStringMask(" ");
		df.addMask(margin);
		NumericMaskComponent month = new DateMaskComponent(DateMaskComponent.MONTH);
		df.addMask(month);
		df.addMask(datedelimiter);
		NumericMaskComponent day = new DateMaskComponent(DateMaskComponent.DAY);
		df.addMask(day);
		df.addMask(datedelimiter);
		NumericMaskComponent year = new DateMaskComponent(DateMaskComponent.YEAR);
		df.addMask(year);
		df.addMask(margin);
	}

	/**
	 * Builds a time field. This takes only one parameter (TSDateField)
	 */
	public void buildFrench(TSMaskedTextField df) {
		// add the masks for this component
		// [sp][hour mask][:][min mask][:][secs mask][sp][AMPM mask][sp]
		// french dates are day.month.year
		// french times are hour:minute:sec
		StaticStringMask datedelimiter = new StaticStringMask(".");
		StaticStringMask margin = new StaticStringMask(" ");
		df.addMask(margin);
		NumericMaskComponent day = new NumericMaskComponent(2);
		day.setId(TSDateField.DAY_MASK);
		df.addMask(day);
		df.addMask(datedelimiter);
		NumericMaskComponent month = new NumericMaskComponent(2);
		month.setId(TSDateField.MONTH_NUMBER_MASK);
		df.addMask(month);
		df.addMask(datedelimiter);
		NumericMaskComponent year = new NumericMaskComponent(4);
		year.setId(TSDateField.YEAR_MASK);
		df.addMask(year);
		df.addMask(margin);
	}

}
