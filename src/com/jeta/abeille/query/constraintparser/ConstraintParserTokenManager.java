/* Generated By:JavaCC: Do not edit this line. ConstraintParserTokenManager.java */
package com.jeta.abeille.query.constraintparser;

import java.util.ArrayList;
import java.util.Iterator;
import com.jeta.abeille.query.*;
import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

public class ConstraintParserTokenManager implements ConstraintParserConstants {
	public java.io.PrintStream debugStream = System.out;

	public void setDebugStream(java.io.PrintStream ds) {
		debugStream = ds;
	}

	private final int jjStopStringLiteralDfa_0(int pos, long active0) {
		switch (pos) {
		case 0:
			if ((active0 & 0x3000000L) != 0L) {
				jjmatchedKind = 26;
				return 47;
			}
			if ((active0 & 0x1000L) != 0L)
				return 19;
			return -1;
		case 1:
			if ((active0 & 0x3000000L) != 0L) {
				jjmatchedKind = 26;
				jjmatchedPos = 1;
				return 47;
			}
			return -1;
		case 2:
			if ((active0 & 0x1000000L) != 0L) {
				if (jjmatchedPos < 1) {
					jjmatchedKind = 26;
					jjmatchedPos = 1;
				}
				return -1;
			}
			if ((active0 & 0x2000000L) != 0L) {
				jjmatchedKind = 26;
				jjmatchedPos = 2;
				return 47;
			}
			return -1;
		case 3:
			if ((active0 & 0x2000000L) != 0L)
				return 47;
			if ((active0 & 0x1000000L) != 0L) {
				if (jjmatchedPos < 1) {
					jjmatchedKind = 26;
					jjmatchedPos = 1;
				}
				return -1;
			}
			return -1;
		case 4:
			if ((active0 & 0x1000000L) != 0L) {
				if (jjmatchedPos < 1) {
					jjmatchedKind = 26;
					jjmatchedPos = 1;
				}
				return -1;
			}
			return -1;
		case 5:
			if ((active0 & 0x1000000L) != 0L) {
				if (jjmatchedPos < 1) {
					jjmatchedKind = 26;
					jjmatchedPos = 1;
				}
				return -1;
			}
			return -1;
		default:
			return -1;
		}
	}

	private final int jjStartNfa_0(int pos, long active0) {
		return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
	}

	private final int jjStopAtPos(int pos, int kind) {
		jjmatchedKind = kind;
		jjmatchedPos = pos;
		return pos + 1;
	}

	private final int jjStartNfaWithStates_0(int pos, int kind, int state) {
		jjmatchedKind = kind;
		jjmatchedPos = pos;
		try {
			curChar = input_stream.readChar();
		} catch (java.io.IOException e) {
			return pos + 1;
		}
		return jjMoveNfa_0(state, pos + 1);
	}

	private final int jjMoveStringLiteralDfa0_0() {
		switch (curChar) {
		case 10:
			return jjStopAtPos(0, 4);
		case 33:
			return jjMoveStringLiteralDfa1_0(0x40000L);
		case 40:
			return jjStopAtPos(0, 20);
		case 41:
			return jjStopAtPos(0, 21);
		case 46:
			return jjStartNfaWithStates_0(0, 12, 19);
		case 60:
			jjmatchedKind = 13;
			return jjMoveStringLiteralDfa1_0(0x84000L);
		case 61:
			return jjStopAtPos(0, 17);
		case 62:
			jjmatchedKind = 15;
			return jjMoveStringLiteralDfa1_0(0x10000L);
		case 63:
			return jjStopAtPos(0, 22);
		case 64:
			return jjStopAtPos(0, 23);
		case 73:
			return jjMoveStringLiteralDfa1_0(0x1000000L);
		case 76:
			return jjMoveStringLiteralDfa1_0(0x2000000L);
		default:
			return jjMoveNfa_0(2, 0);
		}
	}

