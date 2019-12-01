/* Generated By:JavaCC: Do not edit this line. TimeMaskParser.java */
package com.jeta.foundation.gui.components.datetime;

import com.jeta.foundation.gui.components.TimeMask;

public class TimeMaskParser implements TimeMaskParserConstants {

	/**
	 * hh (1-12), HH (0-23), a (AM/PM), mm (minutes), ss (seconds)
	 */
	public static void main(String args[]) throws ParseException {

	}

	public TimeMask parse() throws ParseException {
		TimeMask ddef = new TimeMask();
		subParse(ddef);
		return ddef;
	}

	final public void subParse(TimeMask tmdef) throws ParseException {
		Token x;
		String el;
		if (jj_2_1(3)) {
			x = jj_consume_token(ANY2);
			tmdef.addElement(x.image);
			subParse(tmdef);
		} else if (jj_2_2(3)) {
			el = element();
			tmdef.addElement(el);
			subParse(tmdef);
		} else if (jj_2_3(3)) {
			jj_consume_token(0);
		} else {
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public String element() throws ParseException {
		Token x;
		if (jj_2_4(3)) {
			x = jj_consume_token(hh);
			{
				if (true)
					return x.image;
			}
		} else if (jj_2_5(3)) {
			x = jj_consume_token(HH);
			{
				if (true)
					return x.image;
			}
		} else if (jj_2_6(3)) {
			x = jj_consume_token(mm);
			{
				if (true)
					return x.image;
			}
		} else if (jj_2_7(3)) {
			x = jj_consume_token(ss);
			{
				if (true)
					return x.image;
			}
		} else if (jj_2_8(3)) {
			x = jj_consume_token(a);
			{
				if (true)
					return x.image;
			}
		} else {
			jj_consume_token(-1);
			throw new ParseException();
		}
		throw new Error("Missing return statement in function");
	}

	final private boolean jj_2_1(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_1();
		jj_save(0, xla);
		return retval;
	}

	final private boolean jj_2_2(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_2();
		jj_save(1, xla);
		return retval;
	}

	final private boolean jj_2_3(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_3();
		jj_save(2, xla);
		return retval;
	}

	final private boolean jj_2_4(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_4();
		jj_save(3, xla);
		return retval;
	}

	final private boolean jj_2_5(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_5();
		jj_save(4, xla);
		return retval;
	}

	final private boolean jj_2_6(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_6();
		jj_save(5, xla);
		return retval;
	}

	final private boolean jj_2_7(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_7();
		jj_save(6, xla);
		return retval;
	}

	final private boolean jj_2_8(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_8();
		jj_save(7, xla);
		return retval;
	}

	final private boolean jj_3_7() {
		if (jj_scan_token(ss))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_6() {
		if (jj_scan_token(mm))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_5() {
		if (jj_scan_token(HH))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_1() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3_4()) {
			jj_scanpos = xsp;
			if (jj_3_5()) {
				jj_scanpos = xsp;
				if (jj_3_6()) {
					jj_scanpos = xsp;
					if (jj_3_7()) {
						jj_scanpos = xsp;
						if (jj_3_8())
							return true;
						if (jj_la == 0 && jj_scanpos == jj_lastpos)
							return false;
					} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
						return false;
				} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
					return false;
			} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_4() {
		if (jj_scan_token(hh))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_3() {
		if (jj_scan_token(0))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_2() {
		if (jj_3R_1())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_1() {
		if (jj_scan_token(ANY2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_8() {
		if (jj_scan_token(a))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	public TimeMaskParserTokenManager token_source;
	SimpleCharStream jj_input_stream;
	public Token token, jj_nt;
	private int jj_ntk;
	private Token jj_scanpos, jj_lastpos;
	private int jj_la;
	public boolean lookingAhead = false;
	private boolean jj_semLA;
	private int jj_gen;
	final private int[] jj_la1 = new int[0];
	final private int[] jj_la1_0 = {};
	final private JJCalls[] jj_2_rtns = new JJCalls[8];
	private boolean jj_rescan = false;
	private int jj_gc = 0;

	public TimeMaskParser(java.io.InputStream stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new TimeMaskParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 0; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public void ReInit(java.io.InputStream stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 0; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public TimeMaskParser(java.io.Reader stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new TimeMaskParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 0; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public void ReInit(java.io.Reader stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 0; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public TimeMaskParser(TimeMaskParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 0; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public void ReInit(TimeMaskParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 0; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	final private Token jj_consume_token(int kind) throws ParseException {
		Token oldToken;
		if ((oldToken = token).next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		if (token.kind == kind) {
			jj_gen++;
			if (++jj_gc > 100) {
				jj_gc = 0;
				for (int i = 0; i < jj_2_rtns.length; i++) {
					JJCalls c = jj_2_rtns[i];
					while (c != null) {
						if (c.gen < jj_gen)
							c.first = null;
						c = c.next;
					}
				}
			}
			return token;
		}
		token = oldToken;
		jj_kind = kind;
		throw generateParseException();
	}

	final private boolean jj_scan_token(int kind) {
		if (jj_scanpos == jj_lastpos) {
			jj_la--;
			if (jj_scanpos.next == null) {
				jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
			} else {
				jj_lastpos = jj_scanpos = jj_scanpos.next;
			}
		} else {
			jj_scanpos = jj_scanpos.next;
		}
		if (jj_rescan) {
			int i = 0;
			Token tok = token;
			while (tok != null && tok != jj_scanpos) {
				i++;
				tok = tok.next;
			}
			if (tok != null)
				jj_add_error_token(kind, i);
		}
		return (jj_scanpos.kind != kind);
	}

	final public Token getNextToken() {
		if (token.next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		jj_gen++;
		return token;
	}

	final public Token getToken(int index) {
		Token t = lookingAhead ? jj_scanpos : token;
		for (int i = 0; i < index; i++) {
			if (t.next != null)
				t = t.next;
			else
				t = t.next = token_source.getNextToken();
		}
		return t;
	}

	final private int jj_ntk() {
		if ((jj_nt = token.next) == null)
			return (jj_ntk = (token.next = token_source.getNextToken()).kind);
		else
			return (jj_ntk = jj_nt.kind);
	}

	private java.util.Vector jj_expentries = new java.util.Vector();
	private int[] jj_expentry;
	private int jj_kind = -1;
	private int[] jj_lasttokens = new int[100];
	private int jj_endpos;

	private void jj_add_error_token(int kind, int pos) {
		if (pos >= 100)
			return;
		if (pos == jj_endpos + 1) {
			jj_lasttokens[jj_endpos++] = kind;
		} else if (jj_endpos != 0) {
			jj_expentry = new int[jj_endpos];
			for (int i = 0; i < jj_endpos; i++) {
				jj_expentry[i] = jj_lasttokens[i];
			}
			boolean exists = false;
			for (java.util.Enumeration enum1 = jj_expentries.elements(); enum1.hasMoreElements();) {
				int[] oldentry = (int[]) (enum1.nextElement());
				if (oldentry.length == jj_expentry.length) {
					exists = true;
					for (int i = 0; i < jj_expentry.length; i++) {
						if (oldentry[i] != jj_expentry[i]) {
							exists = false;
							break;
						}
					}
					if (exists)
						break;
				}
			}
			if (!exists)
				jj_expentries.addElement(jj_expentry);
			if (pos != 0)
				jj_lasttokens[(jj_endpos = pos) - 1] = kind;
		}
	}

	final public ParseException generateParseException() {
		jj_expentries.removeAllElements();
		boolean[] la1tokens = new boolean[8];
		for (int i = 0; i < 8; i++) {
			la1tokens[i] = false;
		}
		if (jj_kind >= 0) {
			la1tokens[jj_kind] = true;
			jj_kind = -1;
		}
		for (int i = 0; i < 0; i++) {
			if (jj_la1[i] == jj_gen) {
				for (int j = 0; j < 32; j++) {
					if ((jj_la1_0[i] & (1 << j)) != 0) {
						la1tokens[j] = true;
					}
				}
			}
		}
		for (int i = 0; i < 8; i++) {
			if (la1tokens[i]) {
				jj_expentry = new int[1];
				jj_expentry[0] = i;
				jj_expentries.addElement(jj_expentry);
			}
		}
		jj_endpos = 0;
		jj_rescan_token();
		jj_add_error_token(0, 0);
		int[][] exptokseq = new int[jj_expentries.size()][];
		for (int i = 0; i < jj_expentries.size(); i++) {
			exptokseq[i] = (int[]) jj_expentries.elementAt(i);
		}
		return new ParseException(token, exptokseq, tokenImage);
	}

	final public void enable_tracing() {
	}

	final public void disable_tracing() {
	}

	final private void jj_rescan_token() {
		jj_rescan = true;
		for (int i = 0; i < 8; i++) {
			JJCalls p = jj_2_rtns[i];
			do {
				if (p.gen > jj_gen) {
					jj_la = p.arg;
					jj_lastpos = jj_scanpos = p.first;
					switch (i) {
					case 0:
						jj_3_1();
						break;
					case 1:
						jj_3_2();
						break;
					case 2:
						jj_3_3();
						break;
					case 3:
						jj_3_4();
						break;
					case 4:
						jj_3_5();
						break;
					case 5:
						jj_3_6();
						break;
					case 6:
						jj_3_7();
						break;
					case 7:
						jj_3_8();
						break;
					}
				}
				p = p.next;
			} while (p != null);
		}
		jj_rescan = false;
	}

	final private void jj_save(int index, int xla) {
		JJCalls p = jj_2_rtns[index];
		while (p.gen > jj_gen) {
			if (p.next == null) {
				p = p.next = new JJCalls();
				break;
			}
			p = p.next;
		}
		p.gen = jj_gen + xla - jj_la;
		p.first = token;
		p.arg = xla;
	}

	static final class JJCalls {
		int gen;
		Token first;
		int arg;
		JJCalls next;
	}

}
