/*
Copyright (c) 2025  Blue Robotics

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.bluerobotics.blueberry.schema.parser.tokens;

import java.util.ArrayList;
import java.util.function.Function;

import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants.TokenIdentifier;

/**
 *
 */
public class TokenList {
	private final ArrayList<Token> m_tokens = new ArrayList<Token>();
	private int m_index = 0;
	public TokenList() {

	}


	public Token getCurrent() {
		return m_tokens.get(m_index);
	}
	public Token get(int i) {
		if(i < 0 || i >= m_tokens.size()) {
			return null;
		}
		return m_tokens.get(i);
	}
	public void clear() {
		m_tokens.clear();
	}
	public void add(Token t) {
		m_tokens.add(t);
	}
	public int size() {
		return m_tokens.size();
	}
	public void remove(Token t) {
		if(t != null) {
			remove(m_tokens.indexOf(t));
		}
	}
	public void remove(int i) {
		if(i >= 0 && i < m_tokens.size()) {
			if(i < m_index) {
				--m_index;
			}
			m_tokens.remove(i);
		}
	}

	public void add(int i, Token t) {
		m_tokens.add(i, t);
	}
	public void resetIndex() {
		m_index = 0;
	}
	/**
	 * replaced an existing token in the list with another.
	 * This only happens if the old token is actually in the list and the new token is not null
	 * @param oldT
	 * @param newT
	 */
	public void replace(Token oldT, Token newT) {
		if(newT != null) {
			int i = m_tokens.indexOf(oldT);
			if(i >= 0) {
				m_tokens.set(i, newT);
			}
		}
	}

	public Token relative(int i) {
		return m_tokens.get(m_index + i);

	}
	public boolean isAtEnd() {
		return m_index >= m_tokens.size();
	}
	public boolean isMore() {
		return m_index < m_tokens.size();
	}
	/**
	 * sets the current index to the specified token.
	 * If the token is null then this will go to the end of the list
	 * @param t
	 */
	public void setIndex(Token t) {
		if(t != null) {
			int i = m_tokens.indexOf(t);
			if(i < 0) {
				throw new RuntimeException("Specified token not in list!");
			}
			m_index = i;
		} else {
			m_index = m_tokens.size();
		}

	}
	public void next() {
		if(!isAtEnd()) {
			++m_index;
		}
	}
	public Token next(Token t) throws SchemaParserException {
		int i = m_tokens.indexOf(t);
		if(i < 0) {
			throw new SchemaParserException("Token " + t + " is not part of list.", t.getStart());
		} else {
			++i;

		}
		return get(i);
	}

	/**
	 * Finds the next token of the specified type from the current index
	 * Does test the token at the current index
	 * @param start the place to start
	 * @param forwardNotReverse
	 * @param howFar - the number of steps to look. Zero means look until the end.
	 * @param test - the test to check for
	 *
	 * @return
	 */
	private int findToken(int start, boolean forwardNotReverse, int howFar, Function<Token, Boolean> test) {
		int i  = start;
		int result = -1;
		boolean notDone = true;
		int n;
		if(forwardNotReverse) {
			if(howFar == 0) {
				n = m_tokens.size();
			} else {
				n = i + howFar;
				if(n > m_tokens.size()) {
					n = m_tokens.size();
				}
			}
		} else {
			if(howFar == 0) {
				n = 0;
			} else {
				n = i - howFar;
				if(i < 0) {
					i = 0;
				}
			}
		}


		int j = i;
		while(notDone) {
			Token t = m_tokens.get(j);
			if(test.apply(t)) {
				result = j;
				break;
			} else if(forwardNotReverse) {
				++j;
				if(j >= n) {
					notDone = false;
				}
			} else {
				--j;
				if(j < n) {
					notDone = false;
				}
			}
		}
		return result;
	}

	public Token find(Token start, boolean forwardNotReverse, Class<?>... cs) {
		Token result = null;
		int startI = m_tokens.indexOf(start);
		if(startI < 0) {
			startI = 0;
		}
		int i = findToken(startI, forwardNotReverse, 0, (t) -> {
			boolean found = false;
			for(Class<?> c : cs) {
				if(t.getClass() == c) {
					found = true;
					break;
				}

			}
			return found;
		});
		if(i >= 0) {
			result = m_tokens.get(i);
		}
		return result;
	}
	/**
	 * goes to the next token that passes the specified test
	 * if none exists then index is moved to past the last element of the list
	 * The current location is tested and will be returned if it passes.
	 * @param test
	 * @return
	 */
	private Token gotoNext(Function<Token, Boolean> test) {
		Token result = findNext(getCurrent(), test);

		setIndex(result);
		return result;
	}
	/**
	 * finds the next token that passes the specified test
	 * if none exists then returns null
	 * The current location is tested and will be returned if it passes.
	 * @param test
	 * @return
	 */
	private Token findNext(Token start, Function<Token, Boolean> test) {
		int startI = m_tokens.indexOf(start);
		if(startI < 0) {
			startI = 0;
		}
		int i = findToken(startI, true, 0, test);

		return get(i);
	}

	/**
	 * Advances to the next token of the specified sub-type
 	 * if none exists then index is moved to past the last element of the list
 	 * The current location is tested and will be returned if it passes.
	 * @param <T>
	 * @param cs
	 * @return
	 */
	public <T extends Token> T gotoNext(Class<T> cs) {
		return cs.cast(gotoNext( t -> {
			return cs == t.getClass();
		}));
	}