	private final int jjMoveStringLiteralDfa1_0(long active0) {
		try {
			curChar = input_stream.readChar();
		} catch (java.io.IOException e) {
			jjStopStringLiteralDfa_0(0, active0);
			return 1;
		}
		switch (curChar) {
		case 61:
			if ((active0 & 0x4000L) != 0L)
				return jjStopAtPos(1, 14);
			else if ((active0 & 0x10000L) != 0L)
				return jjStopAtPos(1, 16);
			else if ((active0 & 0x40000L) != 0L)
				return jjStopAtPos(1, 18);
			break;
		case 62:
			if ((active0 & 0x80000L) != 0L)
				return jjStopAtPos(1, 19);
			break;
		case 73:
			return jjMoveStringLiteralDfa2_0(active0, 0x2000000L);
		case 83:
			return jjMoveStringLiteralDfa2_0(active0, 0x1000000L);
		default:
			break;
		}
		return jjStartNfa_0(0, active0);
	}

	private final int jjMoveStringLiteralDfa2_0(long old0, long active0) {
		if (((active0 &= old0)) == 0L)
			return jjStartNfa_0(0, old0);
		try {
			curChar = input_stream.readChar();
		} catch (java.io.IOException e) {
			jjStopStringLiteralDfa_0(1, active0);
			return 2;
		}
		switch (curChar) {
		case 32:
			return jjMoveStringLiteralDfa3_0(active0, 0x1000000L);
		case 75:
			return jjMoveStringLiteralDfa3_0(active0, 0x2000000L);
		default:
			break;
		}
		return jjStartNfa_0(1, active0);
	}

	private final int jjMoveStringLiteralDfa3_0(long old0, long active0) {
		if (((active0 &= old0)) == 0L)
			return jjStartNfa_0(1, old0);
		try {
			curChar = input_stream.readChar();
		} catch (java.io.IOException e) {
			jjStopStringLiteralDfa_0(2, active0);
			return 3;
		}
		switch (curChar) {
		case 69:
			if ((active0 & 0x2000000L) != 0L)
				return jjStartNfaWithStates_0(3, 25, 47);
			break;
		case 78:
			return jjMoveStringLiteralDfa4_0(active0, 0x1000000L);
		default:
			break;
		}
		return jjStartNfa_0(2, active0);
	}

	private final int jjMoveStringLiteralDfa4_0(long old0, long active0) {
		if (((active0 &= old0)) == 0L)
			return jjStartNfa_0(2, old0);
		try {
			curChar = input_stream.readChar();
		} catch (java.io.IOException e) {
			jjStopStringLiteralDfa_0(3, active0);
			return 4;
		}
		switch (curChar) {
		case 85:
			return jjMoveStringLiteralDfa5_0(active0, 0x1000000L);
		default:
			break;
		}
		return jjStartNfa_0(3, active0);
	}

	private final int jjMoveStringLiteralDfa5_0(long old0, long active0) {
		if (((active0 &= old0)) == 0L)
			return jjStartNfa_0(3, old0);
		try {
			curChar = input_stream.readChar();
		} catch (java.io.IOException e) {
			jjStopStringLiteralDfa_0(4, active0);
			return 5;
		}
		switch (curChar) {
		case 76:
			return jjMoveStringLiteralDfa6_0(active0, 0x1000000L);
		default:
			break;
		}
		return jjStartNfa_0(4, active0);
	}

	private final int jjMoveStringLiteralDfa6_0(long old0, long active0) {
		if (((active0 &= old0)) == 0L)
			return jjStartNfa_0(4, old0);
		try {
			curChar = input_stream.readChar();
		} catch (java.io.IOException e) {
			jjStopStringLiteralDfa_0(5, active0);
			return 6;
		}
		switch (curChar) {
		case 76:
			if ((active0 & 0x1000000L) != 0L)
				return jjStopAtPos(6, 24);
			break;
		default:
			break;
		}
		return jjStartNfa_0(5, active0);
	}

	private final void jjCheckNAdd(int state) {
		if (jjrounds[state] != jjround) {
			jjstateSet[jjnewStateCnt++] = state;
			jjrounds[state] = jjround;
		}
	}

	private final void jjAddStates(int start, int end) {
		do {
			jjstateSet[jjnewStateCnt++] = jjnextStates[start];
		} while (start++ != end);
	}

	private final void jjCheckNAddTwoStates(int state1, int state2) {
		jjCheckNAdd(state1);
		jjCheckNAdd(state2);
	}

	private final void jjCheckNAddStates(int start, int end) {
		do {
			jjCheckNAdd(jjnextStates[start]);
		} while (start++ != end);
	}

	private final void jjCheckNAddStates(int start) {
		jjCheckNAdd(jjnextStates[start]);
		jjCheckNAdd(jjnextStates[start + 1]);
	}

