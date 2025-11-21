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

import java.util.ListIterator;
import java.util.function.Consumer;

/**
 * A token class that combines a number of other tokens
 */
public abstract class GroupToken implements Token {
	@Override
	public boolean isAfter(Token t) {
		return Token.inOrder(t, this);
	}
	private final TokenList m_children;
	
	
	protected GroupToken(TokenList ts) {
		m_children = ts;
		
	}
	protected GroupToken(Token... ts) {
		m_children = new TokenList(ts);
	}
	
	@Override
	public Coord getStart() {
		Coord result = null;
		Token t = m_children.getFirst();
		if(t != null) {
			result = t.getStart();
		}
		return result;
	}

	@Override
	public Coord getEnd() {
		Coord result = null;
		Token t = m_children.getLast();
		if(t != null) {
			result = t.getEnd();
		}
		return result;	
	}

	@Override
	public String getName() {
		String result = "";
		ListIterator<Token> ti = m_children.getIterator();
		while(ti.hasNext()) {
			Token t = ti.next();
			result += t.getName();
		}
		if(result.length() > 40) {
			result = result.substring(0, 20) + " ... " + result.substring(result.length() - 20, result.length());
		}
		return result;
	}
	/**
	 * adds a token to this group.

	 * @param ts
	 */
	public void addChild(Token ts) {
//		ListIterator<Token> ti = m_children.getIterator();
//		while(ti.hasNext()) {
//			Token tt = ti.next();
//			if(tt.isAfter(ts)){
//				int i = ti.previousIndex();
//				m_children.add(i, ts);//add before the element being tested
//				break;
//			} else if(!ti.hasNext()) {
//				m_children.add(ts);//add to the end of the list
//			}
//		}
		m_children.add(ts);
	}
	
	public void addChild(int i, Token t) {
		m_children.add(i, t);
	}
	
	public int getChildrenCount() {
		return m_children.size();
	}
	@Override
	public void consume(boolean deep, Consumer<Token> c) {
		if(deep) {
			ListIterator<Token> ts = m_children.getIterator();
			while(ts.hasNext()) {
				Token t = ts.next();
				t.consume(deep, c);
			}
		}
		c.accept(this);
	}
	
	@Override
	public String toString() {
		String result = getClass().getSimpleName();
		result += "(";
		
		if(m_children.size() > 0) {
			Coord s = m_children.getFirst().getStart();
			Coord e = m_children.getLast().getEnd();
			
			result += s.fromThisToThatString(e);
		}
		
		
		
		
		result += ")";
		return result;
	}
	
	public TokenList getChildren() {
		return m_children;
	}
	
	
	
	
	
	

}
