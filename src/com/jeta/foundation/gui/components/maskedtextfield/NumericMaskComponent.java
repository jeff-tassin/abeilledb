package com.jeta.foundation.gui.components.maskedtextfield;

import java.text.DecimalFormat;
import java.awt.event.*;

/**
 * This is a component that makes up a TSMaskedTextField. This component can
 * accept numeric keyboard inputs. Also, this component has two modes. mode 1.
 * The entire number value is selected. As the user enters values via keyboard,
 * the digits are shifted right. mode 2. Individual digits in the value are
 * selected. The user can hit left or right to select other digits, or up and
 * down to change the selected digit We currently only support up to 9 digit
 * numbers
 * 
 * @author Jeff Tassin
 */
public class NumericMaskComponent extends InputMaskComponent {
	private boolean m_bSelected; // whether this component is selected or not
	private int m_inputmode; // the current input mode
	private long m_value; // the current value
	private int m_numdigits; // the number of digits to show
	private int m_inputpos; // if we are in SINGLE_DIGIT_INPUT mode, this is the
							// currently active digit (zero based)

	/** as user types, only the single selected digit is changed */
	public static int SINGLE_DIGIT_INPUT = 1;
	/** as user types, the digits are changed from right to left */
	public static int SHIFT_DIGIT_INPUT = 2;
	private static int MAX_DEFINED_FORMAT = 8;
	private static DecimalFormat[] m_formats;
	private static long[] m_maxvalues;

	public static final String MODECHANGE_EVT = "modechangeevent";

	static {
		// let's handle the most common formats for numerics
		m_formats = new DecimalFormat[9];
		m_formats[0] = new DecimalFormat("0");
		m_formats[1] = new DecimalFormat("00");
		m_formats[2] = new DecimalFormat("000");
		m_formats[3] = new DecimalFormat("0000");
		m_formats[4] = new DecimalFormat("00000");
		m_formats[5] = new DecimalFormat("000000");
		m_formats[6] = new DecimalFormat("0000000");
		m_formats[7] = new DecimalFormat("00000000");
		m_formats[MAX_DEFINED_FORMAT] = new DecimalFormat("000000000");

		m_maxvalues = new long[9];
		m_maxvalues[0] = 9;
		m_maxvalues[1] = 99;
		m_maxvalues[2] = 999;
		m_maxvalues[3] = 9999;
		m_maxvalues[4] = 99999;
		m_maxvalues[5] = 999999;
		m_maxvalues[6] = 9999999;
		m_maxvalues[7] = 99999999;
		m_maxvalues[MAX_DEFINED_FORMAT] = 999999999;
	}

	{
		m_inputmode = SHIFT_DIGIT_INPUT;
		m_inputpos = 0;
	}

	public NumericMaskComponent() {
		m_numdigits = 0;
	}

	public NumericMaskComponent(int numDigits) {
		m_numdigits = numDigits;
	}

	/**
	 * @return the minimum value for a digit at the given input position Note:
	 *         this is not always zero. For example, in a time mask, the min
	 *         digit can be 1 (as in 12:00 AM)
	 */
	public int getMinDigit(int inputPos) {
		return 0;
	}

	/**
	 * @return the maximum value for a digit at the given input position Note:
	 *         this is not always zero. For example, in a time mask, the max
	 *         digit can be 5 (as in 59 minutes)
	 */
	public int getMaxDigit(int inputPos) {
		return 9;
	}

	/**
	 * @return the min value that can be displayed by this component
	 */
	public long getMinValue() {
		return 0;
	}

	/**
	 * @return the max value that can be displayed by this component
	 */
	public long getMaxValue() {
		return m_maxvalues[m_numdigits - 1];
	}

	/**
	 * @return the value for this component
	 */
	public long getValue() {
		return m_value;
	}

	/**
	 * Decrements the selected value by one. This is done against the entire
	 * value and not a single digit.
	 */
	public void decrement() {
		if (getInputMode() == SINGLE_DIGIT_INPUT)
			decrementDigit();
		else {
			long minvalue = getMinValue();
			long val = m_value - 1;
			if (val < minvalue) {
				setValue(getMaxValue());
			} else {
				setValue(val);
			}
		}
	}

	/**
	 * Increments the selected value by one. This is done against the entire
	 * value and not a single digit.
	 */
	public void increment() {
		if (getInputMode() == SINGLE_DIGIT_INPUT)
			incrementDigit();
		else {
			long maxvalue = getMaxValue();
			setValue(m_value + 1);
			if (m_value > maxvalue)
				setValue(getMinValue());
		}
	}

	/**
	 * Increments the selected digit. If the selected digit is 9, then
	 * automatically roll to zero.
	 */
	public void incrementDigit() {
		DecimalFormat format = getDecimalFormat();
		String sval = format.format(m_value);
		int digit = Character.digit(sval.charAt(m_inputpos), 10);
		if (digit < getMaxDigit(m_inputpos))
			digit++;
		else
			digit = getMinDigit(m_inputpos);

		char cdigit = Character.forDigit(digit, 10);
		sval = sval.substring(0, m_inputpos) + cdigit + sval.substring(m_inputpos + 1, sval.length());
		setValue(Long.parseLong(sval));
	}

	/**
	 * Decrements the selected digit. If the selected digit is 0, then
	 * automatically roll to 9
	 */
	public void decrementDigit() {
		DecimalFormat format = getDecimalFormat();
		String sval = format.format(m_value);
		int digit = Character.digit(sval.charAt(m_inputpos), 10);
		if (digit > getMinDigit(m_inputpos))
			digit--;
		else
			digit = getMaxDigit(m_inputpos);

		char cdigit = Character.forDigit(digit, 10);
		sval = sval.substring(0, m_inputpos) + cdigit + sval.substring(m_inputpos + 1, sval.length());
		setValue(Long.parseLong(sval));
	}

