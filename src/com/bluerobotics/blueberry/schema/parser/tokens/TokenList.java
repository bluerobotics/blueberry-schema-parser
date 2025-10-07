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
		if(i < 0 || i > m_tokens.size()) {
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
				--i;
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
	public void setIndex(Token t) {
		int i = m_tokens.indexOf(t);
		if(i < 0) {
			throw new RuntimeException("Specified token not in list!");
		}
		m_index = i;
	}
	public void next() {
		if(!isAtEnd()) {
			++m_index;
		}
	}
	
	/**
	 * Finds the next token of the specified type from the current index
	 * Does test the token at the current index
	 * @param forwardNotReverse
	 * @param howFar - the number of steps to look. Zero means look until the end.
	 * @param test - the test to check for
	 * 
	 * @return
	 */
	private int findToken(boolean forwardNotReverse, int howFar, Function<Token, Boolean> test) {
		int result = -1;
		boolean notDone = true;
		int n;
		if(forwardNotReverse) {
			if(howFar == 0) {
				n = m_tokens.size();
			} else {
				n = m_index + howFar;
				if(n > m_tokens.size()) {
					n = m_tokens.size();
				}
			}
		} else {
			if(howFar == 0) {
				n = 0;
			} else {
				n = m_index - howFar;
				if(m_index < 0) {
					m_index = 0;
				}
			}
		}
		
		
		int j = m_index;
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
	
	public Token find(boolean forwardNotReverse, Class<?>... cs) {
		Token result = null;
		int i = findToken(forwardNotReverse, 0, (t) -> {
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
		Token result = null;

		int i = findToken(true, 0, test);
		if(i >= 0) {
			result = m_tokens.get(i);
			m_index = i;
		} else {
			m_index = m_tokens.size();
		}
		return result;
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
	 * Advances to the next IdentifierToken of the specified sub-type 
 	 * if none exists then index is moved to past the last element of the list
 	 * The current location is tested and will be returned if it passes.	 
 	 * @param ti
	 * @return
	 */
	public IdentifierToken gotoNextId(TokenIdentifier ti) {
		IdentifierToken it = null;
		Token t = gotoNext((tt) -> {
			boolean result = false;
			if(tt.getClass() == IdentifierToken.class) {
				if(((IdentifierToken)tt).getKeyword() == ti) {
					result = true;
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
	public String toString() {
		return getClass().getSimpleName() + "("+getCurrent()+")";
	}
	
	
	 
	
}
