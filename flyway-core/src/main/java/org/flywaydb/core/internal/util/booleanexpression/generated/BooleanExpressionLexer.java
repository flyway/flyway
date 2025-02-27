/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
// Generated from org/flywaydb/core/internal/util/booleanexpression/generated/BooleanExpression.g4 by ANTLR 4.13.2
package org.flywaydb.core.internal.util.booleanexpression.generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class BooleanExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, OR=2, TRUE=3, FALSE=4, EQUAL=5, NOT_EQUAL=6, LEFT_PAREN=7, RIGHT_PAREN=8, 
		WORD=9, WHITESPACE=10;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"AND", "OR", "TRUE", "FALSE", "EQUAL", "NOT_EQUAL", "LEFT_PAREN", "RIGHT_PAREN", 
			"WORD", "WHITESPACE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'&&'", "'||'", null, null, "'=='", "'!='", "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "AND", "OR", "TRUE", "FALSE", "EQUAL", "NOT_EQUAL", "LEFT_PAREN", 
			"RIGHT_PAREN", "WORD", "WHITESPACE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public BooleanExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "BooleanExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\n<\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\b\u0004\b2\b\b\u000b\b\f\b3\u0001\t\u0004\t7\b\t\u000b\t"+
		"\f\t8\u0001\t\u0001\t\u0000\u0000\n\u0001\u0001\u0003\u0002\u0005\u0003"+
		"\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0001"+
		"\u0000\n\u0002\u0000TTtt\u0002\u0000RRrr\u0002\u0000UUuu\u0002\u0000E"+
		"Eee\u0002\u0000FFff\u0002\u0000AAaa\u0002\u0000LLll\u0002\u0000SSss\u000e"+
		"\u0000\t\r !&&()==||\u0085\u0085\u00a0\u00a0\u1680\u1680\u2000\u200a\u2028"+
		"\u2029\u202f\u202f\u205f\u205f\u3000\u3000\n\u0000\t\r  \u0085\u0085\u00a0"+
		"\u00a0\u1680\u1680\u2000\u200a\u2028\u2029\u202f\u202f\u205f\u205f\u3000"+
		"\u3000=\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000"+
		"\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000"+
		"\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000"+
		"\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000"+
		"\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0001"+
		"\u0015\u0001\u0000\u0000\u0000\u0003\u0018\u0001\u0000\u0000\u0000\u0005"+
		"\u001b\u0001\u0000\u0000\u0000\u0007 \u0001\u0000\u0000\u0000\t&\u0001"+
		"\u0000\u0000\u0000\u000b)\u0001\u0000\u0000\u0000\r,\u0001\u0000\u0000"+
		"\u0000\u000f.\u0001\u0000\u0000\u0000\u00111\u0001\u0000\u0000\u0000\u0013"+
		"6\u0001\u0000\u0000\u0000\u0015\u0016\u0005&\u0000\u0000\u0016\u0017\u0005"+
		"&\u0000\u0000\u0017\u0002\u0001\u0000\u0000\u0000\u0018\u0019\u0005|\u0000"+
		"\u0000\u0019\u001a\u0005|\u0000\u0000\u001a\u0004\u0001\u0000\u0000\u0000"+
		"\u001b\u001c\u0007\u0000\u0000\u0000\u001c\u001d\u0007\u0001\u0000\u0000"+
		"\u001d\u001e\u0007\u0002\u0000\u0000\u001e\u001f\u0007\u0003\u0000\u0000"+
		"\u001f\u0006\u0001\u0000\u0000\u0000 !\u0007\u0004\u0000\u0000!\"\u0007"+
		"\u0005\u0000\u0000\"#\u0007\u0006\u0000\u0000#$\u0007\u0007\u0000\u0000"+
		"$%\u0007\u0003\u0000\u0000%\b\u0001\u0000\u0000\u0000&\'\u0005=\u0000"+
		"\u0000\'(\u0005=\u0000\u0000(\n\u0001\u0000\u0000\u0000)*\u0005!\u0000"+
		"\u0000*+\u0005=\u0000\u0000+\f\u0001\u0000\u0000\u0000,-\u0005(\u0000"+
		"\u0000-\u000e\u0001\u0000\u0000\u0000./\u0005)\u0000\u0000/\u0010\u0001"+
		"\u0000\u0000\u000002\b\b\u0000\u000010\u0001\u0000\u0000\u000023\u0001"+
		"\u0000\u0000\u000031\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u0000"+
		"4\u0012\u0001\u0000\u0000\u000057\u0007\t\u0000\u000065\u0001\u0000\u0000"+
		"\u000078\u0001\u0000\u0000\u000086\u0001\u0000\u0000\u000089\u0001\u0000"+
		"\u0000\u00009:\u0001\u0000\u0000\u0000:;\u0006\t\u0000\u0000;\u0014\u0001"+
		"\u0000\u0000\u0000\u0003\u000038\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
