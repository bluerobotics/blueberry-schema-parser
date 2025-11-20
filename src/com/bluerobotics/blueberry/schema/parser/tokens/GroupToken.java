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
		m_children = new TokenList();
		ListIterator<Token> tts = ts.getIterator();
		while(tts.hasNext()) {
			addChild(tts.next());
		}
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
		// TODO Auto-generated method stub
		return null;
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
	
	
	

}
