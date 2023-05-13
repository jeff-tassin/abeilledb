package com.jeta.plugins.foundation.locale;

import com.jeta.foundation.gui.components.AMPMMaskComponent;
import com.jeta.foundation.gui.components.StaticStringMask;
import com.jeta.foundation.gui.components.TimeElement;
import com.jeta.foundation.gui.components.TimeMaskComponent;
import com.jeta.foundation.gui.components.TimeNames;
import com.jeta.foundation.gui.components.maskedtextfield.TSMaskedTextField;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

/**
 * This is a plugin used to build a time field for a specified locale
 * 
 */
public class TSTimeFieldHandler {

	public TSTimeFieldHandler() {

	}

	/**
	 * Builds a time field. This takes only one parameter (TSTimeField)
	 */
	public void build(TSMaskedTextField tf) {
		boolean b12hour = true;
		try {
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			Boolean bval = new Boolean(userprops.getProperty(TimeNames.ID_24HOUR));
			b12hour = !bval.booleanValue();
		} catch (Exception e) {

		}

		// add the masks for this component
		// [sp][hour mask][:][min mask][:][secs mask][sp][AMPM mask][sp]

		// french dates are day.month.year
		// french times are hour:minute:sec
		StaticStringMask margin = new StaticStringMask(" ");
		tf.addMask(margin);
		TimeMaskComponent hour = new TimeMaskComponent(TimeElement.HOURS_12);
		tf.addMask(hour);
		// @todo change delimited to locale specified
		StaticStringMask tmdelimiter = new StaticStringMask(":");
		tf.addMask(tmdelimiter);
		TimeMaskComponent min = new TimeMaskComponent(TimeElement.MINUTES);
		tf.addMask(min);
		tf.addMask(tmdelimiter);
		TimeMaskComponent sec = new TimeMaskComponent(TimeElement.SECONDS);
		tf.addMask(sec);
		StaticStringMask sp = new StaticStringMask(" ");
		tf.addMask(sp);

		if (b12hour) {
			AMPMMaskComponent ampm = new AMPMMaskComponent(AMPMMaskComponent.AM);
			tf.addMask(ampm);
		}

		tf.addMask(margin);
	}

}