	/**
	 * @return a decimal format object for this mask.
	 */
	DecimalFormat getDecimalFormat() {
		return m_formats[m_numdigits - 1];
	}

	/**
	 * @return the current input mode (either SINGLE_DIGIT_INPUT or
	 *         SHIFT_DIGIT_INPUT)
	 */
	public int getInputMode() {
		return m_inputmode;
	}

	/**
	 * @return the number of digits to show in this mask
	 */
	public int getNumDigits() {
		return m_numdigits;
	}

	/**
	 * Each mask component is responsible for handling keyboard events. It can
	 * override the event handling for the mask control. For this class, we
	 * override the left and right commands if we are in single digit mode. This
	 * allows us to move the focus to the next/prev digit. We also override
	 * up/down if we are in single digit mode. This allows up to increment or
	 * decrement the selected digit. We override up/down when in SHIFT_DIGIT
	 * mode, to add/subtract one to the overall value
	 */
	public boolean handleKeyEvent(KeyEvent e) {
		boolean bresult = false;

		// @todo don't use key codes, use action names so we can change to emacs
		// like commands

		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			toggleInputMode();
			return true;
		}

		if (getInputMode() == SINGLE_DIGIT_INPUT) {
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				if (m_inputpos > 0) {
					m_inputpos--;
					bresult = true;
				}
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (m_inputpos < (m_numdigits - 1)) {
					m_inputpos++;
					bresult = true;
				}
			} else if (e.getKeyCode() == KeyEvent.VK_UP) {
				// increment();
				bresult = true;
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				// decrement();
				bresult = true;
			}
		} else if (getInputMode() == SHIFT_DIGIT_INPUT) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				// increment();
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				// decrement();
			} else {
				char keyChar = e.getKeyChar();
				if (keyChar >= '0' && keyChar <= '9') {
					// okay, if we have a valid number
					String sval = String.valueOf(m_value) + String.valueOf(keyChar);
					if (sval.length() > m_numdigits)
						sval = sval.substring(sval.length() - m_numdigits, sval.length());

					setValue(Long.parseLong(sval));
					bresult = true;
				}
			}
		}
		return bresult;
	}

	/**
    *
    */
	public String getPreSelection() {
		if (m_inputmode == SHIFT_DIGIT_INPUT)
			return "";
		else {
			String sval = toString();
			sval = sval.substring(0, m_inputpos);
			return sval;
		}
	}

	/**
	 * @return the selected string within this component. Because some
	 *         components allow selecting single characters (or sets of
	 *         characters) within the component, we return the string that
	 *         determines the selection.
	 */
	public String getSelection() {
		if (m_inputmode == SHIFT_DIGIT_INPUT)
			return toString();
		else {
			String sval = toString();
			return sval.substring(m_inputpos, m_inputpos + 1);
		}
	}

	/**
	 * Sets the digits for this mask
	 */
	protected void setDigits(int digits) {
		m_numdigits = digits;
	}

	/**
	 * The method is called when this component gets focus from the component to
	 * the immediate right
	 */
	public void setFocusLeft() {
		setInputPos(getNumDigits() - 1);
		setSelected(true);
	}

	/**
	 * This method is called when this component gets focus from the component
	 * to the immediate left
	 */
	public void setFocusRight() {
		setInputPos(0);
		setSelected(true);
	}

	/**
	 * Sets the input mode.
	 */
	public void setInputMode(int mode) {
		if (mode == SHIFT_DIGIT_INPUT || mode == SINGLE_DIGIT_INPUT) {
			m_inputmode = mode;
		} else {
			throw new IllegalArgumentException("Invalid input mode: " + mode);
		}
	}

	/**
	 * Sets the input position that designates the currently active digit when
	 * we are in SINGLE_DIGIT_INPUT_MODE
	 */
	public void setInputPos(int pos) {
		if (pos == -1)
			pos = getNumDigits() - 1;
		m_inputpos = pos;
	}

	/**
	 * Set's this item to selected. This method also allows the caller to
	 * specify a digit within the string to be selected. If a derived class
	 * supports this behavior, it can override this method. Otherwise, the
	 * default is just to select the entier component
	 */
	public void setSelected(boolean bSelected, int charPos) {
		setSelected(bSelected);
		setInputPos(charPos);
	}

	/**
	 * Sets the current value and notifies listeners of the change
	 */
	public void setValue(long value) {
		m_value = value;
		// @todo check this logic. is this really what we want to do
		String sval = toString();
		if (sval.length() > m_numdigits)
			sval = sval.substring(0, m_numdigits);
		m_value = Long.parseLong(sval);
		ActionEvent evt = new ActionEvent(this, 0, VALUE_CHANGE_EVENT);
		notifyListeners(evt);
	}

	/**
	 * Toggles the input mode from SHIFT_DIGIT_INPUT to SINGLE_DIGIT_INPUT and
	 * vice-versa
	 * 
	 */
	public void toggleInputMode() {
		// toggles the input mode from SHIFT_DIGIT_INPUT to SINGLE_DIGIT_INPUT
		if (m_inputmode == SHIFT_DIGIT_INPUT)
			m_inputmode = SINGLE_DIGIT_INPUT;
		else
			m_inputmode = SHIFT_DIGIT_INPUT;

		// allow any listeners to respond to changes
		ActionEvent evt = new ActionEvent(this, 0, MODECHANGE_EVT);
		notifyListeners(evt);
	}

	/**
	 * @return the string representation for this component
	 */
	public String toString() {
		DecimalFormat format = getDecimalFormat();
		return format.format(m_value);
	}

}