	/**
	 * Finds the next IdentifierToken of the specified sub-type
 	 * if none exists then returns null
 	 * The current location is tested and will be returned if it passes.
 	 * @param ti
	 * @return
	 */
	public IdentifierToken findNextId(Token start, TokenIdentifier... tis) {
		IdentifierToken it = null;
		Token t = findNext(start, (tt) -> {
			boolean result = false;
			if(tt.getClass() == IdentifierToken.class) {
				for(TokenIdentifier ti : tis) {
					if(((IdentifierToken)tt).getKeyword() == ti) {
						result = true;
						break;
					}
				}

			}
			return result;
		});
		if(t != null) {
			it = (IdentifierToken)t;
		}
		return it;
	}
	/**
	 * Advances to the next IdentifierToken of the specified sub-type
 	 * if none exists then index is moved to past the last element of the list
 	 * The current location is tested and will be returned if it passes.
 	 * @param ti
	 * @return
	 */
	public IdentifierToken gotoNextId(TokenIdentifier... tis) {
		IdentifierToken result = findNextId(getCurrent(), tis);
		setIndex(result);

		return result;
	}
	/**
	 * get the token in the position relative to the current token if it is of the specified type. Otherwise it return null.
	 * @param <T>
	 * @param i
	 * @param type
	 * @return
	 */
	public <T extends Token> T relative(int i, Class<T> type){
		T result = null;
		Token t = relative(i);
		if(t.getClass() == type) {
			result = type.cast(t);
		}
		return result;
	}
	public Token relative(Token t, int i) {
		Token result = null;
		int j = m_tokens.indexOf(t);
		if(j > 0) {
			--j;
			result = m_tokens.get(j);
		}
		return result;
	}
	public <T extends Token> T relative(Token t, int i, Class<T> type) {
		T result = null;
		Token t2 = relative(t, i);
		if(t2.getClass() == type) {
			result = type.cast(t);
		}
		return result;
	}

	/**
	 * finds the next occurance of the specified type and returns it cast to the specific type
	 * @param <T>
	 * @param start
	 * @param type
	 * @return
	 */
	public <T extends Token> T findNext(Token start, Class<T> type) {
		Token r = findNext(start, t -> {
			return t.getClass() == type;
		});

		return type.cast(r);
	}


	public IdentifierToken relativeId(int i, TokenIdentifier id) {
		IdentifierToken result = relative(i, IdentifierToken.class);
		if(result != null) {
			if(result.getKeyword() != id) {
				result = null;
			}
		}

		return result;
	}
	public String toString() {
		return getClass().getSimpleName() + "("+getCurrent()+")";
	}


	/**
	 * find the bracket that closes the current opening
	 * If the current location is not at an opening, it will move to the next complete closing or the end of the list
	 * @param it
	 * @return
	 * @throws SchemaParserException
	 */
	public IdentifierToken matchBrackets(Token t) throws SchemaParserException {
		IdentifierToken result = null;
		if(t == null) {
			t = m_tokens.get(0);
		}

		IdentifierToken it = null;
		TokenIdentifier match = null;
		if(t instanceof IdentifierToken) {
			it = (IdentifierToken)t;

			switch(it.getKeyword()) {
			case BRACE_START:
				match = TokenIdentifier.BRACE_END;
				break;
			case BRACKET_START:
				match = TokenIdentifier.BRACKET_END;
				break;
			case SQUARE_BRACKET_START:
				match = TokenIdentifier.SQUARE_BRACKET_END;
				break;
			case BRACE_END:
			case BRACKET_END:
			case SQUARE_BRACKET_END:
				throw new SchemaParserException("Somehow got one of these "+it+"without an opening one.", it.getStart());

			default:
				break;
			}
			if(match != null) {
				t = next(t);
			}
		}


		result = findNextId(t, TokenIdentifier.BRACE_START, TokenIdentifier.BRACKET_START, TokenIdentifier.SQUARE_BRACKET_START, TokenIdentifier.BRACE_END, TokenIdentifier.BRACKET_END, TokenIdentifier.SQUARE_BRACKET_END);



		if(result == null && match != null) {
			throw new SchemaParserException("Did not find a match for "+it, it.getStart());
		} else if(match == null) {
			result = matchBrackets(result);
		} else if(result != null && result.getKeyword() == match) {
				//we've got a matching close to roll back up the recursion
				//so return with this result
		} else {
			throw new SchemaParserException("Not sure what error this was "+it, it.getStart());

		}
		//don't think result can ever be null - it will have thrown an exception
		return result;

	}
	/**
	 * Returns true if the first token occurs before the second token in this list
	 * If either token is not actually in the list or either are null then returns false
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean inOrder(Token t1, Token t2) {
		if(t1 == null || t2 == null) {
			return false;
		}
		int i1 = m_tokens.indexOf(t1);
		if(i1 < 0) {
			return false;
		}
		int i2 = m_tokens.indexOf(t2);
		if(i2 < 0) {
			return false;
		}
		if(i1 < i2) {
			return true;
		}
		return false;

	}
	/**
	 * removes a range of tokens from start to end.
	 * Does not include end
	 * @param start
	 * @param end
	 */
	public void remove(Token start, Token end) {
		Token t = relative(end, -1);
		while(inOrder(start, t)) {
			Token newT = relative(t, -1);
			remove(t);
			t = newT;

		}

	}



}