	static final long[] jjbitVec0 = { 0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL };

	private final int jjMoveNfa_0(int startState, int curPos) {
		int[] nextStates;
		int startsAt = 0;
		jjnewStateCnt = 47;
		int i = 1;
		jjstateSet[0] = startState;
		int j, kind = 0x7fffffff;
		for (;;) {
			if (++jjround == 0x7fffffff)
				ReInitRounds();
			if (curChar < 64) {
				long l = 1L << curChar;
				MatchLoop: do {
					switch (jjstateSet[--i]) {
					case 2:
						if ((0x3ff000000000000L & l) != 0L) {
							if (kind > 8)
								kind = 8;
							jjCheckNAddStates(0, 6);
						} else if (curChar == 39)
							jjCheckNAddStates(7, 9);
						else if (curChar == 46)
							jjCheckNAdd(19);
						break;
					case 47:
					case 30:
						if ((0x3ff001800000000L & l) == 0L)
							break;
						if (kind > 26)
							kind = 26;
						jjCheckNAdd(30);
						break;
					case 18:
						if (curChar == 46)
							jjCheckNAdd(19);
						break;
					case 19:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 9)
							kind = 9;
						jjCheckNAddTwoStates(19, 20);
						break;
					case 21:
						if ((0x280000000000L & l) != 0L)
							jjCheckNAdd(22);
						break;
					case 22:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 9)
							kind = 9;
						jjCheckNAdd(22);
						break;
					case 23:
						if (curChar == 39)
							jjCheckNAddStates(7, 9);
						break;
					case 24:
						if ((0xffffff7fffffffffL & l) != 0L)
							jjCheckNAddStates(7, 9);
						break;
					case 25:
						if (curChar == 39)
							jjCheckNAddStates(10, 12);
						break;
					case 26:
						if (curChar == 39)
							jjstateSet[jjnewStateCnt++] = 25;
						break;
					case 27:
						if ((0xffffff7fffffffffL & l) != 0L)
							jjCheckNAddStates(10, 12);
						break;
					case 28:
						if (curChar == 39 && kind > 11)
							kind = 11;
						break;
					case 31:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 8)
							kind = 8;
						jjCheckNAddStates(0, 6);
						break;
					case 32:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 8)
							kind = 8;
						jjCheckNAdd(32);
						break;
					case 33:
						if ((0x3ff000000000000L & l) != 0L)
							jjCheckNAddTwoStates(33, 34);
						break;
					case 34:
						if (curChar == 46)
							jjCheckNAdd(35);
						break;
					case 35:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 9)
							kind = 9;
						jjCheckNAddTwoStates(35, 36);
						break;
					case 37:
						if ((0x280000000000L & l) != 0L)
							jjCheckNAdd(38);
						break;
					case 38:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 9)
							kind = 9;
						jjCheckNAdd(38);
						break;
					case 39:
						if ((0x3ff000000000000L & l) != 0L)
							jjCheckNAddTwoStates(39, 40);
						break;
					case 41:
						if ((0x280000000000L & l) != 0L)
							jjCheckNAdd(42);
						break;
					case 42:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 9)
							kind = 9;
						jjCheckNAdd(42);
						break;
					case 43:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 9)
							kind = 9;
						jjCheckNAddTwoStates(43, 44);
						break;
					case 45:
						if ((0x280000000000L & l) != 0L)
							jjCheckNAdd(46);
						break;
					case 46:
						if ((0x3ff000000000000L & l) == 0L)
							break;
						if (kind > 9)
							kind = 9;
						jjCheckNAdd(46);
						break;
					default:
						break;
					}
				} while (i != startsAt);
			} else if (curChar < 128) {
				long l = 1L << (curChar & 077);
				MatchLoop: do {
					switch (jjstateSet[--i]) {
					case 2:
						if ((0x7fffffe07fffffeL & l) != 0L) {
							if (kind > 26)
								kind = 26;
							jjCheckNAddTwoStates(29, 30);
						}
						if (curChar == 102)
							jjstateSet[jjnewStateCnt++] = 16;
						else if (curChar == 116)
							jjstateSet[jjnewStateCnt++] = 12;
						else if (curChar == 79)
							jjstateSet[jjnewStateCnt++] = 8;
						else if (curChar == 111)
							jjstateSet[jjnewStateCnt++] = 6;
						else if (curChar == 65)
							jjstateSet[jjnewStateCnt++] = 4;
						else if (curChar == 97)
							jjstateSet[jjnewStateCnt++] = 1;
						break;
					case 47:
						if ((0x7fffffe87fffffeL & l) != 0L) {
							if (kind > 26)
								kind = 26;
							jjCheckNAdd(30);
						}
						if ((0x7fffffe07fffffeL & l) != 0L) {
							if (kind > 26)
								kind = 26;
							jjCheckNAddTwoStates(29, 30);
						}
						break;
					case 0:
						if (curChar == 100 && kind > 5)
							kind = 5;
						break;
					case 1:
						if (curChar == 110)
							jjstateSet[jjnewStateCnt++] = 0;
						break;
					case 3:
						if (curChar == 68 && kind > 5)
							kind = 5;
						break;
					case 4:
						if (curChar == 78)
							jjstateSet[jjnewStateCnt++] = 3;
						break;
					case 5:
						if (curChar == 65)
							jjstateSet[jjnewStateCnt++] = 4;
						break;
					case 6:
						if (curChar == 114 && kind > 6)
							kind = 6;
						break;
					case 7:
						if (curChar == 111)
							jjstateSet[jjnewStateCnt++] = 6;
						break;
					case 8:
						if (curChar == 82 && kind > 6)
							kind = 6;
						break;
					case 9:
						if (curChar == 79)
							jjstateSet[jjnewStateCnt++] = 8;
						break;
					case 10:
						if (curChar == 101 && kind > 7)
							kind = 7;
						break;
					case 11:
						if (curChar == 117)
							jjCheckNAdd(10);
						break;
					case 12:
						if (curChar == 114)
							jjstateSet[jjnewStateCnt++] = 11;
						break;
					case 13:
						if (curChar == 116)
							jjstateSet[jjnewStateCnt++] = 12;
						break;
					case 14:
						if (curChar == 115)
							jjCheckNAdd(10);
						break;
					case 15:
						if (curChar == 108)
							jjstateSet[jjnewStateCnt++] = 14;
						break;
					case 16:
						if (curChar == 97)
							jjstateSet[jjnewStateCnt++] = 15;
						break;
					case 17:
						if (curChar == 102)
							jjstateSet[jjnewStateCnt++] = 16;
						break;
					case 20:
						if ((0x2000000020L & l) != 0L)
							jjAddStates(13, 14);
						break;
					case 24:
						jjCheckNAddStates(7, 9);
						break;
					case 27:
						jjCheckNAddStates(10, 12);
						break;
					case 29:
						if ((0x7fffffe07fffffeL & l) == 0L)
							break;
						if (kind > 26)
							kind = 26;
						jjCheckNAddTwoStates(29, 30);
						break;
					case 30:
						if ((0x7fffffe87fffffeL & l) == 0L)
							break;
						if (kind > 26)
							kind = 26;
						jjCheckNAdd(30);
						break;
					case 36:
						if ((0x2000000020L & l) != 0L)
							jjAddStates(15, 16);
						break;
					case 40:
						if ((0x2000000020L & l) != 0L)
							jjAddStates(17, 18);
						break;
					case 44:
						if ((0x2000000020L & l) != 0L)
							jjAddStates(19, 20);
						break;
					default:
						break;
					}
				} while (i != startsAt);
			} else {
				int i2 = (curChar & 0xff) >> 6;
				long l2 = 1L << (curChar & 077);
				MatchLoop: do {
					switch (jjstateSet[--i]) {
					case 24:
						if ((jjbitVec0[i2] & l2) != 0L)
							jjCheckNAddStates(7, 9);
						break;
					case 27:
						if ((jjbitVec0[i2] & l2) != 0L)
							jjCheckNAddStates(10, 12);
						break;
					default:
						break;
					}
				} while (i != startsAt);
			}
			if (kind != 0x7fffffff) {
				jjmatchedKind = kind;
				jjmatchedPos = curPos;
				kind = 0x7fffffff;
			}
			++curPos;
			if ((i = jjnewStateCnt) == (startsAt = 47 - (jjnewStateCnt = startsAt)))
				return curPos;
			try {
				curChar = input_stream.readChar();
			} catch (java.io.IOException e) {
				return curPos;
			}
		}
	}

	static final int[] jjnextStates = { 32, 33, 34, 39, 40, 43, 44, 24, 26, 28, 26, 27, 28, 21, 22, 37, 38, 41, 42, 45,
			46, };
	public static final String[] jjstrLiteralImages = { "", null, null, null, "\12", null, null, null, null, null,
			null, null, "\56", "\74", "\74\75", "\76", "\76\75", "\75", "\41\75", "\74\76", "\50", "\51", "\77",
			"\100", "\111\123\40\116\125\114\114", "\114\111\113\105", null, null, null, };
	public static final String[] lexStateNames = { "DEFAULT", };
	static final long[] jjtoToken = { 0x7fffbf1L, };
	static final long[] jjtoSkip = { 0xeL, };
	private SimpleCharStream input_stream;
	private final int[] jjrounds = new int[47];
	private final int[] jjstateSet = new int[94];
	protected char curChar;

	public ConstraintParserTokenManager(SimpleCharStream stream) {
		if (SimpleCharStream.staticFlag)
			throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
		input_stream = stream;
	}

	public ConstraintParserTokenManager(SimpleCharStream stream, int lexState) {
		this(stream);
		SwitchTo(lexState);
	}

	public void ReInit(SimpleCharStream stream) {
		jjmatchedPos = jjnewStateCnt = 0;
		curLexState = defaultLexState;
		input_stream = stream;
		ReInitRounds();
	}

	private final void ReInitRounds() {
		int i;
		jjround = 0x80000001;
		for (i = 47; i-- > 0;)
			jjrounds[i] = 0x80000000;
	}

	public void ReInit(SimpleCharStream stream, int lexState) {
		ReInit(stream);
		SwitchTo(lexState);
	}

	public void SwitchTo(int lexState) {
		if (lexState >= 1 || lexState < 0)
			throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.",
					TokenMgrError.INVALID_LEXICAL_STATE);
		else
			curLexState = lexState;
	}

	private final Token jjFillToken() {
		Token t = Token.newToken(jjmatchedKind);
		t.kind = jjmatchedKind;
		String im = jjstrLiteralImages[jjmatchedKind];
		t.image = (im == null) ? input_stream.GetImage() : im;
		t.beginLine = input_stream.getBeginLine();
		t.beginColumn = input_stream.getBeginColumn();
		t.endLine = input_stream.getEndLine();
		t.endColumn = input_stream.getEndColumn();
		return t;
	}

	int curLexState = 0;
	int defaultLexState = 0;
	int jjnewStateCnt;
	int jjround;
	int jjmatchedPos;
	int jjmatchedKind;

	public final Token getNextToken() {
		int kind;
		Token specialToken = null;
		Token matchedToken;
		int curPos = 0;

		EOFLoop: for (;;) {
			try {
				curChar = input_stream.BeginToken();
			} catch (java.io.IOException e) {
				jjmatchedKind = 0;
				matchedToken = jjFillToken();
				return matchedToken;
			}

			try {
				input_stream.backup(0);
				while (curChar <= 32 && (0x100002200L & (1L << curChar)) != 0L)
					curChar = input_stream.BeginToken();
			} catch (java.io.IOException e1) {
				continue EOFLoop;
			}
			jjmatchedKind = 0x7fffffff;
			jjmatchedPos = 0;
			curPos = jjMoveStringLiteralDfa0_0();
			if (jjmatchedKind != 0x7fffffff) {
				if (jjmatchedPos + 1 < curPos)
					input_stream.backup(curPos - jjmatchedPos - 1);
				if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
					matchedToken = jjFillToken();
					return matchedToken;
				} else {
					continue EOFLoop;
				}
			}
			int error_line = input_stream.getEndLine();
			int error_column = input_stream.getEndColumn();
			String error_after = null;
			boolean EOFSeen = false;
			try {
				input_stream.readChar();
				input_stream.backup(1);
			} catch (java.io.IOException e1) {
				EOFSeen = true;
				error_after = curPos <= 1 ? "" : input_stream.GetImage();
				if (curChar == '\n' || curChar == '\r') {
					error_line++;
					error_column = 0;
				} else
					error_column++;
			}
			if (!EOFSeen) {
				input_stream.backup(1);
				error_after = curPos <= 1 ? "" : input_stream.GetImage();
			}
			throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar,
					TokenMgrError.LEXICAL_ERROR);
		}
	}

}
